package com.example.camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import com.example.base.BaseActivity;
import com.example.util.PermissionUtils;
import com.example.view.CustomTextureView;
import com.example.view.FocusImageView;

public class Camera2Activity extends BaseActivity {

    private CustomTextureView textureView;
    private String TAG = "Camera2Activity";
    private FocusImageView focusImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionUtils.askPermission(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10, runView);
    }

    private Runnable runView = new Runnable() {
        @Override
        public void run() {
            setContentView(R.layout.activity_camera2);
            textureView = findViewById(R.id.texture_view);
            focusImageView = findViewById(R.id.focus_iv);
            textureView.setAutoFocus(new KitkatCamera.AutoFocus() {
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
                    textureView.switchCamera();
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
                Toast.makeText(Camera2Activity.this, "没有获得必要的权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

}
