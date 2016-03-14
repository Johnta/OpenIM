package com.open.im.pager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterGroup;
import org.jivesoftware.smack.roster.RosterListener;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.activity.AddFriendActivity;
import com.open.im.activity.ChatActivity;
import com.open.im.activity.MainActivity;
import com.open.im.app.MyApp;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyPubSubUtils;
import com.open.im.utils.MyUtils;
import com.open.im.utils.PinyinComparator;
import com.open.im.utils.ThreadUtil;
import com.open.im.view.SideBar;

public class ContactPager extends BasePager {

	private MyAdapter adapter;
	private Roster roster;
	private MainActivity act;
	private WindowManager mWindowManager;
	private ListView lv_show_friends;
	private SideBar indexBar;
	private TextView mDialogText;
	private android.view.WindowManager.LayoutParams lp;
	private AbstractXMPPConnection connection;
	private String groupName;
	private ArrayList<String> friendNames;
	private ArrayList<String> friendNicks;
	private String[] friends;
	private ProgressDialog pd;

	private final static int LOAD_SUCCESS = 201;

	public ContactPager(Context ctx) {
		super(ctx);
		act = (MainActivity) ctx;
	}

	@Override
	public View initView() {

		View view = View.inflate(act, R.layout.activity_im_friends, null);
		mWindowManager = (WindowManager) act.getSystemService(Context.WINDOW_SERVICE);
		lv_show_friends = (ListView) view.findViewById(R.id.lv_show_friends);
		indexBar = (SideBar) view.findViewById(R.id.sideBar);
		indexBar.setListView(lv_show_friends);
		mDialogText = (TextView) View.inflate(act, R.layout.list_position, null);
		mDialogText.setVisibility(View.INVISIBLE);
		lp = new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
		mWindowManager.addView(mDialogText, lp);
		indexBar.setTextView(mDialogText);

		connection = MyApp.connection;

		// 获取点击组时传递过来的组名 查找好友的依据
		groupName = "我的好友";

		return view;
	}

	@Override
	/**
	 * 初始化数据 设置adapter
	 */
	public void initData() {
		friendNames = new ArrayList<String>();
		friendNicks = new ArrayList<String>();

		roster = Roster.getInstanceFor(MyApp.connection);
		group = roster.getGroup(groupName);

		if (group == null) {
			// 创建组
			roster.createGroup(groupName);
			group = roster.getGroup(groupName);
		}

		// 通过组获取组内所有的好友

		registerRosterLinstener();

		pd = new ProgressDialog(act);
		pd.setMessage("正在加载好友列表，请稍后...");
		pd.show();

		queryFriends();

		register();
	}

	/**
	 * 查询好友
	 */
	private void queryFriends() {
		friendNames.clear();
		friendNicks.clear();
		ThreadUtil.runOnBackThread(new Runnable() {
			@Override
			public void run() {
				List<RosterEntry> users = group.getEntries();
				if (users == null) {
					return;
				}

				MyLog.showLog("user::" + users.size());

				// 遍历获得所有组内所有好友的名称
				for (RosterEntry rosterEntry : users) {
					// 判断用户是否在线
					String name = rosterEntry.getName();
					String jid = rosterEntry.getUser();
					// MyLog.showLog("jid::" + jid);
					// 获取卡片信息 通过用户Jid获取用户昵称
					// VCardManager vCardManager =
					// VCardManager.getInstanceFor(connection);
					// try {
					// MyLog.showLog("昵称:" + rosterEntry.getUser());
					// VCard vCard =
					// vCardManager.loadVCard(rosterEntry.getUser());
					// String nickName = vCard.getNickName();
					// friendNicks.add(nickName);
					// } catch (NoResponseException e) {
					// e.printStackTrace();
					// } catch (XMPPErrorException e) {
					// e.printStackTrace();
					// } catch (NotConnectedException e) {
					// e.printStackTrace();
					// }
					if (!friendNames.contains(name)) {
						friendNames.add(name);
					}
					MyLog.showLog("好友:" + name);
				}
				handler.sendEmptyMessage(LOAD_SUCCESS);
			}
		});
	}

	private class MyAdapter extends BaseAdapter implements SectionIndexer {

		@SuppressWarnings("unchecked")
		public MyAdapter() {
			Arrays.sort(friends, new PinyinComparator());
		}

		@Override
		public int getCount() {
			return friends.length;
		}

		@Override
		public Object getItem(int position) {
			if (friends.length != 0) {
				return friends[position];
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String friendName = friends[position];
			View view = null;
			ViewHolder vh = null;
			if (convertView == null) {
				vh = new ViewHolder();
				view = View.inflate(act, R.layout.list_item_show_friends, null);
				vh.tvNick = (TextView) view.findViewById(R.id.tv_friend_name);
				vh.tvCatalog = (TextView) view.findViewById(R.id.tv_log);
				vh.ivAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
				view.setTag(vh);
			} else {
				view = convertView;
				vh = (ViewHolder) view.getTag();
			}

			String catalog = converterToFirstSpell(friendName).substring(0, 1).toUpperCase();
			if (position == 0) {
				vh.tvCatalog.setVisibility(View.VISIBLE);
				vh.tvCatalog.setText(catalog);
			} else {
				String lastCatalog = converterToFirstSpell(friends[position - 1]).substring(0, 1);
				if (catalog.equalsIgnoreCase(lastCatalog)) {
					vh.tvCatalog.setVisibility(View.GONE);
				} else {
					vh.tvCatalog.setVisibility(View.VISIBLE);
					vh.tvCatalog.setText(catalog);
				}
			}

			vh.tvNick.setText(friends[position]);
			vh.ivAvatar.setImageResource(R.drawable.ic_launcher);

			// TextView tv_friend_state = (TextView)
			// view.findViewById(R.id.tv_friend_state);
			// if (position < onlineFriendNames.size()) {
			// tv_friend_name.setText(onlineFriendNames.get(position));
			// // tv_friend_state.setText("在线");
			// tv_friend_name.setTextColor(Color.GREEN);
			// // tv_friend_state.setTextColor(Color.GREEN);
			// } else {
			// tv_friend_name.setText(offlineFriendNames.get(position -
			// onlineFriendNames.size()));
			// // tv_friend_state.setText("离线");
			// tv_friend_name.setTextColor(Color.RED);
			// // tv_friend_state.setTextColor(Color.RED);
			// }
			return view;
		}

		@Override
		public int getPositionForSection(int section) {
			for (int i = 0; i < friendNames.size(); i++) {
				String l = converterToFirstSpell(friendNames.get(i)).substring(0, 1);
				char firstChar = l.toUpperCase().charAt(0);
				if (firstChar == section) {
					return i;
				}
			}
			return -1;
		}

		@Override
		public int getSectionForPosition(int position) {
			return 0;
		}

		@Override
		public Object[] getSections() {
			return null;
		}

	}

	static class ViewHolder {
		TextView tvCatalog;// 目录
		ImageView ivAvatar;// 头像
		TextView tvNick;// 昵称
	}

	/**
	 * 汉字转换位汉语拼音首字母，英文字符不变
	 * 
	 * @param chines
	 *            汉字
	 * @return 拼音
	 */
	public static String converterToFirstSpell(String chines) {
		String pinyinName = "";
		if (chines != null) {
			char[] nameChar = chines.toCharArray();
			HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
			defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
			defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
			for (int i = 0; i < nameChar.length; i++) {
				if (nameChar[i] > 128) {
					try {
						pinyinName += PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0].charAt(0);
					} catch (BadHanyuPinyinOutputFormatCombination e) {
						e.printStackTrace();
					}
				} else {
					pinyinName += nameChar[i];
				}
			}
		}
		return pinyinName;
	}

	private void register() {
		/**
		 * 好友列表条目设置点击监听
		 */
		lv_show_friends.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String friendName = null;
				// if (position < onlineFriendNames.size()) {
				// friendName = onlineFriendNames.get(position);
				// } else {
				// friendName = offlineFriendNames.get(position -
				// onlineFriendNames.size());
				// }
				friendName = friends[position];
				// 跳转到会话界面
				Intent intent = new Intent(act, ChatActivity.class);
				intent.putExtra("friendName", friendName);
				act.startActivity(intent);
			}
		});
		/**
		 * 长按删除好友
		 */
		lv_show_friends.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				String friendName = null;
				// if (position < onlineFriendNames.size()) {
				// friendName = onlineFriendNames.get(position);
				// onlineFriendNames.remove(position);
				// } else {
				// friendName = offlineFriendNames.get(position -
				// onlineFriendNames.size());
				// offlineFriendNames.remove(position -
				// onlineFriendNames.size());
				// }
				friendName = friends[position];
				RosterEntry entry = roster.getEntry(friendName + "@" + connection.getServiceName());
				MyLog.showLog(friendName);
				MyLog.showLog("entry" + entry);
				try {
					roster.removeEntry(entry);
					// 删除好友时取消订阅
					MyPubSubUtils.unSubscribeFriend(friendName + "@" + connection.getServiceName());
					MyUtils.showToast(act, "删除好友成功");
					adapter.notifyDataSetChanged();
				} catch (NotLoggedInException e) {
					e.printStackTrace();
				} catch (NoResponseException e) {
					e.printStackTrace();
				} catch (XMPPErrorException e) {
					e.printStackTrace();
				} catch (NotConnectedException e) {
					e.printStackTrace();
				}
				return true;
			}
		});

	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LOAD_SUCCESS:
				pdDismiss();

				MyLog.showLog("friendNames::" + friendNames);

				friends = (String[]) friendNames.toArray(new String[friendNames.size()]);
				adapter = new MyAdapter();
				lv_show_friends.setAdapter(adapter);

				/**
				 * listView滑动时，显示当前可见的第一条的拼音首字母
				 */
				lv_show_friends.setOnScrollListener(new OnScrollListener() {

					@Override
					public void onScrollStateChanged(AbsListView view, int scrollState) {
					}

					@Override
					public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
						String firstItem = (String) adapter.getItem(firstVisibleItem);
						String converterToFirstSpell = converterToFirstSpell(firstItem);
						if (!TextUtils.isEmpty(converterToFirstSpell)) {
							String log = converterToFirstSpell.substring(0, 1).toUpperCase();
							mDialogText.setText(log);
						}
					}
				});

				/**
				 * 判断何时显示提示首字母的view
				 */
				lv_show_friends.setOnTouchListener(new OnTouchListener() {

					@Override
					public boolean onTouch(View v, MotionEvent event) {
						if (event.getAction() == MotionEvent.ACTION_UP) {
							mDialogText.setVisibility(View.INVISIBLE);
						} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
							mDialogText.setVisibility(View.VISIBLE);
						}
						return false;
					}
				});

				break;

			default:
				break;
			}
		};
	};
	private RosterGroup group;

	private void pdDismiss() {
		if (pd != null && pd.isShowing()) {
			pd.dismiss();
		}
	}

	/**
	 * 注册好友列表监听
	 */
	private void registerRosterLinstener() {
		Roster roster = Roster.getInstanceFor(connection);
		roster.addRosterListener(new RosterListener() {
			@Override
			/**
			 *
			 */
			public void entriesAdded(Collection<String> addresses) {
				// for (String string : addresses) {
				// MyLog.showLog(string);
				// }
				MyLog.showLog("1------");
				queryFriends();
			}

			@Override
			/**
			 *
			 */
			public void entriesUpdated(Collection<String> addresses) {
				MyLog.showLog("2------");
				queryFriends();
			}

			@Override
			/**
			 *删除好友回调
			 */
			public void entriesDeleted(Collection<String> addresses) {
				MyLog.showLog("3-------");
				queryFriends();
			}

			@Override
			/**
			 *好友状态改变时 回调
			 */
			public void presenceChanged(Presence presence) {
				MyLog.showLog("4------好友状态改变");
			}
		});

	}

}
