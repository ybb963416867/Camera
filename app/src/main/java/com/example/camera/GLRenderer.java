package com.example.camera;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.opengl.GLUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRenderer implements GLSurfaceView.Renderer {
    private final Context context;
    private final GLSurfaceView surfaceView;
    private int[] textures = new int[2]; // 存储两个纹理 ID
    private float[][] projectionMatrices = new float[2][16]; // 每个纹理的正交投影矩阵
    float[] combinedProjectionMatrix = new float[16];
    private Bitmap bitmap;
    private int program;

    // FBO 和合并纹理
    private final int[] fbo = new int[1];
    private final int[] combinedTexture = new int[1];

    private FloatBuffer vertexBuffer;

    private FloatBuffer combinedVertexBuffer;

    private final String vertexShaderCode =
            "attribute vec4 a_Position;" +
                    "attribute vec2 a_TexCoord;" +
                    "varying vec2 v_TexCoord;" +
                    "uniform mat4 u_ProjectionMatrix;" +
                    "void main() {" +
                    "  gl_Position = u_ProjectionMatrix * a_Position;" +
                    "  v_TexCoord = a_TexCoord;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform sampler2D u_Texture;" +
                    "varying vec2 v_TexCoord;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(u_Texture, v_TexCoord);" +
                    "}";

    // 顶点数据 (两个三角形组成一个正方形)，纹理坐标的Y方向已反转
    private final float[] vertexData = {
            // X, Y, U, V (V坐标倒置)
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 0.0f, 1.0f
    };

    private int aPositionHandle;
    private int aTexCoordHandle;
    private int uTextureHandle;
    private int uProjectionMatrixHandle;

    public GLRenderer(GLSurfaceView surfaceView) {
        this.context = surfaceView.getContext();
        this.surfaceView = surfaceView;
        // 初始化顶点缓冲区
        ByteBuffer bb = ByteBuffer.allocateDirect(vertexData.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexData);
        vertexBuffer.position(0);
        ByteBuffer aa = ByteBuffer.allocateDirect(vertexData.length * 4);
        aa.order(ByteOrder.nativeOrder());
        combinedVertexBuffer = aa.asFloatBuffer();
        combinedVertexBuffer.put(vertexData);
        combinedVertexBuffer.position(0);

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);

        GLES20.glGenFramebuffers(1, fbo, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0]);

        // 创建合并纹理
        GLES20.glGenTextures(1, combinedTexture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, combinedTexture[0]);
        Log.d("ybb", "width = " + surfaceView.getWidth() + "height = " + surfaceView.getHeight());
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, surfaceView.getWidth(), surfaceView.getHeight(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        // 将合并纹理附加到 FBO
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, combinedTexture[0], 0);
        // 检查 FBO 状态
        int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e("GLRenderer", "Framebuffer incomplete: " + status);
        }

        // 解绑 FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);


        // 创建 OpenGL 程序
        program = createProgram(vertexShaderCode, fragmentShaderCode);
        GLES20.glUseProgram(program);

        // 获取属性和 Uniform 位置
        aPositionHandle = GLES20.glGetAttribLocation(program, "a_Position");
        aTexCoordHandle = GLES20.glGetAttribLocation(program, "a_TexCoord");
        uTextureHandle = GLES20.glGetUniformLocation(program, "u_Texture");
        uProjectionMatrixHandle = GLES20.glGetUniformLocation(program, "u_ProjectionMatrix");

        // 加载图片并生成纹理
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.bg);
        GLES20.glGenTextures(2, textures, 0);

        for (int i = 0; i < textures.length; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        }

        bitmap.recycle();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, combinedTexture[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        // 设置合并纹理的正交投影矩阵，确保宽高比一致
        Matrix.setIdentityM(combinedProjectionMatrix, 0);
        Matrix.orthoM(combinedProjectionMatrix, 0, -1, 1, -1, 1, -1, 1);

        // 设置每个纹理的正交投影矩阵，保持宽高比一致
        float aspectRatio = (float) bitmap.getWidth() / bitmap.getHeight();
        for (int i = 0; i < textures.length; i++) {
            Matrix.setIdentityM(projectionMatrices[i], 0);
//            if (width > height) {
//                Matrix.orthoM(projectionMatrices[i], 0, -aspectRatio, aspectRatio, -1, 1, -1, 1);
//            } else {
//                Matrix.orthoM(projectionMatrices[i], 0, -1, 1, -1 / aspectRatio, 1 / aspectRatio, -1, 1);
//            }
//
//            float offset = (i == 0) ? -1f : 0f;
//            Matrix.translateM(projectionMatrices[i], 0, offset, 0, 0);

            Matrix.setIdentityM(projectionMatrices[i], 0);
            Matrix.orthoM(projectionMatrices[i], 0, -1, 1, -1, 1, -1, 1);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo[0]);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 绘制纹理到 FBO
        for (int i = 0; i < textures.length; i++) {
            GLES20.glUniformMatrix4fv(uProjectionMatrixHandle, 1, false, projectionMatrices[i], 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);
            GLES20.glUniform1i(uTextureHandle, 0);

            GLES20.glEnableVertexAttribArray(aPositionHandle);
            GLES20.glVertexAttribPointer(aPositionHandle, 2, GLES20.GL_FLOAT, false, 4 * 4, vertexBuffer);

            vertexBuffer.position(2);
            GLES20.glEnableVertexAttribArray(aTexCoordHandle);
            GLES20.glVertexAttribPointer(aTexCoordHandle, 2, GLES20.GL_FLOAT, false, 4 * 4, vertexBuffer);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);

            GLES20.glDisableVertexAttribArray(aPositionHandle);
            GLES20.glDisableVertexAttribArray(aTexCoordHandle);
        }

        // 解绑 FBO 并恢复视口大小
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        GLES20.glViewport(0, 0, surfaceView.getWidth(), surfaceView.getHeight());

        // 使用合并投影矩阵绘制合并纹理到屏幕
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, combinedTexture[0]);
        GLES20.glUniformMatrix4fv(uProjectionMatrixHandle, 1, false, combinedProjectionMatrix, 0);

        GLES20.glEnableVertexAttribArray(aPositionHandle);
        GLES20.glVertexAttribPointer(aPositionHandle, 2, GLES20.GL_FLOAT, false, 4 * 4, combinedVertexBuffer);

        combinedVertexBuffer.position(2);
        GLES20.glEnableVertexAttribArray(aTexCoordHandle);
        GLES20.glVertexAttribPointer(aTexCoordHandle, 2, GLES20.GL_FLOAT, false, 4 * 4, combinedVertexBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 4);

        GLES20.glDisableVertexAttribArray(aPositionHandle);
        GLES20.glDisableVertexAttribArray(aTexCoordHandle);

    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        int program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);

        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] == 0) {
            GLES20.glDeleteProgram(program);
            throw new RuntimeException("Error creating program.");
        }

        return program;
    }

    private int loadShader(int type, String shaderCode) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] == 0) {
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
        }

        return shader;
    }


    public void startRecording() {

    }

    public void stopRecording() {

    }
}
