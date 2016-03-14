package com.open.im.bean;

/**
 * 封装订阅的发布的文字消息 时间long 内容  from  Jid  lizh@im2.daimaqiao.net
 * @author Administrator
 *
 */
public class ZoneMessageBean {
	private String msgBody;
	private String msgFromJid;
	private long msgLongDate;
	private String msgMark;
	
	public String getMsgMark() {
		return msgMark;
	}
	public void setMsgMark(String msgMark) {
		this.msgMark = msgMark;
	}
	public String getMsgBody() {
		return msgBody;
	}
	public void setMsgBody(String msgBody) {
		this.msgBody = msgBody;
	}
	public String getMsgFromJid() {
		return msgFromJid;
	}
	public void setMsgFromJid(String msgFromJid) {
		this.msgFromJid = msgFromJid;
	}
	public long getMsgLongDate() {
		return msgLongDate;
	}
	public void setMsgLongDate(long msgLongDate) {
		this.msgLongDate = msgLongDate;
	}
	@Override
	public String toString() {
		return "ZoneMessageBean [msgBody=" + msgBody + ", msgFromJid=" + msgFromJid + ", msgLongDate=" + msgLongDate + ", msgMark=" + msgMark + "]";
	}
	
	
}
