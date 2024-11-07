package com.example.gpengl.multiple

import android.content.Context

class FrameTexture(
    private val context: Context,
    vertPath: String = "shader/base_vert.glsl",
    fragPath: String = "shader/base_frag.glsl"
) : BaseTexture(context, vertPath, fragPath)