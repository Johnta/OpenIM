package com.open.im.bean;

/**
 * @author Administrator
 * 长文本bean
 */
public class LongMsgBean extends ProtocalObj{
	private String type;
	private String uri;
	private long size;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	@Override
	public String toString() {
		return "LongMsgBean [type=" + type + ", uri=" + uri + ", size=" + size + "]";
	}
	
}
