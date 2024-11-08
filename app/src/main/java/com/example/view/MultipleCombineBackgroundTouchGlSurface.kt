package com.example.view

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.example.gpengl.multiple.CoordinateRegion
import com.example.rander.MultipleBackgroundCombineTouchRender

class MultipleCombineBackgroundTouchGlSurface(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs) {

    private var multipleRender: MultipleBackgroundCombineTouchRender

    init {
        setEGLContextClientVersion(2)
        multipleRender = MultipleBackgroundCombineTouchRender(this)
        setRenderer(multipleRender)
        renderMode = RENDERMODE_WHEN_DIRTY
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

    fun startRecord(){
        multipleRender.startRecord()
        queueEvent {
            renderMode = RENDERMODE_CONTINUOUSLY
        }
    }

    fun stopRecord(){
        multipleRender.stopRecord()
        queueEvent {
            renderMode = RENDERMODE_WHEN_DIRTY
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        multipleRender.baseTextureList.forEach {
            if (it.onTouch(it, event)){
                return@forEach
            }
        }
        return true
    }



}