package com.example.util;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaCrypto;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;

import androidx.annotation.RequiresApi;

/**
 * ********************************
 * 项目名称：
 *
 * @Author yangbinbing
 * 邮箱： 963416867@qq.com
 * 创建时间：  22:05
 * 用途 MediaCodec 的一个工具类
 * ********************************
 */
public class MediaCodecUtils {

    private static final String TAG = "MediaCodecUtils";
    private static final String VIDEO_TAG = "video/";
    private static final String AUDIO_TAG = "audio/";

    /**
     * @param mimeType 支持的类型
     * @return 返回MediaCodec支持的一些编码格式的信息
     */
    public static MediaCodecInfo selectCodec(String mimeType) {
        //拿到所有MediaCodecList支持的所有码的总和
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            Log.d(TAG, "codeName:" + codecInfo.getName());
            //如果不是编码器，即为解码器，这里需要编码器所以继续
            if (!codecInfo.isEncoder()) {
                continue;
            }
            //解码的类型，比如video/avc这个是h264编码
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                Log.d(TAG, "type:" + types[j]);
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    /**
     * Create muxer media muxer. 创建一个复用器
     *
     * @param outPath the out path 输出路径
     * @param type    the type  支持特定的类型
     * @return the media muxer 封装器
     * @throws IOException the io exception  异常
     */
    public static MediaMuxer createMuxer(String outPath, int type) throws IOException {
        return new MediaMuxer(outPath, type);
    }


    /**
     * @param extractor 视频媒体提取器
     * @return 媒体的轨道
     */
    public static int getAndSelectVideoTrackIndex(MediaExtractor extractor) {
        for (int index = 0; index < extractor.getTrackCount(); ++index) {
            Log.d(TAG, "format for track" + index + " is " + (extractor.getTrackFormat(index)).getString(MediaFormat.KEY_MIME));
            if (extractor.getTrackFormat(index).getString(MediaFormat.KEY_MIME).startsWith(VIDEO_TAG)) {
                extractor.selectTrack(index);
                return index;
            }
        }

        return -1;
    }

    /**
     * @param extractor 音频媒体提取器
     * @return 媒体的轨道
     */
    public static int getAndSelectAudioTrackIndex(MediaExtractor extractor) {
        for (int index = 0; index < extractor.getTrackCount(); ++index) {
            Log.d(TAG, "format for track" + index + " is " + (extractor.getTrackFormat(index)).getString(MediaFormat.KEY_MIME));
            if (extractor.getTrackFormat(index).getString(MediaFormat.KEY_MIME).startsWith(AUDIO_TAG)) {
                extractor.selectTrack(index);
                return index;
            }
        }

        return -1;
    }

    /**
     * 创建媒体格式
     *
     * @param width          视频宽
     * @param height         视频高
     * @param bitRate        码率
     * @param frameRate      帧率
     * @param iFrameInterval 关键帧间隔
     * @return 媒体格式
     */
    public static MediaFormat createMediaFormat(int width, int height, int bitRate, int frameRate, int iFrameInterval) {
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
        return format;
    }

    /**
     * @param mime         格式类型
     * @param simpleRate   音频采样率
     * @param channelCount 音道数
     * @param bitRate      比特率
     * @return
     */
    public static MediaFormat createAudioFormat(String mime, int simpleRate, int channelCount, int bitRate) {
        MediaFormat audioFormat = MediaFormat.createAudioFormat(mime, simpleRate, channelCount);
        audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitRate);
        //AAC的配置文件 仅限于AAC
        audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectHE);
        return audioFormat;
    }

    /**
     * @param mediaCodecInfo 支持的媒体编码格式信息
     * @param mediaFormat    编码格式 里面包含的关键帧时间间隔，以及征率码率，等
     * @return MediaCodec
     * @throws IOException
     */
    public static MediaCodec createMediaCodec(MediaCodecInfo mediaCodecInfo, MediaFormat mediaFormat) throws IOException {
        MediaCodec mediaCodec = MediaCodec.createByCodecName(mediaCodecInfo.getName());
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        return mediaCodec;
    }

    /**
     * @param inputFormat
     * @param surface
     * @param mediaCrypto
     * @param flag
     * @return
     * @throws IOException
     */
    public static MediaCodec createAudioDecoder(MediaFormat inputFormat, Surface surface, MediaCrypto mediaCrypto, int flag) throws IOException {
        MediaCodec decoderByType = MediaCodec.createDecoderByType(inputFormat.getString(MediaFormat.KEY_MIME));
        decoderByType.configure(inputFormat, surface, mediaCrypto, flag);
        return decoderByType;
    }


}
