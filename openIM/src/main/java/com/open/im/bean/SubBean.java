package com.open.im.bean;

/**
 * Created by Administrator on 2016/3/24.
 */
public class SubBean {
    private String from;
    private String to;
    private long date;
    private String msg;
    private String state;   // 0 未处理  1 同意 2 拒绝

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "SubBean{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", date=" + date +
                ", msg='" + msg + '\'' +
                ", state='" + state + '\'' +
                '}';
    }
}