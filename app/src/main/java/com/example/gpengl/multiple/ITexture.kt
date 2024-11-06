package com.example.gpengl.multiple

interface ITexture {
    fun onSurfaceCreated()
    fun onSurfaceChanged(screenWith: Int, screenHeight: Int)
    fun onDrawFrame()
}
