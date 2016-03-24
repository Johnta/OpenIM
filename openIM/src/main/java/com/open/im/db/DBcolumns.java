package com.open.im.db;

/**
 * 数据库 相关的常量
 *
 * @author Administrator
 */
public class DBcolumns {

    //聊天信息存储的表
    /**
     * 单人聊天信息表名
     */
    public static final String TABLE_MSG = "table_msg";
    /**
     *好友申请表名
     */
    public static final String TABLE_SUB = "table_sub";
    /**
     * 是否同意好友申请
     */
    public static final String SUB_STATE = "sub_state";
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
    public static final String MSG_DATE = "msg_date";
    /**
     * 消息内容
     */
    public static final String MSG_BODY = "msg_body";
    /**
     * 如果上传的是图片 压缩图存到body中 大图路径存此
     */
    public static final String MSG_IMG = "msg_img";

    /**
     * 是否已读
     */
    public static final String MSG_ISREADED = "msg_isreaded";
    /**
     * 消息标记 标记与谁聊天
     */
    public static final String MSG_MARK = "msg_mark";

    /**
     * 聊天信息标记id
     */
    public static final String MSG_STANZAID = "msg_stanzaid";

    /**
     * 消息发送状态
     */
    public static final String MSG_RECEIPT = "msg_receipt";


}
