package com.open.im.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.offline.OfflineMessageManager;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import com.open.im.R;
import com.open.im.activity.MultiChatActivity;
import com.open.im.app.MyApp;
import com.open.im.db.MultiChatDao;
import com.open.im.receiver.MyAddFriendStanzaLinstener;
import com.open.im.receiver.MyChatMessageLinstener;
import com.open.im.receiver.MyMultiChatMessageLinstener;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyLog;
import com.open.im.utils.ThreadUtil;

/**
 * 应用主服务进程
 * 
 * @author Administrator
 * 
 */
public class IMService extends Service {

	private SharedPreferences sp;
	private String username;
	private static IMService mIMService;

	private NotificationManager notificationManager;
	private AbstractXMPPConnection connection;
	private Map<String, MyMultiChatMessageLinstener> linstenerMap;
	private Date lastDate;
	private ChatManagerListener myChatManagerListener;
	private ChatManager cm;
	private MultiUserChatManager multiUserChatManager;
	private AlarmManager mAlarmManager;
	private PendingIntent mPendingIntent;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// 开启前台进程 程序不会被杀死
//		startForegroundService();
		// 初始化 获取用户名密码啥的
		init();
		// 初始化连接配置
		// XMPPConnectionUtils.initXMPPConnection();
		connection = MyApp.connection;
		// 登录
		// login(username, password);

		// 注册聊天室消息监听
//		registerMultiChatLinstener();
		// 登录时检查是否创建了node如果没创建则创建
//		MyPubSubUtils.createNode(username);
		// 给所有已订阅的用户添加监听
//		MyPubSubUtils.registerPublishLinstener(mIMService);
		// 获取所有已订阅的用户曾发布的消息并存到数据库
//		MyPubSubUtils.receiveAllPublishMsg(mIMService);

		// 注册添加好友请求监听
		registerAddFriendLinstener();

		// registerRosterLinstener();
		// 注册消息接收监听
		registerMessageListener();
		// 聊天室邀请监听
//		registerMultiChatInviteLinstener();
		// 文件接收监听
//		registerFileLinstener();
		
		// 离线消息监听
		initOfflineMessages();
		// MultiChatReceiver();
	}
	/**
	 * 方法 开启前台进程
	 */
	private void startForegroundService() {
		/**
		 * 参数一：Notification 显示在状态栏的图标 参数二：Notification 显示在状态栏上时，提示的一句话
		 */
		Notification notification = new Notification(R.drawable.ic_launcher, "OpenIM长期后台运行!", 0);

		// 开启activity时，具体要发送的intent
		Intent intent = new Intent("com.open.openim.main");
		intent.addCategory(Intent.CATEGORY_DEFAULT);

		// 点击Notification 以后，要干的事
		PendingIntent contentIntent = PendingIntent.getActivity(this, 88, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		// 为 Notification 进行常规设置
		notification.setLatestEventInfo(this, "OpenIM", "及时通讯聊天软件", contentIntent);

		// 将当前服务的重要级别提升为前台进程
		startForeground(10086, notification);
	}

	/**
	 * 注册文件接收监听
	 */
	private void registerFileLinstener() {
		// 获取文件传输管理者
		FileTransferManager ftm = FileTransferManager.getInstanceFor(connection);
		// 给文件传输管理者添加一个文件传输监听
		ftm.addFileTransferListener(new FileTransferListener() {
			@Override
			public void fileTransferRequest(final FileTransferRequest request) {
				// 开子线程接收文件
				ThreadUtil.runOnBackThread(new Runnable() {
					@Override
					public void run() {
						// 判断存储卡状态
						String externalStorageState = Environment.getExternalStorageState();
						if (externalStorageState.equals(Environment.MEDIA_MOUNTED)) { // 存储卡已挂载
							// 设置文件保存位置
							String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/exiu/receive";
							// 获取文件名称
							String fileName = request.getFileName();
							// 通过文件路径创建file对象
							File file = new File(filePath + File.separator + fileName);
							// 如果文件的上级文件夹不存在则创建文件夹
							if (!file.getParentFile().exists()) {
								file.getParentFile().mkdirs();
							}
							// 获取接收文件传输对象
							IncomingFileTransfer incomingFileTransfer = request.accept();
							try {
								// 获取文件大小
								long fileSize = incomingFileTransfer.getFileSize();
								MyLog.showLog("filesize::" + fileSize);
								// 接收文件 smack有个bug 第一次接收会失败 报 Negotiating Stream
								incomingFileTransfer.recieveFile(file);
								// 因为smack的bug只要接收没完成 就一直接收 到完成为止 很暴力
								while (!incomingFileTransfer.getStatus().equals(FileTransfer.Status.complete)) {
									// 如果文件接收状态为错误 就取消重新接收
									if (incomingFileTransfer.getStatus().equals(FileTransfer.Status.error)) {
										MyLog.showLog("接收进度::" + incomingFileTransfer.getError());
										incomingFileTransfer.cancel();
										incomingFileTransfer.recieveFile(file);
									} else {
										// 打印当前文件接收状态和接收进度
										MyLog.showLog("状态::" + incomingFileTransfer.getStatus());
										MyLog.showLog("接收进度::" + incomingFileTransfer.getProgress());
									}
								}
								// 文件接收完成
								if (incomingFileTransfer.isDone()) {
									MyLog.showLog("接收完成");
									// 添加这个方法 接收到的图片才能从图片显示出来
									scanFileToPhotoAlbum(filePath + File.separator + fileName);
								}

								// InputStream recieveFile =
								// incomingFileTransfer.recieveFile();
								// while (!incomingFileTransfer.isDone()) {
								// if
								// (incomingFileTransfer.getStatus().equals(FileTransfer.Status.error))
								// {
								// MyLog.showLog("接收进度::" +
								// incomingFileTransfer.getError());
								// }
								// MyLog.showLog(incomingFileTransfer.getStatus().toString());
								// }
							} catch (SmackException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						} else {
							MyLog.showLog("没有存储卡");
						}
					}
				});
			}
		});
	}

	/**
	 * 让保存的图片能在图库中能找到
	 * 
	 * @param path
	 *            图片的路径
	 */
	private void scanFileToPhotoAlbum(String path) {
		// 媒体扫描服务
		MediaScannerConnection.scanFile(this, new String[] { path }, null, new OnScanCompletedListener() {

			@Override
			public void onScanCompleted(String path, Uri uri) {
				Log.i("lzh", "Finished scanning " + path);
			}
		});
	}

	// private void MultiChatReceiver() {
	//
	// BroadcastReceiver addReceiver = new BroadcastReceiver() {
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// MyLog.showLog("添加监听");
	// MultiUserChatManager multiUserChatManager =
	// MultiUserChatManager.getInstanceFor(connection);
	// }
	// };
	// IntentFilter addFilter = new IntentFilter();
	// addFilter.addAction(MyConstance.ACTION_MULTICHAT_ADD_LINSTENER);
	// registerReceiver(addReceiver, addFilter);
	//
	// BroadcastReceiver removeReceiver = new BroadcastReceiver() {
	//
	// @Override
	// public void onReceive(Context context, Intent intent) {
	// MyLog.showLog("删除监听");
	// }
	// };
	// IntentFilter removeFilter = new IntentFilter();
	// removeFilter.addAction(MyConstance.ACTION_MULTICHAT_REMOVE_LINSTENER);
	// registerReceiver(removeReceiver, removeFilter);
	// }

	/**
	 * 聊天室邀请监听
	 */
	private void registerMultiChatInviteLinstener() {
		MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
		multiUserChatManager.addInvitationListener(new InvitationListener() {

			@Override
			public void invitationReceived(XMPPConnection conn, MultiUserChat room, String inviter, String reason, String password, Message message) {
				MyLog.showLog("收到邀请了");

				// room:bb@conference.im2.daimaqiao.net
				// inviter:bb@im2.daimaqiao.net/Spark
				// reason:快来聊天
				// password:bb
				// type:normal
				// MyLog.showLog("room:" + room.getRoom());
				// MyLog.showLog("inviter:" + inviter);
				// MyLog.showLog("reason:" + reason);
				// MyLog.showLog("password:" + password);
				// MyLog.showLog("type:" + message.getType().name());
				try {
					if (!room.isJoined()) {
						DiscussionHistory history = new DiscussionHistory();
						history.setSince(new Date());
						// 使用用户名加入房间 作为用户在房间的昵称
						room.join(MyApp.username, password, history, SmackConfiguration.getDefaultPacketReplyTimeout());
						Intent intent = new Intent(mIMService, MultiChatActivity.class);
						intent.putExtra("roomJid", room.getRoom());
						intent.putExtra("flag", 1); // 1表示加入房间
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						mIMService.startActivity(intent);
					} else {
						MyLog.showLog("他已经在房间里了");
					}
				} catch (XMPPErrorException e) {
					e.printStackTrace();
				} catch (SmackException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 添加好友请求监听
	 */
	private void registerAddFriendLinstener() {
		MyAddFriendStanzaLinstener myAddFriendStanzaLinstener = new MyAddFriendStanzaLinstener(this);
		// 过滤器
		StanzaFilter packetFilter = new StanzaFilter() {

			@Override
			public boolean accept(Stanza stanza) {
				if (stanza instanceof Presence) {
//					Presence presence = (Presence) stanza;
					return true;
					// 如果是好友申请 return true 返回值为true时，下面的监听才有用 这是过滤条件
//					if (presence.getType().equals(Presence.Type.subscribe)) {
//						return true;
//					}
				}
				return false;
			}
		};
		// 添加好友请求监听
		connection.addAsyncStanzaListener(myAddFriendStanzaLinstener, packetFilter);
	}

	// /**
	// * 注册好友列表监听
	// */
	// private void registerRosterLinstener() {
	// Roster roster = Roster.getInstanceFor(connection);
	// roster.addRosterListener(new RosterListener() {
	//
	// @Override
	// /**
	// *
	// */
	// public void entriesAdded(Collection<String> addresses) {
	// for (String string : addresses) {
	// MyLog.showLog(string);
	// }
	// }
	//
	// @Override
	// /**
	// *
	// */
	// public void entriesUpdated(Collection<String> addresses) {
	// MyLog.showLog("2------");
	//
	// }
	//
	// @Override
	// /**
	// *
	// */
	// public void entriesDeleted(Collection<String> addresses) {
	// MyLog.showLog("3-------");
	// }
	//
	// @Override
	// /**
	// *
	// */
	// public void presenceChanged(Presence presence) {
	// MyLog.showLog("4------");
	// }
	// });
	//
	// }

	/**
	 * 获取离线消息管理者 必须在用户登录之后才可以执行
	 * 服务器现在不支持离线消息
	 */
	private void initOfflineMessages() {
		OfflineMessageManager offlineMessageManager = new OfflineMessageManager(connection);
		try {
			boolean isSupport = offlineMessageManager.supportsFlexibleRetrieval();
			MyLog.showLog("是否支持离线::" + isSupport);
			MyLog.showLog(offlineMessageManager.getMessageCount() + "------");
			List<Message> offlineMessages = offlineMessageManager.getMessages();
			MyLog.showLog(offlineMessages.size() + "=======");
			for (Message message : offlineMessages) {
				MyLog.showLog(message.getFrom() + ":" + message.getBody());
			}
			offlineMessageManager.deleteMessages();
		} catch (NoResponseException e) {
			e.printStackTrace();
		} catch (XMPPErrorException e) {
			e.printStackTrace();
		} catch (NotConnectedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化 获取用户名和密码
	 */
	private void init() {
		mIMService = this;
		sp = getSharedPreferences(MyConstance.SP_NAME, 0);
		username = sp.getString("username", "");
		sp.getString("password", "");
		notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		linstenerMap = new HashMap<String, MyMultiChatMessageLinstener>();
		MultiChatDao multiChatDao = MultiChatDao.getInstance(mIMService);
		long theLastMsgDate = multiChatDao.queryTheLastMsgDate();
		lastDate = new Date(theLastMsgDate + 1000);
		MyLog.showLog("date：" + lastDate);

	}

	/**
	 * 方法 把自身返回 方便外部调用
	 * 
	 * @return
	 */
	public static IMService getInstance() {
		return mIMService;
	}

	/**
	 * 注册消息接收监听 这个监听注册后 聊天界面的监听就收不到消息了
	 */
	private void registerMessageListener() {
		cm = ChatManager.getInstanceFor(connection);
		myChatManagerListener = new ChatManagerListener() {

			@Override
			public void chatCreated(Chat chat, boolean createdLocally) {
				// 通过会话对象 注册一个消息接收监听
				chat.addMessageListener(new MyChatMessageLinstener(mIMService, notificationManager));
			}
		};
		cm.addChatListener(myChatManagerListener);
	}

	// /**
	// * 方法 登录
	// *
	// * @param username
	// * @param password
	// */
	// private void login(final String username, final String password) {
	//
	// new Thread() {
	//
	// public void run() {
	//
	// // // 模拟登录耗时
	// // SystemClock.sleep(1000);
	// try {
	// // 创建连接并登录
	// if (!connection.isConnected()) {
	// connection.connect();
	// }
	// connection.setPacketReplyTimeout(60 * 1000);
	// connection.login(username, password);
	//
	// roster = Roster.getInstanceFor(connection);
	// roster.setSubscriptionMode(SubscriptionMode.manual);
	// // 登录成功后 获取离线消息
	// // initOfflineMessages();
	// // 登录成功发广播
	// // sendLoginBroadcast(LOGIN_SUCCESS);
	// // 初始化VCard
	// initVCard(username);
	// // 注册聊天室消息监听
	// registerMultiChatLinstener();
	// // 登录时检查是否创建了node如果没创建则创建
	// MyPubSubUtils.createNode(username);
	// // 给所有已订阅的用户添加监听
	// MyPubSubUtils.registerPublishLinstener(mIMService);
	// // 获取所有已订阅的用户曾发布的消息并存到数据库
	// MyPubSubUtils.receiveAllPublishMsg(mIMService);
	// } catch (SmackException e) {
	// // sendLoginBroadcast(INTERNET_ERROR);
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (XMPPException e) {
	// if (e.getMessage().contains("not-authorized")) {
	// // 未注册异常
	// // sendLoginBroadcast(FAIL_UNKNOWN_USER);
	// } else if (e.getMessage().contains("bad-auth")) {
	// // 密码错误异常
	// // sendLoginBroadcast(FAIL_PASSWORD_ERROR);
	// } else {
	// // sendLoginBroadcast(LOGIN_FAIL);
	// }
	// e.printStackTrace();
	// }
	// }
	//
	// }.start();
	// }

	// /**
	// * 初始化名片信息 如果没有设置昵称则设置昵称 如果设置了昵称 则获取昵称 也必须在用户登录后才能设置
	// *
	// * @param username
	// * @throws NoResponseException
	// * @throws XMPPErrorException
	// * @throws NotConnectedException
	// */
	// private void initVCard(final String username) throws NoResponseException,
	// XMPPErrorException, NotConnectedException {
	// VCardManager vCardManager = VCardManager.getInstanceFor(connection);
	// VCard vCard = vCardManager.loadVCard();
	// String nickName = vCard.getNickName();
	//
	// if (nickName == null) {
	// nickName = sp.getString("nickname", MyApp.username);
	// }
	// MyApp.nickName = nickName;
	// vCard.setNickName(nickName);
	// vCardManager.saveVCard(vCard);
	// sp.edit().putString("nickname", null).commit();
	// MyApp.username = username;
	// }

	/**
	 * 注册聊天室消息监听 必须在用户登录后注册
	 */
	private void registerMultiChatLinstener() {
		ThreadUtil.runOnBackThread(new Runnable() {

			@Override
			public void run() {
				multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
				try {
					List<HostedRoom> hostedRooms = multiUserChatManager.getHostedRooms("conference." + connection.getServiceName());
					for (HostedRoom hostedRoom : hostedRooms) {
						MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(hostedRoom.getJid());
						String roomName = multiUserChatManager.getRoomInfo(hostedRoom.getJid()).getName();
						// roomJid::gg1@conference.im2.daimaqiao.net
						// roomName::Gg1
						MyLog.showLog("roomJid::" + hostedRoom.getJid());
						MyLog.showLog("roomName::" + roomName);
						DiscussionHistory history = new DiscussionHistory();
						// 设置只接受加入本地数据库最后一条以后的消息
						history.setSince(lastDate);
						// history.setSince(new Date());
						MyLog.showLog("上一条时间:" + lastDate);
						// 加入聊天室 使用的是 用户的用户名作为聊天室内的昵称
						// SmackConfiguration.getDefaultPacketReplyTimeout()
						// 5000ms
						if (!multiUserChat.isJoined()) {
							multiUserChat.join(MyApp.username, null, history, SmackConfiguration.getDefaultPacketReplyTimeout());
							MyLog.showLog("加入房间:" + roomName);
							MyMultiChatMessageLinstener multiChatMessageListener = new MyMultiChatMessageLinstener(mIMService, roomName, notificationManager);
							multiUserChat.addMessageListener(multiChatMessageListener);
							linstenerMap.put(roomName, multiChatMessageListener);
							MyApp.linstenerMap = linstenerMap;
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
	};

	/**
	 * 广播 通知登录是否成功
	 * 
	 * @param loginState
	 */
	// private void sendLoginBroadcast(int loginState) {
	// Intent intent = new Intent();
	// intent.setAction(MyConstance.ACTION_IS_LOGIN_SUCCESS);
	// // 将登录状态发送出去
	// intent.putExtra(MyConstance.LOGIN_STATE, loginState);
	// sendBroadcast(intent);
	// }

	@Override
	public void onDestroy() {
		if (myChatManagerListener != null) {
			cm.removeChatListener(myChatManagerListener);
			MyLog.showLog("移除单人消息监听");
		}
		Set<String> keySet = linstenerMap.keySet();
		for (String roomName : keySet) {
			String roomJid = roomName.toLowerCase() + "conference." + connection.getServiceName();
			MultiUserChat multiUserChat = multiUserChatManager.getMultiUserChat(roomJid);
			multiUserChat.removeMessageListener(linstenerMap.get(roomName));
		}
		MyLog.showLog(connection.isConnected() + "");
		// 服务销毁时 断开连接
		if (connection.isConnected()) {
			connection.disconnect();
			// Presence presence = new Presence(Type.unavailable);
			// try {
			// // presence.setFrom(connection.getUser());
			// // presence.setTo(connection.getUser());
			// MyLog.showLog(connection.getUser()); // lizh@im2.daimaqiao.net/Smack
			// connection.sendStanza(presence);
			// MyLog.showLog("用户离线");
			// } catch (NotConnectedException e) {
			// e.printStackTrace();
			// }
		}
		super.onDestroy();
	}
}
