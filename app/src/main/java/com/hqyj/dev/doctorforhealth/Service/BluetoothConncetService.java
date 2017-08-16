package com.hqyj.dev.doctorforhealth.Service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.hqyj.dev.doctorforhealth.Bluetooth.BluetoothTools;
import com.hqyj.dev.doctorforhealth.DataBase.SharedPreferenceDB;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Administrator on 2016/3/21.
 */
public class BluetoothConnectService extends Service {
    private String TAG = "BluetoothConnectService";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetootDevice;
    private String pinCode,bluetype;
    private MyBroadcastReceiver myBroadcastReceiver;
    private static ArrayList<Task> tasklist = new ArrayList<Task>();
    private Thread thread;
    private boolean isRun = true;
    private BluetoothTools Tools = BluetoothTools.getmBluetoothTools(this);
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private MyThread myThread;
    //当蓝牙连接好之后，弹出提示语句
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case 1:
                    Toast.makeText(BluetoothConnectService.this,"扫码枪准备完毕",Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    Toast.makeText(BluetoothConnectService.this,"蓝牙模块准备完毕",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    //连接蓝牙的线程
    private class MyThread extends Thread{
        private String bAdd;
        private String bName;
        public MyThread(String blueAddress,String bluename){
            this.bAdd = blueAddress;
            this.bName = bluename;
        }
        @Override
        public void run() {
            if(Tools.disconnectBluetooth()){

                Log.d(TAG,"success disconnect");
            }

            Log.d(TAG,bAdd);
            mBluetootDevice = mBluetoothAdapter.getRemoteDevice(bAdd);
            if(mBluetootDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                boolean connect = Tools.connectBluetooth(mBluetootDevice,mBluetoothAdapter);
                if(connect){
                    if(bName.equals("blue")){
                        handler.sendEmptyMessage(2);
                    }else if(bName.equals("sm")){
                        handler.sendEmptyMessage(1);
                    }
                    sendBroadcast(new Intent("success"+bName));
                    mInputStream = Tools.getmInputStream();
                    mOutputStream = Tools.getmOutputStream();
                    if(mOutputStream == null || mInputStream == null){
                        Log.d(TAG,"in out is null");
                    }else{
                        Log.d(TAG,"in out is not null");
                    }
                }
            }
        }
    }
    //打开蓝牙设备，注册广播用来搜索蓝牙。
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"server onCreate()");
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)){
            Toast.makeText(this,"本设备不支持蓝牙",Toast.LENGTH_SHORT).show();
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            Toast.makeText(this,"本设备不支持蓝牙",Toast.LENGTH_SHORT).show();
        }
        if(mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }



        IntentFilter infilter = new IntentFilter();
        infilter.addAction(BluetoothDevice.ACTION_FOUND);
        infilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        infilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        infilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        infilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        infilter.addAction("android.bluetooth.device.action.PAIRING_REQUEST");
        infilter.addAction("switch");
        infilter.addAction("connectCMD");
        infilter.addAction("connectsm");
        infilter.addAction("connectblue");
        infilter.addAction("disconnect");
        myBroadcastReceiver = new MyBroadcastReceiver();
        registerReceiver(myBroadcastReceiver,infilter);
    }


    //广播接收器
    private class MyBroadcastReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            mBluetootDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if(action.equals("disconnect")){//断开蓝牙
                Tools.disconnectBluetooth();
            }
            if(action.equals("connectblue")){//连接蓝牙模块的蓝牙

                if(myThread != null){
                    myThread = null;
                }
                String blue = SharedPreferenceDB.getBlue(BluetoothConnectService.this,"blue");
                myThread = new MyThread(blue,"blue");
                myThread.start();

            }
            if(action.equals("connectsm")){//连接扫码枪的蓝牙
                if(myThread != null){
                    myThread = null;
                }
                String sm = SharedPreferenceDB.getBlue(BluetoothConnectService.this,"sm");
                myThread = new MyThread(sm,"sm");
                myThread.start();
            }
            if(action.equals("switch")){//开始扫描蓝牙
                Log.d(TAG,"switch in server");
                Tools.startScan();
            }
            //蓝牙状态改变
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                if(!mBluetoothAdapter.isDiscovering()){
                    mBluetoothAdapter.startDiscovery();
                }
            }
            if(action.equals(BluetoothDevice.ACTION_FOUND)){//发现蓝牙
                if(mBluetootDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    Intent intentdevice = new Intent("mBluetoothDevice");
                    intentdevice.putExtra("DeviceName",mBluetootDevice.getName());
                    intentdevice.putExtra("DeviceAddress",mBluetootDevice.getAddress());
                    sendBroadcast(intentdevice);
                }
                if(mBluetootDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Intent intentdevice = new Intent("mBluetoothDeviceBond");
                    intentdevice.putExtra("DeviceName",mBluetootDevice.getName());
                    intentdevice.putExtra("DeviceAddress",mBluetootDevice.getAddress());
                    sendBroadcast(intentdevice);
                }
            }
            //扫描完成
            if(action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)){
                Tools.stopScan();
                sendBroadcast(new Intent("finish"));
            }
            //发送命令，进行连接
            if(action.equals("connectCMD")){
                String btAddress = intent.getStringExtra("deviceAddress");
                pinCode = intent.getStringExtra("pinCode");
                bluetype = intent.getStringExtra("blue");
                Log.d(TAG,btAddress+" "+pinCode);
                mBluetootDevice = mBluetoothAdapter.getRemoteDevice(btAddress);
                if(mBluetootDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    boolean bond = Tools.createBond(mBluetootDevice.getClass(),mBluetootDevice);
                    Log.d(TAG,bond+"");
                }
                if(mBluetootDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    if(bluetype.equals("blue")){
                        SharedPreferenceDB.setBlue(BluetoothConnectService.this,mBluetootDevice.getAddress(),"0438");
                    }
                    if(bluetype.equals("sm")){
                        SharedPreferenceDB.setBlue(BluetoothConnectService.this,mBluetootDevice.getAddress(),"10010");
                    }
                    Intent inten = new Intent("update");
                    inten.putExtra("bluetype",bluetype);
                    inten.putExtra("bluename",mBluetootDevice.getName());
                    sendBroadcast(inten);
                }

            }
            //配对请求
            if (action.equals("android.bluetooth.device.action.PAIRING_REQUEST")){
                boolean bonds = Tools.setPin(mBluetootDevice.getClass(), mBluetootDevice, pinCode);
                Log.d(TAG,bonds+" ;;;;;");
                Log.d(TAG,"request"+pinCode);
            }
            //绑定状态改变
            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                if(mBluetootDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    if(bluetype.equals("blue")){
                        SharedPreferenceDB.setBlue(BluetoothConnectService.this,mBluetootDevice.getAddress(),"0438");
                    }

                    if(bluetype.equals("sm")){
                        SharedPreferenceDB.setBlue(BluetoothConnectService.this,mBluetootDevice.getAddress(),"10010");
                    }

                    Toast.makeText(BluetoothConnectService.this,"配对成功",Toast.LENGTH_SHORT).show();
                    Intent change = new Intent("change");
                    change.putExtra("yuan",mBluetootDevice.getName()+"\r\n"+mBluetootDevice.getAddress()+"\r\n"+"未配对");
                    change.putExtra("xian",mBluetootDevice.getName()+"\r\n"+mBluetootDevice.getAddress()+"\r\n"+"已配对");
                    change.putExtra("bluetype",bluetype);
                    change.putExtra("bluename",mBluetootDevice.getName());
                    sendBroadcast(change);
                    Log.d(TAG,"配对成功");
                }
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG,"server Destory");
        unregisterReceiver(myBroadcastReceiver);
        Tools.stopScan();
    }
}
