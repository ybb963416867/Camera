package com.example.camera;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.SurfaceTexture;

/**
 * @author yangbinbing
 * @date 2019/9/27
 * @Description
 */
public interface ICamera {
    boolean open(int cameraId);

    void setConfig(Config config);

    boolean preview();

    boolean switchTo(int camreraId);

    void tatePhoto(TakePhotoCallback callback);

    boolean close();

    void setPreviewTexture(SurfaceTexture texture);

    void setOnPreviewFrameCallback(PreviewFrameCallback callback);


    Point getPreviewSize();

    Point getPictureSize();


    interface TakePhotoCallback {
        void onTakePhoto(byte[] bytes, int width, int height);
    }

    interface PreviewFrameCallback {
        void onPreviewFrame(byte[] bytes, int width, int height);
    }

    class Config {
        /**
         * 宽高比
         */
        float rate;
        int minPreviewWidth;
        int minPictureWidth;
    }
}
