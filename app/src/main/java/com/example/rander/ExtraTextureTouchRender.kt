package com.example.rander

import android.graphics.Bitmap
import android.opengl.EGL14
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import android.widget.FrameLayout
import com.example.gpengl.multiple.CoordinateRegion
import com.example.gpengl.multiple.IBaseTexture
import com.example.gpengl.multiple.PicBackgroundTexture
import com.example.gpengl.multiple.PicBackgroundTexture1
import com.example.gpengl.multiple.PicBackgroundTexture2
import com.example.gpengl.multiple.PicBackgroundTextureT
import com.example.gpengl.multiple.MultipleFboCombineTexture
import com.example.gpengl.multiple.ViewBackgroundTexture
import com.example.gpengl.multiple.generateBitmapTexture
import com.example.gpengl.multiple.generateCoordinateRegion
import com.example.gpengl.multiple.getHeight
import com.example.gpengl.multiple.getWidth
import com.example.gpengl.multiple.offSet
import com.example.gpengl.third.record.MediaRecorder
import com.example.util.FileUtils
import com.example.util.Gl2Utils
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.CopyOnWriteArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ExtraTextureTouchRender(private var surfaceView: GLSurfaceView) :
    GLSurfaceView.Renderer {

    private var combineTexture = MultipleFboCombineTexture(2, surfaceView.context)

    private var pic1 = "PicBackgroundTextureT" to PicBackgroundTextureT(surfaceView)
    private var pic2 = "PicBackgroundTexture" to PicBackgroundTexture(surfaceView)
    private var pic3 = "PicBackgroundTexture1" to PicBackgroundTexture1(surfaceView)
    private var pic4 = "PicBackgroundTexture2" to PicBackgroundTexture2(surfaceView)
    private var pic5 = "ViewTexture" to ViewBackgroundTexture(surfaceView)

    private var baseTextureList1 = mapOf<String, IBaseTexture>(
        pic1,
        pic2,
        pic3,
        pic4,
        pic5,
    )

    var baseTextureList = CopyOnWriteArrayList(
        baseTextureList1.values
    )

    private var mMediaRecorder: MediaRecorder? = null


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.96f, 0.8f, 0.156f, 1.0f)
        combineTexture.onSurfaceCreated(surfaceView.width, surfaceView.height)
        baseTextureList.forEach {
            it.onSurfaceCreated()
        }
        mMediaRecorder = MediaRecorder(
            surfaceView.context, surfaceView.width, surfaceView.height, EGL14.eglGetCurrentContext()
        )

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
            if (it != pic5.second) {
                it.onDrawFrame()
            }
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, combineTexture.getTextureArray()[1])
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)



        baseTextureList.forEach {
            it.onDrawFrame()
        }

        combineTexture.onDrawFrame(0)
        //进行录制
        mMediaRecorder?.encodeFrame(combineTexture.getTextureArray()[1], System.nanoTime())
    }

    fun updateTexCord(coordinateRegion: CoordinateRegion) {
        baseTextureList.forEachIndexed { index, iBaseTexture ->
            if (iBaseTexture != pic5.second) {
                when (index) {
                    1 -> {
                        iBaseTexture.updateTexCord(
                            CoordinateRegion().generateCoordinateRegion(
                                (iBaseTexture.getScreenWidth() - coordinateRegion.getWidth() - 200),
                                200f,
                                coordinateRegion.getWidth().toInt(),
                                coordinateRegion.getHeight().toInt()
                            )
                        )
                    }

                    2 -> {
                        iBaseTexture.updateTexCord(
                            coordinateRegion.offSet(
                                0f, coordinateRegion.getHeight() + 50f
                            )
                        )
                    }

                    3 -> {
                        iBaseTexture.updateTexCord(
                            coordinateRegion.offSet(
                                0f, coordinateRegion.getHeight() * 2 + 100f
                            )
                        )
                    }

                    else -> {
                        iBaseTexture.updateTexCord(coordinateRegion)
                    }
                }
            }
        }
    }


    fun loadTexture(resourceId: Int) {

        baseTextureList.forEachIndexed { index, iBaseTexture ->

            if (iBaseTexture != pic5.second) {
                when (index) {
                    0 -> iBaseTexture.updateTextureInfo(
                        iBaseTexture.getTextureInfo().generateBitmapTexture(
                            iBaseTexture.getTextureInfo().textureId, surfaceView.context, resourceId
                        ), true
                    )

                    1 -> iBaseTexture.updateTextureInfo(
                        iBaseTexture.getTextureInfo().generateBitmapTexture(
                            iBaseTexture.getTextureInfo().textureId, surfaceView.context, resourceId
                        ), false, "#80A728F0"
                    )

                    2 -> iBaseTexture.updateTextureInfo(
                        iBaseTexture.getTextureInfo().generateBitmapTexture(
                            iBaseTexture.getTextureInfo().textureId, surfaceView.context, resourceId
                        ), false, "#3872F0"
                    )

                    else -> {
                        iBaseTexture.updateTextureInfo(
                            iBaseTexture.getTextureInfo().generateBitmapTexture(
                                iBaseTexture.getTextureInfo().textureId,
                                surfaceView.context,
                                resourceId
                            ), false, "#4D000000"
                        )
                    }
                }
            }

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

    fun setRecodeView(root: FrameLayout, viewWidth: Int, viewHeight: Int) {
        pic5.second.setViewInfo(root, viewWidth, viewHeight)
    }

    fun capture1() {
        surfaceView.queueEvent {
            val storagePicture = FileUtils.getStoragePicture(surfaceView.context, "a")
            val bitmap = Gl2Utils.getFramebufferPixels(
                combineTexture.getFboFrameBuffer()[0],
                combineTexture.getScreenWidth(),
                combineTexture.getScreenHeight()
            ).toBitmap().savaFile(storagePicture)

            Log.e(TAG, "capture1: $storagePicture")
        }
    }

    fun capture2() {
        surfaceView.queueEvent {
            val storagePicture = FileUtils.getStoragePicture(surfaceView.context, "b")
            val bitmap = Gl2Utils.getFramebufferPixels(
                combineTexture.getFboFrameBuffer()[1],
                combineTexture.getScreenWidth(),
                combineTexture.getScreenHeight()
            ).toBitmap().savaFile(storagePicture)
            Log.e(TAG, "capture2: $storagePicture")
        }
    }

    fun release() {
        combineTexture.release()
    }

    companion object {
        private const val TAG = "ExtraTextureTouchRender"
    }
}

/**
 *
 */
fun Triple<ByteArray, Int, Int>.toBitmap(): Bitmap {
    return Bitmap.createBitmap(this.second, this.third, Bitmap.Config.ARGB_8888).also {
        it.copyPixelsFromBuffer(ByteBuffer.wrap(this.first))
    }
}

fun Bitmap.savaFile(path: String): Boolean = FileUtils.saveImage(this, path)

