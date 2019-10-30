package com.example.manager;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.Surface;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.example.camera.KitkatCamera;
import com.example.camera.R;

import java.io.IOException;
import java.util.List;

/**
 * @author yangbinbing
 * @date 2019/10/28
 * @Description
 */
public class CameraMediaControl implements LifecycleObserver {

    private Context context;
    private MediaPlayer mediaPlayer;
    private Uri videoUrl ;
    private final KitkatCamera kitkatCamera;

    public CameraMediaControl(Context context) {
        this.context = context;
        mediaPlayer = new MediaPlayer();
        kitkatCamera = new KitkatCamera();
        videoUrl = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.video1);
    }

    public void prepare() {
        kitkatCamera.open(kitkatCamera.getCameraId());
        try {
            mediaPlayer.setDataSource(context, videoUrl);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void bindSurface(List<SurfaceTexture> surface, float rate) {
        Log.e("ybb","bindSurface");
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, videoUrl);
        kitkatCamera.setPreviewTexture(surface.get(0));
        kitkatCamera.preview();
        mediaPlayer.setSurface(new Surface(surface.get(1)));
        mediaPlayer.start();
        mediaPlayer.setVolume(0f, 0f);
        mediaPlayer.setLooping(true);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause(){
        kitkatCamera.close();
        mediaPlayer.pause();
    }

    public void onDestroy(){
        kitkatCamera.close();
        mediaPlayer.release();
    }

    public void  switchCamera(){
        kitkatCamera.switchTo(kitkatCamera.getCameraId() == 0 ? 1 : 0);
        kitkatCamera.setPreviewTexture(kitkatCamera.getSurfaceTexture());
        kitkatCamera.setDisplayOrientation(180);
        kitkatCamera.preview();
    }

}
