package com.home.tvwatch

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import okhttp3.*
import okio.ByteString.Companion.toByteString
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Foreground service. Every few seconds it works out which app is on screen,
 * reports finished viewing sessions to the relay, enforces the daily limit,
 * and reacts to commands (message / limit / camera) from the relay.
 */
class MonitorService : Service() {
    private val main = Handler(Looper.getMainLooper())
    private val http = OkHttpClient.Builder().pingInterval(25, TimeUnit.SECONDS).build()
    private val prefs by lazy { getSharedPreferences("tvwatch", MODE_PRIVATE) }
    private val overlay by lazy { Overlay(this) }
    private var socket: WebSocket? = null
    private var connected = false
    private val pending = ArrayDeque<String>()      // events queued while offline

    private var camera: CameraStreamer? = null
    private var lastEventTime = 0L
    private var fgPkg: String? = null               // last app reported by usage events
    private var curPkg: String? = null              // app currently being counted
    private var curStart = 0L
    private var limitMin = 0
    private var warned = false
    private lateinit var homePkgs: Set<String>

    override fun onBind(i: Intent?): IBinder? = null

    override fun onStartCommand(i: Intent?, flags: Int, id: Int): Int {
        if (socket == null) {
            startForeground(1, notification())
            homePkgs = packageManager.queryIntentActivities(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0
            ).map { it.activityInfo.packageName }.toSet() + packageName
            limitMin = prefs.getInt("limit", 0)
            lastEventTime = System.currentTimeMillis() - 2 * 3600_000L
            connect()
            main.post(tick)
        }
        return START_STICKY
    }

    private val tick = object : Runnable {
        override fun run() {
            runCatching { poll() }
            main.postDelayed(this, 3000)
        }
    }

    // ---- usage tracking ----------------------------------------------------

    private fun poll() {
        val now = System.currentTimeMillis()
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val ev = usm.queryEvents(lastEventTime, now)
        val e = UsageEvents.Event()
        while (ev.hasNextEvent()) {
            ev.getNextEvent(e)
            lastEventTime = maxOf(lastEventTime, e.timeStamp + 1)
            when (e.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> fgPkg = e.packageName
                UsageEvents.Event.ACTIVITY_PAUSED -> if (fgPkg == e.packageName) fgPkg = null
            }
        }
        val screenOn = (getSystemService(Context.POWER_SERVICE) as PowerManager).isInteractive
        val want = fgPkg?.takeIf { screenOn && it !in homePkgs }

        if (want != curPkg) {
            closeSession(now)
            curPkg = want
            curStart = now
            send(JSONObject().put("type", "now").put("pkg", want ?: JSONObject.NULL)
                .put("label", want?.let(::label) ?: JSONObject.NULL).put("since", now))
        }
        enforceLimit(now)
    }

    private fun closeSession(now: Long) {
        val p = curPkg ?: return
        if (now - curStart < 5000) return                   // ignore flicker
        addToday(now - curStart)
        send(JSONObject().put("type", "session").put("pkg", p).put("label", label(p))
            .put("start", curStart).put("end", now))
    }

    private fun label(pkg: String): String = try {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    } catch (_: PackageManager.NameNotFoundException) { pkg }

    // ---- daily limit -------------------------------------------------------

    private fun today() = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())

    private fun todayMs(): Long =
        if (prefs.getString("day", "") == today()) prefs.getLong("ms", 0) else 0

    private fun addToday(ms: Long) =
        prefs.edit().putString("day", today()).putLong("ms", todayMs() + ms).apply()

    private fun enforceLimit(now: Long) {
        if (limitMin <= 0) { warned = false; return }
        val used = todayMs() + if (curPkg != null) now - curStart else 0
        val left = limitMin * 60_000L - used
        if (curPkg == null) return
        if (left <= 0) {
            overlay.show("Daily TV time is over. Time to do something else!", null, 8)
            startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } else if (left <= 5 * 60_000L && !warned) {
            warned = true
            overlay.show("5 minutes of TV time left today", null, 10)
        } else if (left > 5 * 60_000L) warned = false
    }

    // ---- relay connection --------------------------------------------------

    private fun connect() {
        val req = Request.Builder().url("${BuildConfig.RELAY_URL}/tv?token=${BuildConfig.TV_TOKEN}").build()
        socket = http.newWebSocket(req, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, r: Response) {
                connected = true
                main.post {
                    while (pending.isNotEmpty()) ws.send(pending.removeFirst())
                    curPkg?.let { p -> send(JSONObject().put("type", "now").put("pkg", p).put("label", label(p)).put("since", curStart)) }
                }
            }
            override fun onMessage(ws: WebSocket, text: String) { main.post { handle(JSONObject(text)) } }
            override fun onClosed(ws: WebSocket, c: Int, r: String) = retry()
            override fun onFailure(ws: WebSocket, t: Throwable, r: Response?) = retry()
        })
    }

    private fun retry() {
        connected = false
        camera?.stop(); camera = null
        main.postDelayed({ connect() }, 5000)
    }

    private fun send(o: JSONObject) {
        val s = o.toString()
        if (connected) socket?.send(s) else if (pending.size < 500) pending.addLast(s)
    }

    private fun handle(m: JSONObject) {
        when (m.getString("type")) {
            "message" -> overlay.show(m.optString("text"), m.optString("image").ifEmpty { null }, m.optInt("seconds", 15))
            "limit" -> { limitMin = m.getInt("minutes"); prefs.edit().putInt("limit", limitMin).apply() }
            "camera" -> if (m.getBoolean("on")) startCamera() else { camera?.stop(); camera = null }
        }
    }

    private fun startCamera() {
        if (camera != null) return
        camera = CameraStreamer(this,
            onFrame = { jpg -> if (connected) socket?.send(jpg.toByteString()) },
            onStatus = { ok, err ->
                send(JSONObject().put("type", "camera-status").put("ok", ok).put("error", err ?: JSONObject.NULL))
                if (!ok) main.post { camera = null }
            })
        runCatching { camera?.start() }.onFailure {
            send(JSONObject().put("type", "camera-status").put("ok", false).put("error", it.message))
            camera = null
        }
    }

    // ---- misc --------------------------------------------------------------

    private fun notification(): Notification {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("tvwatch", "TV Watch", NotificationManager.IMPORTANCE_MIN))
        return Notification.Builder(this, "tvwatch").setContentTitle("TV Watch running")
            .setSmallIcon(android.R.drawable.ic_menu_view).build()
    }

    override fun onDestroy() {
        main.removeCallbacksAndMessages(null)
        closeSession(System.currentTimeMillis())
        camera?.stop(); socket?.close(1000, null); overlay.hide()
    }
}
