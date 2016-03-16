package com.open.im.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.pager.BasePager;
import com.open.im.pager.ContactPager;
import com.open.im.pager.NewsPager;
import com.open.im.pager.RoomsPager;
import com.open.im.pager.SettingPager;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyUtils;
import com.open.im.view.ActionItem;
import com.open.im.view.MyViewPager;
import com.open.im.view.TitlePopup;

public class MainActivity extends Activity implements OnClickListener {

	private MyViewPager viewPager;
	private ImageButton ib_news, ib_contact, ib_setting, ib_groups;
	private MainActivity act;
	private MyAdapter adapter;
	private List<BasePager> pagers;
	private int lastPosition = 0;
	private int height;
	private TextView tv_title;
	private Button btn_add;
	private TitlePopup titlePopup;
	private ImageButton ib_more;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		act = this;
		initView();

		initData();

		register();

	}

	/**
	 * 注册点击监听
	 */
	private void register() {
		ib_news.setOnClickListener(this);
		ib_contact.setOnClickListener(this);
		ib_setting.setOnClickListener(this);
		// ib_groups.setOnClickListener(this);
	}

	/**
	 * 初始化数据
	 */
	private void initData() {

		// 给标题栏弹窗添加子类
		titlePopup.addAction(new ActionItem(act, "发起聊天", R.mipmap.mm_title_btn_compose_normal));
		titlePopup.addAction(new ActionItem(act, "听筒模式", R.mipmap.mm_title_btn_receiver_normal));
		titlePopup.addAction(new ActionItem(act, "登录网页", R.mipmap.mm_title_btn_keyboard_normal));
		titlePopup.addAction(new ActionItem(act, "扫一扫", R.mipmap.mm_title_btn_qrcode_normal));

		pagers = new ArrayList<BasePager>();
		pagers.add(new NewsPager(act));
		pagers.add(new ContactPager(act));
		pagers.add(new RoomsPager(act));
		pagers.add(new SettingPager(act));

		adapter = new MyAdapter();
		viewPager.setAdapter(adapter);

		// 默认显示消息列表页面
		viewPager.setCurrentItem(0);
		ib_news.setEnabled(false);
		tv_title.setText("消息列表");
		btn_add.setVisibility(View.GONE);
		ib_more.setVisibility(View.VISIBLE);

		final ViewTreeObserver vto = viewPager.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				viewPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				height = viewPager.getHeight();
				MyLog.showLog("viewpager高度:" + height);
				MyLog.showLog("屏幕高度:" + MyUtils.getScreenHeight(act));
			}
		});
	}

	public int getViewPagerHight() {
		return height;
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		viewPager = (MyViewPager) findViewById(R.id.viewPager);
		ib_news = (ImageButton) findViewById(R.id.ib_news);
		ib_contact = (ImageButton) findViewById(R.id.ib_contact);
		ib_setting = (ImageButton) findViewById(R.id.ib_setting);
		// ib_groups = (ImageButton) findViewById(R.id.ib_groups);

		titlePopup = new TitlePopup(act, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		tv_title = (TextView) findViewById(R.id.tv_title);
		btn_add = (Button) findViewById(R.id.btn_add);
		ib_more = (ImageButton) findViewById(R.id.btn_more);

		btn_add.setOnClickListener(this);
		ib_more.setOnClickListener(this);
	}

	/**
	 * viewPager设置的adapter 填充四个自定义pager
	 * 
	 * @author Administrator
	 * 
	 */
	private class MyAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return pagers.size();
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {

			View view = pagers.get(position).initView();
			pagers.get(position).initData();
			container.addView(view);

			return view;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// super.destroyItem(container, position, object);
			container.removeView((View) object);
		}

	}

	@Override
	/**
	 * 重新可见时，刷新好友列表
	 */
	protected void onStart() {
		super.onStart();
		// RoomsPager rp = (RoomsPager) pagers.get(2);
		// rp.refreshRoomList();
	}

	@Override
	/**
	 * 处理点击事件
	 */
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ib_news:
			if (0 != lastPosition) {
				showPager(0, false, true, true, true);
				btn_add.setVisibility(View.GONE);
				ib_more.setVisibility(View.VISIBLE);
				tv_title.setText("消息列表");
			}
			break;
		case R.id.ib_contact:
			if (1 != lastPosition) {
				showPager(1, true, false, true, true);
				btn_add.setVisibility(View.VISIBLE);
				ib_more.setVisibility(View.GONE);
				tv_title.setText("我的好友");
			}
			break;
		// case R.id.ib_groups:
		// if (2 != lastPosition) {
		// showPager(2, true, true, false, true);
		// // rp.refreshRoomList();
		// }
		// break;
		case R.id.ib_setting:
			if (3 != lastPosition) {
				showPager(3, true, true, true, false);
				btn_add.setVisibility(View.GONE);
				ib_more.setVisibility(View.GONE);
				tv_title.setText("个人中心");
			}
			break;
		case R.id.btn_add:
			act.startActivity(new Intent(act, AddFriendActivity.class));
			break;
		case R.id.btn_more:
			titlePopup.show(v);
			break;
		}
	}

	/**
	 * 根据点击位置 设置显示的pager
	 * 
	 * @param item
	 * @param b1
	 * @param b2
	 * @param b3
	 */
	private void showPager(int item, boolean b1, boolean b2, boolean b3, boolean b4) {
		viewPager.setCurrentItem(item);
		lastPosition = item;
		ib_news.setEnabled(b1);
		ib_contact.setEnabled(b2);
		// ib_groups.setEnabled(b3);
		ib_setting.setEnabled(b4);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// IMService.getInstance().stopSelf();
		// MyLog.showLog("关闭服务");
	}
}
