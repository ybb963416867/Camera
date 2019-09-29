package com.example.camera;

import android.Manifest;
import android.os.Bundle;
import android.view.View;

import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.base.BaseActivity;
import com.example.util.PermissionUtils;
import com.example.view.CustomSurfaceViewCamera;

public class CameraActivity extends BaseActivity {

    private CustomSurfaceViewCamera surfaceView;

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
            findViewById(R.id.switch_cam).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    surfaceView.switchCamera();
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

}
