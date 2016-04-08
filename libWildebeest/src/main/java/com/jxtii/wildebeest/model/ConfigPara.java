package com.jxtii.wildebeest.model;

import org.litepal.crud.DataSupport;

/**
 * Created by huangyc on 2016/4/5.
 */
public class ConfigPara extends DataSupport {

    private int id;
    private long gpsBearing;
    private float minAcc;
    private long accValidThreshold;
    private int basicScoreAcc;
    private int basicScoreDec;
    private float maxSpeed;
    private float beginSpeed;
    private long noGpsTime;
    private int locFreq;
    private double gAve;
    private int watcherFreq;
    private String createTime;
    private float endSpeed;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getGpsBearing() {
        return gpsBearing;
    }

    public void setGpsBearing(long gpsBearing) {
        this.gpsBearing = gpsBearing;
    }

    public float getMinAcc() {
        return minAcc;
    }

    public void setMinAcc(float minAcc) {
        this.minAcc = minAcc;
    }

    public long getAccValidThreshold() {
        return accValidThreshold;
    }

    public void setAccValidThreshold(long accValidThreshold) {
        this.accValidThreshold = accValidThreshold;
    }

    public int getBasicScoreAcc() {
        return basicScoreAcc;
    }

    public void setBasicScoreAcc(int basicScoreAcc) {
        this.basicScoreAcc = basicScoreAcc;
    }

    public int getBasicScoreDec() {
        return basicScoreDec;
    }

    public void setBasicScoreDec(int basicScoreDec) {
        this.basicScoreDec = basicScoreDec;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public float getBeginSpeed() {
        return beginSpeed;
    }

    public void setBeginSpeed(float beginSpeed) {
        this.beginSpeed = beginSpeed;
    }

    public long getNoGpsTime() {
        return noGpsTime;
    }

    public void setNoGpsTime(long noGpsTime) {
        this.noGpsTime = noGpsTime;
    }

    public int getLocFreq() {
        return locFreq;
    }

    public void setLocFreq(int locFreq) {
        this.locFreq = locFreq;
    }

    public double getgAve() {
        return gAve;
    }

    public void setgAve(double gAve) {
        this.gAve = gAve;
    }

    public int getWatcherFreq() {
        return watcherFreq;
    }

    public void setWatcherFreq(int watcherFreq) {
        this.watcherFreq = watcherFreq;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public float getEndSpeed() {
        return endSpeed;
    }

    public void setEndSpeed(float endSpeed) {
        this.endSpeed = endSpeed;
    }
}
