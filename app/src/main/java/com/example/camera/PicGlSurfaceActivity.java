package com.example.camera;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLES20;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.example.base.BaseActivity;
import com.example.view.PicGLSurface;

import java.nio.IntBuffer;

public class PicGlSurfaceActivity extends BaseActivity {

    private PicGLSurface picGLSurface;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pic_gl_surface);
        picGLSurface = findViewById(R.id.pic_surface);
        imageView = findViewById(R.id.iv);
        BitmapDrawable  bitmapDrawable= (BitmapDrawable) getResources().getDrawable(R.mipmap.photo);
        Bitmap bitmap = bitmapDrawable.getBitmap();
        Bitmap copy = bitmap.copy(Bitmap.Config.ARGB_8888, false);
        picGLSurface.setBitmap(copy);

//        imageView.setImageBitmap(convertToBitmap(copy));
    }


    private Bitmap convertToBitmap(Bitmap copy) {
        int[] iat = new int[copy.getWidth()* copy.getHeight()];
        IntBuffer ib = IntBuffer.allocate(copy.getWidth() * copy.getHeight());
        GLES20.glReadPixels(0, 0, copy.getWidth(), copy.getHeight(), GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                ib);
        int[] ia = ib.array();

        // Convert upside down mirror-reversed image to right-side up normal
        // image.
        //ia数据里面的的图片与原始图片是左右和上下倒立的，是原图片的镜像
        for (int i = 0; i < copy.getHeight(); i++) {
            System.arraycopy(ia, i * copy.getWidth(), iat, (copy.getHeight() - 1 - i) * copy.getWidth(), copy.getWidth());
        }
        Bitmap bitmap = Bitmap.createBitmap(copy.getWidth(), copy.getHeight(), Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(IntBuffer.wrap(iat));
        return bitmap;
    }
}
