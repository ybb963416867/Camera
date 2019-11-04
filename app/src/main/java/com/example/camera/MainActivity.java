package com.example.camera;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.base.BaseActivity;
import com.example.view.VideoGLSurfaceView;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.camera2).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CameraActivity.class)));
        findViewById(R.id.camera2).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, Camera2Activity.class)));
        findViewById(R.id.camera3).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, Camera2Activity.class)));
        findViewById(R.id.video_preview).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, VideoPreviewActivity.class)));
        findViewById(R.id.video_camera_preview).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, VideoCameraActivity.class)));
        findViewById(R.id.egl).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EglPictureActivity.class)));
    }


}
