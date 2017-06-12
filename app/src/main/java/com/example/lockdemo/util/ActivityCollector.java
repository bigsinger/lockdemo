package com.example.lockdemo.util;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * 回收activity，保证所有的activity被销毁
 */

public class ActivityCollector {
    private static List<Activity> activityList = new ArrayList<Activity>();

    public static void addActivity(Activity activity) {
        activityList.add(activity);
    }

    public static void removeActivity(Activity activity) {
        activityList.remove(activity);
    }

    public static void finishAllButLast() {
        Activity activity = activityList.get(activityList.size() - 1);
        removeActivity(activity);

        for (Activity activityItem : activityList) {
            if (!activityItem.isFinishing()) {
                activityItem.finish();
            }
        }

        activityList.clear();
        activityList.add(activity);
    }

    public static void finishAll() {
        for (Activity activity : activityList) {
            if (!activity.isFinishing()) {
//                Log.i("Back",activity.toString());
                activity.finish();
            }
        }

        activityList.clear();
    }
}
