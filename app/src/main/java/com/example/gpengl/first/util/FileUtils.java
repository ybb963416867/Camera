package com.example.gpengl.first.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * description:
 * Created by aserbao on 2018/5/14.
 */


public class FileUtils {

    private static final Date date = new Date();

    @SuppressLint("ConstantLocale")
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.getDefault());

    /**
     * 返回带日期的名称
     *
     * @return String
     */
    public static String getDateName() {
        return getDateName(null);
    }

    /**
     * 返回带日期的名称
     *
     * @param prefix 文件名前缀(会自动拼接 _ )
     * @return String
     */
    public static String getDateName(String prefix) {
        date.setTime(System.currentTimeMillis());
        String dateStr = dateFormat.format(date);
        if (!TextUtils.isEmpty(prefix)) {
            return prefix + "_" + dateStr;
        } else {
            return dateStr;
        }
    }

    /**
     * 获取Cache目录
     *
     * @param context context
     * @return File
     */
    public static File getCacheDir(Context context) {
        return context.getExternalCacheDir();
    }

    /**
     * 获取Cache目录 Movie
     *
     * @param context context
     * @return File
     */
    public static File getCacheMovieDir(Context context) {
        String dir = Environment.DIRECTORY_MOVIES;
        return new File(getCacheDir(context), dir);
    }

    public static String getStorageMp4(Context context, String s) {
        File dirFile = getCacheMovieDir(context);
        dirFile.mkdirs();
        // 创建保存文件
        File mediaFile = new File(dirFile, FileUtils.getDateName(s) + ".mp4");

        return mediaFile.getPath();
    }



}
