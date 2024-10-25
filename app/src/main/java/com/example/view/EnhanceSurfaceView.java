package com.example.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.rander.EnhanceRender;

public class EnhanceSurfaceView extends GLSurfaceView {

    private EnhanceRender mRenderer;

    public EnhanceSurfaceView(Context context) {
        this(context, null);
    }

    public EnhanceSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 设置 OpenGL ES 2.0 环境
        setEGLContextClientVersion(2);

        mRenderer = new EnhanceRender(context);

        // 设置渲染器
        setRenderer(mRenderer);

        // 设置渲染模式为仅在有变化时才渲染
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    // 设置亮度
    public void setBrightness(float brightness) {
        mRenderer.setBrightness(brightness);
        requestRender();
    }

    // 设置对比度
    public void setContrast(float contrast) {
        mRenderer.setContrast(contrast);
        requestRender();
    }
}
