package com.example.gpengl.multiple

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import com.example.util.MatrixUtil
import com.example.util.PositionType
import java.lang.ref.WeakReference

class ViewTexture<T : ViewGroup>(
    val surfaceView: GLSurfaceView,
    vertPath: String = "shader/base_vert.glsl",
    fragPath: String = "shader/base_frag.glsl"
) : BaseTexture(surfaceView, vertPath, fragPath) {

    private var rootViewWeakReference: WeakReference<T>? = null
    private var rootViewWidth = 0
    private var rootViewHeight = 0

    private var backBitmap: Bitmap? = null
    private var handler = Handler(Looper.getMainLooper())
    private var weakReference = WeakReference(surfaceView)
    private var runnable = object : Runnable {
        override fun run() {
            updateViewTexture()
            handler.postDelayed(this, DELAY_MILLIS)
        }
    }

    private var drawFrameCompleteListener: (() -> Unit)? = null

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

    override fun onDrawFrame() {
        super.onDrawFrame()
        drawFrameCompleteListener?.invoke()
        drawFrameCompleteListener = null
    }

    fun start() {
        handler.post(runnable)
    }

    fun stop() {
        handler.removeCallbacks(runnable)
    }

    fun updateViewTexture(isUpdate: Boolean = false, listener: (() -> Unit)? = null) {
        updateBitmap()
        weakReference.get()?.let { glView ->
            glView.queueEvent {
                backBitmap?.let {
                    if (!it.isRecycled) {
                        updateTextureInfo(
                            getTextureInfo().generateBitmapTexture(
                                getTextureInfo().textureId, it, false
                            ), false, getVisibility()
                        )
                        if (isUpdate) {
                            glView.requestRender()
                            drawFrameCompleteListener = listener
                        }
                    }
                }
            }
        }
    }

    fun setViewInfo(rootView: T, viewWidth: Int, viewHeight: Int) {
        this.rootViewWeakReference = WeakReference(rootView)
        this.rootViewWidth = viewWidth
        this.rootViewHeight = viewHeight
        if (backBitmap == null || backBitmap!!.width * backBitmap!!.height != rootViewWidth * rootViewHeight) {
            GLES30.glFinish()
            backBitmap?.recycle()
            backBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
        }

        setVisibility(ITextureVisibility.VISIBLE)
        updateViewTexture(true)
    }

    private fun updateBitmap() {
        backBitmap?.let {
            if (!it.isRecycled) {
                it.eraseColor(Color.TRANSPARENT)
                val canvas = Canvas(it)
                rootViewWeakReference?.get()?.draw(canvas)
//                currentBitmapRef.set(backBitmap)
            }
        }
    }

    override fun release() {
        super.release()
        backBitmap?.recycle()
        backBitmap = null
        stop()
    }

    companion object {
        private const val DELAY_MILLIS = 100L
    }
}