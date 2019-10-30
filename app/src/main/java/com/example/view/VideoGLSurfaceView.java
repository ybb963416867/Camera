package com.example.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.example.camera.MediaControl;
import com.example.rander.VideoDrawer;
import com.example.util.Gl2Utils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author yangbinbing
 * @date 2019/10/25
 * @Description
 */
public class VideoGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private Context context;
    private MediaControl mediaControl;
    private VideoDrawer videoDrawer;
    private SurfaceTexture surfaceTexture;

    public VideoGLSurfaceView(Context context) {
        this(context, null);
    }

    public VideoGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        setEGLContextClientVersion(2);
        setRenderer(this);
        mediaControl = new MediaControl(context);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        int textureId = Gl2Utils.createTextureId();
        surfaceTexture = new SurfaceTexture(textureId);
        videoDrawer = new VideoDrawer(textureId, context.getResources(), surfaceTexture);
        mediaControl.prepare();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        GLES20.glViewport(0, 0, i, i1);
        mediaControl.bindSurface(surfaceTexture, 1);
    }

    @Override
    public void onPause() {
        super.onPause();
        mediaControl.onPause();
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        videoDrawer.draw();
    }
}
