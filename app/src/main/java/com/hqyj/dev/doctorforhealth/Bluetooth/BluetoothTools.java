package com.hqyj.dev.doctorforhealth.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by Administrator on 2016/3/29.
 */
public class BluetoothTools {

    private String TAG = "BluetoothTools";
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private String uuid = "00001101-0000-1000-8000-00805F9B34FB";
    private BluetoothSocket bsocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private static BluetoothTools mBluetoothTools;
    private Context mContext;

    private BluetoothTools(Context context){
        mContext = context;
    }
    public static BluetoothTools getmBluetoothTools(Context context){
        if(mBluetoothTools == null){
            mBluetoothTools = new BluetoothTools(context);
        }
        return mBluetoothTools;
    }

    public InputStream getmInputStream(){
        return mInputStream;
    }
    public OutputStream getmOutputStream(){
        return mOutputStream;
    }
    public BluetoothSocket getmBluetoothSocket(){
        return bsocket;
    }

    public void startScan(){
        Log.d(TAG, "startScan()");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null && mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF){
            mBluetoothAdapter.enable();
        }
        if(mBluetoothAdapter != null && mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON){
            mBluetoothAdapter.startDiscovery();
        }
    }
    public void stopScan(){
        Log.d(TAG,"stopScan()");
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter != null){
            if(mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON && mBluetoothAdapter.isDiscovering()){
                mBluetoothAdapter.cancelDiscovery();
            }
        }
    }
    public boolean createBond(Class<? extends BluetoothDevice> btClass,BluetoothDevice btDevice){
        Log.d(TAG,"createBond()");
        try {
            Method createBondMethod = btClass.getMethod("createBond");
            createBondMethod.setAccessible(true);
            Boolean returnValue = (Boolean)createBondMethod.invoke(btDevice);
            return returnValue.booleanValue();
        } catch (NoSuchMethodException e) {
            Throwable t = e.getCause();
            t.printStackTrace();
            return false;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
    }
    public boolean setPin(Class<? extends BluetoothDevice> btClass,BluetoothDevice btDevice,String pincode){
        try {
            Method pairing = btClass.getDeclaredMethod("setPin", new Class[]{byte[].class});
            pairing.setAccessible(true);
            Boolean returnValue = (Boolean)pairing.invoke(btDevice,new Object[]{pincode.getBytes("UTF-8")});
            Log.d(TAG,"SetPin"+returnValue);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return true;
    }
    public boolean disconnectBluetooth(){
        if(bsocket != null && bsocket.isConnected()){

            try {
                bsocket.close();
                mInputStream.close();
                mOutputStream.close();
                bsocket = null;
                mInputStream=null;
                mOutputStream=null;
                Log.d(TAG,"success");
                return true;
            } catch (IOException e) {
                Log.d(TAG,"close failed");
                return false;
            }
        }
        return true;
    }
    public boolean connectBluetooth(BluetoothDevice device,BluetoothAdapter mBluetoothAdapter){
        Log.d(TAG,"connectBluetooth()1");
        if(mBluetoothAdapter == null || device == null){
            return false;
        }
        if(mBluetoothAdapter.isDiscovering()){
            mBluetoothAdapter.cancelDiscovery();
        }
        Log.d(TAG,"connectBluetooth()2");
        UUID mUUID = UUID.fromString(uuid);
        try {
            bsocket = device.createInsecureRfcommSocketToServiceRecord(mUUID);
            bsocket.connect();
            Log.d(TAG, "connectBluetooth()3");
        } catch (IOException e) {

            mInputStream = null;
            mOutputStream = null;
            bsocket = null;

            Log.d(TAG, "connectBluetooth()4");
            return  false;
        }
        try {
            if(bsocket != null) {
                mInputStream = bsocket.getInputStream();
                mOutputStream = bsocket.getOutputStream();
                if (mInputStream == null) {
                    Log.d(TAG, "fail inputStream");
                }
                Log.d(TAG, "connectBluetooth()5");
            }
        } catch (IOException e) {
            try {
                bsocket.close();
                Log.d(TAG, "connectBluetooth()6");
            } catch (IOException e1) {
                e1.printStackTrace();
                Log.d(TAG, "connectBluetooth()7");
                return false;
            }
        }
        return true;
    }

}
