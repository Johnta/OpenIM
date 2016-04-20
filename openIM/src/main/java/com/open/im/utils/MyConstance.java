package com.open.im.utils;

import android.net.Uri;

/**
 * 应用中的常量
 *
 * @author Administrator
 */
public class MyConstance {

    /**
     * 长文本上传url
     */
    public static final String UPLOAD_TEXT = "http://openim.daimaqiao.net:8080/openstore/api/puttext.php";
    /**
     * 图片上传url
     */
    public static final String UPLOAD_IMAGE = "http://openim.daimaqiao.net:8080/openstore/api/putimage.php";
    /**
     * 语音上传url
     */
    public static final String UPLOAD_VOICE = "http://openim.daimaqiao.net:8080/openstore/api/putvoice.php";
    /**
     * 位置上传url
     */
    public static final String UPLOAD_LOCATION = "http://openim.daimaqiao.net:8080/openstore/api/putlocation.php";
    /**
     * 头像上传url
     */
    public static final String UPLOAD_AVATAR = "http://openim.daimaqiao.net:8080/openstore/api/putavastar.php";

    /**
     * sp名
     */
    public static final String SP_NAME = "config";
    /**
     * 通知栏用到的 不知道有啥用
     */
    public static final int NOTIFY_ID = 8888;

    /**
     * 服务器地址
     */
    public static final String SERVICE_HOST = "openim.daimaqiao.net";
    /**
     * 当前版本客户端下载地址
     */
    public static final String CLIENT_URL = "http://openim.daimaqiao.net:8080/apk/OpenIM-1.0.0.apk";
    /**
     * vCard变更通知RUI
     */
    public static final Uri URI_VCARD = Uri.parse("content://com.openim.vcard");
    /**
     * msg变更通知RUI
     */
    public static final Uri URI_MSG = Uri.parse("content://com.openim.msg");
}
