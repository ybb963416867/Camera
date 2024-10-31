package com.example.gpengl.multiple

interface IBaseTexture {
    fun onSurfaceCreated()
    fun onSurfaceChanged(screenWith: Int, screenHeight: Int)
    fun onDrawFrame(shaderProgram: Int)
    fun updateTexCord(coordinateRegion: CoordinateRegion)
    fun loadBitmapTexture(resourceId: Int)
    fun updateTextureInfo(textureInfo: TextureInfo)
    fun getTextureId(): Int
}
