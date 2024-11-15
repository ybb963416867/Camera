package com.example.gpengl.multiple

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import com.example.util.Gl2Utils
import com.example.util.MatrixUtil
import kotlin.math.abs

data class CoordinatePoint(var x: Float = 0f, var y: Float = 0f, var c: Float = 0f)

data class CoordinateArea(
    var coordinateLeft: Float = 0f,
    var coordinateTop: Float = 0f,
    var coordinateRight: Float = 0f,
    var coordinateBottom: Float = 0f
)

fun CoordinatePoint.copyCoordinatePoint(): CoordinatePoint {
    return CoordinatePoint(x, y, c)
}

data class TextureInfo(var textureId: Int = 0, var width: Int = 0, var height: Int = 0)

fun TextureInfo.generateBitmapTexture(context: Context, resourceId: Int): TextureInfo {
    val createTextureID = Gl2Utils.createTextureId()
    if (createTextureID != 0) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, createTextureID)
        val options = BitmapFactory.Options()
        options.inScaled = false
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )
        width = bitmap.width
        height = bitmap.height
        textureId = createTextureID
        bitmap.recycle()
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    } else {
        Log.e("TextureInfo", "textureId == 0 ")
    }

    return this
}

fun TextureInfo.generateBitmapTexture(createTextureID: Int, bitmap: Bitmap): TextureInfo {
    if (createTextureID != 0) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, createTextureID)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )
        width = bitmap.width
        height = bitmap.height
        textureId = createTextureID
        bitmap.recycle()
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    } else {
        Log.e("TextureInfo", "textureId == 0 ")
    }

    return this
}

fun TextureInfo.generateBitmapTexture(
    createTextureID: Int,
    context: Context,
    resourceId: Int
): TextureInfo {
    if (createTextureID != 0) {
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, createTextureID)
        val options = BitmapFactory.Options()
        options.inScaled = false
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )
        width = bitmap.width
        height = bitmap.height
        textureId = createTextureID
        bitmap.recycle()
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    } else {
        Log.e("TextureInfo", "textureId == 0 ")
    }

    return this
}

fun CoordinatePoint.toFloatSize(
    screenWidth: Float,
    screenHeight: Float
): Triple<Float, Float, Float> {
    val screenToGlCoordinate = MatrixUtil.screenToGlCoordinate(x, y, screenWidth, screenHeight)
    return Triple(screenToGlCoordinate[0], screenToGlCoordinate[1], c)
}

data class CoordinateRegion(
    var leftTop: CoordinatePoint = CoordinatePoint(),
    var rightTop: CoordinatePoint = CoordinatePoint(),
    var leftBottom: CoordinatePoint = CoordinatePoint(),
    var rightBottom: CoordinatePoint = CoordinatePoint()
)

fun CoordinateRegion.getTextureRect(): Rect{
    this.check()
    return Rect().apply {
        left = leftTop.x.toInt()
        top = leftTop.y.toInt()
        right = rightBottom.x.toInt()
        bottom = rightBottom.y.toInt()
    }
}

fun CoordinateRegion.generateCoordinateRegion(left: Float, top: Float, width: Int, height: Int) = this.apply {
    leftTop.x = left
    leftTop.y = top
    rightTop.x = left + width
    rightTop.y = top
    leftBottom.x = left
    leftBottom.y = top + height
    rightBottom.x = left + width
    rightBottom.y = top + height
}.check()

fun CoordinateRegion.getSurfaceArea(
    surfaceWidth: Float, surfaceHeight: Float, xBoundary: Float = 1.0f,
    yBoundary: Float = 1.0f
): CoordinateArea {
    check()
    val lTSurfacePoint =
        MatrixUtil.screenToGlCoordinate(
            leftTop.x,
            leftTop.y,
            surfaceWidth,
            surfaceHeight,
            xBoundary,
            yBoundary
        )
    val rBSurfacePoint =
        MatrixUtil.screenToGlCoordinate(
            rightBottom.x,
            rightBottom.y,
            surfaceWidth,
            surfaceHeight,
            xBoundary,
            yBoundary
        )
    return CoordinateArea(
        coordinateLeft = lTSurfacePoint[0],
        coordinateTop = lTSurfacePoint[1],
        coordinateRight = rBSurfacePoint[0],
        coordinateBottom = rBSurfacePoint[1]
    )
}


fun CoordinateRegion.check(): CoordinateRegion {
    if (leftTop.x != leftBottom.x || rightTop.x != rightBottom.x || leftTop.y != rightTop.y || leftBottom.y != rightBottom.y) {
        throw IllegalArgumentException("CoordinateRegion Argument is error")
    }
    return this
}

fun CoordinateRegion.copyCoordinateRegion(): CoordinateRegion {
    return CoordinateRegion(
        leftTop.copyCoordinatePoint(),
        rightTop.copyCoordinatePoint(),
        leftBottom.copyCoordinatePoint(),
        rightBottom.copyCoordinatePoint()
    )
}

fun CoordinateRegion.offSet(x: Float, y: Float): CoordinateRegion {
    return this.copyCoordinateRegion().apply {
        leftTop.x += x
        leftTop.y += y
        rightTop.x += x
        rightTop.y += y
        leftBottom.x += x
        leftBottom.y += y
        rightBottom.x += x
        rightBottom.y += y
    }
}


fun CoordinateRegion.getWidth(): Float {
    return abs(leftTop.x - rightTop.x)
}

fun CoordinateRegion.getHeight(): Float {
    return abs(leftTop.y - leftBottom.y)
}

fun CoordinateRegion.getFloatArray(screenWidth: Float, screenHeight: Float): FloatArray {
    val lt = leftTop.toFloatSize(screenWidth, screenHeight)
    val lb = leftBottom.toFloatSize(screenWidth, screenHeight)
    val rt = rightTop.toFloatSize(screenWidth, screenHeight)
    val rb = rightBottom.toFloatSize(screenWidth, screenHeight)
    return floatArrayOf(
        lt.first, lt.second, lt.third,
        lb.first, lb.second, lb.third,
        rb.first, rb.second, rb.third,
        rt.first, rt.second, rt.third
    )
}
