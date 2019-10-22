package com.example.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;

import com.example.CameraApplication;
import com.example.camera.Camera2Activity;
import com.example.camera.KitkatCamera;

/**
 * @author yangbinbing
 * @date 2019/9/30
 * @Description
 */
public class CustomTextureView extends TextureView implements TextureView.SurfaceTextureListener {

    private KitkatCamera kitkatCamera;
    private String TAG = "CustomTextureView";
    private KitkatCamera.AutoFocus autoFocus;

    public CustomTextureView(Context context) {
        this(context, null);
    }

    public CustomTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setAutoFocus(KitkatCamera.AutoFocus autoFocus) {
        this.autoFocus = autoFocus;
    }

    private void init() {
        kitkatCamera = new KitkatCamera();
        kitkatCamera.open(0);
        setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.e(TAG, "onSurfaceTextureAvailable");
        //可利用时在设置画布
        kitkatCamera.setPreviewTexture(getSurfaceTexture());
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            kitkatCamera.setDisplayOrientation(90);
        } else {
            kitkatCamera.setDisplayOrientation(180);
        }
        kitkatCamera.preview();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        Log.e(TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Log.e(TAG, "onSurfaceTextureDestroyed");
        kitkatCamera.close();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        Log.e(TAG, "onSurfaceTextureUpdated");
    }

    public void switchCamera() {
        kitkatCamera.switchTo(kitkatCamera.getCameraId() == 0 ? 1 : 0);
        kitkatCamera.setPreviewTexture(getSurfaceTexture());
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            kitkatCamera.setDisplayOrientation(90);
        } else {
            kitkatCamera.setDisplayOrientation(180);
        }
        kitkatCamera.preview();
    }

    public void onFocus(Point point, Camera.AutoFocusCallback callback) {
        kitkatCamera.onFocus(point, callback);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG, "onTouchEvent");
        //前置摄像头没有聚焦
        if (kitkatCamera.getCameraId() == 1) {
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
