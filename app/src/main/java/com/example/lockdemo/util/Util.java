package com.example.lockdemo.util;

import android.app.ActivityManager;
import android.content.Context;
import android.widget.Toast;

import java.util.List;

/**
 * Created by sing on 2017/1/17.
 */

public class Util {
    public static void toast(Context context, String s) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }

    /**
     * 程序是否在前台运行
     *
     * @return
     */
    public static boolean isForeground(Context context) {
        Context appContext = context.getApplicationContext();
        ActivityManager activityManager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = appContext.getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }
}
