package com.example.camera;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.InputDevice;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;

import com.example.manager.EGLHelper;
import com.example.manager.InputSurface;
import com.example.util.FileUtils;
import com.example.util.MediaCodecUtils;
import com.example.util.PermissionUtils;
import com.example.view.OutputSurface;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * ********************************
 * 项目名称：${project_NAME}
 *
 * @Author yangbinbing
 * 邮箱： 963416867@qq.com
 * 创建时间： ${DATA} ${TIME}
 * 用途* 目的给视频添加背景音乐
 * 首先需要将本地视频，解码，
 * 然后将本地音乐解码后
 * 将解码后的音乐和视频编辑在一起
 * 然后编码
 * 然后将他们打包成mp4的视频
 * 的视频添加音乐，在编码成mp4的文件
 * ********************************
 */

public class DecodeVideoEditEncodeMuxAudioVideoActivity extends AppCompatActivity {
    private static String TAG = "DecodeVideoEditEncodeMuxAudioVideoActivity";
    private String videoPath;
    private int mWidth = 1280;
    private int mHeight = 720;
    private int mSourceResId = R.raw.video_480x360_mp4_h264_500kbps_30fps_aac_stereo_128kbps_44100hz;
    private boolean mCopyAudio = true;
    private boolean mCopyVideo = true;
    private MediaFormat mDecoderOutputVideoFormat;
    private MediaFormat mDecoderOutputAudioFormat;
    private MediaFormat mEncoderOutputVideoFormat;
    private MediaFormat mEncoderOutputAudioFormat;
    private int mOutputVideoTrack;
    private int mOutputAudioTrack;
    private boolean mVideoExtractorDone;
    private boolean mVideoDecoderDone;
    private boolean mVideoEncoderDone;
    private boolean mAudioExtractorDone;
    private boolean mAudioDecoderDone;
    private boolean mAudioEncoderDone;
    private LinkedList<Integer> mPendingAudioDecoderOutputBufferIndices;
    private LinkedList<MediaCodec.BufferInfo> mPendingAudioDecoderOutputBufferInfos;
    private LinkedList<Integer> mPendingAudioEncoderInputBufferIndices;
    private LinkedList<Integer> mPendingVideoEncoderOutputBufferIndices;
    private LinkedList<MediaCodec.BufferInfo> mPendingVideoEncoderOutputBufferInfos;
    private LinkedList<Integer> mPendingAudioEncoderOutputBufferIndices;
    private LinkedList<MediaCodec.BufferInfo> mPendingAudioEncoderOutputBufferInfos;
    private boolean mMuxing;
    private int mVideoExtractedFrameCount;
    private int mVideoDecodedFrameCount;
    private int mVideoEncodedFrameCount;
    private int mAudioExtractedFrameCount;
    private int mAudioDecodedFrameCount;
    private int mAudioEncodedFrameCount;
    //h.264 Advance video Coding
    private static final String OUTPUT_VIDEO_MIME_TYPE = "video/avc";
    //音频 编码器
    private static final String OUTPUT_AUDIO_MIME_TYPE = "audio/mp4a-latm";

    private String mOutPutPath;
    private MediaMuxer mMuxer;
    private MediaExtractor mVideoExtractor;
    private EGLHelper mInputSurface;
    private OutputSurface mOutputSurface;
    private MediaCodec mVideoEncode;
    private MediaExtractor mAudioExtractor;
    private MediaFormat mEncoderOutAudioFormat;
    private MediaCodec mAudioEncode;
    private MediaCodec mAudioDecoder;

    private void logState() {
        Log.d(TAG, String.format(
                "loop: "

                        + "V(%b){"
                        + "extracted:%d(done:%b) "
                        + "decoded:%d(done:%b) "
                        + "encoded:%d(done:%b)} "

                        + "A(%b){"
                        + "extracted:%d(done:%b) "
                        + "decoded:%d(done:%b) "
                        + "encoded:%d(done:%b) "

                        + "muxing:%b(V:%d,A:%d)",

                mCopyVideo,
                mVideoExtractedFrameCount, mVideoExtractorDone,
                mVideoDecodedFrameCount, mVideoDecoderDone,
                mVideoEncodedFrameCount, mVideoEncoderDone,

                mCopyAudio,
                mAudioExtractedFrameCount, mAudioExtractorDone,
                mAudioDecodedFrameCount, mAudioDecoderDone,
                mAudioEncodedFrameCount, mAudioEncoderDone,

                mMuxing, mOutputVideoTrack, mOutputAudioTrack));
    }

    /**
     * Used for editing the frames.
     *
     * <p>Swaps green and blue channels by storing an RBGA color in an RGBA buffer.
     */
    private static final String FRAGMENT_SHADER =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 vTextureCoord;\n" +
                    "uniform samplerExternalOES sTexture;\n" +
                    "void main() {\n" +
                    "  gl_FragColor = texture2D(sTexture, vTextureCoord).rbga;\n" +
                    "}\n";
    private MediaCodec mVideoDecoder;
    private HandlerThread mVideoDecoderHandlerThread;
    private CallBack mVideoDecoderHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PermissionUtils.askPermission(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10, runView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(requestCode == 10, grantResults, runView, new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DecodeVideoEditEncodeMuxAudioVideoActivity.this, "没有获得必要的权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private Runnable runView = new Runnable() {
        @Override
        public void run() {
            setContentView(R.layout.activity_decode_video_edit_encode_mux_audio_video);
            findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(DecodeVideoEditEncodeMuxAudioVideoActivity.this, "请等待5s左右,完成会显示Toast,请稍等……", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                extractDecodeEditEncodeMux();
                            } catch (Exception e) {
                                Log.e("报异常了", e.toString());
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            });

            findViewById(R.id.player).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VideoViewActivity.launch(DecodeVideoEditEncodeMuxAudioVideoActivity.this, videoPath);
                }
            });
        }
    };

    private void extractDecodeEditEncodeMux() {
        // Exception that may be thrown during release.
        Exception exception = null;

        mDecoderOutputVideoFormat = null;
        mDecoderOutputAudioFormat = null;
        mEncoderOutputVideoFormat = null;
        mEncoderOutputAudioFormat = null;

        mOutputVideoTrack = -1;
        mOutputAudioTrack = -1;
        mVideoExtractorDone = false;
        mVideoDecoderDone = false;
        mVideoEncoderDone = false;
        mAudioExtractorDone = false;
        mAudioDecoderDone = false;
        mAudioEncoderDone = false;
        mPendingAudioDecoderOutputBufferIndices = new LinkedList<Integer>();
        mPendingAudioDecoderOutputBufferInfos = new LinkedList<MediaCodec.BufferInfo>();
        mPendingAudioEncoderInputBufferIndices = new LinkedList<Integer>();
        mPendingVideoEncoderOutputBufferIndices = new LinkedList<Integer>();
        mPendingVideoEncoderOutputBufferInfos = new LinkedList();
        mPendingAudioEncoderOutputBufferIndices = new LinkedList<Integer>();
        mPendingAudioEncoderOutputBufferInfos = new LinkedList<MediaCodec.BufferInfo>();
        mMuxing = false;
        mVideoExtractedFrameCount = 0;
        mVideoDecodedFrameCount = 0;
        mVideoEncodedFrameCount = 0;
        mAudioExtractedFrameCount = 0;
        mAudioDecodedFrameCount = 0;
        mAudioEncodedFrameCount = 0;
        MediaCodecInfo videoCodeInfo = MediaCodecUtils.selectCodec(OUTPUT_VIDEO_MIME_TYPE);
        if (videoCodeInfo == null) {
            Log.e(TAG, "不支持该编码器" + OUTPUT_VIDEO_MIME_TYPE);
            return;
        }

        MediaCodecInfo audioCodeInfo = MediaCodecUtils.selectCodec(OUTPUT_AUDIO_MIME_TYPE);
        if (videoCodeInfo == null) {
            Log.e(TAG, "不支持该编码器" + OUTPUT_AUDIO_MIME_TYPE);
            return;
        }

        mOutPutPath = FileUtils.getStorageMp4(DecodeVideoEditEncodeMuxAudioVideoActivity.class.getSimpleName()) + "testYbb.mp4";

        try {
            mMuxer = MediaCodecUtils.createMuxer(mOutPutPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            if (mCopyVideo) {
                mVideoExtractor = createExtractor();
                int videoInputTrack = MediaCodecUtils.getAndSelectVideoTrackIndex(mVideoExtractor);
                if (videoInputTrack == -1) {
                    throw new IllegalStateException("missing video track in this video");
                }
                MediaFormat inputFormat = mVideoExtractor.getTrackFormat(videoInputTrack);

                MediaFormat outputVideoFormat = MediaCodecUtils.createMediaFormat(mWidth, mHeight, 2000000, 15, 10);
                AtomicReference<Surface> inputSurfaceReference = new AtomicReference<>();
                mVideoEncode = createVideoEncode(videoCodeInfo, outputVideoFormat, inputSurfaceReference);
                mInputSurface = new EGLHelper();
                mInputSurface.setSurfaceType(EGLHelper.SURFACE_WINDOW, inputSurfaceReference.get());
                mInputSurface.eglInit();
                mInputSurface.makeCurrent();
                mOutputSurface = new OutputSurface();
                mOutputSurface.changeFragmentShader(FRAGMENT_SHADER);
                mVideoDecoder = createVideoDecoder(inputFormat, mOutputSurface.getSurface());
                mInputSurface.checkMakeCurrent();
            }


            if (mCopyAudio) {
                mAudioExtractor = createExtractor();
                int audioInputTrack = MediaCodecUtils.getAndSelectAudioTrackIndex(mAudioExtractor);
                if (audioInputTrack == -1) {
                    throw new IllegalStateException("missing audio track in this video");
                }
                MediaFormat inputFormat = mAudioExtractor.getTrackFormat(audioInputTrack);

                MediaFormat outputAudioFormat = MediaCodecUtils.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 2, 128 * 1024);

                mAudioEncode = createAudioEncoder(audioCodeInfo, outputAudioFormat);
                mAudioDecoder = createAudioDecoder(inputFormat);
            }

            awaitEncode();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void awaitEncode() {
        synchronized (this) {
            while ((mCopyVideo && !mVideoEncoderDone || (mCopyVideo && !mAudioEncoderDone))) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (mCopyVideo) {
            if (mVideoDecodedFrameCount != mVideoEncodedFrameCount) {
                Log.d(TAG, "encoded and decoded video frame counts should match");
            }
            if (mVideoDecodedFrameCount <= mVideoExtractedFrameCount) {
                Log.d(TAG, "decoded frame count should be less than extracted frame count");
            }
        }

        if (mCopyAudio) {
            if (mPendingAudioDecoderOutputBufferIndices.size() == 0) {
                Log.d(TAG, "no frame should be pending");
            }
        }

    }

    private MediaCodec createAudioDecoder(MediaFormat inputFormat) throws IOException {
        MediaCodec audioDecoder = MediaCodecUtils.createAudioDecoder(inputFormat, null, null, 0);
        audioDecoder.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                ByteBuffer decoderInputBuffer = codec.getInputBuffer(index);
                while (!mAudioEncoderDone) {
                    int size = mAudioExtractor.readSampleData(decoderInputBuffer, 0);
                    long presentationTime = mAudioExtractor.getSampleTime();
                    Log.d(TAG, "audio extractor: returned buffer of size " + size);
                    Log.d(TAG, "audio extractor: returned buffer for time " + presentationTime);
                    if (size > 0) {
                        codec.queueInputBuffer(index, 0, size, presentationTime, mAudioExtractor.getSampleFlags());
                    }

                    mAudioExtractorDone = !mAudioExtractor.advance();
                    if (mAudioExtractorDone) {
                        Log.d(TAG, "audio extractor: EOS");
                        codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    }

                    mAudioExtractedFrameCount++;
                    logState();
                    if (size >= 0) {
                        break;
                    }

                }
            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                Log.d(TAG, "audio decoder: returned output buffer: " + index);
                Log.d(TAG, "audio decoder: returned buffer of size " + info.size);
                ByteBuffer decoderOutputBuffer = codec.getOutputBuffer(index);
                if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    Log.d(TAG, "audio decoder: codec config buffer");
                    codec.releaseOutputBuffer(index, false);
                    return;
                }
                Log.d(TAG, "audio decoder: returned buffer for time "
                        + info.presentationTimeUs);
                mPendingAudioDecoderOutputBufferIndices.add(index);
                mPendingAudioDecoderOutputBufferInfos.add(info);
                mAudioDecodedFrameCount++;
                logState();
                tryEncodeAudio();
            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {


            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
                mDecoderOutputAudioFormat = codec.getOutputFormat();
                Log.d(TAG, "audio decoder: output format changed: "
                        + mDecoderOutputAudioFormat);
            }
        });
        audioDecoder.start();
        return audioDecoder;
    }

    private MediaCodec createAudioEncoder(MediaCodecInfo audioCodeInfo, MediaFormat outputAudioFormat) throws IOException {
        MediaCodec encoder = MediaCodecUtils.createMediaCodec(audioCodeInfo, outputAudioFormat);
        encoder.setCallback(new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                Log.d(TAG, "audio encoder: returned input buffer: " + index);
                mPendingAudioEncoderInputBufferIndices.add(index);
                tryEncodeAudio();
            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                Log.d(TAG, "audio encoder: returned output buffer: " + index);
                Log.d(TAG, "audio encoder: returned buffer of size " + info.size);
                muxAudio(index, info);
            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
                Log.d(TAG, "audio encoder: output format changed");
                if (mOutputAudioTrack > 0) {
                    throw new RuntimeException("audio encoder changed its output format again?");
                }

                mEncoderOutAudioFormat = codec.getOutputFormat();
                setupMuxer();
            }
        });
        encoder.start();
        return encoder;
    }

    private void tryEncodeAudio() {

        if (mPendingAudioEncoderInputBufferIndices.size() == 0 || mPendingAudioDecoderOutputBufferIndices.size() == 0) {
            return;
        }

        int decoderIndex = mPendingAudioDecoderOutputBufferIndices.poll();
        int encoderIndex = mPendingAudioEncoderInputBufferIndices.poll();

        MediaCodec.BufferInfo info = mPendingAudioDecoderOutputBufferInfos.poll();
        ByteBuffer encoderInputBuffer = mAudioEncode.getInputBuffer(encoderIndex);
        int size = info.size;
        long presentationTimeUs = info.presentationTimeUs;
        Log.d(TAG, "audio decoder: processing pending buffer: "
                + decoderIndex);
        Log.d(TAG, "audio decoder: pending buffer of size " + size);
        Log.d(TAG, "audio decoder: pending buffer for time " + presentationTimeUs);

        if (size >= 0) {
            ByteBuffer decoderOutputBuffer = mAudioDecoder.getOutputBuffer(decoderIndex).duplicate();
            decoderOutputBuffer.position(info.offset);
            decoderOutputBuffer.limit(info.offset + size);
            encoderInputBuffer.position(0);
            encoderInputBuffer.put(decoderOutputBuffer);
            mAudioEncode.queueInputBuffer(encoderIndex, 0, size, presentationTimeUs, info.flags);
        }

        mAudioDecoder.releaseOutputBuffer(decoderIndex, false);

        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            Log.d(TAG, "audio decoder: EOS");
            mAudioDecoderDone = true;
        }

        logState();
    }

    private void setupMuxer() {
        if (!mMuxing && (!mCopyAudio || mEncoderOutAudioFormat != null) && (!mCopyVideo || mEncoderOutputVideoFormat != null)) {

            if (mCopyVideo) {
                Log.d(TAG, "muxer: adding video track");
                mOutputVideoTrack = mMuxer.addTrack(mEncoderOutputVideoFormat);
            }

            if (mCopyAudio) {
                Log.d(TAG, "muxer: adding audio tarck.");
                mOutputAudioTrack = mMuxer.addTrack(mEncoderOutAudioFormat);
            }


            mMuxer.start();
            mMuxing = true;
            MediaCodec.BufferInfo info;
            while ((info = mPendingVideoEncoderOutputBufferInfos.poll()) != null) {
                int index = mPendingVideoEncoderOutputBufferIndices.poll().intValue();
                muxVideo(index, info);

            }

            while ((info = mPendingAudioEncoderOutputBufferInfos.poll()) != null) {
                int index = mPendingAudioEncoderOutputBufferIndices.poll().intValue();
                muxAudio(index, info);
            }

        }

    }

    private void muxVideo(int index, MediaCodec.BufferInfo info) {
        if (!mMuxing) {
            mPendingVideoEncoderOutputBufferIndices.add(new Integer(index));
            mPendingVideoEncoderOutputBufferInfos.add(info);
            return;
        }

        ByteBuffer encodeOutputBuffer = mVideoEncode.getOutputBuffer(index);
        //这个是判断编码器中的编码器的配置数据 这里我们需要的是媒体的数据，因为一个视频的源文件中包含了编码器的配置数据和媒体数据
        if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            Log.d(TAG, "video encode: codec config buffer");
            mVideoEncode.releaseOutputBuffer(index, false);
            return;
        }

        Log.d(TAG, "video encoder: returned buffer for time" + info.presentationTimeUs);
        if (info.size != 0) {
            mMuxer.writeSampleData(mOutputVideoTrack, encodeOutputBuffer, info);
        }

        mVideoEncode.releaseOutputBuffer(index, false);
        mVideoEncodedFrameCount++;
        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            Log.d(TAG, "video encoder : eos");
            synchronized (this) {
                mVideoEncoderDone = true;
                notifyAll();
            }
        }

        logState();

    }


    private void muxAudio(int index, MediaCodec.BufferInfo info) {
        if (!mMuxing) {
            mPendingAudioDecoderOutputBufferIndices.add(new Integer(index));
            mPendingAudioDecoderOutputBufferInfos.add(info);
            return;
        }

        ByteBuffer encodeOutputBuffer = mAudioEncode.getOutputBuffer(index);
        if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            Log.d(TAG, "andio encoder: codec config buffer");
            mAudioEncode.releaseOutputBuffer(index, false);
            return;
        }

        Log.d(TAG, "audio encoder: returned buffer for time" + info.presentationTimeUs);

        if (info.size != 0) {
            mMuxer.writeSampleData(index, encodeOutputBuffer, info);
        }
        mAudioEncode.releaseOutputBuffer(index, false);
        mAudioEncodedFrameCount++;
        if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            Log.d(TAG, "audio encoder: EOS");
            synchronized (this) {
                mAudioEncoderDone = true;
                notifyAll();
            }
        }
        logState();
    }


    private MediaCodec createVideoDecoder(MediaFormat inputFormat, Surface surface) {
        mVideoDecoderHandlerThread = new HandlerThread("DecodeThread");
        mVideoDecoderHandlerThread.start();
        mVideoDecoderHandle = new CallBack(mVideoDecoderHandlerThread.getLooper());
        MediaCodec.Callback callback = new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {
                ByteBuffer decodeInputBuffer = codec.getInputBuffer(index);
                while (!mVideoExtractorDone) {
                    int size = mVideoExtractor.readSampleData(decodeInputBuffer, 0);
                    long presentationTime = mVideoExtractor.getSampleTime();
                    Log.d(TAG, "video extractor: returned buffer of size " + size);
                    Log.d(TAG, "video extractor: returned buffer for time " + presentationTime);
                    Log.d(TAG, "video extractor: returned buffer for Flags " + mVideoExtractor.getSampleFlags());
                    if (size >= 0) {
                        codec.queueInputBuffer(index, 0, size, presentationTime, mVideoExtractor.getSampleFlags());
                    }
                    //如果下一帧没数据则需要通知，此时流已经结束了
                    mVideoExtractorDone = !mVideoExtractor.advance();

                    if (mVideoExtractorDone) {
                        codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                    }

                    mVideoExtractedFrameCount++;
                    logState();
                    //注意这个while循环的结束
                    if (size > 0) {
                        break;
                    }
                }
            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {

                Log.d(TAG, "video decoder: returned output buffer: " + index);
                Log.d(TAG, "video decoder: returned buffer of size " + info.size);

                if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    Log.d(TAG, "video decoder: codec config buffer");
                    codec.releaseOutputBuffer(index, false);
                    return;
                }

                boolean render = info.size != 0;
                codec.releaseOutputBuffer(index, render);
                if (render) {
                    mInputSurface.makeCurrent();
                    Log.d(TAG, "output surface: await new image");
                    mOutputSurface.awaitNewImage();
                    mOutputSurface.drawImage();
                    mInputSurface.swapBuffers();
                    mInputSurface.setPresentationTime(info.presentationTimeUs * 1000);
                    mInputSurface.checkMakeCurrent();
                    Log.d(TAG, "input surface: swap buffers");
                }

                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    mVideoDecoderDone = true;
                    mVideoEncode.signalEndOfInputStream();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DecodeVideoEditEncodeMuxAudioVideoActivity.this, "处理结束", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                mVideoDecodedFrameCount++;
                logState();
            }

            @Override
            public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
                mDecoderOutputVideoFormat = codec.getOutputFormat();
                Log.d(TAG, "video decoder: output format changed: "
                        + mDecoderOutputVideoFormat);
            }
        };
        mVideoDecoderHandle.create(false, inputFormat.getString(MediaFormat.KEY_MIME), callback);
        MediaCodec decoder = mVideoDecoderHandle.getCodec();
        decoder.configure(inputFormat, surface, null, 0);
        decoder.start();
        return decoder;
    }

    private MediaCodec createVideoEncode(MediaCodecInfo videoCodeInfo, MediaFormat format, AtomicReference<Surface> inputSurfaceReference) {
        MediaCodec mediaCodec = null;
        try {
            mediaCodec = MediaCodecUtils.createMediaCodec(videoCodeInfo, format);
            mediaCodec.setCallback(new MediaCodec.Callback() {
                @Override
                public void onInputBufferAvailable(@NonNull MediaCodec codec, int index) {


                }

                @Override
                public void onOutputBufferAvailable(@NonNull MediaCodec codec, int index, @NonNull MediaCodec.BufferInfo info) {
                    Log.d(TAG, "video encoder: returned output buffer: " + index);
                    Log.d(TAG, "video encoder: returned buffer of size " + info.size);
                    muxVideo(index, info);
                }

                @Override
                public void onError(@NonNull MediaCodec codec, @NonNull MediaCodec.CodecException e) {

                }

                @Override
                public void onOutputFormatChanged(@NonNull MediaCodec codec, @NonNull MediaFormat format) {
                    Log.d(TAG, "video encoder: output format changed");
                    if (mOutputVideoTrack > 0) {
                        throw new RuntimeException("audio encoder changed its output format again?");
                    }
                    mEncoderOutputVideoFormat = codec.getOutputFormat();
                    setupMuxer();
                }
            });

            inputSurfaceReference.set(mediaCodec.createInputSurface());
            mediaCodec.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return mediaCodec;
    }

    /**
     * @return 媒体提取器  可以提取音频和视频
     * @throws IOException
     */
    private MediaExtractor createExtractor() throws IOException {
        MediaExtractor extractor = new MediaExtractor();
        AssetFileDescriptor srcFd = getResources().openRawResourceFd(mSourceResId);
        extractor.setDataSource(srcFd.getFileDescriptor(), srcFd.getStartOffset(), srcFd.getLength());
        return extractor;
    }


    static class CallBack extends Handler {
        public CallBack(@NonNull Looper looper) {
            super(looper);
        }

        private MediaCodec mCodec;
        private String mMine;
        private boolean mEncoder;
        private MediaCodec.Callback mCallback;
        private boolean mSetDone;

        @Override
        public void handleMessage(@NonNull Message msg) {
            try {
                mCodec = mEncoder ? MediaCodec.createEncoderByType(mMine) : MediaCodec.createDecoderByType(mMine);
                mCodec.setCallback(mCallback);
                synchronized (this) {
                    mSetDone = true;
                    notifyAll();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void create(boolean encoder, String mime, MediaCodec.Callback callback) {
            mEncoder = encoder;
            mMine = mime;
            mCallback = callback;
            mSetDone = false;
            sendEmptyMessage(0);
            synchronized (this) {
                while (!mSetDone) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        MediaCodec getCodec() {
            return mCodec;
        }
    }


}
