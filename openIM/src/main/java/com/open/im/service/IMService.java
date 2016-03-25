package com.open.im.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.IBinder;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.receiver.MyAddFriendStanzaListener;
import com.open.im.receiver.MyChatMessageListener;
import com.open.im.receiver.MyNetReceiver;
import com.open.im.receiver.MyReceiptStanzaListener;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyLog;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.offline.OfflineMessageManager;

import java.util.List;

/**
 * 应用主服务进程
 *
 * @author Administrator
 */
public class IMService extends Service {

    private SharedPreferences sp;
    private String username;
    private static IMService mIMService;

    private NotificationManager notificationManager;
    private AbstractXMPPConnection connection;
    private ChatManagerListener myChatManagerListener;
    private ChatManager cm;
    private MyNetReceiver mMyNetReceiver = new MyNetReceiver();
    private IntentFilter mNetFilter = new IntentFilter();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        connection = MyApp.connection;

        // 添加好友请求监听
        registerAddFriendListener();

        // 消息接收监听
        registerMessageListener();

        // 离线消息监听
        initOfflineMessages();

        // 网络状态监听
        registerNetListener();

        // 消息回执监听
        registerReceiptsListener();
    }

    /**
     * 方法 监听消息回执
     */
    private void registerReceiptsListener() {
        connection.addAsyncStanzaListener(new MyReceiptStanzaListener(mIMService), null);
    }

    /**
     * 注册网络状态监听
     */
    private void registerNetListener() {
        mNetFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mMyNetReceiver, mNetFilter);
    }

    @Override
    /**
     * START_STICKY  提高服务的优先级的 但是貌似效果不明显
     * 在运行onStartCommand后service进程被kill后，那将保留在开始状态，但是不保留那些传入的intent。
     * 不久后service就会再次尝试重新创建，因为保留在开始状态，在创建service后将保证调用onstartCommand。
     * 如果没有传递任何开始命令给service，那将获取到null的intent。
     */
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
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
     * 添加好友请求监听
     */
    private void registerAddFriendListener() {
        MyAddFriendStanzaListener myAddFriendStanzaListener = new MyAddFriendStanzaListener(this,notificationManager);
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
        if (connection.isAuthenticated()) {
            // 添加好友请求监听
            connection.addAsyncStanzaListener(myAddFriendStanzaListener, packetFilter);
        }
    }

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
                chat.addMessageListener(new MyChatMessageListener(mIMService, notificationManager));
            }
        };
        cm.addChatListener(myChatManagerListener);
    }

    @Override
    public void onDestroy() {
        if (myChatManagerListener != null) { //移除单人消息监听
            cm.removeChatListener(myChatManagerListener);
        }
        if (mMyNetReceiver != null) {  //移除网络状态监听
            unregisterReceiver(mMyNetReceiver);
        }
        // 服务销毁时 断开连接
        if (connection.isConnected()) {
            Presence presence = new Presence(Presence.Type.unavailable);
            try {
                connection.sendStanza(presence);
                connection.disconnect(presence);
                MyLog.showLog("断开连接");
            } catch (NotConnectedException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
        MyLog.showLog("服务被销毁");
    }
}
