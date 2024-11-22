package com.example.rander

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.widget.FrameLayout
import com.example.gpengl.multiple.OffScreenViewTexture
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyRender(private val glSurfaceView: GLSurfaceView): GLSurfaceView.Renderer {

    private var offScreenViewTexture = OffScreenViewTexture<FrameLayout>(glSurfaceView)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        offScreenViewTexture.onSurfaceCreated()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        offScreenViewTexture.onSurfaceChanged(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        // バッファークリア
        GLES30.glClearColor(0.0f, 1.0f, 0.0f, 1.0f)
        offScreenViewTexture.onDrawFrame()
    }

    fun setViewInfo(rootView: FrameLayout, viewWidth: Int, viewHeight: Int){
        offScreenViewTexture.setViewInfo(rootView, viewWidth, viewHeight)
    }

    fun updateViewTexture(){
        offScreenViewTexture.updateViewTexture()
    }
}