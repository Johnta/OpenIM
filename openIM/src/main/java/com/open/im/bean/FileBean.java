package com.open.im.bean;

/**
 * 文件上传后返回的json对应的bean
 */
public class FileBean extends ProtocalObj{
	private String category;
	private String error;
	private String result;
	private String thumbnail;
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getError() {
		return error;
	}
	public void setError(String error) {
		this.error = error;
	}
	public String getResult() {
		return result;
	}
	public void setResult(String result) {
		this.result = result;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	@Override
	public String toString() {
		return "FileBean [category=" + category + ", error=" + error
				+ ", result=" + result + ", result2=" + thumbnail + "]";
	}
	
}
