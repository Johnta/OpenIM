package com.open.im.bean;

import java.io.Serializable;

import com.google.gson.Gson;
import com.open.im.utils.MyLog;

public class ProtocalObj  implements Serializable{

//	public String toXML() {
//		XStream x = new XStream();
//		// <zz.itcast.imz9.bean.QQMessage> --- <QQMessage>
//		x.alias(this.getClass().getSimpleName(), this.getClass());
//		// ②　fromXML toXML 方法
//		return x.toXML(this);
//	}
//
//	public Object fromXML(String xml) {
//		XStream x = new XStream();
//		// <zz.itcast.imz9.bean.QQMessage> --- <QQMessage>
//		x.alias(this.getClass().getSimpleName(), this.getClass());
//		// ②　fromXML toXML 方法
//
//		return x.fromXML(xml);
//	}
	public String toJson(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	public ProtocalObj fromJson(String json){
		Gson gson = new Gson();
		return gson.fromJson(json, this.getClass());
	}
}
