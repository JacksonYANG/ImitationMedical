package com.hqyj.dev.doctorforhealth.NodeInfo;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2016/4/26.
 */
public class BarNode {
    private String TAG = "BarNode";


    public  String readBarCode(InputStream in) throws IOException{
        if(in == null){
            return null;
        }
        Log.d(TAG,Thread.currentThread().getName()+": readBarCode");
        int MAX_LEN = 32;
        byte[] buffer = new byte[MAX_LEN];
        in.read(buffer);
        Log.d(TAG,"Bra code : "+new String(buffer));
        return new String(buffer);
    }
}
