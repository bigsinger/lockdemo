package com.example.lockdemo;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.example.lockdemo.lock.LockPatternUtils;
import com.example.lockdemo.lock.UnlockGestureActivity;
import com.example.lockdemo.util.Constant;
import com.example.lockdemo.util.Util;

/**
 */

public class App extends Application {
    public static final String TAG = "App";

    private static App mInstance;
    private SharedPreferences mSP;
    private LockPatternUtils mLockPatternUtils;
    private int countActivedActivity = -1;
    private boolean mBackgroundEver = false;

    public static App getInstance() {
        return mInstance;
    }

    public LockPatternUtils getLockPatternUtils() {
        return mLockPatternUtils;
    }

    /**
     * 获取是否开启手势，默认true开启，如果异常，则不开启手势
     *
     * @return
     */
    private boolean isAlpSwitchOn() {
        boolean result = false;
        try {
            result = mSP.getBoolean(Constant.ALP_SWITCH_ON, true);
        } catch (Exception e) {
            result = false;
        }

        return result;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        countActivedActivity = 0;
        mInstance = this;
        mSP = getSharedPreferences(Constant.CONFIG_NAME, MODE_PRIVATE);
        mLockPatternUtils = new LockPatternUtils(this);

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(Activity activity) {
                if (countActivedActivity == 0 && mBackgroundEver == true) {
                    Log.v(TAG, "切到前台  lifecycle");
                    timeOutCheck(activity);
                }
                countActivedActivity++;
            }

            @Override
            public void onActivityResumed(Activity activity) {

            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {
                countActivedActivity--;
                if (countActivedActivity == 0) {
                    Log.v(TAG, "切到后台  lifecycle");
                    mBackgroundEver = true;

                    if (isAlpSwitchOn() == true) {
                        saveStartTime();
                    }
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }

    public void timeOutCheck(Activity activity) {
        long endTime = System.currentTimeMillis();
        if (endTime - getStartTime() >= Constant.TIMEOUT_ALP * 1000) {
            Util.toast(this, "超时了,请重新验证");
            String alp = mSP.getString(Constant.ALP, null);
            if (TextUtils.isEmpty(alp) == false) {
                Intent intent = new Intent(this, UnlockGestureActivity.class);
                intent.putExtra("pattern", alp);
                intent.putExtra("login", false); //手势验证，不进行登录验证
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                // 打开新的Activity
                activity.startActivityForResult(intent, Constant.REQ_COMPARE_PATTERN_TIMEOUT_CHECK);
            }
        }
    }

    public void saveStartTime() {
        mSP.edit().putLong(Constant.START_TIME, System.currentTimeMillis()).commit();
    }

    public long getStartTime() {
        long startTime = 0;
        try {
            startTime = mSP.getLong(Constant.START_TIME, 0);
        } catch (Exception e) {
            startTime = 0;
        }
        return startTime;
    }
}
