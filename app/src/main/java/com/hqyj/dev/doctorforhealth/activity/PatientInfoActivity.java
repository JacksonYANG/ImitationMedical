package com.hqyj.dev.doctorforhealth.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hqyj.dev.doctorforhealth.CustomTools.WaitingDialog;
import com.hqyj.dev.doctorforhealth.DataBase.SharedPreferenceDB;
import com.hqyj.dev.doctorforhealth.R;
import com.hqyj.dev.doctorforhealth.Service.BluetoothConncetService;
import com.hqyj.dev.doctorforhealth.Service.GetNodeInfoService;
import com.hqyj.dev.doctorforhealth.Service.GetWebInfoService;
import com.hqyj.dev.doctorforhealth.Service.Task;
import com.hqyj.dev.doctorforhealth.Web.JsonCommand;
import com.hqyj.dev.doctorforhealth.fragment.BPFragment;
import com.hqyj.dev.doctorforhealth.fragment.BPHistoryFragment;
import com.hqyj.dev.doctorforhealth.fragment.EcgFragment;
import com.hqyj.dev.doctorforhealth.fragment.EcgHistoryFragment;
import com.hqyj.dev.doctorforhealth.fragment.TempFragment;
import com.hqyj.dev.doctorforhealth.fragment.TempHistoryFragment;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/3/29.
 */
public class PatientInfoActivity extends FragmentActivity implements CallBack {
    private String TAG = "PatientInfoActivity";
    private ViewPager mvp;
    private BPFragment bpfragment;
    private BPHistoryFragment bphfragment;
    private EcgFragment ecgfragment;
    private EcgHistoryFragment ecghfragment;
    private TempFragment tempfragment;
    private TempHistoryFragment temphfragment;
    private ArrayList<Fragment> fragmentlist;

    private ListView NameList;
    private TextView infodetail,namelist;
    private EditText SearchEditText;
    private TextView SearchButton;
    private ArrayList<String> patientnamelist = new ArrayList<String>();
    private ArrayAdapter<String> adapterlist;
    private MViewPagerAdapter mvpadapter;

    private static String temp="",press="",ecg="";
    private PBroadCastReceiver receiver;

    private WaitingDialog waitingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_patientinfo_1);

//        waitingDialog = new WaitingDialog(PatientInfoActivity.this);

        initPatientInfo();


        Log.d("PatientInfo", "onCreate()");

        SearchEditText = (EditText) findViewById(R.id.serchET);

        SearchEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG,"adfsfasfafasfsd");
                stopService(new Intent(PatientInfoActivity.this,GetNodeInfoService.class));
                sendBroadcast(new Intent("connectsm"));
            }
        });

        SearchButton = (TextView)findViewById(R.id.seachbyname);

        mvp = (ViewPager) findViewById(R.id.viewpager);
        fragmentlist = new ArrayList<Fragment>();
        bpfragment = new BPFragment();
        bphfragment = new BPHistoryFragment();
        ecgfragment = new EcgFragment();
        ecghfragment = new EcgHistoryFragment();
        tempfragment = new TempFragment();
        temphfragment = new TempHistoryFragment();

        Log.d("PatientInfo", "onCreate()1");

        mvpadapter = new MViewPagerAdapter(getSupportFragmentManager(), fragmentlist);
        mvp.setAdapter(mvpadapter);

        Log.d("PatientInfo", "onCreate()2");

        NameList = (ListView)findViewById(R.id.patientList);
        infodetail = (TextView)findViewById(R.id.patientInfo);
        adapterlist = new ArrayAdapter<String>(PatientInfoActivity.this,android.R.layout.simple_expandable_list_item_1,patientnamelist);
        NameList.setAdapter(adapterlist);

        IntentFilter infilter = new IntentFilter();
        infilter.addAction("successsm");
        infilter.addAction("successblue");
        receiver = new PBroadCastReceiver();
        registerReceiver(receiver,infilter);



//        namelist = (TextView)findViewById(R.id.namelist);
//        namelist.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                stopService(new Intent(PatientInfoActivity.this,GetNodeInfoService.class));
//                sendBroadcast(new Intent("connectblue"));
//
//                waitingDialog.CustomDialog("正在连接蓝牙模块","Loading...");
//
//                Log.d(TAG,"Loading...");
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"onStart()");
        startService(new Intent(PatientInfoActivity.this,BluetoothConncetService.class));
        startService(new Intent(PatientInfoActivity.this,GetWebInfoService.class));
        startService(new Intent(PatientInfoActivity.this, GetNodeInfoService.class));

    }


    private class PBroadCastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("successsm")){

                startService(new Intent(PatientInfoActivity.this, GetNodeInfoService.class));

                GetNodeInfoService.newTask(new Task(PatientInfoActivity.this,Task.BT_READ_BAR_CODE,null));

            }
            if(action.equals("successblue")){
                Log.d(TAG,"shoudao");

                startService(new Intent(PatientInfoActivity.this, GetNodeInfoService.class));
                waitingDialog.dismiss();
            }
        }
    }

    public void initPatientInfo(){

        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
        Date currentdate = new Date(System.currentTimeMillis());
        String currentDate = dateformat.format(currentdate);
        String doctor_id = SharedPreferenceDB.getLoginUser(this).doctor_id;
        Map<String,String> values = new HashMap<String,String>();
        values.put("doctor_id",doctor_id);
        values.put("currentDate",currentDate);
        Task task = new Task(this,Task.NET_WEB_SERVICE_GET_DATA,new Object[]{"getTaskList",values});
        GetWebInfoService.newTask(task);

    }

    @Override
    public void DataHandler(Task task) {

        switch(task.getTaskId()){
            case Task.NET_WEB_SERVICE_GET_DATA:
                String json = (String)task.result;
                JsonCommand.DoctorTask taskList = new JsonCommand.DoctorTask();
                Log.d("PatientInfo",json);
                if(json != null && (taskList = JsonCommand.getTaskList(json))!=null){


                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = taskList;
                    handler.sendMessage(msg);

                } else{
                    Message msg = new Message();
                    msg.what = 2;
                    msg.obj = "服务访问失败";
                    handler.sendMessage(msg);
                }
                break;
            case Task.BT_READ_BAR_CODE:
                String code = (String) task.result;
                if (code != null && !"".equals(code)) {
                    Message msg = new Message();
                    msg.what = 3;
                    msg.obj = code;
                    handler.sendMessage(msg);
                }
                break;
        }

    }
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){

            switch (msg.what){
                case 1:
                    final JsonCommand.DoctorTask taskList = (JsonCommand.DoctorTask) msg.obj;


                    SearchButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String name = SearchEditText.getText().toString();
                            if(name != "") {
                                if(isfind(taskList,name)){
                                    for(JsonCommand.Patient patient:taskList.patientlist){
                                        if(patient.patient_name.equals(name)){
                                            patientnamelist.clear();
                                            patientnamelist.add(patient.patient_name);
                                            adapterlist.notifyDataSetChanged();
                                        }
                                    }
                                }else{
                                    patientnamelist.clear();
                                    Toast.makeText(PatientInfoActivity.this,"没有找到该患者信息",Toast.LENGTH_SHORT).show();
                                    for(JsonCommand.Patient patient : taskList.patientlist){

                                        if(patient.patient_name != "null" && patient.patient_id != "null") {
                                            patientnamelist.add(patient.patient_name);
                                            adapterlist.notifyDataSetChanged();
                                        }
                                    }
                                }
                            }else{
                                patientnamelist.clear();
                                for(JsonCommand.Patient patient : taskList.patientlist){

                                    if(patient.patient_name != "null" && patient.patient_id != "null") {
                                        patientnamelist.add(patient.patient_name);
                                        adapterlist.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    });
                    SearchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if(actionId == KeyEvent.KEYCODE_ENTER){
                                String code = SearchEditText.getText().toString();

                                for(JsonCommand.Patient patient:taskList.patientlist) {
                                    if (patient.patient_qr_code.equals(code)) {
                                        patientnamelist.clear();
                                        patientnamelist.add(patient.patient_name);
                                        adapterlist.notifyDataSetChanged();
                                    }
                                }
                            }
                            return true;
                        }
                    });

                    for(JsonCommand.Patient patient : taskList.patientlist){
                        if(patient.patient_name != "null" && patient.patient_id != "null") {
                            patientnamelist.add(patient.patient_name);
                            adapterlist.notifyDataSetChanged();
                        }
                    }
                    NameList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String Name = patientnamelist.get(position);

                            for(JsonCommand.Patient patient : taskList.patientlist) {

                                if (patient.patient_name.equals(Name)) {
                                    Log.d(TAG,patient.patient_name+"  111");
                                    SharedPreferenceDB.setPatientInfo(PatientInfoActivity.this, patient);
                                    int task_items = patient.task_items;
                                    int idd = Integer.parseInt(String.valueOf(task_items));
                                    Log.d("PatentInfo", idd + "");
                                    if (idd != 0) {
                                        if (idd == 1) {
                                            fragmentlist.removeAll(fragmentlist);
                                            ecg = "心电 ";
                                            press = "";
                                            temp = "";
                                            fragmentlist.add(ecgfragment);
                                            fragmentlist.add(ecghfragment);

                                        }
                                        if (idd == 2) {
                                            fragmentlist.removeAll(fragmentlist);
                                            ecg = "";
                                            press = "血压 ";
                                            temp = "";
                                            fragmentlist.add(bpfragment);
                                            fragmentlist.add(bphfragment);

                                        }
                                        if (idd == 3) {
                                            fragmentlist.removeAll(fragmentlist);
                                            ecg = "心电 ";
                                            press = "血压 ";
                                            temp = "";
                                            fragmentlist.add(ecgfragment);
                                            fragmentlist.add(ecghfragment);

                                            fragmentlist.add(bpfragment);
                                            fragmentlist.add(bphfragment);

                                        }
                                        if (idd == 8) {
                                            fragmentlist.removeAll(fragmentlist);
                                            ecg = "";
                                            press = "";
                                            temp = "体温 ";
                                            fragmentlist.add(tempfragment);
                                            fragmentlist.add(temphfragment);
                                        }
                                        if (idd == 9) {
                                            fragmentlist.removeAll(fragmentlist);
                                            ecg = "心电 ";
                                            press = "";
                                            temp = "体温 ";

                                            fragmentlist.add(ecgfragment);

                                            fragmentlist.add(ecghfragment);
                                            fragmentlist.add(tempfragment);

                                            fragmentlist.add(temphfragment);


                                        }
                                        if (idd == 10) {
                                            fragmentlist.removeAll(fragmentlist);
                                            ecg = "";
                                            press = "血压 ";
                                            temp = "体温 ";


                                            fragmentlist.add(bpfragment);
                                            fragmentlist.add(bphfragment);
                                            fragmentlist.add(tempfragment);
                                            fragmentlist.add(temphfragment);
                                        }
                                        if (idd == 11 || idd == 15) {
                                            fragmentlist.removeAll(fragmentlist);
                                            ecg = "心电 ";
                                            press = "血压 ";
                                            temp = "体温 ";
                                            fragmentlist.add(ecgfragment);
                                            fragmentlist.add(ecghfragment);

                                            fragmentlist.add(bpfragment);
                                            fragmentlist.add(bphfragment);
                                            fragmentlist.add(tempfragment);
                                            fragmentlist.add(temphfragment);
                                        }
                                        mvpadapter.notifyDataSetChanged();
                                    }
                                    infodetail.setText("姓名："+patient.patient_name + "    " + "家庭住址："+patient.patient_add +  "    " + "出生日期："+patient.patient_dob + " "+"\n" +
                                            "性别："+ patient.patient_gender + "    " +"联系方式："+ patient.patient_tel + "    " +"条形码："+patient.patient_qr_code + " " + "\n" +
                                            "身高："+ patient.patient_height + "    " + "体重："+patient.patient_weight + "    " +"重大病史："+patient.patient_disease + " " + "\n" +
                                            "您选择的服务有 ： " + ecg + press + temp
                                    );

                                }
                            }
                        }
                    });
                    break;
                case 2:
                    String logcat = (String)msg.obj;
                    Toast.makeText(PatientInfoActivity.this, logcat, Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    // 处理掉条形码最后的回车符或换行符
                    String code = msg.obj.toString().trim();
                    char endChar = code.charAt(code.length() - 1) ;
                    if(endChar == '\n' || endChar == '\r')
                        code = code.substring(0,  code.length() - 2);
                    SearchEditText.setText(code);
                    sendBroadcast(new Intent("disconnect"));
                    // 回调监听方法
                    SearchEditText.onEditorAction(KeyEvent.KEYCODE_ENTER);
                    break;
            }
        }

    };
    private boolean isfind(JsonCommand.DoctorTask taskList,String name){
        for(JsonCommand.Patient patient : taskList.patientlist){
            if(patient.patient_name.equals(name)){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause()");
        stopService(new Intent(PatientInfoActivity.this, BluetoothConncetService.class));
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestroy()");
        unregisterReceiver(receiver);

        stopService(new Intent(PatientInfoActivity.this,GetNodeInfoService.class));
        stopService(new Intent(PatientInfoActivity.this,GetWebInfoService.class));
    }

    public class MViewPagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<Fragment> list;
        public MViewPagerAdapter(FragmentManager fm,ArrayList<Fragment> list) {
            super(fm);
            // TODO Auto-generated constructor stub
            this.list = list;
        }

        @Override
        public Fragment getItem(int arg0) {
            // TODO Auto-generated method stub

            return list.get(arg0);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return list.size();
        }

        @Override
        public void finishUpdate(ViewGroup container) {
            super.finishUpdate(container);
        }

        @Override
        public void startUpdate(ViewGroup container) {
            super.startUpdate(container);
        }

        @Override
        public int getItemPosition(Object object) {
            return PagerAdapter.POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return super.isViewFromObject(view, object);
        }
    }
}
