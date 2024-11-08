package com.example.camera

import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gpengl.multiple.CoordinatePoint
import com.example.gpengl.multiple.CoordinateRegion
import com.example.gpengl.multiple.check
import com.example.view.MultipleCombineBackgroundTouchGlSurface

class MultipleTextureTouchActivity : AppCompatActivity() {

    private lateinit var glSurfaceView: MultipleCombineBackgroundTouchGlSurface

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mutiple_texture_touch)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 创建 GLSurfaceView 并设置 OpenGL 版本
        glSurfaceView = findViewById(R.id.glSurfaceView)


        findViewById<Button>(R.id.but_load_pic).setOnClickListener {
            glSurfaceView.loadTexture(R.mipmap.bg)
        }

        findViewById<Button>(R.id.but_load_pic1).setOnClickListener {
            glSurfaceView.loadTexture(R.mipmap.photo2)
        }

        findViewById<Button>(R.id.but_load_pic2).setOnClickListener {
            glSurfaceView.loadTexture(R.mipmap.photo)
        }

        findViewById<Button>(R.id.but_load_pic3).setOnClickListener {
            glSurfaceView.loadTexture(R.mipmap.yunshen)
        }

        findViewById<Button>(R.id.but_adjust_position).setOnClickListener {
            glSurfaceView.updateTexCord(
                CoordinateRegion(
                    leftTop = CoordinatePoint(
                        x = 100f,
                        y = 100f
                    ),
                    rightTop = CoordinatePoint(x = 600f, y = 100f),
                    leftBottom = CoordinatePoint(x = 100f, y = 400f),
                    rightBottom = CoordinatePoint(x = 600f, y = 400f)
                ).check()
            )
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

    }
}