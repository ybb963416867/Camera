package com.example.camera

import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gpengl.multiple.CoordinatePoint
import com.example.gpengl.multiple.CoordinateRegion
import com.example.libffmpeg.FFMpegManager
import com.example.view.FFmpegGlSurface

class FFmpegRecodeActivity : AppCompatActivity() {
    private lateinit var glSurfaceView: FFmpegGlSurface
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_ffmpeg_recode)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val rootView = findViewById<FrameLayout>(R.id.fl_root)
        // 创建 GLSurfaceView 并设置 OpenGL 版本
        glSurfaceView = findViewById(R.id.glSurfaceView)

        findViewById<Button>(R.id.but_load_pic).setOnClickListener {
            glSurfaceView.loadTexture(R.mipmap.bg)
        }


        findViewById<Button>(R.id.but_update).setOnClickListener {
            glSurfaceView.update()
        }

        findViewById<Button>(R.id.but_adjust_position1).setOnClickListener {
            glSurfaceView.updateTexCord(
                CoordinateRegion(
                    leftTop = CoordinatePoint(
                        x = 0f,
                        y = 0f
                    ),
                    rightTop = CoordinatePoint(x = 500f, y = 0f),
                    leftBottom = CoordinatePoint(x = 0f, y = 400f),
                    rightBottom = CoordinatePoint(x = 500f, y = 400f)
                )
            )
        }

        findViewById<Button>(R.id.but_recorder_start).setOnClickListener {
            glSurfaceView.startRecord()
            val ffmpegVersion = FFMpegManager.getFFmpegVersion()
            Log.e("ybb", ffmpegVersion)
        }

        findViewById<Button>(R.id.but_recorder_stop).setOnClickListener {
            glSurfaceView.stopRecord()
        }

        findViewById<Button>(R.id.but_test).setOnClickListener {
            glSurfaceView.setRecodeView(rootView, rootView.width, rootView.height)
        }

        rootView.post {
            val surfaceViewLocation = IntArray(2)
            glSurfaceView.getLocationOnScreen(surfaceViewLocation)

            val rootViewLocation = IntArray(2)
            rootView.getLocationOnScreen(rootViewLocation)

            val rootRect = Rect(
                rootViewLocation[0],
                rootViewLocation[1],
                rootViewLocation[0] + rootView.width,
                rootViewLocation[1] + rootView.height
            )
            val surfaceRect = Rect(
                surfaceViewLocation[0],
                surfaceViewLocation[1],
                surfaceViewLocation[0] + glSurfaceView.width,
                surfaceViewLocation[1] + glSurfaceView.height
            )

            val point = if (surfaceRect.contains(rootRect)) {
                Point(rootRect.left - surfaceRect.left, rootRect.top - surfaceRect.top)
            } else {
                Point(0, 0)
            }


            Log.d(
                "ybb",
                "surfaceViewLocation: ${surfaceViewLocation[0]}, ${surfaceViewLocation[1]} rootViewLocation: ${rootViewLocation[0]}, ${rootViewLocation[1]}"
            )
            glSurfaceView.setLocationViewInfo(
                rootView,
                rootView.width,
                rootView.height,
                point.x,
                point.y
            )
        }

        findViewById<Button>(R.id.but_capture1).setOnClickListener {
            glSurfaceView.capture1()
        }

        findViewById<Button>(R.id.but_capture2).setOnClickListener {
            glSurfaceView.capture2()
        }


    }
}