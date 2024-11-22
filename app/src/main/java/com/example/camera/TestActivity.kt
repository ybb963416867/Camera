package com.example.camera

import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.opengles.GL10

class TestActivity : AppCompatActivity() {

    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var frameLayout: FrameLayout
    private var surfaceTexture: SurfaceTexture? = null
    private var surface: Surface? = null
    private lateinit var start : Button
    private var renderThread = HandlerThread("RenderThread")
    private lateinit var renderHandler : Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_test)
        renderThread.start()
        renderHandler = Handler(renderThread.looper)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        start = findViewById(R.id.but_start)
        frameLayout = findViewById(R.id.fl)
        glSurfaceView = findViewById(R.id.glSurfaceView)
        glSurfaceView.setEGLContextClientVersion(2)
        glSurfaceView.setRenderer(MyRenderer())
        glSurfaceView.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY


        start.setOnClickListener {
            startDrawing()
        }
    }


    inner class MyRenderer : GLSurfaceView.Renderer {
        private var textureId = -1
        private var programId = -1
        private var positionHandle = -1
        private var textureCoordHandle = -1
        private var matrixHandle = -1
        private var textureHandle = -1

        private val vertexShaderCode = """
            attribute vec4 vPosition;
            attribute vec2 vCoord;
            uniform mat4 vMatrix;
            varying vec2 textureCoordinate;
            
            void main() {
                gl_Position = vMatrix * vPosition;
                textureCoordinate = vCoord;
            }
        """

        private val fragmentShaderCode = """
            precision mediump float;
            varying vec2 textureCoordinate;
            uniform sampler2D vTexture;
            
            void main() {
                gl_FragColor = texture2D(vTexture, textureCoordinate);
            }
        """

        private val vertexBuffer: FloatBuffer
        private val textureBuffer: FloatBuffer
        private val mvpMatrix = FloatArray(16)
        private val projectionMatrix = FloatArray(16)
        private val viewMatrix = FloatArray(16)

        init {
            // 顶点数据
            val vertices = floatArrayOf(
                -1.0f, -1.0f, 0.0f,  // 左下
                1.0f, -1.0f, 0.0f,   // 右下
                -1.0f, 1.0f, 0.0f,   // 左上
                1.0f, 1.0f, 0.0f     // 右上
            )
            vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(vertices)
                    position(0)
                }
            }

            // 纹理坐标数据
            val textureCoords = floatArrayOf(
                0.0f, 1.0f,  // 左下
                1.0f, 1.0f,  // 右下
                0.0f, 0.0f,  // 左上
                1.0f, 0.0f   // 右上
            )
            textureBuffer = ByteBuffer.allocateDirect(textureCoords.size * 4).run {
                order(ByteOrder.nativeOrder())
                asFloatBuffer().apply {
                    put(textureCoords)
                    position(0)
                }
            }
        }

        override fun onSurfaceCreated(
            gl: GL10?,
            config: javax.microedition.khronos.egl.EGLConfig?
        ) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
            GLES20.glEnable(GLES20.GL_DEPTH_TEST)
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

            // 创建纹理
            val textures = IntArray(1)
            GLES20.glGenTextures(1, textures, 0)
            textureId = textures[0]
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)

            // 创建SurfaceTexture
            surfaceTexture = SurfaceTexture(textureId)
            surface = Surface(surfaceTexture)
            surfaceTexture?.setOnFrameAvailableListener {
                glSurfaceView.queueEvent {
                    glSurfaceView.requestRender()
                }
            }

            // 编译和链接着色器
            val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
            val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
            programId = GLES20.glCreateProgram().also {
                GLES20.glAttachShader(it, vertexShader)
                GLES20.glAttachShader(it, fragmentShader)
                GLES20.glLinkProgram(it)
            }

            // 获取着色器变量的句柄
            positionHandle = GLES20.glGetAttribLocation(programId, "vPosition")
            textureCoordHandle = GLES20.glGetAttribLocation(programId, "vCoord")
            matrixHandle = GLES20.glGetUniformLocation(programId, "vMatrix")
            textureHandle = GLES20.glGetUniformLocation(programId, "vTexture")
        }

        override fun onDrawFrame(gl: GL10?) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            surfaceTexture?.updateTexImage()
            checkGLError()
            // 使用着色器程序
            GLES20.glUseProgram(programId)

            // 设置顶点数据
            GLES20.glEnableVertexAttribArray(positionHandle)
            vertexBuffer.position(0)
            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)

            // 设置纹理坐标数据
            GLES20.glEnableVertexAttribArray(textureCoordHandle)
            textureBuffer.position(0)
            GLES20.glVertexAttribPointer(textureCoordHandle, 2, GLES20.GL_FLOAT, false, 8, textureBuffer)

            // 设置MVP矩阵
            Matrix.setIdentityM(mvpMatrix, 0)
            Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0)
            GLES20.glUniformMatrix4fv(matrixHandle, 1, false, mvpMatrix, 0)

            // 绑定纹理
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
            GLES20.glUniform1i(textureHandle, 0)

            // 绘制
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

            // 禁用顶点数组
            GLES20.glDisableVertexAttribArray(positionHandle)
            GLES20.glDisableVertexAttribArray(textureCoordHandle)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
            val ratio = width.toFloat() / height.toFloat()
            Matrix.frustumM(projectionMatrix, 0, -ratio, ratio, -1f, 1f, 3f, 7f)
            Matrix.setLookAtM(viewMatrix, 0, 0f, 0f, -3f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)
        }

        private fun loadShader(type: Int, shaderCode: String): Int {
            return GLES20.glCreateShader(type).also { shader ->
                GLES20.glShaderSource(shader, shaderCode)
                GLES20.glCompileShader(shader)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
        stopDrawing()
        // 释放资源
        surface?.release()
        surface = null
        surfaceTexture?.release()
        surfaceTexture = null
    }

    private fun startDrawing() {
        renderHandler.post {
            surfaceTexture?.setDefaultBufferSize(frameLayout.width, frameLayout.height)
            val canvas = surface?.lockCanvas(null)
            if (canvas != null) {
                try {
                    canvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR)
                    frameLayout.draw(canvas)
                } finally {
                    surface?.unlockCanvasAndPost(canvas)
                }
            }
        }
    }

    private fun stopDrawing() {
        // 停止绘制线程
    }

    fun checkGLError() {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            Log.e("OpenGL", "GL Error: $error")
        }
    }

}