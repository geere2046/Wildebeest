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
import com.jxtii.wildebeest.bean.PubDataList;
import com.jxtii.wildebeest.bean.RouteFinishBus;
import com.jxtii.wildebeest.core.AMAPLocalizer;
import com.jxtii.wildebeest.model.CompreRecord;
import com.jxtii.wildebeest.model.ConfigPara;
import com.jxtii.wildebeest.model.NoGpsInfo;
import com.jxtii.wildebeest.model.PointRecord;
import com.jxtii.wildebeest.model.PositionRecord;
import com.jxtii.wildebeest.model.RouteLog;
import com.jxtii.wildebeest.util.CommUtil;
import com.jxtii.wildebeest.util.DateStr;
import com.jxtii.wildebeest.util.DistanceUtil;
import com.jxtii.wildebeest.util.LogEnum;
import com.jxtii.wildebeest.util.WriteLog;
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
    TimerTask mTimerTaskSc;
    int interval = 900;
    PowerManager.WakeLock m_wakeLockObj;
    AMapLocation amapLocation;
    AMapLocation amapLocationClone;
    AMapLocation amapLocationWatcher;

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        logAndWrite("onCreateService", LogEnum.WARN, true);
        ctx = TaskService.this;
        amapLocalizer = AMAPLocalizer.getInstance(ctx);
        EventBus.getDefault().register(this);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent == null) {
            logAndWrite("onStartCommand intent is null", LogEnum.WARN, true);
            stopSelfSevice();
        } else {
            interval = intent.getIntExtra("interval", 30 * 1000);
            logAndWrite("onStartCommand interval = " + interval, LogEnum.WARN, true);
            if (amapLocalizer != null){
                logAndWrite("amapLocalizer != null", LogEnum.WARN, false);
                amapLocalizer.setLocationManager(true, "gps", interval);
            }else{
                logAndWrite("amapLocalizer == null", LogEnum.WARN, false);
                amapLocalizer = AMAPLocalizer.getInstance(ctx);
                amapLocalizer.setLocationManager(true, "gps", interval);
            }
            stopTimer();
            if (mTimer == null)
                mTimer = new Timer();
            if (mTimerTask == null) {
                mTimerTask = new TimerTask() {
                    public void run() {
                        acquireWakeLock(ctx);
//                        logAndWrite("mTimerTask", LogEnum.INFO, false);
                        uploadLocInfo();
                        isNeedFinish();
                        releaseWakeLock();
                    }
                };
            }
            mTimer.scheduleAtFixedRate(mTimerTask, 1 * 1000,
                    interval);
            if(mTimerTaskSc == null){
                mTimerTaskSc = new TimerTask() {
                    public void run() {
                        acquireWakeLock(ctx);
                        logAndWrite("mTimerTaskSc", LogEnum.INFO, false);
                        try {
                            if (amapLocationWatcher == null) {
                                logAndWrite("amapLocationWatcher == null", LogEnum.WARN, false);
                                stopSelfSevice();
                            } else {
                                logAndWrite("amapLocationWatcher != null", LogEnum.WARN, false);
                                amapLocationWatcher = null;
                            }
                            syncConfig();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        releaseWakeLock();
                    }
                };
            }
            mTimer.scheduleAtFixedRate(mTimerTaskSc, 1 * 60 * 1000,
                    CommUtil.WATCHER_FREQ);
        }
        return START_STICKY;
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void oreceiveAMapLocation(AMapLocation amapLocation){
        logAndWrite("AMapLocation is " + amapLocation.toStr(), LogEnum.INFO, false);
        this.amapLocation = amapLocation;
        this.amapLocationClone = amapLocation;
        this.amapLocationWatcher = amapLocation;
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void receivePointRecordBus(PointRecordBus bus) {
        logAndWrite("PointRecordBus is " + bus.toStr(), LogEnum.INFO, false);
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
                    logAndWrite("paramStr = " + paramStr, LogEnum.WARN, false);
                    PubData pubData = new WebserviceClient().loadData(paramStr);
                    logAndWrite("upload pjRouteFactorSpeeding is " + pubData.getCode(), LogEnum.INFO, true);
                    if(pubData.getData() != null){
                        logAndWrite("pubData.getData() = " + JSON.toJSONString(pubData.getData()), LogEnum.WARN, false);
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
                    logAndWrite("paramStr = " + paramStr, LogEnum.WARN, false);
                    PubData pubData = new WebserviceClient().loadData(paramStr);
                    logAndWrite("upload pjRouteFactorInterface is" + pubData.getCode(), LogEnum.INFO, true);
                    if(pubData.getData() != null){
                        logAndWrite("pubData.getData() = " + JSON.toJSONString(pubData.getData()), LogEnum.WARN, false);
                    }
                }
                uploadRouteLocation(this.amapLocationClone, log);
                this.amapLocationClone = null;
            } else {
                params.put("rLat", 0.0);
                params.put("rLon", 0.0);
                params.put("rAlt", 0.0);
                logAndWrite("this.amapLocationClone is null", LogEnum.INFO, true);
            }
        }
    }

    /**
     * 同步配置
     */
    void syncConfig() {
        ConfigPara para = DataSupport.findLast(ConfigPara.class);
        if (para == null) {
            logAndWrite("syncConfig init", LogEnum.INFO, true);
            downloadConfig();
        } else {
            String createTime = para.getCreateTime();
            long span = CommUtil.timeSpanSecond(createTime, DateStr.yyyymmddHHmmssStr());
            if (span > 60 * 60) {//每小时同步一次配置init
                logAndWrite("syncConfig reflash", LogEnum.INFO, true);
                DataSupport.deleteAll(ConfigPara.class);
                downloadConfig();
            } else {
                logAndWrite("syncConfig span is " + span, LogEnum.INFO, false);
            }
        }
    }

    /**
     * 下载配置
     */
    void downloadConfig() {
        Map<String, Object> attrMap = new HashMap<String, Object>();
        attrMap.put("sqlKey", "sql_attr_view");
        attrMap.put("attrCode", "config_para");
        attrMap.put("sqlType", "sql");
        String paramStr = JSON.toJSONString(attrMap);
        PubDataList pubDataList = new WebserviceClient().loadDataList(paramStr);//TODO 混淆后pubDataList为null
        logAndWrite(pubDataList != null ?"downloadConfig.getCode() = " + pubDataList.getCode():"downloadConfig pubDataList is null", LogEnum.INFO, false);
        if (pubDataList != null && "00".equals(pubDataList.getCode())) {
            if (pubDataList.getData() != null && pubDataList.getData().size() > 0) {
                logAndWrite("downloadConfig : " + JSON.toJSONString(pubDataList.getData()), LogEnum.INFO, false);
                ConfigPara paraIn = new ConfigPara();
                for (Map<String, Object> mapInner : pubDataList.getData()) {
                    if ("GPS_BEARING".equals(mapInner.get("ATTR_VALUE").toString())) {
                        paraIn.setGpsBearing(Long.valueOf(mapInner.get("ATTR_VALUE_NAME").toString()));
                    } else if ("MIN_ACC".equals(mapInner.get("ATTR_VALUE").toString())) {
                        paraIn.setMinAcc(Float.valueOf(mapInner.get("ATTR_VALUE_NAME").toString()));
                    } else if ("ACC_VALID_THRESHOLD".equals(mapInner.get("ATTR_VALUE").toString())) {
                        paraIn.setAccValidThreshold(Long.valueOf(mapInner.get("ATTR_VALUE_NAME").toString()));
                    } else if ("BASIC_SCORE_ACC".equals(mapInner.get("ATTR_VALUE").toString())) {
                        paraIn.setBasicScoreAcc(Integer.valueOf(mapInner.get("ATTR_VALUE_NAME").toString()));
                    } else if ("BASIC_SCORE_DEC".equals(mapInner.get("ATTR_VALUE").toString())) {
                        paraIn.setBasicScoreDec(Integer.valueOf(mapInner.get("ATTR_VALUE_NAME").toString()));
                    } else if ("MAX_SPEED".equals(mapInner.get("ATTR_VALUE").toString())) {
                        paraIn.setMaxSpeed(Float.valueOf(mapInner.get("ATTR_VALUE_NAME").toString()));
                    } else if ("BEGIN_SPEED".equals(mapInner.get("ATTR_VALUE").toString())) {
                        paraIn.setBeginSpeed(Float.valueOf(mapInner.get("ATTR_VALUE_NAME").toString()));
                    } else if ("NOGPS_TIME".equals(mapInner.get("ATTR_VALUE").toString())) {
                        paraIn.setNoGpsTime(Long.valueOf(mapInner.get("ATTR_VALUE_NAME").toString()));
                    } else if ("LOC_FREQ".equals(mapInner.get("ATTR_VALUE").toString())) {
                        paraIn.setLocFreq(Integer.valueOf(mapInner.get("ATTR_VALUE_NAME").toString()));
                    } else if ("G_AVE".equals(mapInner.get("ATTR_VALUE").toString())) {
                        paraIn.setgAve(Double.valueOf(mapInner.get("ATTR_VALUE_NAME").toString()));
                    } else if ("WATCHER_FREQ".equals(mapInner.get("ATTR_VALUE").toString())) {
                        paraIn.setWatcherFreq(Integer.valueOf(mapInner.get("ATTR_VALUE_NAME").toString()));
                    } else if ("END_SPEED".equals(mapInner.get("ATTR_VALUE").toString())) {
                        paraIn.setEndSpeed(Integer.valueOf(mapInner.get("ATTR_VALUE_NAME").toString()));
                    }
                }
                paraIn.setCreateTime(DateStr.yyyymmddHHmmssStr());
                paraIn.save();
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
                logAndWrite("locinfo is not null", LogEnum.DEBUG, false);
                locinfo = "";
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
        logAndWrite("paramStr = " + paramStr, LogEnum.DEBUG, false);
        PubData pubData = new WebserviceClient().loadData(paramStr);
        logAndWrite("upload pjRouteLocation is " + pubData.getCode(), LogEnum.INFO, true);
        if (pubData != null && "00".equals(pubData.getCode())) {
            if (pubData.getData() != null) {
                logAndWrite("pubData.getData() = " + JSON.toJSONString(pubData.getData()), LogEnum.INFO, false);
                if (pubData.getData().get("msgCode") != null && "2".equals(pubData.getData().get("msgCode").toString())) {
                    logAndWrite("调用结束路线逻辑", LogEnum.WARN, true);
                    correctRouteFinish(log);
                } else if (pubData.getData().get("msgCode") != null && "1".equals(pubData.getData().get("msgCode").toString())) {
                    logAndWrite("pubData.getData().msgContent = " + pubData.getData().get("msgContent").toString(), LogEnum.INFO, false);
                }
            }
        }
    }

    /**
     * 过滤脏数据，修正路线结束信息
     * @param log
     */
    void correctRouteFinish(RouteLog log) {
        logAndWrite("correctRouteFinish", LogEnum.INFO, true);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sqlKey", "sql_max_location_time");
        params.put("sqlType", "sql");
        params.put("rRouteId", log.getpRouteId());
        String paramStr = JSON.toJSONString(params);
        logAndWrite("paramStr = " + paramStr, LogEnum.WARN, false);
        PubData pubData = new WebserviceClient().loadData(paramStr);
        logAndWrite("pubData.getCode() = " + pubData.getCode(), LogEnum.WARN, false);
        if (pubData != null && "00".equals(pubData.getCode())) {
            if (pubData.getData() != null) {
                logAndWrite("pubData.getData() = " + JSON.toJSONString(pubData.getData()), LogEnum.INFO, false);
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
            logAndWrite("gps closed", LogEnum.INFO, true);
            uploadFinishInfo();
        }else{
            Boolean hadFinish = false;
            NoGpsInfo noGpsInfo = null;
            List<NoGpsInfo> listInfo = DataSupport.select("id", "noGpsTime").order("noGpsTime desc").limit(2).find(NoGpsInfo.class);
            if (listInfo != null && listInfo.size() > 0) {
                noGpsInfo = listInfo.get(0);
            }
            if (noGpsInfo != null) {
                String last = noGpsInfo.getNoGpsTime();
                long max = CommUtil.timeSpanSecond(last, DateStr.yyyymmddHHmmssStr());
                if (max > CommUtil.NOGPS_TIME) {
                    logAndWrite(max + ">" + CommUtil.NOGPS_TIME + "&&last=" + last, LogEnum.INFO, true);
                    hadFinish = true;
                    uploadFinishInfo();
                }
            }
            if (!hadFinish) {
                PositionRecord noPo = null;
                List<PositionRecord> listPo = DataSupport.select("id", "dateStr").order("dateStr desc").limit(2).find(PositionRecord.class);
                if (listPo != null && listPo.size() > 0) {
                    noPo = listPo.get(0);
                }
                if (noPo != null) {
                    String last = noPo.getDateStr();
                    long max = CommUtil.timeSpanSecond(last, DateStr.yyyymmddHHmmssStr());
                    if (max > CommUtil.NOGPS_TIME) {
                        logAndWrite(max + ">" + CommUtil.NOGPS_TIME + "&&last=" + last, LogEnum.INFO, true);
                        uploadFinishInfo();
                    }
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
                    logAndWrite("uploadFinishInfo", LogEnum.INFO, true);
                    long timeFin = CommUtil.timeSpanSecond(cr.getBeginTime(), cr.getCurrentTime());
                    float aveSp = cr.getTravelMeter() * 18 / (timeFin * 5);
                    Map<String, Object> paramAfter = new HashMap<String, Object>();
                    paramAfter.put("sqlKey", "nosql");
                    paramAfter.put("sqlType", "nosql");
                    paramAfter.put("rRouteId", log.getpRouteId());
                    paramAfter.put("rHighSpeed", cr.getMaxSpeed());
                    paramAfter.put("rAveSpeed", aveSp);
                    paramAfter.put("rTravelMeter", cr.getTravelMeter());
                    paramAfter.put("rUsePhone", cr.getUsePhone());
                    Map<String, Object> config = new HashMap<String, Object>();
                    config.put("interfaceName", "pjRouteFactorFinish");
                    config.put("asyn", "false");
                    paramAfter.put("interfaceConfig", config);
                    String paramStr = JSON.toJSONString(paramAfter);
                    logAndWrite("paramStr = " + paramStr, LogEnum.WARN, false);
                    PubData pubData = new WebserviceClient().loadData(paramStr);
                    logAndWrite("upload finishInfo is " + pubData.getCode(), LogEnum.WARN, true);
                    if (pubData != null && "00".equals(pubData.getCode())) {
                        if (pubData.getData() != null) {
                            logAndWrite("pubData.getData() = " + JSON.toJSONString(pubData.getData()), LogEnum.INFO, false);
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
        logAndWrite("onDestroy", LogEnum.INFO, false);
        super.onDestroy();
        stopSelfSevice();
    }

    public void onLowMemory() {
        logAndWrite("onLowMemory", LogEnum.INFO, false);
        super.onLowMemory();
    }

    public void onTrimMemory(int level) {
        logAndWrite("onTrimMemory", LogEnum.INFO, false);
        super.onTrimMemory(level);
    }

    void stopSelfSevice() {
        logAndWrite("stopSelfSevice", LogEnum.INFO, true);
        if (amapLocalizer != null) {
            amapLocalizer.setLocationManager(false, "", 0);
        }
        AlarmManager am = (AlarmManager) this
                .getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent();
        intent.setAction(CommUtil.START_INTENT);
        intent.setPackage(ctx.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1,
                intent, 0);
        long triggerAtTime = SystemClock.elapsedRealtime() + 30 * 1000;
        am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime,
                pendingIntent);
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
        if (mTimerTaskSc != null) {
            mTimerTaskSc.cancel();
            mTimerTaskSc = null;
        }
    }

    public void acquireWakeLock(Context cxt) {
        logAndWrite(">>>>>>点亮屏幕", LogEnum.VERBOSE, false);
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
        logAndWrite(">>>>>>取消点亮", LogEnum.VERBOSE, false);
        if (m_wakeLockObj != null && m_wakeLockObj.isHeld()) {
            m_wakeLockObj.setReferenceCounted(false);
            m_wakeLockObj.release();
            m_wakeLockObj = null;
        }
    }

    /**
     * 记录本地日志
     *
     * @param log
     */
    void writeLog(final String log) {
        new Thread() {
            public void run() {
                WriteLog.getInstance().write(TAG, log);
            }
        }.start();
    }

    /**
     * 打印和记录日志
     *
     * @param log
     * @param level
     * @param needWrite
     */
    void logAndWrite(String log,LogEnum level,Boolean needWrite) {
        switch (level) {
            case VERBOSE:
                Log.v(TAG,log);
                if(needWrite)
                    writeLog(log);
                break;
            case DEBUG:
                Log.d(TAG,log);
                if(needWrite)
                    writeLog(log);
                break;
            case INFO:
                Log.i(TAG,log);
                if(needWrite)
                    writeLog(log);
                break;
            case WARN:
                Log.w(TAG,log);
                if(needWrite)
                    writeLog(log);
                break;
        }
    }
}
