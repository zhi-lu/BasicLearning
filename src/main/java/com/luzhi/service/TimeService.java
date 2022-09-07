package com.luzhi.service;

/**
 * @author zhilu
 */
public class TimeService {
    private long time;

    private static final TimeService TIME_SERVICE = new TimeService();

    public static TimeService getInstance() {
        TIME_SERVICE.setTime(System.currentTimeMillis());
        return TIME_SERVICE;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
