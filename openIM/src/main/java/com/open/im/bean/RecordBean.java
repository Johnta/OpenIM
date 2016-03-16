package com.open.im.bean;
/**
 * 发送录音的bean
 * @author Administrator
 *
 */
public class RecordBean extends ProtocalObj{
	private String type;
	private String uri;
	/**
	 * 文件大小
	 */
	private long size;
	/**
	 * 播放时长 单位秒
	 */
	private int length;
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
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	@Override
	public String toString() {
		return "RecordBean [type=" + type + ", uri=" + uri + ", size=" + size + ", length=" + length + "]";
	}
	
	
}	
