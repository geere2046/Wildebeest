package com.jxtii.wildebeest.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.jxtii.wildebeest.bean.PointRecordBus;
import com.jxtii.wildebeest.bean.PubData;
import com.jxtii.wildebeest.bean.RouteFinishBus;
import com.jxtii.wildebeest.core.AMAPLocalizer;
import com.jxtii.wildebeest.model.CompreRecord;
import com.jxtii.wildebeest.model.NoGpsInfo;
import com.jxtii.wildebeest.model.PointRecord;
import com.jxtii.wildebeest.model.PositionRecord;
import com.jxtii.wildebeest.model.RouteLog;
import com.jxtii.wildebeest.util.CommUtil;
import com.jxtii.wildebeest.util.DateStr;
import com.jxtii.wildebeest.util.DistanceUtil;
import com.jxtii.wildebeest.webservice.WebserviceClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by huangyc on 2016/3/4.
 */
public class TaskService extends Service {

    String TAG = TaskService.class.getSimpleName();
    Context ctx;
    AMAPLocalizer amapLocalizer;
    Timer mTimer;
    TimerTask mTimerTask;
    int interval = 900;
    PowerManager.WakeLock m_wakeLockObj;
    AMapLocation amapLocation;
    AMapLocation amapLocationClone;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w(TAG, ">>>>>>>onCreate service");
        ctx = TaskService.this;
        amapLocalizer = AMAPLocalizer.getInstance(ctx);
        EventBus.getDefault().register(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.w(TAG, ">>>>>>>onStartCommand intent is null");
//            stopSelfSevice();
        } else {
            interval = intent.getIntExtra("interval", 30 * 1000);
            Log.w(TAG, ">>>>>>>onStartCommand interval = " + interval);
            if (amapLocalizer != null)
                amapLocalizer.setLocationManager(true, "gps", interval);
            stopTimer();
            if (mTimer == null)
                mTimer = new Timer();
            if (mTimerTask == null) {
                mTimerTask = new TimerTask() {
                    public void run() {
                        acquireWakeLock(ctx);
                        uploadLocInfo();
                        isNeedFinish();
                        releaseWakeLock();
                    }
                };
            }
            mTimer.scheduleAtFixedRate(mTimerTask, 1 * 1000,
                    interval);
        }
        return START_STICKY;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void oreceiveAMapLocation(AMapLocation amapLocation){
        Log.w(TAG, "AMapLocation is " + amapLocation.toStr());
        this.amapLocation = amapLocation;
        this.amapLocationClone = amapLocation;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void receivePointRecordBus(PointRecordBus bus) {
        Log.w(TAG, "PointRecordBus is " + bus.toStr());
        Map<String, Object> params = new HashMap<String, Object>();
        RouteLog log = DataSupport.findLast(RouteLog.class);
        if (log != null) {
            params.put("rRouteId", log.getpRouteId());
            if (this.amapLocationClone != null) {
                params.put("rLat", this.amapLocationClone.getLatitude());
                params.put("rLon", this.amapLocationClone.getLongitude());
                params.put("rAlt", this.amapLocationClone.getAltitude());
                if (bus.getEventType() == 1) {
                    params.put("sqlKey", "nosql");
                    params.put("sqlType", "nosql");
                    params.put("rSpeed", Double.valueOf(String.valueOf(bus.getRecord())));
                    params.put("rType", "00");
                    params.put("rAccelerate", 0.0);
                    params.put("geoType", "gcj");
                    Map<String, Object> config = new HashMap<String, Object>();
                    config.put("interfaceName", "pjRouteFactorSpeeding");
                    config.put("asyn", "false");
                    params.put("interfaceConfig", config);
                    String paramStr = JSON.toJSONString(params);
                    Log.w(TAG, "paramStr = " + paramStr);
                    PubData pubData = new WebserviceClient().loadData(paramStr);
                    Log.w(TAG, "pubData.getCode() = " + pubData.getCode());
                    if(pubData.getData() != null){
                        Log.w(TAG, "pubData.getData() = " + JSON.toJSONString(pubData.getData()));
                    }
                } else {
                    if (bus.getEventType() == 2) {
                        params.put("rAccelerate", Double.valueOf(String.valueOf(bus.getRecord())));
                        params.put("rType", "01");
                        params.put("rSpeed", 0.0);
                    } else if (bus.getEventType() == 3) {
                        params.put("rAccelerate", Double.valueOf(String.valueOf(bus.getRecord())));
                        params.put("rType", "02");
                        params.put("rSpeed", 0.0);
                    }
                    params.put("sqlKey", "nosql");
                    params.put("sqlType", "nosql");
                    params.put("geoType", "gcj");
                    Map<String, Object> config = new HashMap<String, Object>();
                    config.put("interfaceName", "pjRouteFactorInterface");
                    config.put("asyn", "false");
                    params.put("interfaceConfig", config);
                    String paramStr = JSON.toJSONString(params);
                    Log.w(TAG, "paramStr = " + paramStr);
                    PubData pubData = new WebserviceClient().loadData(paramStr);
                    Log.w(TAG, "pubData.getCode() = " + pubData.getCode());
                    if(pubData.getData() != null){
                        Log.w(TAG, "pubData.getData() = " + JSON.toJSONString(pubData.getData()));
                    }
                }
                uploadRouteLocation(this.amapLocationClone, log);
                this.amapLocationClone = null;
            } else {
                params.put("rLat", 0.0);
                params.put("rLon", 0.0);
                params.put("rAlt", 0.0);
                Log.i(TAG, "this.amapLocationClone is null");
            }
        }
    }

    /**
     * 上报定位信息
     */
    void uploadLocInfo() {
        try {
            String locinfo = (amapLocalizer != null) ? amapLocalizer.locinfo : "";
            if (!TextUtils.isEmpty(locinfo)) {
                locinfo = "";
                Log.w(TAG, locinfo);
            }
            if (this.amapLocation != null) {
                RouteLog log = DataSupport.findLast(RouteLog.class);
                if(log != null){
                    uploadRouteLocation(this.amapLocation, log);
                    this.amapLocation = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void uploadRouteLocation(AMapLocation aMapLocation,RouteLog log) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sqlKey", "nosql");
        params.put("sqlType", "nosql");
        params.put("rRouteId", log.getpRouteId());
        params.put("rLat", aMapLocation.getLatitude());
        params.put("rLon", aMapLocation.getLongitude());
        params.put("rAlt", aMapLocation.getAltitude());
        params.put("rSpeed", aMapLocation.getSpeed());
        params.put("rAccelerate", 0);
        params.put("addr", aMapLocation.getAddress());
        params.put("loctime", DateStr.yyyymmddHHmmssStr());
        Map<String, Object> config = new HashMap<String, Object>();
        config.put("interfaceName", "pjRouteLocation");
        config.put("asyn", "false");
        params.put("interfaceConfig", config);
        String paramStr = JSON.toJSONString(params);
        Log.d(TAG, "paramStr = " + paramStr);
        PubData pubData = new WebserviceClient().loadData(paramStr);
        Log.i(TAG, "pubData.getCode() = " + pubData.getCode());
        if (pubData != null && "00".equals(pubData.getCode())) {
            if (pubData.getData() != null) {
                Log.i(TAG, "pubData.getData() = " + JSON.toJSONString(pubData.getData()));
                if (pubData.getData().get("msgCode") != null && "2".equals(pubData.getData().get("msgCode").toString())) {
                    Log.w(TAG, "调用结束路线逻辑");
                    correctRouteFinish(log);
                } else if (pubData.getData().get("msgCode") != null && "1".equals(pubData.getData().get("msgCode").toString())) {
                    Log.i(TAG, "pubData.getData().msgContent = " + pubData.getData().get("msgContent").toString());
                }
            }
        }
    }

    /**
     * 过滤脏数据，修正路线结束信息
     * @param log
     */
    void correctRouteFinish(RouteLog log) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sqlKey", "sql_max_location_time");
        params.put("sqlType", "sql");
        params.put("rRouteId", log.getpRouteId());
        String paramStr = JSON.toJSONString(params);
        Log.d(TAG, "paramStr = " + paramStr);
        PubData pubData = new WebserviceClient().loadData(paramStr);
        Log.i(TAG, "pubData.getCode() = " + pubData.getCode());
        if (pubData != null && "00".equals(pubData.getCode())) {
            if (pubData.getData() != null) {
                Log.i(TAG, "pubData.getData() = " + JSON.toJSONString(pubData.getData()));
                if (pubData.getData().get("location_time") != null) {
                    String maxTime = pubData.getData().get("location_time").toString();
                    DataSupport.deleteAll(PositionRecord.class, "dateStr >= ?", maxTime);
                    List<PositionRecord> listIn = DataSupport.select("dateStr").order("dateStr desc").limit(1).find(PositionRecord.class);
                    if (listIn != null && listIn.size() > 0) {
                        CompreRecord cr = new CompreRecord();
                        cr.setCurrentTime(listIn.get(0).getDateStr());
                        CompreRecord lastCr = DataSupport.findLast(CompreRecord.class);
                        if (lastCr != null) {
                            List<PositionRecord> listSpeed = DataSupport.select("speed").order("speed desc").limit(1).find(PositionRecord.class);
                            if (listSpeed != null && listSpeed.size() > 0) {
                                cr.setMaxSpeed(listSpeed.get(0).getSpeed());
                            } else {
                                cr.setMaxSpeed(lastCr.getMaxSpeed());
                            }
                            List<PositionRecord> listAll = DataSupport.select("lat", "lng").order("dateStr desc").limit(1).find(PositionRecord.class);
                            if (listAll != null && listAll.size() > 0) {
                                float tr = 0;
                                PositionRecord mid = null;
                                for (PositionRecord pr : listAll) {
                                    if (mid == null) {
                                        mid = pr;
                                    } else {
                                        tr += (float) DistanceUtil.distance(pr.getLng(), pr.getLat(), mid.getLng(), mid.getLat());
                                    }
                                }
                            } else {
                                cr.setTravelMeter(lastCr.getTravelMeter());
                            }
                            cr.setSaveLat(lastCr.getSaveLat());
                            cr.setSaveLng(lastCr.getSaveLng());
                            cr.update(lastCr.getId());
                        }
                    }
                }
            }
        }
        uploadFinishInfo();
    }

    /**
     * gps已关闭或超过CommUtil.NOGPS_TIME显示gps没信号或速度为0km/h
     */
    void isNeedFinish(){
        Boolean isOpen = CommUtil.isOpenGPS(ctx);
        if(!isOpen){
            uploadFinishInfo();
        }else{
            NoGpsInfo noGpsInfo = null;
            List<NoGpsInfo> listInfo = DataSupport.select("id", "noGpsTime").order("noGpsTime desc").limit(2).find(NoGpsInfo.class);
            if (listInfo != null && listInfo.size() > 0) {
                noGpsInfo = listInfo.get(0);
            }
            if(noGpsInfo != null){
                String last = noGpsInfo.getNoGpsTime();
                long max = CommUtil.timeSpanSecond(last, DateStr.yyyymmddHHmmssStr());
                if (max > CommUtil.NOGPS_TIME) {
                    uploadFinishInfo();
                }
            }
        }
    }

    /**
     * 完成线路算分
     */
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
                    if (pubData != null && "00".equals(pubData.getCode())) {
                        if (pubData.getData() != null) {
                            Log.w(TAG, "pubData.getData() = " + JSON.toJSONString(pubData.getData()));
                            if (pubData.getData().get("msgCode") != null && "0".equals(pubData.getData().get("msgCode").toString())) {
                                deleteAll();
                                RouteFinishBus rfBus = new RouteFinishBus();
                                rfBus.setRouteId(log.getpRouteId());
                                rfBus.setFinishTime(DateStr.yyyymmddHHmmssStr());
                                EventBus.getDefault().post(rfBus);
                            }
                        }
                    }
                }
            }
        }.start();
    }

    void deleteAll(){
        DataSupport.deleteAll(RouteLog.class);
        DataSupport.deleteAll(PositionRecord.class);
        DataSupport.deleteAll(CompreRecord.class);
        DataSupport.deleteAll(PointRecord.class);
        DataSupport.deleteAll(NoGpsInfo.class);
    }

    public void onDestroy() {
        Log.i(TAG, ">>>>>>>>  onDestroy");
        super.onDestroy();
//        stopSelfSevice();
    }

    public void onLowMemory() {
        Log.i(TAG,">>>>>>>>  onLowMemory");
        super.onLowMemory();
    }

    public void onTrimMemory(int level) {
        Log.i(TAG,">>>>>>>>  onTrimMemory");
        super.onTrimMemory(level);
    }

    void stopSelfSevice() {
        Log.i(TAG,">>>>>>>>  stopSelfSevice");
        //TODO 注释，可能影响服务正常启动
//        if (amapLocalizer != null) {
//            amapLocalizer.setLocationManager(false, "", 0);
//        }
//        AlarmManager am = (AlarmManager) this
//                .getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent();
//        intent.setAction(CommUtil.START_INTENT);
//        intent.setPackage(ctx.getPackageName());
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1,
//                intent, 0);
//        long triggerAtTime = SystemClock.elapsedRealtime() + 30 * 1000;
//        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime,
//                pendingIntent);
        stopTimer();
        EventBus.getDefault().unregister(this);
        this.stopSelf();
    }

    void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    public void acquireWakeLock(Context cxt) {
        Log.d(TAG, ">>>>>>点亮屏幕");
        if (m_wakeLockObj == null) {
            PowerManager pm = (PowerManager) cxt
                    .getSystemService(Context.POWER_SERVICE);
            m_wakeLockObj = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK
                    | PowerManager.ACQUIRE_CAUSES_WAKEUP
                    | PowerManager.ON_AFTER_RELEASE, TAG);
            m_wakeLockObj.acquire();
        }
    }

    public void releaseWakeLock() {
        Log.d(TAG, ">>>>>>取消点亮");
        if (m_wakeLockObj != null && m_wakeLockObj.isHeld()) {
            m_wakeLockObj.setReferenceCounted(false);
            m_wakeLockObj.release();
            m_wakeLockObj = null;
        }
    }
}
