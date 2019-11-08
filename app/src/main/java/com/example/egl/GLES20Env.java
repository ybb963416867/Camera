package com.example.egl;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.example.filter.AFilter;
import com.example.manager.EGLHelper;
import com.example.util.Gl2Utils;

import java.nio.IntBuffer;

/**
 * @author yangbinbing
 * @date 2019/11/4
 * @Description
 */
public class GLES20Env {

    private int mWidth;
    private int mHeight;
    private EGLHelper mEGLHelper;

    final static String TAG = "GLES20BackEnv";
    final static boolean LIST_CONFIGS = false;

    private AFilter mFilter;
    Bitmap mBitmap;
    String mThreadOwner;

    public GLES20Env(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        mEGLHelper = new EGLHelper();
        mEGLHelper.eglInit(width, height);
        mEGLHelper.makeCurrent();
    }

    public void setThreadOwner(String threadOwner) {
        this.mThreadOwner = threadOwner;
    }

    public void setFilter(final AFilter filter) {
        mFilter = filter;

        // Does this thread own the OpenGL context?
        if (!Thread.currentThread().getName().equals(mThreadOwner)) {
            Log.e(TAG, "setRenderer: This thread does not own the OpenGL context.");
            return;
        }
        // Call the renderer initialization routines
        mFilter.create();
        mFilter.setSize(mWidth, mHeight);
    }

    public Bitmap getBitmap() {
        if (mFilter == null) {
            Log.e(TAG, "getBitmap: Renderer was not set.");
            return null;
        }
        if (!Thread.currentThread().getName().equals(mThreadOwner)) {
            Log.e(TAG, "getBitmap: This thread does not own the OpenGL context.");
            return null;
        }
        mFilter.setTextureId(Gl2Utils.createTexture(mBitmap));
        mFilter.draw();
        return convertToBitmap();
    }

    public void destroy() {
        mEGLHelper.destroy();
        if (mBitmap != null && !mBitmap.isRecycled()) {
            mBitmap.recycle();
        }
    }


    private Bitmap convertToBitmap() {
        int[] iat = new int[mWidth * mHeight];
        IntBuffer ib = IntBuffer.allocate(mWidth * mHeight);
        mEGLHelper.mGL.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                ib);
        int[] ia = ib.array();

        // Convert upside down mirror-reversed image to right-side up normal
        // image.
        //ia数据里面的的图片与原始图片是左右和上下倒立的，是原图片的镜像
        for (int i = 0; i < mHeight; i++) {
            System.arraycopy(ia, i * mWidth, iat, (mHeight - 1 - i) * mWidth, mWidth);
        }
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(iat));
        return bitmap;
    }

    public void setInput(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

}
