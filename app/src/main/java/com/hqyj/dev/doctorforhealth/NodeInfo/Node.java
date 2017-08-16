package com.hqyj.dev.doctorforhealth.NodeInfo;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2016/3/29.
 */
abstract class Node {
    private String TAG = "Node";
    abstract boolean startRead(OutputStream out);
    abstract float readData(InputStream in) throws IOException;
    abstract boolean stopRead(OutputStream out);
    public boolean sendCmd(byte[] cmd, OutputStream out) {
        // TODO Auto-generated method stub
        Log.d(TAG,"cmd");
        if (out == null) {
            Log.d(TAG, "out == null");
            return false;
        }
        if(cmd == null || cmd.length <= 0) {
            Log.d(TAG,"cmd == null || cmd.length <= 0");
            return false;
        }
        try {
            out.write(cmd);
        } catch (Exception e) {
            Log.d(TAG,"out.write failed");
            return false;
        }
        return true;
    }
}
