package com.example.view

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import com.chillingvan.canvasgl.CanvasGL
import com.chillingvan.canvasgl.ICanvasGL
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

open class BaseGlCanvasView(context: Context, attrs: AttributeSet? = null) :
    GLSurfaceView(context, attrs), GLSurfaceView.Renderer {
    private lateinit var mCanvasGL: CanvasGL
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        mCanvasGL = CanvasGL()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        mCanvasGL.setSize(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
    }

    open fun onGLDraw(canvas: ICanvasGL) {
        onGLDraw(mCanvasGL)
    }
}