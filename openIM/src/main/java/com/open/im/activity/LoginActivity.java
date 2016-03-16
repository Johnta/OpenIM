package com.open.im.activity;

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
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.service.IMService;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyUtils;
import com.open.im.utils.ThreadUtil;
import com.open.im.utils.XMPPConnectionUtils;
import com.open.im.view.ClearEditText;

public class LoginActivity extends Activity implements OnClickListener {

	private ClearEditText et_username, et_pwd;
	private Button btn_login, btn_register;
	private LoginActivity act;
	private ProgressDialog pd;
	private int beforeLength;

	/**
	 * 登录状态
	 */
	private static final int LOGIN_SUCCESS = 0;
	private static final int FAIL_PASSWORD_ERROR = 1;
	private static final int FAIL_UNKNOWN_USER = 2;
	private static final int INTERNET_ERROR = 3;
	private static final int LOGIN_FAIL = 4;

	private SharedPreferences sp;
	private Intent service;
	private AbstractXMPPConnection connection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		act = this;
		sp = getSharedPreferences(MyConstance.SP_NAME, 0);
		boolean isServiceRunning = MyUtils.isServiceRunning(act, IMService.class.getName());
		if (isServiceRunning) {
			startActivity(new Intent(act, MainActivity.class));
			finish();
		} else {
			setContentView(R.layout.activity_login);

			initView();

			// 登录注册键点击事件
			btn_login.setOnClickListener(this);
			btn_register.setOnClickListener(this);
			
			et_username.addTextChangedListener(new TextWatcher() {
				
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {
					beforeLength = s.length();
				}
				
				@Override
				public void afterTextChanged(Editable s) {
					int afterLength = s.length();
					if (afterLength < beforeLength) {
						et_pwd.setText("");
					}
				}
			});
		}
		// 注册广播接收者 接收登录是否成功
		// initReceiver();
	}

	// /**
	// * 方法 注册一个广播接收者 接收服务发来的是否登录成功的信息
	// */
	// private void initReceiver() {
	// receiver = new BroadcastReceiver() {
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// if (MyConstance.ACTION_IS_LOGIN_SUCCESS.equals(intent.getAction())) {
	// if (pd != null && pd.isShowing()) {
	// pd.dismiss();
	// }
	// int loginState = intent.getIntExtra(MyConstance.LOGIN_STATE, 100);
	// switch (loginState) {
	// case LOGIN_SUCCESS:
	// MyToast.showToast(act, "登录成功");
	// Intent intent2 = new Intent(act, MainActivity.class);
	// act.startActivity(intent2);
	// finish();
	// break;
	// // 登录失败要把服务关了
	// case LOGIN_FAIL:
	// MyToast.showToast(act, "登录失败");
	// stopService(service);
	// break;
	// case FAIL_PASSWORD_ERROR:
	// MyToast.showToast(act, "密码错误");
	// stopService(service);
	// break;
	// case FAIL_UNKNOWN_USER:
	// MyToast.showToast(act, "用户未注册");
	// stopService(service);
	// break;
	// case INTERNET_ERROR:
	// MyToast.showToast(act, "网络错误，请检查您的网络");
	// stopService(service);
	// break;
	// }
	// }
	// }
	// };
	//
	// // 注册广播接收者
	// IntentFilter filter = new IntentFilter();
	// filter.addAction(MyConstance.ACTION_IS_LOGIN_SUCCESS);
	// registerReceiver(receiver, filter);
	// }

	/**
	 * 界面初始化
	 */
	private void initView() {

		et_username = (ClearEditText) findViewById(R.id.et_username);
		et_pwd = (ClearEditText) findViewById(R.id.et_pwd);
		btn_login = (Button) findViewById(R.id.btn_login);
		btn_register = (Button) findViewById(R.id.btn_register);

		String userName = sp.getString("username", "");
		String password = sp.getString("password", "");

		MyLog.showLog("et" + et_username);
		
		et_username.setText(userName);
		et_pwd.setText(password);

	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		/**
		 * 登录
		 */
		case R.id.btn_login:
			final String username = et_username.getText().toString().trim();
			final String password = et_pwd.getText().toString().trim();
			if (TextUtils.isEmpty(username)) {
				MyUtils.showToast(act, "用户名不能为空");
//				et_username.setError("用户名不能为空");
				return;
			} else if (TextUtils.isEmpty(password)) {
				MyUtils.showToast(act, "密码不能为空");
//				et_pwd.setError("密码不能为空");
				return;
			}

			login(username, password);

			break;
		/**
		 * 注册
		 */
		case R.id.btn_register:
			Intent intent = new Intent(act, RegisterActivity.class);
			act.startActivity(intent);
			break;
		}
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
		XMPPConnectionUtils.initXMPPConnection();
		connection = MyApp.connection;
		pd = new ProgressDialog(act);
		pd.setMessage("拼命加载中，请稍后...");
		pd.show();
		ThreadUtil.runOnBackThread(new Runnable() {

			@Override
			public void run() {
				try {
					if (!connection.isConnected()) {
						connection.connect();
					}
					connection.setPacketReplyTimeout(60 * 1000);
					connection.login(username, password);

					initVCard(username);

					// Roster roster = Roster.getInstanceFor(connection);
					// roster.setSubscriptionMode(SubscriptionMode.manual);

					service = new Intent(act, IMService.class);
					act.startService(service);

					Intent intent = new Intent(act, MainActivity.class);
					act.startActivity(intent);
					finish();

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
	private void initVCard(final String username) throws NoResponseException, XMPPErrorException, NotConnectedException {
		VCardManager vCardManager = VCardManager.getInstanceFor(connection);
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

	private void pdDismiss() {
		if (pd != null && pd.isShowing()) {
			pd.dismiss();
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			pdDismiss();
			switch (msg.what) {
			case LOGIN_SUCCESS:
				MyUtils.showToast(act, "登录成功");
				break;
			case INTERNET_ERROR:
				MyUtils.showToast(act, "网络错误，请检查您的网络");
				break;
			case FAIL_UNKNOWN_USER:
				MyUtils.showToast(act, "用户未注册");
				break;
			case FAIL_PASSWORD_ERROR:
				MyUtils.showToast(act, "密码错误");
				break;
			case LOGIN_FAIL:
				MyUtils.showToast(act, "登录失败");
				break;
			}
		};
	};
}
