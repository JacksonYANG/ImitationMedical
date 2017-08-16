package com.hqyj.dev.doctorforhealth.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.view.Window;
import android.view.WindowManager;

import com.hqyj.dev.doctorforhealth.R;


/**
 * Created by Administrator on 2016/3/18.
 */
public class WelcomeActivity extends Activity {

    private MyThreadDelay mMyThreadDelay;
    private boolean onRun = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_welcome);
        onRun = true;
        mMyThreadDelay = new MyThreadDelay();
        mMyThreadDelay.start();
    }
    private class MyThreadDelay extends Thread{
        @Override
        public void run() {
            if(onRun){
                try {
                    Thread.sleep(1500);
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
                    onRun = false;
                    mMyThreadDelay.interrupt();;
                    mMyThreadDelay = null;
                    Intent intent = new Intent(WelcomeActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };
}
