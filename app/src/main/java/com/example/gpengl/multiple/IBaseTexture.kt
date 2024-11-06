package com.example.gpengl.multiple

interface IBaseTexture {
    fun onSurfaceCreated()
    fun onSurfaceChanged(screenWith: Int, screenHeight: Int)
    fun onDrawFrame()
    fun updateTexCord(coordinateRegion: CoordinateRegion)
    fun updateTextureInfo(textureInfo: TextureInfo, isRecoverCord: Boolean = false)
    fun getTextureInfo(): TextureInfo
    fun getScreenWidth(): Int
    fun getScreenHeight(): Int
}
