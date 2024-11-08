package com.example.gpengl.multiple

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import android.view.SurfaceView
import com.example.gpengl.second.util.OpenGLUtils
import com.example.util.Gl2Utils

class PicTextureT(
    private var surfaceView: SurfaceView,
    vertPath: String = "shader/base_vert.glsl",
    fragPath: String = "shader/base_frag.glsl"
) : BaseTexture(surfaceView, vertPath, fragPath)