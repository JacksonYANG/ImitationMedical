package com.hqyj.dev.doctorforhealth.CustomTools;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by Administrator on 2016/5/6.
 */
public class WaitingDialog {
    private String TAG = "WaitingDialog";
    private ProgressDialog progressDialog;
    private boolean isRun = true;
    private MyThread myThread;
    private Context context;

    public WaitingDialog(Context context1){
        this.context = context1;
    }
    private ProgressDialog getProgressDialog(){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(context);
        }
        return progressDialog;
    }
    public void CustomDialog(String title,String msg){
        Log.d(TAG,"CustomDialog()");
        isRun = true;
        Log.d(TAG, String.valueOf(isRun));
        getProgressDialog().setTitle(title);
        getProgressDialog().setMessage(msg);
        getProgressDialog().setCancelable(true);
        getProgressDialog().show();
        myThread = new MyThread();
        myThread.start();

    }
    public void dismiss(){
        getProgressDialog().dismiss();
        if(progressDialog != null){
            progressDialog = null;
            Log.d(TAG,"progressDialog == null");
        }
        Log.d(TAG,"dismiss()");
        if(myThread != null && myThread.isAlive()){
            myThread = null;
            isRun = false;
            Log.d(TAG,"isRun == false");
        }
    }
    private class MyThread extends Thread{
        @Override
        public void run() {
            Log.d(TAG,"run()");
            int i = 8;
            while(isRun && i-- > 0){
                try {
                    Log.d(TAG,"sleep()");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            handler.sendEmptyMessage(1);
        }
    }
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case 1:
                    Log.d(TAG,"handler()");
                    dismiss();
                    break;
            }
        }
    };
}
