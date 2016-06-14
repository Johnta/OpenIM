package com.open.im.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.open.im.IMPushServiceAIDL;
import com.open.im.R;
import com.open.im.activity.MainActivity;
import com.open.im.receiver.ScreenListener;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyLog;
import com.open.im.utils.ThreadUtil;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeoutException;

/**
 * 接收来自RabbitMQ的推送
 * 目前是持久化模式 不在线的话 推送会在再次上线时推送过去 但是 多用户同时在线，只有一个用户可以收到推送，并且是轮流收到
 * <p/>
 * 交换机持久化 队列持久化 消息持久化
 * Created by lzh12 on 2016/6/7.
 */
public class IMPushService extends Service {

    private static IMPushService mIMPushService;
    private ConnectionFactory factory;
    private static final String DURABLE_EXCHANGE_NAME = "durable_3";
    // TODO 这个参数应该是唯一的 跟 username有关
    private String DURABLE_QUEUE_NAME;
    private static final boolean durable = true; //消息队列持久化
    private PowerManager.WakeLock wakeLock;
    private NotificationManager notificationManager;
    private int locked;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    private class MyBinder extends IMPushServiceAIDL.Stub{

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void stopIMPushService() throws RemoteException {
            stopSelf();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mIMPushService = this;
        MyLog.showLog("IMPushService---onCreate");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        SharedPreferences sp = getSharedPreferences(MyConstance.SP_NAME, 0);
        String username = sp.getString("username", "");
        DURABLE_QUEUE_NAME = username + "#OpenIM";
        setupConnectionFactory();
        subscribePush();

//        keepCPUAlive();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public static IMPushService getInstance(){
        return mIMPushService;
    }

    /**
     * 初始化连接工厂类
     */
    private void setupConnectionFactory() {
        factory = new ConnectionFactory();
        String uri = "amqp://push.openim.top";
        try {
            factory.setAutomaticRecoveryEnabled(true);
            factory.setUri(uri);
        } catch (KeyManagementException | NoSuchAlgorithmException | URISyntaxException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * 订阅
     */
    private void subscribePush() {
        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Connection connection = factory.newConnection();
                    Channel channel = connection.createChannel();
                    channel.exchangeDeclare(DURABLE_EXCHANGE_NAME, "fanout", durable);

                    MyLog.showLog(DURABLE_QUEUE_NAME + "============");
                    channel.queueDeclare(DURABLE_QUEUE_NAME, durable, false, false, null);
                    channel.queueBind(DURABLE_QUEUE_NAME, DURABLE_EXCHANGE_NAME, "");
                    Consumer consumer = new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope,
                                                   AMQP.BasicProperties properties, byte[] body) throws IOException {
                            String message = new String(body, "UTF-8");
                            newMsgNotify(message);
                            MyLog.showLog("收到RabbitMQ推送::" + message);
                        }
                    };
                    channel.basicConsume(DURABLE_QUEUE_NAME, true, consumer);
                } catch (IOException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 新消息通知
     */
    private void newMsgNotify(String messageBody) {
        CharSequence tickerText = "RabbitMQ新通知！";
        // 收到单人消息时，亮屏3秒钟
        acquireWakeLock();
        Notification notification = new Notification(R.mipmap.ic_launcher, tickerText, System.currentTimeMillis());
        // 设置默认声音
        notification.defaults = Notification.DEFAULT_SOUND;
        // 设定震动(需加VIBRATE权限)
        notification.defaults = Notification.DEFAULT_VIBRATE;
        // 点击通知后 通知栏消失
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        Intent intent = new Intent(this, MainActivity.class);
        // 必须添加
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 77, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setLatestEventInfo(this, "RabbitMQ", messageBody, contentIntent);
        notificationManager.notify(5555, notification);
    }

    /**
     * 方法 点亮屏幕3秒钟 要加权限 <uses-permission
     * android:name="android.permission.WAKE_LOCK"></uses-permission>
     */
    private void acquireWakeLock() {
        if (wakeLock == null) {
            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            wakeLock = powerManager.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "lzh");
        }
        wakeLock.acquire(1000);
    }

    /**
     * 方法 在锁屏时 保持CPU运行
     */
    private void keepCPUAlive() {
        //获取电源锁，保证cpu运行
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "pw_tag");
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

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
