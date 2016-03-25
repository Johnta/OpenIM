package com.open.im.app;


import android.app.Application;

import com.open.im.log.CrashHandler;

import org.jivesoftware.smack.AbstractXMPPConnection;

public class MyApp extends Application{
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

	@Override
	public void onCreate() {
		super.onCreate();
		// 未捕获的异常都会发送到指定邮箱
		CrashHandler crashHandler = CrashHandler.getInstance() ;
		crashHandler.init(this, "1365260937@qq.com") ;
	}
}
