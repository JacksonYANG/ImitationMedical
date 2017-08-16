package com.hqyj.dev.doctorforhealth.Service;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;


import com.hqyj.dev.doctorforhealth.Web.SoapWeb;
import com.hqyj.dev.doctorforhealth.activity.CallBack;

import java.util.ArrayList;
import java.util.Map;


/**
 * Created by Administrator on 2016/3/21.
 */
public class GetWebInfoService extends Service implements Runnable{
    private static String TAG = "GetWebInfoService";
    private Thread thread;
    private static ArrayList<Task> tasklist = new ArrayList<Task>();
    private boolean isRun = true;

    @Override
    public void onCreate() {//开启任务线程
        super.onCreate();
        Log.d(TAG, "onCreate()");
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        if(thread == null){
            thread = new Thread(this);
            thread.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        thread.interrupt();
        thread = null;
        isRun = false;
    }
    //新增任务
    public static void newTask(Task task){
        Log.d(TAG, "newTask()");
        for(Task t : tasklist){
            if(t.getActivity() == task.getActivity() && t.getTaskId() == task.getTaskId()){
                Toast.makeText(task.getActivity(),"任务已存在，请稍后...",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        tasklist.add(task);
    }
    @Override
    public void run() {//执行任务线程
        while(isRun){
            if(tasklist.size() > 0){
                Task task = tasklist.get(0);
                startTask(task);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }
    }
    //具体任务执行的地方
    private void startTask(Task task){
        switch (task.getTaskId()){
            case Task.NET_WEB_SERVICE_GET_DATA:
                task.result = SoapWeb.CallSoapWeb(task.getActivity(),(String)task.params[0],(Map<String,String>)task.params[1]);
                break;
            case Task.NET_WEB_SERVICE_GET_DATA_TEMP:
                task.result = SoapWeb.CallSoapWeb(task.getActivity(),(String)task.params[0],(Map<String,String>)task.params[1]);
                break;
            case Task.NET_WEB_SERVICE_GET_DATA_TEMP_HIS:
                Log.d(TAG,"startTask()");
                task.result = SoapWeb.CallSoapWeb(task.getActivity(),(String)task.params[0],(Map<String,String>)task.params[1]);
                break;
            case Task.NET_WEB_SERVICE_GET_DATA_ECG:
                task.result = SoapWeb.CallSoapWeb(task.getActivity(),(String)task.params[0],(Map<String,String>)task.params[1]);
                break;
            case Task.NET_WEB_SERVICE_GET_DATA_ECG_HIS:
                task.result = SoapWeb.CallSoapWeb(task.getActivity(),(String)task.params[0],(Map<String,String>)task.params[1]);
                break;
            case Task.NET_WEB_SERVICE_GET_DATA_BP:
                task.result = SoapWeb.CallSoapWeb(task.getActivity(),(String)task.params[0],(Map<String,String>)task.params[1]);
                break;
            case Task.NET_WEB_SERVICE_GET_DATA_BP_HIS:
                task.result = SoapWeb.CallSoapWeb(task.getActivity(), (String) task.params[0], (Map<String, String>) task.params[1]);
                break;

        }
        Message msg = new Message();
        msg.what = task.getTaskId();
        msg.obj = task;
        handler.sendMessage(msg);

        tasklist.remove(task);
    }
    //任务完成之后处理数据额地方
    public Handler handler = new Handler(){
        public void handleMessage(Message msg){
            Log.d(TAG,"handler");
            switch (msg.what){
                case Task.NET_WEB_SERVICE_GET_DATA:
                    Task task = (Task) msg.obj;
                    Log.d(TAG,task.result.toString());
                    Activity activity = task.getActivity();
                    CallBack cb = (CallBack) activity;
                    cb.DataHandler(task);
                    break;
                case Task.NET_WEB_SERVICE_GET_DATA_TEMP:
                    Task t = (Task)msg.obj;
                    Intent intent = new Intent("NET_WEB_SERVICE_GET_DATA_TEMP");
                    intent.putExtra("updataResult",(String)t.result);
                    intent.putExtra("id",t.getTaskId());
                    sendBroadcast(intent);
                    break;
                case Task.NET_WEB_SERVICE_GET_DATA_TEMP_HIS:
                    Log.d(TAG,"NET_WEB_SERVICE_GET_DATA_TEMP_HIS");
                    Task th = (Task)msg.obj;
                    Intent it = new Intent("NET_WEB_SERVICE_GET_DATA_TEMP_HIS");
                    it.putExtra("updataResult",(String)th.result);
                    it.putExtra("id",th.getTaskId());
                    sendBroadcast(it);
                    break;
                case Task.NET_WEB_SERVICE_GET_DATA_ECG:
                    Log.d(TAG,"NET_WEB_SERVICE_GET_DATA_ECG");
                    Task tk = (Task) msg.obj;
                    Intent Itk = new Intent("NET_WEB_SERVICE_GET_DATA_ECG");
                    Itk.putExtra("updataResult",(String)tk.result);
                    Itk.putExtra("id",tk.getTaskId());
                    sendBroadcast(Itk);
                    break;
                case Task.NET_WEB_SERVICE_GET_DATA_ECG_HIS:
                    Log.d(TAG,"NET_WEB_SERVICE_GET_DATA_ECG_HIS");
                    Task as = (Task)msg.obj;
                    Intent in = new Intent("NET_WEB_SERVICE_GET_DATA_ECG_HIS");
                    in.putExtra("updataResult",(String)as.result);
                    in.putExtra("id",as.getTaskId());
                    sendBroadcast(in);
                    break;
                case Task.NET_WEB_SERVICE_GET_DATA_BP:
                    Log.d(TAG,"NET_WEB_SERVICE_GET_DATA_BP");
                    Task taskbphis = (Task) msg.obj;
                    Intent intentbphis = new Intent("NET_WEB_SERVICE_GET_DATA_BP");
                    intentbphis.putExtra("updataResult", (String) taskbphis.result);
                    intentbphis.putExtra("id",taskbphis.getTaskId());
                    sendBroadcast(intentbphis);
                    break;
                case Task.NET_WEB_SERVICE_GET_DATA_BP_HIS:
                    Log.d(TAG,"NET_WEB_SERVICE_GET_DATA_BP_HIS");
                    Task taskbp = (Task) msg.obj;
                    Intent intentbp = new Intent("NET_WEB_SERVICE_GET_DATA_BP_HIS");
                    intentbp.putExtra("updataResult", (String) taskbp.result);
                    intentbp.putExtra("id",taskbp.getTaskId());
                    sendBroadcast(intentbp);
                    break;
            }
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
