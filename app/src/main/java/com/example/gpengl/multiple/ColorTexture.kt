package com.example.gpengl.multiple

import android.opengl.GLES20
import android.opengl.Matrix
import android.view.MotionEvent
import android.view.SurfaceView
import com.example.util.Gl2Utils
import com.example.util.Gl2Utils.genColorImage
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer

class ColorTexture(
    private var surfaceView: SurfaceView,
    private var vertPath: String = "shader/base_vert.glsl",
    private var fragPath: String = "shader/base_frag.glsl"
) : IBaseTexture {
    private var screenWidth = 0
    private var screenHeight = 0

    private var textureInfo = TextureInfo()
    private var vertexBuffer: FloatBuffer
    var texCoordBuffer: FloatBuffer
    val matrix: FloatArray = Gl2Utils.originalMatrix
    private var textureWidth = 0
    private var textureHeight = 0

    private var positionHandle = 0
    private var texCoordHandle = 0
    private var uTextureHandle = 0
    private var matrixHandle = 0
    private var currentRegion = CoordinateRegion()
    private var colorStr = "#00000000"
    private var iTextureVisibility = ITextureVisibility.INVISIBLE
    private var shaderProgram = 0

    private var colorBuffer: IntBuffer? = null

    private val texCoords = floatArrayOf(
        0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f
    )

    private var vbo = IntArray(2)

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
        if (coordinateRegion.getWidth() != currentRegion.getWidth() || coordinateRegion.getHeight() != currentRegion.getHeight()) {
            val genColorImage = genColorImage(
                currentRegion.getWidth().toInt(), currentRegion.getHeight().toInt(), colorStr
            )

//        colorBuffer = ByteBuffer.allocateDirect(genColorImage.size * Int.SIZE_BYTES).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer()

            colorBuffer = IntBuffer.allocate(genColorImage.size).apply {
                put(genColorImage).position(0)
            }

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureInfo.textureId)
            GLES20.glTexImage2D(
                GLES20.GL_TEXTURE_2D,
                0,
                GLES20.GL_RGBA,
                currentRegion.getWidth().toInt(),
                currentRegion.getHeight().toInt(),
                0,
                GLES20.GL_RGBA,
                GLES20.GL_UNSIGNED_BYTE,
                colorBuffer
            )

            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR
            )
            GLES20.glTexParameteri(
                GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR
            )

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        }

        currentRegion = coordinateRegion.copyCoordinateRegion()

        val newVertices = currentRegion.getFloatArray(
            screenWidth = screenWidth.toFloat(), screenHeight = screenHeight.toFloat()
        )

//        Log.e("colorTexture", "newVertices: " + newVertices.contentToString())
        vertexBuffer.clear()
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

        currentRegion =
            currentRegion.generateCoordinateRegion(0f, 0f, surfaceView.width, surfaceView.height)
        Matrix.setIdentityM(matrix, 0)
        GLES20.glLinkProgram(shaderProgram)

        GLES20.glGenBuffers(2, vbo, 0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER,vertexBuffer.capacity() * 4, vertexBuffer, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[1])
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, texCoordBuffer.capacity() * 4, texCoordBuffer, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)

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
        textureWidth = textureInfo.width
        textureHeight = textureInfo.height
        this.iTextureVisibility = iTextureVisibility

        val genColorImage = genColorImage(
            currentRegion.getWidth().toInt(), currentRegion.getHeight().toInt(), colorStr
        )
//        colorBuffer = IntBuffer.allocate(genColorImage.size).apply {
//            put(genColorImage).position(0)
//        }

        colorBuffer = ByteBuffer.allocateDirect(genColorImage.size * Int.SIZE_BYTES)
            .order(ByteOrder.nativeOrder()).asIntBuffer().apply {
                put(genColorImage).position(0)
            }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textureInfo.textureId)
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            currentRegion.getWidth().toInt(),
            currentRegion.getHeight().toInt(),
            0,
            GLES20.GL_RGBA,
            GLES20.GL_UNSIGNED_BYTE,
            colorBuffer
        )

        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR
        )

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
        Matrix.setIdentityM(matrix, 0)
    }

    override fun updateTextureInfo(
        textureInfo: TextureInfo,
        isRecoverCord: Boolean,
        backgroundColor: String?,
        iTextureVisibility: ITextureVisibility
    ) {
        if (backgroundColor != null) {
            colorStr = backgroundColor
        }
        this.updateTextureInfo(textureInfo, isRecoverCord)
        this.iTextureVisibility
    }

    override fun onDrawFrame() {
        if (iTextureVisibility == ITextureVisibility.VISIBLE) {
            GLES20.glEnable(GLES20.GL_BLEND)
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
            GLES20.glUseProgram(shaderProgram)
            GLES20.glEnableVertexAttribArray(positionHandle)

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[0])
            GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, vertexBuffer.capacity() * 4, vertexBuffer)
            GLES20.glVertexAttribPointer(
                positionHandle,
                3,
                GLES20.GL_FLOAT,
                false,
                12,
                0
            )

            GLES20.glEnableVertexAttribArray(texCoordHandle)
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vbo[1])
            GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, texCoordBuffer.capacity() * 4, texCoordBuffer)
            GLES20.glVertexAttribPointer(
                texCoordHandle,
                2,
                GLES20.GL_FLOAT,
                false,
                8,
                0
            )

            GLES20.glUniformMatrix4fv(matrixHandle, 1, false, matrix, 0)

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureInfo.textureId)
            GLES20.glUniform1i(uTextureHandle, 0)

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4)

            GLES20.glDisableVertexAttribArray(positionHandle)
            GLES20.glDisableVertexAttribArray(texCoordHandle)
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
        }
    }

    override fun initCoordinate() {

    }

    override fun release() {
        GLES20.glDeleteProgram(shaderProgram)
        GLES20.glDeleteTextures(1, IntArray(textureInfo.textureId), 0)
    }

    override fun getVisibility(): ITextureVisibility {
        return iTextureVisibility
    }

    override fun setVisibility(visibility: ITextureVisibility) {
        this.iTextureVisibility = visibility
    }

    override fun clearTexture(colorString: String) {

    }

    override fun acceptTouchEvent(event: MotionEvent): Boolean {
        return false
    }

    override fun onTouch(baseTexture: IBaseTexture, event: MotionEvent): Boolean {
        return false
    }

}