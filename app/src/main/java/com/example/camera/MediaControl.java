package com.example.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.icu.text.RelativeDateTimeFormatter;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.io.IOException;

/**
 * @author yangbinbing
 * @date 2019/10/25
 * @Description
 */
public class MediaControl implements LifecycleObserver {
    private Context context;
    private MediaPlayer mediaPlayer;
    private Uri videoUrl ;

    public MediaControl(Context context) {
        this.context = context;
        mediaPlayer = new MediaPlayer();
        videoUrl = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.video1);
    }

    public void prepare() {
        try {
            mediaPlayer.setDataSource(context, videoUrl);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void bindSurface(SurfaceTexture surface, float rate) {
        Log.e("ybb","bindSurface");
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, videoUrl);
        mediaPlayer.setSurface(new Surface(surface));

        mediaPlayer.start();
        mediaPlayer.setVolume(0f, 0f);

        mediaPlayer.setLooping(true);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause(){
        mediaPlayer.pause();
    }


}




























