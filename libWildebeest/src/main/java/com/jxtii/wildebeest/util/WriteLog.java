package com.jxtii.wildebeest.util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * 日志记录工具（单个日志文件阀值暂定500KB）
 * <p/>
 * Created by huangyc on 2016/3/31.
 */
public class WriteLog {

    static WriteLog instance = null;
    File file = null;
    FileWriter writer;
    BufferedReader br;
    FileReader reader;
    final String LOG_DIR = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + File.separator + "wildebeestLog";
    String timeStr = null;
    StringBuffer sb = new StringBuffer();
    static String TAG = "WriteLog";
    int maxSize = 200;// 文件最大阀值（KB）

    public static WriteLog getInstance() {
        if (instance == null)
            instance = new WriteLog();
        return instance;
    }

    public synchronized void write(String filename, String log) {
        init(filename);
        writeLog(log);
        close();
    }

    void init(String fileName) {
        try {
            file = new File(LOG_DIR);
            if (!file.exists()) {
                file.mkdir();
            }
            String logPath = LOG_DIR + File.separator + fileName + ".log";
            file = new File(logPath);
            if (!file.exists()) {
                file.createNewFile();
            } else {
                FileInputStream fi = new FileInputStream(file);
                int kb = fi.available() / 1024;
                Log.d(TAG, fileName + " size = " + kb + "KB");
                if (kb >= maxSize) {
                    Boolean del = file.delete();
                    if (del) {
                        Log.i(TAG, "delete " + fileName + " success");
                        file = new File(logPath);
                        file.createNewFile();
                    } else {
                        Log.i(TAG, "delete " + fileName + " failure");
                    }
                }
                fi.close();
            }
            reader = new FileReader(file);
            br = new BufferedReader(reader);
            String s;
            while ((s = br.readLine()) != null) {
                sb.append(s + "\n");
            }
            writer = new FileWriter(file, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void close() {
        try {
            reader.close();
            br.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void writeLog(String log) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                "{MM-dd_HH:mm:ss.SSS}", Locale.CHINA);
        timeStr = simpleDateFormat.format(System.currentTimeMillis());
        try {
            String last = sb.toString();
            sb = new StringBuffer();
            Log.d(TAG, last);
            writer.write(last + timeStr + log + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
