package com.example.rander;

import android.content.res.Resources;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.example.util.Gl2Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * @author yangbinbing
 * @date 2019/10/24
 * @Description
 */
public class VideoDrawer implements IDrawer{
    private int mTexture;
    private Resources mResources;
    private SurfaceTexture mSurfaceTexture;
    /**
     * 视频的顶点坐标
     */
    private float[] VERTEX = {
            1f, -1f, 0f,// bottom right
            1f, 1f, 0f,//top right
            -1f, 1f, 0f,//top lift
            -1f, -1f, 0f,//bottom lift
    };

    private float[] UV_TEX_VERTEX = {
            1f, 1f,//右上
            1f, 0f,// 右下
            0f, 0f,//左下
            0f, 1f,//左上
    };

    private short[] DRAW_ORDER = {
            1, 0, 3, 3, 1, 2
    };

    public  float[] mMVP = new float[16];
    private int mProgram;
    private int mPositionHandle;
    private int mTextureCoordinatorHandle;
    private int uMVPMatrixHandle;
    private int mTextureHandle;
    private ShortBuffer mDrawListBuffer;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mUVTexVertexBuffer;


    public VideoDrawer(int texture, Resources res, SurfaceTexture surfaceTexture) {
        this.mTexture = texture;
        this.mResources = res;
        this.mSurfaceTexture = surfaceTexture;
        init();
    }

    private void init() {
        mProgram = Gl2Utils.createGlProgram(Gl2Utils.uRes(mResources, "shader/video_vertex.vert"), Gl2Utils.uRes(mResources, "shader/video_fragment.frag"));
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        Gl2Utils.checkLocation(mPositionHandle,"vPosition");

        mTextureCoordinatorHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        Gl2Utils.checkLocation(mTextureCoordinatorHandle,"inputTextureCoordinate");

        uMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatirx");
        Gl2Utils.checkLocation(uMVPMatrixHandle,"uMVPMatirx");

        mTextureHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");
        Gl2Utils.checkLocation(mTextureHandle,"s_texture");

        mDrawListBuffer = ByteBuffer.allocateDirect(DRAW_ORDER.length * 4)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(DRAW_ORDER);
        mDrawListBuffer.position(0);

        mVertexBuffer = ByteBuffer.allocateDirect(VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(VERTEX);
        mVertexBuffer.position(0);

        mUVTexVertexBuffer = ByteBuffer.allocateDirect(UV_TEX_VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(UV_TEX_VERTEX);
        mUVTexVertexBuffer.position(0);
        Matrix.setIdentityM(mMVP,0);
    }

    @Override
    public void  draw(){
        mSurfaceTexture.updateTexImage();
        GLES20.glUseProgram(mProgram);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,mTexture);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle,3,GLES20.GL_FLOAT,false,0,mVertexBuffer);

        GLES20.glEnableVertexAttribArray(mTextureCoordinatorHandle);
        GLES20.glVertexAttribPointer(mTextureCoordinatorHandle,2,GLES20.GL_FLOAT,false,0,mUVTexVertexBuffer);

        GLES20.glUniformMatrix4fv(uMVPMatrixHandle,1,false,mMVP,0);
        GLES20.glUniform1i(mTextureHandle,0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES,DRAW_ORDER.length,GLES20.GL_UNSIGNED_SHORT,mDrawListBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinatorHandle);
        GLES20.glDisableVertexAttribArray(uMVPMatrixHandle);
        GLES20.glDisableVertexAttribArray(mTextureHandle);
    }

    @Override
    public float[] getMvp() {
        return mMVP;
    }


}









