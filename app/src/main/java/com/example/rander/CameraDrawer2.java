package com.example.rander;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import com.example.camera.CameraCapture;
import com.example.camera.KitkatCamera;
import com.example.camera.R;
import com.example.util.Gl2Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * @author yangbinbing
 * @date 2019/10/29
 * @Description
 */
public class CameraDrawer2 implements IDrawer {

    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawListBuffer;
    private FloatBuffer mUVTexVertexBuffer;
    private int mProgram = 0;
    private int mPositionHandle = 0;
    private int mTextureCoordinatorHandle = 0;
    private int mMVPMatrixHandle = 0;
    private Context context;
    private SurfaceTexture mSurface;

    private float[] mVertices = {
            -1f, 1f,    // top left
            -1f, -1f,  // bottom left
            1f, -1f, // bottom right
            1f, 1f, // top right
    };


    private float mTextHeightRatio = 0.1f;


    private float[] UV_TEX_VERTEX = {
            0f, 1f - mTextHeightRatio,  // bottom right
            1f, 1f - mTextHeightRatio,  // bottom left
            1f, 0f + mTextHeightRatio,  // top left
            0f, 0f + mTextHeightRatio  // top right
    };


    private short[] DRAW_ORDER = {0, 2, 1, 0, 3, 2};

    private float[] mMVP = new float[16];

    private int mTexture;

    public CameraDrawer2(int texture, Context context, SurfaceTexture mSurface) {
        this.context = context;
        this.mTexture = texture;
        this.mSurface = mSurface;
        init();
    }

    public  void  init() {
        String vertexShader = Gl2Utils.uRes(context.getResources(), "shader/video_vertex_shader.glsl");
        String fragmentShader = Gl2Utils.uRes(context.getResources(),"shader/video_normal_fragment_shader.glsl");

        mProgram = Gl2Utils.createGlProgram(vertexShader, fragmentShader);
        if (mProgram == 0) {
            throw new RuntimeException("Unable to create GLES program");
        }

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        Gl2Utils.checkLocation(mPositionHandle, "vPosition");

        mTextureCoordinatorHandle = GLES20.glGetAttribLocation(mProgram, "inputTextureCoordinate");
        Gl2Utils.checkLocation(mTextureCoordinatorHandle, "inputTextureCoordinate");

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        Gl2Utils.checkLocation(mMVPMatrixHandle, "uMVPMatrix");

        mDrawListBuffer = ByteBuffer.allocateDirect(DRAW_ORDER.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(DRAW_ORDER);
        mVertexBuffer = ByteBuffer.allocateDirect(mVertices.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(mVertices);
        mUVTexVertexBuffer = ByteBuffer.allocateDirect(UV_TEX_VERTEX.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(UV_TEX_VERTEX);

        mUVTexVertexBuffer.position(0);
        mDrawListBuffer.position(0);
        mVertexBuffer.position(0);
        Matrix.setIdentityM(mMVP, 0);
    }


    @Override
    public   void  draw() {
        if (CameraCapture.get().getCameraPosition() == 1) {
            mMVP[5] = Math.abs(mMVP[5]);
        } else {
            mMVP[5] = -Math.abs(mMVP[5]);
        }
        mSurface.updateTexImage();
        GLES20.glUseProgram(mProgram);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture);

        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0,
                mVertexBuffer);

        GLES20.glEnableVertexAttribArray(mTextureCoordinatorHandle);
        GLES20.glVertexAttribPointer(mTextureCoordinatorHandle, 2, GLES20.GL_FLOAT, false, 0,
                mUVTexVertexBuffer);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVP, 0);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, DRAW_ORDER.length,
                GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTextureCoordinatorHandle);
        GLES20.glDisableVertexAttribArray(mMVPMatrixHandle);
    }

    @Override
    public float[] getMvp() {
        return mMVP;
    }
}
