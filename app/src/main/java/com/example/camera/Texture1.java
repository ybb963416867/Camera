package com.example.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class Texture1 {
    private int textureId;
    private FloatBuffer vertexBuffer;
    private FloatBuffer texCoordBuffer;
    private Context context;
    private int imageWidth;
    private int imageHeight;

    private final float[] texCoords = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };

    public Texture1(Context context) {
        this.context = context;

        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        texCoordBuffer.put(texCoords).position(0);

        float[] vertices = {
                0.0f,  0.8f, 0.0f,   // 左上角
                0.0f, -0.8f, 0.0f,   // 左下角
                0.8f, -0.8f, 0.0f,   // 右下角
                0.8f,  0.8f, 0.0f    // 右上角
        };

        vertexBuffer = ByteBuffer.allocateDirect(vertices.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer.put(vertices).position(0);


    }

    public void onCreate() {
        textureId = loadTexture(context,  R.mipmap.photo2);
    }

    // 设置根据屏幕尺寸和纹理大小计算的顶点坐标
    public void updateVertices(int screenWidth, int screenHeight) {
//        float ratioX = (float) imageWidth / screenWidth;
//        float ratioY = (float) imageHeight / screenHeight;


    }

    public void draw(int shaderProgram) {
        int positionHandle = GLES20.glGetAttribLocation(shaderProgram, "aPosition");
        int texCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "aTexCoord");
        int textureUniform = GLES20.glGetUniformLocation(shaderProgram, "uTexture");

        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glEnableVertexAttribArray(texCoordHandle);

        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer);
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBuffer);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(textureUniform, 0);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);

        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }

    // 加载纹理的方法
    private int loadTexture(Context context, int resourceId) {
        final int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
        }
        return textureHandle[0];
    }


}

