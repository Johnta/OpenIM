package com.open.im.app;



import java.util.Map;

import org.jivesoftware.smack.AbstractXMPPConnection;

import com.open.im.receiver.MyMultiChatMessageLinstener;

public class MyApp {
	/**
	 * 在登录注册界面创建的连接对象 登录界面赋值
	 */
	public static AbstractXMPPConnection connection;
	/**
	 * 登录后 存储用户昵称
	 */
	public static String nickName;
	/**
	 * 登录后存储用户名
	 */
	public static String username;
	/**
	 * 服务开启时，创建的map，里面存储已经添加过监听的聊天室的所有的监听
	 */
	public static Map<String, MyMultiChatMessageLinstener> linstenerMap;
}
