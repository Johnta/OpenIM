package com.open.im.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.open.im.receiver.MyReceiptReceivedListener;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jivesoftware.smack.roster.rosterstore.RosterStore;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * 单例模式获取connection对象
 * Created by lzh12 on 2016/5/24.
 */
public class XMPPConnection {

    private static String sendLogPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/exiu/send_log.txt";
    private static String receiveLogPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/exiu/receive_log.txt";

    private static File sendFile = new File(sendLogPath);
    private static File receiveFile = new File(receiveLogPath);

    private static String rosterVer;

    private XMPPConnection() {
    }

    private static XMPPTCPConnection connection;

    /**
     * 获取连接对象
     * @param ctx
     * @return
     */
    public static XMPPTCPConnection getInstance(Context ctx) {
        if (connection == null) {
            connection = new XMPPTCPConnection(config());

            // 获取Roster本地版本号
            SharedPreferences sp = ctx.getSharedPreferences(MyConstance.SP_NAME, 0);
            rosterVer = sp.getString(MyConstance.ROSTER_VER, "");

            // 设置手动同意好友请求
            Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);

            System.setProperty("http.keepAlive", "false");

            // 消息回执
            ProviderManager.addExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
            ProviderManager.addExtensionProvider(DeliveryReceiptRequest.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceiptRequest.Provider());
            DeliveryReceiptManager deliveryReceiptManager = DeliveryReceiptManager.getInstanceFor(connection);
            // 收到消息后 总是给回执
            deliveryReceiptManager.setAutoReceiptMode(DeliveryReceiptManager.AutoReceiptMode.always);
            // 自动添加要求回执的请求
            deliveryReceiptManager.autoAddDeliveryReceiptRequests();
            // 添加消息回执监听
            deliveryReceiptManager.addReceiptReceivedListener(new MyReceiptReceivedListener(ctx));

            // 设置使用流管理
            connection.setUseStreamManagement(true);
            connection.setUseStreamManagementResumption(true);

            // 设置不允许自动重连
            ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
            reconnectionManager.disableAutomaticReconnection();

            Roster roster = Roster.getInstanceFor(connection);
            final RosterStore rosterStore = new RosterStore() {
                @Override
                public Collection<RosterPacket.Item> getEntries() {
                    // TODO 默认返回的是null 但是会报异常 就给他返回了个空list
                    return new ArrayList<RosterPacket.Item>();
                }

                @Override
                public RosterPacket.Item getEntry(String s) {
                    return null;
                }

                @Override
                public String getRosterVersion() {
                    MyLog.showLog("rosterVer::" + rosterVer);
                    return rosterVer;
                }

                @Override
                public boolean addEntry(RosterPacket.Item item, String s) {
                    return false;
                }

                @Override
                public boolean resetEntries(Collection<RosterPacket.Item> collection, String s) {
                    return false;
                }

                @Override
                public boolean removeEntry(String s, String s1) {
                    return false;
                }
            };
            roster.setRosterStore(rosterStore);
            roster.setRosterLoadedAtLogin(false);
        }

        /**
         * 监听创建连接后 发出的数据
         */
        connection.addPacketSendingListener(new StanzaListener() {

            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                CharSequence xml = packet.toXML();
                if (!sendFile.getParentFile().exists()) {
                    sendFile.getParentFile().mkdirs();
                }
                try {
                    FileOutputStream fos = new FileOutputStream(sendFile, true);
                    fos.write((new Date()  + "==============" + xml.toString()).getBytes());
                    fos.write("\n".getBytes());
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MyLog.showLog("发出的流::" + xml.toString());
            }
        }, null);

        connection.addAsyncStanzaListener(new StanzaListener() {
            @Override
            public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
                CharSequence xml = packet.toXML();
                if (!receiveFile.getParentFile().exists()) {
                    receiveFile.getParentFile().mkdirs();
                }
                try {
                    FileOutputStream fos = new FileOutputStream(receiveFile, true);
                    fos.write((new Date() + "==============" + xml.toString()).getBytes());
                    fos.write("\n".getBytes());
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MyLog.showLog("收到的流::" + xml.toString());
            }
        }, null);
        return connection;
    }

    /**
     * 初始化配置信息
     * @return
     */
    private static XMPPTCPConnectionConfiguration config() {
        return XMPPTCPConnectionConfiguration.builder()
                .setHost(MyConstance.SERVICE_HOST)
                .setPort(5222).setServiceName(MyConstance.SERVICE_HOST)
                .setConnectTimeout(30 * 1000)
                .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                .setSendPresence(false)
                .build();
    }
}
