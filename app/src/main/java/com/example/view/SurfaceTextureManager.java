package com.example.view;

/**
 * @author yangbinbing
 * @date 2019/11/12
 * @Description
 */

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.Log;

import com.example.filter.AFilter;
import com.example.filter.GrayFilter;
import com.example.filter.OesFilter;

/**
 * Manages a SurfaceTexture.  Creates SurfaceTexture and TextureRender objects, and provides
 * functions that wait for frames and render them to the current EGL surface.
 * <p>
 * The SurfaceTexture can be passed to Camera.setPreviewTexture() to receive camera output.
 */
public class SurfaceTextureManager implements SurfaceTexture.OnFrameAvailableListener {
    private SurfaceTexture mSurfaceTexture;
    private String TAG = "SurfaceTextureManager";

    private Object mFrameSyncObject = new Object();
    private boolean mFrameAvailable;
    private AFilter oesFilter;

    /**
     * Creates instances of TextureRender and SurfaceTexture.
     */
    public SurfaceTextureManager(Context context) {
        oesFilter = new OesFilter(context.getResources());
        //可以使用这个滤镜，但是，需要改下片元着色器的配置，2个地方需要改
//        oesFilter =new GrayFilter(context.getResources());
        oesFilter.create();
        mSurfaceTexture = new SurfaceTexture(oesFilter.getTextureId());

        mSurfaceTexture.setOnFrameAvailableListener(this);
    }

    public void release() {
        oesFilter = null;
        mSurfaceTexture = null;
    }

    /**
     * Returns the SurfaceTexture.
     */
    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    /**
     * Latches the next buffer into the texture.  Must be called from the thread that created
     * the OutputSurface object.
     */
    public void awaitNewImage() {
        //这是一个同步锁而已，只能同时一个县城访问synchronized代码块
        synchronized (mFrameSyncObject) {
            while (!mFrameAvailable) {
                try {
                    // Wait for onFrameAvailable() to signal us.  Use a timeout to avoid
                    // stalling the test if it doesn't arrive.
                    mFrameSyncObject.wait();
                    if (!mFrameAvailable) {
                        // TODO: if "spurious wakeup", continue while loop
                        throw new RuntimeException("Camera frame wait timed out");
                    }
                } catch (InterruptedException ie) {
                    // shouldn't happen
                    throw new RuntimeException(ie);
                }
            }
            mFrameAvailable = false;
        }
        mSurfaceTexture.updateTexImage();
    }

    /**
     * Draws the data from SurfaceTexture onto the current EGL surface.
     */
    public void drawImage() {
        oesFilter.draw();
    }

    @Override
    public void onFrameAvailable(SurfaceTexture st) {
        Log.d(TAG, "new frame available");
        synchronized (mFrameSyncObject) {
            if (mFrameAvailable) {
                throw new RuntimeException("mFrameAvailable already set, frame could be dropped");
            }
            mFrameAvailable = true;
            mFrameSyncObject.notifyAll();
        }
    }
}

