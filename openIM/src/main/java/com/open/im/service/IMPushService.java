package com.open.im.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import com.open.im.R;
import com.open.im.activity.MainActivity;
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
 * Created by lzh12 on 2016/6/7.
 */
public class IMPushService extends Service {

    private ConnectionFactory factory;
    private static final String EXCHANGE_NAME = "lzh";
    private PowerManager.WakeLock wakeLock;
    private NotificationManager notificationManager;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.showLog("IMPushService---onCreate");
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        setupConnectionFactory();
        subscribePush();
    }

    /**
     * 初始化连接工厂类
     */
    private void setupConnectionFactory() {
        factory = new ConnectionFactory();
        String uri = "amqp://push.openim.top";
        try {
            factory.setAutomaticRecoveryEnabled(false);
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
                    channel.exchangeDeclare(EXCHANGE_NAME, "direct");
                    String queueName = channel.queueDeclare().getQueue();
                    channel.queueBind(queueName, EXCHANGE_NAME, "chat");
                    Consumer consumer = new DefaultConsumer(channel) {
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope,
                                                   AMQP.BasicProperties properties, byte[] body) throws IOException {
                            String message = new String(body, "UTF-8");
                            newMsgNotify(message);
                        }
                    };
                    channel.basicConsume(queueName, true, consumer);
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
}
