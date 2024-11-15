package com.example.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.example.gpengl.multiple.CoordinateRegion
import com.example.rander.MultipleBackgroundCombineRender

class MultipleCombineBackgroundGlSurface(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs) {

    private var multipleRender: MultipleBackgroundCombineRender

    init {
        setEGLContextClientVersion(2)
        multipleRender = MultipleBackgroundCombineRender(this)
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

    fun release() {
        multipleRender.release()
    }


}