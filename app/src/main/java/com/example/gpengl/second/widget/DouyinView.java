package com.example.gpengl.second.widget;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.SurfaceHolder;

public class DouyinView extends GLSurfaceView {

    private DouyinRenderer douyinRenderer;

    public DouyinView(Context context) {
        this(context,null);
    }

    public DouyinView(Context context, AttributeSet attrs) {
        super(context, attrs);
        /**
         * 配置GLSurfaceView
         */
        //设置EGL版本
        setEGLContextClientVersion(2);
        douyinRenderer = new DouyinRenderer(this);
        setRenderer(douyinRenderer);
        //设置按需渲染 当我们调用 requestRender 请求GLThread 回调一次 onDrawFrame
        // 连续渲染 就是自动的回调onDrawFrame
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        super.surfaceDestroyed(holder);
        douyinRenderer.onSurfaceDestroyed();
    }
}
