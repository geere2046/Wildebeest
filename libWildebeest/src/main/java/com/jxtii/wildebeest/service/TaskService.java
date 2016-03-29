package com.jxtii.wildebeest.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.jxtii.wildebeest.bean.PointRecordBus;
import com.jxtii.wildebeest.bean.PubData;
import com.jxtii.wildebeest.core.AMAPLocalizer;
import com.jxtii.wildebeest.model.RouteLog;
import com.jxtii.wildebeest.util.DateStr;
import com.jxtii.wildebeest.webservice.WebserviceClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.litepal.crud.DataSupport;

import java.util.HashMap;
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
            stopSelfSevice();
        } else {
            interval = intent.getIntExtra("interval", 900);
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
                Map<String, Object> paramAfter = new HashMap<String, Object>();
                paramAfter.put("sqlKey", "nosql");
                paramAfter.put("sqlType", "nosql");
                paramAfter.put("rRouteId", log.getpRouteId());
                paramAfter.put("rLat", this.amapLocationClone.getLatitude());
                paramAfter.put("rLon", this.amapLocationClone.getLongitude());
                paramAfter.put("rAlt", this.amapLocationClone.getAltitude());
                paramAfter.put("rSpeed", this.amapLocationClone.getSpeed());
                paramAfter.put("rAccelerate", 0);
                paramAfter.put("addr", this.amapLocationClone.getAddress());
                paramAfter.put("loctime", DateStr.yyyymmddHHmmssStr());
                Map<String, Object> config = new HashMap<String, Object>();
                config.put("interfaceName", "pjRouteLocation");
                config.put("asyn", "false");
                paramAfter.put("interfaceConfig", config);
                String paramStr = JSON.toJSONString(paramAfter);
                Log.w(TAG, "paramStr = " + paramStr);
                PubData pubData = new WebserviceClient().loadData(paramStr);
                Log.w(TAG, "pubData.getCode() = " + pubData.getCode());
                if(pubData.getData() != null){
                    Log.w(TAG, "pubData.getData() = " + JSON.toJSONString(pubData.getData()));
                }
                this.amapLocationClone = null;
            } else {
                params.put("rLat", 0.0);
                params.put("rLon", 0.0);
                params.put("rAlt", 0.0);
                Log.i(TAG, "this.amapLocationClone is null");
            }
        }
    }

    void uploadLocInfo() {
        try {
            String locinfo = (amapLocalizer != null) ? amapLocalizer.locinfo : "";
            if (!TextUtils.isEmpty(locinfo)) {
                locinfo = "";
                Log.w(TAG, locinfo);
            }
            if (this.amapLocation != null) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("sqlKey", "nosql");
                params.put("sqlType", "nosql");
                RouteLog log = DataSupport.findLast(RouteLog.class);
                if(log != null){
                    params.put("rRouteId", log.getpRouteId());
                    params.put("rLat", this.amapLocation.getLatitude());
                    params.put("rLon", this.amapLocation.getLongitude());
                    params.put("rAlt", this.amapLocation.getAltitude());
                    params.put("rSpeed", this.amapLocation.getSpeed());
                    params.put("rAccelerate", 0);
                    params.put("addr", this.amapLocation.getAddress());
                    params.put("loctime", DateStr.yyyymmddHHmmssStr());
                    Map<String, Object> config = new HashMap<String, Object>();
                    config.put("interfaceName", "pjRouteLocation");
                    config.put("asyn", "false");
                    params.put("interfaceConfig", config);
                    String paramStr = JSON.toJSONString(params);
                    Log.w(TAG, "paramStr = " + paramStr);
                    PubData pubData = new WebserviceClient().loadData(paramStr);
                    Log.w(TAG, "pubData.getCode() = " + pubData.getCode());
                    if(pubData.getData() != null){
                        Log.w(TAG, "pubData.getData() = " + JSON.toJSONString(pubData.getData()));
                    }
                    this.amapLocation = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        Log.w(TAG,">>>>>>>>  onDestroy");
        super.onDestroy();
        stopSelfSevice();
    }

    @Override
    public void onLowMemory() {
        Log.w(TAG,">>>>>>>>  onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        Log.w(TAG,">>>>>>>>  onTrimMemory");
        super.onTrimMemory(level);
    }

    void stopSelfSevice() {
        Log.w(TAG,">>>>>>>>  stopSelfSevice");
//        AlarmManager am = (AlarmManager) this
//                .getSystemService(Context.ALARM_SERVICE);
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 3,
//                new Intent(CommUtil.START_INTENT), 0);
//        long triggerAtTime = SystemClock.elapsedRealtime() + 5 * 1000;
//        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime,
//                pendingIntent);
        //暂时屏蔽停止定位服务
//        if (amapLocalizer != null)
//            amapLocalizer.setLocationManager(false, "", 0);
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
            m_wakeLockObj.setReferenceCounted(false);// 处理RuntimeException:
            // WakeLock
            // under-locked
            // BaiDuLocReceiver
            m_wakeLockObj.release();
            m_wakeLockObj = null;
        }
    }
}
