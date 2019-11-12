package com.example.camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.base.BaseActivity;
import com.example.manager.EGLHelper;
import com.example.util.FileUtils;
import com.example.util.MediaRecorder;
import com.example.util.PermissionUtils;
import com.example.view.SurfaceTextureManager;


public class OpenglVideoRecodeActivity extends BaseActivity {

    private KitkatCamera kitkatCamera;
    private EGLHelper eglHelper;
    private MediaRecorder mediaRecorder;
    private static final String TAG = "OpenglVideoRecode";

    private static final long DURATION_SEC = 8;             // 8 seconds of video

    private SurfaceTextureManager mStManager;

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
                Toast.makeText(OpenglVideoRecodeActivity.this, "没有获得必要的权限", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(OpenglVideoRecodeActivity.this, "请等待5s左右,完成会显示Toast,请稍等……", Toast.LENGTH_SHORT);
                                    }
                                });
                                encodeCameraToMpeg();
                            } catch (Exception e) {
                                Log.e("报异常了", e.toString());
                                e.printStackTrace();
                            } catch (Throwable throwable) {
                                Log.e("报异常了", "aa");
                                throwable.printStackTrace();
                            }
                        }
                    }).start();
                }
            });
        }
    };

    /**
     * Tests encoding of AVC video from Camera input.  The output is saved as an MP4 file.
     */
    private void encodeCameraToMpeg() {

        try {
            kitkatCamera = new KitkatCamera();
            kitkatCamera.open(0);
            kitkatCamera.setDisplayOrientation(90);
            prepareEncoder(kitkatCamera.getPreviewSize().x, kitkatCamera.getPictureSize().y);
            eglHelper.makeCurrent();
            /**
             * 相机绑定SurfaceTexture 而surfaceTexture 的纹理使用的OesFilter纹理，相机数据数据变化引发
             * SurfaceTexture 的onFrameAvailable 回调 然后去调用OesFilter的draw方法，绘制到纹理中
             */
            mStManager = new SurfaceTextureManager(OpenglVideoRecodeActivity.this);
            SurfaceTexture st = mStManager.getSurfaceTexture();
            kitkatCamera.setPreviewTexture(st);
            kitkatCamera.preview();

            long startWhen = System.nanoTime();
            long desiredEnd = startWhen + DURATION_SEC * 1000000000L;
            int frameCount = 0;

            while (System.nanoTime() < desiredEnd) {
//            while (frameCount < 100) {
                // Feed any pending encoder output into the muxer.
                mediaRecorder.drainEncoder(false);

                frameCount++;
                Log.d(TAG, "frameCount:" + frameCount);
                // Acquire a new frame of input, and render it to the Surface.  If we had a
                // GLSurfaceView we could switch EGL contexts and call drawImage() a second
                // time to render it on screen.  The texture can be shared between contexts by
                // passing the GLSurfaceView's EGLContext as eglCreateContext()'s share_context
                // argument.
                mStManager.awaitNewImage();
                mStManager.drawImage();

                // Set the presentation time stamp from the SurfaceTexture's time stamp.  This
                // will be used by MediaMuxer to set the PTS in the video.
                Log.d(TAG, "present: " + ((st.getTimestamp() - startWhen) / 1000000.0) + "ms");
                eglHelper.setPresentationTime(st.getTimestamp());

                // Submit it to the encoder.  The eglSwapBuffers call will block if the input
                // is full, which would be bad if it stayed full until we dequeued an output
                // buffer (which we can't do, since we're stuck here).  So long as we fully drain
                // the encoder before supplying additional input, the system guarantees that we
                // can supply another frame without blocking.
                Log.d(TAG, "sending frame to encoder");
                eglHelper.swapBuffers();
            }
            mediaRecorder.drainEncoder(true);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // release everything we grabbed
            releaseCamera();
            releaseEncoder();
            releaseSurfaceTexture();
        }
    }

    /**
     * Stops camera preview, and releases the camera to the system.
     */
    private void releaseCamera() {
        Log.d(TAG, "releasing camera");
        if (kitkatCamera != null) {
            kitkatCamera.close();
        }
    }

    private void releaseSurfaceTexture() {
        if (mStManager != null) {
            mStManager.release();
            mStManager = null;
        }
    }

    private void prepareEncoder(int width, int height) {
        mediaRecorder = new MediaRecorder(width, height, FileUtils.getStorageMp4(MainActivity.class.getSimpleName()));
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
        if (mediaRecorder != null) {
            mediaRecorder.releaseEncoder();
        }
        if (eglHelper != null) {
            eglHelper.destroy();
        }
    }

}
