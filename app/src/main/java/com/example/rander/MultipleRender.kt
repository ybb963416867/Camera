package com.example.rander

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import com.example.gpengl.multiple.CoordinateRegion
import com.example.gpengl.multiple.IBaseTexture
import com.example.gpengl.multiple.PicTexture
import com.example.gpengl.multiple.PicTextureT
import com.example.gpengl.multiple.TextureInfo
import com.example.gpengl.multiple.generateBitmapTexture
import com.example.gpengl.multiple.getHeight
import com.example.gpengl.multiple.offSet
import com.example.util.Gl2Utils
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MultipleRender(var context: Context) : GLSurfaceView.Renderer {

    private var shaderProgram = 0
    private var baseTextureList = listOf<IBaseTexture>(
        PicTextureT(context),
        PicTexture(context),
        PicTexture(context),
        PicTextureT(context)
    )

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.96f, 0.8f, 0.156f, 1.0f)

        shaderProgram = Gl2Utils.createGlProgram(
            Gl2Utils.uRes(context.resources, "shader/base_vert.glsl"),
            Gl2Utils.uRes(context.resources, "shader/base_frag.glsl")
        )

        GLES20.glLinkProgram(shaderProgram)
        baseTextureList.forEach {
            it.onSurfaceCreated()
        }
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
        GLES20.glUseProgram(shaderProgram)
        baseTextureList.forEach {
            it.onDrawFrame(shaderProgram)
        }
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
                    iBaseTexture.updateTexCord(coordinateRegion.offSet(0f, coordinateRegion.getHeight() * 2 + 100f))
                }

                0 -> {
                    iBaseTexture.updateTexCord(coordinateRegion)
                }
                else -> {
                    iBaseTexture.updateTexCord(coordinateRegion)
                }
            }
        }
    }


    fun loadTexture(resourceId: Int) {
        baseTextureList.forEachIndexed { index, iBaseTexture ->
//            iBaseTexture.setTextureInfo(TextureInfo().generateBitmapTexture(context, resourceId))
//            iBaseTexture.loadBitmapTexture(resourceId)
//            val textureInfo = TextureInfo()
//            textureInfo.textureId = iBaseTexture.getTextureId()
//            val options = BitmapFactory.Options()
//            options.inScaled = false
//            val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
//
//            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureInfo.textureId)
//
//            GLES20.glTexParameteri(
//                GLES20.GL_TEXTURE_2D,
//                GLES20.GL_TEXTURE_MIN_FILTER,
//                GLES20.GL_LINEAR
//            )
//            GLES20.glTexParameteri(
//                GLES20.GL_TEXTURE_2D,
//                GLES20.GL_TEXTURE_MAG_FILTER,
//                GLES20.GL_LINEAR
//            )
//
//            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
//            textureInfo.width = bitmap.width
//            textureInfo.height = bitmap.height
//            bitmap.recycle()
//
//            iBaseTexture.updateTextureInfo(textureInfo)

            iBaseTexture.updateTextureInfo(TextureInfo().generateBitmapTexture(context, resourceId))
        }
    }

}