package com.example.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.example.manager.CameraMediaControl;
import com.example.manager.CameraMediaControl2;
import com.example.rander.CameraDrawer2;
import com.example.rander.IDrawer;
import com.example.rander.VideoDrawer;
import com.example.util.Gl2Utils;

import java.lang.reflect.Array;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author yangbinbing
 * @date 2019/10/28
 * @Description
 */
public class VideoCameraGlSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {

    private CameraTouchControl control;
    private Context context;
    private CameraMediaControl2 mediaControl;
//    private CameraMediaControl mediaControl;
    private ArrayList mSurfaceTextures = new ArrayList<SurfaceTexture>();
    private ArrayList<IDrawer> mDirectDrawers = new ArrayList<>();

    public VideoCameraGlSurfaceView(Context context) {
        this(context, null);
    }

    public VideoCameraGlSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        setRenderer(this);
        control = new CameraTouchControl(this);
        control.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                IDrawer directDrawer = mDirectDrawers.remove(mDirectDrawers.size() - 1);
                mDirectDrawers.add(0, directDrawer);
            }
        });

        mediaControl = new CameraMediaControl2(context);
//        mediaControl = new CameraMediaControl(context);
        mediaControl.prepare();
    }

    @SuppressLint("Recycle")
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        int[] textureID = Gl2Utils.createTextureID(2);
        for (int i : textureID) {
            SurfaceTexture surfaceTexture = new SurfaceTexture(i);
            mSurfaceTextures.add(surfaceTexture);
            if (i == 0) {
                mDirectDrawers.add(new CameraDrawer2(i, context, surfaceTexture));
            } else {
                mDirectDrawers.add(new VideoDrawer(i, context.getResources(), surfaceTexture));
            }
        }

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float rate = height / width;
        mediaControl.bindSurface(mSurfaceTextures, rate);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        for (int i = 0; i < mDirectDrawers.size(); i++) {
            IDrawer directDrawer = mDirectDrawers.get(i);
            if (i == 0) {
                Matrix.setIdentityM(directDrawer.getMvp(), 0);
            } else {
                control.calculateMatrix(directDrawer.getMvp());
            }
            directDrawer.draw();
        }
    }

    public void  switchCamera(){
        mediaControl.switchCamera();
    }

    public void  onDestroy(){
        mediaControl.onDestroy();
    }
}
