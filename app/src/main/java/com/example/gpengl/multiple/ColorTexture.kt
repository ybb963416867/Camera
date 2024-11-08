package com.example.gpengl.multiple

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceView
import com.example.rander.ColorRender
import com.example.util.Gl2Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.Arrays

class ColorTexture(
    private var surfaceView: SurfaceView, private var vertPath: String = "shader/base_vert.glsl",
    private var fragPath: String = "shader/base_frag.glsl"
) : IBaseTexture {
    private var screenWidth = 0
    private var screenHeight = 0

    private var textureInfo = TextureInfo()
    private var vertexBuffer: FloatBuffer
    private var texCoordBuffer: FloatBuffer
    val matrix: FloatArray = Gl2Utils.getOriginalMatrix()
    private var textureWidth = 0
    private var textureHeight = 0

    private var positionHandle = 0
    private var texCoordHandle = 0
    private var uTextureHandle = 0
    private var matrixHandle = 0
    private var currentRegion = CoordinateRegion()

    private var colorBuffer: IntBuffer? = null

    private val texCoords = floatArrayOf(
        0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f
    )

    private var vertices: FloatArray = floatArrayOf(
        -1f, 1f, 0.0f,  // 左上角
        -1f, -1f, 0.0f,  // 左下角
        1f, -1f, 0.0f,  // 右下角
        1f, 1f, 0.0f // 右上角
    )

    init {
        texCoordBuffer =
            ByteBuffer.allocateDirect(texCoords.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        texCoordBuffer.put(texCoords).position(0)

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(vertices).position(0)
    }


    override fun updateTexCord(coordinateRegion: CoordinateRegion) {
        GLES20.glViewport(
            coordinateRegion.leftTop.x.toInt(),
            coordinateRegion.leftTop.y.toInt(),
            coordinateRegion.getWidth().toInt(),
            coordinateRegion.getHeight().toInt()
        )

        currentRegion = coordinateRegion.copyCoordinateRegion()

        vertexBuffer.clear()
        val newVertices = currentRegion.getFloatArray(
            screenWidth = screenWidth.toFloat(), screenHeight = screenHeight.toFloat()
        )

        Log.e("ybb", "newVertices: " + newVertices.contentToString())
        vertexBuffer.put(
            newVertices
        ).position(0)


        val genColorImage = genColorImage(
            coordinateRegion.getWidth().toInt(),
            coordinateRegion.getHeight().toInt(),
            (0xffA728F0).toInt()
        )
        colorBuffer = IntBuffer.allocate(genColorImage.size).apply {
            put(genColorImage).position(0)
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureInfo.textureId)
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            coordinateRegion.getWidth().toInt(),
            coordinateRegion.getHeight().toInt(),
            0,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            colorBuffer
        )

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

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    override fun onSurfaceCreated() {

        val shaderProgram = Gl2Utils.createGlProgram(
            Gl2Utils.uRes(surfaceView.context.resources, vertPath),
            Gl2Utils.uRes(surfaceView.context.resources, fragPath)
        )

        GLES20.glLinkProgram(shaderProgram)

        GLES20.glUseProgram(shaderProgram)

        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition")
        texCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "vCoord")
        uTextureHandle = GLES20.glGetUniformLocation(shaderProgram, "vTexture")
        matrixHandle = GLES20.glGetUniformLocation(shaderProgram, "vMatrix")

        textureInfo.textureId = Gl2Utils.createTextureID(1)[0]
    }

    override fun onSurfaceChanged(screenWidth: Int, screenHeight: Int) {
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
    }

    override fun getTextureInfo(): TextureInfo {
        return textureInfo
    }

    override fun getScreenWidth(): Int {
        return screenWidth
    }

    override fun getScreenHeight(): Int {
        return screenHeight
    }

    override fun getTexCoordinateRegion(): CoordinateRegion {
        return currentRegion
    }

    override fun updateTextureInfo(textureInfo: TextureInfo, isRecoverCord: Boolean) {
        Matrix.setIdentityM(matrix, 0)
        textureWidth = textureInfo.width
        textureHeight = textureInfo.height

        val genColorImage = genColorImage(screenWidth, screenHeight, (0xffA728F0).toInt())
        colorBuffer = IntBuffer.allocate(genColorImage.size).apply {
            put(genColorImage).position(0)
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textureInfo.textureId)
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            screenWidth,
            screenHeight,
            0,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            colorBuffer
        )

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

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        if (isRecoverCord) {
            updateTexCord(
                CoordinateRegion().generateCoordinateRegion(
                    0f, 0f, screenWidth, screenHeight
                )
            )
        }
    }

    override fun onDrawFrame() {
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(texCoordHandle)

        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBuffer)

        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, matrix, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureInfo.textureId)
        GLES20.glUniform1i(uTextureHandle, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    override fun acceptTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    override fun onTouch(baseTexture: IBaseTexture, event: MotionEvent): Boolean {
        return false
    }

    fun genColorImage(width: Int, height: Int, color: Int): IntArray {
        val pixels = IntArray(width * height)
        // 重新组合成小端格式的整数
        val colorPixel = ColorRender.convertToLittleEndian(color)

        Log.d("ybb", "colorPixel: $colorPixel")
        Arrays.fill(pixels, colorPixel)
        return pixels
    }

}