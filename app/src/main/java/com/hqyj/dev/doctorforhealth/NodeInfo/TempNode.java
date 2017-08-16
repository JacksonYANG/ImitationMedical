package com.hqyj.dev.doctorforhealth.NodeInfo;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2016/3/29.
 */
public class TempNode extends Node{
    private static String TAG = "TempNode";
    private static byte[] Tempbuffer = new byte[TempNode.DATA_CMD_LEN];

    // ===================命令字定义========================
    public static final byte DATA_HEAD = (byte) 0xFF;
    public static final byte DATA_HEAD2 = (byte)0xC9;
    public static final byte DATA_CMD_LENGTH_TEMP = (byte)0x03;
    public static final byte DATA_CMD_START_TEMP = (byte)0xA0;
    public static final byte DATA_CMD_STOP_TEMP = (byte)0xA1;
    public static final byte DATA_CMD_LEN = 7;

    public static byte checkSum(byte[] data){
        Log.d(TAG, "checkSum");
        int sum = data[2] + data[4];
        return (byte)(sum & 0xFF);
    }
    public static int processData(byte[] buffer){
        if(buffer[0] == DATA_HEAD && buffer[1] == DATA_HEAD2){
            return (buffer[5] & 0xFF)<< 8 | buffer[6] & 0xFF;
        }
        return -1;
    }
    public static boolean checkData(byte[] buffer,int start){
        if(buffer.length < start+5){
            return false;
        }
        if(buffer[start] == DATA_HEAD && buffer[start+1] == DATA_HEAD2 && buffer[start+4] == DATA_CMD_START_TEMP){
            return true;
        }
        return false;
    }
    @Override
    public boolean startRead(OutputStream out) {
        Log.d(TAG,"开始读取体温");
        byte[] buf = new byte[]{DATA_HEAD,DATA_HEAD2,DATA_CMD_LENGTH_TEMP,0,DATA_CMD_START_TEMP};
        buf[3] = checkSum(buf);
        return sendCmd(buf,out);
    }

    @Override
    public boolean stopRead(OutputStream out) {
        Log.d(TAG,"停止读取心体温");
        byte[] buf = new byte[]{DATA_HEAD,DATA_HEAD2,DATA_CMD_LENGTH_TEMP,0,DATA_CMD_STOP_TEMP};
        buf[3] = checkSum(buf);
        return sendCmd(buf,out);
    }

    @Override
    public float readData(InputStream in) throws IOException {
        Log.d(TAG,"readData()");
        if(in == null){
            return -1;
        }
        byte[] buffer = new byte[DATA_CMD_LEN];
        int size = 0;
        Log.d(TAG,"TEMP开始读取时间1"+System.currentTimeMillis());
        while(size < DATA_CMD_LEN){
            size += in.read(buffer,size,DATA_CMD_LEN - size);
            for (int i =0; i < buffer.length; i++){
                Log.d(TAG, "buff:"+i+":"+buffer[i]);
            }
        }
        Log.d(TAG,"TEMP开始读取时间2"+System.currentTimeMillis());
        int start = 0;
        while(!checkData(buffer, start)){
            Log.d(">>>","TEMP数据纠错");
            start++;
            if(start >= DATA_CMD_LEN - 1){
                Log.d(">>>","TEMP数据最后检查");
                if(buffer[start] == DATA_HEAD){
                    break;
                }
                Log.d(">>>","TEMP数据出错");
                return -1;
            }
        }
        if(start != 0){
            Log.d(TAG, "TEMP数据纠错成功，位置：" + start);
            System.arraycopy(buffer, start, Tempbuffer, 0, DATA_CMD_LEN - start);
            start = DATA_CMD_LEN - start;
            while(start < DATA_CMD_LEN){
                start += in.read(Tempbuffer,start,DATA_CMD_LEN - start);
            }
            return processData(Tempbuffer);
        }else{
            Log.d(TAG,"return temp");
            return processData(buffer);
        }
    }
}
