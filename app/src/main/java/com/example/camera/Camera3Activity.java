package com.example.camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.util.PermissionUtils;
import com.example.view.CameraView;
import com.example.view.FocusImageView;

public class Camera3Activity extends AppCompatActivity {

    private CameraView mCameraView;
    private FocusImageView focusImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionUtils.askPermission(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10, runView);
    }

    private Runnable runView = new Runnable() {
        @Override
        public void run() {
            setContentView(R.layout.activity_camera3);
            mCameraView = findViewById(R.id.texture_view);
            focusImageView = findViewById(R.id.focus_iv);
            mCameraView.setAutoFocus(new KitkatCamera.AutoFocus() {
                @Override
                public void startFocus(Point point) {
                    focusImageView.startFocus(point);
                }

                @Override
                public void focusSuccess() {
                    focusImageView.onFocusSuccess();
                }

                @Override
                public void focusFailed() {
                    focusImageView.onFocusFailed();
                }
            });
            findViewById(R.id.btn_switch).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCameraView.switchCamera();
                }
            });
        }
    };

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(requestCode == 10, grantResults, runView, new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Camera3Activity.this, "没有获得必要的权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraView.onPause();
    }
}
