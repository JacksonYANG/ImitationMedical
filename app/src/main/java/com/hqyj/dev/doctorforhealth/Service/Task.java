package com.hqyj.dev.doctorforhealth.Service;

import android.app.Activity;

/**
 * Created by Administrator on 2016/3/29.
 */
public class Task {
    //获取服务器信息，用来校验医师的登录名和密码
    public static final int NET_WEB_SERVICE_GET_DATA = 1;
    //向服务器上传体温数据
    public static final int NET_WEB_SERVICE_GET_DATA_TEMP = 2;
    //从服务器上获取体温数据
    public static final int NET_WEB_SERVICE_GET_DATA_TEMP_HIS = 3;
    //向服务器上传心电数据
    public static final int NET_WEB_SERVICE_GET_DATA_ECG = 4;
    //从服务器上获取心电数据
    public static final int NET_WEB_SERVICE_GET_DATA_ECG_HIS = 5;
    //向服务器上传血压数据
    public static final int NET_WEB_SERVICE_GET_DATA_BP = 6;
    //从服务器上获取血压数据
    public static final int NET_WEB_SERVICE_GET_DATA_BP_HIS = 7;
    //开始读取体温
    public static final int BT_START_TEMP = 8;
    //读取体温
    public static final int BT_READ_TEMP = 9;
    //停止读取体温
    public static final int BT_STOP_TEMP = 10;
    //开始读取心电
    public static final int BT_START_ECG = 11;
    //读取心电
    public static final int BT_READ_ECG = 12;
    //停止读取心电
    public static final int BT_STOP_ECG = 13;
    //开始读取血压
    public static final int BT_START_BP = 14;
    //读取血压
    public static final int BT_READ_BP = 15;
    //停止读取血压
    public static final int BT_STOP_BP = 16;
    //通过长条码来查找患者信息
    public static final int BT_READ_BAR_CODE = 17;

    private int Id;
    public Object[] params;
    public Object result;
    public boolean isTimeOut = false;
    private Activity activity;
    /**
     * 任务构造器
     * @param context 文本
     * @param taskId 任务ID
     * @param params 任务参数
     */
    public Task(Activity context, int taskId, Object[] params){
        this.Id = taskId;
        this.params = params;
        activity = context;
    }
    //返回执行当前任务的Activity
    public Activity getActivity(){
        return activity;
    }
    //得到任务ID
    public int getTaskId() {
        return this.Id;
    }
}
