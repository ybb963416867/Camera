package com.example.gpengl.multiple

import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent
import com.example.util.HandlerTouchDrag

open class BaseBackgroundTexture(
    glSurfaceView: GLSurfaceView,
    vertPath: String,
    fragPath: String
) :
    IBaseTexture {
    val frameTexture = FrameTexture(glSurfaceView.context, vertPath, fragPath)
    private val backgroundTexture = ColorTexture(glSurfaceView.context, vertPath, fragPath)
    private var isDispatch = false

    private var currentRegion = CoordinateRegion()

    private var screenWidth = 0
    private var screenHeight = 0

    private var handlerTouchDrag = HandlerTouchDrag()

    init {
        handlerTouchDrag.touchDragListenerHV = object :
            HandlerTouchDrag.TouchDragListenerHV {
            override fun onDrag(
                startPoint: FloatArray,
                endPoint: FloatArray,
                xDiff: Float,
                yDiff: Float
            ) {
                Log.e(
                    "ybb",
                    "onDrag 前: xDiff = $xDiff yDiff = $yDiff width = ${currentRegion.getWidth()} height = ${currentRegion.getHeight()}"
                )

                updateTexCord(currentRegion.offSet(xDiff, yDiff))
                glSurfaceView.requestRender()

                Log.e(
                    "ybb",
                    "onDrag 后: xDiff = $xDiff yDiff = $yDiff width = ${currentRegion.getWidth()} height = ${currentRegion.getHeight()}"
                )
            }

            override fun onZoomX(
                centerX: Float,
                centerY: Float,
                zoomDist: Float,
                startPoint: FloatArray,
                endPoint: FloatArray
            ) {

            }

            override fun onZoomY(
                centerX: Float,
                centerY: Float,
                zoomDist: Float,
                startPoint: FloatArray,
                endPoint: FloatArray
            ) {

            }

        }
    }

    override fun updateTexCord(coordinateRegion: CoordinateRegion) {
        currentRegion = coordinateRegion.copyCoordinateRegion()
        frameTexture.updateTexCord(coordinateRegion)
        backgroundTexture.updateTexCord(coordinateRegion)
    }

    override fun updateTextureInfo(textureInfo: TextureInfo, isRecoverCord: Boolean) {
        currentRegion = currentRegion.generateCoordinateRegion(0f, 0f, screenWidth, screenHeight)
        frameTexture.updateTextureInfo(textureInfo, isRecoverCord)
        backgroundTexture.updateTextureInfo(textureInfo, isRecoverCord)
        frameTexture.updateTexCord(currentRegion)
        backgroundTexture.updateTexCord(currentRegion)
    }

    override fun getTextureInfo(): TextureInfo {
        return frameTexture.getTextureInfo()
    }

    override fun getScreenWidth(): Int {
        return screenWidth
    }

    override fun getScreenHeight(): Int {
        return screenHeight
    }

    override fun getTexCoordinateRegion(): CoordinateRegion {
        return currentRegion
    }

    override fun onSurfaceCreated() {
        frameTexture.onSurfaceCreated()
        backgroundTexture.onSurfaceCreated()
    }

    override fun onSurfaceChanged(screenWidth: Int, screenHeight: Int) {
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
        frameTexture.onSurfaceChanged(screenWidth, screenHeight)
        backgroundTexture.onSurfaceChanged(screenWidth, screenHeight)
    }

    override fun onDrawFrame() {
        backgroundTexture.onDrawFrame()
        frameTexture.onDrawFrame()
    }

    override fun acceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val textureRect = currentRegion.getTextureRect()
                if (textureRect.contains(
                        event.x.toInt(),
                        event.y.toInt()
                    ) && textureRect.width() < screenWidth && textureRect.height() < screenHeight
                ) {
                    isDispatch = true
                }
            }

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                isDispatch = false
            }
        }

        return isDispatch
    }

    override fun onTouch(baseTexture: IBaseTexture, event: MotionEvent): Boolean {
        if (isDispatch) {
            Log.e("ybb", "onTouch: ${baseTexture.javaClass.simpleName}")
            handlerTouchDrag.onTouchEvent(event)
        }
        return isDispatch
    }


}
