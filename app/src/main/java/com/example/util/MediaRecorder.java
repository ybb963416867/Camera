package com.example.util;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;

/**
 * @author yangbinbing
 * @date 2019/11/8
 * @Description
 */
public class MediaRecorder {
    private String TAG = "MediaRecorder";
    private int width;
    private int height;
    /**
     * 码率
     */
    private int bitRate = 1500_000;
    /**
     * 帧率
     */
    private int frameRate = 30;
    /**
     * 关键帧间距 i 帧
     */
    private int iFrameInterval = 5;
    /**
     * 编码器
     */
    private MediaCodec mEncoder;
    /**
     * 输出路径
     */
    private String mOutputPath;
    /**
     * 格式封装器，用来间视频封装成mp4格式
     */
    private MediaMuxer mMuxer;
    private int mTrackIndex;
    /**
     * 封装格式是否开启
     */
    private boolean mMuxerStarted;
    /**
     * 用于视频录制的虚拟屏幕
     */
    private Surface encodeInputSurface;

    public MediaRecorder(int width, int height, String outputPath) {
        this.width = width;
        this.height = height;
        this.mOutputPath = outputPath;
    }

    public Surface getEncodeInputSurface() {
        return encodeInputSurface;
    }

    public void start() {
        MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
        /**
         * 配置MediaCodec 编码器
         */
        //视频格式
        // 类型（avc高级编码 h264） 编码出的宽、高
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
        //参数配置
        // 1500kbs码率
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        //设置帧率
        format.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        //设置码率
        format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
        //设置关键帧间距
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval);
        Log.d(TAG, "format: " + format);
        try {
            mEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
//        mInputSurface = new CodecInputSurface(mEncoder.createInputSurface());
        //交给虚拟屏幕 通过opengl 将预览的纹理 绘制到这一个虚拟屏幕中
        //这样MediaCodec 就会自动编码 inputSurface 中的图像
        encodeInputSurface = mEncoder.createInputSurface();
        mEncoder.start();
        //  H.264
        // 播放：
        //  MP4 -> 解复用 (解封装) -> 解码 -> 绘制
        //封装器 复用器
        // 一个 mp4 的封装器 将h.264 通过它写出到文件就可以了
        try {
            mMuxer = new MediaMuxer(mOutputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException ioe) {
            throw new RuntimeException("MediaMuxer creation failed", ioe);
        }

        mTrackIndex = -1;
        mMuxerStarted = false;
    }
}
