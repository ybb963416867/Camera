#include "libffmpeg.h"

AVFormatContext *format_ctx = nullptr;
const AVCodec *codec = nullptr;
AVStream *videoStream = nullptr;
AVCodecContext *codec_ctx = nullptr;
AVFrame *frame = nullptr;
SwsContext *sws_ctx = nullptr;
int is_recording = 0;
AVPacket pkt;

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
    if (!videoStream) {
        LOGD("create videoStream failed");
        return -1;
    }
    LOGD("create videoStream success");

    codec_ctx = avcodec_alloc_context3(codec);
    codec_ctx->bit_rate = 400000;
    codec_ctx->width = videoWidth;
    codec_ctx->height = videoHeight;
    codec_ctx->time_base = (AVRational) {1, 30};
    codec_ctx->framerate = (AVRational) {30, 1};
    codec_ctx->gop_size = 10;
    codec_ctx->max_b_frames = 1;
    codec_ctx->pix_fmt = AV_PIX_FMT_YUV420P;

    // 将编码器参数添加到视频流
    if (avcodec_parameters_from_context(videoStream->codecpar, codec_ctx) < 0) {
        LOGD("Failed to copy codec parameters to stream");
        return -1;
    }

    LOGD("Success to copy codec parameters to stream");

    // 打开输出文件
    if (!(format_ctx->oformat->flags & AVFMT_NOFILE)) {
        if (avio_open(&format_ctx->pb, outputFilename, AVIO_FLAG_WRITE) < 0) {
            LOGE("Could not open output file '%s'\n", outputFilename);
            return -1;
        }
    }
    LOGD("open output file '%s'\n", outputFilename);


    // 写文件头
    if (avformat_write_header(format_ctx, nullptr) < 0) {
        LOGE("Error occurred when writing header");
        return -1;
    }

    LOGD("Success occurred when writing header");

    // 分配帧并初始化转换上下文
    frame = av_frame_alloc();
    if (!frame) {
        LOGE("Could not allocate video frame");
        return -1;
    }

    LOGD("allocate video frame success");

    frame->format = AV_PIX_FMT_RGBA;
    frame->width = videoWidth;
    frame->height = videoHeight;

    if (av_frame_get_buffer(frame, 32) < 0) {
        LOGD("Could not allocate the video frame data");
        return -1;
    }

    LOGD("allocate the video frame data success");

    sws_ctx = sws_getContext(videoWidth, videoHeight, AV_PIX_FMT_RGBA, videoWidth, videoHeight, AV_PIX_FMT_YUV420P,
                             SWS_BICUBIC, nullptr, nullptr, nullptr);

    is_recording = 1;

    return 0;
}


// 将 RGBA 数据写入文件
int write_frame(const uint8_t *rgba_data, int width, int height) {
    if (!is_recording) return -1;

    // 转换 RGBA 到 YUV420P
    const uint8_t *in_data[1] = { rgba_data };
    int in_linesize[1] = { 4 * width }; // RGBA 每像素 4 字节

    // 使用 libswscale 转换图像格式
    sws_scale(sws_ctx, in_data, in_linesize, 0, height, frame->data, frame->linesize);

    frame->pts++; // 增加时间戳

    // 编码帧并写入数据包
    if (avcodec_send_frame(codec_ctx, frame) < 0) {
        fprintf(stderr, "Error sending frame to encoder\n");
        return -1;
    }

    if (avcodec_receive_packet(codec_ctx, &pkt) == 0) {
        av_interleaved_write_frame(format_ctx, &pkt);
        av_packet_unref(&pkt);
    }
    return 0;
}
