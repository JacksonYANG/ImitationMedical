package com.hqyj.dev.doctorforhealth.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


import com.hqyj.dev.doctorforhealth.CustomTools.DrawSwich;
import com.hqyj.dev.doctorforhealth.CustomTools.WaitingDialog;
import com.hqyj.dev.doctorforhealth.R;
import com.hqyj.dev.doctorforhealth.Service.BluetoothConnectService;

import java.util.ArrayList;


/**
 * Created by Administrator on 2016/3/18.
 */
public class BluetoothConnectActivity extends Activity{
    private static String TAG = "BluetoothConnectActivity";
    private Button jiancebtn,blutoothbtn,saomiaobtn;//三个按钮实例化，第一个是检测按钮，第二个是开启蓝牙按钮，第三个是扫描蓝牙按钮

    private DrawSwich mSwitch;
    //下面三个是一起的功能，用于显示扫描到的蓝牙设备到ListView中
    private ArrayList<String> blueDeviceName = new ArrayList<String>();//扫描到蓝牙设备后用ArrayList显示，对应下面的ListView
    private ArrayAdapter<String> blueAdapter;
    private ListView blueDeviceList;

    private BluetoothReceiver mBluetoothReceiver;//内部类bluetoothreceiver实例化

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//应用程序窗体显示为无标题
        Log.d(TAG,"BluetoothConnectActivity");//若成功则打印出相应信息
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);//设定窗体全屏
        setContentView(R.layout.activity_bluetoothconnect);//跳转到页面布局activity_bluetoothconnect上
        Log.d(TAG,"setContentView");//同上打印信息

        blutoothbtn = (Button)findViewById(R.id.blutoothbtn);
        blutoothbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectDialog("blue");//调用下述的一个public函数,形式参数为一个final字符串
            }
        });

        saomiaobtn = (Button)findViewById(R.id.saomiaobtn);
        saomiaobtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectDialog("sm");//同上跳转至下面的public函数
            }
        });

        jiancebtn = (Button)findViewById(R.id.jiancebtn);
        jiancebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(BluetoothConnectActivity.this, PatientInfoActivity.class));//启动一个名字为PatientInfoActivity的活动

            }
        });

        mSwitch = (DrawSwich)findViewById(R.id.switch1);//DrawSwitch是一个自定义UI控件，具体自定义代码在CustonTools下的DrawSwitch里面
        mSwitch.setOnChangedListener(new DrawSwich.OnChangedListener() {
            @Override
            public void OnChanged(DrawSwich drawSwitch, boolean checkState) {
                Log.d(TAG,"switch");
                sendBroadcast(new Intent("switch"));//向所有广播接收器发送一个名字为switch的广播
            }
        });
        blueDeviceList = (ListView)findViewById(R.id.BlueDeviceList);//开头的ListView变量实例化
        blueAdapter = new ArrayAdapter<String>(BluetoothConnectActivity.this,android.R.layout.simple_expandable_list_item_1,blueDeviceName);
        blueDeviceList.setAdapter(blueAdapter);
        //动态注册广播
        IntentFilter intentfilter = new IntentFilter();
        //广播接收器监听以下五条action
        intentfilter.addAction("mBluetoothDevice");
        intentfilter.addAction("mBluetoothDeviceBond");
        intentfilter.addAction("finish");
        intentfilter.addAction("change");
        intentfilter.addAction("update");
        mBluetoothReceiver = new BluetoothReceiver();
        registerReceiver(mBluetoothReceiver, intentfilter);
        //将BluetoothReceiver实例化，并且将其注册，使其能监听所有值为intentfilter的广播

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG,"onStart()");

        //在打开软件的生命周期中，首先启动BluetoothConnectService服务
        startService(new Intent(this, BluetoothConnectService.class));
    }

    private class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();//获得接受的mBluetoothReceiver中的广播事件
            //将action与上面所说的动态广播字符串进行一一匹配
            if(action.equals("mBluetoothDevice")){
                //在BluetoothService第168行对应有putExtra,对应的键值为蓝牙设备的名字和地址
                //这个if条件中包含的是未配对的情况
                String DeviceName = intent.getStringExtra("DeviceName");//
                String DeviceAddress = intent.getStringExtra("DeviceAddress");
                Log.d(TAG, DeviceName + DeviceAddress);
                //如果blueDeviceName中没有包含上述的DeviceName和DeviceAddress，则发送通知为未配对
                if(!blueDeviceName.contains(DeviceName + "\r\n" + DeviceAddress+ "\r\n" + "未配对")){
                    blueDeviceName.add(DeviceName + "\r\n" + DeviceAddress+ "\r\n" + "未配对");
                    blueAdapter.notifyDataSetChanged();//调用BaseAdapter中的更新函数，更新当前列表
                }
            }
            if(action.equals("mBluetoothDeviceBond")){
                //这段条件逻辑中包含的是已配对的逻辑,
                String DeviceName = intent.getStringExtra("DeviceName");
                String DeviceAddress = intent.getStringExtra("DeviceAddress");
                Log.d(TAG, DeviceName + DeviceAddress);
                if(!blueDeviceName.contains(DeviceName + "\r\n" + DeviceAddress+ "\r\n" + "已配对")){
                    blueDeviceName.add(DeviceName + "\r\n" + DeviceAddress+ "\r\n" + "已配对");
                    blueAdapter.notifyDataSetChanged();
                }
            }
            if(action.equals("finish")){
                //当广播接收器捕获值为finish的广播后，对应为BluetothService中的182行蓝牙扫描结束
                mSwitch.setChecked(false);//对应DrawSwitch类中使得newStatus为flase
                mSwitch.invalidate();//关闭DrawSwitch View,使其无效
            }
            if(action.equals("update")){
                //若action字符串捕获值为update的广播后，执行下述逻辑
                String bluetype = intent.getStringExtra("bluetype");//获得bluetype的键值（蓝牙类型）
                String bluename = intent.getStringExtra("bluename");//获得bluename的键值（蓝牙名字）
                if(bluetype.equals("blue")){
                    //如果bluetype和blue相匹配，则已配对，将UI中蓝牙按钮的文字设置为蓝牙设备名字+已配对
                    blutoothbtn.setText(bluename+" 已配对");
                }else if(bluetype.equals("sm")){
                    //如果bluetype跟sm（这么邪恶的？？？？）这个字符串相匹配，则显示已配对
                    saomiaobtn.setText(bluename+" 已配对");
                }
            }
            if(action.equals("change")){
                //如果action与change字符串相同
                String yuan = intent.getStringExtra("yuan");//代表原来的蓝牙设备的名字
                String xian = intent.getStringExtra("xian");//代表现在的蓝牙设备的名字
                String bluetype = intent.getStringExtra("bluetype");
                String bluename = intent.getStringExtra("bluename");
                if(blueDeviceName.contains(yuan)){
                    blueDeviceName.remove(yuan);//蓝牙设备名字列表中删除原来的设备名称
                    blueDeviceName.add(xian);//蓝牙设备名字列表中添加现在的设备名称
                    blueAdapter.notifyDataSetChanged();//刷新蓝牙适配器
                }
                if(bluetype.equals("blue")){
                    blutoothbtn.setText(bluename+" 已配对");
                }else if(bluetype.equals("sm")){
                    saomiaobtn.setText(bluename+" 已配对");
                }
            }
        }
    }

    public void connectDialog(final String str){
        //设置对话框的相应逻辑
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_connect,null);//加载连接对话框的布局
        builder.setView(view);
        final EditText editText = (EditText)view.findViewById(R.id.edittext);
        ListView listView = (ListView) view.findViewById(R.id.btList);
        ArrayList<String> list = new ArrayList<String>();
        for(String AllBlueDevice: blueDeviceName){
            list.add(AllBlueDevice);//添加所有的蓝牙设备名字到ListView的列表中
        }
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,list);
        listView.setAdapter(arrayAdapter);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String[] array = blueDeviceName.get(position).split("\r\n");//若产生点击事件，获取除去空格回车等空白符的相应点击位置的蓝牙设备的名字
                String DeviceName = array[0];
                String DeviceAddress = array[1];
                String pinCode = editText.getText().toString();//连接设备时的密码

                Intent intent = new Intent("connectCMD");//产生一个活动为connectCMD，对应逻辑在BluetoothService中
                intent.putExtra("deviceAddress",DeviceAddress);//设置deviceAddress键值对
                intent.putExtra("pinCode",pinCode);//设置pinCode键值对
                Log.d(TAG,str+"  blue");
                intent.putExtra("blue",str);//设置blue相应键值对
                sendBroadcast(intent);//发送广播

                alertDialog.dismiss();//关闭对话框
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"onPause()");
        stopService(new Intent(this, BluetoothConnectService.class));//生命周期为暂停时，停止蓝牙服务

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"onDestory()");

        unregisterReceiver(mBluetoothReceiver);//注销所有的包含于mBluetoothReceiver中的广播过滤器
    }
}
