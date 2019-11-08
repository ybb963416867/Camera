package com.example.view;

import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.example.CameraApplication;
import com.example.camera.KitkatCamera;
import com.example.rander.CameraRender;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author yangbinbing
 * @date 2019/10/21
 * @Description
 */
public class CameraView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private KitkatCamera mCamera2;
    private CameraRender mCameraRender;
    private int cameraId;
    private Runnable mRunnable;
    private String TAG = "CameraView";
    private KitkatCamera.AutoFocus autoFocus;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setAutoFocus(KitkatCamera.AutoFocus autoFocus) {
        this.autoFocus = autoFocus;
    }

    private void init() {
        setEGLContextClientVersion(2);
        setRenderer(this);
        //脏模式
//        setRenderMode(RENDERMODE_WHEN_DIRTY);
        mCamera2 = new KitkatCamera();
        mCameraRender = new CameraRender(getResources());
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        mCameraRender.onSurfaceCreated(gl10, eglConfig);
        if (mRunnable != null) {
            mRunnable.run();
            mRunnable = null;
        }
        mCamera2.open(mCamera2.getCameraId() == 1 ? 0 : 1);
        Point point = mCamera2.getPreviewSize();
        mCameraRender.setDataSize(point.x, point.y);
        mCamera2.setPreviewTexture(mCameraRender.getSurfaceTexture());
//        mCameraRender.getSurfaceTexture().setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
//            @Override
//            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//                requestRender();
//            }
//        });
        mCamera2.preview();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        mCameraRender.setViewSize(width, height);
        /**
         * 前两个是起始位置，后面两个是宽
         * 坐标系是Y轴朝上，左下角是0，X轴朝右，左下角是0.都是整数。
         */
        GLES20.glViewport(0, 0, width, height);
    }

    public void switchCamera() {
        mRunnable = new Runnable() {

            @Override
            public void run() {
                mCamera2.close();
            }
        };
        onPause();
        onResume();
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        mCameraRender.onDrawFrame(gl10);
    }

    @Override
    public void onPause() {
        super.onPause();
        mCamera2.close();
    }

    public void onFocus(Point point, Camera.AutoFocusCallback callback) {
        mCamera2.onFocus(point, callback);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent");
        //前置摄像头没有聚焦
        if (mCamera2.getCameraId() == 1) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                float sRawX = event.getRawX();
                float sRawY = event.getRawY();
                Log.d(TAG, "sRawX:" + sRawX + "sRawY:" + sRawY);
                float rawY = sRawY * CameraApplication.screenWidth / CameraApplication.screenHeight;
                float temp = sRawX;
                float rawX = rawY;
                rawY = (this.getWidth() - temp) * CameraApplication.screenWidth / CameraApplication.screenHeight;
                Log.d(TAG, "rawx:" + rawX + "rawy:" + rawY);
                Point point = new Point((int) rawX, (int) rawY);
                onFocus(point, new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            Log.d(TAG, "聚焦成功");
                            if (autoFocus != null) {
                                autoFocus.focusSuccess();
                            }
                        } else {
                            Log.d(TAG, "聚焦失败");
                            if (autoFocus != null) {
                                autoFocus.focusFailed();
                            }
                        }
                    }
                });
                if (autoFocus != null) {
                    autoFocus.startFocus(new Point((int) sRawX, (int) sRawY));
                }
        }
        return true;
    }
}
