package com.example.rander;

import android.opengl.GLES20;
import android.view.View;

import com.example.view.LineShape;
import com.example.view.Triangle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class LineRender extends Shape {
    private Shape shape;

    public LineRender(View mView) {
        super(mView);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        shape = new LineShape(mView);
        shape.onSurfaceCreated(gl, config);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        shape.onSurfaceChanged(gl, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        shape.onDrawFrame(gl);
    }
}
