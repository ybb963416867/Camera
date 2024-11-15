package com.example.gpengl.multiple

import android.content.Context
import android.opengl.GLES20
import android.opengl.Matrix
import android.util.Log
import com.example.util.Gl2Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class MultipleFboCombineTexture(
    numFbo: Int,
    private var context: Context,
    private var vertPath: String = "shader/base_vert.glsl",
    private var fragPath: String = "shader/base_frag.glsl"
) : IBaseFboCombineTexture {

    private var combinedProjectionMatrix: FloatArray = FloatArray(16)

    // FBO 和合并纹理
    private var fbo: IntArray = IntArray(numFbo)
    private var combinedTexture: IntArray = IntArray(numFbo)


    private val combinedVertexBuffer: FloatBuffer
    private var texCoordBuffer: FloatBuffer

//    private val texCoords = floatArrayOf(
//        0.0f, 0.0f,
//        0.0f, 1.0f,
//        1.0f, 1.0f,
//        1.0f, 0.0f
//    )

    private val texCoords = floatArrayOf(
        0.0f, 1.0f,
        0.0f, 0.0f,
        1.0f, 0.0f,
        1.0f, 1.0f
    )

    private var vertices: FloatArray = floatArrayOf(
        -1f, 1f, 0.0f,  // 左上角
        -1f, -1f, 0.0f,  // 左下角
        1f, -1f, 0.0f,  // 右下角
        1f, 1f, 0.0f // 右上角
    )

    private var positionHandle = 0
    private var texCoordHandle = 0
    private var uTextureHandle = 0
    private var matrixHandle = 0
    private var screenWidth = 0
    private var screenHeight = 0

    private var program = 0

    init {
        texCoordBuffer =
            ByteBuffer.allocateDirect(texCoords.size * 4).order(ByteOrder.nativeOrder())
                .asFloatBuffer()
        texCoordBuffer.put(texCoords).position(0)


        val aa = ByteBuffer.allocateDirect(vertices.size * 4)
        aa.order(ByteOrder.nativeOrder())
        combinedVertexBuffer = aa.asFloatBuffer()
        combinedVertexBuffer.put(vertices)
        combinedVertexBuffer.position(0)
    }

    override fun getTextureArray(): IntArray {
        return combinedTexture
    }

    override fun getScreenWidth(): Int {
        return screenWidth
    }

    override fun getScreenHeight(): Int {
        return screenHeight
    }

    override fun getFboFrameBuffer(): IntArray {
        return fbo
    }

    override fun onSurfaceCreated(screenWidth: Int, screenHeight: Int) {
        Matrix.setIdentityM(combinedProjectionMatrix, 0)
        GLES20.glGenFramebuffers(2, fbo, 0)

        // 创建合并纹理
        GLES20.glGenTextures(combinedTexture.size, combinedTexture, 0)

        for (i in combinedTexture.indices) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, combinedTexture[i])
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                screenWidth,
                screenHeight,
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                null
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

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[i])

            // 将合并纹理附加到 FBO
            GLES20.glFramebufferTexture2D(
                GLES20.GL_FRAMEBUFFER,
                GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D,
                combinedTexture[i],
                0
            )
            // 检查 FBO 状态
            val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
            if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                Log.e("GLRenderer", "Framebuffer incomplete: $status")
            }
            // 解绑 FBO
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        }

        // 创建 OpenGL 程序

        program = Gl2Utils.createGlProgram(
            Gl2Utils.uRes(context.resources, vertPath), Gl2Utils.uRes(context.resources, fragPath)
        )

        GLES20.glUseProgram(program)

        // 获取属性和 Uniform 位置
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        texCoordHandle = GLES20.glGetAttribLocation(program, "vCoord")
        uTextureHandle = GLES20.glGetUniformLocation(program, "vTexture")
        matrixHandle = GLES20.glGetUniformLocation(program, "vMatrix")
    }

    override fun onSurfaceChanged(screenWidth: Int, screenHeight: Int) {
        this.screenWidth = screenWidth
        this.screenHeight = screenHeight

        for (element in combinedTexture) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, element)
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                screenWidth,
                screenHeight,
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                null
            )
        }

        // 设置合并纹理的正交投影矩阵，确保宽高比一致
        Matrix.setIdentityM(combinedProjectionMatrix, 0)
    }

    override fun onDrawFrame(textureIdIndex: Int) {
        if (textureIdIndex >= combinedTexture.size) {
            throw IllegalArgumentException("textureIdIndex >= combinedTexture.size")
        }
        // 解绑 FBO 并恢复视口大小
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glViewport(0, 0, screenWidth, screenHeight)

        // 使用合并投影矩阵绘制合并纹理到屏幕
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, combinedTexture[textureIdIndex])
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, combinedProjectionMatrix, 0)

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(
            positionHandle, 3, GLES20.GL_FLOAT, false, 12, combinedVertexBuffer
        )
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBuffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    override fun release() {
        GLES20.glDeleteTextures(combinedTexture.size, combinedTexture, 0)
        GLES20.glDeleteFramebuffers(fbo.size, fbo, 0)
        GLES20.glDeleteProgram(program)
    }
}