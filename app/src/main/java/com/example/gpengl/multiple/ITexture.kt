package com.example.gpengl.multiple

interface ITexture {
    fun onSurfaceCreated()
    fun onSurfaceChanged(screenWidth: Int, screenHeight: Int)
    fun onDrawFrame()
}
