package com.open.im.activity;

import java.util.Map;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.RoomInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.adapter.MyLVAdapter;
import com.open.im.app.MyApp;
import com.open.im.db.MultiChatDao;
import com.open.im.receiver.MyMultiChatMessageLinstener;
import com.open.im.utils.MyUtils;

public class MultiChatActivity extends Activity implements OnClickListener {
	private TextView tv_title;
	private EditText et_msg;
	private TextView tv_send;
	private ListView lv_messages;
	private MultiChatDao multiChatDao;
	private MultiChatActivity act;
	private AbstractXMPPConnection connection;
	private String roomJid;
	private MultiUserChat multiUserChat;
	private String roomName;
	private ProgressDialog pd;
	private Cursor cursor;
	private static final int QUERY_SUCCESS = 101;
	private MyLVAdapter adapter;
	private Button btn_invite;
	private Button btn_quit;
	private int flag;
	private MyMultiChatMessageLinstener multiChatMessageListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_multichat);
		//初始化
		init();
		//初始化消息数据
		initData();
		//按钮点击事件监听
		register();
	}

	/**
	 * 查询数据库 初始化聊天数据 收到的消息先存到数据 发出的消息发出后存到数据库 显示界面直接跟数据库关联 数据库改变 listview改变
	 */
	private void initData() {
		queryMessage();
	}

	/**
	 * 开子线程查数据库 获取聊天记录
	 */
	private void queryMessage() {
		pd = new ProgressDialog(act);
		pd.setMessage("加载中...");
		pd.show();
		new Thread() {

			public void run() {
				// 开子线程查询与指定好友的所有的聊天信息
				cursor = multiChatDao.getAllMessages(MyApp.username + "#" + roomName);
				// 发送查询完成消息
				handler.sendEmptyMessage(QUERY_SUCCESS);
			};
		}.start();
	}

	/**
	 * 消息查询完成处理操作
	 */
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			// 查询成功
			case QUERY_SUCCESS:
				if (pd.isShowing()) {
					pd.dismiss();
				}
				if (adapter == null) {
					// 使用的是cursorAdapter 查询结束后创建adapter
					adapter = new MyLVAdapter(act, cursor,lv_messages);
				}
				lv_messages.setAdapter(adapter);
				// 设置默认显示在listView的最后一行
				lv_messages.setSelection(adapter.getCount() - 1);
				break;
			}
		};
	};

//	/**
//	 * cursorAdapter填充listView
//	 * 
//	 * @author Administrator
//	 * 
//	 */
//	private class MyAdapter extends CursorAdapter {
//
//		public MyAdapter(Context context, Cursor c) {
//			super(context, c);
//		}
//
//		@Override
//		/**
//		 * 当关联的数据库发生变化时，会执行此方法
//		 * 此方法的super不能删除 它是更新数据
//		 */
//		protected void onContentChanged() {
//			super.onContentChanged();
//			// 每次数据库发生变化时，都让listView显示在最后一行
//			lv_messages.setSelection(adapter.getCount());
//		}
//
//		@Override
//		/**
//		 * 创建新的view 当convertView为null时，会执行此方法创建新的view
//		 */
//		public View newView(Context context, Cursor cursor, ViewGroup parent) {
//			ViewHolder vh = new ViewHolder();
//			View view = View.inflate(act, R.layout.list_item_multichat_detail, null);
//			LinearLayout ll_receive = (LinearLayout) view.findViewById(R.id.ll_receive);
//			TextView tv_others = (TextView) view.findViewById(R.id.tv_others);
//			TextView tv_receive_body = (TextView) view.findViewById(R.id.tv_receive_body);
//			TextView tv_receive_date = (TextView) view.findViewById(R.id.tv_receive_date);
//			LinearLayout ll_send = (LinearLayout) view.findViewById(R.id.ll_send);
//			TextView tv_send_body = (TextView) view.findViewById(R.id.tv_send_body);
//			TextView tv_send_date = (TextView) view.findViewById(R.id.tv_send_date);
//
//			vh.receive = ll_receive;
//			vh.receiveOthers = tv_others;
//			vh.receiveBody = tv_receive_body;
//			vh.receiveDate = tv_receive_date;
//			vh.send = ll_send;
//			vh.sendBody = tv_send_body;
//			vh.sendDate = tv_send_date;
//
//			view.setTag(vh);
//			return view;
//		}
//
//		@Override
//		/**
//		 * 为view设置数据
//		 */
//		public void bindView(View view, Context context, Cursor cursor) {
//			ViewHolder vh = (ViewHolder) view.getTag();
//			// cursor是前面查询数据后 返回的那个cursor 也是创建cursorAdapter时的参数
//			// 通过cursor获取查询到的的cursor指向的表里的数据
//			// int isComing =
//			// cursor.getInt(cursor.getColumnIndex(DBcolumns.MSG_ISCOMING));
//			String msgBody = cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_BODY));
//			// 显示时间 如果是今天 则只显示时间
//			// 如果不是今天 则显示日期
//			Long msgDateLong = cursor.getLong(cursor.getColumnIndex(DBcolumns.MSG_DATE));
//			String msgDate;
//			if (DateUtils.isToday(msgDateLong)) { // 判断是否是今天
//				msgDate = DateFormat.getTimeFormat(act).format(msgDateLong);
//			} else {
//				msgDate = DateFormat.getDateFormat(act).format(msgDateLong);
//			}
//
//			String msgFrom = cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_FROM));
//			// 判断是收消息 还是发消息
//			if (msgFrom.equals(MyApp.username)) {
//				vh.send.setVisibility(View.VISIBLE);
//				vh.receive.setVisibility(View.GONE);
//				vh.sendBody.setText(msgBody);
//				vh.sendDate.setText(msgDate);
//			} else {
//				// adapter定理 有if必有else 不然会乱跳
//				vh.send.setVisibility(View.GONE);
//				vh.receive.setVisibility(View.VISIBLE);
//				vh.receiveBody.setText(msgBody);
//				vh.receiveDate.setText(msgDate);
//				vh.receiveOthers.setText(msgFrom + ":");
//			}
//		}
//
//	}
//
//	/**
//	 * 使用viewHolder减少findviewbyid的次数 缩短显示条目的时间
//	 * 
//	 * @author Administrator
//	 * 
//	 */
//	private class ViewHolder {
//
//		public TextView receiveOthers;
//		public TextView sendDate;
//		public TextView sendBody;
//		public TextView receiveDate;
//		public TextView receiveBody;
//		public LinearLayout send;
//		public LinearLayout receive;
//
//	}

	/**
	 * 邀请好友加入聊天室对话框
	 */
	private void showInviteFriendDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		final AlertDialog dialog = builder.create();
		// 自定义的dialog 初始化控件
		View dialogView = View.inflate(act, R.layout.dialog_join_room, null);
		Button btn_ok = (Button) dialogView.findViewById(R.id.btn_ok);
		Button btn_cancel = (Button) dialogView.findViewById(R.id.btn_cancel);
		TextView tv_title = (TextView) dialogView.findViewById(R.id.tv_title);
		final EditText friendName = (EditText) dialogView.findViewById(R.id.et_input_pwd);
		tv_title.setText("请输入好友账号");
		friendName.setHint("用户名");

		// 确定按钮点击事件
		btn_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String inputPwd = friendName.getText().toString().trim();
				if (TextUtils.isEmpty(inputPwd)) {
					friendName.setError("请输入用户名");
					return;
				}
				try {
					// 邀请好友加入聊天室
					multiUserChat.invite(inputPwd + "@" + connection.getServiceName(), "快来一起聊天吧!");
					dialog.dismiss();
				} catch (NotConnectedException e) {
					e.printStackTrace();
				}
			}

		});
		// 取消按钮点击事件
		btn_cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.setView(dialogView, 0, 0, 0, 0);
		dialog.show();
	}

	/**
	 * 注册按钮点击监听
	 */
	private void register() {
		//离开
		btn_quit.setOnClickListener(this);
		//邀请好友加入
		btn_invite.setOnClickListener(this);
		//发送消息
		tv_send.setOnClickListener(this);
		//长按离开按钮删除房间 必须是owner删除 其他人删除会报禁止
		btn_quit.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				try {
					multiUserChat.destroy("删除房间", roomJid);
					finish();
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

	/**
	 * 初始化
	 */
	private void init() {
		// 初始化控件
		initView();
		//获取连接对象
		connection = MyApp.connection;
		//获取聊天室数据库操作Dao  用于查询数据库消息
		multiChatDao = MultiChatDao.getInstance(act);
		//获取聊天室管理者对象
		MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
		//获得启动activity时传递过来的数据  房间Jid  和 标记  1 表示此次创建activity是加入房间 其他表示普通进入(已经加入过了)
		roomJid = getIntent().getStringExtra("roomJid");
		flag = getIntent().getIntExtra("flag", 0);

		try {
			//通过房间JID获取房间信息对象
			RoomInfo roomInfo = multiUserChatManager.getRoomInfo(roomJid);
			//通过房间信息对象获取房间名称
			roomName = roomInfo.getName();
			//设置房间标题
			tv_title.setText(roomName);
			//通过房间Jid获取当前聊天室对象
			multiUserChat = multiUserChatManager.getMultiUserChat(roomJid);
			//存储着已经给各个聊天室已经添加过的监听   在开启服务时创建 并赋值给MyApp中的静态变量
			Map<String, MyMultiChatMessageLinstener> linstenerMap;
//			if (flag == 1) {  //如果flag为1 表示此次进入是加入房间  那么创建监听并添加监听 并将监听存到map中
				//获取消息通知对象 收到消息时在通知栏提示新消息
				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				linstenerMap = MyApp.linstenerMap;
				multiChatMessageListener = linstenerMap.get(roomName);
				if (multiChatMessageListener == null) {
					//获取聊天室消息监听对象 当收到消息时，会回调对象里的方法
					multiChatMessageListener = new MyMultiChatMessageLinstener(act, roomName, notificationManager);
				}
				//从Myapp获取Map对象 并将 添加的监听存到Map中
				linstenerMap.put(roomName, multiChatMessageListener);
				//添加本聊天室消息监听
				multiUserChat.addMessageListener(multiChatMessageListener);
//			} 
//			else {  //如果是普通进入 (这次进入之前就已经加入聊天室过了)
//				//从MyAPP获取map对象
//				linstenerMap = MyApp.linstenerMap;
//				//从map对象中 获取本聊天室使用的监听对象  用于将离开聊天室时取消监听用
//				multiChatMessageListener = linstenerMap.get(roomName);
//			}
		} catch (NoResponseException e) {
			e.printStackTrace();
		} catch (XMPPErrorException e) {
			e.printStackTrace();
		} catch (NotConnectedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化控件
	 */
	private void initView() {
		act = this;
		tv_title = (TextView) findViewById(R.id.tv_title);
		et_msg = (EditText) findViewById(R.id.et_msg);
		tv_send = (TextView) findViewById(R.id.tv_send);
		lv_messages = (ListView) findViewById(R.id.lv_messages);
		btn_invite = (Button) findViewById(R.id.btn_invite);
		btn_quit = (Button) findViewById(R.id.btn_quit);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
	}

	@Override
	/**
	 * 按钮点击事件
	 */
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_invite:  //邀请按钮点击事件 弹出邀请对话框
			showInviteFriendDialog();
			break;
		case R.id.btn_quit:  //离开按钮点击事件 退出本聊天室
			try {
				multiUserChat.leave();  //离开聊天室
				multiUserChat.removeMessageListener(multiChatMessageListener);  //取消本聊天室的消息监听
				finish(); //关闭本页
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
			break;
		case R.id.tv_send:  //发送按钮点击事件
			String msgBody = et_msg.getText().toString().trim();
			et_msg.setText("");
			if (TextUtils.isEmpty(msgBody)) {
				MyUtils.showToast(act, "消息不能为空");
				return;
			}
			try {
				multiUserChat.sendMessage(msgBody);  //发送群聊消息
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
			break;
		}
	}
}
