package com.example.util;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

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
    private MediaCodec.BufferInfo mBufferInfo;

    public MediaRecorder(int width, int height, String outputPath) {
        this.width = width;
        this.height = height;
        this.mOutputPath = outputPath;
    }

    public Surface getEncodeInputSurface() {
        return encodeInputSurface;
    }

    public void start() {
        mBufferInfo = new MediaCodec.BufferInfo();
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


    /**
     * @param endOfStream 是否结束
     * @des 编码成MP4
     */
    public void drainEncoder(boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;
        Log.d(TAG, "drainEncoder(" + endOfStream + ")");

        if (endOfStream) {
            Log.d(TAG, "sending EOS to encoder");
            mEncoder.signalEndOfInputStream();
        }

        ByteBuffer[] encoderOutputBuffers = mEncoder.getOutputBuffers();
        while (true) {
            int encoderStatus = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
            //again later
            if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                // no output available yet
                if (!endOfStream) {
                    break;      // out of while
                } else {
                    Log.d(TAG, "no output available, spinning to await EOS");
                }
                //out buffer hanged
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                // not expected for an encoder
                encoderOutputBuffers = mEncoder.getOutputBuffers();
                //format changed
            } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                // should happen before receiving buffers, and should only happen once
                if (mMuxerStarted) {
                    throw new RuntimeException("format changed twice");
                }
                MediaFormat newFormat = mEncoder.getOutputFormat();
                Log.d(TAG, "encoder output format changed: " + newFormat);

                // now that we have the Magic Goodies, start the muxer
                mTrackIndex = mMuxer.addTrack(newFormat);
                mMuxer.start();
                mMuxerStarted = true;
            } else if (encoderStatus < 0) {
                Log.w(TAG, "unexpected result from encoder.dequeueOutputBuffer: " +
                        encoderStatus);
                // let's ignore it
            } else {
                ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                if (encodedData == null) {
                    throw new RuntimeException("encoderOutputBuffer " + encoderStatus +
                            " was null");
                }
                //这个flag 只能为1和4   1的时候是正常的，运行下面的  如果为4的时候是流结束将会走这个方法
                //这里判断flag为4 的情况，代表这个流结束了，所以不需要。
                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    // The codec config data was pulled out and fed to the muxer when we got
                    // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                    Log.d(TAG, "ignoring BUFFER_FLAG_CODEC_CONFIG");
                    mBufferInfo.size = 0;
                }

                if (mBufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer hasn't started");
                    }

                    // adjust the ByteBuffer values to match BufferInfo (not needed?)
                    encodedData.position(mBufferInfo.offset);
                    encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

                    mMuxer.writeSampleData(mTrackIndex, encodedData, mBufferInfo);
                    Log.d(TAG, "sent " + mBufferInfo.size + " bytes to muxer");
                }

                mEncoder.releaseOutputBuffer(encoderStatus, false);

                if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!endOfStream) {
                        Log.w(TAG, "reached end of stream unexpectedly");
                    } else {
                        Log.d(TAG, "end of stream reached");
                    }
                    break;      // out of while
                }
            }
        }
    }


    public void releaseEncoder() {
        Log.d(TAG, "releasing encoder objects");
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
        if (mMuxer != null) {
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }
    }
}
