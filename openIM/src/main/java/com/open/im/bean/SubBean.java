package com.open.im.bean;

/**
 * 好友申请bean
 * Created by Administrator on 2016/3/24.
 */
public class SubBean extends ProtocalObj{
    private String subFrom;
    private String subTo;
    private long subDate;
    private String msg;
    private String subState;   // 0 收到请求  1 同意对方请求 2 发出请求  3 对方同意请求
    private String avatarUrl;
    private String nick;
    private String owner;
    private String mark;

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public String getSubState() {
        return subState;
    }

    public void setSubState(String subState) {
        this.subState = subState;
    }

    public String getFrom() {
        return subFrom;
    }

    public void setFrom(String from) {
        this.subFrom = from;
    }

    public String getTo() {
        return subTo;
    }

    public void setTo(String to) {
        this.subTo = to;
    }

    public long getDate() {
        return subDate;
    }

    public void setDate(long date) {
        this.subDate = date;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    @Override
    public String toString() {
        return "SubBean{" +
                "subFrom='" + subFrom + '\'' +
                ", subTo='" + subTo + '\'' +
                ", subDate=" + subDate +
                ", msg='" + msg + '\'' +
                ", subState='" + subState + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", nick='" + nick + '\'' +
                ", owner='" + owner + '\'' +
                ", mark='" + mark + '\'' +
                '}';
    }
}
