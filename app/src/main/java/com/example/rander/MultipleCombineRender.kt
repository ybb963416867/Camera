package com.example.rander
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import com.example.gpengl.multiple.CoordinateRegion
import com.example.gpengl.multiple.IBaseTexture
import com.example.gpengl.multiple.PicTextureT
import com.example.gpengl.multiple.generateBitmapTexture
import com.example.util.Gl2Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MultipleCombineRender(private var surfaceView: GLSurfaceView) : GLSurfaceView.Renderer {

    private var combinedProjectionMatrix: FloatArray = FloatArray(16)
    private var program = 0

    // FBO 和合并纹理
    private val fbo = IntArray(1)
    private val combinedTexture = IntArray(1)


    private val combinedVertexBuffer: FloatBuffer
    private var texCoordBuffer: FloatBuffer

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

    private var positionHandle = 0
    private var texCoordHandle = 0
    private var uTextureHandle = 0
    private var matrixHandle = 0


    private var baseTextureList = listOf<IBaseTexture>(
        PicTextureT(surfaceView.context),
//        PicTexture(surfaceView.context),
//        PicTexture(surfaceView.context),
//        PicTextureT(surfaceView.context),
//        PicTextureT(surfaceView.context),
//        PicTexture(surfaceView.context),
    )


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


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.96f, 0.8f, 0.156f, 1.0f)

        baseTextureList.forEach {
            it.onSurfaceCreated()
        }

        Matrix.setIdentityM(combinedProjectionMatrix, 0)
        GLES20.glGenFramebuffers(1, fbo, 0)
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0])

        // 创建合并纹理
        GLES20.glGenTextures(1, combinedTexture, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, combinedTexture[0])
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
            GLES20.GL_FRAMEBUFFER,
            GLES20.GL_COLOR_ATTACHMENT0,
            GLES20.GL_TEXTURE_2D,
            combinedTexture[0],
            0
        )
        // 检查 FBO 状态
        val status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER)
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("GLRenderer", "Framebuffer incomplete: $status")
        }

        // 解绑 FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)


        // 创建 OpenGL 程序

        program = Gl2Utils.createGlProgram(
            Gl2Utils.uRes(surfaceView.context.resources, "shader/base_vert.glsl"),
            Gl2Utils.uRes(surfaceView.context.resources, "shader/base_frag.glsl")
        )

        GLES20.glLinkProgram(program)

        GLES20.glUseProgram(program)

        // 获取属性和 Uniform 位置
        positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        texCoordHandle = GLES20.glGetAttribLocation(program, "vCoord")
        uTextureHandle = GLES20.glGetUniformLocation(program, "vTexture")
        matrixHandle = GLES20.glGetUniformLocation(program, "vMatrix")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        baseTextureList.forEach {
            it.onSurfaceChanged(width, height)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        // 使用着色器程序

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0])
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        baseTextureList.forEach {
            it.onDrawFrame()
        }

        // 解绑 FBO 并恢复视口大小
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glViewport(0, 0, surfaceView.width, surfaceView.height)

        // 使用合并投影矩阵绘制合并纹理到屏幕
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, combinedTexture[0])
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, combinedProjectionMatrix, 0)

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, combinedVertexBuffer)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBuffer)


        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)

    }

    fun updateTexCord(coordinateRegion: CoordinateRegion) {
        baseTextureList.forEach {
            it.updateTexCord(coordinateRegion)
        }
    }


    fun loadTexture(resourceId: Int) {
        baseTextureList.forEach {
            it.updateTextureInfo(it.getTextureInfo().generateBitmapTexture(it.getTextureInfo().textureId, surfaceView.context, resourceId), true)
        }
    }


}