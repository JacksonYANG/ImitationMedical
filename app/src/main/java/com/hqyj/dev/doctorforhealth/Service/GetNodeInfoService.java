package com.hqyj.dev.doctorforhealth.Service;


import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothSocket;

import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.hqyj.dev.doctorforhealth.Bluetooth.BluetoothTools;
import com.hqyj.dev.doctorforhealth.NodeInfo.BPNode;
import com.hqyj.dev.doctorforhealth.NodeInfo.BarNode;
import com.hqyj.dev.doctorforhealth.NodeInfo.ECGNode;
import com.hqyj.dev.doctorforhealth.NodeInfo.TempNode;
import com.hqyj.dev.doctorforhealth.PaintTools.ECGView;
import com.hqyj.dev.doctorforhealth.activity.CallBack;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 *
 * Created by Administrator on 2016/3/21.
 */
public class GetNodeInfoService extends Service implements Runnable{
    private String TAG = "GetNodeInfoService";
    private BluetoothTools btTools;
    private BluetoothSocket mSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private BluetoothSocket socket;
    private boolean isRun = true;
    private Thread thread;
    private static ArrayList<Task> taskList = new ArrayList<Task>();
    private TempNode tempNode;
    private ECGNode ecgNede;
    private BPNode bloodPressNode;
    private BarNode barNode;
    private DataReadLooper mEcgDataReadLooper;
    private DataReadLooper mBPDataReadLooper;
    private DataReadLooper mTempDataReadLooper;
    @Override
    public void onCreate() {//初始化，启动任务线程
        super.onCreate();
        btTools = BluetoothTools.getmBluetoothTools(this);
        tempNode = new TempNode();
        ecgNede = new ECGNode();
        bloodPressNode = new BPNode();
        barNode = new BarNode();
        mInputStream = btTools.getmInputStream();
        mOutputStream = btTools.getmOutputStream();
        mSocket = btTools.getmBluetoothSocket();
        socket = btTools.getmBluetoothSocket();
        if(socket == null){
            Log.d(TAG, "socket is null");
        }else{
            Log.d(TAG,"socket not null");
        }
        Log.d(TAG,"oncreate12");
        if(mInputStream == null || mOutputStream == null){
            Log.d(TAG, "in out is null");
        }else{
            Log.d(TAG,"in out not null");
        }
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");
        if(thread == null){
            Log.d(TAG,"onStartCommand1");
            thread = new Thread(this);
            thread.start();
        }
        Log.d(TAG,"onStartCommand2");
        return super.onStartCommand(intent, flags, startId);
    }
    //接受任务，存入列表
    public static void newTask(Task task){
        for(Task t : taskList){
            if(t.getActivity() == task.getActivity() && t.getTaskId() == task.getTaskId()){
                Toast.makeText(task.getActivity(),"任务已存在",Toast.LENGTH_SHORT).show();
                return;
            }
        }
        taskList.add(task);
    }
    @Override
    public void run() {//执行任务线程
        Log.d(TAG,"run()");
        while(isRun){

            Task task = null;

            if(taskList.size() > 0){
                Log.d(TAG,"run()1");
                task = taskList.get(0);
                startTask(task);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }
    }
    //具体执行任务的地方
    private void startTask(Task task){
        int delay = 200;
        Log.d(TAG,"startTask");
        switch(task.getTaskId()){
            case Task.BT_START_TEMP:
                Log.d(TAG,"开始读取体温");
                if(mOutputStream == null){
                    Log.d(TAG,"out is null");
                    task.result = false;
                    break;
                }
                task.result = tempNode.startRead(mOutputStream);
                Log.d(TAG,task.result.toString()+"start");
                break;
            case Task.BT_READ_TEMP:
                Log.d(TAG,"读取体温数据");

                if(mTempDataReadLooper != null && mTempDataReadLooper.isAlive()){
                    Log.d(TAG,"TempDataReadLooper is alive");
                    break;
                }
                mTempDataReadLooper = new DataReadLooper(task,200);
                mTempDataReadLooper.setName("TempDataReadLooper");
                mTempDataReadLooper.start();

                break;
            case Task.BT_STOP_TEMP:
                Log.d(TAG,"停止读取体温");
                if(mTempDataReadLooper != null && mTempDataReadLooper.isAlive()){
                    mTempDataReadLooper.isRun = false;
                    mTempDataReadLooper.interrupt();
                    mTempDataReadLooper = null;

                    task.result = tempNode.stopRead(mOutputStream);
                }
                Log.d(TAG,"stop");
                break;
            case Task.BT_START_ECG:
                Log.d(TAG,"开始心电采集");
                if(mOutputStream == null){
                    Log.d(TAG,"out is null");
                    task.result = false;
                    break;
                }

                task.result = ecgNede.startRead(mOutputStream);

                Log.d(TAG,"break start ecg");
                break;
            case Task.BT_READ_ECG:
                Log.d(TAG,"读取心电数据");
                if(task.params != null && (Integer)task.params[0] > 0){
                    delay = (Integer)task.params[0];
                }
                if(mEcgDataReadLooper != null && mEcgDataReadLooper.isAlive()){
                    break;
                }
                Log.d(TAG,"开始心电数据采集");
                mEcgDataReadLooper = new DataReadLooper(task,delay);
                mEcgDataReadLooper.setName("EcgDataReadLooper");
                mEcgDataReadLooper.start();
                break;
            case Task.BT_STOP_ECG:
                Log.d(TAG,"停止心电数据采集");
                if(mEcgDataReadLooper != null && mEcgDataReadLooper.isAlive()){
                    mEcgDataReadLooper.isRun = false;
                    mEcgDataReadLooper.interrupt();
                    mEcgDataReadLooper = null;

                    task.result = ecgNede.stopRead(mOutputStream);
                }
                break;
            case Task.BT_START_BP:
                Log.d(TAG,"开始血压采集");
                if(mSocket == null && (!mSocket.isConnected())){
                    Log.d(TAG,"Socket is null");
                    task.result = false;
                    break;
                }
                try {
                    task.result = bloodPressNode.startBP(mSocket);
                    Log.d(TAG, String.valueOf(task.result));
                } catch (IOException e) {
                    Log.d(TAG,"catch error");
                    task.result = null;
                }
                break;
            case Task.BT_READ_BP:
                Log.d(TAG,"读取血压数据");
                if(mBPDataReadLooper != null && mBPDataReadLooper.isAlive()){
                    Log.d(TAG,"BPDataReadLooper is alive.");
                    break;
                }
                mBPDataReadLooper = new DataReadLooper(task, 200);
                mBPDataReadLooper.setName("BPDataReadLooper");
                mBPDataReadLooper.start();
                break;
            case Task.BT_STOP_BP:
                Log.d(TAG,"停止血压采集");
                if(mBPDataReadLooper != null && mBPDataReadLooper.isAlive()){
                    task.result = bloodPressNode.stopBP(mOutputStream);
                    mBPDataReadLooper.isRun =false;
                    mBPDataReadLooper.interrupt();
                    mBPDataReadLooper = null;

                }else{
                    Log.d(TAG,"BP read looper already stop.");
                }
                break;
            case Task.BT_READ_BAR_CODE:
                Log.d(TAG,"读取长条码");
                try {
                    Log.d(TAG,"fasffaf");
                    task.result = barNode.readBarCode(mInputStream);
                    Log.d(TAG,"asfasf");
                    Log.d(TAG,String.valueOf(task.result));
                } catch (IOException e) {
                    Log.d(TAG,"adc00");
                    task.result = null;
                }
                break;
        }
        Log.d(TAG,"asdfafa44444");
        Message msg = new Message();
        msg.what = task.getTaskId();
        msg.obj = task;
        handler.sendMessage(msg);
        taskList.remove(task);
        Log.d(TAG, "asdfafa55555");
    }
    //任务完成之后进行处理的地方
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case Task.BT_START_TEMP:
                    Log.d(TAG,"BT_START_TEMP");
                    Task tasktemp = (Task)msg.obj;
                    boolean bo = (boolean)tasktemp.result;
                    Intent temp = new Intent("BT_START_TEMP");
                    temp.putExtra("result",bo);
                    sendBroadcast(temp);
                    break;

                case Task.BT_STOP_TEMP:

                    Intent intentt = new Intent("BT_STOP_TEMP");
                    sendBroadcast(intentt);
                    break;
                case Task.BT_START_ECG:
                    Task taks = (Task)msg.obj;

                    boolean b = (boolean) taks.result;
                    Log.d(TAG, String.valueOf(b)+"asdfasf");
                    Intent intentecg = new Intent("BT_START_ECG");
                    intentecg.putExtra("result", b);
                    sendBroadcast(intentecg);

                    break;
                case Task.BT_STOP_ECG:
                    Intent intentsecg = new Intent("BT_STOP_ECG");
                    sendBroadcast(intentsecg);
                    break;
                case Task.BT_START_BP:
                    Task taskbp = (Task)msg.obj;
                    boolean boo = (boolean)taskbp.result;
                    Intent intentbp = new Intent("BT_START_BP");
                    intentbp.putExtra("result", boo);
                    sendBroadcast(intentbp);
                    break;
                case Task.BT_STOP_BP:
                    Intent intentsbp = new Intent("BT_STOP_BP");
                    sendBroadcast(intentsbp);
                    break;
                case Task.BT_READ_BAR_CODE:
                    Task code = (Task) (msg.obj);
                    if (code == null)
                        return;
                    // 得到执行目标任务的Activity
                    Activity act = code.getActivity();
                    if (act == null)
                        return;
                    CallBack cb = (CallBack) act;
                    // 调用目标任务完成后需要Activity中UI线程需要做的工作
                    cb.DataHandler(code);
                    break;
            }
        }
    };
    //循环刦数据的线程
    private class DataReadLooper extends Thread{
        public boolean isRun = true;
        private Task mTask;
        private long mDelay;
        private boolean isRealTime = false;
        private long startTime;
        private int count = 0;

        public DataReadLooper(Task task,int delay){
            this.mTask = task;
            if(task.getTaskId() == Task.BT_READ_ECG){
                mDelay = 0;
            }else{
                mDelay = delay;
            }
        }

        @Override
        public void run() {
            while(isRun){
                if(mDelay > 0){
                    try {
                        Thread.sleep(mDelay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                switch(mTask.getTaskId()){
                    case Task.BT_READ_TEMP:

                        try {
                            mTask.result = tempNode.readData(mInputStream);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } catch (IOException e) {
                            Log.d(TAG,"IOException");
                            isRun = false;
                            mTempDataReadLooper = null;
                            mTask.result = null;
                        }

                        break;
                    case Task.BT_READ_ECG:
                        startTime = System.currentTimeMillis();
                        try {
                            Log.d(TAG,"Thread looper1");
                            int f = ecgNede.redECGData(mInputStream);
                            mTask.result = f;
                            Log.d(TAG, String.valueOf(mTask.result)+"sfa");
                            ECGView.mECGDataHolder.feedData(f);
                            Log.d(TAG,"Thread looper3");
                        } catch (IOException e) {
                            isRun = false;
                            Log.d(TAG, "Thread looper4");
                            mEcgDataReadLooper = null;
                            mTask.result = null;
                        }
                        break;
                    case Task.BT_READ_BP:
                        mTask.result = bloodPressNode.readBP();
                        break;
                }
                if(mTask.getTaskId() == Task.BT_READ_TEMP){
                    Log.d(TAG, "Thread looper36123");
                    Intent intent = new Intent("BT_READ_TEMP");
                    if(mTask.result != null) {
                        intent.putExtra("result", (float) mTask.result);
                    }
                    Log.d(TAG, "Thread looper5123");
                    sendBroadcast(intent);
                }
                if(mTask.getTaskId() == Task.BT_READ_ECG) {
                    Log.d(TAG, "Thread looper36");
                    Intent intent = new Intent("BT_READ_ECG");
                    intent.putExtra("result", (int) mTask.result);
                    Log.d(TAG, "Thread looper5");
                    sendBroadcast(intent);
                }
                if(mTask.getTaskId() == Task.BT_READ_BP){
                    Log.d(TAG, "Thread looper36453");
                    Intent intent = new Intent("BT_READ_BP");
                    intent.putExtra("result", (int[]) mTask.result);
                    Log.d(TAG, "Thread looper54354");
                    sendBroadcast(intent);
                }
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mEcgDataReadLooper = null;
        mBPDataReadLooper = null;
        mTempDataReadLooper = null;
        thread = null;
        isRun = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
