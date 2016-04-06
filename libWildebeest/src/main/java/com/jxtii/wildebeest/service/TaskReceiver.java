package com.jxtii.wildebeest.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jxtii.wildebeest.model.CompreRecord;
import com.jxtii.wildebeest.util.CommUtil;
import com.jxtii.wildebeest.util.LogEnum;
import com.jxtii.wildebeest.util.WriteLog;

import org.litepal.crud.DataSupport;

/**
 * Created by huangyc on 2016/3/3.
 */
public class TaskReceiver extends BroadcastReceiver {

    String TAG = TaskReceiver.class.getSimpleName();
    Context ctx = null;

    public void onReceive(Context context, Intent intent) {

        ctx = context;

        if (CommUtil.START_INTENT.equals(intent.getAction())) {
            logAndWrite("receive START_INTENT", LogEnum.INFO, false);
            Boolean flag = CommUtil.isServiceRunning(context, CommUtil.TASK_SERVICE);
            if (flag) {
                logAndWrite("TASK_SERVICE is alive", LogEnum.WARN, true);
            } else {
                logAndWrite("TASK_SERVICE is dead", LogEnum.WARN, true);
                startTaskService();
            }
            Boolean flagSc = CommUtil.isServiceRunning(context, CommUtil.CORE_SERVICE);
            if (flagSc) {
                logAndWrite("CORE_SERVICE is alive", LogEnum.WARN, true);
            } else {
                logAndWrite("CORE_SERVICE is dead", LogEnum.WARN, true);
                startCoreService();
            }
        } else if (CommUtil.STOP_INTENT.equals(intent.getAction())) {
            logAndWrite("receive STOP_INTENT", LogEnum.INFO, true);
            stopTaskService();
        } else if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            logAndWrite("BOOT_COMPLETED", LogEnum.INFO, true);
            AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
            long triggerAtTime = System.currentTimeMillis() + 2 * 60 * 1000;
            long interval = 15 * 60 * 1000;
            Intent intentBoot = new Intent();
            intentBoot.setAction(CommUtil.START_INTENT);
            intentBoot.setPackage(ctx.getPackageName());
            PendingIntent pt = PendingIntent.getBroadcast(ctx, 0, intentBoot, 0);
            am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, interval, pt);
        } else if("android.intent.action.USER_PRESENT".equals(intent.getAction())){
            logAndWrite("USER_PRESENT", LogEnum.INFO, true);
            CompreRecord lastCr = DataSupport.findLast(CompreRecord.class);
            if(lastCr != null){
                int co = lastCr.getUsePhone();
                logAndWrite("USER_PRESENT = " + co, LogEnum.INFO, false);
                lastCr.setUsePhone(co+1);
                lastCr.update(lastCr.getId());
            }
        }
    }

    void startTaskService() {
        logAndWrite("startTaskService " + ctx.getPackageName(), LogEnum.INFO, false);
        Intent intent = new Intent();
        intent.setAction(CommUtil.TASK_SERVICE_ACTION);
        intent.setPackage(ctx.getPackageName());//TODO 放到so中限制第三方用户使用
        //Implicit intents with startService are not safe
//        intent.setClass(ctx, TaskService.class);
        intent.putExtra("interval", CommUtil.LOC_FREQ);
        ctx.startService(intent);
    }

    void startCoreService() {
        logAndWrite("startCoreService " + ctx.getPackageName(), LogEnum.INFO, false);
        Intent intent2 = new Intent();
        intent2.setAction(CommUtil.CORE_SERVICE_ACTION);
        intent2.setPackage(ctx.getPackageName());//TODO 放到so中限制第三方用户使用
        ctx.startService(intent2);
    }

    void stopTaskService() {
        logAndWrite("stopTaskService " + ctx.getPackageName(), LogEnum.INFO, false);
        Intent intent = new Intent();
        intent.setAction(CommUtil.TASK_SERVICE_ACTION);
        intent.setPackage(ctx.getPackageName());//TODO 放到so中限制第三方用户使用
        //Implicit intents with startService are not safe
//        intent.setClass(ctx, TaskService.class);
        ctx.stopService(intent);

        Intent intent2 = new Intent();
        intent2.setAction(CommUtil.CORE_SERVICE_ACTION);
        intent2.setPackage(ctx.getPackageName());//TODO 放到so中限制第三方用户使用
        ctx.stopService(intent2);
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
