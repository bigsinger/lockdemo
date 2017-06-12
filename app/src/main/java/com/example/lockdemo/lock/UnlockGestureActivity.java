package com.example.lockdemo.lock;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lockdemo.App;
import com.example.lockdemo.R;
import com.example.lockdemo.lock.LockPatternView.Cell;
import com.example.lockdemo.util.Constant;
import com.example.lockdemo.util.Util;
import com.example.lockdemo.view.BaseActivity;

import java.util.List;

public class UnlockGestureActivity extends BaseActivity {
    private static final String TAG = "UnlockGPActivity";
    private LockPatternView mLockPatternView;
    private int mFailedPatternAttemptsSinceLastTimeout = 0;
    private CountDownTimer mCountdownTimer = null;
    private TextView mUserName;
    private TextView mErrorTips;
    Runnable attemptLockout = new Runnable() {

        @Override
        public void run() {
            mLockPatternView.clearPattern();
            mLockPatternView.setEnabled(false);
            mCountdownTimer = new CountDownTimer(LockPatternUtils.FAILED_ATTEMPT_TIMEOUT_MS + 1, 1000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    int secondsRemaining = (int) (millisUntilFinished / 1000) - 1;
                    if (secondsRemaining > 0) {
                        mErrorTips.setText(secondsRemaining + " 秒后重试");
                    } else {
                        mErrorTips.setText("请绘制手势密码");
                        mErrorTips.setTextColor(Color.WHITE);
                    }

                }

                @Override
                public void onFinish() {
                    mLockPatternView.setEnabled(true);
                    mFailedPatternAttemptsSinceLastTimeout = 0;
                }
            }.start();
        }
    };
    private Animation mShakeAnim;
    private TextView mTextViewForget;
    private TextView mChangeUser;
    private String mALP;
    private boolean isLogin = true;
    private Toast mToast;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Constant.ON_LOGIN_OK:
                    //登录成功
                    Bundle bundle = new Bundle();
                    // 打开新的Activity
                    Intent intent = new Intent();
                    intent.putExtras(bundle);
                    UnlockGestureActivity.this.setResult(RESULT_OK, intent);
                    finish();
                    break;
                case Constant.ON_LOGIN_FAILED:
                    Util.toast(UnlockGestureActivity.this, "登录失败，错误：xxx");
                    break;
                default:
                    break;
            }
        }
    };
    private Runnable mClearPatternRunnable = new Runnable() {
        public void run() {
            mLockPatternView.clearPattern();
        }
    };
    protected LockPatternView.OnPatternListener mChooseNewLockPatternListener = new LockPatternView.OnPatternListener() {

        public void onPatternStart() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
            patternInProgress();
        }

        public void onPatternCleared() {
            mLockPatternView.removeCallbacks(mClearPatternRunnable);
        }

        public void onPatternDetected(List<LockPatternView.Cell> pattern) {
            if (pattern == null)
                return;
            if (App.getInstance().getLockPatternUtils().checkPattern(pattern, mALP)) {
                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Correct);
//                showToast("解锁成功");
                Intent intent = new Intent();
                UnlockGestureActivity.this.setResult(RESULT_OK, intent);
                finish();
            } else {
                mLockPatternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                if (pattern.size() >= LockPatternUtils.MIN_PATTERN_REGISTER_FAIL) {
                    mFailedPatternAttemptsSinceLastTimeout++;
                    int retry = LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT - mFailedPatternAttemptsSinceLastTimeout;
                    if (retry >= 0) {
                        if (retry == 0) {
                            Util.toast(UnlockGestureActivity.this, "您已 5 次输错密码，请重新登录");
                            Intent intent = new Intent();
                            UnlockGestureActivity.this.setResult(Constant.RESULT_FAILED, intent);
                            finish();
                        } else {
                            mErrorTips.setText("密码错误，还可以再输入" + retry + "次");
                            mErrorTips.setTextColor(Color.RED);
                            mErrorTips.startAnimation(mShakeAnim);
                        }
                    }

                } else {
                    Util.toast(UnlockGestureActivity.this, "输入长度不够(至少4个点)，请重试");
                }

                if (mFailedPatternAttemptsSinceLastTimeout >= LockPatternUtils.FAILED_ATTEMPTS_BEFORE_TIMEOUT) {
//                    mHandler.postDelayed(attemptLockout, 200); //重试倒计时
                } else {
                    mLockPatternView.postDelayed(mClearPatternRunnable, 2000);
                }
            }
        }

        public void onPatternCellAdded(List<Cell> pattern) {

        }

        private void patternInProgress() {
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.gesture_unlock);

        mLockPatternView = (LockPatternView) this.findViewById(R.id.gesturepwd_unlock_lockview);
        mLockPatternView.setOnPatternListener(mChooseNewLockPatternListener);
        mLockPatternView.setTactileFeedbackEnabled(true);
        mUserName = (TextView) findViewById(R.id.gesturepwd_unlock_text);
        mTextViewForget = (TextView) findViewById(R.id.gesturepwd_unlock_forget);
        mErrorTips = (TextView) findViewById(R.id.gesturepwd_unlock_failtip);
        mChangeUser = (TextView) findViewById(R.id.gesturepwd_change_user);
        mShakeAnim = AnimationUtils.loadAnimation(this, R.anim.shake_x);

        mErrorTips.setText("请绘制手势密码");
        mErrorTips.setTextColor(Color.WHITE);
        mUserName.setText("account");

        mChangeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.getInstance().getLockPatternUtils().clearLock();
                Intent intent = new Intent();
                UnlockGestureActivity.this.setResult(Constant.RESULT_CHANGE_USER, intent);
                finish();
            }
        });

        mTextViewForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                App.getInstance().getLockPatternUtils().clearLock();
                Intent intent = new Intent();
                UnlockGestureActivity.this.setResult(Constant.RESULT_FORGOT_PATTERN, intent);
                finish();
            }
        });

        //异常
        try {
            Intent intent = getIntent();
            mALP = intent.getStringExtra("pattern");
            if (mALP == null)
                throw new RuntimeException("error");
            isLogin = intent.getBooleanExtra("login", true);
        } catch (Exception e) {
            App.getInstance().getLockPatternUtils().clearLock();
            Intent intent = new Intent();
            UnlockGestureActivity.this.setResult(Constant.RESULT_ERRORS, intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCountdownTimer != null)
            mCountdownTimer.cancel();
        if (handler != null)
            handler.removeCallbacksAndMessages(null);
    }

}
