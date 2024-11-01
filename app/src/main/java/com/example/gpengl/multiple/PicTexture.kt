package com.example.gpengl.multiple

import android.content.Context
import com.example.util.MatrixUtil
import com.example.util.PositionType


class PicTexture(val context: Context) : BaseTexture() {

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