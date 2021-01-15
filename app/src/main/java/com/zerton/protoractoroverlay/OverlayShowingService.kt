package com.zerton.protoractoroverlay

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import kotlin.math.abs

class OverlayShowingService : Service(), OnTouchListener {

    private var mode = NONE

    private lateinit var wm: WindowManager



    private lateinit var closeView: ImageView
    private lateinit var imageView: ImageView

    private var offsetX = 0f
    private var offsetY = 0f
    private var originalXPos = 0
    private var originalYPos = 0


    private val layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_FULLSCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                        WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT)

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        wm = getSystemService(WINDOW_SERVICE) as WindowManager

        imageView = ImageView(this)
        imageView.setOnTouchListener(this)
        imageView.adjustViewBounds = true
        imageView.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.protaractor, null))

        val params = layoutParams
        params.gravity = Gravity.START or Gravity.TOP
        params.x = dpToPx(48)
        params.y = dpToPx(48)

        wm.addView(imageView, params)

        closeView = ImageView(this)
        closeView.adjustViewBounds = true
        closeView.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_close, null))
        closeView.setOnClickListener { stopSelf() }
        wm.addView(closeView, params)
    }


    override fun onDestroy() {
        super.onDestroy()
            wm.removeView(imageView)
            wm.removeView(closeView)

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val x = event.rawX
                val y = event.rawY
                mode = NONE
                val location = IntArray(2)
                imageView.getLocationOnScreen(location)
                originalXPos = location[0]
                originalYPos = location[1]
                offsetX = originalXPos - x
                offsetY = originalYPos - y
            }

            MotionEvent.ACTION_MOVE -> {
                val topLeftLocationOnScreen = IntArray(2)
                closeView.getLocationOnScreen(topLeftLocationOnScreen)
                println("topLeftY=" + topLeftLocationOnScreen[1])
                println("originalY=$originalYPos")
                val x = event.rawX
                val y = event.rawY
                val params = imageView.layoutParams as WindowManager.LayoutParams
                val newX = (offsetX + x).toInt()
                val newY = (offsetY + y).toInt()
                if (abs(newX - originalXPos) < 1 && abs(newY - originalYPos) < 1 && mode == NONE) {
                    return false
                }
                params.x = newX - topLeftLocationOnScreen[0]
                params.y = newY - topLeftLocationOnScreen[1]
                wm.updateViewLayout(imageView, params)
                mode = DRAG
            }
            MotionEvent.ACTION_UP -> {
                if (mode == DRAG) {
                    return true
                }
            }
        }
        return false
    }


    private fun dpToPx(dp: Int): Int {
        val displayMetrics = resources.displayMetrics
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
    }
}