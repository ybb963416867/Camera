package com.example.rander

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.filter.PicFilter
import com.example.gpengl.multiple.CoordinateRegion
import com.example.gpengl.multiple.IBaseTexture
import com.example.gpengl.multiple.PicTextureT
import com.example.gpengl.multiple.generateBitmapTexture
import com.example.gpengl.multiple.generateCoordinateRegion
import com.example.gpengl.multiple.getHeight
import com.example.gpengl.multiple.getWidth
import com.example.gpengl.multiple.offSet
import com.example.util.Gl2Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MultipleCombineRender(private var surfaceView: GLSurfaceView) : GLSurfaceView.Renderer {

    private var shaderProgram = 0
    private var baseTextureList = listOf<IBaseTexture>(
        PicTextureT(surfaceView.context),
//        PicTexture(surfaceView.context),
//        PicTexture(surfaceView.context),
//        PicTextureT(surfaceView.context),
//        PicTextureT(surfaceView.context),
//        PicTexture(surfaceView.context),
    )

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

    private var vertexBuffer: FloatBuffer
    private var texCoordBuffer: FloatBuffer = ByteBuffer.allocateDirect(texCoords.size * 4).order(ByteOrder.nativeOrder())
        .asFloatBuffer()
    private var picFilter: PicFilter

    private val fbo = IntArray(1)
    private val combinedTexture = IntArray(1)

    val matrix: FloatArray = Gl2Utils.getOriginalMatrix()

    init {
        texCoordBuffer.put(texCoords).position(0)

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(vertices).position(0)

        picFilter = PicFilter(surfaceView.context.resources)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.96f, 0.8f, 0.156f, 1.0f)
        picFilter.create()
        GLES20.glGenFramebuffers(1, fbo, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0])

        // 创建合并纹理
        GLES20.glGenTextures(1, combinedTexture, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, combinedTexture[0])
        Log.d("ybb", "width = " + surfaceView.width + "height = " + surfaceView.height)
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            surfaceView.width,
            surfaceView.height,
            0,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            null
        )

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)


        // 将合并纹理附加到 FBO
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
            combinedTexture[0], 0
        )

        // 检查 FBO 状态
        val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("GLRenderer", "Framebuffer incomplete: $status")
        }


        // 解绑 FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)


        shaderProgram = Gl2Utils.createGlProgram(
            Gl2Utils.uRes(surfaceView.context.resources, "shader/base_vert.glsl"),
            Gl2Utils.uRes(surfaceView.context.resources, "shader/base_frag.glsl")
        )

        GLES20.glLinkProgram(shaderProgram)
        baseTextureList.forEach {
            it.onSurfaceCreated()
        }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        picFilter.setSize(width, height)
        Matrix.orthoM(matrix, 0, -1f, 1f, -1f, 1f, -1f, 1f)
        baseTextureList.forEach {
            it.onSurfaceChanged(width, height)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0])
//        // 将合并纹理附加到 FBO
        GLES20.glFramebufferTexture2D(
            GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D,
            combinedTexture[0], 0
        )

        val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("GLRenderer", "Framebuffer incomplete: $status")
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        // 使用着色器程序
        GLES20.glUseProgram(shaderProgram)
        baseTextureList.forEach {
            it.onDrawFrame(shaderProgram)
        }

        // 解绑 FBO 并恢复视口大小
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glViewport(0, 0, surfaceView.width, surfaceView.height)
        //处理后的纹理，也就是黑白图的纹理
        picFilter.draw()

        // 使用合并投影矩阵绘制合并纹理到屏幕
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
//        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, combinedTexture[0])
//
//        val positionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition")
//        val texCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "vCoord")
//        val textureUniform = GLES20.glGetUniformLocation(shaderProgram, "vTexture")
//        val matrixHandle = GLES20.glGetUniformLocation(shaderProgram, "vMatrix")
//
//        GLES20.glEnableVertexAttribArray(positionHandle)
//        GLES20.glEnableVertexAttribArray(texCoordHandle)
//
//        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)
//        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBuffer)
//
//        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, matrix, 0)
//
//        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
////        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, combinedTexture[0])
//        GLES20.glUniform1i(textureUniform, 0)
//
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)
//
//        GLES20.glDisableVertexAttribArray(positionHandle)
//        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    fun updateTexCord(coordinateRegion: CoordinateRegion) {
        baseTextureList.forEachIndexed { index, iBaseTexture ->
            when (index) {
                2 -> {
                    iBaseTexture.updateTexCord(
                        coordinateRegion.offSet(
                            0f,
                            coordinateRegion.getHeight() + 50f
                        )
                    )
                }

                3 -> {
                    iBaseTexture.updateTexCord(
                        coordinateRegion.offSet(
                            0f,
                            coordinateRegion.getHeight() * 2 + 100f
                        )
                    )
                }

                0 -> {
                    iBaseTexture.updateTexCord(coordinateRegion)
                }

                4, 5 -> {
                    iBaseTexture.updateTexCord(
                        CoordinateRegion().generateCoordinateRegion(
                            (iBaseTexture.getScreenWidth() - coordinateRegion.getWidth() - 200),
                            200f,
                            coordinateRegion.getWidth().toInt(),
                            coordinateRegion.getHeight().toInt()
                        )
                    )
                }

                else -> {
                    iBaseTexture.updateTexCord(coordinateRegion)
                }
            }
        }
    }


    fun loadTexture(resourceId: Int) {
        baseTextureList.forEachIndexed { index, iBaseTexture ->
            when (index) {
                4, 5 -> iBaseTexture.updateTextureInfo(
                    iBaseTexture.getTextureInfo()
                        .generateBitmapTexture(surfaceView.context, resourceId), true
                )

                else -> iBaseTexture.updateTextureInfo(
                    iBaseTexture.getTextureInfo()
                        .generateBitmapTexture(surfaceView.context, resourceId)
                )
            }

        }
    }

}