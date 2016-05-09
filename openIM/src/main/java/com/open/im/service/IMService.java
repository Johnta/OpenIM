package com.open.im.service;

import android.app.AlarmManager;
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
import android.os.PowerManager;
import android.os.SystemClock;

import com.open.im.app.MyApp;
import com.open.im.bean.VCardBean;
import com.open.im.db.OpenIMDao;
import com.open.im.receiver.MyAddFriendStanzaListener;
import com.open.im.receiver.MyChatMessageListener;
import com.open.im.receiver.MyReceiptStanzaListener;
import com.open.im.receiver.MyRosterStanzaListener;
import com.open.im.receiver.ScreenListener;
import com.open.im.receiver.TickAlarmReceiver;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyNetUtils;
import com.open.im.utils.MyVCardUtils;
import com.open.im.utils.ThreadUtil;
import com.open.im.utils.XMPPConnectionUtils;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.filter.StanzaFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.offline.OfflineMessageHeader;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.offline.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 应用主服务进程
 *
 * @author Administrator
 */
public class IMService extends Service {

    private static final int LOGIN_FIRST = 1000;
    private static final int LOGIN_SECOND = 2000;
    private SharedPreferences sp;
    private String username;
    private static IMService mIMService;

    private NotificationManager notificationManager;
    private XMPPTCPConnection connection;
    private ChatManagerListener myChatManagerListener;
    private ChatManager cm;

    private String password;
    private MyReceiptStanzaListener mReceiptStanzaListener;
    private MyAddFriendStanzaListener mAddFriendStanzaListener;
    private ConnectionListener mConnectionListener;
    private BroadcastReceiver mNetReceiver;
    private OpenIMDao openIMDao;

    private boolean loginState = true;
    private MyRosterStanzaListener myRosterStanzaListener;
    private PingFailedListener pingFailedListener;
    private PingManager pingManager;
    private PowerManager.WakeLock wl;
    private int locked;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 初始化服务里需要使用的对象
        initObject();

        // 初始化数据
        initData();

        // 网络状态监听
        registerNetListener();

//        //注册连接状态监听
//        registerConnectionListener();

        // 开启计时器 每5分钟唤醒一次服务
        setTickAlarm();

        // 初始化登录状态 若已登录则不做操作 若未登录 则登录
        initLoginState();
        // 注册好友名单监听
        registerRosterListener();

        // 锁屏后保持CPU运行
//        keepCPUAlive();

        MyLog.showLog("onCreate");

    }

    /**
     * 好友版本号监听 当本地版本号与服务端版本号不一致时，更新通讯录
     */
    private void registerRosterListener() {
        myRosterStanzaListener = new MyRosterStanzaListener(mIMService);
        connection.addAsyncStanzaListener(myRosterStanzaListener, null);
    }

    /**
     * 判断是否是登录状态
     * 若已登录 则不做操作
     * 若未连接 则连接并登录
     * 若conn对象为空 则初始化对象并连接登录
     */
    private void initLoginState() {
        // 判断连接是否为空 如果为空则重新登录
        if (connection == null) {
            XMPPConnectionUtils.initXMPPConnection(mIMService);
            reLogin();
        } else if (!connection.isConnected()) {
            reLogin();
        } else if (connection.isAuthenticated()) {
            handler.sendEmptyMessage(LOGIN_FIRST);
        }
    }

    /**
     * 初始化服务中需要使用的对象
     */
    private void initObject() {
        mIMService = this;
        openIMDao = OpenIMDao.getInstance(mIMService);
        sp = getSharedPreferences(MyConstance.SP_NAME, 0);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        connection = MyApp.connection;
    }

    /**
     * 初始化 获取用户名和密码
     */
    private void initData() {
        username = sp.getString("username", "");
        if (MyApp.username == null) {
            MyApp.username = username;
        }
        password = sp.getString("password", "");
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
                    loginState = false;
                }

                @Override
                public void connectionClosedOnError(Exception e) {
                    MyLog.showLog("因为错误，连接被关闭");
                    loginState = false;
                    // 移除各种监听  不包括连接状态监听
//                    removeListener();
                }

                @Override
                public void reconnectionSuccessful() {
                    MyLog.showLog("重新连接成功");
//                    if (!loginState){
//                        // 判断连接是否为空 如果为空则重新登录
//                        if (connection == null) {
//                            XMPPConnectionUtils.initXMPPConnection();
//                            reLogin();
//                        } else if (!connection.isConnected()) {
//                            reLogin();
//                        } else if (connection.isAuthenticated()) {
//                            handler.sendEmptyMessage(LOGIN_FIRST);
//                        }
//                    }
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
                        handler.sendEmptyMessage(LOGIN_SECOND);
                        MyLog.showLog("已经登录过了");
                    } else {
                        connection.login(username, password);
                        MyLog.showLog("服务中重新登录");
                        handler.sendEmptyMessage(LOGIN_FIRST);
                    }
                    MyApp.username = username;
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
        PendingIntent tickPendIntent = PendingIntent.getBroadcast(this,
                requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        //小米2s的MIUI操作系统，目前最短广播间隔为5分钟，少于5分钟的alarm会等到5分钟再触发
        long triggerAtTime = System.currentTimeMillis();
        int interval = 300 * 1000;
        alarmMgr.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, interval, tickPendIntent);
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
                        MyLog.showLog("loginState::" + loginState);
                        if (!loginState) {
                            // 重新连接网络时，判断连接状态，登录
                            initLoginState();
                        }
                    } else {
                        loginState = false;
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
     * 添加好友请求监听
     */
    private void registerAddFriendListener() {
        mAddFriendStanzaListener = new MyAddFriendStanzaListener(this, notificationManager);
        // 过滤器
        StanzaFilter packetFilter = new StanzaFilter() {

            @Override
            public boolean accept(Stanza stanza) {
                return stanza instanceof Presence;
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
        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                if (connection != null && connection.isAuthenticated()) {
                    OfflineMessageManager offlineMessageManager = new OfflineMessageManager(connection);
                    try {
                        boolean isSupport = offlineMessageManager.supportsFlexibleRetrieval();
                        MyLog.showLog("是否支持离线::" + isSupport);
                        if (isSupport) {
                            int messageCount = offlineMessageManager.getMessageCount();
                            MyLog.showLog("离线消息个数:" + messageCount);
                            ArrayList<String> nodes = new ArrayList<String>();
                            while (messageCount > 5) {
                                nodes.clear();
                                MyLog.showLog("offline_1::" + SystemClock.currentThreadTimeMillis());
                                List<OfflineMessageHeader> headers = offlineMessageManager.getHeaders();
                                for (int i = 0; i < 5; i++) {
                                    nodes.add(headers.get(i).getStamp());
                                }
                                MyLog.showLog("offline_2::" + SystemClock.currentThreadTimeMillis());
                                /**
                                 * 自定义方法 根据nodes获取服务器指定的离线消息(Smack提供的消息太耗时了)
                                 */
                                getOfflineMessageByNodes(nodes);
                                MyLog.showLog("offline_3::" + SystemClock.currentThreadTimeMillis());
                                /**
                                 * 自定义方法 根据nodes删除服务器指定离线消息
                                 */
                                deleteOfflineMessagesByNodes(nodes);
                                MyLog.showLog("offline_4::" + SystemClock.currentThreadTimeMillis());
                                messageCount = offlineMessageManager.getMessageCount();
                                MyLog.showLog("还剩::" + messageCount);
                            }
                            /**
                             * 获取离线消息
                             */
                            offlineMessageManager.getMessages();
                            /**
                             * 删除服务器端的离线消息
                             */
                            deleteOfflineMessages();
                            /**
                             * 将状态设置成在线  连接时不告诉服务器状态
                             */
                            Presence presence = new Presence(Presence.Type.available);
                            connection.sendStanza(presence);
                        }
                    } catch (SmackException.NoResponseException e) {
                        e.printStackTrace();
                    } catch (XMPPException.XMPPErrorException e) {
                        e.printStackTrace();
                    } catch (NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 自定义方法 根据nodes获取服务器指定的离线消息(Smack提供的消息太耗时了)
     *
     * @param nodes
     */
    private void getOfflineMessageByNodes(ArrayList<String> nodes) {
        OfflineMessageRequest request = new OfflineMessageRequest();
        Iterator messageFilter = nodes.iterator();
        while (messageFilter.hasNext()) {
            String messageCollector = (String) messageFilter.next();
            org.jivesoftware.smackx.offline.packet.OfflineMessageRequest.Item message = new org.jivesoftware.smackx.offline.packet.OfflineMessageRequest.Item(messageCollector);
            message.setAction("view");
            request.addItem(message);
        }
        try {
            connection.createPacketCollectorAndSend(request).nextResultOrThrow();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (NotConnectedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除服务器端所有的离线消息
     *
     * @throws SmackException.NoResponseException
     * @throws XMPPException.XMPPErrorException
     * @throws NotConnectedException
     */
    private void deleteOfflineMessages() throws SmackException.NoResponseException, XMPPException.XMPPErrorException, NotConnectedException {
        OfflineMessageRequest request = new OfflineMessageRequest();
        request.setPurge(true);
        request.setType(IQ.Type.set);
        connection.createPacketCollectorAndSend(request).nextResultOrThrow();
    }

    /**
     * 通过nodes删除服务器端指定的离线消息
     *
     * @param nodes
     * @throws SmackException.NoResponseException
     * @throws XMPPException.XMPPErrorException
     * @throws NotConnectedException
     */
    private void deleteOfflineMessagesByNodes(ArrayList<String> nodes) throws SmackException.NoResponseException, XMPPException.XMPPErrorException, NotConnectedException {
        OfflineMessageRequest request = new OfflineMessageRequest();
        Iterator iterator = nodes.iterator();
        while (iterator.hasNext()) {
            String node = (String) iterator.next();
            OfflineMessageRequest.Item item = new OfflineMessageRequest.Item(node);
            item.setAction("remove");
            request.addItem(item);
            request.setType(IQ.Type.set);
        }
        connection.createPacketCollectorAndSend(request).nextResultOrThrow();
    }


    /**
     * 方法 把自身返回 方便外部调用
     *
     * @return 服务本身对象
     */
    public static IMService getInstance() {
        return mIMService;
    }


    /**
     * 获取好友 及自己的vCard信息并存储到数据库
     */
    private void initFriendInfo() {
        if (connection != null) {
            ThreadUtil.runOnBackThread(new Runnable() {
                @Override
                public void run() {
                    // 登录后查询自己的VCard信息
                    VCardBean userVCard = MyVCardUtils.queryVCard(null);
                    MyApp.avatarUrl = userVCard.getAvatar();
                    userVCard.setJid(MyApp.username + "@" + MyConstance.SERVICE_HOST);
                    openIMDao.updateSingleVCard(userVCard);
                    // 缓存好友的VCard信息
                    Roster roster = Roster.getInstanceFor(MyApp.connection);
                    try {
                        roster.reload();
                    } catch (SmackException.NotLoggedInException e) {
                        e.printStackTrace();
                    } catch (NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 方法 每隔20秒 ping一下服务器
     */
    private void initPingConnection() {
        pingManager = PingManager.getInstanceFor(connection);
        pingManager.setPingInterval(30);
        try {
            pingManager.pingMyServer(true);
        } catch (NotConnectedException e) {
            e.printStackTrace();
        }
        /**
         * ping失败监听
         */
        pingFailedListener = new PingFailedListener() {
            @Override
            public void pingFailed() {
                MyLog.showLog("ping失败");
                loginState = false;
                if (MyNetUtils.isNetworkConnected(mIMService)) {
                    initLoginState();
                }
            }
        };
        pingManager.registerPingFailedListener(pingFailedListener);
    }

    /**
     * 注册消息接收监听
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
                case LOGIN_FIRST:
                    loginState = true;
                    //注册连接状态监听
                    registerConnectionListener();
                    // 添加好友请求监听
                    registerAddFriendListener();
                    // 消息接收监听
                    registerMessageListener();
                    // 初始化离线消息
                    initOfflineMessages();
                    // 消息回执监听
                    registerReceiptsListener();
                    // ping服务器
                    initPingConnection();
                    //获取好友 及自己的vCard信息并存储到数据库
                    initFriendInfo();
                    break;
                case LOGIN_SECOND:
                    loginState = true;
                    // 之前已经登录过了
                    // 初始化离线消息
                    initOfflineMessages();
                    break;
            }
        }
    };

    /**
     * 方法 移除各种监听
     */
    private void removeListener() {
        if (cm != null && myChatManagerListener != null) { //移除单人消息监听
            cm.removeChatListener(myChatManagerListener);
        }
        if (connection != null && mReceiptStanzaListener != null) {  //移除消息回执监听
            connection.removeAsyncStanzaListener(mReceiptStanzaListener);
        }
        if (connection != null && mAddFriendStanzaListener != null) { //移除好友申请监听
            connection.removeAsyncStanzaListener(mAddFriendStanzaListener);
        }
        if (connection != null && pingFailedListener != null && pingManager != null) {
            pingManager.unregisterPingFailedListener(pingFailedListener);
        }
    }

    @Override
    public void onDestroy() {
        //移除各种监听 不包括连接状态监听
        removeListener();
        if (connection != null && mConnectionListener != null) {  //移除连接状态监听
            connection.removeConnectionListener(mConnectionListener);
        }
        if (mNetReceiver != null) {  //移除网络状态监听
            unregisterReceiver(mNetReceiver);
        }
        if (connection != null && myRosterStanzaListener != null) {  // 移除好友监听
            connection.removeAsyncStanzaListener(myRosterStanzaListener);
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
     * 方法 在锁屏时 保持CPU运行
     */
    private void keepCPUAlive() {
        //获取电源锁，保证cpu运行
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "pw_tag");
        ScreenListener l = new ScreenListener(this);
        l.begin(new ScreenListener.ScreenStateListener() {
            @Override
            public void onUserPresent() {
            }

            @Override
            public void onScreenOn() {
                if (wl != null && locked == 1) {
                    wl.release();
                    locked = 0;
                    MyLog.showLog("亮屏");
                }
            }

            @Override
            public void onScreenOff() {
                if (wl != null && locked == 0) {
                    wl.acquire();
                    locked = 1;
                    MyLog.showLog("锁屏");
                }
            }
        });
    }
}
