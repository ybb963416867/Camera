package com.example.gpengl.multiple

enum class ITextureVisibility {
    VISIBLE,
    INVISIBLE,
}

interface ITexture {
    fun onSurfaceCreated()
    fun onSurfaceChanged(screenWidth: Int, screenHeight: Int)
    fun onDrawFrame()
    fun getVisibility(): ITextureVisibility
    fun setVisibility(visibility: ITextureVisibility)
    fun clearTexture(colorString: String)
}
