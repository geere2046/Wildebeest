package com.jxtii.wildebeest.model;

import org.litepal.crud.DataSupport;

/**
 * Created by huangyc on 2016/3/25.
 */
public class NoGpsInfo extends DataSupport {

    private int id;
    private String noGpsTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNoGpsTime() {
        return noGpsTime;
    }

    public void setNoGpsTime(String noGpsTime) {
        this.noGpsTime = noGpsTime;
    }
}
