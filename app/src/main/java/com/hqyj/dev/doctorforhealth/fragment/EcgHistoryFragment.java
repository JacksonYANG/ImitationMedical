package com.hqyj.dev.doctorforhealth.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hqyj.dev.doctorforhealth.Bluetooth.BluetoothTools;
import com.hqyj.dev.doctorforhealth.DataBase.SharedPreferenceDB;
import com.hqyj.dev.doctorforhealth.PaintTools.Utils;
import com.hqyj.dev.doctorforhealth.R;
import com.hqyj.dev.doctorforhealth.Service.GetNodeInfoService;
import com.hqyj.dev.doctorforhealth.Service.GetWebInfoService;
import com.hqyj.dev.doctorforhealth.Service.Task;
import com.hqyj.dev.doctorforhealth.Web.JsonCommand;

import org.achartengine.ChartFactory;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/3/29.
 */
public class EcgHistoryFragment extends Fragment{

    private String TAG = "EcgHistoryFragment";
    private View view;
    private TextView title;
    private EcgHReceiver ecghReceiver;
    private Map<String,String> map = new HashMap<String,String>();
    private JsonCommand.QueryResult queryResult;
    private BluetoothTools btTools;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG,"ecgh11111111111111111111111111111111111111111111");
        view = inflater.inflate(R.layout.fragment_ecghistoryfragment, (ViewGroup) getActivity().findViewById(R.id.viewpager),false);
        title = (TextView) view.findViewById(R.id.ecghTitle);
        IntentFilter infilter = new IntentFilter();
        infilter.addAction("PatientInfo");
        infilter.addAction("NET_WEB_SERVICE_GET_DATA_ECG_HIS");
        ecghReceiver = new EcgHReceiver();
        getActivity().registerReceiver(ecghReceiver,infilter);

        Map<String, String> value = new HashMap<String, String>();
        value.put("patient_id", SharedPreferenceDB.getPatientInfo(getActivity()).patient_id);
        GetWebInfoService.newTask(new Task(getActivity(), Task.NET_WEB_SERVICE_GET_DATA_ECG_HIS, new Object[]{"queryPaientResult", value}));
        map.put("patient_name",SharedPreferenceDB.getPatientInfo(getActivity()).patient_name);
        Message msg = new Message();
        msg.what = 1;
        msg.obj = map;
        handler.sendMessage(msg);

        btTools = BluetoothTools.getmBluetoothTools(getActivity());
        if(btTools.getmInputStream() == null || btTools.getmOutputStream() == null || btTools.getmBluetoothSocket() == null){
            Log.d(TAG, "in out is null");
            getActivity().stopService(new Intent(getActivity(),GetNodeInfoService.class));
            getActivity().sendBroadcast(new Intent("connectblue"));
        }else{
            Log.d(TAG,"in out socket not null");
        }

        return view;
    }
    private class EcgHReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals("NET_WEB_SERVICE_GET_DATA_ECG_HIS")){
                String json = intent.getStringExtra("updataResult");
                if (json != null) {
                    System.out.println("result:" + json);
                    queryResult = JsonCommand.getQueryResult(json);
                    if(queryResult == null && queryResult.code != 0){
                        handler.sendEmptyMessage(4);
                        return;
                    }

                }else{
                    handler.sendEmptyMessage(2);
                    return;
                }
                handler.sendEmptyMessage(3);
            }
        }
    }
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    HashMap<String,String> map = (HashMap<String, String>) msg.obj;
                    String patient_name = map.get("patient_name");
                    title.setText(patient_name+"的心电结果");
                    break;
                case 2:
                    Toast.makeText(getActivity(), "服务器访问出错！", Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    if(queryResult.count != 0) {
                        long[] time = new long[queryResult.count];
                        int count = 0;
                        long max = 0;

                        if (queryResult == null) {
                            return;
                        }
                        if (queryResult.count != 0) {
                            for (int i = 0; i < queryResult.count; i++) {
                                if (queryResult.resultList[i].ecg_data == null || queryResult.resultList[i].ecg_data.length <= 0)
                                    continue;
                                long ecg_time = queryResult.resultList[i].ecg_time;
                                if (ecg_time <= 0) {
                                    continue;
                                }
                                time[i] = ecg_time;
                            }
                        }
                        for (int i = 0; i < time.length; i++) {
                            if (max < time[i]) {
                                max = time[i];
                                count = i;
                            }
                        }
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd\nhh:mm:ss");
                        Date datedate = new Date(max);
                        String date = formatter.format(datedate);

                        byte[] data = queryResult.resultList[count].ecg_data;
//                        Log.d("AGC", data.toString());

                        if (data == null) {
                            Log.d(">>>", "No Ecg data found！");
//                            Toast.makeText(getActivity(), "没有心电数据", Toast.LENGTH_LONG).show();
                            return;
                        }
                        LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.ecgDataViewLayout);
                        layout.addView(initChartView(Utils.convertByteArrToIntArr(data), date, "时间（s）", "电压（mv）"));

                    }
                    break;
                case 4:
                    Toast.makeText(getActivity(), queryResult.errStr, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };
    private View initChartView(int[] ecgData, String dataSpec, String xLabel, String yLabel){

        // 设置心电图线颜色
        int color = Color.BLACK;

        // 获得图表初始化引擎
        XYMultipleSeriesRenderer renderer = buildRenderer(color);

        int length = renderer.getSeriesRendererCount();
        for (int i = 0; i < length; i++) {
            ((XYSeriesRenderer) renderer.getSeriesRendererAt(i)).setFillPoints(true);
        }

        setChartSettings(renderer, xLabel, yLabel,
                0, 2, 0.005, 2.5,
                Color.BLACK, Color.BLUE);

        DisplayMetrics dm;
        dm = getResources().getDisplayMetrics();
        float width = (float) dm.widthPixels;
        int height =  dm.heightPixels;
        Log.d(">>>>", "width = " + width + " height = " + height);
        if(width/height > 1.68){   // for screen size is 16:9
            Log.d("EcgResultActiivty", "Screen 16:9");
            renderer.setXLabels(11);
            renderer.setYLabels(6);
        }else {
            Log.d("EcgResultActiivty", "Screen 5:3");
            renderer.setXLabels(12);
            renderer.setYLabels(6);
        }
        renderer.setAxesColor(Color.RED);
        renderer.setShowGrid(true);
        renderer.setXLabelsAlign(Paint.Align.RIGHT);
        renderer.setYLabelsAlign(Paint.Align.RIGHT);

        XYMultipleSeriesDataset dataset = buildDataset(dataSpec, ecgData);
        XYSeries series = dataset.getSeriesAt(0);

        View view = ChartFactory.getLineChartView(getActivity(), dataset, renderer);
        return view;
    }
    protected void setChartSettings(XYMultipleSeriesRenderer renderer, String xTitle, String yTitle, double xMin, double xMax, double yMin, double yMax, int axesColor, int labelsColor) {
        renderer.setXTitle(xTitle);
        renderer.setYTitle(yTitle);
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setAxesColor(axesColor);
        renderer.setXLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0, Color.BLACK);
        renderer.setLabelsColor(labelsColor);
    }
    protected XYMultipleSeriesDataset buildDataset(String title, int[] ecgData) {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();

        // 设置数据
        XYSeries serie = new XYSeries(title);
        int seriesLength = ecgData.length;
        for (int i = 0; i < seriesLength; i++) {
            serie.add(i*0.005, ecgData[i] * 0.005);
        }
        dataset.addSeries(serie);


        return dataset;
    }
    protected XYMultipleSeriesRenderer buildRenderer(int color) {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        setRenderer(renderer, color);
        return renderer;
    }

    protected void setRenderer(XYMultipleSeriesRenderer renderer, int color) {
        renderer.setMarginsColor(Color.WHITE);
        renderer.setGridColor(Color.RED);
        renderer.setAxisTitleTextSize(28);
        renderer.setChartTitleTextSize(40);
        renderer.setLabelsTextSize(18);
        renderer.setLegendTextSize(28);
        //renderer.setPointSize(3f);
        renderer.setMargins(new int[] { 60, 60, 20, 10 });
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setLineWidth(2);
        r.setColor(color);
        //r.setPointStyle(styles[i]);
        renderer.addSeriesRenderer(r);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(ecghReceiver);
    }
}
