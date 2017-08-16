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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hqyj.dev.doctorforhealth.Bluetooth.BluetoothTools;
import com.hqyj.dev.doctorforhealth.DataBase.SharedPreferenceDB;
import com.hqyj.dev.doctorforhealth.R;
import com.hqyj.dev.doctorforhealth.Service.GetNodeInfoService;
import com.hqyj.dev.doctorforhealth.Service.GetWebInfoService;
import com.hqyj.dev.doctorforhealth.Service.Task;
import com.hqyj.dev.doctorforhealth.Web.JsonCommand;

import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2016/3/29.
 */
public class TempHistoryFragment extends Fragment{

    private String TAG = "TempHistoryFragment";
    private View view;
    private TextView title;
    private TemphReceiver temphReceiver;
    private Map<String,String> map = new HashMap<String,String>();
    private JsonCommand.QueryResult queryResult;
    private BluetoothTools btTools;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG,"temph11111111111111111111111111111111111111111111");
        view = inflater.inflate(R.layout.fragment_temphistoryfragment, (ViewGroup) getActivity().findViewById(R.id.viewpager),false);
        title = (TextView) view.findViewById(R.id.temphTitle);
        IntentFilter infilter = new IntentFilter();
        infilter.addAction("PatientInfo");
        infilter.addAction("NET_WEB_SERVICE_GET_DATA_TEMP_HIS");
        temphReceiver = new TemphReceiver();
        getActivity().registerReceiver(temphReceiver,infilter);

        Map<String,String> value = new HashMap<String,String>();
        value.put("patient_id", SharedPreferenceDB.getPatientInfo(getActivity()).patient_id);
        GetWebInfoService.newTask(new Task(getActivity(), Task.NET_WEB_SERVICE_GET_DATA_TEMP_HIS,new Object[]{"queryPaientResult",value}));
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
    private class TemphReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("NET_WEB_SERVICE_GET_DATA_TEMP_HIS")){
                String json = intent.getStringExtra("updataResult");
                Log.d(TAG,"json :" + json);
                if (json != null) {

                    queryResult = JsonCommand.getQueryResult(json);
                    if (queryResult == null && queryResult.code != 0) {
                        Message msg = new Message();
                        msg.what = 2;
                        msg.obj = queryResult.errStr;
                        handler.sendMessage(msg);
                        Log.e("WebService:", queryResult.errStr);
                        return;
                    }
                } else {
                    Message msg = new Message();
                    msg.what = 3;
                    msg.obj = "服务器访问出错！";
                    handler.sendMessage(msg);
                    return;
                }
                Message msg = new Message();
                msg.what = 4;
                msg.obj = queryResult;
                handler.sendMessage(msg);
            }
        }
    }
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    HashMap<String,String> map = (HashMap<String, String>) msg.obj;
                    String patient_name = map.get("patient_name");
                    title.setText(patient_name+"的体温结果");
                    break;
                case 2:
                    Toast.makeText(getActivity(),msg.obj.toString(),Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(getActivity(),msg.obj.toString(),Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    queryResult = (JsonCommand.QueryResult) msg.obj;
                    if(queryResult.count == 0){
//                        Toast.makeText(getActivity(), "没有历史数据！", Toast.LENGTH_LONG).show();
                        return;
                    }
                    LinearLayout layout = (LinearLayout) view.findViewById(R.id.tempDataViewLayout);
                    View v = getTempChartView(queryResult);
                    if(v == null){
                        Toast.makeText(getActivity(), "温度数据格式不正确！", Toast.LENGTH_LONG).show();

                        return ;
                    }
                    layout.addView(v);
                    break;
            }
        }
    };
    public View getTempChartView(JsonCommand.QueryResult queryPatientResult) {
        if (queryPatientResult == null)
            return null;

        if (queryPatientResult.count != 0) {
            ArrayList<Float> tempData = new ArrayList<Float>();
            ArrayList<Date> dates = new ArrayList<Date>();
            for (int i = 0; i < queryPatientResult.count; i++) {
                //System.out.println("i:" + queryPatientResult.resultList[i].temperature);
                float temp;
                try{
                    temp = Float.valueOf(queryPatientResult.resultList[i].temperature);
                }catch(Exception e){
                    Log.e(TAG, "温度数据格式不正确:" + queryPatientResult.resultList[i].temperature);
                    continue;
                }
                if(temp <= 0.1f)
                    continue;
                long temp_time = queryPatientResult.resultList[i].temperature_time;
                if(temp_time <= 0) {
                    Log.d(TAG, "temp_time");
                    continue;
                }
                dates.add(new Date(temp_time));// 获取时间
                tempData.add(temp);			   // 获取体温
            }

            return initChartView(tempData, dates, "体温数据", "日期", "温度（℃）");
        }
        return null;
    }
    private View initChartView(ArrayList<Float> tempData, ArrayList<Date> dates, String dataSpec, String xLabel, String yLabel) {

        if (tempData == null || dates == null)
            return null;

        final long HOUR = 3600 * 1000;
        final long DAY = HOUR * 24;
        double xMin = 0, xMax = 0, yMin = 0, yMax = 0;
        if(dates.size() != 0){
            xMin = dates.get(0).getTime() - 2 * DAY;
            yMin = tempData.get(0);
        }

        for (Date d : dates) {
            if (xMin > d.getTime()) {
                xMin = d.getTime();
            }

            if (xMax < d.getTime()) {
                xMax = d.getTime();
            }
        }

        for (double t : tempData) {
            if (yMin > t) {
                yMin = t;
            }

            if (yMax < t) {
                yMax = t;
            }
        }

        yMin -= 0.2;
        yMax += 0.2;

        xMax += 2 * DAY;

        // 获得图表初始化引擎
        XYMultipleSeriesRenderer renderer = buildRenderer();

        SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd");
        for (int i = 0; i < tempData.size(); i++) {
            renderer.addXTextLabel(dates.get(0).getTime(), df.format(dates.get(0)));
        }

        setChartSettings(renderer, xLabel, yLabel, xMin, xMax, yMin, yMax, Color.BLACK, Color.BLACK);

        XYMultipleSeriesDataset dataset = buildDateDataset("体温", dates, tempData);
        View view = ChartFactory.getTimeChartView(getActivity(), dataset, renderer,"yy-MM-dd");
        return view;
    }
    protected XYMultipleSeriesDataset buildDateDataset(String titles,ArrayList<Date> dates, ArrayList<Float> tempData) {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        TimeSeries series = new TimeSeries(titles);
        int seriesLength = dates.size();
        for (int k = 0; k < seriesLength; k++) {
            // 当float -> double类型赋值时，因为补位会丢失精度
            BigDecimal b = new BigDecimal(String.valueOf(tempData.get(k)));
            series.add(dates.get(k), b.doubleValue());
        }
        dataset.addSeries(series);
        return dataset;
    }
    protected void setChartSettings(XYMultipleSeriesRenderer renderer,
                                    String xTitle, String yTitle, double xMin, double xMax,
                                    double yMin, double yMax, int axesColor, int labelsColor) {
        // 设置整个图表效果
        renderer.setXTitle(xTitle);
        renderer.setYTitle(yTitle);
        renderer.setMargins(new int[] { 30, 70, 20, 10 });
        renderer.setMarginsColor(Color.WHITE);
        // 轴颜色
        renderer.setAxesColor(axesColor);
        // 轴字体大小
        renderer.setAxisTitleTextSize(28);

        // 标签属性
        renderer.setLabelsTextSize(18);
        renderer.setLabelsColor(labelsColor);
        renderer.setXLabelsColor(Color.BLACK);
        renderer.setYLabelsColor(0, Color.BLACK);
        renderer.setXLabelsAlign(Paint.Align.RIGHT);
        renderer.setYLabelsAlign(Paint.Align.RIGHT);

        // XY轴最大值和最小值
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);

        renderer.setLegendTextSize(28);
        renderer.setPointSize(5f);
        renderer.setShowGrid(false);
        renderer.setGridColor(Color.LTGRAY);
        renderer.setShowGridX(true);
        renderer.setXLabelsAngle(45);
        renderer.setXLabelsAlign(Paint.Align.LEFT);

        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setLineWidth(2);
        r.setColor(Color.RED);
        r.setDisplayChartValues(true);
        r.setDisplayChartValuesDistance(40);
        r.setChartValuesTextSize(28);
        r.setFillPoints(true);
        r.setPointStyle(PointStyle.CIRCLE);
        // r.setPointStyle(styles[i]);
        renderer.addSeriesRenderer(r);

    }
    protected XYMultipleSeriesRenderer buildRenderer() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        // setRenderer(renderer, color);
        return renderer;
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(temphReceiver);
    }
}
