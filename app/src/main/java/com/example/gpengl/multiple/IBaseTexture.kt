package com.example.gpengl.multiple

interface IBaseTexture: ITexture, TextureTouchEvent {
    fun updateTexCord(coordinateRegion: CoordinateRegion)
    fun updateTextureInfo(textureInfo: TextureInfo, isRecoverCord: Boolean = false)
    fun getTextureInfo(): TextureInfo
    fun getScreenWidth(): Int
    fun getScreenHeight(): Int
    fun getTexCoordinateRegion(): CoordinateRegion
}
