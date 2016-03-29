package com.jxtii.wildebeest.bean;

import com.jxtii.wildebeest.util.AccelerationEnum;

/**
 * Created by huangyc on 2016/3/18.
 */
public class AccJudgeBean {

    AccelerationEnum accState;
    long beginTime;
    long duration;

    public AccelerationEnum getAccState() {
        return accState;
    }

    public void setAccState(AccelerationEnum accState) {
        this.accState = accState;
    }

    public long getBeginTime() {
        return beginTime;
    }

    public void setBeginTime(long beginTime) {
        this.beginTime = beginTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}
