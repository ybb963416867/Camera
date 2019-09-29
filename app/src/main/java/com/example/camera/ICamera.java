package com.example.camera;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.view.SurfaceHolder;

/**
 * @author yangbinbing
 * @date 2019/9/27
 * @Description
 */
public interface ICamera {
    /**
     * 开发相机
     * @param cameraId
     * @return
     */
    boolean open(int cameraId);

    /**
     * 设置摄像头的配置
     * @param config
     */
    void setConfig(Config config);

    /**
     * 摄像头的预览
     * @return
     */
    boolean preview();

    /**
     * 切换摄像头
     * @param camreraId
     * @return
     */
    boolean switchTo(int camreraId);

    /**
     * 照相
     * @param callback
     */
    void tatePhoto(TakePhotoCallback callback);

    /**
     * 关闭摄像头
     * @return
     */
    boolean close();

    /**
     * 设置照片预览
     * @param texture
     */
    void setPreviewTexture(SurfaceTexture texture);

    /**
     * 设置摄像头预览的回调
     * @param callback
     */
    void setOnPreviewFrameCallback(PreviewFrameCallback callback);

    /**
     * 设置摄像头的画布
     * @param holder
     */
    void setPreviewDisplay(SurfaceHolder holder);

    /**
     * 设置摄像头的偏转
     * @param i
     */
    void setDisplayOrientation(int i);


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
