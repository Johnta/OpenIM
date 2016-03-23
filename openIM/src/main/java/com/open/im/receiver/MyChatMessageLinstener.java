package com.open.im.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.text.TextUtils;

import com.open.im.R;
import com.open.im.activity.ChatActivity;
import com.open.im.bean.BaseBean;
import com.open.im.bean.MessageBean;
import com.open.im.db.ChatDao;
import com.open.im.service.IMService;
import com.open.im.utils.MyConstance;

import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;

import java.util.Date;

/**
 * 自定义的会话消息接收监听
 *
 * @author Administrator
 */
public class MyChatMessageLinstener implements ChatMessageListener {

    private IMService ctx;
    private NotificationManager notificationManager;
    private ChatDao chatDao;
    private SharedPreferences sp;
    private PowerManager.WakeLock wakeLock;
    private PowerManager powerManager;

    public MyChatMessageLinstener(IMService ctx, NotificationManager notificationManager) {
        this.ctx = ctx;
        this.notificationManager = notificationManager;
        chatDao = ChatDao.getInstance(ctx);
        sp = ctx.getSharedPreferences(MyConstance.SP_NAME, 0);
    }

    @Override
    public void processMessage(Chat chat, Message message) {
        String messageBody = message.getBody();
        long msgDate = new Date().getTime();
        if (TextUtils.isEmpty(messageBody)) {
            return;
        }
        BaseBean baseBean = new BaseBean();
        MessageBean msg = new MessageBean();
        int msgType = 0;
        String msgImg = "";
        String msgBody = "";

        try {
            baseBean = (BaseBean) baseBean.fromJson(messageBody);
            String type = baseBean.getType();
            if (type.equals("image")) {
                msgType = 1;
                msgBody = baseBean.getUri();
                msgImg = baseBean.getThumbnail();
            } else if (type.equals("audio")) {
                msgType = 2;
                msgBody = baseBean.getUri();
                msgImg = "";
            } else if (type.equals("location")) {
                msgType = 3;
                msgBody = "location#" + baseBean.getLatitude() + "#" + baseBean.getLongitude() + "#" + baseBean.getDescription() + "#" + baseBean.getManner() + "#" + baseBean.getUri();
                msgImg = "";
            }
        } catch (Exception e) {
            msgType = 0;
            msgBody = messageBody;
            msgImg = "";
        }


        String friendName = message.getFrom().substring(0, message.getFrom().indexOf("@"));
        String username = sp.getString("username", null);
        msg.setFromUser(friendName);
        msg.setMsgStanzaId(message.getStanzaId());
        msg.setToUser(message.getTo().substring(0, message.getTo().indexOf("@")));
        msg.setMsgBody(msgBody);
        msg.setMsgDateLong(msgDate);
        msg.setIsReaded("0"); // 0表示未读 1表示已读
        msg.setType(msgType);
        msg.setMsgImg(msgImg);
        msg.setMsgMark(friendName + "#" + username); // 存个标记 标记是跟谁聊天
        msg.setMsgOwner(username);
        msg.setMsgReceipt("0");  //收到消息

        chatDao.insertMsg(msg);
        newMsgNotify(msg.getMsgBody(), msg.getFromUser());

    }

    /**
     * 新消息通知
     *
     * @param messageBody
     * @param friendName
     */
    private void newMsgNotify(String messageBody, String friendName) {
        CharSequence tickerText = "您有新消息，请注意查收！";
        // 收到单人消息时，亮屏3秒钟
        acquireWakeLock();
        Notification notification = new Notification(R.mipmap.ic_launcher, tickerText, System.currentTimeMillis());
        // 设置默认声音
        notification.defaults = Notification.DEFAULT_SOUND;
        // 设定震动(需加VIBRATE权限)
        notification.defaults = Notification.DEFAULT_VIBRATE;
        // 点击通知后 通知栏消失
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        Intent intent = new Intent(ctx, ChatActivity.class);
        intent.putExtra("friendName", friendName);
        // 必须添加
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(ctx, 99, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(ctx, friendName, messageBody, contentIntent);
        notificationManager.notify(MyConstance.NOTIFY_ID, notification);
    }

    /**
     * 方法 点亮屏幕3秒钟 要加权限 <uses-permission
     * android:name="android.permission.WAKE_LOCK"></uses-permission>
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
