
package com.serotonin.m2m2.vo.bean;


public class ImageValueBean {
    private final String time;
    private final String uri;

    public ImageValueBean(String time, String uri) {
        this.time = time;
        this.uri = uri;
    }

    public String getTime() {
        return time;
    }

    public String getUri() {
        return uri;
    }
}
