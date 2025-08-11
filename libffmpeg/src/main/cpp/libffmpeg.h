//
// Created by dell on 2024/12/13.
//

#ifndef CAMERA_LIBFFMPEG_H
#define CAMERA_LIBFFMPEG_H

#endif //CAMERA_LIBFFMPEG_H

#include "ffmpeg/logutil.h"

#include <jni.h>
#include <string>

extern "C" {
#include <libavutil/avutil.h>
#include "libavformat/avformat.h"
#include "libavcodec/avcodec.h"
#include "libswscale/swscale.h"
#include "libavutil/imgutils.h"
}

struct RecorderContext {
    AVFormatContext* formatCtx = nullptr;
    AVCodecContext* codecCtx = nullptr;
    AVStream* videoStream = nullptr;
    AVFrame* frame = nullptr;
    AVFrame* rgbaFrame = nullptr;
    AVPacket* packet = nullptr;
    SwsContext* swsCtx = nullptr;

    int width = 0;
    int height = 0;
    int frameCount = 0;
    bool isRecording = false;

    ~RecorderContext() {
        cleanup();
    }

    void cleanup() {
        if (swsCtx) {
            sws_freeContext(swsCtx);
            swsCtx = nullptr;
        }

        if (frame) {
            av_frame_free(&frame);
        }

        if (rgbaFrame) {
            av_frame_free(&rgbaFrame);
        }

        if (packet) {
            av_packet_free(&packet);
        }

        if (codecCtx) {
            avcodec_free_context(&codecCtx);
        }

        if (formatCtx) {
            if (isRecording) {
                av_write_trailer(formatCtx);
            }
            avio_closep(&formatCtx->pb);
            avformat_free_context(formatCtx);
        }
    }
};

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_libffmpeg_FFmpegRecorder_startRecord(JNIEnv *env, jobject thiz,
                                                      jstring output_path, jint width, jint height,
                                                      jint fps, jint bitrate);

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_libffmpeg_FFmpegRecorder_encodeFrame(JNIEnv *env, jobject thiz,
                                                      jbyteArray rgba_data);

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_libffmpeg_FFmpegRecorder_getFFmpegVersion(JNIEnv *env, jobject thiz);

// 新增：获取支持的编码器列表
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_libffmpeg_FFmpegRecorder_getSupportedEncoders(JNIEnv *env, jobject thiz);