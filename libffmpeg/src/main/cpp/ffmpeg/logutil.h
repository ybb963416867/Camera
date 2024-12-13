//
// Created by glumes on 2018/3/7.
//

#include <android/log.h>

#ifndef ANDROIDCPPSOLIB_LOGUTIL_H
#define ANDROIDCPPSOLIB_LOGUTIL_H

const bool LOG_FLAG = false;
#define LOG_TAG "_YUN_SONIC_"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#endif //ANDROIDCPPSOLIB_LOGUTIL_H


#define LOGD_ENABLED 0
#if LOGD_ENABLED
#define PSLOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#else
#define PSLOGD(...) ((void)0) // 如果日志被禁用，则不执行任何操作
#endif