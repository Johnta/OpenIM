package com.open.im.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.service.IMService;
import com.open.im.utils.MyConstance;

public class RegisterSuccessActivity extends Activity {

	private Button btn_login;
	private TextView tv_username;
	private String username;
	private String password;
	private RegisterSuccessActivity act;
	private Intent service;
	private SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register_result);

		// 初始化
		init();
		// 注册监听
		register();
	}

	/**
	 * 注册监听
	 */
	private void register() {
		btn_login.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 跳转到登录页面
				Intent intent = new Intent(act, LoginActivity.class);
				startActivity(intent);

				// VCardManager vCardManager =
				// VCardManager.getInstanceFor(connection);
				// // MyLog.showLog(connection.getUser());
				// VCard vCard = vCardManager.loadVCard(username +
				// "@im2.daimaqiao.net");
				//
				// vCard.setNickName(nickName);
				// MyApp.nickName = nickName;
				// vCardManager.saveVCard(vCard);

				// // 登录
				// connection.login(username, password);
				// login(username, password);
				// 用户登录成功
				// if (connection.isAuthenticated()) {
				// // 设置用户基本信息
				// setVCard(nickName, connection);
				// MyLog.showLog("设置昵称成功");
				// finish();
				// }
				// // 跳转到主页
				// Intent intent = new Intent(act, MainActivity.class);
				// act.startActivity(intent);
			}
		});
	}

	/**
	 * 方法 登录
	 * 
	 * @param username
	 * @param password
	 */
	private void login(final String username, final String password) {

		sp.edit().putString("username", username).commit();
		sp.edit().putString("password", password).commit();

		service = new Intent(act, IMService.class);
		act.startService(service);
	}

	// /**
	// * 设置用户基本信息
	// *
	// * @param nickName
	// * @param connection
	// * @throws NoResponseException
	// * @throws XMPPErrorException
	// * @throws NotConnectedException
	// */
	// private void setVCard(String nickName, AbstractXMPPConnection connection)
	// throws NoResponseException, XMPPErrorException,
	// NotConnectedException {
	// // 设置用户信息类 编辑用户信息 在此处设置了一个昵称 不编辑用户信息是没办法查找到用户的
	// VCardManager vCardManager = VCardManager.getInstanceFor(connection);
	// VCard vCard = vCardManager.loadVCard(connection.getUser());
	//
	// vCard.setNickName(nickName);
	// MyApp.nickName = nickName;
	// vCardManager.saveVCard(vCard);
	// }

	/**
	 * 初始化
	 */
	private void init() {
		act = this;
		btn_login = (Button) findViewById(R.id.btn_login);
		tv_username = (TextView) findViewById(R.id.tv_username);

		sp = getSharedPreferences(MyConstance.SP_NAME, 0);
		username = sp.getString("username", null);
		password = sp.getString("password", null);

		tv_username.setText(username);

	}
}
