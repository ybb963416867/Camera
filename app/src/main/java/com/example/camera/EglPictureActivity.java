package com.example.camera;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.base.BaseActivity;
import com.example.egl.GLES20Env;
import com.example.filter.GrayFilter;

public class EglPictureActivity extends BaseActivity {

    private ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_egl_picture);
        iv = findViewById(R.id.iv);
        BitmapDrawable drawable = (BitmapDrawable) getResources().getDrawable(R.mipmap.bg);
        Bitmap bmp = drawable.getBitmap();
        int mBmpWidth = bmp.getWidth();
        int mBmpHeight = bmp.getHeight();
        GLES20Env mBackEnv = new GLES20Env(mBmpWidth, mBmpHeight);
        mBackEnv.setThreadOwner(getMainLooper().getThread().getName());
        mBackEnv.setFilter(new GrayFilter(getResources()));
        mBackEnv.setInput(bmp);
        iv.setImageBitmap(mBackEnv.getBitmap());
//        saveBitmap(mBackEnv.getBitmap());
    }
}
