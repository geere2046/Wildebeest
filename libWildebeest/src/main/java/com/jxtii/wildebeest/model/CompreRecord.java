package com.jxtii.wildebeest.model;

import org.litepal.crud.DataSupport;

/**
 * 记录运行信息类
 *
 * Created by huangyc on 2016/3/8.
 */
public class CompreRecord extends DataSupport {

    private int id;

    private String beginTime;

    private String currentTime;

    private float maxSpeed;

    private float travelMeter;

    private double saveLat;

    private double saveLng;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(String beginTime) {
        this.beginTime = beginTime;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public float getTravelMeter() {
        return travelMeter;
    }

    public void setTravelMeter(float travelMeter) {
        this.travelMeter = travelMeter;
    }

    public double getSaveLat() {
        return saveLat;
    }

    public void setSaveLat(double saveLat) {
        this.saveLat = saveLat;
    }

    public double getSaveLng() {
        return saveLng;
    }

    public void setSaveLng(double saveLng) {
        this.saveLng = saveLng;
    }
}
