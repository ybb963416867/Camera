package com.example.camera;

import android.content.Intent;
import android.os.Bundle;

import com.example.base.BaseActivity;
import com.example.gpengl.MainRecordActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.camera1).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CameraActivity.class)));
        findViewById(R.id.camera2).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, Camera2Activity.class)));
        findViewById(R.id.camera3).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, Camera3Activity.class)));
        findViewById(R.id.video_preview).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, VideoPreviewActivity.class)));
        findViewById(R.id.video_camera_preview).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, VideoCameraActivity.class)));
        findViewById(R.id.egl).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EglPictureActivity.class)));
        findViewById(R.id.pic).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PicGlSurfaceActivity.class)));
        findViewById(R.id.fbo).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, FboActivity.class)));
        findViewById(R.id.but_mediaCodec).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EncodeAndMuxActivity.class)));
        findViewById(R.id.openGl_video_recode).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, OpenglVideoRecodeActivity.class)));
        findViewById(R.id.edit_video).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DecodeVideoEditEncodeMuxAudioVideoActivity.class)));
        findViewById(R.id.draw_line).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, LineActivity.class)));
        findViewById(R.id.but_recorder).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MainRecordActivity.class)));
        findViewById(R.id.but_exo_play).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ExoPlayActivity.class)));
        findViewById(R.id.but_color_texture).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ColorTextureActivity.class)));
        findViewById(R.id.but_enhance).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, EnhanceActivity.class)));
        findViewById(R.id.but_multi_texture).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MultipleTextureActivity.class)));
        findViewById(R.id.but_multi_texture_combine).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MultipleTextureCombineActivity.class)));
        findViewById(R.id.but_multi_texture_background).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MultipleTextureBackgroundActivity.class)));
        findViewById(R.id.but_multi_texture_touch).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MultipleTextureTouchActivity.class)));
        findViewById(R.id.but_recode).setOnClickListener(v -> startActivity(new Intent(this, ExtraTextureRecodeActivity.class)));
        findViewById(R.id.but_recode_off_screen).setOnClickListener(v -> startActivity(new Intent(this, ExtraTextureRecodeOffScreenActivity.class)));
        findViewById(R.id.but_recode_off_screen_view_location).setOnClickListener(v -> startActivity(new Intent(this, OffScreenRenderViewRecodeActivity.class)));
        findViewById(R.id.but_recode_ffmpeg).setOnClickListener(v -> startActivity(new Intent(this, FFmpegRecodeActivity.class)));
        findViewById(R.id.but_test).setOnClickListener(v -> startActivity(new Intent(MainActivity.this, TestActivity.class)));
    }


}
