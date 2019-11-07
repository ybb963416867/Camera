package com.example.camera;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.example.base.BaseActivity;
import com.example.view.FBOPictureGLSurface;

public class FboActivity extends BaseActivity {

    private ImageView iv;
    private FBOPictureGLSurface pictureGLSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fbo);
        iv = findViewById(R.id.iv);
        pictureGLSurface = findViewById(R.id.pgl);
        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.mipmap.bg);
        Bitmap bitmap = drawable.getBitmap();
        Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        pictureGLSurface.setBitmap(copy);
        pictureGLSurface.setPicCallBack(byteBuffer -> {
            Bitmap bitmap1 = Bitmap.createBitmap(copy.getWidth(), copy.getHeight(), Bitmap.Config.ARGB_8888);
            bitmap1.copyPixelsFromBuffer(byteBuffer);
            Log.e("ybb", "width:" + bitmap1.getWidth() + "height" + bitmap1.getHeight() + "");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e("ybb", "运行了");
                    iv.setImageBitmap(bitmap1);
                }
            });
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
