package com.yuzhi.fine.fragment;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.interfaces.datasets.IDataSet;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.jxtii.wildebeest.bean.PubData;
import com.jxtii.wildebeest.model.CompreRecord;
import com.jxtii.wildebeest.model.PointRecord;
import com.jxtii.wildebeest.model.PositionRecord;
import com.jxtii.wildebeest.model.RouteLog;
import com.jxtii.wildebeest.util.CommUtil;
import com.jxtii.wildebeest.util.DateStr;
import com.jxtii.wildebeest.webservice.WebserviceClient;
import com.yuzhi.fine.R;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WildebeestFragment extends Fragment implements View.OnClickListener {

    String TAG = WildebeestFragment.class.getSimpleName();
    Context context;
    static final int GPS_OPEN_STATUS = 1;
    Timer timer;
    TimerTask timerTask;
    Typeface tf;
    String[] mParties = new String[]{
            "急加速", "急减速", "速度", "夜间驾驶", "行驶区域", "行驶时长", "行驶里程"
    };

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    int rapidAcc = 100;
                    int radidDec = 100;
                    int speeding = 100;
                    int v8 = 100;
                    int v9 = 100;
                    int v10 = 100;
                    int v11 = 100;

                    List<PointRecord> listSpeeding = DataSupport.where("eventType = ?", "1").find(PointRecord.class);
                    if (listSpeeding != null && listSpeeding.size() > 0) {
                        int sum = 0;
                        for (PointRecord pointRecord : listSpeeding) {
                            sum += pointRecord.getPoint();
                        }
                        speeding -= sum;
                        mParties[2] = mParties[2].substring(0,2) + "+" + listSpeeding.size();
                    }
                    speeding = speeding > 0 ? speeding : 0;

                    List<PointRecord> listRapidAcc = DataSupport.where("eventType = ?", "2").find(PointRecord.class);
                    if (listRapidAcc != null && listRapidAcc.size() > 0) {
                        int sum = 0;
                        for (PointRecord pointRecord : listRapidAcc) {
                            sum += pointRecord.getPoint();
                        }
                        rapidAcc -= sum;
                        mParties[0] = mParties[0].substring(0,3) + "+" + listRapidAcc.size();
                    }
                    rapidAcc = rapidAcc > 0 ? rapidAcc : 0;

                    List<PointRecord> listRapidDec = DataSupport.where("eventType = ?", "3").find(PointRecord.class);
                    if (listRapidDec != null && listRapidDec.size() > 0) {
                        int sum = 0;
                        for (PointRecord pointRecord : listRapidDec) {
                            sum += pointRecord.getPoint();
                        }
                        radidDec -= sum;
                        mParties[1] = mParties[1].substring(0,3) + "+" + listRapidDec.size();
                    }
                    radidDec = radidDec > 0 ? radidDec : 0;

                    double score = speeding * 0.25 + rapidAcc * 0.25 + radidDec * 0.35 + v8 * 0.05 + v9 * 0.05
                            + v10 * 0.025 + v11 * 0.025;

                    bbb.setText(CommUtil.floatToStr((float) score, 1));

                    int cnt = 7;
                    ArrayList<Entry> yVals1 = new ArrayList<Entry>();
                    yVals1.add(new Entry(rapidAcc, 0));
                    yVals1.add(new Entry(radidDec, 1));
                    yVals1.add(new Entry(speeding, 2));
                    yVals1.add(new Entry(v8, 3));
                    yVals1.add(new Entry(v9, 4));
                    yVals1.add(new Entry(v10, 5));
                    yVals1.add(new Entry(v11, 6));

                    ArrayList<String> xVals = new ArrayList<String>();

                    for (int i = 0; i < cnt; i++)
                        xVals.add(mParties[i % mParties.length]);

                    RadarDataSet set1 = new RadarDataSet(yVals1, "驾驶评分");
                    set1.setColor(ColorTemplate.COLORFUL_COLORS[0]);
                    set1.setFillColor(ColorTemplate.COLORFUL_COLORS[0]);
                    set1.setValueTextColor(ColorTemplate.COLORFUL_COLORS[1]);
                    set1.setValueTextSize(40);
                    set1.setDrawFilled(true);
                    set1.setLineWidth(2f);

                    ArrayList<IRadarDataSet> sets = new ArrayList<IRadarDataSet>();
                    sets.add(set1);

                    RadarData data = new RadarData(xVals, sets);
                    data.setValueTypeface(tf);
                    data.setValueTextSize(8f);
                    data.setDrawValues(false);

                    mChart.setData(data);
                    for (IDataSet<?> set : mChart.getData().getDataSets())
                        set.setDrawValues(!set.isDrawValuesEnabled());
//                    mChart.animateY(1400);

                    mChart.invalidate();

                    CompreRecord cr = DataSupport.findLast(CompreRecord.class);
                    if (cr != null) {
                        tvTravel.setText(CommUtil.floatToStr(cr.getTravelMeter(), 1));
                        tvMaxspeed.setText(CommUtil.floatToStr(cr.getMaxSpeed(), 1));
                        tvTimepass.setText(CommUtil.timeSpanHHmm(cr.getBeginTime(), cr.getCurrentTime()));
                        long timeFin = CommUtil.timeSpanSecond(cr.getBeginTime(), cr.getCurrentTime());
                        float aveSp = cr.getTravelMeter() * 18 / (timeFin * 5);
                        tvAvespeed.setText(CommUtil.floatToStr(aveSp, 1));
                    }else {
                        tvTravel.setText("");
                        tvMaxspeed.setText("");
                        tvTimepass.setText("");
                        tvAvespeed.setText("");
                    }
                    break;
            }
        }
    };

    @Bind(R.id.aaa)
    TextView aaa;
    @Bind(R.id.vw_title)
    RelativeLayout vwTitle;
    @Bind(R.id.tv_1)
    TextView tv1;
    @Bind(R.id.tv_2)
    TextView tv2;
    @Bind(R.id.tv_3)
    TextView tv3;
    @Bind(R.id.tv_4)
    TextView tv4;
    @Bind(R.id.tv_5)
    TextView tv5;
    @Bind(R.id.tv_6)
    TextView tv6;
    @Bind(R.id.tv_7)
    TextView tv7;
    @Bind(R.id.tv_8)
    TextView tv8;
    @Bind(R.id.tv_9)
    TextView tv9;
    @Bind(R.id.tv_10)
    TextView tv10;
    @Bind(R.id.vw_scroll)
    ScrollView vwScroll;
    @Bind(R.id.btn_finish)
    Button btnFinish;
    @Bind(R.id.btn_start)
    Button btnStart;
    @Bind(R.id.chart1)
    RadarChart mChart;
    @Bind(R.id.para_vw)
    LinearLayout paraVw;
    @Bind(R.id.bottom_vw)
    LinearLayout bottomVw;
    @Bind(R.id.tv_travel)
    TextView tvTravel;
    @Bind(R.id.tv_timepass)
    TextView tvTimepass;
    @Bind(R.id.tv_maxspeed)
    TextView tvMaxspeed;
    @Bind(R.id.tv_avespeed)
    TextView tvAvespeed;
    @Bind(R.id.bbb)
    TextView bbb;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wildebeest, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = getActivity();
        initData();
        initView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    void initData() {
        startReflash();
    }

    void initView() {
        btnStart.setOnClickListener(this);
        btnFinish.setOnClickListener(this);

        tf = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Regular.ttf");

        mChart.setDescription("你的数据");
        mChart.setWebLineWidth(1.5f);
        mChart.setWebLineWidthInner(0.75f);
        mChart.setWebAlpha(100);

        mChart.animateXY(
                1400, 1400,
                Easing.EasingOption.EaseInOutQuad,
                Easing.EasingOption.EaseInOutQuad);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTypeface(tf);
        xAxis.setTextSize(9f);

        YAxis yAxis = mChart.getYAxis();
        yAxis.setTypeface(tf);
        yAxis.setLabelCount(5, false);
        yAxis.setTextSize(9f);
        yAxis.setAxisMinValue(0f);

        Legend l = mChart.getLegend();
        l.setPosition(Legend.LegendPosition.RIGHT_OF_CHART);
        l.setTypeface(tf);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(5f);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                showToast("start");
                initLocService();
                uploadInitInfo();
                break;
            case R.id.btn_finish:
                showToast("end");
                stopLocService();
                uploadFinishInfo();
                break;
        }
    }

    void uploadInitInfo() {
        new Thread() {
            public void run() {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("sqlKey", "proc_init_route_info");
                params.put("sqlType", "proc");
                params.put("employeeId", "12345678");//TODO 员工ID需补充
                String paramStr = JSON.toJSONString(params);
                PubData pubData = new WebserviceClient().updateData(paramStr);
                if (pubData != null && "00".equals(pubData.getCode())) {
                    String proId = (String) pubData.getData().get("pr_route_id");
                    RouteLog log = new RouteLog();
                    log.setpRouteId(proId);
                    log.save();
                }
            }
        }.start();
    }

    void uploadFinishInfo() {
        new Thread() {
            public void run() {
                RouteLog log = DataSupport.findLast(RouteLog.class);
                CompreRecord cr = DataSupport.findLast(CompreRecord.class);
                if (log != null && cr != null) {
                    long timeFin = CommUtil.timeSpanSecond(cr.getBeginTime(), cr.getCurrentTime());
                    float aveSp = cr.getTravelMeter() * 18 / (timeFin * 5);
                    Map<String, Object> paramAfter = new HashMap<String, Object>();
                    paramAfter.put("sqlKey", "nosql");
                    paramAfter.put("sqlType", "nosql");
                    paramAfter.put("rRouteId", log.getpRouteId());
                    paramAfter.put("rHighSpeed", cr.getMaxSpeed());
                    paramAfter.put("rAveSpeed", aveSp);
                    paramAfter.put("rTravelMeter", cr.getTravelMeter());
                    Map<String, Object> config = new HashMap<String, Object>();
                    config.put("interfaceName", "pjRouteFactorFinish");
                    config.put("asyn", "false");
                    paramAfter.put("interfaceConfig", config);
                    String paramStr = JSON.toJSONString(paramAfter);
                    Log.w(TAG, "paramStr = " + paramStr);
                    PubData pubData = new WebserviceClient().loadData(paramStr);
                    Log.w(TAG, "pubData.getCode() = " + pubData.getCode());
                    if (pubData.getData() != null) {
                        Log.w(TAG, "pubData.getData() = " + JSON.toJSONString(pubData.getData()));
                    }
                }
            }
        }.start();
    }

    void initLocService() {
        if (CommUtil.isOpenGPS(context)) {
            showToast("已开启GPS！");
            startLocService();
        } else {
            showToast("请开启GPS！");
            Intent intent = new Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, GPS_OPEN_STATUS);
        }

    }

    void stopLocService() {
        stopReflash();

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(CommUtil.START_INTENT);
        intent.setPackage("com.yuzhi.fine");
        intent.putExtra("interval", 2000);
        //Implicit intents with startService are not safe
//        Intent intent = new Intent(CommUtil.START_INTENT);
        PendingIntent pt = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.cancel(pt);

        intent = new Intent();
        intent.setAction(CommUtil.STOP_INTENT);
        intent.setPackage("com.yuzhi.fine");
        intent.putExtra("interval", 2000);
        //Implicit intents with startService are not safe
//        intent = new Intent(CommUtil.STOP_INTENT);
        pt = PendingIntent.getBroadcast(context, 0, intent, 0);
        long triggerAtTime = SystemClock.elapsedRealtime() + 5 * 1000;
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pt);
    }

    void startLocService() {
        DataSupport.deleteAll(PositionRecord.class);
        DataSupport.deleteAll(CompreRecord.class);
        DataSupport.deleteAll(PointRecord.class);
        startReflash();

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long triggerAtTime = System.currentTimeMillis() + 5 * 1000;
        long interval = 15 * 60 * 1000;
        Intent intent = new Intent();
        intent.setAction(CommUtil.START_INTENT);
        intent.setPackage("com.yuzhi.fine");
        intent.putExtra("interval", 2000);
        //Implicit intents with startService are not safe
//        Intent intent = new Intent(CommUtil.START_INTENT);
        PendingIntent pt = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, interval, pt);

        /*Intent ii = new Intent();
        ii.setAction("com.jxtii.wildebeest.task_service");
        ii.setPackage("com.yuzhi.fine");
        //Implicit intents with startService are not safe
//        ii.setClass(getActivity(), TaskService.class);
        ii.putExtra("interval", 2000);
        getActivity().startService(ii);*/
    }

    void startReflash() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                handler.sendEmptyMessage(0);
            }
        };
        timer.scheduleAtFixedRate(timerTask, 2 * 1000, 5 * 1000);
    }

    void stopReflash() {
        if (timer != null)
            timer.cancel();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_OPEN_STATUS:
                Boolean isOpen = CommUtil.isOpenGPS(context);
                if (isOpen) {
                    showToast("已开启GPS！");
                    startLocService();
                } else {
                    showToast("请开启GPS！");
                    Intent intent = new Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(intent, GPS_OPEN_STATUS);
                }
                break;
        }
    }

    void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
