package com.example.view;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.camera.KitkatCamera;

/**
 * @author yangbinbing
 * @date 2019/9/29
 * @Description
 */
public class CustomSurfaceViewCamera extends SurfaceView implements SurfaceHolder.Callback {

    private String TAG = "CustomSurfaceViewCamera";

    private KitkatCamera kitkatCamera;

    public CustomSurfaceViewCamera(Context context) {
        this(context,null);
    }

    public CustomSurfaceViewCamera(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSurfaceViewCamera(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        kitkatCamera = new KitkatCamera();
        kitkatCamera.open(0);
        kitkatCamera.setPreviewDisplay(getHolder());
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.e(TAG,"surfaceCreated");
        kitkatCamera.setPreviewDisplay(surfaceHolder);
        if (this.getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT){
            kitkatCamera.setDisplayOrientation(90);
        }else {
            kitkatCamera.setDisplayOrientation(180);
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.e(TAG,"surfaceChanged");
        kitkatCamera.preview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        kitkatCamera.close();
    }

    public  void  switchCamera(){
        kitkatCamera.switchTo(kitkatCamera.getCameraId()==0?1:0);
        kitkatCamera.setPreviewDisplay(getHolder());
        kitkatCamera.setDisplayOrientation(90);
        kitkatCamera.preview();
    }
}
