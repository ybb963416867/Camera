package com.example.gpengl.multiple

import android.content.Context
//"shader/base_background_frag.glsl"
class PicBackgroundTextureT(
    context: Context,
    vertPath: String = "shader/base_vert.glsl",
    fragPath: String = "shader/base_background_frag.glsl"
) : BaseBackgroundTexture(context, vertPath, fragPath)