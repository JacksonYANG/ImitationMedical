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
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Administrator on 2016/3/29.
 */
public class BPHistoryFragment extends Fragment{

    private String TAG = "BPHistoryFragment";
    private View view;
    private TextView title;
    private BPHReceiver bphReceiver;
    private Map<String,String> map = new HashMap<String,String>();
    private JsonCommand.QueryResult queryResult;
    private final long HOUR = 3600 * 1000;
    private final long DAY = HOUR * 24;
    private BluetoothTools btTools;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG,"bph11111111111111111111111111111111111111111111");
        view = inflater.inflate(R.layout.fragment_bphistoryfragment, (ViewGroup) getActivity().findViewById(R.id.viewpager),false);
        title = (TextView) view.findViewById(R.id.bphTitle);
        IntentFilter infilter = new IntentFilter();
        infilter.addAction("PatientInfo");
        infilter.addAction("NET_WEB_SERVICE_GET_DATA_BP_HIS");
        bphReceiver = new BPHReceiver();
        getActivity().registerReceiver(bphReceiver,infilter);

        Map<String, String> value = new HashMap<String, String>();
        value.put("patient_id", SharedPreferenceDB.getPatientInfo(getActivity()).patient_id);
        GetWebInfoService.newTask(new Task(getActivity(),Task.NET_WEB_SERVICE_GET_DATA_BP_HIS,new Object[]{"queryPaientResult", value}));
        map.put("patient_name", SharedPreferenceDB.getPatientInfo(getActivity()).patient_name);
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
    private class BPHReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals("NET_WEB_SERVICE_GET_DATA_BP_HIS")){
                String json = intent.getStringExtra("updataResult");
                Log.d(TAG,"json :"+json);
                if (json != null) {
                    queryResult = JsonCommand.getQueryResult(json);
                    if (queryResult == null
                            && queryResult.code != 0) {
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
                handler.sendEmptyMessage(4);
            }
        }
    }
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    HashMap<String,String> map = (HashMap<String, String>) msg.obj;
                    String patient_name = map.get("patient_name");
                    title.setText(patient_name+"的血压结果");
                    break;
                case 2:
                    Toast.makeText(getActivity(),msg.obj.toString(),Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(getActivity(),msg.obj.toString(),Toast.LENGTH_SHORT).show();
                    break;
                case 4:
                    LinearLayout layout = (LinearLayout) getActivity().findViewById(R.id.bpDataViewLayout);
                    View view = getBPChartView(queryResult);
                    if(view == null){
//                        Toast.makeText(getActivity(), "没有历史数据！", Toast.LENGTH_LONG).show();
                        return;
                    }
                    layout.addView(view);
                    break;
            }
        }
    };
    public View getBPChartView(JsonCommand.QueryResult queryPatientResult) {

        if (queryPatientResult == null)
            return null;

        if (queryPatientResult.count > 0) {
            // 高压、低压数据
            ArrayList<HashMap<String, Float>> bpData = new ArrayList<HashMap<String, Float>>();
            // 对应日期数据
            ArrayList<Date> dates = new ArrayList<Date>();
            HashMap<String, Float> map;
            for (int i = 0; i < queryPatientResult.count; i++) {
                float hp, lp;
                try{
                    hp = Float
                            .valueOf(queryPatientResult.resultList[i].hp_data);
                    lp = Float
                            .valueOf(queryPatientResult.resultList[i].lp_data);

                }catch(Exception e){
                    continue;
                }

                if(hp < 0.1f || lp < 0.1f){
                    continue;
                }

                long bp_time = queryPatientResult.resultList[i].bloodpress_time;
                if(bp_time <= 0)
                    continue;

                dates.add(new Date(bp_time));// 获取时间
                // 获得血压数据
                map = new HashMap<String, Float>();
                map.put("hp", hp);
                map.put("lp", lp);
                bpData.add(map);
            }

            if(bpData.size() == 0 || dates.size() == 0)
                return null;

            return initChartView(bpData, dates, "血压数据", "日期", "血压（mmHg)");
        }
        return null;
    }
    private View initChartView(ArrayList<HashMap<String, Float>> bpData, ArrayList<Date> dates, String dataSpec,
                               String xLabel, String yLabel) {

        if (bpData == null || dates == null)
            return null;

        double xMin = 0, xMax = 0, yMin = 0, yMax = 0;
        // 找到日期最小值再向前两天
        xMin = dates.get(0).getTime() - 2 * DAY;
        for (Date d : dates) {
            if(d == null)
                continue;
            if (xMin > d.getTime()) {
                xMin = d.getTime();
            }

            if (xMax < d.getTime()) {
                xMax = d.getTime();
            }
        }

        // 血压最小值
        yMin = bpData.get(0).get("lp");
        Iterator<HashMap<String, Float>> iterator = bpData.iterator();
        while(iterator.hasNext()){
            HashMap<String, Float> ent = iterator.next();
            float hp = ent.get("hp");
            float lp = ent.get("lp");

            // 如果血压太小，则放弃数据
            if(lp <= 0.1f)
                continue;

            // 找出低压最低值
            if (yMin > lp) {
                yMin = lp;
            }

            // 找出高压最高值
            if (yMax < hp) {
                yMax = hp;
            }
        }

        // 为了防止高压、低压都在最上边和最下边
        yMin -= 5;
        yMax += 5;

        // 日期增加2天，防止结果在最右边
        xMax += 2 * DAY;

        // 获得图表初始化引擎
        XYMultipleSeriesRenderer renderer = buildRenderer();

        SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd");
        for (int i = 0; i < dates.size(); i++) {
            renderer.addXTextLabel(dates.get(i).getTime(), df.format(dates.get(i)));
        }

        setChartSettings(renderer, xLabel, yLabel, xMin, xMax, yMin, yMax,
                Color.BLACK, Color.BLACK);

        XYMultipleSeriesDataset dataset = buildDateDataset("收缩压", "舒张压", dates,
                bpData);
        View view = ChartFactory.getTimeChartView(getActivity(), dataset, renderer,
                "yy-MM-dd");
        return view;
    }
    protected XYMultipleSeriesDataset buildDateDataset(String titles0, String title1,
                                                       ArrayList<Date> dates, ArrayList<HashMap<String, Float>> bpData) {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        TimeSeries series;
        series = new TimeSeries(titles0);
        int seriesLength = dates.size();
        for (int k = 0; k < seriesLength; k++) {
            // 当float -> double类型赋值时，因为补位会丢失精度
            BigDecimal b = new BigDecimal(String.valueOf(bpData.get(k).get("hp")));
            series.add(dates.get(k), b.doubleValue());
        }
        dataset.addSeries(series);

        series = new TimeSeries(title1);
        for (int k = 0; k < seriesLength; k++) {
            // 当float -> double类型赋值时，因为补位会丢失精度
            BigDecimal b = new BigDecimal(String.valueOf(bpData.get(k).get("lp")));
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

        // first Series
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

        // second Series
        r = new XYSeriesRenderer();
        r.setLineWidth(2);
        r.setColor(Color.BLUE);
        r.setDisplayChartValues(true);
        r.setDisplayChartValuesDistance(40);
        r.setChartValuesTextSize(28);
        r.setFillPoints(true);
        r.setPointStyle(PointStyle.DIAMOND);
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
        getActivity().unregisterReceiver(bphReceiver);
    }
}
