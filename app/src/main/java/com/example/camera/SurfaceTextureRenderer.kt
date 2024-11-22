package com.example.camera

import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.opengl.GLES11Ext
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Surface
import android.widget.FrameLayout
import java.nio.FloatBuffer
import java.util.Random
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

internal class SurfaceTextureRenderer(glSurfaceView: GLSurfaceView) : GLSurfaceView.Renderer,
    OnFrameAvailableListener {
    // テクスチャの大きさ
    private var m_iTextureWidth = 0
    private var m_iTextureHeight = 0

    // GL関連
    private var m_iProgram = 0
    private var mHPosition = 0
    private var mHCoord = 0
    private var uTextureHandle = 0
    private var matrixHandle = 0
    private var mHCoordMatrix = 0
    private var m_iTextureID = 0
    private var posVBO = 0
    private var coordVBO = 0

    //	private final FloatBuffer    m_fbVertex;
    /**
     * 定点坐标的buffer,里面存放定点坐标数据
     */
    protected var mVerBuffer: FloatBuffer

    /**
     * 纹理坐标的buffer 里面存放纹理坐标
     */
    protected var mTexBuffer: FloatBuffer

    private val m_f16MVPMatrix = FloatArray(16)
    private val m_f16STMatrix = FloatArray(16)

    // Surface関連
    private var m_surface: Surface? = null
    private var m_surfacetexture: SurfaceTexture? = null
    private var m_bNewFrameAvailable = false

    // ランダムサークル描画用メンバ変数
    private val m_paint = Paint()
    private val m_random = Random()
    private val m_handlerDrawInSurface = Handler(Looper.getMainLooper())
    private val glSurfaceView: GLSurfaceView

    /**
     * 图片的定点坐标
     */
    private val pos = floatArrayOf(
        -1.0f, -1.0f,0.0f,   //top left
        1.0f, -1.0f,0.0f,  //bottom left
        -1.0f, 1.0f,0.0f,  // top right
        1.0f, 1.0f,0.0f,  //bottom right

    )

    /**
     * 纹理坐标
     */
    private val coord = floatArrayOf(
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.0f,
    )

//    private val coord = floatArrayOf(
//        0.0f, 0.0f,
//        0.0f, 1.0f,
//        1.0f, 1.0f,
//        1.0f, 0.0f
//    )
//
//    private var pos: FloatArray = floatArrayOf(
//        -1f, 1f,  // 左上角
//        -1f, -1f,  // 左下角
//        1f, -1f,  // 右下角
//        1f, 1f, // 右上角
//    )

    private val m_runnableDrawInSurface = Runnable {
        //			drawInSurface();
//			m_handlerDrawInSurface.postDelayed( m_runnableDrawInSurface, 100 );
    }

    // シェーダーコード vec4(vCoord, 0, 1)
    private val m_strVertexShaderCode = """
        uniform mat4 vMatrix;
        uniform mat4 vCoordMatrix;
        attribute vec4 vPosition;
        attribute vec2 vCoord;
        varying vec2 varyingTexCoord;
        void main() {
          gl_Position = vMatrix * vPosition;
          varyingTexCoord = (vCoordMatrix * vec4(vCoord, 0, 1)).xy;
        }
"""

    // 1行目：GL_TEXTURE_EXTERNAL_OESテクスチャを使用するための宣言。
    // 4行目：sampler2D変数の代わりにsamplerExternalOES変数を定義。
    private val m_strFragmentShaderCode = """#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 varyingTexCoord;
uniform samplerExternalOES vTexture;
void main() {
  gl_FragColor = texture2D(vTexture, varyingTexCoord);
}
"""

    // コンストラクタ
    init {
        Log.d(TAG, "Constructor")
        Log.d(TAG, "Thread name = " + Thread.currentThread().name) // Thread name = main
        this.glSurfaceView = glSurfaceView
        //		float[] m_afVertex = {
//				// X, Y, Z, U, V
//				-1.0f, -1.0f, 0.0f, 0.0f, 0.0f,
//				1.0f, -1.0f, 0.0f, 1.0f, 0.0f,
//				-1.0f, 1.0f, 0.0f, 0.0f, 1.0f,
//				1.0f, 1.0f, 0.0f, 1.0f, 1.0f,
//				};
        mVerBuffer = MyUtils.makeFloatBuffer(pos)

        mTexBuffer = MyUtils.makeFloatBuffer(coord)

        Matrix.setIdentityM(m_f16MVPMatrix, 0)
        Matrix.setIdentityM(m_f16STMatrix, 0)
    }

    override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
        Log.d(TAG, "onSurfaceCreated")
        Log.d(TAG, "Thread name = " + Thread.currentThread().name) // Thread name = GLThread XXXX

        m_iProgram = MyUtils.createProgram(m_strVertexShaderCode, m_strFragmentShaderCode)
        if (0 == m_iProgram) {
            return
        }

        // AttributeLocation
        mHPosition = GLES20.glGetAttribLocation(m_iProgram, "vPosition")
        MyUtils.checkGlError("glGetAttribLocation vPosition")
        mHCoord = GLES20.glGetAttribLocation(m_iProgram, "vCoord")
        MyUtils.checkGlError("glGetAttribLocation vCoord")

        uTextureHandle = GLES20.glGetUniformLocation(m_iProgram, "vTexture")

        // UniformLocation
        matrixHandle = GLES20.glGetUniformLocation(m_iProgram, "vMatrix")
        MyUtils.checkGlError("glGetUniformLocation vMatrix")
        mHCoordMatrix = GLES20.glGetUniformLocation(m_iProgram, "vCoordMatrix")
        MyUtils.checkGlError("glGetUniformLocation vCoordMatrix")

        // Texture
        val aiTextureID = IntArray(1)
        GLES20.glGenTextures(1, aiTextureID, 0)
        m_iTextureID = aiTextureID[0]
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, m_iTextureID)
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0)

        // VBOの作成とデータのグラフィックスメモリへのコピー
        val aiVboID = IntArray(2)
        GLES20.glGenBuffers(2, aiVboID, 0)
        posVBO = aiVboID[0]
        coordVBO = aiVboID[1]

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, posVBO)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER, mVerBuffer.capacity() * 4, mVerBuffer, GLES20.GL_STATIC_DRAW
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, coordVBO)
        GLES20.glBufferData(
            GLES20.GL_ARRAY_BUFFER, mTexBuffer.capacity() * 4, mTexBuffer, GLES20.GL_STATIC_DRAW
        )
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)


        // SurfaceTextureとSurfaceの作成
        m_surfacetexture = SurfaceTexture(m_iTextureID)
        m_surfacetexture!!.setOnFrameAvailableListener(this)
        m_surface = Surface(m_surfacetexture)

        synchronized(this) {
            m_bNewFrameAvailable = false
        }
    }

    override fun onSurfaceChanged(gl10: GL10, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged")
        Log.d(TAG, "Thread name = " + Thread.currentThread().name) // Thread name = GLThread XXXX

        // ビューポート設定
        GLES20.glViewport(0, 0, width, height)

        // テクスチャの大きさ
        m_iTextureWidth = width
        m_iTextureHeight = height
        // Surfaceでの描画
        //drawInSurface();
    }

    override fun onDrawFrame(gl10: GL10) {
        //Log.d( TAG, "onDrawFrame" );
        //Log.d( TAG, "Thread name = " + Thread.currentThread().getName() );	// Thread name = GLThread XXXX

        synchronized(this) {
            if (m_bNewFrameAvailable) {
                m_surfacetexture!!.updateTexImage()
                m_surfacetexture!!.getTransformMatrix(m_f16STMatrix)
                m_bNewFrameAvailable = false
            }
        }

        // バッファークリア
        GLES20.glClearColor(0.0f, 1.0f, 0.0f, 1.0f)
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)

        // 使用するシェーダープログラムの指定
        GLES20.glUseProgram(m_iProgram)
        MyUtils.checkGlError("glUseProgram")

        // テクスチャの有効化
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, m_iTextureID)
        GLES20.glUniform1i(uTextureHandle, 0)
        // VBOのバインド
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, posVBO)

        // シェーダープログラムへ頂点座標値データの転送
        GLES20.glEnableVertexAttribArray(mHPosition)
        GLES20.glVertexAttribPointer(mHPosition, 3, GLES20.GL_FLOAT, false, 12, 0)
        MyUtils.checkGlError("glVertexAttribPointer Position")
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        MyUtils.checkGlError("glEnableVertexAttribArray Position")

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, coordVBO)
        GLES20.glEnableVertexAttribArray(mHCoord)
        // シェーダープログラムへテクスチャ座標値データの転送
        GLES20.glVertexAttribPointer(mHCoord, 2, GLES20.GL_FLOAT, false, 8, 0)
        MyUtils.checkGlError("glVertexAttribPointer TexCoord")
        // VBOのバインドの解除
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        MyUtils.checkGlError("glEnableVertexAttribArray TexCoord")


        // シェーダープログラムへ行列データの転送
        GLES20.glUniformMatrix4fv(matrixHandle, 1, false, m_f16MVPMatrix, 0)
        MyUtils.checkGlError("glUniformMatrix4fv MVPMatrix")
        GLES20.glUniformMatrix4fv(mHCoordMatrix, 1, false, m_f16STMatrix, 0)
        MyUtils.checkGlError("glUniformMatrix4fv STMatrix")

        // 描画
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        MyUtils.checkGlError("glDrawArrays")
        GLES20.glFlush()
        MyUtils.checkGlError("glFlush")
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
        Log.d(TAG, "onFrameAvailable")
        Log.d(TAG, "Thread name = " + Thread.currentThread().name) // Thread name = main

        synchronized(this) {
            m_bNewFrameAvailable = true
            glSurfaceView.requestRender()
        }
    }

    // Surfaceでの描画
    private fun drawInSurface(frameLayout: FrameLayout, width: Int, height: Int) {
        Log.d(TAG, "drawInSurface")
        Log.d(TAG, "Thread name = " + Thread.currentThread().name) // Thread name = GLThread XXXX

        if (null == m_surface) {
            return
        }

        val canvas = m_surface!!.lockCanvas(null) // nullを渡し、描画領域をサーフェス全体とする。
        if (canvas != null) {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            frameLayout.draw(canvas)
        }

        //
//		// 全体塗りつぶし
//		canvas.drawColor( Color.rgb( 128, 128, 128 ) );
//
//		// 矩形
//		m_paint.setColor( Color.rgb( m_random.nextInt( 255 ), m_random.nextInt( 255 ), m_random.nextInt( 255 ) ) );
//		m_paint.setStyle( Paint.Style.FILL );
//		int iX          = m_random.nextInt( m_iTextureWidth );
//		int iY          = m_random.nextInt( m_iTextureWidth );
//		int iHalfWidth  = m_random.nextInt( 200 );
//		int iHalfHeight = m_random.nextInt( 200 );
//		canvas.drawRect( iX - iHalfWidth, iY - iHalfHeight, iX + iHalfWidth, iY + iHalfHeight, m_paint );
//
//		// 円
//		m_paint.setColor( Color.rgb( m_random.nextInt( 255 ), m_random.nextInt( 255 ), m_random.nextInt( 255 ) ) );
//		m_paint.setStyle( Paint.Style.STROKE );
//		m_paint.setStrokeWidth( 30 );
//		int iRadius = m_random.nextInt( 200 );
//		canvas.drawCircle( m_random.nextInt( m_iTextureWidth ), m_random.nextInt( m_iTextureHeight ), iRadius, m_paint );
        m_surface!!.unlockCanvasAndPost(canvas)
    }

    fun startDrawInSurface(frameLayout: FrameLayout, width: Int, height: Int) {
        m_surfacetexture!!.setDefaultBufferSize(width, height)
        //		m_handlerDrawInSurface.postDelayed( m_runnableDrawInSurface, 0 );
        m_handlerDrawInSurface.post {
            drawInSurface(frameLayout, width, height)
        }
    }

    fun stopDrawInSurface() {
        m_handlerDrawInSurface.removeCallbacks(m_runnableDrawInSurface)
    }

    companion object {
        // 定数
        private const val TAG = "SurfaceTextureRenderer"
    }
}
