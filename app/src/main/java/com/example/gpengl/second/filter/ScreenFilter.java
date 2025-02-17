package com.example.gpengl.second.filter;

import android.content.Context;

import com.example.camera.R;


/**
 * 负责往屏幕上渲染
 */
public class ScreenFilter extends AbstractFilter {

    public ScreenFilter(Context context) {
        super(context, R.raw.base_vertex, R.raw.base_frag);
    }

    @Override
    protected void initCoordinate() {
        super.initCoordinate();
        mGLTextureBuffer.clear();
        float[] texture = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f
        };
        mGLTextureBuffer.put(texture);
    }
}
