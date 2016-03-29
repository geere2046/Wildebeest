package com.jxtii.wildebeest.model;

import org.litepal.crud.DataSupport;

/**
 * 扣分记录
 *
 * Created by huangyc on 2016/3/11.
 */
public class PointRecord extends DataSupport{

    private int id;

    private int eventType;//扣分类型，1超速2急加速3急减速

    private float record;//原扣分因子

    private int point;//被扣分数

    private String createTime;//记录时间

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public float getRecord() {
        return record;
    }

    public void setRecord(float record) {
        this.record = record;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
