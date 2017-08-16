package com.hqyj.dev.doctorforhealth.fragment;

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
import com.hqyj.dev.doctorforhealth.NodeInfo.BPNode;
import com.hqyj.dev.doctorforhealth.R;
import com.hqyj.dev.doctorforhealth.Service.GetNodeInfoService;
import com.hqyj.dev.doctorforhealth.Service.GetWebInfoService;
import com.hqyj.dev.doctorforhealth.Service.Task;
import com.hqyj.dev.doctorforhealth.Web.JsonCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/3/29.
 */
public class BPFragment extends Fragment{

    private String TAG = "BPFragment";
    private View view;
    private TextView title,textViewTitle,textViewResult;
    private BPReceiver bpReceiver;
    private Map<String,String> map = new HashMap<String,String>();
    private Button bpstart,bpupdate;
    private String patient_id;
    private int task_id,result_id;
    private JsonCommand.QueryResult re;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG,"bp11111111111111111111111111111111111111111111");
        view = inflater.inflate(R.layout.fragment_bpfragment, (ViewGroup) getActivity().findViewById(R.id.viewpager),false);
        title = (TextView) view.findViewById(R.id.bpTitle);

        textViewResult = (TextView) view.findViewById(R.id.textViewResult);
        textViewTitle = (TextView)view.findViewById(R.id.textViewTitle);
        bpstart = (Button)view.findViewById(R.id.bpStart);
        bpupdate = (Button)view.findViewById(R.id.bpupdate);
        bpupdate.setVisibility(View.GONE);
        bpstart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetNodeInfoService.newTask(new Task(getActivity(),Task.BT_START_BP,null));
            }
        });

        IntentFilter infilter = new IntentFilter();
        infilter.addAction("PatientInfo");
        infilter.addAction("BT_START_BP");
        infilter.addAction("BT_READ_BP");
        infilter.addAction("NET_WEB_SERVICE_GET_DATA_BP");
        infilter.addAction("BT_STOP_BP");
        bpReceiver = new BPReceiver();
        getActivity().registerReceiver(bpReceiver,infilter);

        patient_id = SharedPreferenceDB.getPatientInfo(getActivity()).patient_id;
        task_id = SharedPreferenceDB.getPatientInfo(getActivity()).task_id;
        result_id = SharedPreferenceDB.getPatientInfo(getActivity()).result_id;
        map.put("patient_name",SharedPreferenceDB.getPatientInfo(getActivity()).patient_name);
        Message msg = new Message();
        msg.what = 1;
        msg.obj = map;
        handler.sendMessage(msg);



        return view;
    }
    private class BPReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals("BT_START_BP")){
                if((Boolean)intent.getBooleanExtra("result",false)){
                    // 读取血压数据
                    GetNodeInfoService.newTask(new Task(getActivity(), Task.BT_READ_BP, null));
                }else{
                    Toast.makeText(getActivity(), "血压计打开失败！", Toast.LENGTH_LONG).show();
                }
            }
            if(action.equals("BT_READ_BP")){
                int[] result = intent.getIntArrayExtra("result");
                if (result == null) {
                    Log.e(TAG, "数据采集出错！");
                    return;
                }
                Message msg;
                switch(result[BPNode.BPData.OFT_RS]){
                    case BPNode.DATA_RTN_AWEAK:
                    case BPNode.DATA_RTN_SLEEP:
                    case BPNode.DATA_RTN_STOP:
                        break;
                    // 返回当前血压结果
                    case BPNode.DATA_RTN_START:
                        msg = new Message();
                        msg.what = 11;
                        msg.arg1 = result[BPNode.BPData.OFT_CP];
                        msg.arg2 = result[BPNode.BPData.OFT_HT];
                        handler.sendMessage(msg);
                        break;
                    // 返回最终血压结果
                    case BPNode.DATA_RTN_RESULT:
                        msg = new Message();
                        msg.what = 12;
                        msg.arg1 = result[BPNode.BPData.OFT_SP];
                        msg.arg2 = result[BPNode.BPData.OFT_DP];
                        String r =  "收缩压：" + msg.arg1 + "\n舒张压：" + msg.arg2 + "\n心率：" +
                                result[BPNode.BPData.OFT_PR];
                        if(result[BPNode.BPData.OFT_AT] == 1)
                            r += "\n心率不齐";
                        msg.obj = r;
                        handler.sendMessage(msg);
                        break;
                    // 返回错误号
                    case BPNode.DATA_RTN_ERROR:
                        msg = new Message();
                        msg.what = 13;
                        String rt =  BPNode.getErrorStr(result[BPNode.BPData.OFT_EN]);
                        msg.obj = rt;
                        handler.sendMessage(msg);
                        break;
                }
            }
            if(action.equals("BT_STOP_BP")){
                getActivity().sendBroadcast(new Intent("disconnect"));

            }
            if(action.equals("NET_WEB_SERVICE_GET_DATA_BP")){
                String json = intent.getStringExtra("updataResult");
                Log.d(TAG,"json "+json);
                if(json == null){
                    handler.sendEmptyMessage(4);
                    return;
                }
                re = JsonCommand.getUploadResult(json);
                if(re.code != 0){
                    Message msg = new Message();
                    msg.what = 2;
                    msg.obj = re.errStr;
                    handler.sendMessage(msg);
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
                    title.setText(patient_name+"的血压测量");
                    break;
                case 2:
                    Toast.makeText(getActivity(), "结果上传失败：" + msg.obj.toString(), Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    Toast.makeText(getActivity(), "结果上传成功！", Toast.LENGTH_LONG).show();
                    break;
                case 4:
                    Toast.makeText(getActivity(), "结果上传失败", Toast.LENGTH_LONG).show();
                    break;
                case 11:
                    textViewTitle.setText("实时压力数据:");
                    String str = msg.arg1 + " Hg/mm";
                    textViewResult.setText(str);
                    break;
                case 12:

                    int hp = msg.arg1;
                    int lp = msg.arg2;
                    String json= "{\"count\":1,\"resultList\":[{\"patient_id\":"+ patient_id+"" + ","+
                            "\"patient_photo\":"+"\"\""+"," +
                            "\"task_id\":"+task_id+"," +
                            "\"finished_items\":"+(1 << 1)+"," +
                            "\"result_id\":"+result_id+"," +
                            "\"hp_data\":"+"\""+hp+"\""+"," +
                            "\"lp_data\":"+"\""+lp+"\""+"," +
                            "\"bloodpress_time\":\""+ System.currentTimeMillis() +"\"}]}";
                    final Map<String, String> value = new HashMap<String, String>();
                    value.put("data", json);

                    textViewTitle.setText("血压采集完毕：");
                    textViewResult.setText(msg.obj.toString());
                    bpupdate.setVisibility(View.VISIBLE);
                    bpupdate.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            GetNodeInfoService.newTask(new Task(getActivity(), Task.BT_STOP_BP, null));

                            GetWebInfoService.newTask(new Task(getActivity(),Task.NET_WEB_SERVICE_GET_DATA_BP,new Object[]{"uploadResult", value}));
                        }
                    });
                    break;
                case 13:
                    textViewTitle.setText("血压采集出错：");
                    textViewResult.setText(msg.obj + "");
                    GetNodeInfoService.newTask(new Task(getActivity(), Task.BT_STOP_BP, null));

                    break;
            }
        }
    };
    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(bpReceiver);
    }
}
