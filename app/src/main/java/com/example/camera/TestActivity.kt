package com.example.camera
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.view.TestGlSurface


class TestActivity : AppCompatActivity() {

    private lateinit var surfaceView: TestGlSurface
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.enableEdgeToEdge()
        setContentView(R.layout.activity_test)
        surfaceView = findViewById(R.id.glSurfaceView)

        findViewById<Button>(R.id.but_test).setOnClickListener {
            surfaceView.loadTexture(R.mipmap.cc)
        }
    }




    override fun onResume() {
        super.onResume()

    }

    override fun onPause() {
        super.onPause()
    }
}