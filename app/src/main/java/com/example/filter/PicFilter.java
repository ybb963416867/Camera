package com.example.filter;

import android.content.res.Resources;

/**
 * @author yangbinbing
 * @date 2019/11/7
 * @Description
 */
public class PicFilter extends AFilter{
    public PicFilter(Resources mRes) {
        super(mRes);
    }

    @Override
    protected void onCreate() {
        createProgramByAssetsFile("shader/base_vert.glsl","shader/base_frag.glsl");
    }

    @Override
    protected void onSizeChanged(int width, int height) {

    }
}
