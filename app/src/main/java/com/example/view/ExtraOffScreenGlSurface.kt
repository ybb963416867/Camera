package com.example.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PixelFormat
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.FrameLayout
import com.example.gpengl.multiple.CoordinateRegion
import com.example.gpengl.multiple.IBaseTexture
import com.example.rander.ExtraOffScreenRender

class ExtraOffScreenGlSurface(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs) {

    private var multipleRender: ExtraOffScreenRender
    private var currentActiveTexture: IBaseTexture? = null

    init {
        setEGLContextClientVersion(2)
        multipleRender = ExtraOffScreenRender(this)
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

                if (currentActiveTexture == null){
                    Log.e("baseOesTexture", "currentActiveTexture == null")
                }else {
                    Log.e("baseOesTexture", "currentActiveTexture != null")
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

    fun setRecodeView(root: FrameLayout, viewWidth: Int, viewHeight: Int) {
        multipleRender.setRecodeView(root, viewWidth, viewHeight)
    }

    fun capture1() {
        multipleRender.capture1()
    }

    fun capture2() {
        multipleRender.capture2()
    }

    fun update(){
        multipleRender.update()
    }

    fun release(){
        multipleRender.release()
    }

}





