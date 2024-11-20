package com.example.rander

import android.content.res.Resources
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.util.Log
import com.example.filter.PicFilter
import com.example.util.Gl2Utils.genColorImage
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class ColorRender(res: Resources?) : GLSurfaceView.Renderer {
    private val picFilter = PicFilter(res)
    private val TAG = "PictureRender"
    private val textureId = IntArray(1)
    private var mWidth = 0
    private var mHeight = 0

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        picFilter.create()
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        Log.d(TAG, "onSurfaceChanged")
        //按GLSurfaceView 的大小进行显示
        this.mWidth = width
        this.mHeight = height
        GLES20.glViewport(0, 0, width, height)
        //按图片的原本大小居中进行显示
        //GLES20.glViewport((width-mBitmap.getWidth())/2,(height-mBitmap.getHeight())/2,mBitmap.getWidth(),mBitmap.getHeight());
        //Gl2Utils.getPicOriginMatrix(Gl2Utils.getOriginalMatrix(),mBitmap.getWidth(),mBitmap.getHeight(),width,height);
//        Gl2Utils.getPicOriginMatrix(picFilter.getMatrix(), mBitmap.getWidth(), mBitmap.getHeight(), width, height, 6);
    }

    override fun onDrawFrame(gl: GL10) {
        Log.d(TAG, "onDrawFrame")

        GLES20.glGenTextures(1, textureId, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0])

        // 设置纹理参数
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D,
            GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_NEAREST
        )

        // 获取颜色数据0x26C9ED 2542061  0xF0CA27 15780391
        val pixels = genColorImage(mWidth, mHeight, "#FF0000")
        val buffer = IntBuffer.allocate(pixels.size)
        // 创建ByteBuffer并填充数据
//        ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length * 4);
//        buffer.order(ByteOrder.nativeOrder());
//        for (int i = 0; i < pixels.length; i++) {
//            int color = pixels[i];
//            buffer.put((byte) ((color >> 16) & 0xFF));  // Red
//            buffer.put((byte) ((color >> 8) & 0xFF));   // Green
//            buffer.put((byte) (color & 0xFF));          // Blue
//            buffer.put((byte) ((color >> 24) & 0xFF));  // Alpha
//        }
        buffer.put(pixels)
        buffer.position(0)

        // 上传纹理数据
        GLES20.glTexImage2D(
            GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mWidth, mHeight, 0,
            GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer
        )
        //            int textureID = Gl2Utils.createTexture(mBitmap);
        picFilter.textureId = textureId[0]
        picFilter.draw()
    }

    companion object {
//        fun genColorImage(width: Int, height: Int, color: Int): IntArray {
//            val pixels = IntArray(width * height)
//            // 重新组合成小端格式的整数
////        int colorPixel = convertToLittleEndian(color);
//            val c = Color.parseColor("#FF0000")
//            Log.d("ybb", "colorPixel: $c")
//            Arrays.fill(pixels, c)
//            return pixels
//        }
//
//        /**
//         * android 设备的gpu需要小端显示
//         *
//         * @param color 颜色
//         * @return 颜色
//         */
//        fun convertToLittleEndian(color: Int): Int {
//            // 将颜色分成四个字节
//            val red = ((color shr 16) and 0xFF).toByte() // 提取红色分量
//            val green = ((color shr 8) and 0xFF).toByte() // 提取绿色分量
//            val blue = (color and 0xFF).toByte() // 提取蓝色分量
//            var alpha = ((color shr 24) and 0xFF).toByte() // 提取Alpha分量（如果有）
//            if (alpha.toInt() == 0) {
//                alpha = 0xFF.toByte()
//            }
//            //        Log.d("ybb", "red: " + red + " green: " + green + " blue: " + blue + " alpha:" + (alpha & 0xFF));
//            // 重新组合成小端格式的整
//            //return ((blue & 0xFF)) |
//            //        ((green & 0xFF) << 8) |
//            //        ((red & 0xFF) << 16) |
//            //        ((alpha & 0xFF) << 24);
//            return ((blue.toInt() and 0xFF) shl 16) or ((green.toInt() and 0xFF) shl 8) or ((red.toInt() and 0xFF)) or ((alpha.toInt() and 0xFF) shl 24)
//        }
    }
}
