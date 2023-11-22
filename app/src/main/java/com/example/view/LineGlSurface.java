package com.example.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.rander.LineRender;

public class LineGlSurface extends GLSurfaceView {

    private Renderer mRender;
    public LineGlSurface(Context context) {
        this(context, null);
    }

    public LineGlSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        mRender = new LineRender(this);
        setEGLContextClientVersion(2);
        setRenderer(mRender);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
