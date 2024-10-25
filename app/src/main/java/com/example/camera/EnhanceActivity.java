package com.example.camera;

import android.os.Bundle;
import android.widget.SeekBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.view.EnhanceSurfaceView;

public class EnhanceActivity extends AppCompatActivity {

    private EnhanceSurfaceView myGLSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_enhance);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.enhance_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        myGLSurfaceView = findViewById(R.id.glSurfaceView);


        // 初始化亮度和对比度的 SeekBar 控件
        SeekBar brightnessBar = findViewById(R.id.brightnessSeekBar);
        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float brightness = (progress - 50) / 50.0f; // 范围 -1.0 到 1.0
                myGLSurfaceView.setBrightness(brightness);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        SeekBar contrastBar = findViewById(R.id.contrastSeekBar);
        contrastBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float contrast = progress / 50.0f; // 范围 0.0 到 2.0
                myGLSurfaceView.setContrast(contrast);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

    }
}