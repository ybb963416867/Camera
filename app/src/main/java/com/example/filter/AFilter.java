package com.example.filter;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.util.SparseArray;

import com.example.util.Gl2Utils;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

/**
 * @author yangbinbing
 * @date 2019/10/18
 * @Description
 */
public abstract class AFilter {
    private static final String TAG = "AFilter";
    public static final float[] OM = Gl2Utils.getOriginalMatrix();

    /**
     * 程序句柄
     */
    protected int mProgram;

    /**
     * 定点句柄
     */
    protected int mHPosition;

    /**
     * 纹理坐标句柄
     */
    protected int mHCoord;

    /**
     * 总变换句柄
     */
    protected int mHMatrix;

    /**
     * 默认的纹理贴图
     */
    protected int mHTexture;

    protected Resources mRes;

    /**
     * 定点坐标的buffer,里面存放定点坐标数据
     */
    protected FloatBuffer mVerBuffer;

    /**
     * 纹理坐标的buffer 里面存放纹理坐标
     */
    protected FloatBuffer mTexBuffer;

    /**
     * 索引坐标的buffer
     */
    protected ShortBuffer mIndexBuffer;

    protected int mFlag = 0;

    private float[] matrix = Arrays.copyOf(OM, 16);
    private int textureType = 0;
    private int textureId = 0;

    private SparseArray<boolean[]> mBools;
    private SparseArray<int[]> mInts;
    private SparseArray<float[]> mFloats;

    /**
     * 图片的定点坐标
     */
    private float pos[] = {
            -1.0f, 1.0f,//top left
            -1.0f, -1.0f,//bottom left
            1.0f, 1.0f,// top right
            1.0f, -1.0f,//bottom right

    };

    /**
     * 纹理坐标
     */
    private float[] coord = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };

    public AFilter(Resources mRes) {
        this.mRes = mRes;
        initBuffer();
    }

    public final void create() {
        onCreate();
    }

    public final void setSize(int width, int height) {
        onSizeChanged(width, height);
    }

    public void draw() {
        onClear();
        onUseProgram();
        onSetExpandData();
        onBindTexture();
        onDraw();
    }

    public void setMatrix(float[] matrix) {
        this.matrix = matrix;
    }

    public float[] getMatrix() {
        return matrix;
    }

    public final void setTextureType(int type) {
        this.textureType = type;
    }

    public final int getTextureType() {
        return textureType;
    }

    public final int getTextureId() {
        return textureId;
    }

    public final void setTextureId(int textureId) {
        this.textureId = textureId;
    }

    public void setFlag(int flag) {
        this.mFlag = flag;
    }

    public int getFlag() {
        return mFlag;
    }

    public void setFloat(int type, float... params) {
        if (mFloats == null) {
            mFloats = new SparseArray<>();
        }
        mFloats.put(type, params);
    }

    public void setInt(int type, int... params) {
        if (mInts == null) {
            mInts = new SparseArray<>();
        }
        mInts.put(type, params);
    }

    public void setBool(int type, boolean... params) {
        if (mBools == null) {
            mBools = new SparseArray<>();
        }
        mBools.put(type, params);
    }

    public boolean getBool(int type, int index) {
        if (mBools == null) return false;
        boolean[] b = mBools.get(type);
        return !(b == null || b.length <= index) && b[index];
    }

    public int getInt(int type, int index) {
        if (mInts == null) return 0;
        int[] b = mInts.get(type);
        if (b == null || b.length <= index) {
            return 0;
        }
        return b[index];
    }

    public float getFloat(int type, int index) {
        if (mFloats == null) return 0;
        float[] b = mFloats.get(type);
        if (b == null || b.length <= index) {
            return 0;
        }
        return b[index];
    }

    public int getOutputTexture() {
        return -1;
    }

    private void initBuffer() {
        ByteBuffer a = ByteBuffer.allocateDirect(pos.length * 4);
        a.order(ByteOrder.nativeOrder());
        mVerBuffer = a.asFloatBuffer();
        mVerBuffer.put(pos);
        mVerBuffer.position(0);
        ByteBuffer b = ByteBuffer.allocateDirect(coord.length * 4);
        b.order(ByteOrder.nativeOrder());
        mTexBuffer = b.asFloatBuffer();
        mTexBuffer.put(coord);
        mTexBuffer.position(0);
    }

    /**
     * 实现此方法，完成程序的创建，可直接调用createProgram来实现
     */
    protected abstract void onCreate();

    protected abstract void onSizeChanged(int width, int height);

    protected final void createProgram(String vertex, String fragment) {
        mProgram = Gl2Utils.createGlProgram(vertex, fragment);
        mHPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        mHCoord = GLES20.glGetAttribLocation(mProgram, "vCoord");
        mHMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        mHTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");
    }

    protected final void createProgramByAssetsFile(String vertex, String fragment) {
        createProgram(Gl2Utils.uRes(mRes, vertex), Gl2Utils.uRes(mRes, fragment));
    }

    protected void onUseProgram() {
        GLES20.glUseProgram(mProgram);
    }

    /**
     *
     */
    protected void onDraw() {
        GLES20.glEnableVertexAttribArray(mHPosition);
        GLES20.glVertexAttribPointer(mHPosition, 2, GLES20.GL_FLOAT, false, 0, mVerBuffer);
        GLES20.glEnableVertexAttribArray(mHCoord);
        GLES20.glVertexAttribPointer(mHCoord, 2, GLES20.GL_FLOAT, false, 0, mTexBuffer);
        /**
         * 从第0个开始绘制，一共几个顶点
         */
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, pos.length / 2);
        GLES20.glDisableVertexAttribArray(mHPosition);
        GLES20.glDisableVertexAttribArray(mHCoord);
    }

    protected void onClear() {
        //黑色
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    }

    /**
     * 出入一个矩阵数据
     */
    protected void onSetExpandData() {
        GLES20.glUniformMatrix4fv(mHMatrix, 1, false, matrix, 0);
    }

    /**
     * // 绑定纹理
     * GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
     * GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
     *
     * 这个是处理图片用的
     */
    protected void onBindTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + textureType);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTextureId());
        GLES20.glUniform1i(mHTexture, textureType);
    }


}
