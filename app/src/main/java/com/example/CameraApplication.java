package com.example;

import android.app.Application;
import android.util.DisplayMetrics;

/**
 * @author yangbinbing
 * @date 2019/10/22
 * @Description
 */
public class CameraApplication extends Application {
    public static CameraApplication app;
    public static int screenWidth;
    public static int screenHeight;

    @Override
    public void onCreate() {
        super.onCreate();
        DisplayMetrics mDisplayMetrics = getApplicationContext().getResources()
                .getDisplayMetrics();
        screenWidth = mDisplayMetrics.widthPixels;
        screenHeight = mDisplayMetrics.heightPixels;
        app=this;
    }
    public static CameraApplication getInstance() {
        return app;
    }



}
