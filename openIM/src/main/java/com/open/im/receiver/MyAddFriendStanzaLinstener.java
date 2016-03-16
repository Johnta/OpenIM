package com.open.im.receiver;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Looper;
import android.view.WindowManager;

import com.open.im.app.MyApp;
import com.open.im.service.IMService;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyPubSubUtils;

/**
 * 监听收到好友请求 非四大组件弹窗
 * 
 * @author Administrator
 * 
 */
public class MyAddFriendStanzaLinstener implements StanzaListener {

	private IMService imService;
	private AbstractXMPPConnection connection;

	public MyAddFriendStanzaLinstener(IMService imService) {
		this.imService = imService;
	}

	@Override
	/**
	 * 
	 */
	public void processPacket(Stanza packet) throws NotConnectedException {
		if (packet instanceof Presence) {
			Presence presence = (Presence) packet;
			final String from = presence.getFrom();
			// String to = presence.getTo();
			Type type = presence.getType();
			MyLog.showLog("---" + type.name());
			if (type.equals(Presence.Type.subscribe)) { // 收到添加好友申请
				MyLog.showLog("收到好友邀请:" + from);
				AlertDialog.Builder builder = new AlertDialog.Builder(imService);
				builder.setMessage(from + "请求添加您为好友！");
				builder.setTitle("提示");
				connection = MyApp.connection;
				builder.setPositiveButton("同意", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							Presence response = new Presence(Type.subscribed);
							response.setTo(from);
							connection.sendStanza(response);
							// Presence subscription = new
							// Presence(Presence.Type.subscribe);
							// subscription.setTo(from);
							// connection.sendStanza(subscription);
							Roster roster = Roster.getInstanceFor(connection);
							boolean isContains = roster.contains(from);
							if (!isContains) {
								roster.createEntry(from, from.substring(0, from.indexOf("@")), new String[] { "我的好友" });
//								MyPubSubUtils.subscribeFriend(from);
							}

						} catch (NotConnectedException e) {
							e.printStackTrace();
						} catch (NotLoggedInException e) {
							e.printStackTrace();
						} catch (NoResponseException e) {
							e.printStackTrace();
						} catch (XMPPErrorException e) {
							e.printStackTrace();
						}
						MyLog.showLog("同意");
					}
				});
				builder.setNegativeButton("拒绝", new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						try {
							Presence response = new Presence(Type.unsubscribed);
							response.setTo(from);
							connection.sendStanza(response);
						} catch (NotConnectedException e) {
							e.printStackTrace();
						}
						MyLog.showLog("拒绝");
					}
				});
				// create之前必须加上Looper.prepare();不然会报错 Can't create handler
				// inside thread that has not called Looper.prepare()
				// 必须加上这两句 1
				Looper.prepare();
				// 下面这两句将弹窗变成系统alert 要加权限android.permission.SYSTEM_ALERT_WINDOW
				AlertDialog dialog = builder.create();
				dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
				dialog.show();
				// 必须加上这两句 2 show之后必须加上Looper.loop();，不然不显示
				Looper.loop();
				MyLog.showLog("弹出窗体");
			}
		}
	}
}

// else if (type.equals(Presence.Type.subscribed)) { // 同意添加好友
// MyLog.showLog(from + "同意添加好友");
// } else if (type.equals(Presence.Type.unsubscribe)) { // 拒绝添加好友
// MyLog.showLog(from + "拒绝添加好友");
// } else if (type.equals(Presence.Type.available)) { // 好友上线
// MyLog.showLog(from + "上线了");
// } else if (type.equals(Presence.Type.unavailable)) { //用户下线
// MyLog.showLog(from + "下线了");
// } else {
//
// }