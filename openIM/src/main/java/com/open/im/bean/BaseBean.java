package com.open.im.bean;

/**
 * 包含所有字段的bean
 * @author Administrator
 * 
 */
public class BaseBean extends ProtocalObj {
	/**
	 * 公有的
	 */
	private String type;
	private String uri;
	private long size;
	/**
	 * 图片独有
	 */
	private String resolution;
	private String thumbnail;
	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	/**
	 * 位置独有
	 */
	private double longitude;
	private double latitude;
	private float accuracy;
	private String manner;
	private String description;
	/**
	 * 语音独有 播放时长 单位秒
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

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}

	public String getManner() {
		return manner;
	}

	public void setManner(String manner) {
		this.manner = manner;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	@Override
	public String toString() {
		return "BaseBean [type=" + type + ", uri=" + uri + ", size=" + size + ", resolution=" + resolution + ", thumbnail=" + thumbnail + ", longitude=" + longitude + ", latitude=" + latitude
				+ ", accuracy=" + accuracy + ", manner=" + manner + ", description=" + description + ", length=" + length + "]";
	}
}
