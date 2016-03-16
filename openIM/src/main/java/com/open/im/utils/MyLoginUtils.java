package com.open.im.utils;

import java.io.IOException;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.open.im.activity.MainActivity;
import com.open.im.app.MyApp;
import com.open.im.service.IMService;

public class MyLoginUtils {

	/**
	 * 登录状态
	 */
	private static final int LOGIN_SUCCESS = 0;
	private static final int FAIL_PASSWORD_ERROR = 1;
	private static final int FAIL_UNKNOWN_USER = 2;
	private static final int INTERNET_ERROR = 3;
	private static final int LOGIN_FAIL = 4;

	/**
	 * 方法 登录
	 * 
	 * @param username
	 * @param password
	 */
	public static void login(final Activity act, final String username, final String password) {
		XMPPConnectionUtils.initXMPPConnection();
		final AbstractXMPPConnection connection = MyApp.connection;
		ThreadUtil.runOnBackThread(new Runnable() {
			@Override
			public void run() {
				try {
					if (!connection.isConnected()) {
						connection.connect();
					}
					connection.setPacketReplyTimeout(60 * 1000);
					connection.login(username, password);

					initVCard(act, username);

					// Roster roster = Roster.getInstanceFor(connection);
					// roster.setSubscriptionMode(SubscriptionMode.manual);

					Intent service = new Intent(act, IMService.class);
					act.startService(service);

					Intent intent = new Intent(act, MainActivity.class);
					act.startActivity(intent);

					handler.sendEmptyMessage(LOGIN_SUCCESS);

				} catch (SmackException e) {
					handler.sendEmptyMessage(INTERNET_ERROR);
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (XMPPException e) {
					if (e.getMessage().contains("not-authorized")) {
						// 未注册异常
						handler.sendEmptyMessage(FAIL_UNKNOWN_USER);
					} else if (e.getMessage().contains("bad-auth")) {
						// 密码错误异常
						handler.sendEmptyMessage(FAIL_PASSWORD_ERROR);
					} else {
						handler.sendEmptyMessage(LOGIN_FAIL);
					}
					e.printStackTrace();
				}
			}
		});

		// service = new Intent(act, IMService.class);
		// act.startService(service);
	}

	/**
	 * 初始化名片信息 如果没有设置昵称则设置昵称 如果设置了昵称 则获取昵称 也必须在用户登录后才能设置
	 * 
	 * @param username
	 * @throws NoResponseException
	 * @throws XMPPErrorException
	 * @throws NotConnectedException
	 */
	private static void initVCard(Activity act, final String username) throws NoResponseException, XMPPErrorException, NotConnectedException {
		VCardManager vCardManager = VCardManager.getInstanceFor(MyApp.connection);
		SharedPreferences sp = act.getPreferences(0);
		VCard vCard = vCardManager.loadVCard();
		String nickName = vCard.getNickName();
		if (nickName == null) {
			nickName = sp.getString("nickname", MyApp.username);
		}
		MyApp.nickName = nickName;
		vCard.setNickName(nickName);
		vCardManager.saveVCard(vCard);
		sp.edit().putString("nickname", null).commit();
		MyApp.username = username;
	}

	private static Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LOGIN_SUCCESS:
				// MyUtils.showToast(act, "登录成功");
				break;
			case INTERNET_ERROR:
				// MyUtils.showToast(act, "网络错误，请检查您的网络");
				break;
			case FAIL_UNKNOWN_USER:
				// MyUtils.showToast(act, "用户未注册");
				break;
			case FAIL_PASSWORD_ERROR:
				// MyUtils.showToast(act, "密码错误");
				break;
			case LOGIN_FAIL:
				// MyUtils.showToast(act, "登录失败");
				break;
			}
		};
	};
}
