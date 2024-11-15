package com.example.gpengl.multiple

interface IBaseTexture : ITexture, TextureTouchEvent {
    fun updateTexCord(coordinateRegion: CoordinateRegion)
    fun updateTextureInfo(
        textureInfo: TextureInfo,
        isRecoverCord: Boolean = false,
        iTextureVisibility: ITextureVisibility = ITextureVisibility.VISIBLE
    )

    fun updateTextureInfo(
        textureInfo: TextureInfo,
        isRecoverCord: Boolean = false,
        backgroundColor: String?,
        iTextureVisibility: ITextureVisibility = ITextureVisibility.VISIBLE
    )

    fun getTextureInfo(): TextureInfo
    fun getScreenWidth(): Int
    fun getScreenHeight(): Int
    fun getTexCoordinateRegion(): CoordinateRegion
}
