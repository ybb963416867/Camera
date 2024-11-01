package com.example.camera

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.util.FileUtils
import com.example.util.PermissionUtils
import java.io.IOException

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        PermissionUtils.askPermission(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_EXTERNAL_STORAGE),
            10
        ) { }
        setContentView(R.layout.activity_test)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val customGLSurfaceView = findViewById<GLSurfaceViewRecorder>(R.id.customGLSurfaceView)
        // 设置按钮点击事件
        findViewById<View>(R.id.but_start).setOnClickListener { v: View? ->
            try {

                val videoPath =
                    com.example.gpengl.first.util.FileUtils.getStorageMp4(applicationContext, TestActivity::class.java.simpleName)
                Log.e("ybb", "videoPath: $videoPath")
                customGLSurfaceView.startRecording(videoPath)
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }
        findViewById<View>(R.id.but_stop).setOnClickListener { v: View? -> customGLSurfaceView.stopRecording() }

        findViewById<View>(R.id.but_update).setOnClickListener { v: View? ->customGLSurfaceView.requestRender()}

        //        // 设置图片的宽和高
//        customGLSurfaceView.setImageDimensions(300, 300);
//
//        // 设置显示区域的边界
//        customGLSurfaceView.setDisplayArea(100, 500, 100, 700);
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        PermissionUtils.onRequestPermissionsResult(requestCode == 10, grantResults, {}) {
            Toast.makeText(
                this@TestActivity,
                "没有获得必要的权限",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}