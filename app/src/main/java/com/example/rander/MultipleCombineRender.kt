package com.example.rander

import android.opengl.EGL14
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import com.example.gpengl.multiple.CoordinateRegion
import com.example.gpengl.multiple.FboCombineTexture
import com.example.gpengl.multiple.IBaseTexture
import com.example.gpengl.multiple.PicTexture
import com.example.gpengl.multiple.PicTextureT
import com.example.gpengl.multiple.generateBitmapTexture
import com.example.gpengl.multiple.generateCoordinateRegion
import com.example.gpengl.multiple.getHeight
import com.example.gpengl.multiple.getWidth
import com.example.gpengl.multiple.offSet
import com.example.gpengl.third.record.MediaRecorder
import java.io.IOException
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MultipleCombineRender(private var surfaceView: GLSurfaceView) : GLSurfaceView.Renderer {


    private var combineTexture = FboCombineTexture(surfaceView.context)
    private var baseTextureList = listOf<IBaseTexture>(
        PicTextureT(surfaceView.context),
        PicTexture(surfaceView.context),
        PicTexture(surfaceView.context),
        PicTextureT(surfaceView.context),
        PicTextureT(surfaceView.context),
        PicTexture(surfaceView.context),
    )

    private var mMediaRecorder: MediaRecorder? = null


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.96f, 0.8f, 0.156f, 1.0f)
        combineTexture.onSurfaceCreated()
        baseTextureList.forEach {
            it.onSurfaceCreated()
        }
        mMediaRecorder =
            MediaRecorder(surfaceView.context, surfaceView.width, surfaceView.height, EGL14.eglGetCurrentContext())

    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        combineTexture.onSurfaceChanged(width, height)
        baseTextureList.forEach {
            it.onSurfaceChanged(width, height)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        // 使用着色器程序

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, combineTexture.getFboFrameBuffer()[0])
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        baseTextureList.forEach {
            it.onDrawFrame()
        }

        Log.e("ybb", "onDrawFrame")

        combineTexture.onDrawFrame()
        //进行录制
        mMediaRecorder?.encodeFrame(combineTexture.getTextureArray()[0], System.nanoTime())
    }

    fun updateTexCord(coordinateRegion: CoordinateRegion) {
        baseTextureList.forEachIndexed { index, iBaseTexture ->
            when (index) {
                2 -> {
                    iBaseTexture.updateTexCord(
                        coordinateRegion.offSet(
                            0f,
                            coordinateRegion.getHeight() + 50f
                        )
                    )
                }

                3 -> {
                    iBaseTexture.updateTexCord(
                        coordinateRegion.offSet(
                            0f,
                            coordinateRegion.getHeight() * 2 + 100f
                        )
                    )
                }

                0 -> {
                    iBaseTexture.updateTexCord(coordinateRegion)
                }

                4, 5 -> {
                    iBaseTexture.updateTexCord(
                        CoordinateRegion().generateCoordinateRegion(
                            (iBaseTexture.getScreenWidth() - coordinateRegion.getWidth() - 200),
                            200f,
                            coordinateRegion.getWidth().toInt(),
                            coordinateRegion.getHeight().toInt()
                        )
                    )
                }

                else -> {
                    iBaseTexture.updateTexCord(coordinateRegion)
                }
            }

        }
    }


    fun loadTexture(resourceId: Int) {
        baseTextureList.forEach {
            it.updateTextureInfo(
                it.getTextureInfo().generateBitmapTexture(
                    it.getTextureInfo().textureId, surfaceView.context, resourceId
                ), true
            )
        }
    }

    fun startRecord() {
        try {
            mMediaRecorder?.start(1f)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopRecord() {
        mMediaRecorder?.stop()
    }


}