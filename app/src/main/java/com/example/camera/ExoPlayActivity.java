package com.example.camera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import android.net.Uri;
import android.os.Bundle;

public class ExoPlayActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exo_play);

        PlayerView playerView = findViewById(R.id.play_view);
        // /storage/emulated/0/Android/data/com.example.camera/cache/Movies/01000003.mp4
        Uri videoUri = Uri.parse("/storage/emulated/0/Android/data/com.yunshen.wscanner/files/01000003.mp4");
        MediaItem mediaItem = MediaItem.fromUri(videoUri);

        ExoPlayer player = new ExoPlayer.Builder(this).build();
        player.addMediaItem(mediaItem);
        player.prepare();
        playerView.setPlayer(player);
    }
}