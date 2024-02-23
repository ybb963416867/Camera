package com.example.gpengl.third;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.camera.R;
import com.example.gpengl.third.widget.DouyinView;
import com.example.gpengl.third.widget.RecordButton;


public class ThirdActivity extends AppCompatActivity {

    DouyinView douyinView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        douyinView = findViewById(R.id.douyinView);

        RecordButton recordButton = findViewById(R.id.btn_record);
        recordButton.setOnRecordListener(new RecordButton.OnRecordListener() {
            /**
             * 开始录制
             */
            @Override
            public void onRecordStart() {
                douyinView.startRecord();
            }

            /**
             * 停止录制
             */
            @Override
            public void onRecordStop() {
                douyinView.stopRecord();
            }
        });
        RadioGroup radioGroup = findViewById(R.id.rg_speed);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            /**
             * 选择录制模式
             * @param group
             * @param checkedId
             */
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_extra_slow){
                    douyinView.setSpeed(DouyinView.Speed.MODE_EXTRA_SLOW);
                }else if (checkedId == R.id.rb_slow){
                    douyinView.setSpeed(DouyinView.Speed.MODE_SLOW);
                }else if (checkedId == R.id.rb_normal){
                    douyinView.setSpeed(DouyinView.Speed.MODE_NORMAL);
                }else if (checkedId == R.id.rb_fast){
                    douyinView.setSpeed(DouyinView.Speed.MODE_FAST);
                } else if (checkedId == R.id.rb_extra_fast) {
                    douyinView.setSpeed(DouyinView.Speed.MODE_EXTRA_FAST);
                }
            }
        });
    }
}
