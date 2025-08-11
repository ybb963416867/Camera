package com.example.rander

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import android.widget.FrameLayout
import com.example.gpengl.multiple.CoordinateRegion
import com.example.gpengl.multiple.MultipleFboCombineTexture
import com.example.gpengl.multiple.OffScreenViewBackgroundTexture
import com.example.gpengl.multiple.PicBackgroundTexture
import com.example.gpengl.multiple.PicBackgroundTexture1
import com.example.gpengl.multiple.PicBackgroundTexture2
import com.example.gpengl.multiple.PicBackgroundTextureT
import com.example.gpengl.multiple.generateBitmapTexture
import com.example.gpengl.multiple.generateCoordinateRegion
import com.example.gpengl.multiple.getHeight
import com.example.gpengl.multiple.getWidth
import com.example.gpengl.multiple.offSet
import com.example.libffmpeg.FFMpegManager
import com.example.libffmpeg.PBOFrameCapture
import com.example.util.FileUtils
import com.example.util.Gl2Utils
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class FFmpegRecodeRender(private var surfaceView: GLSurfaceView) :
    GLSurfaceView.Renderer {

    private var combineTexture = MultipleFboCombineTexture(2, surfaceView.context)

    private var pic1 = "PicBackgroundTextureT" to PicBackgroundTextureT(surfaceView)
    private var pic2 = "PicBackgroundTexture" to PicBackgroundTexture(surfaceView)
    private var pic3 = "PicBackgroundTexture1" to PicBackgroundTexture1(surfaceView)
    private var pic4 = "PicBackgroundTexture2" to PicBackgroundTexture2(surfaceView)
    private var pic5 = "ViewTexture" to OffScreenViewBackgroundTexture<FrameLayout>(surfaceView)

    private var isRecording = false

    private var baseTextureList1 = mapOf(
        pic1,
        pic2,
        pic3,
        pic4,
        pic5,
    )

    var baseTextureList = CopyOnWriteArrayList(
        baseTextureList1.values
    )

    private var pboCapture : PBOFrameCapture? = null

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.96f, 0.8f, 0.156f, 1.0f)
        combineTexture.onSurfaceCreated(surfaceView.width, surfaceView.height)
        baseTextureList.forEach {
            it.onSurfaceCreated()
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        combineTexture.onSurfaceChanged(width, height)
        baseTextureList.forEach {
            it.onSurfaceChanged(width, height)
        }
        initializePBO()
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        // 使用着色器程序
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, combineTexture.getFboFrameBuffer()[0])
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

        baseTextureList.forEach {
            if (it != pic5.second) {
                it.onDrawFrame()
            }
        }
        GLES20.glDisable(GLES20.GL_BLEND)

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, combineTexture.getTextureArray()[1])
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        baseTextureList.forEach {
            it.onDrawFrame()
        }

        combineTexture.onDrawFrame(1)
        GLES20.glDisable(GLES20.GL_BLEND)

        if (isRecording && pboCapture != null) {
            captureCurrentFrame()
        }
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

    private fun  initializePBO() {
        val width = combineTexture.getScreenWidth()
        val height = combineTexture.getScreenHeight()

        if (width > 0 && height > 0) {
            pboCapture?.release() // 释放之前的PBO
            pboCapture = PBOFrameCapture(width, height, surfaceView.context)
            pboCapture?.initialize()
            Log.i("ybb", "PBO initialized with size: ${width}x${height}")
        }
    }

    private fun captureCurrentFrame() {
        try {
            // 绑定到combineTexture的最终输出FBO
            // 这里需要根据你的combineTexture实现来确定最终输出的FBO
            val finalFBO = combineTexture.getFboFrameBuffer()[1] // 或者其他最终输出的FBO

            // 绑定到最终输出的FBO进行读取
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, finalFBO)

            // 使用PBO捕获帧
            val success = pboCapture?.captureFrame() ?: false
            if (!success) {
                Log.w(TAG, "PBO frame capture failed")
            }

            // 恢复默认framebuffer
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)

        } catch (e: Exception) {
            Log.e(TAG, "Error capturing frame: ${e.message}")
        }
    }

    fun startRecord() {
        try {
            pic5.second.start()
            isRecording = true
            val mPath = FileUtils.getStorageMp4(surfaceView.context, System.currentTimeMillis().toString())
            pboCapture?.startRecording(mPath)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopRecord() {
        pic5.second.stop()
        isRecording = false
        pboCapture?.stopRecording()
    }

    fun setRecodeView(root: FrameLayout, viewWidth: Int, viewHeight: Int) {
        pic5.second.setViewInfo(root, viewWidth, viewHeight)
    }

    fun setLocationViewInfo(rootView: FrameLayout, viewWidth: Int, viewHeight: Int, left: Int, top: Int){
        pic5.second.setLocationViewInfo(rootView, viewWidth, viewHeight, left, top)
    }

    fun capture1() {
        pic5.second.updateViewTexture(true) {
            surfaceView.queueEvent {
                val recodeInfo = Gl2Utils.getFramebufferPixels(
                    combineTexture.getFboFrameBuffer()[0],
                    combineTexture.getScreenWidth(),
                    combineTexture.getScreenHeight()
                )
            }
        }
    }

    fun capture2() {
        pic5.second.updateViewTexture(true) {
            surfaceView.queueEvent {
                val storagePicture = FileUtils.getStoragePicture(surfaceView.context, "b")
                val recodeInfo = Gl2Utils.getFramebufferPixels(
                    combineTexture.getFboFrameBuffer()[1],
                    combineTexture.getScreenWidth(),
                    combineTexture.getScreenHeight()
                )
            }
        }
    }

    fun update(){
        pic5.second.updateViewTexture()
    }

    fun release() {
        combineTexture.release()
        baseTextureList.forEach {
            it.release()
        }
    }

    companion object {
        private const val TAG = "ExtraTextureTouchRender"
    }
}



