package com.example.camera;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class CustomGLSurfaceView extends GLSurfaceView {
    private final GLRenderer renderer;

    public CustomGLSurfaceView(Context context) {
        this(context, null);
    }

    public CustomGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        renderer = new GLRenderer(this);
        setRenderer(renderer);
    }

    public void startRecording() {
        renderer.startRecording();
    }

    public void stopRecording() {
        renderer.stopRecording();
    }
}
