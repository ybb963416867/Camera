package com.example.gpengl.multiple

import android.annotation.SuppressLint
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Build
import android.view.MotionEvent
import androidx.annotation.RequiresApi
import com.example.util.Gl2Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

open class BaseTexture(
    private var surfaceView: GLSurfaceView,
    private var vertPath: String,
    private var fragPath: String
) : IBaseTexture {
    private var screenWidth = 0
    private var screenHeight = 0

    private var textureInfo = TextureInfo()
    private var vertexBuffer: FloatBuffer
    private var texCoordBuffer: FloatBuffer
    val matrix: FloatArray = Gl2Utils.originalMatrix
    var textureWidth = 0
    var textureHeight = 0

    private var positionHandle = 0
    private var texCoordHandle = 0
    private var uTextureHandle = 0
    private var matrixHandle = 0
    private var currentRegion = CoordinateRegion()
    private var iTextureVisibility = ITextureVisibility.INVISIBLE
    private var frameBuffer: IntBuffer? = null
    private var shaderProgram = 0

    private val texCoords = floatArrayOf(
        0.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
        1.0f, 0.0f
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

        currentRegion = coordinateRegion.copyCoordinateRegion()

        vertexBuffer.clear()
        val newVertices = currentRegion.getFloatArray(
            screenWidth = screenWidth.toFloat(), screenHeight = screenHeight.toFloat()
        )

        vertexBuffer.put(
            newVertices
        ).position(0)
    }

    override fun onSurfaceCreated() {
        initCoordinate()
        shaderProgram = Gl2Utils.createGlProgram(
            Gl2Utils.uRes(surfaceView.context.resources, vertPath),
            Gl2Utils.uRes(surfaceView.context.resources, fragPath)
        )

        Matrix.setIdentityM(matrix, 0)
        currentRegion =
            currentRegion.generateCoordinateRegion(0f, 0f, surfaceView.width, surfaceView.height)

        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition")
        texCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "vCoord")
        uTextureHandle = GLES20.glGetUniformLocation(shaderProgram, "vTexture")
        matrixHandle = GLES20.glGetUniformLocation(shaderProgram, "vMatrix")

        textureInfo.textureId = Gl2Utils.create2DTexture(1)[0]
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

    override fun updateTextureInfo(
        textureInfo: TextureInfo,
        isRecoverCord: Boolean,
        iTextureVisibility: ITextureVisibility
    ) {
        this.textureInfo.textureId = textureInfo.textureId
        textureWidth = textureInfo.width
        textureHeight = textureInfo.height
        this.iTextureVisibility = iTextureVisibility
        Matrix.setIdentityM(matrix, 0)
        if (isRecoverCord) {
            currentRegion = CoordinateRegion().generateCoordinateRegion(
                0f, 0f, screenWidth, screenHeight
            )
        }
        updateTexCord(currentRegion)

    }

    override fun updateTextureInfo(
        textureInfo: TextureInfo,
        isRecoverCord: Boolean,
        backgroundColor: String?,
        iTextureVisibility: ITextureVisibility
    ) {
        this.updateTextureInfo(textureInfo, isRecoverCord, iTextureVisibility)
    }

    override fun onDrawFrame() {
        if (iTextureVisibility == ITextureVisibility.VISIBLE) {
            GLES20.glUseProgram(shaderProgram)
            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glEnableVertexAttribArray(texCoordHandle)

            GLES20.glVertexAttribPointer(
                positionHandle,
                3,
                GLES20.GL_FLOAT,
                false,
                12,
                vertexBuffer
            )
            GLES20.glVertexAttribPointer(
                texCoordHandle,
                2,
                GLES20.GL_FLOAT,
                false,
                8,
                texCoordBuffer
            )

            GLES20.glUniformMatrix4fv(matrixHandle, 1, false, matrix, 0)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureInfo.textureId)
            GLES20.glUniform1i(uTextureHandle, 0)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)

            GLES20.glDisableVertexAttribArray(positionHandle)
            GLES20.glDisableVertexAttribArray(texCoordHandle)
        }
    }

    override fun initCoordinate() {

    }

    override fun getVisibility(): ITextureVisibility {
        return iTextureVisibility
    }

    override fun setVisibility(visibility: ITextureVisibility) {
        this.iTextureVisibility = visibility
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun clearTexture(colorString: String) {
        surfaceView.queueEvent {
            if (textureInfo.width == 0) {
                textureInfo.width = getScreenWidth()
            }

            if (textureInfo.height == 0) {
                textureInfo.height = getScreenHeight()
            }

            val genColorImage =
                Gl2Utils.genColorImage(textureInfo.width, textureInfo.height, "#00000000")
            if (frameBuffer == null || frameBuffer!!.capacity() != textureInfo.width * textureInfo.height * Integer.BYTES) {
                frameBuffer =
                    ByteBuffer.allocateDirect(textureInfo.width * textureInfo.height * Integer.BYTES)
                        .order(
                            ByteOrder.nativeOrder()
                        ).asIntBuffer().apply {
                            position(0)
                        }
            }

            frameBuffer!!.put(genColorImage).position(0)

            updateTextureInfo(
                getTextureInfo().generateTexture(
                    getTextureInfo().textureId,
                    frameBuffer!!,
                    textureInfo.width,
                    textureInfo.height
                ), false
            )

            surfaceView.requestRender()
        }
    }

    override fun release() {
        GLES20.glDeleteProgram(shaderProgram)
        GLES20.glDeleteTextures(1, IntArray(textureInfo.textureId), 0)
    }

    override fun acceptTouchEvent(event: MotionEvent): Boolean {
        return false
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(baseTexture: IBaseTexture, event: MotionEvent): Boolean {
        return false
    }

}
