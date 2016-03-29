package com.jxtii.wildebeest.bean;

import java.util.List;
import java.util.Map;

public class PubDataList {

    private Page page;
    private String code;
    private List<Map<String, Object>> data;

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

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
}
