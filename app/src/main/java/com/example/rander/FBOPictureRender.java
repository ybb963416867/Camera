package com.example.rander;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.util.Log;

import com.example.filter.GrayFilter;
import com.example.filter.PicFilter;
import com.example.util.Gl2Utils;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @author yangbinbing
 * @date 2019/11/6
 * @Description
 */
public class FBOPictureRender implements GLSurfaceView.Renderer {


    private Bitmap mBitmap;
    private final GrayFilter grayFilter;
    private final PicFilter picFilter;
    private int[] fFrame = new int[1];
    private int[] fRender = new int[1];
    private int[] fTexture = new int[2];
    private ByteBuffer mBuffer;
    private FBOPictureRender.PicCallBack callBack;
    private int width;
    private int height;

    public void setCallBack(PicCallBack callBack) {
        this.callBack = callBack;
    }

    /**
     * @param resources
     */
    public FBOPictureRender(Resources resources) {
        grayFilter = new GrayFilter(resources);
        picFilter = new PicFilter(resources);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        grayFilter.create();
        grayFilter.setMatrix(Gl2Utils.flip(Gl2Utils.getOriginalMatrix(), false, true));
        picFilter.create();
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        Gl2Utils.getPicOriginMatrix(picFilter.getMatrix(), mBitmap.getWidth(), mBitmap.getHeight(), width, height, 6);

        this.width = width;
        this.height = height;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        if (mBitmap != null && !mBitmap.isRecycled()) {
            Log.e("ybb", "onDrawFrame");
            createTexture();
            //将一个2D纹理的某个mip级别或者立方图面连接到帧缓存区附着点
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, fTexture[1], 0);
            //间一个渲染缓存区对象连接在帧缓存区附着点上
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, fRender[0]);
            GLES20.glViewport(0, 0, mBitmap.getWidth(), mBitmap.getHeight());
            grayFilter.setTextureId(fTexture[0]);
            grayFilter.draw();
            mBuffer = ByteBuffer.allocate(mBitmap.getWidth() * mBitmap.getHeight() * 4);
            GLES20.glReadPixels(0, 0, mBitmap.getWidth(), mBitmap.getHeight(), GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, mBuffer);
            if (callBack != null) {
                callBack.callBack(mBuffer);
            }

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            GLES20.glBindTexture(GLES30.GL_TEXTURE_2D, 0);

            GLES20.glViewport(0, 0, width, height);
            //处理后的纹理，也就是黑白图的纹理
            picFilter.setTextureId(fTexture[1]);
            //没有处理的纹理，也就是原始图像
//            picFilter.setTextureId(fTexture[0]);
            picFilter.draw();
//            destroy();
        }


    }

    public void destroy() {
        GLES20.glDeleteTextures(2, fTexture, 0);
        GLES20.glDeleteFramebuffers(1, fFrame, 0);
        GLES20.glDeleteRenderbuffers(1, fRender, 0);
        if (mBitmap != null) {
            mBitmap.recycle();
        }
    }

    private void createTexture() {

        //创建纹理，一个纹理用于绘制，一个纹理用于fbo
        //创建一个帧缓存区对象
        GLES20.glGenFramebuffers(1, fFrame, 0);
        //将该帧缓存区绑定对象绑定到管线上面
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fFrame[0]);
        //创建一个渲染缓存区对象用于fbo
        GLES20.glGenRenderbuffers(1, fRender, 0);
        //将渲染缓存区绑定到管线上
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, fRender[0]);
        //指定保存渲染缓存区的大小和格式
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, mBitmap.getWidth(), mBitmap.getHeight());

        //创建纹理
        GLES20.glGenTextures(2, fTexture, 0);

        for (int i = 0; i < 2; i++) {
            //间纹理绑定到管线上
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, fTexture[i]);
            if (i == 0) {
                //间bitmap绑定到纹理上面
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mBitmap, 0);
            } else {
                /**
                 * target ---- 是常数GL_TEXTURE_2D。
                 * level ---- 表示多级分辨率的纹理图像的级数，若只有一种分辨率，则level设为0。
                 * components ---- 是一个从1到4的整数，指出选择了R、G、B、A中的哪些分量用于调整和混合，1表示选择了R分量，2表示选择了R和A两个分量，3表示选择了R、G、B三个分量，4表示选择了R、G、B、A四个分量。
                 * width和height ---- 给出了纹理图像的长度和宽度，参数border为纹理边界宽度，它通常为0，width和height必须是2m+2b，这里m是整数，长和宽可以有不同的值，b是border的值。纹理映射的最大尺寸依赖于OpenGL，但它至少必须是使用64x64（若带边界为66x66），若width和height设置为0，则纹理映射有效地关闭。
                 * format和type ---- 描述了纹理映射的格式和数据类型，它们在这里的意义与在函数glDrawPixels()中的意义相同，事实上，纹理数据与glDrawPixels()所用的数据有同样的格式。参数format可以是GL_COLOR_INDEX、GL_RGB、GL_RGBA、GL_RED、GL_GREEN、GL_BLUE、GL_ALPHA、GL_LUMINANCE或GL_LUMINANCE_ALPHA（注意：不能用GL_STENCIL_INDEX和GL_DEPTH_COMPONENT）。类似地，参数type是GL_BYPE、GL_UNSIGNED_BYTE、GL_SHORT、 GL_UNSIGNED_SHORT、GL_INT、GL_UNSIGNED_INT、GL_FLOAT或GL_BITMAP。
                 * 　pixels ---- 包含了纹理图像数据，这个数据描述了纹理图像本身和它的边界。
                 * 指定fbo帧缓存中的数据的宽高
                 */
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mBitmap.getWidth(), mBitmap.getHeight(), 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mBitmap, 0);
            }
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        }

    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public interface PicCallBack {
        void callBack(ByteBuffer byteBuffer);
    }
}
























