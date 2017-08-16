package com.hqyj.dev.doctorforhealth.Web;

import android.util.Log;

import com.hqyj.dev.doctorforhealth.Service.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2016/3/29.
 */
public class JsonCommand {
    private static String TAG = "JsonCommand";
    public static class Doctor {
        public int code;
        public String errorStr;
        public String doctor_id;
        public String doctor_name;
        public String doctor_tname;
        public String doctor_permisson;
        public String card_data;
        public String doctor_tel;
        public String doctor_add;
        public String doctor_level;
        public String doctor_gender;
        public String doctor_dob;
    }

    public static Doctor getDoctorObject(String json){
        JSONObject jsonDoctor;
        Doctor doctor = new Doctor();
        try {
            jsonDoctor = new JSONObject(json);
            doctor.doctor_id = jsonDoctor.getString("doctor_id");
            doctor.code = jsonDoctor.getInt("code");
            doctor.errorStr = jsonDoctor.getString("errorStr");
            doctor.card_data = jsonDoctor.getString("card_data");
            doctor.doctor_add = jsonDoctor.getString("doctor_add");
            doctor.doctor_dob = jsonDoctor.getString("doctor_dob");
            doctor.doctor_gender = jsonDoctor.getString("doctor_gender");
            doctor.doctor_level = jsonDoctor.getString("doctor_level");
            doctor.doctor_name = jsonDoctor.getString("doctor_name");
            //doctor.doctor_permisson = jsonDoctor.getString("doctor_permisson");
            doctor.doctor_tel = jsonDoctor.getString("doctor_tel");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return doctor;
    }
    public static class DoctorTask{
        public int code;
        public String errorStr;
        public int  count;
        public Patient[] patientlist;
    }
    public static class Patient{
        public String patient_id;
        public String patient_name;
        public String patient_dob;
        public String patient_gender;
        public String patient_tel;
        public String patient_mail;
        public String patient_id_type;
        public String patient_idcard;
        public String patient_add;
        public String patient_marriage;
        public byte[] patient_photo;
        public String patient_height;
        public String patient_weight;
        public String patient_disease;
        public String patient_qr_code;
        public String info;
        public String reg_date;
        public int task_id;
        public int task_items;
        public int finished_items;
        public int result_id;
    }

    public static DoctorTask getTaskList(String json){
        JSONObject jsonDownloadTask;
        DoctorTask taskList = new DoctorTask();
        System.out.println("TaskList Json:" + json);
        try {
            jsonDownloadTask = new JSONObject(json);
            taskList.code = jsonDownloadTask.getInt("code");
            taskList.count = jsonDownloadTask.getInt("count");
            taskList.errorStr = jsonDownloadTask.getString("errorStr");
            taskList.patientlist = new Patient[taskList.count];
            JSONArray jsonTaskList = jsonDownloadTask.getJSONArray("tasklist");
            JSONObject temp2 = (JSONObject) jsonTaskList.get(taskList.count - 1);
            if(jsonTaskList == null)
                return null;
            for(int i = 0 ; i < taskList.count - 1; i++){
                for(int j = taskList.count - 1; j > i; j--) {
                    JSONObject temp = (JSONObject) jsonTaskList.get(i);
                    JSONObject temp1 = (JSONObject) jsonTaskList.get(j);
                    if (temp == null)
                        return null;

                        if (temp.getString("patient_name").equals(temp1.getString("patient_name")) && temp.getString("patient_id").equals(temp1.getString("patient_id"))) {
                            Log.d(TAG, "0");
                            taskList.patientlist[i] = new Patient();
                            taskList.patientlist[i].patient_id = "null";
                            taskList.patientlist[i].patient_name = "null";
                            taskList.patientlist[i].patient_mail = "null";
                            taskList.patientlist[i].info = "null";
                            taskList.patientlist[i].patient_add = "null";
                            taskList.patientlist[i].patient_disease = "null";
                            taskList.patientlist[i].patient_dob = "null";
                            taskList.patientlist[i].patient_gender = "null";
                            taskList.patientlist[i].patient_height = "null";
                            taskList.patientlist[i].patient_id_type = "null";
                            taskList.patientlist[i].patient_idcard = "null";
                            taskList.patientlist[i].patient_marriage = "null";
                            taskList.patientlist[i].patient_photo = Base64.decodeBase64("null");
                            taskList.patientlist[i].patient_qr_code = "null";
                            taskList.patientlist[i].patient_tel = "null";
                            taskList.patientlist[i].patient_weight = "null";
                            taskList.patientlist[i].reg_date = "null";
                            taskList.patientlist[i].result_id = 0;
                            taskList.patientlist[i].task_id = 0;
                            taskList.patientlist[i].task_items = 0;
                            taskList.patientlist[i].finished_items = 0;
                            break;
                        } else if (j == i + 1) {
                            Log.d(TAG, "1");
                            taskList.patientlist[i] = new Patient();
                            taskList.patientlist[i].patient_id = temp.getString("patient_id");
                            taskList.patientlist[i].patient_name = temp.getString("patient_name");
                            taskList.patientlist[i].patient_mail = temp.getString("patient_mail");
                            taskList.patientlist[i].info = temp.getString("info");
                            taskList.patientlist[i].patient_add = temp.getString("patient_add");
                            taskList.patientlist[i].patient_disease = temp.getString("patient_disease");
                            taskList.patientlist[i].patient_dob = temp.getString("patient_dob");
                            taskList.patientlist[i].patient_gender = temp.getString("patient_gender");
                            taskList.patientlist[i].patient_height = temp.getString("patient_height");
                            taskList.patientlist[i].patient_id_type = temp.getString("patient_id_type");
                            taskList.patientlist[i].patient_idcard = temp.getString("patient_idcard");
                            taskList.patientlist[i].patient_marriage = temp.getString("patient_marriage");
                            taskList.patientlist[i].patient_photo = Base64.decodeBase64(temp.getString("patient_photo"));
                            taskList.patientlist[i].patient_qr_code = temp.getString("patient_qr_code");
                            taskList.patientlist[i].patient_tel = temp.getString("patient_tel");
                            taskList.patientlist[i].patient_weight = temp.getString("patient_weight");
                            taskList.patientlist[i].reg_date = temp.getString("reg_date");
                            taskList.patientlist[i].result_id = temp.getInt("result_id");
                            taskList.patientlist[i].task_id = temp.getInt("task_id");
                            taskList.patientlist[i].task_items = temp.getInt("task_items");
                            taskList.patientlist[i].finished_items = temp.getInt("finished_items");
                        }
                    }
            }
            taskList.patientlist[taskList.count - 1] = new Patient();
            taskList.patientlist[taskList.count - 1].patient_id = temp2.getString("patient_id");
            taskList.patientlist[taskList.count - 1].patient_name = temp2.getString("patient_name");
            taskList.patientlist[taskList.count - 1].patient_mail = temp2.getString("patient_mail");
            taskList.patientlist[taskList.count - 1].info = temp2.getString("info");
            taskList.patientlist[taskList.count - 1].patient_add = temp2.getString("patient_add");
            taskList.patientlist[taskList.count - 1].patient_disease = temp2.getString("patient_disease");
            taskList.patientlist[taskList.count - 1].patient_dob = temp2.getString("patient_dob");
            taskList.patientlist[taskList.count - 1].patient_gender = temp2.getString("patient_gender");
            taskList.patientlist[taskList.count - 1].patient_height = temp2.getString("patient_height");
            taskList.patientlist[taskList.count - 1].patient_id_type = temp2.getString("patient_id_type");
            taskList.patientlist[taskList.count - 1].patient_idcard = temp2.getString("patient_idcard");
            taskList.patientlist[taskList.count - 1].patient_marriage = temp2.getString("patient_marriage");
            taskList.patientlist[taskList.count - 1].patient_photo = Base64.decodeBase64(temp2.getString("patient_photo"));
            taskList.patientlist[taskList.count - 1].patient_qr_code = temp2.getString("patient_qr_code");
            taskList.patientlist[taskList.count - 1].patient_tel = temp2.getString("patient_tel");
            taskList.patientlist[taskList.count - 1].patient_weight = temp2.getString("patient_weight");
            taskList.patientlist[taskList.count - 1].reg_date = temp2.getString("reg_date");
            taskList.patientlist[taskList.count - 1].result_id = temp2.getInt("result_id");
            taskList.patientlist[taskList.count - 1].task_id = temp2.getInt("task_id");
            taskList.patientlist[taskList.count - 1].task_items = temp2.getInt("task_items");
            taskList.patientlist[taskList.count - 1].finished_items = temp2.getInt("finished_items");




        } catch (JSONException e) {
            // TODO Auto-generated catch block
            Log.d(TAG,"catch");
            e.printStackTrace();
            return null;
        }
        return taskList;
    }

    public static class QueryResult {
        public int code;
        public String errStr;
        public int count;
        public PaientResult[] resultList;
    }

    public static class PaientResult {
        public int result_id;//结果id
        public byte[] ecg_data;//心电数据
        public long ecg_time;//心电采集时间
        public String ecg_info;//心电结果建议
        public String hp_data;//收缩压数据
        public String lp_data;//舒张压数据
        public long bloodpress_time;//血压采集时间
        public String bloodpress_info;//血压结果建议
        public String glucose_data;//血糖数据
        public long glucose_time;//血糖采集时间
        public String glucose_info;//血糖结果建议
        public String temperature;//体温数据
        public long temperature_time;//体温采集时间
        public String temperature_info;//体温结果建议
        public String date;//结果上传日期
        public String info;//专家备注信息
    }
    public static QueryResult getUploadResult(String json){
        JSONObject jsonQueryResult;
        QueryResult queryResult = new QueryResult();
        try {
            jsonQueryResult = new JSONObject(json);
            queryResult.code = jsonQueryResult.getInt("code");
            queryResult.errStr = jsonQueryResult.getString("errorStr");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return queryResult;
    }

    public static QueryResult getQueryResult(String json){
        JSONObject jsonQueryResult;
        QueryResult queryResult = new QueryResult();
        try {
            jsonQueryResult = new JSONObject(json);
            queryResult.code = jsonQueryResult.getInt("code");
            queryResult.count = jsonQueryResult.getInt("count");
            queryResult.errStr = jsonQueryResult.getString("errorStr");
            queryResult.resultList = new PaientResult[queryResult.count];
            JSONArray jsonResultList = jsonQueryResult.getJSONArray("list");
            if(jsonResultList == null)
                return null;
            for(int i = 0 ; i < queryResult.count; i++){
                JSONObject temp = (JSONObject) jsonResultList.get(i);
                if( temp == null)
                    return null;
                queryResult.resultList[i]=new PaientResult();
                queryResult.resultList[i].result_id= temp.getInt("result_id");

                byte[] tempbyte;
                String a = temp.getString("ecg_data");
                if (a.equalsIgnoreCase("null")){
                    tempbyte = null;
                } else {
                    tempbyte = Base64.decodeBase64(temp.getString("ecg_data"));
                }
                queryResult.resultList[i].ecg_data= tempbyte;
                queryResult.resultList[i].ecg_info= temp.getString("ecg_info");
                queryResult.resultList[i].ecg_time= temp.getLong("ecg_time");
                queryResult.resultList[i].hp_data= temp.getString("hp_data");
                queryResult.resultList[i].lp_data= temp.getString("lp_data");
                queryResult.resultList[i].bloodpress_time=temp.getLong("bloodpress_time");
                queryResult.resultList[i].bloodpress_info=temp.getString("bloodpress_info");
                queryResult.resultList[i].glucose_data= temp.getString("glucose_data");
                queryResult.resultList[i].glucose_info= temp.getString("glucose_info");
                queryResult.resultList[i].glucose_time= temp.getLong("glucose_time");
                queryResult.resultList[i].temperature= temp.getString("temperature");
                queryResult.resultList[i].temperature_info= temp.getString("temperature_info");
                queryResult.resultList[i].temperature_time= temp.getLong("temperature_time");
                queryResult.resultList[i].date=temp.getString("date");
                queryResult.resultList[i].info= temp.getString("info");
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
        return queryResult;
    }

}
