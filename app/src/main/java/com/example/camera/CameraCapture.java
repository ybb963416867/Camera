package com.example.camera;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author liyachao 296777513
 * @version 1.0
 * @date 2017/3/1
 */
public class CameraCapture {

    private static final float DEFAULT_PREVIEW_RATE = 4f / 3f;

    private float mScreenRate = DEFAULT_PREVIEW_RATE;
    private Camera mCamera;
    private Camera.Parameters mParams;
    private boolean isPreviewing = false;
    private boolean isOpenBackCamera = true;

    private SurfaceTexture mSurface;

    private static CameraCapture sInstance = new CameraCapture();

    public int getCameraPosition() {
        return mCameraPosition;
    }

    private int mCameraPosition = 1;

    private CameraCapture() {

    }


    public static CameraCapture get() {
        return sInstance;
    }

    public void setRatio(float screenRate) {
        mScreenRate = screenRate;
    }


    /**
     * open back camera
     */
    public void openBackCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCamera = Camera.open(i);
                isOpenBackCamera = true;
                return;
            }
        }
        doStopCamera();
    }

    /**
     * open front camera
     */
    public void openFrontCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCamera = Camera.open(i);
                isOpenBackCamera = false;
                return;
            }
        }
        doStopCamera();
    }


    public void switchCamera(int cameraPosition) {
        doStopCamera();
        if (cameraPosition == mCameraPosition) {
            //现在是后置，变更为前置
            openFrontCamera();//打开当前选中的摄像头
            mCameraPosition = 0;
        } else {
            openBackCamera();//打开当前选中的摄像头
            mCameraPosition = 1;
        }
        doStartPreview(mSurface);
    }


    /**
     * 使用Surfaceview开启预览
     *
     * @param holder
     */
    public void doStartPreview(SurfaceHolder holder) {
        if (isPreviewing) {
            mCamera.stopPreview();
            return;
        }
        if (mCamera != null) {
            try {
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            initCamera();
        }
    }


    /**
     * 使用TextureView预览Camera
     *
     * @param surface
     */
    public void doStartPreview(SurfaceTexture surface) {
        if (isPreviewing) {
            mCamera.stopPreview();
            return;
        }
        if (mCamera != null) {
            mSurface = surface;
            try {
                mCamera.setPreviewTexture(surface);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            initCamera();
        }

    }

    /**
     * 停止预览，释放Camera
     */
    public void doStopCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            isPreviewing = false;
            mCamera.release();
            mCamera = null;
        }
    }


    public boolean isPreviewing() {
        return isPreviewing;
    }

    public boolean isOpenBackCamera() {
        return isOpenBackCamera;
    }


    private void initCamera() {
        if (mCamera != null) {

            mParams = mCamera.getParameters();
            //设置PreviewSize和PictureSize
            Camera.Size previewSize = chooseOptimalSize(
                    mParams.getSupportedPreviewSizes(), mScreenRate, 800);
            mParams.setPreviewSize(previewSize.width, previewSize.height);

            mCamera.setDisplayOrientation(90);

            // 设置摄像头为自动聚焦
            List<String> focusModes = mParams.getSupportedFocusModes();
            if (focusModes.contains("continuous-video")) {
                mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
            mCamera.setParameters(mParams);
            mCamera.startPreview();//开启预览
            isPreviewing = true;

            mParams = mCamera.getParameters(); //重新get一次
        }
    }


    public static boolean equalRate(Camera.Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        if (Math.abs(r - rate) <= 0.03) {
            return true;
        } else {
            return false;
        }
    }

    public static Camera.Size chooseOptimalSize(List<Camera.Size> list, float th, int minWidth) {
        Collections.sort(list, new CameraSizeComparator());

        int mismatchingIndex = 0;
        int i = 0;
        for (Camera.Size s : list) {
            if ((s.width >= minWidth) && equalRate(s, th)) {
                break;
            }
            if (s.width >= minWidth && mismatchingIndex == 0) {
                mismatchingIndex = i;
            }
            i++;
        }
        if (i == list.size()) {
            i = mismatchingIndex;//如果没找到，就选最小的size
        }
        return list.get(i);
    }


    // 为Size定义一个比较器Comparator
    public static class CameraSizeComparator implements Comparator<Camera.Size> {
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            // TODO Auto-generated method stub
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }

    }


}
