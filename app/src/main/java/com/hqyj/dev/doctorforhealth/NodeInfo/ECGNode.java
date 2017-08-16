package com.hqyj.dev.doctorforhealth.NodeInfo;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2016/3/21.
 */
public class ECGNode extends Node{
    private static String TAG = "ECGNode";
    private static byte[] ECGbuffer = new byte[ECGNode.DATA_CMD_LEN];

    // ===================命令字定义========================
    public static final byte DATA_HEAD = (byte) 0xFF;
    public static final byte DATA_HEAD2 = (byte)0xCE;
    public static final byte DATA_CMD_LENGTH_ECG = (byte)0x03;
    public static final byte DATA_CMD_START_ECG = (byte)0xA0;
    public static final byte DATA_CMD_STOP_ECG = (byte)0xA1;
    public static final byte DATA_CMD_LEN = 7;
    private static int count = 0;

    public static byte checkSum(byte[] data){
        Log.d(TAG,"checkSum");
        int sum = data[2] + data[4];
        return (byte)(sum & 0xFF);
    }
    public static int processEcgData(byte[] buffer){
        if(buffer[0] == DATA_HEAD && buffer[1] == DATA_HEAD2){
            return (buffer[5] & 0xFF)<< 8 | buffer[6] & 0xFF;
        }
        return -1;
    }
    public static boolean checkData(byte[] buffer,int start){
        if(buffer.length < start+5){
            return false;
        }
        if(buffer[start] == DATA_HEAD && buffer[start+1] == DATA_HEAD2 && buffer[start+4] == DATA_CMD_START_ECG){
            return true;
        }
        return false;
    }
    @Override
    public boolean startRead(OutputStream out) {
        if(out != null) {
            Log.d(TAG, "开始读取心电");
            byte[] buf = new byte[]{ECGNode.DATA_HEAD, ECGNode.DATA_HEAD2, ECGNode.DATA_CMD_LENGTH_ECG, 0, ECGNode.DATA_CMD_START_ECG};
            buf[3] = ECGNode.checkSum(buf);
            return sendCmd(buf, out);
        }else{
            Log.d(TAG,"out is null");
            return false;
        }
    }

    @Override
    public boolean stopRead(OutputStream out) {
        Log.d(TAG,"停止读取心电");
        byte[] buf = new byte[]{ECGNode.DATA_HEAD,ECGNode.DATA_HEAD2,ECGNode.DATA_CMD_LENGTH_ECG,0,ECGNode.DATA_CMD_STOP_ECG};
        buf[3] = checkSum(buf);
        return sendCmd(buf,out);
    }

    @Override
    public float readData(InputStream in) throws IOException {
        return 0.0f;
    }
    public int redECGData(InputStream in) throws IOException {
        Log.d(TAG,"readData()");
        if(in == null){
            return -1;
        }
        byte[] buffer = new byte[ECGNode.DATA_CMD_LEN];
        int size = 0;
        Log.d(TAG,"ECG开始读取时间1"+System.currentTimeMillis());
        while(size < ECGNode.DATA_CMD_LEN){
            size += in.read(buffer,size,ECGNode.DATA_CMD_LEN - size);
            for (int i =0; i < buffer.length; i++){
                Log.d(TAG, "buff:"+i+":"+buffer[i]);
            }
        }
        Log.d(TAG,"ECG开始读取时间2"+System.currentTimeMillis());
        int start = 0;
        while(!ECGNode.checkData(buffer,start)){
            Log.d(">>>","ECG数据纠错");
            start++;
            if(start >= ECGNode.DATA_CMD_LEN - 1){
                Log.d(">>>","ECG数据最后检查");
                if(buffer[start] == ECGNode.DATA_HEAD){
                    break;
                }
                Log.d(">>>","ECG数据出错");
                count++;
                if(count == 100){
                    Log.d(TAG,count+"");
                    count = 0;
                    Log.d(TAG,count+"");
                    return 1;
                }
                return -1;
            }
        }
        if(start != 0){
            Log.d(TAG, "ECG数据纠错成功，位置：" + start);
            System.arraycopy(buffer, start, ECGbuffer, 0, ECGNode.DATA_CMD_LEN - start);
            start = ECGNode.DATA_CMD_LEN - start;
            while(start < ECGNode.DATA_CMD_LEN){
                start += in.read(ECGbuffer,start,ECGNode.DATA_CMD_LEN - start);
            }
            return ECGNode.processEcgData(ECGbuffer);
        }else{
            Log.d(TAG,"return ecg");
            return ECGNode.processEcgData(buffer);
        }
    }
}





















