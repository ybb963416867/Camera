package com.example.camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.opengl.GLES20;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.manager.EGLHelper;
import com.example.util.FileUtils;
import com.example.util.MediaRecorder;
import com.example.util.PermissionUtils;

public class EncodeAndMuxActivity extends AppCompatActivity {

    private String videoPath;
    private String TAG = "EncodeAndMuxActivity";
    private EGLHelper eglHelper;
    private MediaRecorder mediaRecorder;

    // RGB color values for generated frames
    private static final int TEST_R0 = 0;
    private static final int TEST_G0 = 136;
    private static final int TEST_B0 = 0;
    private static final int TEST_R1 = 236;
    private static final int TEST_G1 = 50;
    private static final int TEST_B1 = 186;
    private int encWidth;
    private int encHeight;


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
                Toast.makeText(EncodeAndMuxActivity.this, "没有获得必要的权限", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private Runnable runView = new Runnable() {
        @Override
        public void run() {
            setContentView(R.layout.activity_opengl_video_recode);
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
                                        Toast.makeText(EncodeAndMuxActivity.this, "请等待5s左右,完成会显示Toast,请稍等……", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                encodeCameraToMpeg();
//                                CameraToMpegWrapper.runTest(MainActivity.this);
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
                    VideoViewActivity.launch(EncodeAndMuxActivity.this, videoPath);
                }
            });
        }
    };

    private void encodeCameraToMpeg() {
        // arbitrary but popular values
        try {
            encWidth = 1280;
            encHeight = 720;
            int encBitRate = 6000000;
            prepareEncoder(encWidth, encHeight);
            eglHelper.makeCurrent();
            int frames = 30;

            for (int i = 0; i < frames; i++) {
                mediaRecorder.drainEncoder(false);
                generateSurfaceFrame(i);
                eglHelper.setPresentationTime(computePresentationTimeNsec(i));
                eglHelper.swapBuffers();
            }
            mediaRecorder.drainEncoder(true);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            releaseEncoder();
        }


    }

    private void prepareEncoder(int width, int height) {
        videoPath = FileUtils.getStorageMp4(getApplicationContext(), EncodeAndMuxActivity.class.getSimpleName());
        Log.i(TAG, "videoPAth:" + videoPath);
        mediaRecorder = new MediaRecorder(width, height, videoPath);
        mediaRecorder.start();
        eglHelper = new EGLHelper();
        eglHelper.setSurfaceType(EGLHelper.SURFACE_WINDOW, mediaRecorder.getEncodeInputSurface());
        eglHelper.eglInit();
    }

    /**
     * Releases encoder resources.
     */
    private void releaseEncoder() {
        Log.d(TAG, "releasing encoder objects");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(EncodeAndMuxActivity.this, "录制完了", Toast.LENGTH_SHORT).show();
            }
        });
        if (mediaRecorder != null) {
            mediaRecorder.releaseEncoder();
        }
        if (eglHelper != null) {
            eglHelper.destroy();
        }
    }

    /**
     * Generates a frame of data using GL commands.  We have an 8-frame animation
     * sequence that wraps around.  It looks like this:
     * <pre>
     *   0 1 2 3
     *   7 6 5 4
     * </pre>
     * We draw one of the eight rectangles and leave the rest set to the clear color.
     */
    private void generateSurfaceFrame(int frameIndex) {
        frameIndex %= 8;

        int startX, startY;
        if (frameIndex < 4) {
            // (0,0) is bottom-left in GL
            startX = frameIndex * (encWidth / 4);
            startY = encHeight / 2;
        } else {
            startX = (7 - frameIndex) * (encWidth / 4);
            startY = 0;
        }

        GLES20.glClearColor(TEST_R0 / 255.0f, TEST_G0 / 255.0f, TEST_B0 / 255.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        //开启裁剪测试
        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        //开启裁剪
        GLES20.glScissor(startX, startY, encWidth / 4, encHeight / 2);
        GLES20.glClearColor(TEST_R1 / 255.0f, TEST_G1 / 255.0f, TEST_B1 / 255.0f, 1.0f);

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);


    }

    /**
     * Generates the presentation time for frame N, in nanoseconds.
     */
    private static long computePresentationTimeNsec(int frameIndex) {
        final long ONE_BILLION = 1000000000;
        return frameIndex * ONE_BILLION / 30;
    }

}
