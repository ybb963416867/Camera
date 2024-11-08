package com.example.gpengl.multiple

import android.opengl.GLSurfaceView

class PicBackgroundTextureT(
    glSurfaceView: GLSurfaceView,
    vertPath: String = "shader/base_vert.glsl",
    fragPath: String = "shader/base_frag.glsl"
) : BaseBackgroundTexture(glSurfaceView, vertPath, fragPath)