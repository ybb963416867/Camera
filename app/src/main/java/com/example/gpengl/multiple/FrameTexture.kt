package com.example.gpengl.multiple

import android.view.SurfaceView

class FrameTexture(
    private var surfaceView: SurfaceView,
    vertPath: String = "shader/base_vert.glsl",
    fragPath: String = "shader/base_frag.glsl"
) : BaseTexture(surfaceView, vertPath, fragPath)