package com.example.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

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
}
