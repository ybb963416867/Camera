package com.example.gpengl.multiple

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLUtils
import com.example.gpengl.second.util.OpenGLUtils
import com.example.util.Gl2Utils

class PicTextureT(val context: Context) : BaseTexture() {

    fun a(resourceId: Int){
        val textureInfo = TextureInfo()
        val textureId = IntArray(1)
        OpenGLUtils.glGenTextures(textureId)
        val options = BitmapFactory.Options()
        options.inScaled = false
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        textureInfo.textureId = textureId[0]
        textureInfo.width = bitmap.width
        textureInfo.height = bitmap.height
        bitmap.recycle()
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    // 加载纹理的方法
    override fun loadBitmapTexture(resourceId: Int) {
        if (textureInfo1.textureId != 0) {
            val options = BitmapFactory.Options()
            options.inScaled = false
            val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureInfo1.textureId)

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
            textureWidth = bitmap.width
            textureHeight = bitmap.height
            bitmap.recycle()
        }
    }

}