package com.example.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import com.example.gpengl.multiple.CoordinateRegion
import com.example.gpengl.multiple.IBaseTexture
import com.example.rander.MultipleBackgroundCombineTouchRender

class MultipleCombineBackgroundTouchGlSurface(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs) {

    private var multipleRender: MultipleBackgroundCombineTouchRender
    private var currentActiveTexture: IBaseTexture? = null

    init {
        setEGLContextClientVersion(2)
        multipleRender = MultipleBackgroundCombineTouchRender(this)
        setRenderer(multipleRender)
        renderMode = RENDERMODE_WHEN_DIRTY
        holder.setFormat(PixelFormat.TRANSLUCENT)
    }

    fun loadTexture(resourceId: Int) {
        queueEvent {
            multipleRender.loadTexture(resourceId)
            requestRender()
        }

    }

    fun updateTexCord(coordinateRegion: CoordinateRegion) {
        multipleRender.updateTexCord(coordinateRegion)
        requestRender()
    }

    fun startRecord() {
        multipleRender.startRecord()
        queueEvent {
            renderMode = RENDERMODE_CONTINUOUSLY
        }
    }

    fun stopRecord() {
        multipleRender.stopRecord()
        queueEvent {
            renderMode = RENDERMODE_WHEN_DIRTY
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentActiveTexture =
                    multipleRender.baseTextureList.lastOrNull { it.acceptTouchEvent(event) }
                currentActiveTexture?.let {
                    multipleRender.baseTextureList.remove(it)
                    multipleRender.baseTextureList.add(it)
                    requestRender()
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                currentActiveTexture?.acceptTouchEvent(event)
                currentActiveTexture = null
            }
        }

        currentActiveTexture?.let {
            it.onTouch(it, event)
        }

        return true
    }

    fun release() {
        multipleRender.release()
    }

}