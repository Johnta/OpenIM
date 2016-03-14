package com.open.im.bean;
/**
 * 发图片时的bean
 * @author Administrator
 *
 */
public class ImageBean extends ProtocalObj{
	private String type;
	private String uri;
	private String thumbnail;
	public String getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	private long size;
	private String resolution;
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
	public String getResolution() {
		return resolution;
	}
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}
	@Override
	public String toString() {
		return "ImageBean [type=" + type + ", uri=" + uri + ", thumbnail=" + thumbnail + ", size=" + size + ", resolution=" + resolution + "]";
	}
	
	
}
