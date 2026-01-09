package com.jdocker.protocol;

import java.util.Map;

public class Request {

    private String action;
    private Map<String, String> data;

    public Request() {}

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
