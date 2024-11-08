package com.example.gpengl.multiple

import android.opengl.GLSurfaceView
import com.example.util.MatrixUtil
import com.example.util.PositionType

class PicBackgroundTexture2(
    glSurfaceView: GLSurfaceView,
    vertPath: String = "shader/base_vert.glsl",
    fragPath: String = "shader/base_frag.glsl"
) : BaseBackgroundTexture(glSurfaceView, vertPath, fragPath) {

    override fun updateTexCord(coordinateRegion: CoordinateRegion) {
        super.updateTexCord(coordinateRegion)
        MatrixUtil.getPicOriginMatrix(
            frameTexture.matrix,
            frameTexture.textureWidth,
            frameTexture.textureHeight,
            coordinateRegion.getWidth().toInt(),
            coordinateRegion.getHeight().toInt(),
            getScreenWidth(),
            getScreenHeight(),
            coordinateRegion,
            PositionType.RIGHT_TOP
        )
    }
}