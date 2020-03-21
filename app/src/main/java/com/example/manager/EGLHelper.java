package com.example.manager;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.opengles.GL10;


/**
 * EGL执行流程
 * a, 选择Display
 * b, 选择Config
 * c, 创建Surface
 * d, 创建Context
 * e, 指定当前的环境为绘制环境
 */

public class EGLHelper {

    public EGL14 mEgl;
    public EGLDisplay mEglDisplay;
    public EGLConfig mEglConfig;
    public EGLSurface mEglSurface;
    public EGLContext mEglContext;
    private int mWidth;
    private int mHeight;

    private static final int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

    public static final int SURFACE_PBUFFER = 1;
    public static final int SURFACE_PIM = 2;
    public static final int SURFACE_WINDOW = 3;

    private int surfaceType = SURFACE_PBUFFER;
    private Object surface_native_obj;

    private int red = 8;
    private int green = 8;
    private int blue = 8;
    private int alpha = 8;
    private int depth = 16;
    private int renderType = 4;
    private int bufferType = EGL10.EGL_SINGLE_BUFFER;
    private EGLContext shareContext = EGL14.EGL_NO_CONTEXT;


    public void config(int red, int green, int blue, int alpha, int depth, int renderType) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
        this.depth = depth;
        this.renderType = renderType;
    }

    /**
     * @param type 如果type为SURFACE_WINDOW 时需要设置 surface
     *             如果type为SURFACE_PBUFFER 时需要设置宽高 此时不需要调用这个方法，也不需要设置surface
     * @param obj
     */
    public void setSurfaceType(int type, Object... obj) {
        this.surfaceType = type;
        if (obj != null) {
            this.surface_native_obj = obj[0];
        }
    }

    /**
     * @param width
     * @param height
     * eglCreatePbufferSurface  需要指定egl中的图片数据的宽高
     */
    public void setWidthAndHeight(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
    }

    public GlError eglInit() {
        int[] attributes = new int[]{
                EGL10.EGL_RED_SIZE, red,  //指定RGB中的R大小（bits）
                EGL10.EGL_GREEN_SIZE, green, //指定G大小
                EGL10.EGL_BLUE_SIZE, blue,  //指定B大小
                EGL10.EGL_ALPHA_SIZE, alpha, //指定Alpha大小，以上四项实际上指定了像素格式
                EGL10.EGL_DEPTH_SIZE, depth, //指定深度缓存(Z Buffer)大小
                EGL10.EGL_RENDERABLE_TYPE, renderType, //指定渲染api版本, EGL14.EGL_OPENGL_ES2_BIT
                EGL10.EGL_NONE};  //总是以EGL10.EGL_NONE结尾

        //获取Display
        mEglDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);

        if (mEglDisplay == EGL14.EGL_NO_DISPLAY) {
            throw new RuntimeException("unable to get EGL14 display");
        }

        int[] version = new int[2];    //主版本号和副版本号
        EGL14.eglInitialize(mEglDisplay, version, 0, version, 1);
        //选择Config
        int[] configNum = new int[1];
        EGLConfig[] configs = new EGLConfig[1];
        if (!EGL14.eglChooseConfig(mEglDisplay, attributes, 0, configs, 0, configs.length, configNum, 0)) {
            throw new RuntimeException("EGL error " + EGL14.eglGetError());
        }
        mEglConfig = configs[0];
        //创建Surface

        mEglSurface = createSurface();
        //创建Context
        int[] contextAttr = new int[]{
                EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL10.EGL_NONE
        };
        mEglContext = EGL14.eglCreateContext(mEglDisplay, mEglConfig, EGL14.EGL_NO_CONTEXT, contextAttr, 0);
        return GlError.OK;
    }

    //在完成EGL的初始化之后，需要通过eglMakeCurrent()函数来将当前的上下文切换，这样opengl的函数才能启动作用
    //该接口将申请到的display，draw（surface）和 context进行了绑定。也就是说，
    // 在context下的OpenGLAPI指令将draw（surface）作为其渲染最终目的地。
    // 而display作为draw（surface）的前端显示。调用后，当前线程使用的EGLContex为context。
    public void makeCurrent() {
        EGL14.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext);
    }

    /**
     * 释放EGL 中的上下文和 虚拟屏， 如果多个地方需要调用倒Egl 需要调用这个区切换
     */
    public void releaseEGlContextAndDisplay() {
        if (!EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)) {
            throw new RuntimeException("eglMakeCurrent failed");
        }
    }

    public boolean swapBuffers() {
        boolean result = EGL14.eglSwapBuffers(mEglDisplay, mEglSurface);
        return result;
    }

    /**
     * Sends the presentation time stamp to EGL.  Time is expressed in nanoseconds.
     */
    public void setPresentationTime(long nsecs) {
        EGLExt.eglPresentationTimeANDROID(mEglDisplay, mEglSurface, nsecs);
    }

    public void destroy() {
        if (mEglDisplay != EGL14.EGL_NO_DISPLAY) {
            EGL14.eglMakeCurrent(mEglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE,
                    EGL14.EGL_NO_CONTEXT);
            EGL14.eglDestroySurface(mEglDisplay, mEglSurface);
            EGL14.eglDestroyContext(mEglDisplay, mEglContext);
            EGL14.eglTerminate(mEglDisplay);
        }
    }


    private EGLSurface createSurface() {
        switch (surfaceType) {
            case SURFACE_WINDOW:
                int[] surAttr = new int[]{
                        EGL10.EGL_NONE
                };
                //顾名思义WindowSurface是和窗口相关的，也就是在屏幕上的一块显示区的封装，渲染后即显示在界面上。
                return EGL14.eglCreateWindowSurface(mEglDisplay, mEglConfig, surface_native_obj, surAttr, 0);
//            case SURFACE_PIM:
            //这个是一位图的形式放在内存里面
//                return EGL14.eglCreatePixmapSurface(mEglDisplay, mEglConfig, 0, attr, 0);
            default:
                if (mWidth == 0 || mHeight == 0) {
                    throw new IllegalArgumentException("width is 0 or height is 0");
                }
                //在显存中开辟一个空间，将渲染后的数据(帧)存放在这里。
                int[] surAttr1 = new int[]{
                        EGL10.EGL_WIDTH, mWidth,
                        EGL10.EGL_HEIGHT, mHeight,
                        EGL10.EGL_NONE
                };
                return EGL14.eglCreatePbufferSurface(mEglDisplay, mEglConfig, surAttr1, 0);
        }
    }

}
