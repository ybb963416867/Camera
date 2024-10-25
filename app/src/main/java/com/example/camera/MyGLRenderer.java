package com.example.camera;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class MyGLRenderer implements GLSurfaceView.Renderer {

    private final Context context;
    private int[] textureIds = new int[2]; // 存储纹理的ID
    private int shaderProgram;

    // 顶点坐标和纹理坐标数据
    private FloatBuffer vertexBuffer1, vertexBuffer2, texCoordBuffer;

    private final float[] vertices1 = { // 左侧的纹理坐标
            -0.8f,  0.8f, 0.0f,   // 左上角
            -0.8f, -0.8f, 0.0f,   // 左下角
            0.0f, -0.8f, 0.0f,   // 右下角
            0.0f,  0.8f, 0.0f    // 右上角
    };

    private final float[] vertices2 = { // 右侧的纹理坐标
            0.0f,  0.8f, 0.0f,   // 左上角
            0.0f, -0.8f, 0.0f,   // 左下角
            0.8f, -0.8f, 0.0f,   // 右下角
            0.8f,  0.8f, 0.0f    // 右上角
    };

    private final float[] texCoords = { // 纹理坐标
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };

    public MyGLRenderer(Context context) {
        this.context = context;

        // 初始化顶点和纹理坐标的 FloatBuffer
        vertexBuffer1 = ByteBuffer.allocateDirect(vertices1.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer1.put(vertices1).position(0);

        vertexBuffer2 = ByteBuffer.allocateDirect(vertices2.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        vertexBuffer2.put(vertices2).position(0);

        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        texCoordBuffer.put(texCoords).position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // 加载两个纹理
        textureIds[0] = loadTexture(context, R.mipmap.bg);
        textureIds[1] = loadTexture(context, R.mipmap.photo2);

        // 加载并编译着色器
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // 创建着色器程序并链接
        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, fragmentShader);
        GLES20.glLinkProgram(shaderProgram);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 使用着色器程序
        GLES20.glUseProgram(shaderProgram);

        // 绘制第一个纹理
        drawTexture(textureIds[0], vertexBuffer1);

        // 绘制第二个纹理
        drawTexture(textureIds[1], vertexBuffer2);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
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

    // 绘制纹理的方法
    private void drawTexture(int textureId, FloatBuffer vertexBuffer) {
        // 获取着色器属性位置
        int positionHandle = GLES20.glGetAttribLocation(shaderProgram, "aPosition");
        int texCoordHandle = GLES20.glGetAttribLocation(shaderProgram, "aTexCoord");
        int textureUniform = GLES20.glGetUniformLocation(shaderProgram, "uTexture");

        // 启用顶点坐标和纹理坐标数组
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glEnableVertexAttribArray(texCoordHandle);

        // 传递顶点数据
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer);
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, texCoordBuffer);

        // 绑定纹理
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);
        GLES20.glUniform1i(textureUniform, 0);

        // 绘制四边形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);

        // 禁用顶点属性数组
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }

    // 着色器代码
    private final String vertexShaderCode =
            "attribute vec4 aPosition;" +
                    "attribute vec2 aTexCoord;" +
                    "varying vec2 vTexCoord;" +
                    "void main() {" +
                    "  gl_Position = aPosition;" +
                    "  vTexCoord = aTexCoord;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec2 vTexCoord;" +
                    "uniform sampler2D uTexture;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(uTexture, vTexCoord);" +
                    "}";

    // 加载着色器的方法
    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
