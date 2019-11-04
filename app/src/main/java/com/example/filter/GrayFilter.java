package com.example.filter;

import android.content.res.Resources;

/**
 * @author yangbinbing
 * @date 2019/11/4
 * @Description
 */
public class GrayFilter extends AFilter {
    public GrayFilter(Resources mRes) {
        super(mRes);
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/base_vert.glsl","shader/gray_fragment.glsl");
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }
}
