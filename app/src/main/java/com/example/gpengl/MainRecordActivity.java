package com.example.gpengl;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.camera.R;
import com.example.gpengl.first.FirstActivity;
import com.example.gpengl.second.SecondActivity;
import com.example.gpengl.third.ThirdActivity;


public class MainRecordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPermission();
        setContentView(R.layout.activity_main_record);
    }

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private void getPermission() {
        ActivityCompat.requestPermissions(this,
                PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
    }

    public void first(View view) {
        startActivity(new Intent(MainRecordActivity.this, FirstActivity.class));
    }

    public void second(View view) {
        startActivity(new Intent(MainRecordActivity.this, SecondActivity.class));
    }

    public void third(View view) {
        startActivity(new Intent(MainRecordActivity.this, ThirdActivity.class));
    }
}
