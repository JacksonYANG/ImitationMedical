package com.hqyj.dev.doctorforhealth.NodeInfo;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.awt.font.TextAttribute;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;

/**
 * Created by Administrator on 2016/3/29.
 */
public class BPNode extends Node{
    private String TAG = "BPNode";
    //命令头
    public static final byte DATA_HEAD = (byte) 0xFF;
    public static final byte DATA_HEAD2 = (byte) 0xCD;
    //休眠
    public static final byte DATA_CMD_SLEEP = (byte) 0xAB;
    public static final byte DATA_RTN_SLEEP = (byte) 0X5B;
    //唤醒
    public static final byte DATA_CMD_AWEAK = (byte) 0xAA;
    public static final byte DATA_RTN_AWEAK = (byte) 0X5A;
    //启动测量
    public static final byte DATA_CMD_START = (byte) 0xA0;
    public static final byte DATA_RTN_START = (byte) 0X54;
    //停止测量
    public static final byte DATA_CMD_STOP = (byte) 0xA3;
    public static final byte DATA_RTN_STOP = (byte) 0X53;
    //返回结果
    public static final byte DATA_RTN_RESULT = (byte) 0X55;
    public static final byte DATA_RTN_ERROR = (byte) 0X56;
    public static final int DATA_STATUS_NO_PLUS =  0x00;
    public static final int DATA_STATUS_BOND_LOOSE =  0x01;
    public static final int DATA_STATUS_ERROR_RESULT =  0x02;
    public static final int DATA_STATUS_OVER_PRESS =  0x03;
    public static final int DATA_STATUS_NOT_OK = 0x04;
    public static final int DATA_STATUS_OK = 0x0F;
    public static final byte ERROR = -1;



    private LinkedList<Byte> mDataQueue;
    private BPReadThread mReadThread;



    public static byte checkSum(byte[] data, int len) {
        int sum = 0;
        for (int i = 0; i < len; i++) {
            sum += data[i];
        }
        return (byte) (sum & 0xFF);
    }
    public static String getErrorStr(int no){
        switch(no){
            case DATA_STATUS_NOT_OK:
                return "体位移动";
            case DATA_STATUS_BOND_LOOSE:
                return "袖带未绑好";
            case DATA_STATUS_ERROR_RESULT:
                return "气压值有误";
            case DATA_STATUS_NO_PLUS:
                return "测量不到有效的脉搏";
            case DATA_STATUS_OVER_PRESS:
                return "进入超压保护";
        }
        return null;
    }
    public static final byte[] getControlCMD(int cmd){
        switch(cmd){
            case DATA_CMD_START:
                return new byte[]{DATA_HEAD, DATA_HEAD2, 0x3, (byte) 0xA3, DATA_CMD_START};
            case DATA_CMD_STOP:
                return new byte[]{DATA_HEAD, DATA_HEAD2, 0x3, (byte) 0xA6, DATA_CMD_STOP};
            case DATA_CMD_AWEAK:
                return new byte[]{DATA_HEAD, DATA_HEAD2, 0x3, (byte) 0xAD, DATA_CMD_AWEAK};
            case DATA_CMD_SLEEP:
                return new byte[]{DATA_HEAD, DATA_HEAD2, 0x3, (byte) 0xAE, DATA_CMD_SLEEP};
        }
        return null;
    }
    private static byte checkData(byte[] buf){


        if(buf[0] != DATA_HEAD || buf[1]!= DATA_HEAD2){
            return ERROR;
        }
        int len = buf[2];
        int sum = 0;
        for(int i = 0; i < len; i++){
            if(i == 1)   // 跳过校验字节
                continue;
            sum += (buf[i+2] & 0xFF);
        }

        // 取校验低字节
        sum &= 0xFF;

        // 位校验
        if(sum != (buf[3] & 0xFF))
            return ERROR;		// 失败返回

        switch(buf[4]){
            case DATA_RTN_AWEAK:
                break;
            case DATA_RTN_SLEEP:
                break;
            case DATA_RTN_START:
                break;
            case DATA_RTN_STOP:
                break;
            case DATA_RTN_ERROR:
                break;
            case DATA_RTN_RESULT:
                break;
        }

        return buf[4];
    }
    public static class BPData{
        /** 当前血压下标 */
        public static int OFT_CP = 0;
        /** 心跳下标 */
        public static int OFT_HT = 1;
        /** 收缩压下标 */
        public static int OFT_SP = 2;
        /** 舒张压下标 */
        public static int OFT_DP = 3;
        /** 心律不齐下标 */
        public static int OFT_AT = 4;
        /** 脉率下标 */
        public static int OFT_PR = 5;
        /** 结果下标 */
        public static int OFT_RS = 6;
        /** 错误号下标 */
        public static int OFT_EN = 7;

        /** 当前压力值 */
        public int currentPressure;

        /** 有无心跳,1:有，0:无 */
        public int heart;

        /** 收缩压(Systolic pressure) */
        public int sp;
        /** 舒张压(Diastolic pressure) */
        public int dp;
        /** arrhythmia心律不齐 ,1:有，0:无 */
        public int arrhythmia;
        /** 脉率 */
        public int pr;

        /** 结果 */
        public int result;

        /** 错误号 */
        public int errNo = DATA_STATUS_OK;

        private static BPData mInstance;

        public static BPData getInstance(){
            if(mInstance == null)
                return new BPData();
            else
                return mInstance;
        }

        /** 有效数据 */
        public BPData setData(int r, int sp, int dp, int pr, int a){
            result = r;
            this.sp = sp;
            this.dp = dp;
            this.arrhythmia = a;
            this.pr = pr;
            return this;
        }

        /** 当前血压值 */
        public BPData setData(int r, int c, int h){
            result = r;
            currentPressure = c;
            heart = h;
            return this;
        }

        /** 结果 */
        public BPData setData(int r, int no){
            result = r;
            errNo = no;
            return this;
        }


        /** 结果 */
        private BPData(){
        }
    }
    public static BPData getBPData(byte[] buf){
        if(buf.length <= 0)
            return null;
        int result = checkData(buf);
        switch(result){
            case ERROR:
                return null;
            case DATA_RTN_ERROR:
                return BPData.getInstance().setData(result, buf[5] & 0xFF);
            case DATA_RTN_RESULT:
                int hp = ((buf[5] & 127) << 8 | buf[6]) & 0xFF;
                int lp = (buf[7] << 8 | buf[8]) & 0xFF;
                int arr = (buf[5] & 128) == 0 ? 0:1;
                int pr = buf[9] & 0xFF;
                return BPData.getInstance().setData(result, hp, lp, pr, arr);
            case DATA_RTN_SLEEP:
            case DATA_RTN_AWEAK:
            case DATA_RTN_STOP:
                return BPData.getInstance().setData(result, DATA_STATUS_OK);
            case DATA_RTN_START:
                return BPData.getInstance().setData(result, (buf[5] & 15) << 8 | (buf[6] & 0xFF), ((buf[5] & 16) == 0? 0: 1));
        }
        return null;
    }
    private class BPReadThread extends Thread {
        private boolean isStart = true;
        private BluetoothSocket mSocket;
        private InputStream mIn;

        public BPReadThread(BluetoothSocket socket) throws IOException {
            Log.d(TAG, "BPReadThread()...");
            mSocket = socket;
            mIn = socket.getInputStream();
            mDataQueue = new LinkedList<Byte>();
        }

        public void cancel() {
            isStart = false;
//            try {
//                mSocket.close();
//            } catch (IOException e) {
//            }
        }

        public void run() {
            while (isStart) {
                try {
                    int data = mIn.read();
                    synchronized (mDataQueue) {
                        mDataQueue.add((byte) (data & 0xFF));
                    }
                } catch (Exception e) {
                    Log.d(TAG,"BPReadThread read error!");
                    isStart = false;
                    mReadThread = null;
                }
            }
        }
    }
    public boolean startBP(BluetoothSocket socket) throws IOException {
        Log.d(TAG,"startBP");
        if(socket == null) {
            Log.d(TAG, "socker == null");
            return false;
        }
        // 清空旧数据
        if(mDataQueue != null && mDataQueue.size() > 0){
            synchronized (mDataQueue) {
                mDataQueue.clear();
            }
        }

        if (mReadThread == null) {
            mReadThread = new BPReadThread(socket);
            mReadThread.start();
        }
        return sendCmd(BPNode.getControlCMD(BPNode.DATA_CMD_START), socket.getOutputStream());
    }

    public boolean stopBP(OutputStream out) {
        Log.d(TAG,"stopBP");
        if(sendCmd(BPNode.getControlCMD(BPNode.DATA_CMD_STOP), out)) {
            return false;
        }
        if (mReadThread != null && mReadThread.isAlive()) {
            mReadThread.cancel();
            mReadThread = null;
        }

        return true;
    }

    public int[] readBP() {
        if (mDataQueue == null)
            return null;

        // 对取得数据进行解析处理
        BPData data = null;
        synchronized (mDataQueue) {
            // 保证第一个数据为有效数据
            while (mDataQueue.size() != 0
                    && mDataQueue.get(0).byteValue() != BPNode.DATA_HEAD)
                mDataQueue.remove();

            if (mDataQueue.size() == 0) {
                return null;
            }

            // 处理串口中得到的原始数据帧
            for (int i = 0; i < mDataQueue.size(); i++) {
                try {
                    if (mDataQueue.get(i).byteValue() == BPNode.DATA_HEAD
                            && mDataQueue.get(i + 1).byteValue() == BPNode.DATA_HEAD2) {
                        int len = mDataQueue.get(i + 2);
                        len += 2;
                        // 防止一帧数据还没有接收完
                        if (mDataQueue.size() < i + len)
                            return null;

                        byte[] buf = new byte[len];
                        // 删除已经处理数据
                        for (int j = 0; j < len; j++) {
                            buf[j] = mDataQueue.remove(i);
                            Log.d(TAG,"> " + Integer.toHexString(buf[j]));
                        }
                        data = BPNode.getBPData(buf);
                        break;
                    }
                } catch (Exception e) {
                    return null;
                }
            }
            if (data == null)
                return null;
            else
                return new int[] { data.currentPressure, data.heart, data.sp,
                        data.dp, data.arrhythmia, data.pr, data.result,
                        data.errNo };
        }
    }

    @Override
    boolean startRead(OutputStream out) {
        return false;
    }

    @Override
    float readData(InputStream in) throws IOException {
        return 0;
    }

    @Override
    boolean stopRead(OutputStream out) {
        return false;
    }
}
