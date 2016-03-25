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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Looper;
import android.os.PowerManager;
import android.view.WindowManager;

import com.open.im.R;
import com.open.im.activity.ChatActivity;
import com.open.im.activity.SubscribeActivity;
import com.open.im.app.MyApp;
import com.open.im.bean.SubBean;
import com.open.im.db.ChatDao;
import com.open.im.service.IMService;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyLog;

import java.util.Date;

/**
 * 监听收到好友请求 非四大组件弹窗
 *
 * @author Administrator
 */
public class MyAddFriendStanzaListener implements StanzaListener {

    private final NotificationManager notificationManager;
    private IMService imService;
    private AbstractXMPPConnection connection;
    private final ChatDao chatDao;
    private PowerManager.WakeLock wakeLock;
    private PowerManager powerManager;

    public MyAddFriendStanzaListener(IMService imService, NotificationManager notificationManager) {
        this.imService = imService;
        connection = MyApp.connection;
        this.notificationManager = notificationManager;
        chatDao = ChatDao.getInstance(imService);
    }

    @Override
    /**
     *
     */
    public void processPacket(Stanza packet) throws NotConnectedException {
        MyLog.showLog("好友监听");
        if (packet instanceof Presence) {
            Presence presence = (Presence) packet;
            final String msgFrom = presence.getFrom();
            String msgTo = presence.getTo();
            Type type = presence.getType();
            MyLog.showLog("---" + type.name());
            if (type.equals(Presence.Type.subscribe)) { // 收到添加好友申请
                MyLog.showLog("收到好友邀请:" + msgFrom);
                Roster roster = Roster.getInstanceFor(connection);
                boolean isContains = roster.contains(msgFrom);
                MyLog.showLog("是否包含该好友::" + isContains);
                if (isContains) {
                    return;
                }

                SubBean subBean = new SubBean();
                String from = msgFrom.substring(0, msgFrom.indexOf("@"));
                subBean.setFrom(msgFrom);
                String to = msgTo.substring(0, msgTo.indexOf("@"));
                subBean.setTo(to);
                subBean.setMsg(presence.getStatus());
                subBean.setDate(new Date().getTime());
                subBean.setState("0");
                chatDao.insertSub(subBean);
                newMsgNotify(subBean.getMsg(),from);
//                MyLog.showLog("插入Bean:" + subBean);

//                AlertDialog.Builder builder = new AlertDialog.Builder(imService);
//                builder.setMessage(msgFrom + "请求添加您为好友！");
//                builder.setTitle("提示");
//                builder.setPositiveButton("同意", new OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        try {
//                            Presence response = new Presence(Type.subscribed);
//                            response.setTo(msgFrom);
//                            connection.sendStanza(response);
//                        } catch (NotConnectedException e) {
//                            e.printStackTrace();
//                        }
////                        MyLog.showLog("同意");
//                    }
//                });
//                builder.setNegativeButton("拒绝", new OnClickListener() {
//
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        try {
//                            Presence response = new Presence(Type.unsubscribed);
//                            response.setTo(msgFrom);
//                            connection.sendStanza(response);
//                        } catch (NotConnectedException e) {
//                            e.printStackTrace();
//                        }
//                        MyLog.showLog("拒绝");
//                    }
//                });
//                MyLog.showLog("到这儿没");
//                // create之前必须加上Looper.prepare();不然会报错 Can't create handler
//                // inside thread that has not called Looper.prepare()
//                // 必须加上这两句 1
//                Looper.prepare();
//                // 下面这两句将弹窗变成系统alert 要加权限android.permission.SYSTEM_ALERT_WINDOW
//                AlertDialog dialog = builder.create();
//                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//                dialog.show();
//                MyLog.showLog("到这儿没2");
//                // 必须加上这两句 2 show之后必须加上Looper.loop();，不然不显示
//                Looper.loop();
            } else if (type.equals(Type.subscribed)) {
                Roster roster = Roster.getInstanceFor(connection);
                try {
                    //如果对方同意了好友请求，则创建好友，并且回复对方同意添加对方为好友
                    roster.createEntry(msgFrom, msgFrom.substring(0, msgFrom.indexOf("@")), null);
                    Presence response = new Presence(Type.subscribed);
                    response.setTo(msgFrom);
                    connection.sendStanza(response);
                } catch (NotLoggedInException e) {
                    e.printStackTrace();
                } catch (NoResponseException e) {
                    e.printStackTrace();
                } catch (XMPPErrorException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 新消息通知
     *
     * @param messageBody
     * @param friendName
     */
    private void newMsgNotify(String messageBody, String friendName) {
        CharSequence tickerText = "有人添加您为好友！";
        // 收到单人消息时，亮屏3秒钟
        acquireWakeLock();
        Notification notification = new Notification(R.mipmap.ic_launcher, tickerText, System.currentTimeMillis());
        // 设置默认声音
        notification.defaults = Notification.DEFAULT_SOUND;
        // 设定震动(需加VIBRATE权限)
        notification.defaults = Notification.DEFAULT_VIBRATE;
        // 点击通知后 通知栏消失
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        Intent intent = new Intent(imService, SubscribeActivity.class);
        // 必须添加
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(imService, 88, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(imService, friendName, messageBody, contentIntent);
        notificationManager.notify(MyConstance.NOTIFY_ID, notification);
    }

    /**
     * 方法 点亮屏幕3秒钟 要加权限 <uses-permission
     * android:name="android.permission.WAKE_LOCK"></uses-permission>
     */
    private void acquireWakeLock() {
        if (wakeLock == null) {
            powerManager = (PowerManager) imService.getSystemService(Context.POWER_SERVICE);
            // wakeLock = powerManager.newWakeLock(PowerManager., tag)
            wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "lzh");
        }
        wakeLock.acquire(3000);
    }
}

