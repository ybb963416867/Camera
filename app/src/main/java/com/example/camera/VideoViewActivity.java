package com.example.camera;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import android.widget.VideoView;

public class VideoViewActivity extends AppCompatActivity {

    public static final String VIDEO_PATH = "video_path";
    private VideoView videoView;
    private String TAG = "VideoViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_view);
        videoView = findViewById(R.id.video_view);

        String stringExtra = getIntent().getStringExtra(VIDEO_PATH);
        if (TextUtils.isEmpty(stringExtra)) {
            Log.i(TAG, stringExtra);
            Toast.makeText(this, "文本路径错误", Toast.LENGTH_SHORT).show();
        }else {
            videoView.setVideoPath(stringExtra);
            videoView.start();
        }
    }

    public static void launch(Activity activity, String videoPath) {
        Intent intent = new Intent(activity, VideoViewActivity.class);
        intent.putExtra(VIDEO_PATH, videoPath);
        activity.startActivity(intent);
    }
}
