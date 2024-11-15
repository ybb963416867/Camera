package com.example.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import com.example.rander.FBOPictureRender;

/**
 * @author yangbinbing
 * @date 2019/11/6
 * @Description 创建一个用于显示图片的SurfaceView
 */
public class FBOPictureGLSurface extends GLSurfaceView {

    private FBOPictureRender pictureRender;

    public FBOPictureGLSurface(Context context) {
        this(context, null);
    }

    public FBOPictureGLSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
//        Log.e("ybb", "init");
        setEGLContextClientVersion(2);
        pictureRender = new FBOPictureRender(getResources());
        setRenderer(pictureRender);
        //这个渲染模式为当调用requestRender()是Render会去主动渲染
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public void setBitmap(Bitmap bitmap) {
//        Log.e("ybb", "setBitmap");
        pictureRender.setBitmap(bitmap);
        requestRender();
    }

    public void setPicCallBack(FBOPictureRender.PicCallBack pb) {
        pictureRender.setCallBack(pb);
    }



}
