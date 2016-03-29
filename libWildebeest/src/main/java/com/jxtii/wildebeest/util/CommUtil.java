package com.jxtii.wildebeest.util;

import android.app.ActivityManager;
import android.content.Context;
import android.location.LocationManager;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by huangyc on 2016/3/3.
 */
public class CommUtil {

    public static final String START_INTENT = "com.jxtii.wildebeest.task_receiver";
    public static final String STOP_INTENT = "com.jxtii.wildebeest.stop_receiver";
    public static final String TASK_SERVICE = "com.jxtii.wildebeest.service.TaskService";
    public static final String TASK_SERVICE_ACTION = "com.jxtii.wildebeest.task_service";
    public static final String CORE_SERVICE_ACTION = "com.jxtii.wildebeest.core_service";
    //以下为枚举参数
    public static final String ACC_STATE = "0";
    public static final String DEC_STATE = "1";
    public static final String UNKOWN_STATE = "2";
    //以下为关键指标
    public static final long GPS_BEARING = 60;//gps方向有效时间，需参考连续定位设置频率
    public static final float MIN_ACC = 0.01f;//线性加速度过滤阀值
    public static final long ACC_VALID_THRESHOLD = 300;//加速度最小持续时间(ms)
    public static final int BASIC_SCORE_ACC = 5;//急加速的基础扣分
    public static final int BASIC_SCORE_DEC = 10;//急减速的基础扣分
    public static final float MAX_SPEED = 30;//超速阀值30km/h
    public static final float BEGIN_SPEED = 5.0f;//启动记录路线速度
    public static final long NOGPS_TIME = 180;
    public static final int LOC_FREQ = 10*1000;//上报定位数据频率
    public static final double G_AVE = 0.4;//加速度判断阀值
    //TODO 网络通讯参数 需放到so中
    public static final String NAME_SPACE = "http://ep.wqsm.gaf.com/";
    public static final String WS_URL = "http://182.106.128.43/PubService.ws";

    /**
     * 判断GPS是否打开
     *
     * @param c
     * @return
     */
    public static boolean isOpenGPS(Context c) {
        boolean flag = false;
        LocationManager locationManager = (LocationManager) c
                .getSystemService(Context.LOCATION_SERVICE);
        if (locationManager
                .isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            flag = true;
        }
        return flag;
    }

    /**
     * 判断服务是否在运行
     *
     * @param mContext
     * @param className
     * @return
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager.getRunningServices(100);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    /**
     * 从float转化为str
     * @param fl
     * @param su 保留小数点后精度位数，小于1时取整数
     * @return
     */
    public static String floatToStr(float fl, int su) {
        String str = su < 1 ? "0" : ".";
        for (int i = 0; i < su; i++) {
            str += "0";
        }
        DecimalFormat decimalFormat = new DecimalFormat(str);
        String p = decimalFormat.format(fl);
        return p;
    }

    /**
     * 计算时间差，返回秒
     * @param last yyyyMMddHHmmss
     * @param curr yyyyMMddHHmmss
     * @return
     */
    public static long timeSpanSecond(String last, String curr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            Date dateFirst = sdf.parse(last);
            Date dateCurr = sdf.parse(curr);
            long between = (dateCurr.getTime() - dateFirst.getTime()) / 1000;
            return between;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 计算时间差，返回HH:mm
     * @param last yyyyMMddHHmmss
     * @param curr yyyyMMddHHmmss
     * @return
     */
    public static String timeSpanHHmm(String last, String curr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            Date dateFirst = sdf.parse(last);
            Date dateCurr = sdf.parse(curr);
            long between = (dateCurr.getTime() - dateFirst.getTime()) / 1000;
            long day1 = between / (24 * 3600);
            long hour1 = between % (24 * 3600) / 3600;
            long minute1 = between % 3600 / 60;
            long second1 = between % 60 / 60;

            long hour = day1 * 24 + hour1;
            String pre = hour > 9 ? hour + "" : "0" + hour;
            String suf = minute1 > 9 ? minute1 + "" : "0" + minute1;
            return pre + ":" + suf;
        } catch (ParseException e) {
            e.printStackTrace();
            return "00:00";
        }
    }

}