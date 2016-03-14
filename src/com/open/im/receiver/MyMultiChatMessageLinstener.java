package com.open.im.receiver;

import java.util.Date;

import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.text.TextUtils;

import com.open.im.R;
import com.open.im.activity.MultiChatActivity;
import com.open.im.app.MyApp;
import com.open.im.bean.MessageBean;
import com.open.im.db.MultiChatDao;
import com.open.im.utils.MyConstance;

/**
 * 聊天室消息监听
 * 
 * @author Administrator
 * 
 */
public class MyMultiChatMessageLinstener implements MessageListener {

	private Context ctx;
	private MultiChatDao chatDao;
	private String roomName;
	private NotificationManager notificationManager;
	private PowerManager.WakeLock wakeLock;
	private PowerManager powerManager;

	public MyMultiChatMessageLinstener(Context ctx, String roomName, NotificationManager notificationManager) {
		this.ctx = ctx;
		this.roomName = roomName;
		this.notificationManager = notificationManager;
		chatDao = MultiChatDao.getInstance(ctx);
	}

	@Override
	public void processMessage(Message message) {
		String messageBody = message.getBody();
		if (TextUtils.isEmpty(messageBody)) {
			return;
		}
		String stanzaId = message.getStanzaId();
		String lastMsgStanzaId = chatDao.queryTheLastMsgStanzaId();
		if (stanzaId.equals(lastMsgStanzaId)) {
			return;
		}
		// 封装
		MessageBean msg = new MessageBean();
		String msgFrom = message.getFrom().substring(message.getFrom().indexOf("/") + 1); // 显示的是昵称
		msg.setMsgStanzaId(stanzaId);
		msg.setFromUser(msgFrom);
		msg.setMsgBody(messageBody);
		msg.setType(0);
		msg.setMsgDateLong(new Date().getTime());
		msg.setMsgMark(MyApp.username + "#" + roomName); // 存个标记 标记是谁在那个聊天室
		// 存储到数据库
		chatDao.insertMsg(msg);
		if (!msgFrom.equals(MyApp.username)) { // 如果是发出的消息 就 不提示
			String roomJid = roomName + "@conference." + MyApp.connection.getServiceName();
			if (notificationManager != null) { // 如果没有通知栏对象 也不提示
				newMsgNotify(messageBody, roomJid);
			}
		}
	}

	/**
	 * 新消息通知
	 * 
	 * @param messageBody
	 * @param roomName
	 */
	private void newMsgNotify(String messageBody, String roomJid) {
		CharSequence tickerText = "您有新消息，请注意查收！";
//		acquireWakeLock();
		Notification notification = new Notification(R.drawable.ic_launcher, tickerText, System.currentTimeMillis());
		// 设置默认声音
		notification.defaults = Notification.DEFAULT_SOUND;
		// 设定震动(需加VIBRATE权限)
		notification.defaults = Notification.DEFAULT_VIBRATE;
		// 点击通知后 通知栏消失
		notification.flags = Notification.FLAG_AUTO_CANCEL;

		Intent intent = new Intent(ctx, MultiChatActivity.class);
		intent.putExtra("roomJid", roomJid);
		// 必须添加
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(ctx, 99, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(ctx, roomName, messageBody, contentIntent);
		notificationManager.notify(MyConstance.NOTIFY_ID, notification);
	}

	/**
	 * 方法 屏幕保持唤醒状态 要加权限 android.permission.WAKE_LOCK 摄像头 存储卡都要权限
	 */
	private void acquireWakeLock() {
		if (wakeLock == null) {
			powerManager = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
			// wakeLock = powerManager.newWakeLock(PowerManager., tag)
			wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "lzh");
		}
		wakeLock.acquire(3000);
	}
}
