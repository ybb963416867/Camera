package com.example.gpengl.multiple

interface IBaseFboCombineTexture: ITexture {
    fun getTextureArray(): IntArray
    fun getScreenWidth(): Int
    fun getScreenHeight(): Int
    fun getFboFrameBuffer(): IntArray
}
