package com.home.tvwatch

import android.Manifest
import android.app.Activity
import android.app.AppOpsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.widget.TextView

/** One-time setup screen: grants permissions, starts the service. */
class MainActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        status = TextView(this).apply { textSize = 28f; setPadding(80, 80, 80, 80) }
        setContentView(status)
    }

    override fun onResume() {
        super.onResume()
        when {
            !hasUsageAccess() -> {
                status.text = "Step 1/3: enable TV Watch under Usage access, then press Back."
                startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
            }
            !Settings.canDrawOverlays(this) -> {
                status.text = "Step 2/3: allow Display over other apps for TV Watch, then press Back."
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            }
            checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED -> {
                status.text = "Step 3/3: allow camera access."
                requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
            }
            else -> {
                startForegroundService(Intent(this, MonitorService::class.java))
                status.text = "TV Watch is running.\nYou can close this screen."
            }
        }
    }

    override fun onRequestPermissionsResult(r: Int, p: Array<out String>, g: IntArray) = onResume()

    private fun hasUsageAccess(): Boolean {
        val ops = getSystemService(APP_OPS_SERVICE) as AppOpsManager
        return ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName) ==
            AppOpsManager.MODE_ALLOWED
    }
}
