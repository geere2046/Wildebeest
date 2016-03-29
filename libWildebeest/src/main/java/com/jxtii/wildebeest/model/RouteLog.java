package com.jxtii.wildebeest.model;

import org.litepal.crud.DataSupport;

/**
 * Created by huangyc on 2016/3/22.
 */
public class RouteLog extends DataSupport {

    private int id;
    private String pRouteId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getpRouteId() {
        return pRouteId;
    }

    public void setpRouteId(String pRouteId) {
        this.pRouteId = pRouteId;
    }
}
