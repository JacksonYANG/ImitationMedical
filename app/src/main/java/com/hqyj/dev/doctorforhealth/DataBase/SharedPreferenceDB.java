package com.hqyj.dev.doctorforhealth.DataBase;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.hqyj.dev.doctorforhealth.Web.JsonCommand;

/**
 * Created by Administrator on 2016/3/29.
 */
public class SharedPreferenceDB {

    private static SharedPreferences sp;

    private static SharedPreferences getInstance(Context context){
        if(sp == null){
            sp = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return sp;
    }

    public static void setPatientInfo(Context context,JsonCommand.Patient patient){
        getInstance(context).edit().putString("patient_id",patient.patient_id).commit();
        getInstance(context).edit().putString("patient_name",patient.patient_name).commit();
        getInstance(context).edit().putString("patient_dob",patient.patient_dob).commit();
        getInstance(context).edit().putString("patient_gender",patient.patient_gender).commit();
        getInstance(context).edit().putString("patient_tel",patient.patient_tel).commit();
        getInstance(context).edit().putString("patient_mail",patient.patient_mail).commit();
        getInstance(context).edit().putString("patient_id_type",patient.patient_id_type).commit();
        getInstance(context).edit().putString("patient_idcard",patient.patient_idcard).commit();
        getInstance(context).edit().putString("patient_add",patient.patient_add).commit();
        getInstance(context).edit().putString("patient_marriage",patient.patient_marriage).commit();
        getInstance(context).edit().putString("patient_photo", String.valueOf(patient.patient_photo)).commit();
        getInstance(context).edit().putString("patient_height",patient.patient_height).commit();
        getInstance(context).edit().putString("patient_weight",patient.patient_weight).commit();
        getInstance(context).edit().putString("patient_disease",patient.patient_disease).commit();
        getInstance(context).edit().putString("patient_qr_code",patient.patient_qr_code).commit();
        getInstance(context).edit().putString("info",patient.info).commit();
        getInstance(context).edit().putString("reg_date",patient.reg_date).commit();
        getInstance(context).edit().putString("task_id", String.valueOf(patient.task_id)).commit();
        getInstance(context).edit().putString("task_items", String.valueOf(patient.task_items)).commit();
        getInstance(context).edit().putString("finished_items", String.valueOf(patient.finished_items)).commit();
        getInstance(context).edit().putString("result_id", String.valueOf(patient.result_id)).commit();
    }
    public static JsonCommand.Patient getPatientInfo(Context context){
        JsonCommand.Patient patient = new JsonCommand.Patient();

        patient.patient_id = getInstance(context).getString("patient_id",null);
        patient.patient_name = getInstance(context).getString("patient_name",null);
        patient.patient_dob = getInstance(context).getString("patient_dob",null);
        patient.patient_gender = getInstance(context).getString("patient_gender",null);
        patient.patient_tel = getInstance(context).getString("patient_tel",null);
        patient.patient_mail = getInstance(context).getString("patient_mail",null);
        patient.patient_id_type = getInstance(context).getString("patient_id_type",null);
        patient.patient_idcard = getInstance(context).getString("patient_idcard",null);
        patient.patient_add = getInstance(context).getString("patient_add",null);
        patient.patient_marriage = getInstance(context).getString("patient_marriage",null);
        patient.patient_photo = getInstance(context).getString("patient_photo",null).getBytes();
        patient.patient_height = getInstance(context).getString("patient_height",null);
        patient.patient_weight = getInstance(context).getString("patient_weight",null);
        patient.patient_disease = getInstance(context).getString("patient_disease",null);
        patient.patient_qr_code = getInstance(context).getString("patient_qr_code",null);
        patient.info = getInstance(context).getString("info",null);
        patient.reg_date = getInstance(context).getString("reg_date",null);
        patient.task_id = Integer.parseInt(getInstance(context).getString("task_id",null));
        patient.task_items = Integer.parseInt(getInstance(context).getString("task_items",null));
        patient.finished_items = Integer.parseInt(getInstance(context).getString("finished_items",null));
        patient.result_id = Integer.parseInt(getInstance(context).getString("result_id",null));

        return patient;
    }

    public static void setBlue(Context context,String Address,String pincode){
        if(pincode.equals("0438")) {
            getInstance(context).edit().putString("blueAddress", Address).commit();
        }
        if(pincode.equals("10010")) {
            getInstance(context).edit().putString("smAddress", Address).commit();
        }
    }
    public static String getBlue(Context context,String string){
        if(string.equals("blue")){
            return getInstance(context).getString("blueAddress",null);
        }
        if(string.equals("sm")){
            return getInstance(context).getString("smAddress",null);
        }
        return null;
    }
    public static void setLoginUser(Context context, JsonCommand.Doctor doctor){
        getInstance(context).edit().putString("doctor_id", doctor.doctor_id).commit();
        getInstance(context).edit().putString("doctor_name", doctor.doctor_name).commit();
        getInstance(context).edit().putString("doctor_tname", doctor.doctor_tname).commit();
        getInstance(context).edit().putString("doctor_level", doctor.doctor_level).commit();
        getInstance(context).edit().putString("doctor_add", doctor.doctor_add).commit();
        getInstance(context).edit().putString("doctor_dob", doctor.doctor_dob).commit();
        getInstance(context).edit().putString("doctor_gender", doctor.doctor_gender).commit();
        getInstance(context).edit().putString("doctor_tel", doctor.doctor_tel).commit();
        getInstance(context).edit().putString("card_data", doctor.card_data).commit();
    }
    public static JsonCommand.Doctor getLoginUser(Context context){
        JsonCommand.Doctor doctor = new JsonCommand.Doctor();
        doctor.doctor_id = getInstance(context).getString("doctor_id", null);
        doctor.card_data = getInstance(context).getString("card_data", null);
        doctor.doctor_add = getInstance(context).getString("doctor_add", null);
        doctor.doctor_dob = getInstance(context).getString("doctor_dob", null);
        doctor.doctor_gender = getInstance(context).getString("doctor_gender", null);
        doctor.doctor_level = getInstance(context).getString("doctor_level", null);
        doctor.doctor_name = getInstance(context).getString("doctor_name", null);
        //doctor.doctor_permisson = jsonDoctor.getString("doctor_permisson");
        doctor.doctor_tel = getInstance(context).getString("doctor_tel", null);
        return doctor;
    }

}
