package com.example.libffmpeg

class FFmpegRecorder {

    external fun startRecord(outputPath: String, width: Int, height: Int,
                             fps: Int, bitrate: Int): Int
    external fun encodeFrame(rgbaData: ByteArray): Int
    external fun stopRecord(): Int
    external fun getFFmpegVersion(): String
    external fun getSupportedEncoders(): String

    companion object {
        init {
            System.loadLibrary("libffmpeg")
        }
    }
}