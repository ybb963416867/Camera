package com.example.gpengl.multiple

import android.graphics.SurfaceTexture
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.ViewGroup
import com.example.util.Gl2Utils
import com.example.util.MatrixUtil
import com.example.util.PositionType
import java.lang.ref.WeakReference
import java.nio.ByteBuffer

class OffScreenViewBackgroundTexture<T : ViewGroup>(
    val surfaceView: GLSurfaceView,
    vertPath: String = "shader/oes_base_vertex.vert",
    fragPath: String = "shader/oes_base_fragment.frag"
) : BaseOesGroundTexture(surfaceView, vertPath, fragPath) {

    private var rootViewWeakReference: WeakReference<T>? = null
    private var rootViewWidth = 0
    private var rootViewHeight = 0

    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null
    private val handler = Handler(Looper.getMainLooper())
    private val surfaceViewRef = WeakReference(surfaceView)
    private val handlerThread = HandlerThread("RenderThread")
    private val renderHandler: Handler
    private var newFrameAvailable = false
    private var drawFrameCompleteListener: (() -> Unit)? = null
//    private var currentTextureId = 0
//    private var backupTextureId = 0

    init {
        handlerThread.start()
        renderHandler = Handler(handlerThread.looper)
    }

    override fun onSurfaceCreated() {
        super.onSurfaceCreated()
//        backupTextureId = Gl2Utils.createOESTextureID(1)[0]
//        currentTextureId = getTextureInfo().textureId
        surfaceTexture = SurfaceTexture(getTextureInfo().textureId)
        surface = Surface(surfaceTexture)

        synchronized(this) {
            newFrameAvailable = false
        }

        surfaceTexture?.setOnFrameAvailableListener {
            if (newFrameAvailable) {
                surfaceViewRef.get()?.requestRender()
            }
        }

    }


    /**
     * 使用 PBO 将 surfaceTexture 更新后的内容拷贝到新纹理
     */
    private fun updateTextureWithPBO() {
        // Step 1: 获取当前纹理 ID
        val currentTextureId = getTextureInfo().textureId

        // Step 2: 创建一个新的纹理 ID
        val newTextureId = Gl2Utils.createOESTextureID(1)[0]

        // Step 3: 创建并绑定 PBO
        val pboIds = IntArray(1)
        GLES30.glGenBuffers(1, pboIds, 0)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pboIds[0])

        // Step 4: 创建一个缓存区来存储像素数据
        val bufferSize = rootViewWidth * rootViewHeight * 4 // Assuming RGBA format
        val buffer = ByteBuffer.allocateDirect(bufferSize)
        buffer.position(0)

        // Step 5: 从 SurfaceTexture 中读取像素数据
        GLES30.glReadPixels(0, 0, rootViewWidth, rootViewHeight, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buffer)

        // Step 6: 将读取到的像素数据通过 PBO 复制到新的纹理
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, newTextureId)
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_RGBA, rootViewWidth, rootViewHeight, 0, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buffer)

        // Step 7: 解绑 PBO
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)

        // Step 8: 释放 PBO
        GLES30.glDeleteBuffers(1, pboIds, 0)

        // Now use the new texture ID in subsequent rendering.
        // (update your texture info accordingly)
        getTextureInfo().textureId = newTextureId
    }

    companion object {
        private const val DELAY_MILLIS = 200L
    }

    override fun onDrawFrame() {
        synchronized(this) {
            if (newFrameAvailable) {
                surfaceTexture?.updateTexImage()
                surfaceTexture?.getTransformMatrix(frameTexture.coordsMatrix)

                updateTextureInfo(
                    getTextureInfo().apply {
                        width = rootViewWidth
                        height = rootViewHeight
                    }, false, getVisibility()
                )

                drawFrameCompleteListener?.invoke()
                drawFrameCompleteListener = null

                newFrameAvailable = false
            } else {
                updateTextureInfo(
                    getTextureInfo().apply {
                        width = rootViewWidth
                        height = rootViewHeight
                    }, false, getVisibility()
                )
            }
        }
        super.onDrawFrame()
    }

    override fun updateTexCord(coordinateRegion: CoordinateRegion) {
        super.updateTexCord(coordinateRegion)
        MatrixUtil.getPicOriginMatrix(
            frameTexture.matrix,
            getTextureInfo().width,
            getTextureInfo().height,
            coordinateRegion.getWidth().toInt(),
            coordinateRegion.getHeight().toInt(),
            getScreenWidth(),
            getScreenHeight(),
            coordinateRegion,
            PositionType.MIDDLE_TOP
        )
    }

    fun start() {
        handler.postDelayed(updateRunnable, DELAY_MILLIS)
    }

    fun stop() {
        handler.removeCallbacks(updateRunnable)
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            updateViewTexture()
            handler.postDelayed(this, DELAY_MILLIS)
        }
    }

    fun setViewInfo(rootView: T, viewWidth: Int, viewHeight: Int) {
        this.rootViewWeakReference = WeakReference(rootView)
        this.rootViewWidth = viewWidth
        this.rootViewHeight = viewHeight
        updateTextureInfo(
            getTextureInfo().apply {
                width = rootViewWidth
                height = rootViewHeight
            }, false, "#4DFF0000", getVisibility()
        )
        setVisibility(ITextureVisibility.VISIBLE)
    }

    fun updateViewTexture(isUpdate: Boolean = false, listener: (() -> Unit)? = null) {
        surfaceTexture?.setDefaultBufferSize(rootViewWidth, rootViewHeight)
        rootViewWeakReference?.get()?.let { rootView ->
            if (!newFrameAvailable) {
                handler.post {
                    surface?.let { surface ->
                        val canvas = surface.lockCanvas(null)
                        if (canvas != null) {
                            try {
                                canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR) // 清空画布
                                rootView.draw(canvas) // 绘制 rootView
                                if (isUpdate) {
                                    drawFrameCompleteListener = listener
                                }
                            } finally {
                                surface.unlockCanvasAndPost(canvas)
                            }

                            synchronized(this) {
                                newFrameAvailable = true
                            }
                        } else {
                            Log.e("ybb", "Failed to lock canvas")
                        }
                    }
                }
            }
        }
    }


    override fun release() {
        super.release()
        handler.post {
            surface?.release()
            surface = null
            surfaceTexture?.release()
            surfaceTexture = null
        }
        stop()
    }
}