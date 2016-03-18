package com.open.im.utils;

import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.Roster.SubscriptionMode;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

import com.open.im.app.MyApp;

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
		configBuilder.setHost("openim.daimaqiao.net");
		configBuilder.setPort(5222);
		configBuilder.setServiceName("openim.daimaqiao.net");
		configBuilder.setConnectTimeout(30 * 1000);
		// configBuilder.setSendPresence(false);

		// 设置手动同意好友请求
		Roster.setDefaultSubscriptionMode(SubscriptionMode.manual);
		// 获取连接对象
		XMPPTCPConnection connection = new XMPPTCPConnection(configBuilder.build());
		// 消息回执
		ProviderManager.addExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
		ProviderManager.addExtensionProvider(DeliveryReceiptRequest.ELEMENT, DeliveryReceipt.NAMESPACE, new DeliveryReceiptRequest.Provider());
		DeliveryReceiptManager.getInstanceFor(connection).autoAddDeliveryReceiptRequests();

		// 设置使用流管理
		connection.setUseStreamManagement(true);
		connection.setUseStreamManagementResumption(true);

		// 设置允许自动重连
		ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(connection);
		reconnectionManager.enableAutomaticReconnection();

		// 将连接对象变成全应用变量
		MyApp.connection = connection;

		/**
		 * 监听创建连接后 发出的数据
		 */
		connection.addPacketSendingListener(new StanzaListener() {

			@Override
			public void processPacket(Stanza packet) throws NotConnectedException {
				CharSequence xml = packet.toXML();
				MyLog.showLog("发出的流::" + xml.toString());
			}
		}, null);

//		connection.addAsyncStanzaListener(new StanzaListener() {
//			@Override
//			public void processPacket(Stanza packet) throws NotConnectedException {
//				CharSequence xml = packet.toXML();
//				MyLog.showLog("收到的流::" + xml.toString());
//			}
//		}, null);

	}

}
