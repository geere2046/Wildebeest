package com.jxtii.wildebeest.model;

import org.litepal.annotation.Column;
import org.litepal.crud.DataSupport;

/**
 * GPS输出关键点信息
 *
 * Created by huangyc on 2016/3/7.
 */
public class PositionRecord extends DataSupport {

    private int id;

    @Column(nullable = false)
    private double lat;

    @Column(nullable = false)
    private double lng;

    private String dateStr;

    private float bearing;

    private float speed;

    private String extra;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }
}
