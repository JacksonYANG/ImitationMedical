package com.hqyj.dev.doctorforhealth.Web;

import android.content.Context;
import android.util.Log;


import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.ksoap2.transport.Transport;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 2016/3/21.
 */
public class SoapWeb {
    private static String TAG = "SoapWeb";
    public static final String NAME_SPACE = "http://tempuri.org/";
    private static final String URL = "http://www.farsightdev.com/IMServices.asmx";

    public static String CallSoapWeb(Context context,String methodName,Map<String,String> map){
        SoapObject so = new SoapObject(NAME_SPACE,methodName);
        if(map != null){
            Iterator iterator = map.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<String,String> entry = (Map.Entry)iterator.next();
                Log.d("SOAP",entry.getKey()+":"+entry.getValue());
                so.addProperty(entry.getKey(), entry.getValue());
            }
        }

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = so;

        envelope.dotNet = true;
//        envelope.setOutputSoapObject(so);
        HttpTransportSE ht = new HttpTransportSE(URL);
//        ht.debug = true;
        try {
            ht.call(null,envelope);
        } catch (IOException e) {
            Log.d(TAG,"IOException");
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            Log.d(TAG,"XmlPullParserException");
            e.printStackTrace();
        }
        try {
            SoapPrimitive sp = (SoapPrimitive) envelope.getResponse();
            if(sp != null){
                Log.d("TAG","return soap");
                return sp.toString();
            }
        } catch (Exception e) {
            Log.d(TAG,"SoapFault");
            e.printStackTrace();
        }
        return null;
    }
}
