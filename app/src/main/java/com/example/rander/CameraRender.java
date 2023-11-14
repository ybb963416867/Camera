package com.example.rander;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.nfc.Tag;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.example.filter.OesFilter;
import com.example.util.Gl2Utils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author yangbinbing
 * @date 2019/10/21
 * @Description
 */
public class CameraRender implements GLSurfaceView.Renderer {

    private final OesFilter mOesFilter;
    private int dataWidth;
    private int dataHeight;
    private float[] matrix=new float[16];
    private int width;
    private int height;
    private int cameraId;
    private SurfaceTexture  surfaceTexture;
    private String TAG = "CameraRender";

    public CameraRender(Resources res) {
        mOesFilter = new OesFilter(res);
    }

    public void  setDataSize(int dataWidth,int dataHeight){
        this.dataWidth=dataWidth;
        this.dataHeight=dataHeight;
        calculateMatrix();
    }

    public void setViewSize(int width,int height){
        this.width=width;
        this.height=height;
        calculateMatrix();
    }

    private void calculateMatrix() {
        Gl2Utils.getPicOriginMatrix(matrix,this.dataWidth,this.dataHeight,this.width,this.height);
        if (cameraId == 1) {
            Log.e(TAG, "1");
            Gl2Utils.flip(matrix, true, false);
            Gl2Utils.rotate(matrix, 90);
        } else {
            Log.e(TAG, "0");
            Gl2Utils.rotate(matrix, 270);
        }
        mOesFilter.setMatrix(matrix);
    }

    public SurfaceTexture  getSurfaceTexture(){
        return surfaceTexture;
    }

    public void setCameraId(int id){
        this.cameraId=id;
        calculateMatrix();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        int texture = Gl2Utils.createTextureId();
        surfaceTexture = new SurfaceTexture(texture);
        mOesFilter.create();
        mOesFilter.setTextureId(texture);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        setViewSize(width,height);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        if (surfaceTexture!=null){
            surfaceTexture.updateTexImage();
        }
        mOesFilter.draw();
    }
}
