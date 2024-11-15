package com.example.gpengl.multiple

interface IFboTexture {
    fun onSurfaceCreated(screenWidth: Int, screenHeight: Int)
    fun onSurfaceChanged(screenWidth: Int, screenHeight: Int)
    fun onDrawFrame(textureIdIndex: Int)
}