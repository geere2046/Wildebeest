package com.jxtii.wildebeest.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.jxtii.wildebeest.util.CommUtil;
import com.jxtii.wildebeest.util.DateStr;

/**
 * Created by huangyc on 2016/3/3.
 */
public class TaskReceiver extends BroadcastReceiver {

    String TAG = TaskReceiver.class.getSimpleName();
    Context ctx = null;

    public void onReceive(Context context, Intent intent) {

        ctx = context;

        if (CommUtil.START_INTENT.equals(intent.getAction())) {
            Log.i(TAG, DateStr.HHmmssStr()+" _ receive START_INTENT");
            Boolean flag = CommUtil.isServiceRunning(context, CommUtil.TASK_SERVICE);
            if (flag) {
                Log.i(TAG, DateStr.HHmmssStr() + " _ TASK_SERVICE is alive");
            } else {
                Log.w(TAG, DateStr.HHmmssStr() + " _ TASK_SERVICE is dead");
                startTaskService();
            }
        } else if (CommUtil.STOP_INTENT.equals(intent.getAction())) {
            Log.i(TAG, DateStr.HHmmssStr()+" _ receive STOP_INTENT");
            stopTaskService();
        }
    }

    void startTaskService() {
        Log.i(TAG, DateStr.HHmmssStr()+" _ startTaskService " + ctx.getPackageName());
        Intent intent = new Intent();
        intent.setAction(CommUtil.TASK_SERVICE_ACTION);
        intent.setPackage(ctx.getPackageName());
        //Implicit intents with startService are not safe
//        intent.setClass(ctx, TaskService.class);
        intent.putExtra("interval", CommUtil.LOC_FREQ);
        ctx.startService(intent);

        Intent intent2 = new Intent();
        intent2.setAction(CommUtil.CORE_SERVICE_ACTION);
        intent2.setPackage("com.yuzhi.fine");
        ctx.startService(intent2);
    }

    void stopTaskService() {
        Log.i(TAG, DateStr.HHmmssStr()+" _ stopTaskService " + ctx.getPackageName());
        Intent intent = new Intent();
        intent.setAction(CommUtil.TASK_SERVICE_ACTION);
        intent.setPackage(ctx.getPackageName());
        //Implicit intents with startService are not safe
//        intent.setClass(ctx, TaskService.class);
        ctx.stopService(intent);

        Intent intent2 = new Intent();
        intent2.setAction(CommUtil.CORE_SERVICE_ACTION);
        intent2.setPackage(ctx.getPackageName());
        ctx.stopService(intent2);
    }
}
