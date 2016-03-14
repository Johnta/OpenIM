package com.open.im.utils;

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
	public static final String UPDATE_URL = "http://im2.daimaqiao.net:8080/openstore/api/upload.php";
	
	public static final String HOMEURL = "http://im2.daimaqiao.net:8080/openstore/api/getfile.php?fileid=";
	
	/**
	 * 消息类型
	 */
	public static final String TYPE_AUDIO = "audio";
	public static final String TYPE_TEXT = "text";
	public static final String TYPE_LOCATION = "location";
	public static final String TYPE_IMAGE = "image";
	/**
	 * 聊天消息类型个数
	 */
	public static final int TYPE_MAX_COUNT = 4;

	/**
	 * 服务中登录时 发送的广播的 action
	 */
	public static final String ACTION_IS_LOGIN_SUCCESS = "com.exiu.im.login";

	/**
	 * 服务中发送给登录界面的登录状态
	 */
	public static final String LOGIN_STATE = "loginState";

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
	 * 文件上传地址
	 */
	public static final String UPLOAD_FILE_URL = "http://alydevapi.114995.com/api/FileUpload/UploadPic";
	// public static final String UPLOAD_FILE_URL =
	// "http://192.168.1.36:8080/server/UpLoadServlet";
	public static String KEY_IS_AUTO_UPDATE = "key_is_auto_update";

	// public static final String ACTION_MULTICHAT_REMOVE_LINSTENER =
	// "com.exiu.im.multichat.remove";
	//
	// public static final String ACTION_MULTICHAT_ADD_LINSTENER =
	// "com.exiu.im.multichat.add";
}
