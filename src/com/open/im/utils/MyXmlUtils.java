package com.open.im.utils;

import java.io.ByteArrayInputStream;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.util.Xml;

public class MyXmlUtils {

	
	public static String getzoneMsgs(Context context,String str){
		String msgBody = null;
		try {
			//1.拿到PullParser
			XmlPullParser parser = Xml.newPullParser();
			//2.设置输入流
			ByteArrayInputStream is = new ByteArrayInputStream(str.getBytes());
			parser.setInput(is, "utf-8");
			//3.拿到事件类型
			int eventType = parser.getEventType();
			//4.不停的做循环判断XmlPullParser.END_DOCUMENT
			while (eventType != XmlPullParser.END_DOCUMENT) {
				switch (eventType) {
				case XmlPullParser.START_TAG:

					if ("body".equals(parser.getName())) {
						msgBody = parser.nextText();
					}
					//对开始标签做一个判断
//					if ("weather".equals(parser.getName())) {
//						//当根标签的时候需要new出来一个集合
//						channels = new ArrayList<ZoneMessageBean>();
//					} else if ("channel".equals(parser.getName())) {
//						//当子标签的时候需要new出业务bean对象
//						channel = new ZoneMessageBean();
//						String id =  parser.getAttributeValue(0);
//						channel.id = id;	
//					}else if ("city".equals(parser.getName())) {
//						//不断的获取属性信息
//						channel.city =  parser.nextText();		
//					}else if ("temp".equals(parser.getName())) {
//						channel.temp = parser.nextText();
//					}else if ("wind".equals(parser.getName())) {
//						channel.wind = parser.nextText();
//					}else if ("pm250".equals(parser.getName())) {
//						channel.pm250 = parser.nextText();
//					}

					break;

				case XmlPullParser.END_TAG:

//					if("message".equals(parser.getName())){
//						if(channels!=null){
//							channels.add(channel);
//						}
//					}
					
					
					
					break;

				default:
					break;
				}
				//拿到下一个事件类型
				eventType = parser.next();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return msgBody;
	}
	
}
