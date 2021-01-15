package com.zerton.protoractoroverlay

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!Settings.canDrawOverlays(this)) {
            val intentSettings = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            startActivityForResult(intentSettings, 1)
        }

        val svc = Intent(this, OverlayShowingService::class.java)
        startService(svc)
        finish()
    }
}