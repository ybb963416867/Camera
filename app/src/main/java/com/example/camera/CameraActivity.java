package com.example.camera;

import android.Manifest;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.base.BaseActivity;
import com.example.util.PermissionUtils;

public class CameraActivity extends BaseActivity implements SurfaceHolder.Callback{

    private KitkatCamera kitkatCamera;
    private SurfaceView surfaceView;
    private Button switchCam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionUtils.askPermission(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10, runView);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private Runnable runView = new Runnable() {
        @Override
        public void run() {
            setContentView(R.layout.activity_camera);
            surfaceView = findViewById(R.id.surface);
            final SurfaceHolder holder = surfaceView.getHolder();
            kitkatCamera = new KitkatCamera();
            kitkatCamera.setPreviewDisplay(holder);
            holder.addCallback(CameraActivity.this);
            switchCam = findViewById(R.id.switch_cam);
            switchCam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    kitkatCamera.switchTo(kitkatCamera.getCameraId()==0?1:0);
                    kitkatCamera.setPreviewDisplay(holder);
                    kitkatCamera.setDisplayOrientation(90);
                    kitkatCamera.preview();
                }
            });
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(requestCode == 10, grantResults, runView, new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CameraActivity.this, "没有获得必要的权限", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        kitkatCamera.open(0);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        kitkatCamera.setPreviewDisplay(surfaceHolder);
        kitkatCamera.setDisplayOrientation(90);
        kitkatCamera.preview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        kitkatCamera.close();
    }
}
