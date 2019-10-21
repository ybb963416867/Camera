package com.example.camera;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.rander.CameraDrawer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author yangbinbing
 * @date 2019/10/21
 * @Description
 */
public class CameraView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private KitkatCamera mCamera2;
    private CameraDrawer mCameraDrawer;
    private int cameraId;
    private Runnable mRunnable;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        mCamera2 = new KitkatCamera();
        mCameraDrawer = new CameraDrawer(getResources());
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        mCameraDrawer.onSurfaceCreated(gl10, eglConfig);
        if (mRunnable != null) {
            mRunnable.run();
            mRunnable = null;
        }
        mCamera2.open(cameraId);
        Point point = mCamera2.getPreviewSize();
        mCameraDrawer.setDataSize(point.x, point.y);
        mCamera2.setPreviewTexture(mCameraDrawer.getSurfaceTexture());
        mCameraDrawer.getSurfaceTexture().setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                requestRender();
            }
        });
        mCamera2.preview();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        mCameraDrawer.setViewSize(width, height);
        GLES20.glViewport(0, 0, width, height);
    }

    public void switchCamera() {
        mRunnable = new Runnable() {

            @Override
            public void run() {
                mCamera2.close();
                cameraId = cameraId == 1 ? 0 : 1;
            }
        };
        onPause();
        onResume();
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        mCameraDrawer.onDrawFrame(gl10);
    }

    @Override
    public void onPause() {
        super.onPause();
        mCamera2.close();
    }
}
