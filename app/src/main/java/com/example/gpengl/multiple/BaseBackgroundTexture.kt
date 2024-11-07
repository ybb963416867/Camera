package com.example.gpengl.multiple

import android.content.Context

open class BaseBackgroundTexture(
    context: Context,
    vertPath: String,
    fragPath: String
) :
    IBaseTexture {
    val frameTexture = FrameTexture(context, vertPath, fragPath)
    private val backgroundTexture = ColorTexture(context, vertPath, fragPath)

    private var screenWidth = 0
    private var screenHeight = 0

    override fun updateTexCord(coordinateRegion: CoordinateRegion) {
        frameTexture.updateTexCord(coordinateRegion)
        backgroundTexture.updateTexCord(coordinateRegion)
    }

    override fun updateTextureInfo(textureInfo: TextureInfo, isRecoverCord: Boolean) {
        frameTexture.updateTextureInfo(textureInfo, isRecoverCord)
        backgroundTexture.updateTextureInfo(textureInfo, isRecoverCord)
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


}
