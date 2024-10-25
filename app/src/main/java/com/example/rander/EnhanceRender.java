package com.example.rander;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import com.example.camera.R;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class EnhanceRender implements GLSurfaceView.Renderer {

    private final Context context;
    private int mProgram;
    private int mTextureId;
    private FloatBuffer vertexBuffer, textureBuffer;

    // 亮度和对比度
    private float brightness = 0.0f;
    private float contrast = 1.0f;

    private final float[] vertexData = {
            -1.0f,  1.0f, // 左上角
            -1.0f, -1.0f, // 左下角
            1.0f,  1.0f, // 右上角
            1.0f, -1.0f  // 右下角
    };

    private final float[] textureData = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
    };

    public EnhanceRender(Context context) {
        this.context = context;

        // 初始化顶点缓冲区
        vertexBuffer = ByteBuffer.allocateDirect(vertexData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(vertexData);
        vertexBuffer.position(0);

        // 初始化纹理缓冲区
        textureBuffer = ByteBuffer.allocateDirect(textureData.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(textureData);
        textureBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // 编译着色器
        String vertexShaderCode = "attribute vec4 vPosition;" +
                "attribute vec2 aTexCoord;" +
                "varying vec2 vTexCoord;" +
                "void main() {" +
                "  gl_Position = vPosition;" +
                "  vTexCoord = aTexCoord;" +
                "}";

        String fragmentShaderCode = "precision mediump float;" +
                "uniform sampler2D uTexture;" +
                "uniform float brightness;" +
                "uniform float contrast;" +
                "varying vec2 vTexCoord;" +
                "void main() {" +
                "  vec4 color = texture2D(uTexture, vTexCoord);" +
                "  color.rgb += brightness;" +
                "  color.rgb = (color.rgb - 0.5) * contrast + 0.5;" +
                "  gl_FragColor = color;" +
                "}";

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // 创建 OpenGL 程序并链接着色器
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        // 加载纹理
        mTextureId = loadTexture(BitmapFactory.decodeResource(context.getResources(), R.mipmap.photo));
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glUseProgram(mProgram);

        // 传递顶点数据
        int positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(positionHandle);
        GLES20.glVertexAttribPointer(positionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer);

        // 传递纹理坐标数据
        int texCoordHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        GLES20.glEnableVertexAttribArray(texCoordHandle);
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, textureBuffer);

        // 传递亮度和对比度数据
        int brightnessHandle = GLES20.glGetUniformLocation(mProgram, "brightness");
        GLES20.glUniform1f(brightnessHandle, brightness);

        int contrastHandle = GLES20.glGetUniformLocation(mProgram, "contrast");
        GLES20.glUniform1f(contrastHandle, contrast);

        // 绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);

        // 绘制矩形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        // 禁用顶点数组
        GLES20.glDisableVertexAttribArray(positionHandle);
        GLES20.glDisableVertexAttribArray(texCoordHandle);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    // 加载纹理
    public int loadTexture(Bitmap bitmap) {
        int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] != 0) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            bitmap.recycle();
        }

        return textureHandle[0];
    }

    public static int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    public void setBrightness(float brightness) {
        this.brightness = brightness;
    }

    public void setContrast(float contrast) {
        this.contrast = contrast;
    }
}
