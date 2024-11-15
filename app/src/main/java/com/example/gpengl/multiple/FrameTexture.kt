package com.example.gpengl.multiple

import android.opengl.GLSurfaceView
import android.opengl.Matrix

class FrameTexture(
    private var surfaceView: GLSurfaceView,
    vertPath: String = "shader/base_vert.glsl",
    fragPath: String = "shader/base_frag.glsl"
) : BaseTexture(surfaceView, vertPath, fragPath) {

    override fun updateTextureInfo(textureInfo: TextureInfo, isRecoverCord: Boolean, iTextureVisibility: ITextureVisibility) {
        getTextureInfo().textureId = textureInfo.textureId
        textureWidth = textureInfo.width
        textureHeight = textureInfo.height
        setVisibility(iTextureVisibility)
        Matrix.setIdentityM(matrix, 0)
    }
}