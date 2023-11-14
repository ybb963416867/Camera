package com.example.view;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.util.Log;
import android.view.Surface;

import com.example.manager.EGLHelper;
import com.example.rander.TextureRender;

import java.nio.channels.AsynchronousChannelGroup;
import java.util.IllegalFormatException;

/**
 * ********************************
 * 项目名称：
 *
 * @Author yangbinbing
 * 邮箱： 963416867@qq.com
 * 创建时间：  20:43
 * 用途
 * ********************************
 */
public class OutputSurface implements SurfaceTexture.OnFrameAvailableListener {

    private TextureRender mTextureRender;
    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private EGLHelper eglHelper;
    private String TAG = "OutputSurface";
    private Object mFrameSynObject = new Object();
    private boolean mFrameAvailable;

    public OutputSurface() {
        setup();
    }

    public OutputSurface(int Width, int height) {
        if (Width <= 0 || height <= 0) {
            throw new IllegalArgumentException();
        }
        eglSetup(Width, height);
        setup();
    }

    @SuppressLint("Recycle")
    private void setup() {
        mTextureRender = new TextureRender();
        mTextureRender.surfaceCreated();
        mSurfaceTexture = new SurfaceTexture(mTextureRender.getTextureId());
        mSurfaceTexture.setOnFrameAvailableListener(this);
        mSurface = new Surface(mSurfaceTexture);
    }

    public Surface getSurface() {
        return mSurface;
    }

    /**
     * @param surfaceTexture 如果有一帧的画面准备好那么会运行这个方法
     *                       同时唤醒draw 去更新纹理，会调用draw绘画，此时mFrameAvailable是false;如果为true 说明这帧掉落
     */
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        Log.d(TAG, "new frame available");
        synchronized (mFrameSynObject){

            if (mFrameAvailable){
                throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
            }

            mFrameAvailable = true;
            mFrameSynObject.notify();
        }
    }

    private void eglSetup(int width, int height) {
        eglHelper = new EGLHelper();
        eglHelper.setWidthAndHeight(width, height);
        eglHelper.eglInit();
        eglHelper.makeCurrent();
    }

    public void  awaitNewImage(){
        final  int TIMEOUT_MS = 500;
        synchronized (mFrameSynObject){
            while(!mFrameAvailable){
                try {
                    mFrameSynObject.wait(TIMEOUT_MS);
                    if (!mFrameAvailable){
                        throw new RuntimeException("Surface frame wait timed out");
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            mFrameAvailable = false;
        }

        mSurfaceTexture.updateTexImage();
    }

    public void  drawImage(){
        mTextureRender.drawFrame(mSurfaceTexture);
    }

    public void changeFragmentShader(String fragmentShader) {
        mTextureRender.changeFragmentShader(fragmentShader);
    }

    public void release() {
        if (eglHelper!=null){
            eglHelper.destroy();
        }
       if (mSurface!=null){
           mSurface.release();
       }
        mTextureRender = null;
        mSurfaceTexture = null;
    }
}
