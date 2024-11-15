package com.example.gpengl.multiple

interface IBaseFboCombineTexture: IFboTexture {
    fun getTextureArray(): IntArray
    fun getScreenWidth(): Int
    fun getScreenHeight(): Int
    fun getFboFrameBuffer(): IntArray
}
