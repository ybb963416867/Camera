package com.example.filter;

import android.content.res.Resources;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * @author yangbinbing
 * @date 2019/10/21
 * @Description
 */
public class OesFilter extends AFilter {

    private int mHCoordMatrix;
    private float[] mCoordMatrix = Arrays.copyOf(OM, 16);

    public OesFilter(Resources mRes) {
        super(mRes);
    }

    /**
     *
     */
    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/oes_base_vertex.vert", "shader/oes_base_fragment.frag");
        mHCoordMatrix = GLES20.glGetUniformLocation(mProgram, "vCoordMatrix");
    }

    public void setCoordMatrix(float[] matrix) {
        this.mCoordMatrix = matrix;
    }

    @Override
    protected void onSetExpandData() {
        super.onSetExpandData();
        GLES20.glUniformMatrix4fv(mHCoordMatrix, 1, false, mCoordMatrix, 0);
    }

    @Override
    protected void onBindTexture() {
        super.onBindTexture();
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + getTextureType());
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, getTextureId());
        GLES20.glUniform1i(mHTexture, getTextureType());
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }
}
