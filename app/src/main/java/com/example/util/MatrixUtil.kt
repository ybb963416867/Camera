package com.example.util

import android.opengl.Matrix
import com.example.gpengl.multiple.CoordinateRegion
import com.example.gpengl.multiple.getHeight
import com.example.gpengl.multiple.getSurfaceArea
import com.example.gpengl.multiple.getWidth
import kotlin.math.abs

enum class PositionType {
    CENTER,
    LEFT_TOP,
    RIGHT_TOP,
    LEFT_BOTTOM,
    RIGHT_BOTTOM,
    MIDDLE_TOP,
    MIDDLE_BOTTOM
}

object MatrixUtil {


    fun screenToGlCoordinate(
        px: Float,
        py: Float,
        screenWidth: Float,
        screenHeight: Float,
        xBoundary: Float = 1.0f,
        yBoundary: Float = 1.0f
    ): FloatArray {
        val vx = (px / screenWidth) * xBoundary * 2.0f - xBoundary // 转换 X 坐标
        val vy = yBoundary - (py / screenHeight) * yBoundary * 2.0f // 转换 Y 坐标并反转 Y 轴
        return floatArrayOf(vx, vy)
    }

    private fun screenToGlCoordinateX(
        px: Float, screenWidth: Float, xBoundary: Float = 1.0f
    ): Float {
        return (px / screenWidth) * 2.0f * xBoundary - 1.0f * xBoundary
    }

    private fun screenToGlCoordinateY(
        py: Float, screenHeight: Float, yBoundary: Float = 1.0f
    ): Float {
        return (1.0f - (py / screenHeight) * 2.0f) * yBoundary
    }

    /**
     * @param type 0 center  1 左上 2 右上 3左下  4 右下  5 中上 6 中下
     */
    fun getPicOriginMatrix(
        matrix: FloatArray?,
        imgWidth: Int,
        imgHeight: Int,
        viewWidth: Int,
        viewHeight: Int,
        surfaceWidth: Int,
        surfaceHeight: Int,
        coordinateRegion: CoordinateRegion,
        type: PositionType
    ) {

        if (imgHeight <= 0 || imgWidth <= 0 || viewWidth <= 0 || viewHeight <= 0 || surfaceWidth <= 0 || surfaceHeight <= 0) {
            return
        }

        val originArea = coordinateRegion.getSurfaceArea(
            surfaceWidth = surfaceWidth.toFloat(),
            surfaceHeight = surfaceHeight.toFloat()
        )

        val oLeft: Float
        val oRight: Float
        val oTop: Float
        val oBottom: Float

        val viewAspectRatio = viewWidth.toFloat() / viewHeight
        val bitmapAspectRatio = imgWidth.toFloat() / imgHeight
        val projection = FloatArray(16)
        val mViewMatrix = FloatArray(16)
        if (bitmapAspectRatio > viewAspectRatio) {
            oLeft = -1f
            oRight = 1f
            oTop = bitmapAspectRatio / viewAspectRatio
            oBottom = -bitmapAspectRatio / viewAspectRatio

            Matrix.orthoM(
                projection,
                0,
                oLeft,
                oRight,
                oBottom,
                oTop,
                1f,
                3f
            )
        } else {
            oLeft = -viewAspectRatio / bitmapAspectRatio
            oRight = viewAspectRatio / bitmapAspectRatio
            oTop = 1f
            oBottom = -1f
            Matrix.orthoM(
                projection,
                0,
                oLeft,
                oRight,
                oBottom,
                oTop,
                1f,
                3f
            )
        }
        Matrix.setLookAtM(
            mViewMatrix, 0,
            (originArea.coordinateLeft + originArea.coordinateRight) / 2f,
            (originArea.coordinateBottom + originArea.coordinateTop) / 2f, 1f,
            (originArea.coordinateLeft + originArea.coordinateRight) / 2f,
            (originArea.coordinateBottom + originArea.coordinateTop) / 2f, 0f, 0f, 1.0f, 0.0f
        )

        val matrixOriginArea = coordinateRegion.getSurfaceArea(
            surfaceWidth = surfaceWidth.toFloat(),
            surfaceHeight = surfaceHeight.toFloat(),
            xBoundary = abs(oRight - oLeft) / 2f,
            yBoundary = abs(oTop - oBottom) / 2f
        )
        Matrix.multiplyMM(matrix, 0, projection, 0, mViewMatrix, 0)

        var difWidth = 0f
        var difHeight = 0f
        if (bitmapAspectRatio > viewAspectRatio) {
            val originHeight = coordinateRegion.getWidth().div(bitmapAspectRatio)
            difHeight = abs(
                screenToGlCoordinateY(
                    originHeight,
                    screenHeight = surfaceHeight.toFloat(),
                    abs(oTop - oBottom) / 2f
                ) - screenToGlCoordinateY(
                    coordinateRegion.getHeight(),
                    screenHeight = surfaceHeight.toFloat(),
                    abs(oTop - oBottom) / 2f
                )
            )
        } else {
            val originWidth = coordinateRegion.getHeight().times(bitmapAspectRatio)
            difWidth = abs(
                screenToGlCoordinateX(
                    originWidth,
                    screenWidth = surfaceWidth.toFloat(),
                    abs(oLeft - oRight) / 2f
                ) - screenToGlCoordinateX(
                    coordinateRegion.getWidth(),
                    screenWidth = surfaceWidth.toFloat(),
                    abs(oLeft - oRight) / 2f
                )
            )
        }


        when (type) {
            PositionType.CENTER -> {
                Matrix.translateM(
                    matrix, 0,
                    (matrixOriginArea.coordinateRight + matrixOriginArea.coordinateLeft) / 2f,
                    (matrixOriginArea.coordinateTop + matrixOriginArea.coordinateBottom) / 2f, 0f
                )
            }

            PositionType.LEFT_TOP -> {
                Matrix.translateM(
                    matrix, 0,
                    (matrixOriginArea.coordinateRight + matrixOriginArea.coordinateLeft) / 2f - difWidth.div(
                        2f
                    ),
                    (matrixOriginArea.coordinateTop + matrixOriginArea.coordinateBottom) / 2f + difHeight.div(
                        2f
                    ), 0f
                )
            }

            PositionType.RIGHT_TOP -> {
                Matrix.translateM(
                    matrix, 0,
                    (matrixOriginArea.coordinateRight + matrixOriginArea.coordinateLeft) / 2f + difWidth.div(
                        2f
                    ),
                    (matrixOriginArea.coordinateTop + matrixOriginArea.coordinateBottom) / 2f + difHeight.div(
                        2f
                    ), 0f
                )
            }

            PositionType.LEFT_BOTTOM -> {
                Matrix.translateM(
                    matrix, 0,
                    (matrixOriginArea.coordinateRight + matrixOriginArea.coordinateLeft) / 2f - difWidth.div(
                        2f
                    ),
                    (matrixOriginArea.coordinateTop + matrixOriginArea.coordinateBottom) / 2f - difHeight.div(
                        2f
                    ), 0f
                )
            }

            PositionType.RIGHT_BOTTOM -> {
                Matrix.translateM(
                    matrix, 0,
                    (matrixOriginArea.coordinateRight + matrixOriginArea.coordinateLeft) / 2f + difWidth.div(
                        2f
                    ),
                    (matrixOriginArea.coordinateTop + matrixOriginArea.coordinateBottom) / 2f - difHeight.div(
                        2f
                    ), 0f
                )
            }

            PositionType.MIDDLE_TOP -> {
                Matrix.translateM(
                    matrix, 0,
                    (matrixOriginArea.coordinateRight + matrixOriginArea.coordinateLeft) / 2f,
                    (matrixOriginArea.coordinateTop + matrixOriginArea.coordinateBottom) / 2f + difHeight.div(
                        2f
                    ), 0f
                )
            }

            PositionType.MIDDLE_BOTTOM -> {
                Matrix.translateM(
                    matrix, 0,
                    (matrixOriginArea.coordinateRight + matrixOriginArea.coordinateLeft) / 2f,
                    (matrixOriginArea.coordinateTop + matrixOriginArea.coordinateBottom) / 2f - difHeight.div(
                        2f
                    ), 0f
                )
            }
        }
    }



    /**
     * @param type 0 center  1 左上 2 右上 3左下  4 右下  5 中上 6 中下
     */
    fun getPicOriginMatrix(
        matrix: FloatArray?,
        colorMatrix: FloatArray?,
        imgWidth: Int,
        imgHeight: Int,
        viewWidth: Int,
        viewHeight: Int,
        surfaceWidth: Int,
        surfaceHeight: Int,
        coordinateRegion: CoordinateRegion,
        type: PositionType
    ) {

        if (imgHeight <= 0 || imgWidth <= 0 || viewWidth <= 0 || viewHeight <= 0 || surfaceWidth <= 0 || surfaceHeight <= 0) {
            return
        }

        val originArea = coordinateRegion.getSurfaceArea(
            surfaceWidth = surfaceWidth.toFloat(),
            surfaceHeight = surfaceHeight.toFloat()
        )

        val oLeft: Float
        val oRight: Float
        val oTop: Float
        val oBottom: Float

        val viewAspectRatio = viewWidth.toFloat() / viewHeight
        val bitmapAspectRatio = imgWidth.toFloat() / imgHeight
        val projection = FloatArray(16)
        val colorProjection = FloatArray(16)
        val mViewMatrix = FloatArray(16)
        if (bitmapAspectRatio > viewAspectRatio) {
            oLeft = -1f
            oRight = 1f
            oTop = bitmapAspectRatio / viewAspectRatio
            oBottom = -bitmapAspectRatio / viewAspectRatio

            Matrix.orthoM(
                projection,
                0,
                oLeft,
                oRight,
                oBottom,
                oTop,
                1f,
                3f
            )
        } else {
            oLeft = -viewAspectRatio / bitmapAspectRatio
            oRight = viewAspectRatio / bitmapAspectRatio
            oTop = 1f
            oBottom = -1f
            Matrix.orthoM(
                projection,
                0,
                oLeft,
                oRight,
                oBottom,
                oTop,
                1f,
                3f
            )
        }

        Matrix.orthoM(colorProjection, 0, -1f, 1f, -1f, 1f, 1f, 3f)
        Matrix.setLookAtM(
            mViewMatrix, 0,
            (originArea.coordinateLeft + originArea.coordinateRight) / 2f,
            (originArea.coordinateBottom + originArea.coordinateTop) / 2f, 1f,
            (originArea.coordinateLeft + originArea.coordinateRight) / 2f,
            (originArea.coordinateBottom + originArea.coordinateTop) / 2f, 0f, 0f, 1.0f, 0.0f
        )

        val matrixOriginArea = coordinateRegion.getSurfaceArea(
            surfaceWidth = surfaceWidth.toFloat(),
            surfaceHeight = surfaceHeight.toFloat(),
            xBoundary = abs(oRight - oLeft) / 2f,
            yBoundary = abs(oTop - oBottom) / 2f
        )
        Matrix.multiplyMM(matrix, 0, projection, 0, mViewMatrix, 0)
        Matrix.multiplyMM(colorMatrix, 0, colorProjection, 0, mViewMatrix, 0)
        var difWidth = 0f
        var difHeight = 0f
        if (bitmapAspectRatio > viewAspectRatio) {
            val originHeight = coordinateRegion.getWidth().div(bitmapAspectRatio)
            difHeight = abs(
                screenToGlCoordinateY(
                    originHeight,
                    screenHeight = surfaceHeight.toFloat(),
                    abs(oTop - oBottom) / 2f
                ) - screenToGlCoordinateY(
                    coordinateRegion.getHeight(),
                    screenHeight = surfaceHeight.toFloat(),
                    abs(oTop - oBottom) / 2f
                )
            )
        } else {
            val originWidth = coordinateRegion.getHeight().times(bitmapAspectRatio)
            difWidth = abs(
                screenToGlCoordinateX(
                    originWidth,
                    screenWidth = surfaceWidth.toFloat(),
                    abs(oLeft - oRight) / 2f
                ) - screenToGlCoordinateX(
                    coordinateRegion.getWidth(),
                    screenWidth = surfaceWidth.toFloat(),
                    abs(oLeft - oRight) / 2f
                )
            )
        }


        when (type) {
            PositionType.CENTER -> {
                Matrix.translateM(
                    matrix, 0,
                    (matrixOriginArea.coordinateRight + matrixOriginArea.coordinateLeft) / 2f,
                    (matrixOriginArea.coordinateTop + matrixOriginArea.coordinateBottom) / 2f, 0f
                )
            }

            PositionType.LEFT_TOP -> {
                Matrix.translateM(
                    matrix, 0,
                    (matrixOriginArea.coordinateRight + matrixOriginArea.coordinateLeft) / 2f - difWidth.div(
                        2f
                    ),
                    (matrixOriginArea.coordinateTop + matrixOriginArea.coordinateBottom) / 2f + difHeight.div(
                        2f
                    ), 0f
                )
            }

            PositionType.RIGHT_TOP -> {
                Matrix.translateM(
                    matrix, 0,
                    (matrixOriginArea.coordinateRight + matrixOriginArea.coordinateLeft) / 2f + difWidth.div(
                        2f
                    ),
                    (matrixOriginArea.coordinateTop + matrixOriginArea.coordinateBottom) / 2f + difHeight.div(
                        2f
                    ), 0f
                )
            }

            PositionType.LEFT_BOTTOM -> {
                Matrix.translateM(
                    matrix, 0,
                    (matrixOriginArea.coordinateRight + matrixOriginArea.coordinateLeft) / 2f - difWidth.div(
                        2f
                    ),
                    (matrixOriginArea.coordinateTop + matrixOriginArea.coordinateBottom) / 2f - difHeight.div(
                        2f
                    ), 0f
                )
            }

            PositionType.RIGHT_BOTTOM -> {
                Matrix.translateM(
                    matrix, 0,
                    (matrixOriginArea.coordinateRight + matrixOriginArea.coordinateLeft) / 2f + difWidth.div(
                        2f
                    ),
                    (matrixOriginArea.coordinateTop + matrixOriginArea.coordinateBottom) / 2f - difHeight.div(
                        2f
                    ), 0f
                )
            }

            PositionType.MIDDLE_TOP -> {
                Matrix.translateM(
                    matrix, 0,
                    (matrixOriginArea.coordinateRight + matrixOriginArea.coordinateLeft) / 2f,
                    (matrixOriginArea.coordinateTop + matrixOriginArea.coordinateBottom) / 2f + difHeight.div(
                        2f
                    ), 0f
                )
            }

            PositionType.MIDDLE_BOTTOM -> {
                Matrix.translateM(
                    matrix, 0,
                    (matrixOriginArea.coordinateRight + matrixOriginArea.coordinateLeft) / 2f,
                    (matrixOriginArea.coordinateTop + matrixOriginArea.coordinateBottom) / 2f - difHeight.div(
                        2f
                    ), 0f
                )
            }
        }
    }

}