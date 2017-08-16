package com.hqyj.dev.doctorforhealth.fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hqyj.dev.doctorforhealth.DataBase.SharedPreferenceDB;
import com.hqyj.dev.doctorforhealth.PaintTools.ECGView;
import com.hqyj.dev.doctorforhealth.PaintTools.Utils;
import com.hqyj.dev.doctorforhealth.R;
import com.hqyj.dev.doctorforhealth.Service.GetNodeInfoService;
import com.hqyj.dev.doctorforhealth.Service.GetWebInfoService;
import com.hqyj.dev.doctorforhealth.Service.Task;
import com.hqyj.dev.doctorforhealth.Web.Base64;
import com.hqyj.dev.doctorforhealth.Web.JsonCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/3/29.
 */
public class EcgFragment extends Fragment{

    private String TAG = "EcgFragment";
    private View view;
    private TextView title;
    private EcgReceiver ecgReceiver;
    private Map<String,String> map = new HashMap<String,String>();
    private ECGView mView;
    private int delay;
    private Button ecgStart,ecgUpdate;
    private int mCount = 0;
    private String patient_id;
    private int task_id,finished_items,result_id;
    private JsonCommand.QueryResult queryResult;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG,"ecg11111111111111111111111111111111111111111111");
        view = inflater.inflate(R.layout.fragment_ecgfragment, (ViewGroup) getActivity().findViewById(R.id.viewpager),false);

        mView = (ECGView)view.findViewById(R.id.mView);
        delay = mView.getDrawDelay();

        title = (TextView) view.findViewById(R.id.ecgTitle);
        ecgStart = (Button) view.findViewById(R.id.ecgstart);
        ecgUpdate = (Button)view.findViewById(R.id.ecgupdate);
        ecgUpdate.setVisibility(View.GONE);
        ecgStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetNodeInfoService.newTask(new Task(getActivity(), Task.BT_START_ECG, null));
            }
        });

        IntentFilter infilter = new IntentFilter();
        infilter.addAction("PatientInfo");
        infilter.addAction("BT_START_ECG");
        infilter.addAction("BT_READ_ECG");
        infilter.addAction("NET_WEB_SERVICE_GET_DATA_ECG");
        infilter.addAction("BT_STOP_ECG");
        ecgReceiver = new EcgReceiver();
        getActivity().registerReceiver(ecgReceiver,infilter);

        task_id = SharedPreferenceDB.getPatientInfo(getActivity()).task_id;
        finished_items = SharedPreferenceDB.getPatientInfo(getActivity()).finished_items;
        patient_id = SharedPreferenceDB.getPatientInfo(getActivity()).patient_id;
        result_id = SharedPreferenceDB.getPatientInfo(getActivity()).result_id;
        map.put("patient_name",SharedPreferenceDB.getPatientInfo(getActivity()).patient_name);
        Message msg = new Message();
        msg.what = 1;
        msg.obj = map;
        handler.sendMessage(msg);



        return view;
    }
    private class EcgReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("BT_START_ECG")){
                if((boolean)intent.getBooleanExtra("result",false)){
                    GetNodeInfoService.newTask(new Task(getActivity(),Task.BT_READ_ECG,new Object[]{delay, mView}));
                }else{
                    Toast.makeText(getActivity(), "心电打开失败！", Toast.LENGTH_LONG).show();
                }
            }
            if(action.equals("BT_READ_ECG")){

                int data = intent.getIntExtra("result",0);
                if(data == 1){
                    GetNodeInfoService.newTask(new Task(getActivity(),Task.BT_STOP_ECG,null));
                }
                if(data != -1 || data != 1) {
                    if(mView.getTotalDataCount() <= mCount){

                        mView.destroyView();
                        int[] ecgdata= mView.getDataBuffer();
                        byte[] byteData = Utils.convertIntArrToByteArr(ecgdata);
                        String ecgdataStr = Base64.encodeBase64String(byteData);

                        String json= "{\"count\":1,\"resultList\":[{\"patient_id\":"+ patient_id+"" + ","+
                                "\"patient_photo\":"+"\"\""+"," +
                                "\"task_id\":"+task_id+"," +
                                "\"finished_items\":1," +
                                "\"result_id\":"+result_id+"," +
                                "\"ecg_data\":"+"\""+ecgdataStr+"\""+"," +
                                "\"ecg_time\":\""+ System.currentTimeMillis() +"\"}]}";
                        final Map<String, String> value = new HashMap<String, String>();
                        value.put("data", json);

                        ecgUpdate.setVisibility(View.VISIBLE);
                        ecgUpdate.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCount = 0;
                                GetNodeInfoService.newTask(new Task(getActivity(),Task.BT_STOP_ECG,null));
                                GetWebInfoService.newTask(new Task(getActivity(),Task.NET_WEB_SERVICE_GET_DATA_ECG,new Object[]{"uploadResult", value}));
                            }
                        });
                        return;
                    }
                    mCount++;
                }

            }
            if(action.equals("BT_STOP_ECG")){
                getActivity().sendBroadcast(new Intent("disconnect"));

            }
            if(action.equals("NET_WEB_SERVICE_GET_DATA_ECG")){
                String json = intent.getStringExtra("updataResult");
                if(json == null){
                    handler.sendEmptyMessage(4);
                    return;
                }
                queryResult = JsonCommand.getUploadResult(json);
                if(queryResult.code != 0){
                    handler.sendEmptyMessage(2);
                    return;
                }else{
                    handler.sendEmptyMessage(3);
                    return;
                }
            }
        }
    }
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    HashMap<String,String> map = (HashMap<String, String>) msg.obj;
                    String patient_name = map.get("patient_name");
                    title.setText(patient_name+"的心电测量");
                    break;
                case 2:
                    Toast.makeText(getActivity(), "结果上传失败：" + msg.obj.toString(), Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    Toast.makeText(getActivity(), "结果上传成功！", Toast.LENGTH_LONG).show();
                    break;
                case 4:
                    Toast.makeText(getActivity(), "结果上传失败！", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().unregisterReceiver(ecgReceiver);
    }
}
