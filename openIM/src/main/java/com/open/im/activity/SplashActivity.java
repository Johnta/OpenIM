package com.open.im.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;

import com.open.im.R;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyLog;

public class SplashActivity extends Activity {

	private SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
		sp = getSharedPreferences(MyConstance.SP_NAME, 0);
		login();
	}

	private void login() {

		new Thread() {
			public void run() {
				// 手机从开机到现在的毫秒值
				long startTime = SystemClock.uptimeMillis();

				String username = sp.getString("username", "");
				String password = sp.getString("password", "");
				MyLog.showLog("username::" + username);
				MyLog.showLog("password::" + password);

				if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
					MyLog.showLog("闪屏界面登录");
				}

				long endTime = SystemClock.uptimeMillis();
				long passTime = endTime - startTime; // 联网的用时

				if (passTime < 1000) { // 联网很快，2秒内，就完成了
					try {
						Thread.sleep(1000 - passTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				handler.sendEmptyMessage(GO_LOGIN);
			};
		}.start();

	}

	private final int GO_LOGIN = 101;

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case GO_LOGIN:
				// 跳转至主页面
				Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
				startActivity(intent);
				finish();// 结束当前activity
				break;
			}
		};
	};

}
