#include "libffmpeg.h"

AVFormatContext *format_ctx = nullptr;
const AVCodec *codec = nullptr;
AVStream *videoStream = nullptr;

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_libffmpeg_FFMpegManager_getFFmpegVersion(JNIEnv *env, jclass clazz) {
    const char *version = av_version_info();
    return env->NewStringUTF(version);
}


extern "C"
JNIEXPORT jint JNICALL
Java_com_example_libffmpeg_FFMpegManager_startRecord(JNIEnv *env, jobject thiz, jstring url,
                                                     jint width, jint height, jint fps,
                                                     jint bitrate) {
    const char *outputFilename = env->GetStringUTFChars(url, JNI_FALSE);
    int videoWidth = width;
    int videoHeight = height;
    int videoFps = fps;
    int videoBitrate = bitrate;
    LOGD("outputFilename = %s videoWidth = %d videoHeight = %d videoFps = %d videoBitrate = %d",
         outputFilename, videoWidth, videoHeight, videoFps, videoBitrate);
    if (avformat_alloc_output_context2(&format_ctx, nullptr, "mp4", outputFilename) < 0) {
        LOGD("avformat_alloc_output_context2 error");
        return -1;
    }

    LOGD("avformat_alloc_output_context2 success");

    codec = avcodec_find_decoder(AV_CODEC_ID_H264);
    if (!codec) {
        LOGD("codec no found");
        return -1;
    }

    LOGD("codec found");

    videoStream = avformat_new_stream(format_ctx, codec);
    if (!videoStream){
        LOGD("create videoStream failed");
        return -1;
    }
    LOGD("create videoStream success");


    return 0;
}