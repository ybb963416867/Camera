package com.example.camera;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        CustomGLSurfaceView customGLSurfaceView = findViewById(R.id.customGLSurfaceView);
        findViewById(R.id.but_recorder_start).setOnClickListener(v -> {

        });

        findViewById(R.id.but_recorder_stop).setOnClickListener(v -> {

        });

//        // 设置图片的宽和高
//        customGLSurfaceView.setImageDimensions(300, 300);
//
//        // 设置显示区域的边界
//        customGLSurfaceView.setDisplayArea(100, 500, 100, 700);

    }
}