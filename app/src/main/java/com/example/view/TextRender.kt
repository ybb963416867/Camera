package com.example.view

import android.opengl.GLSurfaceView
import com.example.gpengl.multiple.CoordinateRegion
import com.example.gpengl.multiple.PicTexture
import com.example.gpengl.multiple.generateBitmapTexture
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class TextRender(private var surfaceView: GLSurfaceView) : GLSurfaceView.Renderer {

    private var picTexture = PicTexture(surfaceView)
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        picTexture.onSurfaceCreated()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        picTexture.onSurfaceChanged(width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        picTexture.onDrawFrame()
    }

    fun loadTexture(resourceId: Int) {
        picTexture.updateTextureInfo(
            picTexture.getTextureInfo().generateBitmapTexture(surfaceView.context, resourceId), false
        )
    }

    fun updateTexCord(coordinateRegion: CoordinateRegion) {
        picTexture.updateTexCord(coordinateRegion)
    }

    fun release() {
        picTexture.release()
    }
}