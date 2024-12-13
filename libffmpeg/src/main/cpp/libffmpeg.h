//
// Created by dell on 2024/12/13.
//

#ifndef CAMERA_LIBFFMPEG_H
#define CAMERA_LIBFFMPEG_H

#endif //CAMERA_LIBFFMPEG_H

#include "ffmpeg/logutil.h"

#include <jni.h>

extern "C" {
#include <libavutil/avutil.h>
#include "libavformat/avformat.h"
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_libffmpeg_FFMpegManager_getFFmpegVersion(JNIEnv *env, jclass clazz);

extern "C"
JNIEXPORT jint JNICALL
Java_com_example_libffmpeg_FFMpegManager_startRecord(JNIEnv *env, jobject thiz, jstring url,
                                                     jint width, jint height, jint fps,
                                                     jint bitrate);