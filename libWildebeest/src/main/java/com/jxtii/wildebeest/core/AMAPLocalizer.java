package com.jxtii.wildebeest.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.jxtii.wildebeest.bean.GpsInfoBus;
import com.jxtii.wildebeest.bean.PointRecordBus;
import com.jxtii.wildebeest.bean.PubData;
import com.jxtii.wildebeest.bean.RouteFinishBus;
import com.jxtii.wildebeest.model.CompreRecord;
import com.jxtii.wildebeest.model.NoGpsInfo;
import com.jxtii.wildebeest.model.PointRecord;
import com.jxtii.wildebeest.model.PositionRecord;
import com.jxtii.wildebeest.model.RouteLog;
import com.jxtii.wildebeest.util.CalPointUtil;
import com.jxtii.wildebeest.util.CommUtil;
import com.jxtii.wildebeest.util.DateStr;
import com.jxtii.wildebeest.util.DistanceUtil;
import com.jxtii.wildebeest.util.LogEnum;
import com.jxtii.wildebeest.util.WriteLog;
import com.jxtii.wildebeest.webservice.WebserviceClient;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by huangyc on 2016/3/4.
 */
public class AMAPLocalizer implements AMapLocationListener {

    String TAG = AMAPLocalizer.class.getSimpleName();
    Context ctx;
    AMapLocationClient aMapLocationClient = null;
    volatile static AMAPLocalizer instance = null;
    public String locinfo = null;
    SQLiteDatabase db = null;
    Boolean isStart = false;
    Boolean flag = true;

    private AMAPLocalizer(Context ctx) {
        this.ctx = ctx;
        if (this.aMapLocationClient == null)
            this.aMapLocationClient = new AMapLocationClient(ctx);
        this.aMapLocationClient.setLocationListener(this);
        db = Connector.getDatabase();
    }

    public static AMAPLocalizer getInstance(Context ctx) {
        if (instance == null) {
            synchronized (AMAPLocalizer.class) {
                if (instance == null) {
                    instance = new AMAPLocalizer(ctx);
                }
            }
        }
        return instance;
    }

    public static void destroyInstance(Context ctx) {
        if (instance != null) {
            synchronized (AMAPLocalizer.class) {
                instance = null;
            }
        }
    }

    /**
     * 开启或关闭定位
     *
     * @param swit
     *            开启或销毁定位，单次定位无需销毁
     * @param locMode
     *            定位模式
     * @param minTime
     *            定位间隔
     */
    public void setLocationManager(Boolean swit, String locMode, long minTime) {
        logAndWrite("swit is " + swit + " " + (this.aMapLocationClient != null), LogEnum.INFO, false);
        if (swit) {
            if(this.aMapLocationClient == null){
                this.aMapLocationClient = new AMapLocationClient(ctx);
                this.aMapLocationClient.setLocationListener(this);
            }
            setLocationOption(locMode, minTime);
            if(!this.aMapLocationClient.isStarted()){// aMapLocationClient.isStarted()有bug，无论是否启动成功都返回false
                logAndWrite("AMapLocationClient start", LogEnum.INFO, false);
                this.aMapLocationClient.startLocation();
            }else{
                logAndWrite("AMapLocationClient haved start", LogEnum.INFO, false);
            }
        } else {
            if (this.aMapLocationClient != null) {
                this.aMapLocationClient.unRegisterLocationListener(this);
                this.aMapLocationClient.stopLocation();
                this.aMapLocationClient.onDestroy();
            }
            this.aMapLocationClient = null;
            instance = null;
        }
    }

    void setLocationOption(String locMode, long minTime) {
        AMapLocationClientOption locationOption = new AMapLocationClientOption();
        if (TextUtils.isEmpty(locMode)) {
            locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
            logAndWrite("Device_Sensors", LogEnum.INFO, false);
        } else {
            if ("low".equalsIgnoreCase(locMode)) {
                locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);
                logAndWrite("Battery_Saving", LogEnum.INFO, false);
            } else if ("gps".equalsIgnoreCase(locMode)) {
                locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Device_Sensors);
                logAndWrite("Device_Sensors", LogEnum.INFO, false);
            } else {
                //设置该选项将延长定位返回时长30s，仅在高精度模式单次定位下有效
                locationOption.setGpsFirst(true);
                locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                logAndWrite("Hight_Accuracy", LogEnum.INFO, false);
            }
        }
        if (minTime < 900) {
            locationOption.setOnceLocation(true);
        } else {
            locationOption.setOnceLocation(false);
            locationOption.setInterval(minTime);
        }
        locationOption.setMockEnable(false);//default false
        locationOption.setWifiActiveScan(true);//default true
        locationOption.setHttpTimeOut(15000);
        locationOption.setKillProcess(true);//default false
        locationOption.setNeedAddress(true);//default true
        locationOption.setOnceLocation(false);//default false
        aMapLocationClient.setLocationOption(locationOption);
    }

    public void onLocationChanged(AMapLocation amapLocation) {

        EventBus.getDefault().post(amapLocation);

        if (amapLocation != null && amapLocation.getErrorCode() == 0) {
            logAndWrite(amapLocation.getProvider() + " " + isStart, LogEnum.INFO, false);
            String extra = "";
            if ("gps".equals(amapLocation.getProvider())) {
                extra = "定位结果来自GPS信号";
                if (amapLocation.getSatellites() != 0)
                    extra += (",连接" + amapLocation.getSatellites() + "颗卫星");
                if (amapLocation.hasAccuracy())
                    extra += (",精度：" + amapLocation.getAccuracy() + "米");
                if (amapLocation.hasSpeed())
                    extra += (",速度:"
                            + CommUtil.floatToStr(
                            amapLocation.getSpeed() * 18 / 5, 1) + "公里/时");
                if (amapLocation.hasAltitude())
                    extra += (",海拔:"
                            + CommUtil.floatToStr(
                            (float) amapLocation.getAltitude(), 1) + "米");
                if (amapLocation.hasBearing()) {
                    extra += (",方向:北偏东" + amapLocation.getBearing() + "度");
                }
            } else if ("lbs".equals(amapLocation.getProvider())) {
                if (amapLocation.hasAccuracy()) {
                    Float acc = amapLocation.getAccuracy();
                    if (acc <= 100.0) {
                        extra = "定位结果来自WIFI信号";
                    } else {
                        extra = "定位结果来自基站信号";
                    }
                    extra += (",精度：" + acc + "米");
                } else {
                    extra = "定位结果来自网络定位信号";
                }
            }
            extra += ",定位通道1";
            extra += amapLocation.getLocationType();
            extra += amapLocation.getLocationDetail();
            Double geoLat = amapLocation.getLatitude();
            Double geoLng = amapLocation.getLongitude();
            locinfo = geoLat + ";" + geoLng + ";定位器不做地址解析;"
                    + amapLocation.getProvider() + ";" + extra;
//            logAndWrite("locinfo = " + locinfo, LogEnum.INFO, false);

            /*//TODO 模拟速度超过10km/h时启动
            if(flag&& !isStart){
                uploadInitInfo();
                isStart = true;
            }else{
                flag = false;
            }*/

            RouteLog log = DataSupport.findLast(RouteLog.class);
            if(log == null){
                isStart = false;
            }else{
                isStart = true;
            }

            float curSpeed = 0;
            if (amapLocation.hasSpeed()) {
                curSpeed = (float) (amapLocation.getSpeed() * 18.0 / 5);
                logAndWrite("isStart=" + isStart + ";curSpeed=" + curSpeed + ";beginSpeed=" + CommUtil.BEGIN_SPEED + ";" + (curSpeed > CommUtil.BEGIN_SPEED), LogEnum.INFO, true);
                if (curSpeed > CommUtil.BEGIN_SPEED && !isStart) {
                    uploadInitInfo();
                } else if (curSpeed != 0.0 && CommUtil.END_SPEED <= curSpeed) {
                    DataSupport.deleteAll(NoGpsInfo.class);
                } else if (curSpeed < CommUtil.END_SPEED) {
                    validNoGpsInfo();
                } else if (curSpeed == 0.0) {
                    validNoGpsInfo();
                } else {
                    logAndWrite("other situation", LogEnum.INFO, true);
                }
            } else {
                logAndWrite("isStart=" + isStart + ";noSpeed", LogEnum.INFO, true);
                validNoGpsInfo();
            }
            if (isStart) {
                if (amapLocation.hasBearing()) {
                    GpsInfoBus gpsInfoBus = new GpsInfoBus();
                    gpsInfoBus.setxDrift(Math.sin(amapLocation.getBearing()));
                    gpsInfoBus.setyDrift(Math.cos(amapLocation.getBearing()));
                    gpsInfoBus.setzDrift(0);
                    gpsInfoBus.setCreateTime(DateStr.yyyymmddHHmmssStr());
                    EventBus.getDefault().post(gpsInfoBus);
                } /*else {
                    //TODO 模拟方向数据
                    amapLocation.setBearing(0);
                    extra += (",方向:北偏东" + amapLocation.getBearing() + "度");
                    GpsInfoBus gpsInfoBus = new GpsInfoBus();
                    gpsInfoBus.setxDrift(Math.sin(amapLocation.getBearing()));
                    gpsInfoBus.setyDrift(Math.cos(amapLocation.getBearing()));
                    gpsInfoBus.setzDrift(0);
                    gpsInfoBus.setCreateTime(DateStr.yyyymmddHHmmssStr());
                    EventBus.getDefault().post(gpsInfoBus);
                }*/

                PositionRecord pr = new PositionRecord();
                pr.setLat(geoLat);
                pr.setLng(geoLng);
                pr.setDateStr(DateStr.yyyymmddHHmmssStr());
                pr.setExtra(amapLocation.toStr());
                pr.setSpeed(curSpeed);
                pr.save();

                int crCount = DataSupport.count(CompreRecord.class);
                logAndWrite("CompreRecord count = " + crCount, LogEnum.DEBUG, false);
                if (crCount == 0) {
                    CompreRecord cr = new CompreRecord();
                    cr.setBeginTime(DateStr.yyyymmddHHmmssStr());
                    cr.setCurrentTime(DateStr.yyyymmddHHmmssStr());
                    cr.setMaxSpeed(curSpeed);
                    cr.setTravelMeter(0);
                    cr.setSaveLat(geoLat);
                    cr.setSaveLng(geoLng);
                    cr.save();
                } else {
                    CompreRecord cr = new CompreRecord();
                    cr.setCurrentTime(DateStr.yyyymmddHHmmssStr());
                    CompreRecord lastCr = DataSupport.findLast(CompreRecord.class);
                    if (lastCr != null) {
                        float lastSpeed = lastCr.getMaxSpeed();
                        if (curSpeed > lastSpeed) {
                            cr.setMaxSpeed(curSpeed);
                        }
                        float lastDis = lastCr.getTravelMeter();
                        float curDistance = (float) DistanceUtil.distance(geoLng, geoLat, lastCr.getSaveLng(), lastCr.getSaveLat());
                        cr.setTravelMeter(lastDis + curDistance);
                        cr.setSaveLat(geoLat);
                        cr.setSaveLng(geoLng);
                        cr.update(lastCr.getId());
                    }
                }
                int pointSpeed = CalPointUtil.calSpeeding(curSpeed);
                if (pointSpeed > 0) {
                    PointRecord pointRecord = new PointRecord();
                    pointRecord.setCreateTime(DateStr.yyyymmddHHmmssStr());
                    pointRecord.setEventType(1);
                    pointRecord.setRecord(curSpeed);
                    pointRecord.setPoint(pointSpeed);
                    pointRecord.save();

                    PointRecordBus bus = new PointRecordBus();
                    bus.setPoint(pointRecord.getPoint());
                    bus.setEventType(pointRecord.getEventType());
                    bus.setRecord(pointRecord.getRecord());
                    EventBus.getDefault().post(bus);
                }
            }
        } else if (amapLocation != null && amapLocation.getErrorCode() != 0) {
            logAndWrite("amapLocation.getErrorCode() = "
                    + amapLocation.getErrorCode()
                    + " amapLocation.getErrorInfo() = "
                    + amapLocation.getErrorInfo(), LogEnum.WARN, true);
            // 华为8817e实测 错误12：缺少定位权限，请给app授予定位权限。判断无效，监听无返回，没有错误抛出
            locinfo = amapLocation.getErrorCode() + "_"
                    + amapLocation.getErrorInfo();
        } else {
            logAndWrite("获取定位信息失败", LogEnum.WARN, true);
            locinfo = "获取定位信息失败";
        }
    }

    void deleteAll(){
        DataSupport.deleteAll(RouteLog.class);
        DataSupport.deleteAll(PositionRecord.class);
        DataSupport.deleteAll(CompreRecord.class);
        DataSupport.deleteAll(PointRecord.class);
        DataSupport.deleteAll(NoGpsInfo.class);
    }

    void uploadInitInfo() {
        new Thread() {
            public void run() {
                SharedPreferences sp = ctx.getApplicationContext()
                        .getSharedPreferences("basic_data", Context.MODE_PRIVATE);
                String employeeId = sp.getString("id", "12345678");
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("sqlKey", "proc_init_route_info");
                params.put("sqlType", "proc");
                params.put("employeeId", employeeId);
                String paramStr = JSON.toJSONString(params);
                PubData pubData = new WebserviceClient().updateData(paramStr);
                logAndWrite("upload initInfo is " + pubData.getCode(), LogEnum.INFO, true);
                if (pubData != null && "00".equals(pubData.getCode())) {
                    isStart = true;
                    deleteAll();
                    String proId = (String) pubData.getData().get("pr_route_id");
                    RouteLog log = new RouteLog();
                    log.setpRouteId(proId);
                    log.save();
                }
            }
        }.start();
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
                    paramAfter.put("rUsePhone", cr.getUsePhone());
                    Map<String, Object> config = new HashMap<String, Object>();
                    config.put("interfaceName", "pjRouteFactorFinish");
                    config.put("asyn", "false");
                    paramAfter.put("interfaceConfig", config);
                    String paramStr = JSON.toJSONString(paramAfter);
                    logAndWrite("paramStr = " + paramStr, LogEnum.WARN, false);
                    PubData pubData = new WebserviceClient().loadData(paramStr);
                    logAndWrite("upload finishInfo is " + pubData.getCode(), LogEnum.INFO, true);
                    if (pubData != null && "00".equals(pubData.getCode())) {
                        if (pubData.getData() != null) {
                            logAndWrite("pubData.getData() = " + JSON.toJSONString(pubData.getData()), LogEnum.INFO, false);
                            if (pubData.getData().get("msgCode") != null){
                                logAndWrite(pubData.getData().get("msgCode") +"", LogEnum.WARN, true);
                            }
                            if (pubData.getData().get("msgCode") != null && "0".equals(pubData.getData().get("msgCode").toString())) {
                                deleteAll();
                                isStart = false;
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

    void validNoGpsInfo() {
        if (isStart) {
            logAndWrite("validNoGpsInfo", LogEnum.INFO, false);
            NoGpsInfo noGpsInfo = null;
            List<NoGpsInfo> listInfo = DataSupport.select("id", "noGpsTime").order("noGpsTime desc").limit(2).find(NoGpsInfo.class);
            if (listInfo != null && listInfo.size() > 0) {
                noGpsInfo = listInfo.get(0);
            }
            if (noGpsInfo == null) {
                logAndWrite("init noGpsInfo", LogEnum.INFO, true);
                NoGpsInfo noGps = new NoGpsInfo();
                noGps.setNoGpsTime(DateStr.yyyymmddHHmmssStr());
                noGps.save();
            } else {
                String last = noGpsInfo.getNoGpsTime();
                long max = CommUtil.timeSpanSecond(last, DateStr.yyyymmddHHmmssStr());
                if (max > CommUtil.NOGPS_TIME) {
                    logAndWrite("max="+max+";set="+CommUtil.NOGPS_TIME+";last="+last, LogEnum.INFO, true);
                    uploadFinishInfo();
                }
            }
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
