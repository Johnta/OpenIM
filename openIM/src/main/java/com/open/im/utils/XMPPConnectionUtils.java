package com.open.im.utils;

import com.open.im.app.MyApp;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.Roster.SubscriptionMode;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jivesoftware.smack.roster.rosterstore.RosterStore;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

import java.util.ArrayList;
import java.util.Collection;

public class XMPPConnectionUtils {

    /**
     * 初始化连接
     */
    public static void initXMPPConnection() {

        XMPPTCPConnectionConfiguration.Builder configBuilder = XMPPTCPConnectionConfiguration.builder();

        // //TODO 下面这几段 是加密相关 还没弄通
        // SASLMechanism mechanism = new SASLDigestMD5Mechanism();
        // SASLAuthentication.registerSASLMechanism(mechanism);
        // SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
        // SASLAuthentication.unBlacklistSASLMechanism("DIGEST-MD5");
        //
        // SASLAuthentication.registerSASLMechanism(new SASLPlainMechanism());
        // SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
        //
        // SSLContext context;
        // try {
        // context = SSLContext.getInstance("TLS");
        // configBuilder.setCustomSSLContext(context);
        // // configBuilder.setEnabledSSLCiphers(new String[]{});
        // // configBuilder.setEnabledSSLProtocols(new String[]{});
        // configBuilder.setSecurityMode(SecurityMode.disabled);
        // configBuilder.setDebuggerEnabled(true);
        // configBuilder.setCompressionEnabled(false);
        // } catch (NoSuchAlgorithmException e) {
        // e.printStackTrace();
        // }

        // 设置主机IP地址ַ
        configBuilder.setHost(MyConstance.SERVICE_HOST);
        configBuilder.setPort(5222);
        configBuilder.setServiceName(MyConstance.SERVICE_HOST);
        configBuilder.setConnectTimeout(30 * 1000);
        configBuilder.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        configBuilder.setSendPresence(false);

        // 设置手动同意好友请求
        Roster.setDefaultSubscriptionMode(SubscriptionMode.manual);

        // 获取连接对象
        final XMPPTCPConnection connection = new XMPPTCPConnection(configBuilder.build());
        // 消息回执
        ProviderManager.addExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
        ProviderManager.addExtensionProvider(DeliveryReceiptRequest.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceiptRequest.Provider());
        DeliveryReceiptManager.getInstanceFor(connection).autoAddDeliveryReceiptRequests();

        // 设置使用流管理
        connection.setUseStreamManagement(true);
        connection.setUseStreamManagementResumption(true);

        // 设置允许自动重连
//        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
//        reconnectionManager.enableAutomaticReconnection();

//        ProviderManager.addExtensionProvider(RosterVer.ELEMENT, RosterVer.NAMESPACE, new RosterVerStreamFeatureProvider());
//        ProviderManager.addIQProvider(RosterPacket.ELEMENT, RosterPacket.NAMESPACE, new RosterPacketProvider());
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
                MyLog.showLog("rosterVer_init::" + MyApp.rosterVer);
                return MyApp.rosterVer;
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
        roster.setRosterLoadedAtLogin(true);

        // 将连接对象变成全应用变量
        MyApp.connection = connection;


        /**
         * 监听创建连接后 发出的数据
         */
        connection.addPacketSendingListener(new StanzaListener() {

            @Override
            public void processPacket(Stanza packet) throws NotConnectedException {
                CharSequence xml = packet.toXML();
//                MyLog.showLog("发出的流::" + xml.toString());
            }
        }, null);

        connection.addAsyncStanzaListener(new StanzaListener() {
            @Override
            public void processPacket(Stanza packet) throws NotConnectedException {
                CharSequence xml = packet.toXML();
                MyLog.showLog("收到的流::" + xml.toString());
            }
        }, null);
    }
}
