package com.example.rander;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.example.camera.R;
import com.example.filter.PicFilter;
import com.example.util.Gl2Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Arrays;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class ColorRender implements GLSurfaceView.Renderer {

    private final PicFilter picFilter;
    private String TAG = "PictureRender";
    private int[] textureId = new int[1];
    private int mWidth;
    private int mHeight;

    public ColorRender(Resources res) {
        picFilter = new PicFilter(res);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        picFilter.create();

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged");
        //按GLSurfaceView 的大小进行显示
        this.mWidth = width;
        this.mHeight = height;
        GLES20.glViewport(0, 0, width, height);
        //按图片的原本大小居中进行显示
        //GLES20.glViewport((width-mBitmap.getWidth())/2,(height-mBitmap.getHeight())/2,mBitmap.getWidth(),mBitmap.getHeight());
        //Gl2Utils.getPicOriginMatrix(Gl2Utils.getOriginalMatrix(),mBitmap.getWidth(),mBitmap.getHeight(),width,height);
//        Gl2Utils.getPicOriginMatrix(picFilter.getMatrix(), mBitmap.getWidth(), mBitmap.getHeight(), width, height, 6);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d(TAG, "onDrawFrame");

        GLES20.glGenTextures(1, textureId, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);

        // 设置纹理参数
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // 获取颜色数据0x26C9ED 2542061  0xF0CA27 15780391
        int[] pixels = genColorImage(mWidth, mHeight, 0xffA728F0);
        IntBuffer buffer = IntBuffer.allocate(pixels.length);
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
        buffer.put(pixels);
        buffer.position(0);

        // 上传纹理数据
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mWidth, mHeight, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buffer);
//            int textureID = Gl2Utils.createTexture(mBitmap);
        picFilter.setTextureId(textureId[0]);
        picFilter.draw();
    }

    public static int[] genColorImage(int width, int height, int color) {
        int[] pixels = new int[width * height];
        // 重新组合成小端格式的整数
        int colorPixel = convertToLittleEndian(color);

//        Log.d("ybb", "colorPixel: " + colorPixel);
        Arrays.fill(pixels, colorPixel);
        return pixels;
    }

    /**
     * android 设备的gpu需要小端显示
     *
     * @param color 颜色
     * @return 颜色
     */
    public static int convertToLittleEndian(int color) {
        // 将颜色分成四个字节
        byte red = (byte) ((color >> 16) & 0xFF);   // 提取红色分量
        byte green = (byte) ((color >> 8) & 0xFF);  // 提取绿色分量
        byte blue = (byte) (color & 0xFF);          // 提取蓝色分量
        byte alpha = (byte) ((color >> 24) & 0xFF); // 提取Alpha分量（如果有）
        if (alpha == 0) {
            alpha = (byte) 0xFF;
        }
//        Log.d("ybb", "red: " + red + " green: " + green + " blue: " + blue + " alpha:" + (alpha & 0xFF));
        // 重新组合成小端格式的整
        //return ((blue & 0xFF)) |
        //        ((green & 0xFF) << 8) |
        //        ((red & 0xFF) << 16) |
        //        ((alpha & 0xFF) << 24);
        return ((blue & 0xFF) << 16) | ((green & 0xFF) << 8) | ((red & 0xFF)) | ((alpha & 0xFF) << 24);
    }
}
