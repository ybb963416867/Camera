package com.example.rander

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.example.gpengl.multiple.CoordinateRegion
import com.example.gpengl.multiple.IBaseTexture
import com.example.gpengl.multiple.PicTextureT
import com.example.gpengl.multiple.generateBitmapTexture
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MultipleCombineRender(private var surfaceView: GLSurfaceView) : GLSurfaceView.Renderer {


    private var baseTextureList = listOf<IBaseTexture>(
        PicTextureT(surfaceView.context),
//        PicTexture(surfaceView.context),
//        PicTexture(surfaceView.context),
//        PicTextureT(surfaceView.context),
//        PicTextureT(surfaceView.context),
//        PicTexture(surfaceView.context),
    )


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.96f, 0.8f, 0.156f, 1.0f)

        baseTextureList.forEach {
            it.onSurfaceCreated()
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        baseTextureList.forEach {
            it.onSurfaceChanged(width, height)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        // 使用着色器程序
        baseTextureList.forEach {
            it.onDrawFrame()
        }

    }

    fun updateTexCord(coordinateRegion: CoordinateRegion) {
        baseTextureList.forEach {
            it.updateTexCord(coordinateRegion)
        }
    }


    fun loadTexture(resourceId: Int) {
        baseTextureList.forEach {
            it.updateTextureInfo(it.getTextureInfo().generateBitmapTexture(it.getTextureInfo().textureId, surfaceView.context, resourceId), true)
        }
    }

}