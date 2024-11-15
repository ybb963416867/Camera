package com.example.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLSurfaceViewRecorder extends GLSurfaceView implements GLSurfaceView.Renderer {

    private int[] textures = new int[2]; // 存储两个纹理 ID
    private float[][] projectionMatrices = new float[2][16]; // 每个纹理的正交投影矩阵
    private Bitmap bitmap;
    private int program;
    private FloatBuffer vertexBuffer;
    private android.opengl.EGLConfig config;

    private final String vertexShaderCode = "attribute vec4 a_Position;" +
            "attribute vec2 a_TexCoord;" +
            "varying vec2 v_TexCoord;" +
            "uniform mat4 u_ProjectionMatrix;" +
            "void main() {" +
            "  gl_Position = u_ProjectionMatrix * a_Position;" +
            "  v_TexCoord = a_TexCoord;" +
            "}";

    private final String fragmentShaderCode = "precision mediump float;" +
            "uniform sampler2D u_Texture;" +
            "varying vec2 v_TexCoord;" +
            "void main() {" +
            "  gl_FragColor = texture2D(u_Texture, v_TexCoord);" +
            "}";

    private final float[] vertexData = {
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 0.0f, 1.0f
    };

    private int aPositionHandle;
    private int aTexCoordHandle;
    private int uTextureHandle;
    private int uProjectionMatrixHandle;

    private MediaRecorder mediaRecorder;
    private Surface recorderSurface;
    private EGLDisplay eglDisplay;
    private EGLContext eglContext;
    private EGLSurface eglSurface;
    private int fbo; // Framebuffer object
    private int fboTexture; // FBO的纹理
    private int width;
    private int height;

    public GLSurfaceViewRecorder(Context context) {
        this(context, null);
    }

    public GLSurfaceViewRecorder(Context context, AttributeSet attrs) {
        super(context, attrs);

        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_CONTINUOUSLY);

        ByteBuffer bb = ByteBuffer.allocateDirect(vertexData.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(vertexData);
        vertexBuffer.position(0);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);

        // 初始化 OpenGL 程序
        program = createProgram(vertexShaderCode, fragmentShaderCode);
        GLES20.glUseProgram(program);

        aPositionHandle = GLES20.glGetAttribLocation(program, "a_Position");
        aTexCoordHandle = GLES20.glGetAttribLocation(program, "a_TexCoord");
        uTextureHandle = GLES20.glGetUniformLocation(program, "u_Texture");
        uProjectionMatrixHandle = GLES20.glGetUniformLocation(program, "u_ProjectionMatrix");

        // 初始化纹理
        bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.mipmap.bg);
        GLES20.glGenTextures(2, textures, 0);

        for (int i = 0; i < textures.length; i++) {
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[i]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        }

        bitmap.recycle();

        // 初始化 EGL 离屏渲染
        initEGL();
        initFBO();
    }


    private void initEGL() {
        eglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        if (eglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetDisplay failed");
        }
        //初始化显示器
        int[] version = new int[2];
        // 12.1020203
        //major：主版本 记录在 version[0]
        //minor : 子版本 记录在 version[1]
        if (!EGL14.eglInitialize(eglDisplay, version, 0, version, 1)) {
            throw new RuntimeException("eglInitialize failed");
        }
        // egl 根据我们配置的属性 选择一个配置
        int[] attrib_list = {
                EGL14.EGL_RED_SIZE, 8, // 缓冲区中 红分量 位数
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT, //egl版本 2
                EGL14.EGL_NONE
        };

        android.opengl.EGLConfig[] configs = new android.opengl.EGLConfig[1];
        int[] num_config = new int[1];
        // attrib_list：属性列表+属性列表的第几个开始
        // configs：获取的配置 (输出参数)
        //num_config: 长度和 configs 一样就行了
        if (!EGL14.eglChooseConfig(eglDisplay, attrib_list, 0,
                configs, 0, configs.length, num_config, 0)) {
            throw new IllegalArgumentException("eglChooseConfig#2 failed");
        }

        int[] ctx_attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, //egl版本 2
                EGL14.EGL_NONE
        };
        config =  configs[0];
        //创建EGL上下文
        // 3 share_context: 共享上下文 传绘制线程(GLThread)中的EGL上下文 达到共享资源的目的 发生关系
        eglContext = EGL14.eglCreateContext(eglDisplay, config,  EGL14.eglGetCurrentContext(), ctx_attrib_list, 0);
        // 创建失败
        if (eglContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("EGL Context Error.");
        }
    }

    private void initFBO() {
        int[] fboIds = new int[1];
        GLES20.glGenFramebuffers(1, fboIds, 0);
        fbo = fboIds[0];

        int[] textureIds = new int[1];
        GLES20.glGenTextures(1, textureIds, 0);
        fboTexture = textureIds[0];

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fboTexture);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, getWidth(), getHeight(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fboTexture, 0);

        if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer not complete.");
        }

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
        float aspectRatio = (float) bitmap.getWidth() / bitmap.getHeight();

        for (int i = 0; i < textures.length; i++) {
            if (width > height) {
                Matrix.orthoM(projectionMatrices[i], 0, -aspectRatio, aspectRatio, -1, 1, -1, 1);
            } else {
                Matrix.orthoM(projectionMatrices[i], 0, -1, 1, -1 / aspectRatio, 1 / aspectRatio, -1, 1);
            }
            float offset = (i == 0) ? -1f : 0f;
            Matrix.translateM(projectionMatrices[i], 0, offset, 0, 0);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        // 渲染到 FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        renderScene();

        // 渲染到显示的 Surface
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        renderScene();

        // 录制的 EGLSurface 渲染
        if (eglSurface != null && eglSurface != EGL14.EGL_NO_SURFACE) {

//            if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
//                throw new RuntimeException("eglMakeCurrent failed. EGL Error: " + EGL14.eglGetError());
//            }

//            // 从 FBO 中读取像素
//            ByteBuffer pixelBuffer = ByteBuffer.allocateDirect(width * height * 4);
//            pixelBuffer.order(ByteOrder.nativeOrder());
//            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fbo);
//            GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer);
//
//            // 使用 FBO 纹理将像素数据绘制到 `recorderSurface`
//            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
//            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            // 在此处使用 glTexSubImage2D 将 pixelBuffer 数据加载到一个纹理上，然后将纹理绘制到屏幕上
//            Log.e("ybb", "eglSwapBuffers");
            EGL14.eglSwapBuffers(eglDisplay, eglSurface);
            // 切换回默认的 EGL 上下文
//            if (eglDisplay != null && !EGL14.eglMakeCurrent(eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)) {
//                throw new RuntimeException("eglMakeCurrent failed");
//            }
        }
    }

    private void renderScene() {
        for (int i = 0; i < textures.length; i++) {
            GLES20.glUniformMatrix4fv(uProjectionMatrixHandle, 1, false, projectionMatrices[i], 0);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
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
    }


    public void startRecording(String outputPath) throws IOException {
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

        // 设置分辨率，确保与 GLSurfaceView 尺寸匹配
        mediaRecorder.setVideoSize(getWidth(), getHeight());
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setOutputFile(outputPath);
        mediaRecorder.setVideoEncodingBitRate(10000000);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            Log.e("GLSurfaceViewRecorder", "MediaRecorder prepare failed: " + e.getMessage());
            throw e;
        }

        recorderSurface = mediaRecorder.getSurface();
        mediaRecorder.start();

        // 创建 EGL Surface 并进行检查
        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, config, recorderSurface, new int[]{EGL14.EGL_NONE}, 0);
        if (eglSurface == null || eglSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("Failed to create EGLSurface. EGL Error: " + EGL14.eglGetError());
        }

        // 切换 EGL 上下文，检查状态
        if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
            throw new RuntimeException("eglMakeCurrent failed. EGL Error: " + EGL14.eglGetError());
        }

        Log.d("GLSurfaceViewRecorder", "Recording started successfully.");
    }

    public void stopRecording() {
        queueEvent(() -> {
            if (mediaRecorder != null) {
                try {
                    mediaRecorder.stop();
                } catch (IllegalStateException e){
                    Log.e("CameraApp", "停止录制时出错: ${e.message}");
                }finally {
                    mediaRecorder.release();
                    mediaRecorder = null;
                }
            }
            if (recorderSurface != null) {
                recorderSurface.release();
                recorderSurface = null;
            }
            if (eglSurface != null) {
                EGL14.eglDestroySurface(eglDisplay, eglSurface);
                eglSurface = null;
            }
            if (eglContext != null) {
                EGL14.eglDestroyContext(eglDisplay, eglContext);
                eglContext = null;
            }
            if (eglDisplay != null) {
                EGL14.eglTerminate(eglDisplay);
                eglDisplay = null;
            }
        });
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

}
