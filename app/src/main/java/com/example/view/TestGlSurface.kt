package com.example.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.example.gpengl.multiple.CoordinateRegion

class TestGlSurface(
    context: Context,
    attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs) {
    private var multipleRender: TextRender

    init {
        setEGLContextClientVersion(2)
        multipleRender = TextRender(this)
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

    fun release() {
        multipleRender.release()
    }
}