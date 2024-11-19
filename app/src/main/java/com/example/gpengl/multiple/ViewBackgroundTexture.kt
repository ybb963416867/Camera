package com.example.gpengl.multiple

import android.graphics.Bitmap
import android.graphics.Canvas
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.view.ViewGroup
import com.example.util.MatrixUtil
import com.example.util.PositionType
import java.util.concurrent.atomic.AtomicReference

class ViewBackgroundTexture<T: ViewGroup>(
    val surfaceView: GLSurfaceView,
    vertPath: String = "shader/base_vert.glsl",
    fragPath: String = "shader/base_frag.glsl"
) : BaseBackgroundTexture(surfaceView, vertPath, fragPath) {

    private var rootView: T? = null
    private var rootViewWidth = 0
    private var rootViewHeight = 0
    private var currentBitmapRef = AtomicReference<Bitmap?>(null)
    private var backBitmap: Bitmap? = null

    override fun updateTexCord(coordinateRegion: CoordinateRegion) {
        super.updateTexCord(coordinateRegion)
        MatrixUtil.getPicOriginMatrix(
            frameTexture.matrix,
            frameTexture.textureWidth,
            frameTexture.textureHeight,
            coordinateRegion.getWidth().toInt(),
            coordinateRegion.getHeight().toInt(),
            getScreenWidth(),
            getScreenHeight(),
            coordinateRegion,
            PositionType.MIDDLE_TOP
        )
    }

//"#4DFF0080"
    override fun onDrawFrame() {
        rootView?.post {
            updateBitmap()
        }
        currentBitmapRef.get()?.let {
            if (!it.isRecycled) {
                updateTextureInfo(
                    getTextureInfo().generateBitmapTexture(
                        getTextureInfo().textureId, it, false
                    ), false,
                )
            }

        }
        super.onDrawFrame()
    }

    fun setViewInfo(rootView: T, viewWidth: Int, viewHeight: Int) {
        this.rootView = rootView
        this.rootViewWidth = viewWidth
        this.rootViewHeight = viewHeight
        if (backBitmap == null || backBitmap!!.width * backBitmap!!.height != rootViewWidth * rootViewHeight) {
            GLES20.glFinish()
            backBitmap?.recycle()
            backBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888)
        }

        updateBitmap()
        surfaceView.requestRender()
    }

    private fun updateBitmap() {
        backBitmap?.let {
            if (!it.isRecycled) {
                val canvas = Canvas(it)
                rootView?.draw(canvas)
                currentBitmapRef.set(backBitmap)
            }
        }
    }

    override fun release() {
        super.release()
        backBitmap?.recycle()
        backBitmap = null
        currentBitmapRef.get()?.recycle()
    }
}