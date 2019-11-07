package com.example.camera;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;

import com.example.base.BaseActivity;
import com.example.view.PicGLSurface;

public class PicGlSurfaceActivity extends BaseActivity {

    private PicGLSurface picGLSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_gl_surface);
        picGLSurface = findViewById(R.id.pic_surface);
        BitmapDrawable  bitmapDrawable= (BitmapDrawable) getResources().getDrawable(R.mipmap.bg);
        Bitmap bitmap = bitmapDrawable.getBitmap();
        Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        picGLSurface.setBitmap(copy);
    }
}
