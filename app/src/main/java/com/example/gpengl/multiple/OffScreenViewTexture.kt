package com.example.gpengl.multiple

import android.graphics.SurfaceTexture
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.view.ViewGroup
import com.example.util.MatrixUtil
import com.example.util.PositionType
import java.lang.ref.WeakReference

class OffScreenViewTexture<T : ViewGroup>(
    val surfaceView: GLSurfaceView,
    vertPath: String = "shader/oes_base_vertex.vert",
    fragPath: String = "shader/oes_base_fragment.frag"
) : BaseOesTexture(surfaceView, vertPath, fragPath) {

    private var rootViewWeakReference: WeakReference<T>? = null
    private var rootViewWidth = 0
    private var rootViewHeight = 0

    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null
    private val handler = Handler(Looper.getMainLooper())
    private val surfaceViewRef = WeakReference(surfaceView)
    private var newFrameAvailable = false
    private var drawFrameCompleteListener: (() -> Unit)? = null

    override fun onSurfaceCreated() {
        super.onSurfaceCreated()
        surfaceTexture = SurfaceTexture(getTextureInfo().textureId)
        surface = Surface(surfaceTexture)

        synchronized(this) {
            newFrameAvailable = false
        }

        surfaceTexture?.setOnFrameAvailableListener {
            synchronized(this) {
                surfaceViewRef.get()?.queueEvent {
                    newFrameAvailable = true
                    surfaceTexture?.updateTexImage()
                    surfaceTexture?.getTransformMatrix(coordsMatrix)
                    surfaceViewRef.get()?.requestRender()
                }
            }
        }

        updateTexCord(CoordinateRegion().generateCoordinateRegion(100f, 100f, 1000, 500))
    }

    companion object {
        private const val DELAY_MILLIS = 100L
    }

    override fun onDrawFrame() {
        synchronized(this) {
            if (newFrameAvailable) {
//                surfaceTexture?.updateTexImage()
//                surfaceTexture?.getTransformMatrix(coordsMatrix)
//                updateTextureInfo(
//                    getTextureInfo().apply {
//                        width = rootViewWidth
//                        height = rootViewHeight
//                    }
//                    , false, getVisibility()
//                )

                drawFrameCompleteListener?.invoke()
                drawFrameCompleteListener = null
                newFrameAvailable = false
            }
        }

        super.onDrawFrame()
    }

    override fun updateTexCord(coordinateRegion: CoordinateRegion) {
        super.updateTexCord(coordinateRegion)
        MatrixUtil.getPicOriginMatrix(
            matrix,
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
//        updateTextureInfo(
//            getTextureInfo().apply {
//                width = rootViewWidth
//                height = rootViewHeight
//            }
//            , false, getVisibility()
//        )
//        setVisibility(ITextureVisibility.VISIBLE)
//        updateTexCord(CoordinateRegion().generateCoordinateRegion(200f, 100f, 800, 300))
        surfaceView.queueEvent {
            updateTextureInfo(
                getTextureInfo().apply {
                    width = rootViewWidth
                    height = rootViewHeight
                }, false, "#4DFF0000", getVisibility()
            )
            setVisibility(ITextureVisibility.VISIBLE)
            updateViewTexture()
        }
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
                                Log.e(
                                    "ybb",
                                    "Drawing rootView with width: $rootViewWidth, height: $rootViewHeight"
                                )
                                rootView.draw(canvas) // 绘制 rootView
                                Log.e("ybb", "Finished drawing rootView")
                            } finally {
                                surface.unlockCanvasAndPost(canvas)
                            }
                            if (isUpdate) {
                                drawFrameCompleteListener = listener
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
            handler.removeCallbacks(updateRunnable)
        }
        stop()
    }
}