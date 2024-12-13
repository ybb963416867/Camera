package com.example.libffmpeg;

public class FFMpegManager {

    static {
        System.loadLibrary("libffmpeg");
    }


    public static native String getFFmpegVersion();

    public native int startRecord(String url, int width, int height, int fps, int bitrate);
}
