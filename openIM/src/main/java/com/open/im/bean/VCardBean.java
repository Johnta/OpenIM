package com.open.im.bean;

import android.graphics.Bitmap;

/**
 * 用户卡片信息bean
 * Created by Administrator on 2016/3/21.
 */
public class VCardBean {

    private String jid;
    private String nickName;
    private String homeAddress;
    private String email;
    private String phone;
    private String sex;
    private String desc;
    private String bday;
    private Bitmap bitmap;
    private String avatarUrl;

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getBday() {
        return bday;
    }

    public void setBday(String bday) {
        this.bday = bday;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "VCardBean{" +
                "jid='" + jid + '\'' +
                ", nickName='" + nickName + '\'' +
                ", homeAddress='" + homeAddress + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", sex='" + sex + '\'' +
                ", desc='" + desc + '\'' +
                ", bday='" + bday + '\'' +
                ", bitmap=" + bitmap +
                ", avatarUrl='" + avatarUrl + '\'' +
                '}';
    }
}
