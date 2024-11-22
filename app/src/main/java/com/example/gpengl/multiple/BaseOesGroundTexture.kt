package com.example.gpengl.multiple

import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent
import com.example.util.HandlerTouchDrag

open class BaseOesGroundTexture(
    var glSurfaceView: GLSurfaceView,
    vertPath: String,
    fragPath: String
) :
    IBaseTexture {
    val frameTexture = BaseOesTexture(glSurfaceView, vertPath, fragPath)
    private val backgroundTexture = ColorTexture(glSurfaceView)
    private var isDispatch = false

    private var currentRegion = CoordinateRegion()

    private var screenWidth = 0
    private var screenHeight = 0

    private var handlerTouchDrag = HandlerTouchDrag()
    private var iTextureVisibility = ITextureVisibility.INVISIBLE

    init {
        handlerTouchDrag.touchDragListenerHV = object :
            HandlerTouchDrag.TouchDragListenerHV {
            override fun onDrag(
                startPoint: FloatArray,
                endPoint: FloatArray,
                xDiff: Float,
                yDiff: Float
            ) {
                updateTexCord(currentRegion.offSet(xDiff, yDiff))
                glSurfaceView.requestRender()
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

    override fun updateTextureInfo(
        textureInfo: TextureInfo,
        isRecoverCord: Boolean,
        iTextureVisibility: ITextureVisibility
    ) {
        if (isRecoverCord) {
            currentRegion =
                CoordinateRegion().generateCoordinateRegion(0f, 0f, screenWidth, screenHeight)
        }
        this.iTextureVisibility = iTextureVisibility
        frameTexture.updateTextureInfo(textureInfo, isRecoverCord)
        backgroundTexture.updateTextureInfo(textureInfo, isRecoverCord)
        updateTexCord(currentRegion)
    }

    override fun updateTextureInfo(
        textureInfo: TextureInfo,
        isRecoverCord: Boolean,
        backgroundColor: String?,
        iTextureVisibility: ITextureVisibility
    ) {
        if (isRecoverCord) {
            currentRegion =
                CoordinateRegion().generateCoordinateRegion(0f, 0f, screenWidth, screenHeight)
        }
        this.iTextureVisibility = iTextureVisibility
        frameTexture.updateTextureInfo(textureInfo, isRecoverCord)
        backgroundTexture.updateTextureInfo(textureInfo, isRecoverCord, backgroundColor)
        updateTexCord(currentRegion)
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
        currentRegion = currentRegion.generateCoordinateRegion(
            0f,
            0f,
            glSurfaceView.width,
            glSurfaceView.height
        )
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
        if (iTextureVisibility == ITextureVisibility.VISIBLE) {
            Log.e("baseOesTexture", "onDrawFrame")
            frameTexture.updateTexCord(currentRegion)
            frameTexture.onDrawFrame()
            backgroundTexture.updateTexCord(currentRegion)
            backgroundTexture.onDrawFrame()
        }
    }

    override fun initCoordinate() {
        backgroundTexture.initCoordinate()
        frameTexture.initCoordinate()
    }

    override fun getVisibility(): ITextureVisibility {
        return iTextureVisibility
    }

    override fun setVisibility(visibility: ITextureVisibility) {
        this.iTextureVisibility = visibility
    }

    override fun clearTexture(colorString: String) {

    }

    override fun release() {
        frameTexture.release()
        backgroundTexture.release()
    }

    override fun acceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val textureRect = currentRegion.getTextureRect()
                val state = textureRect.contains(
                    event.x.toInt(),
                    event.y.toInt()
                ) && textureRect.width() < screenWidth && textureRect.height() < screenHeight

                Log.e(
                    "baseOesTexture",
                    "textureRect = $textureRect x = ${event.x} y = ${event.y} screenWidth = $screenWidth screenH = $screenHeight state = $state"
                )
                if (state
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
            handlerTouchDrag.onTouchEvent(event)
        }
        return isDispatch
    }


}
