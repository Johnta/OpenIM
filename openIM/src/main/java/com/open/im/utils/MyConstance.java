package com.open.im.utils;

import android.net.Uri;

/**
 * 应用中的常量
 * 
 * @author Administrator
 * 
 */
public class MyConstance {

	/**
	 * 软件更新json地址
	 */
	public static final String UPDATE_URL = "http://openim.daimaqiao.net:8080/openstore/api/upload.php";
	
	public static final String HOME_URL = "http://openim.daimaqiao.net:8080/openstore/api/getfile.php?fileid=";

	/**
	 * 存储用户聊天信息的数据库
	 */
	public static final String DB_NAME = "db_imChat.db";

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
