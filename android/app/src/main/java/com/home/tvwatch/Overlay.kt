package com.home.tvwatch

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.view.Gravity
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/** Full-width banner drawn over whatever app is playing. Main thread only. */
class Overlay(private val ctx: Context) {
    private val wm = ctx.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val main = Handler(Looper.getMainLooper())
    private var view: FrameLayout? = null
    private val dismiss = Runnable { hide() }

    fun show(text: String, imageB64: String?, seconds: Int) {
        hide()
        val col = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(60, 40, 60, 40)
            setBackgroundColor(Color.argb(235, 20, 24, 32))
        }
        if (imageB64 != null) {
            val bytes = Base64.decode(imageB64, Base64.DEFAULT)
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bmp != null) col.addView(ImageView(ctx).apply {
                setImageBitmap(bmp); adjustViewBounds = true; maxHeight = 600
            })
        }
        if (text.isNotBlank()) col.addView(TextView(ctx).apply {
            this.text = text; textSize = 34f; setTextColor(Color.WHITE); gravity = Gravity.CENTER
        })
        val root = FrameLayout(ctx).apply { addView(col) }
        val type = if (Build.VERSION.SDK_INT >= 26) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                   else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        ).apply { gravity = Gravity.TOP }
        wm.addView(root, lp)
        view = root
        main.postDelayed(dismiss, seconds * 1000L)
    }

    fun hide() {
        main.removeCallbacks(dismiss)
        view?.let { runCatching { wm.removeView(it) } }
        view = null
    }
}
