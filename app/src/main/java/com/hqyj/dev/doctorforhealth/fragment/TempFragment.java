package com.hqyj.dev.doctorforhealth.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hqyj.dev.doctorforhealth.DataBase.SharedPreferenceDB;
import com.hqyj.dev.doctorforhealth.R;
import com.hqyj.dev.doctorforhealth.Service.GetNodeInfoService;
import com.hqyj.dev.doctorforhealth.Service.GetWebInfoService;
import com.hqyj.dev.doctorforhealth.Service.Task;
import com.hqyj.dev.doctorforhealth.Web.JsonCommand;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Administrator on 2016/3/22.
 */
public class TempFragment extends Fragment{
    private String TAG = "TempFragment";
    private View view;
    private Button update,readData;
    private TextView temp,start,title;
    private MyBroadcastReceiver myBroadcastReceiver;
    private Map<String,String> map = new HashMap<String,String>();
    private String patient_id,patient_name;
    private int task_id,result_id,finished_items;
    private TimeTickThread5 mTimeThread5;
    private LinkedList ll = new LinkedList();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        Log.d(TAG,"temp11111111111111111111111111111111111111111111");
        view = inflater.inflate(R.layout.fragment_tempfragment, (ViewGroup)getActivity().findViewById(R.id.viewpager), false);

        title = (TextView)view.findViewById(R.id.tempUI);
        readData = (Button)view.findViewById(R.id.startTemp);
        update = (Button)view.findViewById(R.id.update);
        temp = (TextView)view.findViewById(R.id.temp);
        start = (TextView)view.findViewById(R.id.ready);
        update.setVisibility(View.GONE);

        readData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                start.setText("开始读取体温");
                GetNodeInfoService.newTask(new Task(getActivity(), Task.BT_START_TEMP, null));
            }
        });
        IntentFilter infilter = new IntentFilter();
        infilter.addAction("BT_READ_TEMP");
        infilter.addAction("BT_START_TEMP");
        infilter.addAction("PatientInfo");
        infilter.addAction("NET_WEB_SERVICE_GET_DATA_TEMP");
        infilter.addAction("BT_STOP_TEMP");
        myBroadcastReceiver = new MyBroadcastReceiver();
        getActivity().registerReceiver(myBroadcastReceiver,infilter);

        ViewGroup vp = (ViewGroup)view.getParent();
        if(vp != null){
            vp.removeAllViewsInLayout();
        }

        patient_id = SharedPreferenceDB.getPatientInfo(getActivity()).patient_id;
        task_id = SharedPreferenceDB.getPatientInfo(getActivity()).task_id;
        result_id = SharedPreferenceDB.getPatientInfo(getActivity()).result_id;
        finished_items = SharedPreferenceDB.getPatientInfo(getActivity()).finished_items;
        patient_name = SharedPreferenceDB.getPatientInfo(getActivity()).patient_name;
        map.put("patient_id",patient_id);
        map.put("task_id",String.valueOf(task_id));
        map.put("result_id",String.valueOf(result_id));
        map.put("finished_items", String.valueOf(finished_items));
        map.put("patient_name",patient_name);
        Message msg = new Message();
        msg.what = 2;
        msg.obj = map;
        handler.sendMessage(msg);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(myBroadcastReceiver);

    }

    private class MyBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("BT_START_TEMP")){
                boolean boo = intent.getBooleanExtra("result",false);
                if(boo){
                    GetNodeInfoService.newTask(new Task(getActivity(), Task.BT_READ_TEMP, null));
                }
            }
            if(action.equals("BT_READ_TEMP")){
                float f = intent.getFloatExtra("result",0)/10;
                ll.add(f);
                Log.d(TAG,f+" 摄氏度");
                if(f == -0.1){
                    Log.d(TAG,"体温采集出错");
                    return;
                }
                if(mTimeThread5 == null){
                    mTimeThread5 = new TimeTickThread5();
                    mTimeThread5.start();
                }
                if(f > 0.0f){
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = f;
                    handler.sendMessage(msg);
                }
            }
            if(action.equals("BT_STOP_TEMP")){
                getActivity().sendBroadcast(new Intent("disconnect"));
            }
            if(action.equals("NET_WEB_SERVICE_GET_DATA_TEMP")){
                String json = intent.getStringExtra("updataResult");
                Log.d(TAG,json);
                int id = intent.getIntExtra("id",0);
                if(json == null){
                    return;
                }
                JsonCommand.QueryResult qr = JsonCommand.getUploadResult(json);
                if(qr.code == 0){
                    handler.sendEmptyMessage(3);
                }
            }
        }
    }

    public Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case 1:
                    float f  = (float)msg.obj;
                    temp.setText(f+"℃");
                    temp.setTag(f+"");
                    break;
                case 2:
                    HashMap<String,String> map = (HashMap<String, String>) msg.obj;

                    final String patient_id = map.get("patient_id");
                    final String task_id = map.get("task_id");
                    final String result_id = map.get("result_id");
                    final int finished_items = Integer.parseInt(map.get("finished_items"));
                    String patient_name = map.get("patient_name");
                    Log.d(TAG,"id:"+patient_id+" "+task_id+" "+result_id+patient_name);
                    title.setText(patient_name+"的体温测量");
                    update.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
//                            GetNodeInfoService.newTask(new Task(getActivity(), Task.BT_STOP_TEMP, null));

                            String tp = temp.getTag().toString();
                            long date = System.currentTimeMillis();
                            String json = "{\"count\":1,\"resultList\":[{\"patient_id\":"+ patient_id+"" + ","+
                                    "\"patient_photo\":"+"\"\""+"," +
                                    "\"task_id\":"+task_id+"," +
                                    "\"finished_items\":"+(finished_items | (1 << 3))+"," +
                                    "\"result_id\":"+result_id+"," +
                                    "\"temperature\":"+"\""+tp+"\""+"," +
                                    "\"temperature_time\":\""+ date +"\"}]}";
                            Log.d(TAG, json);
                            Map<String,String> values = new HashMap<String, String>();
                            values.put("data",json);
                            GetWebInfoService.newTask(new Task(getActivity(), Task.NET_WEB_SERVICE_GET_DATA_TEMP, new Object[]{"uploadResult", values}));
                        }
                    });
                    break;
                case 3:
                    Toast.makeText(getActivity(),"上传成功",Toast.LENGTH_SHORT).show();
                    break;
                case 7:
                    float result = (float)msg.obj;
                    mTimeThread5.stopThread();
                    mTimeThread5 = null;

                    temp.setText(result+"℃");
                    temp.setTag(result+"");

                    update.setVisibility(View.VISIBLE);
                    GetNodeInfoService.newTask(new Task(getActivity(), Task.BT_STOP_TEMP, null));
                    break;
            }
        }
    };
    private class TimeTickThread5 extends Thread{
        private int mCount = 0;
        private boolean isStart = true;
        private float lastTemp = 0;

        public boolean stopThread(){
            isStart = false;
            return true;
        }

        public void run(){
            while(isStart){
                if(mCount++ == 50){
                    Message msg = new Message();
                    msg.what = 7;
                    for(int i = 0; i < 5; i++){
                        float f =  (Float) ll.getLast();
                        lastTemp = lastTemp + f;
                    }
                    msg.obj = lastTemp/5;
                    handler.sendMessage(msg);

                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
