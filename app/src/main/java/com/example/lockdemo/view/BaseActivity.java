package com.example.lockdemo.view;

import android.app.Activity;
import android.os.Bundle;

import com.example.lockdemo.util.ActivityCollector;

public class BaseActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
//        Log.i("Test", "ttt");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }
}
