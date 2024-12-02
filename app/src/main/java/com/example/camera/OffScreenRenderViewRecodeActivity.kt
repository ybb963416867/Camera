package com.example.camera

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gpengl.multiple.CoordinatePoint
import com.example.gpengl.multiple.CoordinateRegion
import com.example.gpengl.multiple.check
import com.example.view.OffScreenViewLocationRecodeGlSurface

class OffScreenRenderViewRecodeActivity : AppCompatActivity() {
    private lateinit var glSurfaceView: OffScreenViewLocationRecodeGlSurface
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_off_screen_render_view_recode)
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
        }

        findViewById<Button>(R.id.but_recorder_stop).setOnClickListener {
            glSurfaceView.stopRecord()
        }

        findViewById<Button>(R.id.but_test).setOnClickListener {
            glSurfaceView.setRecodeView(rootView, rootView.width, rootView.height)
        }

        rootView.post {
            glSurfaceView.setRecodeView(rootView, rootView.width, rootView.height)
        }

        findViewById<Button>(R.id.but_capture1).setOnClickListener {
            glSurfaceView.capture1()
        }

        findViewById<Button>(R.id.but_capture2).setOnClickListener {
            glSurfaceView.capture2()
        }


    }
}