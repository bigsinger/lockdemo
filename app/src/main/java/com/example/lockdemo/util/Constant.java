package com.example.lockdemo.util;

/**
 * Created by sing on 2017/1/16.
 */

public class Constant {
    //配置文件名
    public static final String CONFIG_NAME = "lockdemo";
    //是否开启手势密码
    public static final String ALP_SWITCH_ON = "alpswitchon";
    //手势密码
    public static final String ALP = "Alp";

    public static final String LOGIN_STATE = "loginstate";


    //超时时间(单位：s)，当界面隐藏后超过该时间则再次打开需要手势密码
    public static final int TIMEOUT_ALP = 3;

    //后台保存的起始时间
    public static final String START_TIME = "StartTime";


    public static final int ON_LOGIN_OK = 0;
    public static final int ON_LOGIN_FAILED = 1;

    //请求创建一个新的图案
    public static final int REQ_CREATE_PATTERN = 1;

    //比较已有的图案
    public static final int REQ_COMPARE_PATTERN = 2;
    //超时手势验证请求
    public static final int REQ_COMPARE_PATTERN_TIMEOUT_CHECK = 3;

    public final static int RESULT_FAILED = 10;
    public final static int RESULT_FORGOT_PATTERN = 11;
    public final static int RESULT_ERRORS = 12;
    public final static int RESULT_CHANGE_USER = 13;
    //修改图案
    public static final int CHANGEPSWD_RET_CODE = 14;
    public static final int REQ_COMPARE_PATTERN_CHECK = 15;
    public static final int REQ_COMPARE_PATTERN_CHANGE = 16;
}
