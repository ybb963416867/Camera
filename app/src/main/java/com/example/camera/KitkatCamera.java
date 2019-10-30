package com.example.camera;

import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author yangbinbing
 * @date 2019/9/27
 * @Description
 */
public class KitkatCamera implements ICamera {
    private ICamera.Config mConfig;
    private CameraSizeComparator sizeComparator;
    private Camera mCamera;
    private Camera.Size preSize;
    private Camera.Size picSize;
    private Point mPicSize;
    public static int mCameraId;
    private Point mPreSize;
    private String TAG = "KitkatCamera";
    private SurfaceTexture  mSurfaceTexture;

    public KitkatCamera() {
        this.mConfig = new ICamera.Config();
        mConfig.minPictureWidth = 720;
        mConfig.minPreviewWidth = 720;
        mConfig.rate = 1.778f;
        this.sizeComparator = new CameraSizeComparator();
    }

    public int getCameraId() {
        return mCameraId;
    }

    public void setCameraId(int mCameraId) {
        this.mCameraId = mCameraId;
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    @Override
    public boolean open(int cameraId) {
        this.mCameraId = cameraId;
        mCamera = Camera.open(mCameraId);
        if (mCamera != null) {
            Camera.Parameters param = mCamera.getParameters();
            picSize = getPropPictureSize(param.getSupportedPictureSizes(), mConfig.rate,
                    mConfig.minPictureWidth);
            preSize = getPropPreviewSize(param.getSupportedPreviewSizes(), mConfig.rate, mConfig
                    .minPreviewWidth);
            //拍照后照片的尺寸
            param.setPictureSize(picSize.width, picSize.height);
            //预览后帧数据的尺寸
            param.setPreviewSize(preSize.width, preSize.height);
            mCamera.setParameters(param);
            Camera.Size pre = param.getPreviewSize();
            Camera.Size pic = param.getPictureSize();
            mPicSize = new Point(pic.height, pic.width);
            mPreSize = new Point(pre.height, pre.width);
            Log.e(TAG, "camera previewSize:" + mPreSize.x + "/" + mPreSize.y);
            return true;

        }
        return false;
    }

    @Override
    public void setConfig(Config config) {
        this.mConfig = config;
    }

    @Override
    public boolean preview() {
        if (mCamera != null) {
            mCamera.startPreview();
            return true;
        }
        return false;
    }

    @Override
    public boolean switchTo(int cameraId) {
        close();
        return open(cameraId);
    }

    public void setDisplayOrientation(int i) {
        if (mCamera != null) {
            mCamera.setDisplayOrientation(i);
        }
    }

    @Override
    public void tatePhoto(TakePhotoCallback callback) {

    }

    @Override
    public boolean close() {
        if (mCamera != null) {
            try {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {

            }
        }
        return false;
    }

    @Override
    public void setPreviewTexture(SurfaceTexture texture) {
        if (mCamera != null) {
            try {
                mSurfaceTexture = texture;
                mCamera.setPreviewTexture(texture);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param holder
     */
    @Override
    public void setPreviewDisplay(SurfaceHolder holder) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setOnPreviewFrameCallback(final PreviewFrameCallback callback) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, Camera camera) {
                    if (callback != null) {
                        callback.onPreviewFrame(bytes, mPreSize.x, mPreSize.y);
                    }
                }
            });
        }
    }

    @Override
    public Point getPreviewSize() {
        return mPicSize;
    }

    @Override
    public Point getPictureSize() {
        return mPicSize;
    }

    private class CameraSizeComparator implements Comparator<Camera.Size> {

        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.height > rhs.height) {
                return 0;
            } else if (lhs.height > rhs.height) {
                return 1;
            } else {
                return -1;
            }
        }
    }


    /**
     * @param list     相机支持的size 列表
     * @param th
     * @param minWidth
     * @return
     */
    private Camera.Size getPropPreviewSize(List<Camera.Size> list, float th, int minWidth) {
        //按照相机的高度排序
        Collections.sort(list, sizeComparator);

        int i = 0;
        for (Camera.Size s : list) {
            if ((s.height >= minWidth) && equalRate(s, th)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;
        }
        return list.get(i);
    }

    private Camera.Size getPropPictureSize(List<Camera.Size> list, float th, int minWidth) {
        Collections.sort(list, sizeComparator);

        int i = 0;
        //查找宽高比相近的一个
        for (Camera.Size s : list) {
            if ((s.height >= minWidth) && equalRate(s, th)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;
        }
        return list.get(i);
    }

    private boolean equalRate(Camera.Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        if (Math.abs(r - rate) <= 0.03) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 手动聚焦
     *
     * @param point 触屏坐标 必须传入转换后的坐标
     */
    public void onFocus(Point point, Camera.AutoFocusCallback callback) {
        Camera.Parameters parameters = mCamera.getParameters();
        boolean supportFocus = true;
        boolean supportMetering = true;
        //不支持设置自定义聚焦，则使用自动聚焦，返回
        if (parameters.getMaxNumFocusAreas() <= 0) {
            supportFocus = false;
        }
        if (parameters.getMaxNumMeteringAreas() <= 0) {
            supportMetering = false;
        }
        List<Camera.Area> areas = new ArrayList<Camera.Area>();
        List<Camera.Area> areas1 = new ArrayList<Camera.Area>();
        //再次进行转换
        point.x = (int) (((float) point.x) / mCamera.getParameters().getPreviewSize().width * 2000 - 1000);
        point.y = (int) (((float) point.y) / mCamera.getParameters().getPreviewSize().height * 2000 - 1000);

        int left = point.x - 300;
        int top = point.y - 300;
        int right = point.x + 300;
        int bottom = point.y + 300;
        left = left < -1000 ? -1000 : left;
        top = top < -1000 ? -1000 : top;
        right = right > 1000 ? 1000 : right;
        bottom = bottom > 1000 ? 1000 : bottom;
        areas.add(new Camera.Area(new Rect(left, top, right, bottom), 100));
        areas1.add(new Camera.Area(new Rect(left, top, right, bottom), 100));
        if (supportFocus) {
            parameters.setFocusAreas(areas);
        }
        if (supportMetering) {
            parameters.setMeteringAreas(areas1);
        }

        try {
            mCamera.setParameters(parameters);// 部分手机 会出Exception（红米）
            mCamera.autoFocus(callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface AutoFocus {
        /**
         * @param point 手动聚焦需要当前的操作的触摸点
         */
        void startFocus(Point point);

        /**
         * 对焦成功
         */
        void focusSuccess();

        /**
         * 对焦失败
         */
        void focusFailed();
    }
}
