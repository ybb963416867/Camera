package com.example.gpengl.multiple

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.SurfaceView
import com.example.util.MatrixUtil
import com.example.util.PositionType


class PicTexture(
    private var surfaceView: GLSurfaceView,
    vertPath: String = "shader/base_vert.glsl",
    fragPath: String = "shader/base_frag.glsl"
) : BaseTexture(surfaceView, vertPath, fragPath) {

    override fun updateTexCord(coordinateRegion: CoordinateRegion) {
        super.updateTexCord(coordinateRegion)
        MatrixUtil.getPicOriginMatrix(
            matrix,
            textureWidth,
            textureHeight,
            coordinateRegion.getWidth().toInt(),
            coordinateRegion.getHeight().toInt(),
            getScreenWidth(),
            getScreenHeight(),
            coordinateRegion,
            PositionType.MIDDLE_TOP
        )
    }
}