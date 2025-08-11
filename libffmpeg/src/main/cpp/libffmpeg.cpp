#include "libffmpeg.h"

static RecorderContext* g_recorder = nullptr;

// 添加错误信息获取函数
static void logAVError(const char* operation, int error_code) {
    char error_buf[AV_ERROR_MAX_STRING_SIZE];
    av_strerror(error_code, error_buf, AV_ERROR_MAX_STRING_SIZE);
    LOGE("%s failed with error: %s (code: %d)", operation, error_buf, error_code);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_libffmpeg_FFmpegRecorder_startRecord(JNIEnv *env, jobject thiz,
                                                      jstring output_path, jint width, jint height,
                                                      jint fps, jint bitrate) {
    if (g_recorder) {
        delete g_recorder;
    }
    g_recorder = new RecorderContext();

    // 强制分辨率偶数
    if (width % 2 != 0) width--;
    if (height % 2 != 0) height--;

    g_recorder->width = width;
    g_recorder->height = height;

    const char* outputPath = env->GetStringUTFChars(output_path, nullptr);
    LOGI("Starting record: %s, %dx%d, %d fps, %d bitrate", outputPath, width, height, fps, bitrate);

    int ret = avformat_alloc_output_context2(&g_recorder->formatCtx, nullptr, nullptr, outputPath);
    if (ret < 0) {
        logAVError("avformat_alloc_output_context2", ret);
        env->ReleaseStringUTFChars(output_path, outputPath);
        return -1;

    }

    // 只用 libx264
    const AVCodec* codec = avcodec_find_encoder_by_name("libx264");
    if (!codec) {
        LOGE("libx264 encoder not found. Make sure FFmpeg is built with --enable-gpl --enable-libx264");
        env->ReleaseStringUTFChars(output_path, outputPath);
        return -2;
    }
    LOGI("Using encoder: %s", codec->name);

    g_recorder->videoStream = avformat_new_stream(g_recorder->formatCtx, nullptr);
    if (!g_recorder->videoStream) {
        LOGE("Could not create video stream");
        env->ReleaseStringUTFChars(output_path, outputPath);
        return -3;
    }

    g_recorder->codecCtx = avcodec_alloc_context3(codec);
    if (!g_recorder->codecCtx) {
        LOGE("Could not allocate codec context");
        env->ReleaseStringUTFChars(output_path, outputPath);
        return -4;
    }

    // 基本参数
    g_recorder->codecCtx->codec_id = codec->id;
    g_recorder->codecCtx->width = width;
    g_recorder->codecCtx->height = height;
    g_recorder->codecCtx->time_base = av_make_q(1, fps);
    g_recorder->codecCtx->framerate = av_make_q(fps, 1);
    g_recorder->codecCtx->bit_rate = bitrate > 0 ? bitrate : 2000000; // 默认 2Mbps
    g_recorder->codecCtx->gop_size = 30;
    g_recorder->codecCtx->max_b_frames = 0;
    g_recorder->codecCtx->pix_fmt = AV_PIX_FMT_YUV420P;

    // 如果 MP4 需要全局头
    if (g_recorder->formatCtx->oformat->flags & AVFMT_GLOBALHEADER) {
        g_recorder->codecCtx->flags |= AV_CODEC_FLAG_GLOBAL_HEADER;
    }

    // 编码器参数
    AVDictionary* opts = nullptr;
    av_dict_set(&opts, "preset", "ultrafast", 0);
    av_dict_set(&opts, "profile", "baseline", 0);
    av_dict_set(&opts, "tune", "zerolatency", 0);
    av_dict_set(&opts, "movflags", "+faststart", 0);

    ret = avcodec_open2(g_recorder->codecCtx, codec, &opts);
    av_dict_free(&opts);
    if (ret < 0) {
        logAVError("avcodec_open2", ret);
        env->ReleaseStringUTFChars(output_path, outputPath);
        return -5;
    }

    // 复制参数到流
    ret = avcodec_parameters_from_context(g_recorder->videoStream->codecpar, g_recorder->codecCtx);
    if (ret < 0) {
        logAVError("avcodec_parameters_from_context", ret);
        env->ReleaseStringUTFChars(output_path, outputPath);
        return -6;
    }

    // 分配帧
    g_recorder->frame = av_frame_alloc();
    g_recorder->rgbaFrame = av_frame_alloc();
    if (!g_recorder->frame || !g_recorder->rgbaFrame) {
        LOGE("Could not allocate frames");
        env->ReleaseStringUTFChars(output_path, outputPath);
        return -7;
    }

    g_recorder->frame->format = g_recorder->codecCtx->pix_fmt;
    g_recorder->frame->width = g_recorder->codecCtx->width;
    g_recorder->frame->height = g_recorder->codecCtx->height;
    if (av_frame_get_buffer(g_recorder->frame, 0) < 0) {
        logAVError("av_frame_get_buffer", ret);
        env->ReleaseStringUTFChars(output_path, outputPath);
        return -8;
    }

    // RGBA frame
    g_recorder->rgbaFrame->format = AV_PIX_FMT_RGBA;
    g_recorder->rgbaFrame->width = width;
    g_recorder->rgbaFrame->height = height;

    // 转换上下文
    g_recorder->swsCtx = sws_getContext(
            width, height, AV_PIX_FMT_RGBA,
            g_recorder->codecCtx->width, g_recorder->codecCtx->height, g_recorder->codecCtx->pix_fmt,
            SWS_FAST_BILINEAR, nullptr, nullptr, nullptr
    );
    if (!g_recorder->swsCtx) {
        LOGE("Could not create sws context");
        env->ReleaseStringUTFChars(output_path, outputPath);
        return -9;
    }

    g_recorder->packet = av_packet_alloc();
    if (!g_recorder->packet) {
        LOGE("Could not allocate packet");
        env->ReleaseStringUTFChars(output_path, outputPath);
        return -10;
    }

    // 打开文件
    if (!(g_recorder->formatCtx->oformat->flags & AVFMT_NOFILE)) {
        ret = avio_open(&g_recorder->formatCtx->pb, outputPath, AVIO_FLAG_WRITE);
        if (ret < 0) {
            logAVError("avio_open", ret);
            env->ReleaseStringUTFChars(output_path, outputPath);
            return -11;
        }
    }

    ret = avformat_write_header(g_recorder->formatCtx, nullptr);
    if (ret < 0) {
        logAVError("avformat_write_header", ret);
        env->ReleaseStringUTFChars(output_path, outputPath);
        return -12;
    }

    g_recorder->isRecording = true;
    g_recorder->frameCount = 0;
    env->ReleaseStringUTFChars(output_path, outputPath);

    LOGI("Recording started successfully with libx264");
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_libffmpeg_FFmpegRecorder_encodeFrame(JNIEnv *env, jobject thiz,
                                                      jbyteArray rgba_data) {

    if (!g_recorder || !g_recorder->isRecording) {
        return -1;
    }

    jsize arrayLength = env->GetArrayLength(rgba_data);
    jbyte* rgbaBytes = env->GetByteArrayElements(rgba_data, nullptr);
    if (!rgbaBytes) {
        return -2;
    }

    // 验证数据大小
    int expectedSize = g_recorder->width * g_recorder->height * 4;
    if (arrayLength != expectedSize) {
        LOGE("Invalid data size: expected %d, got %d", expectedSize, arrayLength);
        env->ReleaseByteArrayElements(rgba_data, rgbaBytes, JNI_ABORT);
        return -3;
    }

    // 确保帧数据可写
    int ret = av_frame_make_writable(g_recorder->frame);
    if (ret < 0) {
        logAVError("av_frame_make_writable", ret);
        env->ReleaseByteArrayElements(rgba_data, rgbaBytes, JNI_ABORT);
        return -4;
    }

    // 设置RGBA数据
    int rgbaLinesize[4] = {g_recorder->width * 4, 0, 0, 0};
    uint8_t* rgbaData[4] = {(uint8_t*)rgbaBytes, nullptr, nullptr, nullptr};

    // 颜色转换 RGBA -> 编码器要求的格式
    ret = sws_scale(g_recorder->swsCtx,
                    rgbaData, rgbaLinesize, 0, g_recorder->height,
                    g_recorder->frame->data, g_recorder->frame->linesize);

    if (ret != g_recorder->height) {
        LOGE("sws_scale failed: expected %d, got %d", g_recorder->height, ret);
        env->ReleaseByteArrayElements(rgba_data, rgbaBytes, JNI_ABORT);
        return -5;
    }

    // 设置帧时间戳
    g_recorder->frame->pts = g_recorder->frameCount++;

    // 编码帧
    ret = avcodec_send_frame(g_recorder->codecCtx, g_recorder->frame);
    if (ret < 0) {
        logAVError("avcodec_send_frame", ret);
        env->ReleaseByteArrayElements(rgba_data, rgbaBytes, JNI_ABORT);
        return -6;
    }

    // 获取编码后的包
    while (ret >= 0) {
        ret = avcodec_receive_packet(g_recorder->codecCtx, g_recorder->packet);
        if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
            break;
        } else if (ret < 0) {
            logAVError("avcodec_receive_packet", ret);
            break;
        }

        // 设置时间基
        av_packet_rescale_ts(g_recorder->packet, g_recorder->codecCtx->time_base,
                             g_recorder->videoStream->time_base);
        g_recorder->packet->stream_index = g_recorder->videoStream->index;

        // 写入文件
        ret = av_interleaved_write_frame(g_recorder->formatCtx, g_recorder->packet);
        if (ret < 0) {
            logAVError("av_interleaved_write_frame", ret);
        }

        av_packet_unref(g_recorder->packet);
    }

    env->ReleaseByteArrayElements(rgba_data, rgbaBytes, JNI_ABORT);
    return 0;
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_libffmpeg_FFmpegRecorder_stopRecord(JNIEnv *env, jobject thiz) {
    if (!g_recorder) {
        return -1;
    }

    LOGI("Stopping recording, total frames: %d", g_recorder->frameCount);

    // 刷新编码器
    if (g_recorder->isRecording && g_recorder->codecCtx) {
        int ret = avcodec_send_frame(g_recorder->codecCtx, nullptr);
        if (ret < 0) {
            logAVError("avcodec_send_frame (flush)", ret);
        }

        while (ret >= 0) {
            ret = avcodec_receive_packet(g_recorder->codecCtx, g_recorder->packet);
            if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF) {
                break;
            } else if (ret < 0) {
                logAVError("avcodec_receive_packet (flush)", ret);
                break;
            }

            av_packet_rescale_ts(g_recorder->packet, g_recorder->codecCtx->time_base,
                                 g_recorder->videoStream->time_base);
            g_recorder->packet->stream_index = g_recorder->videoStream->index;

            ret = av_interleaved_write_frame(g_recorder->formatCtx, g_recorder->packet);
            if (ret < 0) {
                logAVError("av_interleaved_write_frame (flush)", ret);
            }

            av_packet_unref(g_recorder->packet);
        }

        // 写文件尾
        ret = av_write_trailer(g_recorder->formatCtx);
        if (ret < 0) {
            logAVError("av_write_trailer", ret);
        }
    }

    g_recorder->isRecording = false;
    delete g_recorder;
    g_recorder = nullptr;

    LOGI("Recording stopped successfully");
    return 0;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_libffmpeg_FFmpegRecorder_getFFmpegVersion(JNIEnv *env, jobject thiz) {
    const char *version = av_version_info();
    return env->NewStringUTF(version);
}

// 新增：获取支持的编码器列表
extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_libffmpeg_FFmpegRecorder_getSupportedEncoders(JNIEnv *env, jobject thiz) {
    std::string encoders = "Supported H264 encoders:\n";

    const AVCodec* codec = nullptr;
    void* iter = nullptr;

    while ((codec = av_codec_iterate(&iter))) {
        if (codec->type == AVMEDIA_TYPE_VIDEO &&
            (codec->id == AV_CODEC_ID_H264) &&
            av_codec_is_encoder(codec)) {
            encoders += std::string(codec->name) + "\n";
        }
    }

    return env->NewStringUTF(encoders.c_str());
}