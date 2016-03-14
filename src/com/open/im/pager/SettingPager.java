package com.open.im.pager;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.activity.LoginActivity;
import com.open.im.activity.MainActivity;
import com.open.im.activity.UserInfoActivity;
import com.open.im.app.MyApp;
import com.open.im.service.IMService;
import com.open.im.utils.MyUtils;

public class SettingPager extends BasePager implements OnClickListener {

	private RelativeLayout rl_logout, rl_info;
	private RelativeLayout rl_zone;
	private TextView tv_username;

	public SettingPager(Context ctx) {
		super(ctx);
	}

	@Override
	public View initView() {
		View view = View.inflate(ctx, R.layout.pager_im_setting, null);
		rl_logout = (RelativeLayout) view.findViewById(R.id.rl_logout);
		rl_info = (RelativeLayout) view.findViewById(R.id.rl_info);
		// rl_zone = (RelativeLayout) view.findViewById(R.id.rl_zone);
		tv_username = (TextView) view.findViewById(R.id.tv_username);

		tv_username.setText(MyApp.username);

		rl_logout.setOnClickListener(this);
		rl_info.setOnClickListener(this);
		// rl_zone.setOnClickListener(this);

		return view;
	}

	@Override
	public void initData() {
	}

	@Override
	public void onClick(View v) {
		MainActivity act = (MainActivity) ctx;
		switch (v.getId()) {
		case R.id.rl_logout:
			// 注销登录时，退出应用，关闭服务
			IMService.getInstance().stopSelf();
			Intent loginIntent = new Intent(ctx, LoginActivity.class);
			ctx.startActivity(loginIntent);
			act.finish();
			break;
		case R.id.rl_info:
			MyUtils.showToast(act, "个人信息");
			Intent zoneIntent = new Intent(ctx, UserInfoActivity.class);
			ctx.startActivity(zoneIntent);
			break;

		default:
			break;
		}
	}

}
