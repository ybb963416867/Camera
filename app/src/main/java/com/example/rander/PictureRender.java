package com.example.rander;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import com.example.filter.PicFilter;
import com.example.util.Gl2Utils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author yangbinbing
 * @date 2019/11/7
 * @Description
 */
public class PictureRender implements GLSurfaceView.Renderer {
    private Bitmap mBitmap;
    private final PicFilter picFilter;
    private String TAG="PictureRender";

    public PictureRender(Resources res) {
        picFilter = new PicFilter(res);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        picFilter.create();

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged");
        //按GLSurfaceView 的大小进行显示
        GLES20.glViewport(0, 0, 200, 200);
        //按图片的原本大小居中进行显示
        //GLES20.glViewport((width-mBitmap.getWidth())/2,(height-mBitmap.getHeight())/2,mBitmap.getWidth(),mBitmap.getHeight());
        //Gl2Utils.getPicOriginMatrix(Gl2Utils.getOriginalMatrix(),mBitmap.getWidth(),mBitmap.getHeight(),width,height);
        Gl2Utils.getPicOriginMatrix(picFilter.getMatrix(), mBitmap.getWidth(), mBitmap.getHeight(), 200, 200, 4);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d(TAG,"onDrawFrame");
        if (mBitmap != null && !mBitmap.isRecycled()) {
            int textureID = Gl2Utils.createTexture(mBitmap);
            picFilter.setTextureId(textureID);
            picFilter.draw();
        }
    }

    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }
}
