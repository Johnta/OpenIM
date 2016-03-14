package com.open.im.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.utils.MyUtils;
import com.open.im.view.ClearEditText;

public class UserInfoUpdateActivity extends Activity implements OnClickListener {
	private TextView tv_title;
	private TextView tv_cancel;
	private TextView tv_save;
	private ClearEditText et_info;
	private UserInfoUpdateActivity act;
	private Intent intent;
	private int type;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userinfo_update);
		// 初始化控件
		initView();
		// 初始化数据
		initData();
		// 点击事件监听
		register();
	}

	private void register() {
		tv_cancel.setOnClickListener(this);

		tv_save.setOnClickListener(this);
	}

	private void initData() {
		intent = getIntent();
		type = intent.getIntExtra("type", -1);
		switch (type) {
		case 1:
			tv_title.setText("昵称");
			et_info.setText(intent.getStringExtra("info"));
			break;
		case 4:
			tv_title.setText("地址");
			et_info.setText(intent.getStringExtra("info"));
			break;
		case 5:
			tv_title.setText("邮箱");
			et_info.setText(intent.getStringExtra("info"));
			break;
		case 6:
			tv_title.setText("电话");
			et_info.setText(intent.getStringExtra("info"));
			break;
		case 7:
			tv_title.setText("签名");
			et_info.setText(intent.getStringExtra("info"));
			break;

		default:
			break;
		}

	}

	private void initView() {
		
		act = this;
		
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_cancel = (TextView) findViewById(R.id.tv_cancel);
		tv_save = (TextView) findViewById(R.id.tv_save);
		et_info = (ClearEditText) findViewById(R.id.et_info);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.tv_cancel) { // 取消按钮点击事件
			finish(); 
		} else if (id == R.id.tv_save) { // 保存按钮点击事件
			String info = et_info.getText().toString().trim();
			if (TextUtils.isEmpty(info)) {
				MyUtils.showToast(act, "请填写正确的信息内容");
			} else {
				intent.setData(Uri.parse(info));
				setResult(type, intent);
				finish();
			}
		}
	}
}
