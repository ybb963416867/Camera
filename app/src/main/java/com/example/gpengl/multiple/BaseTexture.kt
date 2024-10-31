package com.example.gpengl.multiple

import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.example.util.Gl2Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

open class BaseTexture : IBaseTexture {
    var screenWith = 0
    var screenHeight = 0

    var textureInfo1 = TextureInfo()
    private var vertexBuffer: FloatBuffer
    private var texCoordBuffer: FloatBuffer
    val matrix: FloatArray = Gl2Utils.getOriginalMatrix()
    var textureWidth = 0
    var textureHeight = 0

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


    override fun updateTexCord(coordinateRegion: CoordinateRegion){
        Log.e("ybb", coordinateRegion.toString())

        GLES20.glViewport(
            coordinateRegion.leftTop.x.toInt(),
            coordinateRegion.leftTop.y.toInt(),
            coordinateRegion.getWidth().toInt(),
            coordinateRegion.getHeight().toInt()
        )

        vertexBuffer.clear()
        val newVertices = coordinateRegion.getFloatArray(
            screenWidth = screenWith.toFloat(),
            screenHeight = screenHeight.toFloat()
        )

        Log.e("ybb", "newVertices: " + newVertices.contentToString())
        vertexBuffer.put(
            newVertices
        ).position(0)
    }

    override fun loadBitmapTexture(resourceId: Int){

    }


    override fun onSurfaceCreated() {
        textureInfo1.textureId = Gl2Utils.createTextureID(1)[0]
    }

    override fun onSurfaceChanged(screenWith: Int, screenHeight: Int) {
        this.screenWith = screenWith
        this.screenHeight = screenHeight
    }

    override fun updateTextureInfo(textureInfo: TextureInfo){
        this.textureInfo1.textureId = textureInfo.textureId
        Matrix.setIdentityM(matrix, 0)
        textureWidth = textureInfo.width
        textureHeight = textureInfo.height
    }

    override fun getTextureId(): Int {
        return textureInfo1.textureId
    }

    override fun onDrawFrame(shaderProgram: Int) {
        val positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition")
        val texCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "vCoord")
        val textureUniform = GLES20.glGetUniformLocation(shaderProgram, "vTexture")
        val matrixHandle = GLES20.glGetUniformLocation(shaderProgram, "vMatrix")

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(texCoordHandle)

        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBuffer)

        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, matrix, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureInfo1.textureId)
        GLES20.glUniform1i(textureUniform, 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

}
