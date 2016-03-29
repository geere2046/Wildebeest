package com.jxtii.wildebeest.bean;

import org.json.JSONObject;

/**
 * Created by huangyc on 2016/3/22.
 */
public class PointRecordBus {

    private int eventType;//扣分类型，1超速2急加速3急减速
    private float record;//原扣分因子
    private int point;//被扣分数

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

    public String toStr() {
        return this.toStr(1);
    }

    public String toStr(int var1) {
        JSONObject var2 = null;

        try {
            var2 = new JSONObject();
            switch (var1) {
                case 1:
                    var2.put("eventType", this.eventType);
                    var2.put("record", this.record);
                    var2.put("point", this.point);

            }
        } catch (Exception var5) {
            var2 = null;
        }

        return var2 == null ? null : var2.toString();
    }
}
