package com.jxtii.wildebeest.bean;

import java.util.Map;

public class PubData {

    private Page page;
    private String code;
    private Map<String, Object> data;

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }


}
