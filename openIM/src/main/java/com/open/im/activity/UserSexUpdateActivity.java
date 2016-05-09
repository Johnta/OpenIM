package com.open.im.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.utils.MyUtils;

public class UserSexUpdateActivity extends BaseActivity implements OnClickListener {
	private UserSexUpdateActivity act;
	private TextView tv_title;
	private TextView tv_cancel;
	private TextView tv_save;
	private Intent intent;
	private RadioGroup rg_sex;
	private String sex;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_usersex_update);

		initView();

		initData();

		register();
	}

	private void register() {
		tv_cancel.setOnClickListener(this);

		tv_save.setOnClickListener(this);
		
		rg_sex.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.rb_male:
					sex = "男";
					break;
				case R.id.rb_female:
					sex = "女";
					break;
				case R.id.rb_secret:
					sex = "保密";
					break;
				}
			}
		});
	}

	private void initData() {
		intent = getIntent();
		sex = intent.getStringExtra("sex");
		MyUtils.showToast(act, sex);
		if ("男".equals(sex)) {
			rg_sex.check(R.id.rb_male);
		} else if ("女".equals(sex)) {
			rg_sex.check(R.id.rb_female);
		} else if ("保密".equals(sex)) {
			rg_sex.check(R.id.rb_secret);
		} else {
			rg_sex.clearCheck();
		}
	}

	private void initView() {
		act = this;

		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_cancel = (TextView) findViewById(R.id.tv_cancel);
		tv_save = (TextView) findViewById(R.id.tv_save);
		rg_sex = (RadioGroup) findViewById(R.id.rg_sex);
		
		tv_title.setText("性别");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_cancel:
			finish();
			break;
		case R.id.tv_save:
			if (sex != null) {
				intent.setData(Uri.parse(sex));
				setResult(3, intent);
				finish();
			}
			break;
		}
	}
}
