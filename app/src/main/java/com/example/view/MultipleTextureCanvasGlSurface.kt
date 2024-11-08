package com.example.view

import android.content.Context
import android.util.AttributeSet
import com.chillingvan.canvasgl.ICanvasGL
import com.example.gpengl.multiple.CoordinateRegion
import com.example.rander.MultipleRender
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MultipleTextureCanvasGlSurface(
    context: Context,
    attrs: AttributeSet? = null,
) : BaseGlCanvasView(context, attrs) {

    private var multipleRender: MultipleRender
    init {
        setEGLContextClientVersion(2)
        multipleRender = MultipleRender(this)
        setRenderer(this)
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

    override fun onDrawFrame(gl: GL10?) {
        super.onDrawFrame(gl)
        multipleRender.onDrawFrame(gl)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        super.onSurfaceCreated(gl, config)
        multipleRender.onSurfaceCreated(gl, config)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        super.onSurfaceChanged(gl, width, height)
        multipleRender.onSurfaceChanged(gl, width, height)
    }

    override fun onGLDraw(canvas: ICanvasGL) {
        super.onGLDraw(canvas)
    }

}