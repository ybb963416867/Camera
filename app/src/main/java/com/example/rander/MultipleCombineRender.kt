package com.example.rander

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.opengl.Matrix
import android.util.Log
import com.example.camera.R
import com.example.gpengl.multiple.CoordinateRegion
import com.example.gpengl.multiple.IBaseTexture
import com.example.gpengl.multiple.PicTextureT
import com.example.gpengl.multiple.generateBitmapTexture
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MultipleCombineRender(private var surfaceView: GLSurfaceView) : GLSurfaceView.Renderer {


    private val textures = IntArray(2) // 存储两个纹理 ID
    private val projectionMatrices = Array(2) { FloatArray(16) } // 每个纹理的正交投影矩阵
    var combinedProjectionMatrix: FloatArray = FloatArray(16)
    private var bitmap: Bitmap? = null
    private var program = 0

    // FBO 和合并纹理
    private val fbo = IntArray(1)
    private val combinedTexture = IntArray(1)

    private val vertexBuffer: FloatBuffer

    private val vertexBuffer1: FloatBuffer
    private val vertexBuffer2: FloatBuffer

    private val combinedVertexBuffer: FloatBuffer
    private var texCoordBuffer: FloatBuffer

    private val vertexShaderCode = "attribute vec4 a_Position;" +
            "attribute vec2 a_TexCoord;" +
            "varying vec2 v_TexCoord;" +
            "uniform mat4 u_ProjectionMatrix;" +
            "void main() {" +
            "  gl_Position = u_ProjectionMatrix * a_Position;" +
            "  v_TexCoord = a_TexCoord;" +
            "}"

    private val fragmentShaderCode = "precision mediump float;" +
            "uniform sampler2D u_Texture;" +
            "varying vec2 v_TexCoord;" +
            "void main() {" +
            "  gl_FragColor = texture2D(u_Texture, v_TexCoord);" +
            "}"

    // 顶点数据 (两个三角形组成一个正方形)，纹理坐标的Y方向已反转
//    private val vertexData = floatArrayOf( // X, Y, U, V (V坐标倒置)
//        -1.0f, -1.0f, 0.0f, 0.0f,
//        1.0f, -1.0f, 1.0f, 0.0f,
//        1.0f, 1.0f, 1.0f, 1.0f,
//        -1.0f, 1.0f, 0.0f, 1.0f
//    )

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

    private var vertices1: FloatArray = floatArrayOf(
        -1f, 1f, 0.0f,  // 左上角
        -1f, -1f, 0.0f,  // 左下角
        0f, -1f, 0.0f,  // 右下角
        0f, 1f, 0.0f // 右上角
    )

    private var vertices2: FloatArray = floatArrayOf(
        0f, 1f, 0.0f,  // 左上角
        0f, -1f, 0.0f,  // 左下角
        1f, -1f, 0.0f,  // 右下角
        1f, 1f, 0.0f // 右上角
    )

    private var aPositionHandle = 0
    private var aTexCoordHandle = 0
    private var uTextureHandle = 0
    private var uProjectionMatrixHandle = 0


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

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer.put(vertices).position(0)

        vertexBuffer1 = ByteBuffer.allocateDirect(vertices1.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer1.put(vertices1).position(0)

        vertexBuffer2 = ByteBuffer.allocateDirect(vertices2.size * 4).order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexBuffer2.put(vertices2).position(0)

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
        program = createProgram(vertexShaderCode, fragmentShaderCode)
        GLES20.glUseProgram(program)

        // 获取属性和 Uniform 位置
        aPositionHandle = GLES20.glGetAttribLocation(program, "a_Position")
        aTexCoordHandle = GLES20.glGetAttribLocation(program, "a_TexCoord")
        uTextureHandle = GLES20.glGetUniformLocation(program, "u_Texture")
        uProjectionMatrixHandle = GLES20.glGetUniformLocation(program, "u_ProjectionMatrix")

        // 加载图片并生成纹理
        bitmap = BitmapFactory.decodeResource(surfaceView.context.resources, R.mipmap.bg)
        GLES20.glGenTextures(2, textures, 0)

        for (i in textures.indices) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i])
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
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        }

        bitmap?.recycle()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        baseTextureList.forEach {
            it.onSurfaceChanged(width, height)
        }




        GLES20.glViewport(0, 0, width, height)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, combinedTexture[0])
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            width,
            height,
            0,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            null
        )
        // 设置合并纹理的正交投影矩阵，确保宽高比一致
        Matrix.setIdentityM(combinedProjectionMatrix, 0)
        Matrix.orthoM(combinedProjectionMatrix, 0, -1f, 1f, -1f, 1f, -1f, 1f)

        // 设置每个纹理的正交投影矩阵，保持宽高比一致
        val aspectRatio = bitmap!!.width.toFloat() / bitmap!!.height
        for (i in textures.indices) {
            Matrix.setIdentityM(projectionMatrices[i], 0)

            //            if (width > height) {
//                Matrix.orthoM(projectionMatrices[i], 0, -aspectRatio, aspectRatio, -1, 1, -1, 1);
//            } else {
//                Matrix.orthoM(projectionMatrices[i], 0, -1, 1, -1 / aspectRatio, 1 / aspectRatio, -1, 1);
//            }
//
//            float offset = (i == 0) ? -1f : 0f;
//            Matrix.translateM(projectionMatrices[i], 0, offset, 0, 0);
            Matrix.setIdentityM(projectionMatrices[i], 0)
            Matrix.orthoM(projectionMatrices[i], 0, -1f, 1f, -1f, 1f, -1f, 1f)
        }
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        // 使用着色器程序

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0])
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        // 绘制纹理到 FBO
        for (i in textures.indices) {
            GLES20.glUniformMatrix4fv(uProjectionMatrixHandle, 1, false, projectionMatrices[i], 0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i])
            GLES20.glUniform1i(uTextureHandle, 0)

            GLES20.glEnableVertexAttribArray(aPositionHandle)
            GLES20.glEnableVertexAttribArray(aTexCoordHandle)


            if (i == 0){
                GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer1)
            }else {
                GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer2)
            }
            GLES20.glVertexAttribPointer(aTexCoordHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBuffer)


            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)

            GLES20.glDisableVertexAttribArray(aPositionHandle)
            GLES20.glDisableVertexAttribArray(aTexCoordHandle)
        }

        baseTextureList.forEach {
            it.onDrawFrame()
        }

        // 解绑 FBO 并恢复视口大小
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0)
        GLES20.glViewport(0, 0, surfaceView.width, surfaceView.height)

        // 使用合并投影矩阵绘制合并纹理到屏幕
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, combinedTexture[0])
        GLES20.glUniformMatrix4fv(uProjectionMatrixHandle, 1, false, combinedProjectionMatrix, 0)

        GLES20.glEnableVertexAttribArray(aPositionHandle)
        GLES20.glEnableVertexAttribArray(aTexCoordHandle)
        GLES20.glVertexAttribPointer(aPositionHandle, 3, GLES20.GL_FLOAT, false, 12, combinedVertexBuffer)
        GLES20.glVertexAttribPointer(aTexCoordHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBuffer)


        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)

        GLES20.glDisableVertexAttribArray(aPositionHandle)
        GLES20.glDisableVertexAttribArray(aTexCoordHandle)

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

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vertexShader)
        GLES20.glAttachShader(program, fragmentShader)
        GLES20.glLinkProgram(program)

        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            GLES20.glDeleteProgram(program)
            throw RuntimeException("Error creating program.")
        }

        return program
    }



    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shader)
            throw RuntimeException("Error compiling shader: " + GLES20.glGetShaderInfoLog(shader))
        }

        return shader
    }

}