package com.example.lockdemo.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lockdemo.App;
import com.example.lockdemo.R;
import com.example.lockdemo.lock.CreateGestureActivity;
import com.example.lockdemo.lock.UnlockGestureActivity;
import com.example.lockdemo.util.Constant;
import com.example.lockdemo.util.Util;

public class LoginActivity extends Activity {
    public static final int LOGIN_OK = 0;
    public static final int LOGIN_FAILED = 1;
    EditText et_account;
    EditText et_pswd;
    private SharedPreferences mSP;
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case LOGIN_OK:
                    SharedPreferences.Editor editor = mSP.edit();
                    editor.putBoolean(Constant.LOGIN_STATE, true);
                    editor.commit();
                    //如果是登录成功的，之前未设置过手势锁的需要提示进入设置，之前设置过的也无需再次解锁。
                    String alp = mSP.getString(Constant.ALP, null);
                    if (TextUtils.isEmpty(alp)) {
                        createLockerView();
                    } else {
                        openMainActivityAndFinish();
                    }
                    break;
                case LOGIN_FAILED:
                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            return true;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSP = getSharedPreferences(Constant.CONFIG_NAME, MODE_PRIVATE);
        et_account = (EditText) findViewById(R.id.et_account);
        et_pswd = (EditText) findViewById(R.id.et_pswd);

        boolean bRemeber = mSP.getBoolean(Constant.LOGIN_STATE, false);
        if (bRemeber) {
            //已经是登录状态，如果之前未设置过手势锁则进入设置页面，如果之前设置过则进入解锁页面。
            OpenUnlockActivityAndWait();
        }
    }

    public void doLogin(View view) {
        String account = et_account.getText().toString();
        String pswd = et_pswd.getText().toString();

        if (TextUtils.isEmpty(account) == false) {
            if (account.equals("q")) {
                Message msg = Message.obtain();
                msg.what = LOGIN_OK;
                handler.sendMessage(msg);
            } else {
                Message msg = Message.obtain();
                msg.what = LOGIN_FAILED;
                handler.sendMessage(msg);
            }
        }
    }


    public void createLockerView() {
        App.getInstance().getLockPatternUtils().clearLock();
        Intent intent = new Intent(LoginActivity.this, CreateGestureActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // 打开新的Activity
        startActivityForResult(intent, Constant.REQ_CREATE_PATTERN);
    }

    private void OpenUnlockActivityAndWait() {
        //登录成功后，如果没有设置过alp，则弹窗设置。否则则弹窗验证。
        String alp = mSP.getString(Constant.ALP, null);
        if (TextUtils.isEmpty(alp)) {
            App.getInstance().getLockPatternUtils().clearLock();
            Intent intent = new Intent(LoginActivity.this, CreateGestureActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // 打开新的Activity
            startActivityForResult(intent, Constant.REQ_CREATE_PATTERN);
        } else {
            Intent intent = new Intent(LoginActivity.this, UnlockGestureActivity.class);
            intent.putExtra("pattern", alp);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // 打开新的Activity
            startActivityForResult(intent, Constant.REQ_COMPARE_PATTERN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constant.REQ_CREATE_PATTERN:
                //创建手势
                switch (resultCode) {
                    case RESULT_OK:
                        byte[] pattern = data.getByteArrayExtra("pattern");
                        if (pattern != null) {
                            StringBuffer buffer = new StringBuffer();
                            for (byte c : pattern) {
                                buffer.append(c);
                            }

                            //保存创建的手势图
                            SharedPreferences.Editor editor = mSP.edit();
                            editor.putString(Constant.ALP, buffer.toString());
                            editor.commit();

                            openMainActivityAndFinish();
                        }
                        break;
                    case RESULT_CANCELED:
//                        Log.i(TAG, "user cancelled");
                        break;
                    default:
                        break;
                }

                break;
            case Constant.REQ_COMPARE_PATTERN:
                /*
                 * 注意！有四种可能出现情况的返回结果
		         */
                switch (resultCode) {
                    case RESULT_OK:
                        //用户通过验证，登录成功 打开主界面
                        openMainActivityAndFinish();
                        break;
                    case Constant.RESULT_FAILED:
                        //用户多次失败
//                        Log.i(TAG, "user failed");
                        break;
                    case Constant.RESULT_FORGOT_PATTERN:
                        // The user forgot the pattern and invoked your recovery Activity.
                        SharedPreferences.Editor editor = mSP.edit();
                        editor.putString(Constant.ALP, null);
                        editor.commit();
                        et_pswd.setText("");
                        break;
                    case Constant.RESULT_ERRORS:
                        Util.toast(this, "发生异常，请重新登录");
                        break;
                    case Constant.RESULT_CHANGE_USER:
                        et_account.setText("");
                        et_pswd.setText("");
                        break;
                    default:
                        break;
                }

                break;
        }
    }

    private void openMainActivityAndFinish() {
        //打开主界面
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
