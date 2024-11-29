package com.example.gpengl.multiple

import android.annotation.SuppressLint
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.view.MotionEvent
import com.example.util.Gl2Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

open class BaseOesTexture(
    private var surfaceView: GLSurfaceView,
    private var vertPath: String,
    private var fragPath: String
) : IBaseTexture {

    private var screenWidth = 0
    private var screenHeight = 0

    private var textureInfo = TextureInfo()
    private var vertexBuffer: FloatBuffer
    var texCoordBuffer: FloatBuffer
    val matrix: FloatArray = Gl2Utils.originalMatrix.copyOf()
    val coordsMatrix: FloatArray = Gl2Utils.originalMatrix.copyOf()
    var textureWidth = 0
    var textureHeight = 0

    private var positionHandle = 0
    private var texCoordHandle = 0
    private var uTextureHandle = 0
    private var matrixHandle = 0
    private var mHCoordMatrix = 0
    private var currentRegion = CoordinateRegion()
    private var iTextureVisibility = ITextureVisibility.INVISIBLE
    private var shaderProgram = 0
    private var vbo = IntArray(2)

//    private val texCoords = floatArrayOf(
//        0.0f, 1.0f,
//        0.0f, 0.0f,
//        1.0f, 0.0f,
//        1.0f, 1.0f
//    )
//
//    private var vertices: FloatArray = floatArrayOf(
//        -1f, 1f, 0.0f,  // 左上角
//        -1f, -1f, 0.0f,  // 左下角
//        1f, -1f, 0.0f,  // 右下角
//        1f, 1f, 0.0f // 右上角
//    )

    /**
     * 图片的定点坐标
     */
    private val vertices = floatArrayOf(
        -1.0f, -1.0f, 0.0f,   //top left
        1.0f, -1.0f, 0.0f,  //bottom left
        -1.0f, 1.0f, 0.0f,  // top right
        1.0f, 1.0f, 0.0f,  //bottom right
    )

    /**
     * 纹理坐标
     */
    private val texCoords = floatArrayOf(
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
    )

//    /**
//     * 纹理坐标
//     */
//    private val texCoords = floatArrayOf(
//        0.0f, 1.0f,
//        1.0f, 1.0f,
//        0.0f, 0.0f,
//        1.0f, 0.0f,
//    )


    init {
        texCoordBuffer =
            ByteBuffer.allocateDirect(texCoords.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer().apply {
                    put(texCoords).position(0)
                }
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                put(vertices).position(0)
            }
    }

    override fun updateTexCord(coordinateRegion: CoordinateRegion) {
        currentRegion = coordinateRegion.copyCoordinateRegion()

        vertexBuffer.clear()
        val newVertices = currentRegion.getOESFloatArray(
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

        GLES20.glGenBuffers(vbo.size, vbo, 0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            vertexBuffer.capacity() * 4,
            vertexBuffer,
            GLES20.GL_STATIC_DRAW
        )

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[1])
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER,
            texCoordBuffer.capacity() * 4,
            texCoordBuffer,
            GLES20.GL_STATIC_DRAW
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        Matrix.setIdentityM(matrix, 0)
        Matrix.setIdentityM(coordsMatrix, 0)
        currentRegion =
            currentRegion.generateCoordinateRegion(0f, 0f, surfaceView.width, surfaceView.height)

        positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition")
        texCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "vCoord")
        uTextureHandle = GLES20.glGetUniformLocation(shaderProgram, "vTexture")
        matrixHandle = GLES20.glGetUniformLocation(shaderProgram, "vMatrix")
        mHCoordMatrix = GLES20.glGetUniformLocation(shaderProgram, "vCoordMatrix")

        textureInfo.textureId = Gl2Utils.createOESTextureID(1)[0]
    }

    override fun onSurfaceChanged(screenWidth: Int, screenHeight: Int) {
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight
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
        Matrix.setIdentityM(coordsMatrix, 0)
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
            GLES30.glEnable(GLES30.GL_BLEND)
            GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)
            GLES20.glUseProgram(shaderProgram)

            GLES20.glEnableVertexAttribArray(positionHandle)
            GLES20.glEnableVertexAttribArray(texCoordHandle)

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
            GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                0,
                vertexBuffer.capacity() * 4,
                vertexBuffer
            )
            GLES20.glVertexAttribPointer(
                positionHandle,
                3,
                GLES20.GL_FLOAT,
                false,
                12,
                0
            )

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[1])
            GLES20.glBufferSubData(
                GLES20.GL_ARRAY_BUFFER,
                0,
                texCoordBuffer.capacity() * 4,
                texCoordBuffer
            )

            GLES20.glVertexAttribPointer(
                texCoordHandle,
                2,
                GLES20.GL_FLOAT,
                false,
                8,
                0
            )

            GLES20.glUniformMatrix4fv(matrixHandle, 1, false, matrix, 0)
            GLES20.glUniformMatrix4fv(mHCoordMatrix, 1, false, coordsMatrix, 0)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE1)
            GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureInfo.textureId)
            GLES20.glUniform1i(uTextureHandle, 1)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

            GLES20.glDisableVertexAttribArray(positionHandle)
            GLES20.glDisableVertexAttribArray(texCoordHandle)
            GLES30.glDisable(GLES30.GL_BLEND)
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        }
    }

    override fun initCoordinate() {

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


    override fun getVisibility(): ITextureVisibility {
        return iTextureVisibility
    }

    override fun setVisibility(visibility: ITextureVisibility) {
        this.iTextureVisibility = visibility
    }

    override fun clearTexture(colorString: String) {

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