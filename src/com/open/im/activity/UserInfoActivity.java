package com.open.im.activity;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.utils.MyLog;
import com.open.im.utils.ThreadUtil;
import com.open.im.wheel.SelectBirthday;

public class UserInfoActivity extends Activity {
	// private LinearLayout ll_icon;
	// private LinearLayout ll_nick;
	// private LinearLayout ll_sex;
	// private LinearLayout ll_birthday;
	// private LinearLayout ll_address;
	// private LinearLayout ll_email;
	// private LinearLayout ll_phone;
	// private LinearLayout ll_qianming;
	// private ImageView iv_icon;
	// private TextView tv_nick;
	// private TextView tv_sex;
	// private TextView tv_brithday;
	// private TextView tv_address;
	// private TextView tv_email;
	// private TextView tv_phone;
	// private TextView tv_qianming;

	private static final int QUERY_SUCCESS = 100;
	private ListView mListview;
	private UserInfoActivity act;
	private String[] items = { "头像", "昵称", "性别", "生日", "地址", "邮箱", "电话", "签名" };
	private VCard vCard;
	private String nickName;
	private String homeAddress;
	private String email;
	private String phone;
	private String sex;
	private String desc;
	private String bday;
	private ImageButton ib_back;
	protected SelectBirthday birth;
	private VCardManager vCardManager;
	private LinearLayout ll_root;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// setContentView(R.layout.activity_info);
		setContentView(R.layout.activity_userinfo);
		// 初始化控件
		initView();
		// 初始化数据
		initData();
		// 注册点击事件
		register();
	}

	/**
	 * 注册条目点击事件
	 */
	private void register() {
		mListview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(act, UserInfoUpdateActivity.class);
				int type = -1;
				switch (position) {
				case 0: // 头像

					break;
				case 1: // 昵称
					type = 1;
					intent.putExtra("info", nickName);
					intent.putExtra("type", type);
					startActivityForResult(intent, type);
					break;
				case 2: // 性别
					type = 2;
					Intent sexIntent = new Intent(act, UserSexUpdateActivity.class);
					sexIntent.putExtra("sex", sex);
					startActivityForResult(sexIntent, type);
					break;
				case 3: // 生日
					birth = new SelectBirthday(act, bday);
					birth.showAtLocation(ll_root, Gravity.BOTTOM, 0, 0);
					birth.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss() {
							ThreadUtil.runOnBackThread(new Runnable() {
								@Override
								public void run() {
									try {
										/**
										 * 重新加载一个vCard，如果还是使用以前的VCard，
										 * 那么那个VCard对象里面存的日期还是原来的日期
										 */
										vCard = vCardManager.loadVCard();
										bday = vCard.getField("BDAY");
									} catch (NoResponseException e) {
										e.printStackTrace();
									} catch (XMPPErrorException e) {
										e.printStackTrace();
									} catch (NotConnectedException e) {
										e.printStackTrace();
									}
									handler.sendEmptyMessage(QUERY_SUCCESS);
								}
							});
						}
					});
					break;
				case 4: // 地址
					type = 4;
					intent.putExtra("info", homeAddress);
					intent.putExtra("type", type);
					startActivityForResult(intent, type);
					break;
				case 5: // 邮箱
					type = 5;
					intent.putExtra("info", email);
					intent.putExtra("type", type);
					startActivityForResult(intent, type);
					break;
				case 6: // 电话
					type = 6;
					intent.putExtra("info", phone);
					intent.putExtra("type", type);
					startActivityForResult(intent, type);
					break;
				case 7: // 签名
					type = 7;
					intent.putExtra("info", desc);
					intent.putExtra("type", type);
					startActivityForResult(intent, type);
					break;
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (data == null) {
			return;
		}
		String info = data.getDataString();
		switch (requestCode) {
		case 0:

			break;
		case 1:
			vCard.setNickName(info);
			break;
		case 2:
			vCard.setField("SEX", info);
			break;
		case 3:

			break;
		case 4:
			vCard.setField("HOME_ADDRESS", info);
			break;
		case 5:
			vCard.setEmailHome(info);
			break;
		case 6:
			vCard.setField("PHONE", info);
			break;
		case 7:
			vCard.setField("DESC", info);
			break;
		}

		try {
			vCardManager.saveVCard(vCard);
			queryVCard(vCard);

		} catch (NoResponseException e) {
			e.printStackTrace();
		} catch (XMPPErrorException e) {
			e.printStackTrace();
		} catch (NotConnectedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化数据 查询VCard信息
	 */
	private void initData() {
		vCardManager = VCardManager.getInstanceFor(MyApp.connection);
		try {
			vCard = vCardManager.loadVCard();
			// vCard.setField("HOME_ADDRESS", "河南郑州");
			// // vCard.setField("EMAIL","1365260037@qq.com");
			// vCard.setEmailHome("1365260037@qq.com");
			// vCard.setField("PHONE", "110");
			// vCard.setField("SEX", "男");
			// vCard.setField("DESC", "我是一只小蚂蚁~~~");
			// vCard.setField("BDAY", "1989-1-1");
			// //将信息保存到VCard中
			// vCardManager.saveVCard(vCard);
			queryVCard(vCard);
		} catch (NoResponseException e) {
			e.printStackTrace();
		} catch (XMPPErrorException e) {
			e.printStackTrace();
		} catch (NotConnectedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 方法 查询VCard信息
	 * 
	 * @param vCard
	 */
	private void queryVCard(final VCard vCard) {
		ThreadUtil.runOnBackThread(new Runnable() {
			@Override
			public void run() {
				nickName = vCard.getNickName();
				homeAddress = vCard.getField("HOME_ADDRESS");
				email = vCard.getEmailHome();
				phone = vCard.getField("PHONE");
				sex = vCard.getField("SEX");
				desc = vCard.getField("DESC");
				bday = vCard.getField("BDAY");

				handler.sendEmptyMessage(QUERY_SUCCESS);
			}
		});
	}

	private void initView() {
		act = this;
		ll_root = (LinearLayout) findViewById(R.id.ll_root);
		mListview = (ListView) findViewById(R.id.lv_userinfo);
		ib_back = (ImageButton) findViewById(R.id.ib_back);
		ib_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private class ViewHolder {
		TextView item;
		TextView info;
		ImageView icon;
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case QUERY_SUCCESS:
				// 为listView设置数据
				mListview.setAdapter(new ArrayAdapter<String>(act, 0, items) {
					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						ViewHolder vh = null;
						if (convertView == null) {
							convertView = View.inflate(act, R.layout.list_item_userinfo, null);
							vh = new ViewHolder();
							vh.item = (TextView) convertView.findViewById(R.id.tv_item);
							vh.info = (TextView) convertView.findViewById(R.id.tv_info);
							vh.icon = (ImageView) convertView.findViewById(R.id.iv_icon);
							convertView.setTag(vh);
						} else {
							vh = (ViewHolder) convertView.getTag();
						}
						vh.item.setText(items[position]);
						if (position == 0) {
							vh.icon.setVisibility(View.VISIBLE);
							vh.info.setVisibility(View.GONE);
						} else {
							vh.icon.setVisibility(View.GONE);
							vh.info.setVisibility(View.VISIBLE);
						}

						switch (position) {
						case 1:
							if (TextUtils.isEmpty(nickName)) {
								nickName = "未填写";
							}
							vh.info.setText(nickName);
							break;
						case 2:
							if (TextUtils.isEmpty(sex)) {
								sex = "未填写";
							}
							vh.info.setText(sex);
							break;
						case 3:
							if (TextUtils.isEmpty(bday)) {
								bday = "未填写";
							}
							vh.info.setText(bday);
							break;
						case 4:
							if (TextUtils.isEmpty(homeAddress)) {
								homeAddress = "未填写";
							}
							vh.info.setText(homeAddress);
							break;
						case 5:
							if (TextUtils.isEmpty(email)) {
								email = "未填写";
							}
							vh.info.setText(email);
							break;
						case 6:
							if (TextUtils.isEmpty(phone)) {
								phone = "未填写";
							}
							vh.info.setText(phone);
							break;
						case 7:
							if (TextUtils.isEmpty(desc)) {
								desc = "未填写";
							}
							vh.info.setText(desc);
							break;
						}
						return convertView;
					}
				});
				break;

			default:
				break;
			}
		};
	};
}
