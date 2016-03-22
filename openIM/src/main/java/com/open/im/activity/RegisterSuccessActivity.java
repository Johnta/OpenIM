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

	/**
	 * 初始化
	 */
	private void init() {
		act = this;
		btn_login = (Button) findViewById(R.id.btn_login);
		tv_username = (TextView) findViewById(R.id.tv_username);

		sp = getSharedPreferences(MyConstance.SP_NAME, 0);
		username = sp.getString("username", null);

		tv_username.setText(username);

	}
}
