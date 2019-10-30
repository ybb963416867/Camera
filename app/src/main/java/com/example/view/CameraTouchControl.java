package com.example.view;

import android.content.Context;
import android.graphics.RectF;
import android.opengl.Matrix;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * @author yangbinbing
 * @date 2019/10/29
 * @Description
 */
public class CameraTouchControl {
    private Context context;
    private DisplayMetrics dm;
    private float mScreenWidth;
    private float mScreenHeight;
    /**
     *
     */
    private View.OnClickListener onClickListener;
    /**
     * 小视频的高度
     */
    private float mThumbnailHeight;
    /**
     * 小视频的宽度
     */
    private float mThumbnailWidth;

    /**
     * 小视频距离屏幕的最小距离
     */
    private int mMargin;

    /**
     * 记录小视频的坐标
     */
    private RectF mThumbnailRect;

    /**
     * 最小滑动的距离
     */
    private int mTouchSlop;

    /**
     * 摁下时手指大的x的坐标
     */
    private float mDownX = 0f;
    /**
     * 摁下时手指的y的坐标
     */
    private float mDownY = 0f;
    /**
     *
     */
    private float mLastYLength = 0f;
    /**
     *
     */
    private float mLastXLength = 0f;

    /**
     * 标识符 判断手指摁下的范围是否在小视频的坐标内
     */
    private boolean mTouchThumbnail = false;
    /**
     * 标识符 判断手指的移动的小视频而不是点击小视频
     */
    private boolean isMoveThumbnail = false;

    public CameraTouchControl(View view) {
        this.context = view.getContext();
        dm = context.getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
        mThumbnailHeight = mScreenHeight / 4;
        mThumbnailWidth = mScreenWidth / 4;
        mMargin = dip2px(context, 2f);
        mThumbnailRect = new RectF(mMargin, mScreenHeight - mMargin, mMargin + mThumbnailWidth, mScreenHeight - mMargin - mThumbnailHeight);
        mTouchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
        init(view);
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    private void init(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mDownX = event.getX();
                        mDownY = event.getY();
                        if (mDownX > mThumbnailRect.left && mDownX < mThumbnailRect.right
                                && mDownY > mThumbnailRect.bottom && mDownY < mThumbnailRect.top) {
                            mTouchThumbnail = true;
                            mLastYLength = 0f;
                            mLastXLength = 0f;
                            return true;
                        } else {
                            mTouchThumbnail = false;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        float moveX = event.getX();
                        float moveY = event.getY();
                        if (mTouchThumbnail) {
                            float lengthX = Math.abs(mDownX - moveX);
                            float lengthY = Math.abs(mDownY - moveY);
                            float length = (float) Math.sqrt(Math.pow((double) lengthX, 2.0) + Math.pow((double) lengthY, 2.0));
                            if (length > mTouchSlop) {
                                moveView(mThumbnailRect, mDownY - moveY, moveX - mDownX);
                                isMoveThumbnail = true;
                            } else {
                                isMoveThumbnail = false;
                            }
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mTouchThumbnail) {
                            mLastYLength = 0f;
                            mLastXLength = 0f;
                            //抬起手指时，如果不是移动小视频，那么就是点击小视频
                            if (!isMoveThumbnail) {
                                if (onClickListener != null) {
                                    onClickListener.onClick(view);
                                }
                            }
                            return true;
                        }
                }
                return false;
            }
        });
    }


    /**
     * 移动小视频
     *
     * @param rectF   小视频的坐标
     * @param lengthY 在Y轴移动的距离
     * @param lengthX 在X轴移动的距离
     */
    public void moveView(RectF rectF, Float lengthY, Float lengthX) {
        rectF.top = rectF.top - (lengthY - mLastYLength);
        rectF.bottom = rectF.bottom - (lengthY - mLastYLength);
        rectF.left = rectF.left + (lengthX - mLastXLength);
        rectF.right = rectF.right + (lengthX - mLastXLength);

        if (rectF.top > mScreenHeight - mMargin) {
            rectF.top = mScreenHeight - mMargin;
            rectF.bottom = rectF.top - mThumbnailHeight;
        }

        if (rectF.bottom < mMargin) {
            rectF.bottom = mMargin * 1f;
            rectF.top = rectF.bottom + mThumbnailHeight;
        }

        if (rectF.right > mScreenWidth - mMargin) {
            rectF.right = mScreenWidth - mMargin;
            rectF.left = rectF.right - mThumbnailWidth;
        }

        if (rectF.left < mMargin) {
            rectF.left = mMargin;
            rectF.right = rectF.left + mThumbnailWidth;
        }

        mLastYLength = lengthY;
        mLastXLength = lengthX;
    }

    public int dip2px(Context context, float dipValue) {
        float scale = dm.density;
        return (int) (dipValue * scale + 0.5f);
    }


    public void calculateMatrix(float[] mMVP) {
        Matrix.setIdentityM(mMVP, 0);
        float scaleX = 1f / 4f;
        float scaleY = 1f / 4f;
        float ratioX = (mThumbnailRect.left - .5f * (1 - scaleX) * mScreenWidth) / mThumbnailRect.width();
        float ratioY = (mThumbnailRect.top - .5f * (1 + scaleY) * mScreenHeight) / mThumbnailRect.height();
        Matrix.scaleM(mMVP, 0, scaleX, scaleY, 0f);
        Matrix.translateM(mMVP, 0, ratioX * 2, ratioY * 2, 0f);
    }
}


















