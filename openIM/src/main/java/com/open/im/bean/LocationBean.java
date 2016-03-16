package com.open.im.bean;
/**
 * 发送位置的bean
 * @author Administrator
 *
 */
public class LocationBean extends ProtocalObj{
	/*longitude表示经度，小数形式
	latitude表示纬度，小数形式
	accuracy表示定位精度，单位：米
	manner表示获取位置信息的方式，比如gps或者baidu等（小写字母）
	description表示位置信息对应的文字描述信息*/
	private String type;
	private String uri;
	private double longitude;
	private double latitude;
	private float accuracy;
	private String manner;
	private String description;
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
	@Override
	public String toString() {
		return "LocationBean [type=" + type + ", uri=" + uri + ", longitude=" + longitude + ", latitude=" + latitude + ", accuracy=" + accuracy + ", manner=" + manner + ", description=" + description
				+ "]";
	}
	
}
