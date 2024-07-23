package com.example.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.rander.ColorRender;

public class ColorSurfaceView extends GLSurfaceView {
    private ColorRender mRenderer;


    public ColorSurfaceView(Context context) {
        this(context, null);
    }

    public ColorSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){

        // 创建OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        // 设置渲染器，并传入纹理的宽和高
        mRenderer = new ColorRender(getResources());  // 假设宽和高为256
        setRenderer(mRenderer);

        // 仅在绘制数据发生变化时才绘制视图（节省电量）
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
