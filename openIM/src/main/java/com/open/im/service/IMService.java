package com.open.im.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.IBinder;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.bean.VCardBean;
import com.open.im.db.ChatDao;
import com.open.im.receiver.MyAddFriendStanzaListener;
import com.open.im.receiver.MyChatMessageListener;
import com.open.im.receiver.MyReceiptStanzaListener;
import com.open.im.receiver.TickAlarmReceiver;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyNetUtils;
import com.open.im.utils.MyVCardUtils;
import com.open.im.utils.ThreadUtil;
import com.open.im.utils.XMPPConnectionUtils;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.ping.PingManager;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 应用主服务进程
 *
 * @author Administrator
 */
public class IMService extends Service {

    private static final int LOGIN_SUCCESS = 1000;
    private SharedPreferences sp;
    private String username;
    private static IMService mIMService;

    private NotificationManager notificationManager;
    private XMPPTCPConnection connection;
    private ChatManagerListener myChatManagerListener;
    private ChatManager cm;

    private PendingIntent tickPendIntent;
    private String password;
    private MyReceiptStanzaListener mReceiptStanzaListener;
    private MyAddFriendStanzaListener mAddFriendStanzaListener;
    private ConnectionListener mConnectionListener;
    private BroadcastReceiver mNetReceiver;
    private ChatDao chatDao;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setTickAlarm();
//        startForegroundService();
        // 初始化
        init();
        connection = MyApp.connection;
        // 判断连接是否为空 如果为空则重新登录
        if (connection == null) {
            XMPPConnectionUtils.initXMPPConnection();
            reLogin();
        } else if (!connection.isConnected()) {
            reLogin();
        } else if (connection.isAuthenticated()) {
            handler.sendEmptyMessage(LOGIN_SUCCESS);
        }
    }

    /**
     * 注册连接状态监听
     */
    private void registerConnectionListener() {
        if (mConnectionListener == null && connection != null) {  //只添加一个连接状态监听
            mConnectionListener = new ConnectionListener() {
                @Override
                public void connected(XMPPConnection connection) {
                    MyLog.showLog("-------连接成功--------");
                }

                @Override
                public void authenticated(XMPPConnection connection, boolean resumed) {
                    MyLog.showLog("-------登录成功--------");
                }

                @Override
                public void connectionClosed() {
                    MyLog.showLog("连接被关闭");
                }

                @Override
                public void connectionClosedOnError(Exception e) {
                    MyLog.showLog("因为错误，连接被关闭");
                    // 移除各种监听  不包括连接状态监听
                    removeListener();
                }

                @Override
                public void reconnectionSuccessful() {
                    MyLog.showLog("重新连接成功");
                    if (connection == null) {
                        XMPPConnectionUtils.initXMPPConnection();
                    }
                    reLogin();
                }

                @Override
                public void reconnectingIn(int seconds) {
                    MyLog.showLog("正在重新连接");
                }

                @Override
                public void reconnectionFailed(Exception e) {
                    MyLog.showLog("重新连接失败");
                }
            };
            connection.addConnectionListener(mConnectionListener);
        }
    }

    /**
     * 方法  判断连接是否为空 为空则重新登录
     */
    private void reLogin() {
        connection = MyApp.connection;
        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (!connection.isConnected()) {
                        connection.connect();
                    }
                    connection.setPacketReplyTimeout(60 * 1000);
                    if (connection.isAuthenticated()) {  //当应用断网时，connection不为null 并且这个conn已经登录过了
                        MyLog.showLog("已经登录过了");
                    } else {
//                        Presence presence = new Presence(Presence.Type.unavailable);
//                        connection.disconnect(presence);
                        connection.login(username, password);
                        MyLog.showLog("服务中重新登录");
                    }
                    MyApp.username = username;
                    handler.sendEmptyMessage(LOGIN_SUCCESS);
                } catch (SmackException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XMPPException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 开个计时器类似的  唤醒服务
     */
    protected void setTickAlarm() {
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, TickAlarmReceiver.class);
        int requestCode = 0;
        tickPendIntent = PendingIntent.getBroadcast(this,
                requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //小米2s的MIUI操作系统，目前最短广播间隔为5分钟，少于5分钟的alarm会等到5分钟再触发
        long triggerAtTime = System.currentTimeMillis();
        int interval = 300 * 1000;
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, interval, tickPendIntent);
    }

    protected void cancelTickAlarm() {
        AlarmManager alarmMgr = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmMgr.cancel(tickPendIntent);
    }

    /**
     * 方法 监听消息回执
     */
    private void registerReceiptsListener() {
        if (connection != null && connection.isAuthenticated()) {
            mReceiptStanzaListener = new MyReceiptStanzaListener(mIMService);
            connection.addAsyncStanzaListener(mReceiptStanzaListener, null);
        }
    }

    /**
     * 注册网络状态监听
     */
    private void registerNetListener() {
        /**
         * 注册网络连接监听
         */
        mNetReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                    boolean isConnected = MyNetUtils.isNetworkConnected(context);
                    if (isConnected) {
                        MyLog.showLog("连接网络");
                    } else {
                        MyLog.showLog("断开网络");
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetReceiver, filter);
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
        mAddFriendStanzaListener = new MyAddFriendStanzaListener(this, notificationManager);
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
        if (connection != null && connection.isAuthenticated()) {
            // 添加好友请求监听
            connection.addAsyncStanzaListener(mAddFriendStanzaListener, packetFilter);
        }
    }

    /**
     * 获取离线消息管理者 必须在用户登录之后才可以执行
     * 服务器现在不支持离线消息
     */
    private void initOfflineMessages() {
        if (connection != null && connection.isAuthenticated()) {
            OfflineMessageManager offlineMessageManager = new OfflineMessageManager(connection);
            try {
                boolean isSupport = offlineMessageManager.supportsFlexibleRetrieval();
                MyLog.showLog("是否支持离线::" + isSupport);
                if (isSupport) {
                    MyLog.showLog(offlineMessageManager.getMessageCount() + "------");
                    List<Message> offlineMessages = offlineMessageManager.getMessages();
                    MyLog.showLog(offlineMessages.size() + "=======");
                    for (Message message : offlineMessages) {
                        MyLog.showLog(message.getFrom() + ":" + message.getBody());
                    }
                    offlineMessageManager.deleteMessages();
                }
            } catch (NoResponseException e) {
                e.printStackTrace();
            } catch (XMPPErrorException e) {
                e.printStackTrace();
            } catch (NotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化 获取用户名和密码
     */
    private void init() {
        mIMService = this;
        sp = getSharedPreferences(MyConstance.SP_NAME, 0);
        username = sp.getString("username", "");
        if (MyApp.username == null) {
            MyApp.username = username;
        }
        password = sp.getString("password", "");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        chatDao = ChatDao.getInstance(mIMService);
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
        if (connection != null && connection.isAuthenticated()) {
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
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOGIN_SUCCESS:
                    //注册连接状态监听
                    registerConnectionListener();
                    // 添加好友请求监听
                    registerAddFriendListener();
                    // 消息接收监听
                    registerMessageListener();
                    // 初始化离线消息
                    initOfflineMessages();
                    // 网络状态监听
                    registerNetListener();
                    // 消息回执监听
                    registerReceiptsListener();
                    // ping服务器
//                    initPingConnection();
                    //获取好友 及自己的vCard信息并存储到数据库
                    initFriendInfo();

//                    testBase64();
                    break;
            }
        }
    };

//    private void testBase64() {
//        String str = "{namespace: 'http://oim.daimaqiao.net/openim_1.0',version: '1.0.0',type: 'text',size: 1024,properties: {longitude: 1.00000000000, latitude: 1.00000000000, accuracy: 1.00000000000,manner: 'baidu', address: '河南', description: '我',resolution: 'ab', thumbnail: 'cd',length: 1}}";
//        ReceiveBean receiveBean = new ReceiveBean();
//        receiveBean = (ReceiveBean) receiveBean.fromJson(str);
//        MyLog.showLog("receiveBean::" + receiveBean);
//    }

    /**
     * 获取好友 及自己的vCard信息并存储到数据库
     */
    private void initFriendInfo() {
        if (connection != null) {
            ThreadUtil.runOnBackThread(new Runnable() {
                @Override
                public void run() {
                    // 登录后查询自己的VCard信息
                    VCardBean userVCard = MyVCardUtils.queryVcard(null);
                    userVCard.setJid(MyApp.username + "@" +MyConstance.SERVICE_HOST);
                    chatDao.replaceVCard(userVCard);
                    // 缓存好友的VCard信息
                    Roster roster = Roster.getInstanceFor(MyApp.connection);
                    Set<RosterEntry> users = roster.getEntries();
                    if (users != null) {
                        // 遍历获得所有组内所有好友的名称
                        for (RosterEntry rosterEntry : users) {
                            String jid = rosterEntry.getUser();
                            VCardBean vCardBean = MyVCardUtils.queryVcard(jid);
                            vCardBean.setJid(jid);
                            chatDao.replaceVCard(vCardBean);
                        }
                    }
                }
            });
        }
    }

    /**
     * 方法 每隔20秒 ping一下服务器
     */
    private void initPingConnection() {
        PingManager pingManager = PingManager.getInstanceFor(connection);
        pingManager.setPingInterval(30);
        try {
            pingManager.pingMyServer(true);
        } catch (NotConnectedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        //移除各种监听 不包括连接状态监听
        removeListener();
        if (connection != null && mConnectionListener != null) {  //移除连接状态监听
            connection.removeConnectionListener(mConnectionListener);
        }
        // 服务销毁时 断开连接
        if (connection != null && connection.isConnected()) {
            Presence presence = new Presence(Presence.Type.unavailable);
            try {
                connection.sendStanza(presence);
                connection.disconnect(presence);
            } catch (NotConnectedException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
        MyLog.showLog("服务被销毁");
    }

    /**
     * 方法 移除各种监听
     */
    private void removeListener() {
        if (cm != null && myChatManagerListener != null) { //移除单人消息监听
            cm.removeChatListener(myChatManagerListener);
        }
        if (mNetReceiver != null) {  //移除网络状态监听
            unregisterReceiver(mNetReceiver);
        }
        if (connection != null && mReceiptStanzaListener != null) {  //移除消息回执监听
            connection.removeAsyncStanzaListener(mReceiptStanzaListener);
        }
        if (connection != null && mAddFriendStanzaListener != null) { //移除好友申请监听
            connection.removeAsyncStanzaListener(mAddFriendStanzaListener);
        }
    }
}
