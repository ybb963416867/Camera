package com.example.gpengl.multiple

import android.content.Context

class PicBackgroundTextureT(
    context: Context,
    vertPath: String = "shader/base_vert.glsl",
    fragPath: String = "shader/base_frag.glsl"
) : BaseBackgroundTexture(context, vertPath, fragPath)