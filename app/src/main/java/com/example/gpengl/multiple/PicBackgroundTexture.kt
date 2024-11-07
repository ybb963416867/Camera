package com.example.gpengl.multiple

import android.content.Context
import com.example.util.MatrixUtil
import com.example.util.PositionType

//"shader/base_background_frag.glsl"
class PicBackgroundTexture(
    private val context: Context,
    vertPath: String = "shader/base_vert.glsl",
    fragPath: String = "shader/base_frag.glsl"
) : BaseBackgroundTexture(context, vertPath, fragPath) {

    override fun updateTexCord(coordinateRegion: CoordinateRegion) {
        super.updateTexCord(coordinateRegion)
        MatrixUtil.getPicOriginMatrix(
            matrix,
            colorMatrix,
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