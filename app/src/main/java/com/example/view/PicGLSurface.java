package com.example.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.rander.FBOPictureRender;
import com.example.rander.PictureRender;

/**
 * @author yangbinbing
 * @date 2019/11/7
 * @Description
 */
public class PicGLSurface extends GLSurfaceView {

    private PictureRender pictureRender;

    public PicGLSurface(Context context) {
        this(context,null);
    }

    public PicGLSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        pictureRender = new PictureRender(getResources());
        setRenderer(pictureRender);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public  void  setBitmap(Bitmap bitmap){
        pictureRender.setBitmap(bitmap);
        requestRender();
    }
}
