package com.example.gpengl.second.widget;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;


import com.example.gpengl.CameraHelper;
import com.example.gpengl.second.filter.CameraFilter;
import com.example.gpengl.second.filter.CameraFilter2;
import com.example.gpengl.second.filter.ScreenFilter;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class DouyinRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    private ScreenFilter mScreenFilter;
    private DouyinView mView;
    private CameraHelper mCameraHelper;
    private SurfaceTexture mSurfaceTexture;
    private float[] mtx = new float[16];
    private int[] mTextures;
    private CameraFilter mCameraFilter;
    private CameraFilter2 mCameraFilter2;

    public DouyinRenderer(DouyinView douyinView) {
        mView = douyinView;
    }

    /**
     * 画布创建好啦
     *
     * @param gl
     * @param config
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //初始化的操作
        mCameraHelper = new CameraHelper(Camera.CameraInfo.CAMERA_FACING_BACK);
        //准备好摄像头绘制的画布
        //通过opengl创建一个纹理id
        mTextures = new int[1];
        //偷懒 这里可以不配置 （当然 配置了也可以）
        GLES20.glGenTextures(mTextures.length, mTextures, 0);
        mSurfaceTexture = new SurfaceTexture(mTextures[0]);
        //
        mSurfaceTexture.setOnFrameAvailableListener(this);
        //注意：必须在gl线程操作opengl
        mCameraFilter = new CameraFilter(mView.getContext());
        mScreenFilter = new ScreenFilter(mView.getContext());
        mCameraFilter2 = new CameraFilter2(mView.getContext());
    }

    /**
     * 画布发生了改变
     *
     * @param gl
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //开启预览
        mCameraHelper.startPreview(mSurfaceTexture);
        mCameraFilter2.onReady(width, height);
        mCameraFilter.onReady(width,height);
        mScreenFilter.onReady(width,height);
    }

    /**
     * 开始画画吧
     *
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        // 配置屏幕
        //清理屏幕 :告诉opengl 需要把屏幕清理成什么颜色
        GLES20.glClearColor(0, 0, 0, 0);
        //执行上一个：glClearColor配置的屏幕颜色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // 把摄像头的数据先输出来
        // 更新纹理，然后我们才能够使用opengl从SurfaceTexure当中获得数据 进行渲染
        mSurfaceTexture.updateTexImage();
        //surfaceTexture 比较特殊，在opengl当中 使用的是特殊的采样器 samplerExternalOES （不是sampler2D）
        //获得变换矩阵
        mSurfaceTexture.getTransformMatrix(mtx);
//        //
        mCameraFilter.setMatrix(mtx);
        //责任链
        int id = mCameraFilter.onDrawFrame(mTextures[0]);
        //加效果滤镜
        // id  = 效果1.onDrawFrame(id);
        // id = 效果2.onDrawFrame(id);
        //....
//        //加完之后再显示到屏幕中去
//        mScreenFilter.onDrawFrame(id);
//        mCameraFilter2.setMatrix(mtx);
//        mCameraFilter2.onDrawFrame(mTextures[0]);

    }

    /**
     * surfaceTexture 有一个有效的新数据的时候回调
     *
     * @param surfaceTexture
     */
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mView.requestRender();
    }

    public void onSurfaceDestroyed() {
        mCameraHelper.stopPreview();
    }
}
