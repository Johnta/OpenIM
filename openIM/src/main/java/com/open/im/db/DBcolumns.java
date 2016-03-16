package com.open.im.db;

/**
 * 数据库 相关的常量
 * @author Administrator
 *
 */
public class DBcolumns {
	
	//聊天信息存储的表
	/**
	 * 表名
	 */
	public static final String TABLE_MSG= "table_msg";
	/**
	 * 消息ID
	 */
	public static final String MSG_ID = "_id";
	/**
	 * 标记这条消息属于谁  (谁在本机收到的消息)
	 */
	public static final String MSG_OWNER = "msg_owner";
	/**
	 * 消息发送者
	 */
	public static final String MSG_FROM = "msg_from";
	/**
	 * 消息接收者
	 */
	public static final String MSG_TO = "msg_to";
	/**
	 * 消息类型
	 */
	public static final String MSG_TYPE = "msg_type";
	/**
	 * 消息时间
	 */
	public static final String MSG_DATE= "msg_date";
	/**
	 * 消息内容
	 */
	public static final String MSG_BODY = "msg_body";
	/**
	 * 如果上传的是图片 压缩图存到body中 大图路径存此
	 */
	public static final String MSG_IMG = "msg_img";
	/**
	 * 收到 还是 发出 消息
	 */
	public static final String MSG_ISCOMING = "msg_iscoming";
	/**
	 * 是否已读
	 */
	public static final String MSG_ISREADED = "msg_isreaded";
	
	public static final String MSG_MARK = "msg_mark";
//	public static final String MSG_BAK1= "msg_bak1";
//	public static final String MSG_BAK2 = "msg_bak2";
//	public static final String MSG_BAK3 = "msg_bak3";
//	public static final String MSG_BAK4= "msg_bak4";
//	public static final String MSG_BAK5= "msg_bak5";
//	public static final String MSG_BAK6= "msg_bak6";
	/**
	 * 聊天室消息表
	 */
	public static final String TABLE_MULTI_MSG = "table_multi_msg";
	/**
	 * 空间消息表
	 */
	public static final String TABLE_ZONE_MSG = "table_zone_msg";
	
	/**
	 * 多人聊天信息标记id
	 */
	public static final String MSG_STANZAID = "msg_stanzaid";
	
	
	
	/**
	 *  聊天会话表
	 */
//	public static final String TABLE_SESSION = "table_session";
//	public static final String SESSION_id = "session_id";
//	public static final String SESSION_FROM = "session_from";
//	public static final String SESSION_TYPE = "session_type";
//	public static final String SESSION_TIME = "session_time";
//	public static final String SESSION_CONTENT = "session_content";
//	public static final String SESSION_TO = "session_to";// 登录人id
//	public static final String SESSION_ISDISPOSE = "session_isdispose";
	
	/**
	 *  好友消息通知表
	 */
//	public static final String TABLE_SYS_NOTICE = "table_sys_notice";
//	public static final String SYS_NOTICE_ID = "sys_notice_id";
//	public static final String SYS_NOTICE_TYPE = "sys_notice_type";
//	public static final String SYS_NOTICE_FROM= "sys_notice_from";
//	public static final String SYS_NOTICE_FROM_HEAD = "sys_notice_from_head";
//	public static final String SYS_NOTICE_CONTENT = "sys_notice_content";
//	public static final String SYS_NOTICE_ISDISPOSE = "sys_notice_isdispose";
	
}
