package com.open.im.pager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.muc.RoomInfo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.activity.CreateRoomActivity;
import com.open.im.activity.MainActivity;
import com.open.im.activity.MultiChatActivity;
import com.open.im.app.MyApp;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyUtils;
import com.open.im.utils.ThreadUtil;

/**
 * 聊天室界面
 * 
 * @author Administrator
 * 
 */
public class RoomsPager extends BasePager {

	private Button btn_add;
	private ListView lv_rooms;
	private AbstractXMPPConnection connection;
	private List<String> roomNameList;
	private ArrayList<String> roomJidList;
	private MultiUserChat multiUserChat;
	private MultiUserChatManager multiUserChatManager;
	private ArrayAdapter<String> adapter;
	private SharedPreferences sp;
	private RoomInfo roomInfo;

	public RoomsPager(Context ctx) {
		super(ctx);
	}

	@Override
	/**
	 * 初始化控件
	 */
	public View initView() {
		View view = View.inflate(ctx, R.layout.pager_im_rooms, null);
		btn_add = (Button) view.findViewById(R.id.btn_add);
		lv_rooms = (ListView) view.findViewById(R.id.lv_rooms);
		return view;
	}

	@Override
	/**
	 * 初始化数据
	 */
	public void initData() {
		connection = MyApp.connection;
		roomNameList = new ArrayList<String>();
		roomJidList = new ArrayList<String>();
		sp = ctx.getSharedPreferences(MyConstance.SP_NAME, 0);

		// 查询服务器上所有的聊天室并显示
		showHostedRooms();
		// 创建聊天室按钮点击监听
		registerCreateRoomLinstener();
	}

	/**
	 * 查询服务器上所有的聊天室并显示
	 */
	private void showHostedRooms() {
		// 获取聊天室管理者对象
		multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
		// 开子线程查询服务器上所有的房间
		ThreadUtil.runOnBackThread(new Runnable() {
			@Override
			public void run() {
				// 查询服务器上的聊天室
				queryHostedRooms();
				// 查询结束后，在主线程修改UI
				ThreadUtil.runOnUIThread(new Runnable() {
					@Override
					public void run() {
						// adapter使用的是arrayAdapter然后重写getView方法
						adapter = new ArrayAdapter<String>(ctx, 0, roomNameList) {
							public View getView(int position, View convertView, android.view.ViewGroup parent) {
								if (convertView == null) {
									convertView = View.inflate(ctx, R.layout.list_item_rooms, null);
								}
								TextView tv_roomname = (TextView) convertView.findViewById(R.id.tv_roomname);
								// 显示聊天室的名称
								tv_roomname.setText(roomNameList.get(position));
								return convertView;
							};
						};
						// listview设置adapter
						lv_rooms.setAdapter(adapter);
						// 为listview设置条目点击事件
						lv_rooms.setOnItemClickListener(new OnItemClickListener() {
							@Override
							public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
								try {
									// 通过点击位置 获取点击房间的信息对象
									roomInfo = multiUserChatManager.getRoomInfo(roomJidList.get(position));
									// 通过聊天室的jid获取指定聊天室的聊天室对象
									multiUserChat = multiUserChatManager.getMultiUserChat(roomJidList.get(position));

									// MyLog.showLog(roomInfo.getName());// 房间名称
									// bb
									// MyLog.showLog(roomInfo.getDescription());//
									// 房间描述
									// MyLog.showLog(roomInfo.getRoom() + "");
									// // b@conference.im2.daimaqiao.net
									// MyLog.showLog(roomInfo.isPasswordProtected()
									// + ""); // 是否有密码保护

									// 判断是否已经加入这个聊天室了
									boolean isJoined = multiUserChat.isJoined();
									MyLog.showLog("isJoined:" + isJoined);
									if (isJoined) { // 如果已经加入过这个聊天室了
													// 则开启聊天室聊天界面时，intent不携带flag
										Intent intent = new Intent(ctx, MultiChatActivity.class);
										intent.putExtra("roomJid", roomJidList.get(position));
										ctx.startActivity(intent);
										MyLog.showLog("已经加入过了");
									} else { // 没有加入这个聊天室
										MyLog.showLog("首次加入");
										// 首先根据聊天室房间详情对象判断房间是否有密码
										boolean isRoomProtected = roomInfo.isPasswordProtected();
										if (isRoomProtected) { // 如果有密码
											String roomPassword = sp.getString(roomInfo.getName() + "#password", null);
											if (roomPassword == null) {
												// 房间有密码
												// 并且用户从未输入过正确的密码并存到sp时，弹输入密码框
												showJoinRoomDialog(position);
											} else { // 用户曾经进入过房间 保存有密码
														// 密码正确则直接进入房间
												joinRoom(position, null, roomPassword);
											}
										} else {
											joinRoom(position, null, null);
										}
									}
								} catch (NoResponseException e) {
									e.printStackTrace();
								} catch (XMPPErrorException e) {
									e.printStackTrace();
								} catch (NotConnectedException e) {
									e.printStackTrace();
								}
							}
						});
					}
				});
			}
		});
	}

	/**
	 * 查询服务器上所有的聊天室
	 * 
	 * @throws NoResponseException
	 * @throws XMPPErrorException
	 * @throws NotConnectedException
	 */
	private void queryHostedRooms() {
		try {
			// 先把存储聊天室名称 和 Jid的list清空 防止重复添加
			roomNameList.clear();
			roomJidList.clear();
			// 通过聊天室管理者获取服务器上所有的聊天室信息 返回值是一个list
			List<HostedRoom> hostedRooms = multiUserChatManager
			// 必须加上 ".conference."
					.getHostedRooms("conference." + connection.getServiceName());
			// 遍历所有的聊天室
			for (HostedRoom hostedRoom : hostedRooms) {
				// MyLog.showLog("会议室名称：" + hostedRoom.getName()); // bb(0)
				// MyLog.showLog("会议室JID：" + hostedRoom.getJid()); //
				// 会议室JID：bb@conference.im2.daimaqiao.net
				// 通过JID可以获取房间的详细信息
				// RoomInfo roomInfo =
				// multiUserChatManager.getRoomInfo(hostedRoom.getJid());
				// MyLog.showLog("房间名：" + roomInfo.getName()); // bb
				// 将聊天室的名称(含人数) 和 Jid存到集合中

				// 将服务器上所有的聊天室的 名称 Jid存到集合中 这里存的名称是包含人数的 bb (0)
				roomNameList.add(hostedRoom.getName());
				roomJidList.add(hostedRoom.getJid());
			}
		} catch (NoResponseException e) {
			e.printStackTrace();
		} catch (XMPPErrorException e) {
			e.printStackTrace();
		} catch (NotConnectedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 刷新好友分组
	 */
	public void refreshRoomList() {
		if (adapter != null) {
			queryHostedRooms();
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * 进入有密码的房间时，显示输入密码框
	 * 
	 * @param position
	 */
	private void showJoinRoomDialog(final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
		final AlertDialog dialog = builder.create();
		// 自定义的dialog
		View dialogView = View.inflate(ctx, R.layout.dialog_join_room, null);
		Button btn_ok = (Button) dialogView.findViewById(R.id.btn_ok);
		Button btn_cancel = (Button) dialogView.findViewById(R.id.btn_cancel);
		final EditText et_input_pwd = (EditText) dialogView.findViewById(R.id.et_input_pwd);
		// 确定按钮点击事件
		btn_ok.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String inputPwd = et_input_pwd.getText().toString().trim();
				if (TextUtils.isEmpty(inputPwd)) {
					et_input_pwd.setError("请输入房间密码");
					return;
				}
				// 加入房间
				joinRoom(position, dialog, inputPwd);
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
	 * 加入房间
	 * 
	 * @param position
	 * @param dialog
	 * @param inputPwd
	 */
	private void joinRoom(final int position, final AlertDialog dialog, String inputPwd) {
		try {
			// 聊天室历史消息对象
			DiscussionHistory history = new DiscussionHistory();
			// 设置只接受加入聊天室以后的消息
			history.setSince(new Date());
			// 加入聊天室 使用的是 用户的用户名作为聊天室内的昵称
			// SmackConfiguration.getDefaultPacketReplyTimeout() 5000ms
			multiUserChat.join(MyApp.username, inputPwd, history, SmackConfiguration.getDefaultPacketReplyTimeout());
			MyLog.showLog("现在加入的房间数：" + multiUserChatManager.getJoinedRooms().size());
			String roomName = roomInfo.getName(); // 根据房间信息对象获取房间名 bb

			// Intent addLinstener = new Intent();
			// addLinstener.setAction(MyConstance.ACTION_MULTICHAT_ADD_LINSTENER);
			// addLinstener.putExtra("roomName", roomName);
			// ctx.sendBroadcast(addLinstener );

			// 加入房间后 将密码存到sp中 一个房间对应一个密码
			sp.edit().putString(roomName + "#password", inputPwd).commit();

			// MyMultiChatMessageLinstener multiChatMessageListener = new
			// MyMultiChatMessageLinstener(ctx, roomName, notificationManager);
			// multiUserChat.addMessageListener(multiChatMessageListener);

			// 开启聊天室聊天界面 intent要传递过去房间jid 和 flag = 1 表示加入聊天室
			Intent intent = new Intent(ctx, MultiChatActivity.class);
			intent.putExtra("roomJid", roomJidList.get(position));
			intent.putExtra("flag", 1); // 1表示需要添加监听
			ctx.startActivity(intent);

			// 密码框消失
			if (dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
		} catch (XMPPErrorException e) {
			// 密码错误异常
			if (e.getMessage().contains("not-authorized - auth")) {
				MyUtils.showToast((MainActivity) ctx, "密码错误");
			}
			e.printStackTrace();
		} catch (SmackException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 注册创建房间按钮点击监听
	 */
	private void registerCreateRoomLinstener() {
		btn_add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 跳转到创建聊天室页面
				Intent intent = new Intent(ctx, CreateRoomActivity.class);
				ctx.startActivity(intent);
			}
		});
	}

}
