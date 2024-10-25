package com.example.camera;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MyGLRenderer2 implements GLSurfaceView.Renderer {


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


    private Texture texture1;
    private Texture1 texture2;
    private int shaderProgram;

    public MyGLRenderer2(Context context) {
        texture1 = new Texture(context, R.mipmap.photo2);
        texture2 = new Texture1(context, R.mipmap.bg);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // 加载并编译着色器
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // 创建着色器程序并链接
        shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(shaderProgram, vertexShader);
        GLES20.glAttachShader(shaderProgram, fragmentShader);
        GLES20.glLinkProgram(shaderProgram);
        // 编译并链接着色器程序...
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        // 根据屏幕尺寸更新纹理的顶点坐标
        texture1.updateVertices(width, height);
        texture2.updateVertices(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 绘制两个纹理
//        texture1.draw(shaderProgram);
        texture2.draw(shaderProgram);
    }

    // 加载着色器的方法
    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}

