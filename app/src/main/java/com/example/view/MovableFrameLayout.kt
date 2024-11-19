package com.example.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class MovableFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var lastX = 0f
    private var lastY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX
                lastY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - lastX
                val dy = event.rawY - lastY

                val layoutParams = layoutParams as MarginLayoutParams
                layoutParams.leftMargin += dx.toInt()
                layoutParams.topMargin += dy.toInt()
                layoutParams.rightMargin -= dx.toInt()
                layoutParams.bottomMargin -= dy.toInt()
                this.layoutParams = layoutParams

                lastX = event.rawX
                lastY = event.rawY
            }
        }
        return true
    }
}