package com.open.im.bean;

/**
 * 封装会话消息的bean
 * 
 * @author Administrator
 * 
 */
public class MessageBean extends ProtocalObj {

//	private int msgId;// id
	private String fromUser;// 发送者
	private String nick;  // 昵称
	private String toUser;// 接收者
	private int type;// 信息类型
	private String msgBody;// 信息内容
	private Long msgDateLong;// 时间 存的是毫秒值 long型
	private String isRead;// 是否已读
	private String msgMark; // 标记这是跟谁的聊天
	private String msgStanzaId; // 消息标记id
	private String msgOwner; // 标记这条消息是谁的
	private String msgImg; // 缩影图
	private int unreadMsgCount; // 未读消息数量
	private String msgReceipt; // 消息的发送状态(0 收到消息 1 发送中 2 已发送 3 已送达 4 发送失败)
	private String avatarUrl;  // 消息对应人的头像的url

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

	public String getMsgReceipt() {
		return msgReceipt;
	}

	public void setMsgReceipt(String msgReceipt) {
		this.msgReceipt = msgReceipt;
	}

	public int getUnreadMsgCount() {
		return unreadMsgCount;
	}

	public void setUnreadMsgCount(int unreadMsgCount) {
		this.unreadMsgCount = unreadMsgCount;
	}

	public String getMsgImg() {
		return msgImg;
	}

	public void setMsgImg(String msgImg) {
		this.msgImg = msgImg;
	}

	public String getMsgOwner() {
		return msgOwner;
	}

	public void setMsgOwner(String msgOwner) {
		this.msgOwner = msgOwner;
	}

	public MessageBean() {
	}

	public String getMsgStanzaId() {
		return msgStanzaId;
	}

	public void setMsgStanzaId(String msgStanzaId) {
		this.msgStanzaId = msgStanzaId;
	}

	public String getMsgMark() {
		return msgMark;
	}

	public void setMsgMark(String msgMark) {
		this.msgMark = msgMark;
	}

//	public int getMsgId() {
//		return msgId;
//	}
//
//	public void setMsgId(int msgId) {
//		this.msgId = msgId;
//	}

	public String getFromUser() {
		return fromUser;
	}

	public void setFromUser(String fromUser) {
		this.fromUser = fromUser;
	}

	public String getToUser() {
		return toUser;
	}

	public void setToUser(String toUser) {
		this.toUser = toUser;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getMsgBody() {
		return msgBody;
	}

	public void setMsgBody(String msgBody) {
		this.msgBody = msgBody;
	}

	public Long getMsgDateLong() {
		return msgDateLong;
	}

	public void setMsgDateLong(Long msgDateLong) {
		this.msgDateLong = msgDateLong;
	}

	public String getIsRead() {
		return isRead;
	}

	public void setIsRead(String isRead) {
		this.isRead = isRead;
	}

	@Override
	public String toString() {
		return "MessageBean{" +
//				"msgId=" + msgId +
				", fromUser='" + fromUser + '\'' +
				", toUser='" + toUser + '\'' +
				", type=" + type +
				", msgBody='" + msgBody + '\'' +
				", msgDateLong=" + msgDateLong +
				", isRead='" + isRead + '\'' +
				", msgMark='" + msgMark + '\'' +
				", msgStanzaId='" + msgStanzaId + '\'' +
				", msgOwner='" + msgOwner + '\'' +
				", msgImg='" + msgImg + '\'' +
				", unreadMsgCount=" + unreadMsgCount +
				", msgReceipt='" + msgReceipt + '\'' +
				'}';
	}
}
