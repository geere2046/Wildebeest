package com.jxtii.wildebeest.bean;

import org.json.JSONObject;

/**
 * Created by huangyc on 2016/3/16.
 */
public class GpsInfoBus {

    private double xDrift;
    private double yDrift;
    private double zDrift;
    private String createTime;

    public double getxDrift() {
        return xDrift;
    }

    public void setxDrift(double xDrift) {
        this.xDrift = xDrift;
    }

    public double getyDrift() {
        return yDrift;
    }

    public void setyDrift(double yDrift) {
        this.yDrift = yDrift;
    }

    public double getzDrift() {
        return zDrift;
    }

    public void setzDrift(double zDrift) {
        this.zDrift = zDrift;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
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
                    var2.put("xDrift", this.xDrift);
                    var2.put("yDrift", this.yDrift);
                    var2.put("zDrift", this.zDrift);
                    var2.put("createTime", this.createTime);

            }
        } catch (Exception var5) {
            var2 = null;
        }

        return var2 == null ? null : var2.toString();
    }
}
