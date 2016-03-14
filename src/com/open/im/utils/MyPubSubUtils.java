package com.open.im.utils;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.SimplePayload;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

import android.content.Context;

import com.open.im.app.MyApp;
import com.open.im.bean.MessageBean;
import com.open.im.db.ZoneMsgDao;

/**
 * 订阅发布相关的工具类
 * 
 * @author Administrator
 * 
 */
public class MyPubSubUtils {
	private static LeafNode eventNode;

	/**
	 * 方法 订阅好友
	 * 
	 * @param nodeId
	 *            : 好友Jid b@im2.daimaqiao.net
	 */
	public static void subscribeFriend(String nodeId) {
		AbstractXMPPConnection connection = MyApp.connection;
		String pubSubAddress = "pubsub." + connection.getServiceName();
		PubSubManager pubSubManager = new PubSubManager(connection, pubSubAddress);
		MyLog.showLog(nodeId + "订阅");
		try {
			eventNode = pubSubManager.getNode(nodeId);
			// pubSubManager.g
		} catch (Exception e) {
			MyLog.showLog("node不存在");
			e.printStackTrace();
		}
		if (eventNode == null) {
			MyLog.showLog("node==null");
			return;
		}
		try {
			eventNode.subscribe(connection.getUser());
			eventNode.addItemEventListener(new ItemEventListener<Item>() {

				@Override
				public void handlePublishedItems(ItemPublishEvent<Item> items) {
					MyLog.showLog("订阅用户后，添加监听");
				}
			});
			MyLog.showLog("添加好友，并订阅成功");
		} catch (NoResponseException e) {
			e.printStackTrace();
		} catch (XMPPErrorException e) {
			e.printStackTrace();
		} catch (NotConnectedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 方法 取消订阅好友
	 * 
	 * @param nodeId
	 *            : 好友Jid b@im2.daimaqiao.net
	 */
	public static void unSubscribeFriend(String nodeId) {
		AbstractXMPPConnection connection = MyApp.connection;
		String pubSubAddress = "pubsub." + connection.getServiceName();
		PubSubManager pubSubManager = new PubSubManager(connection, pubSubAddress);
		try {
			eventNode = pubSubManager.getNode(nodeId);
			// pubSubManager.g
		} catch (Exception e) {
			MyLog.showLog("node不存在");
			e.printStackTrace();
		}
		if (eventNode == null) {
			MyLog.showLog("node==null");
			return;
		}
		try {
			eventNode.unsubscribe(connection.getUser());
			MyLog.showLog("取消订阅");
		} catch (NoResponseException e) {
			e.printStackTrace();
		} catch (XMPPErrorException e) {
			e.printStackTrace();
		} catch (NotConnectedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 创建节点
	 * 
	 * @param username
	 */
	public static void createNode(final String username) {
		ThreadUtil.runOnBackThread(new Runnable() {

			@Override
			public void run() {
				AbstractXMPPConnection connection = MyApp.connection;
				String pubSubAddress = "pubsub." + connection.getServiceName();
//				String pubSubAddress = connection.getServiceName();
				PubSubManager pubSubManager = new PubSubManager(connection, pubSubAddress);
				String nodeId = username + "@" + connection.getServiceName();
				MyLog.showLog(nodeId + "创建");
				LeafNode myNode = null;
				try {
					myNode = pubSubManager.getNode(nodeId);
					// node不存在时，会直接抛异常，所以在这里抓一下，假设node不存在，不会影响程序的运行
				} catch (Exception e) {
					MyLog.showLog("node不存在");
					e.printStackTrace();
				}
				if (myNode == null) {
					try {
						myNode = pubSubManager.createNode(nodeId);
					} catch (NoResponseException e) {
						e.printStackTrace();
					} catch (XMPPErrorException e) {
						e.printStackTrace();
					} catch (NotConnectedException e) {
						e.printStackTrace();
					}
				}
				try {
					myNode.subscribe(username + "@" + connection.getServiceName());
				} catch (NoResponseException e) {
					e.printStackTrace();
				} catch (XMPPErrorException e) {
					e.printStackTrace();
				} catch (NotConnectedException e) {
					e.printStackTrace();
				}
				MyLog.showLog("创建Node成功,并订阅自己");
			}
		});
	}

	/**
	 * 发布文本消息
	 */
	public static void publishMsg(String nodeId, String msgBody) {
		AbstractXMPPConnection connection = MyApp.connection;
		String pubSubAddress = "pubsub." + connection.getServiceName();
		PubSubManager pubSubManager = new PubSubManager(connection, pubSubAddress);
		try {
			LeafNode myNode = null;
			try {
				myNode = pubSubManager.getNode(nodeId);
				// node不存在时，会直接抛异常，所以在这里抓一下，假设node不存在，不会影响程序的运行
			} catch (Exception e) {
				MyLog.showLog("node不存在");
				e.printStackTrace();
			}
			if (myNode == null) {

				// ConfigureForm form = new ConfigureForm(Type.submit);
				// form.setNodeType(NodeType.leaf);
				// form.setAccessModel(AccessModel.open);
				// form.setPublishModel(PublishModel.open);
				// form.setPersistentItems(true);
				// form.setNotifyRetract(true) ;
				// form.setMaxItems(65535);

				// 如果节点不存在则创建节点
				myNode = pubSubManager.createNode(nodeId);
				// myNode.sendConfigurationForm(form);
			}
			// myNode.subscribe(connection.getUser());
			SimplePayload simplePayload = new SimplePayload("message", "pubsub:test:message", "<message xmlns='pubsub:test:message'><body>" + msgBody + "</body></message>");
			// SimplePayload simplePayload = new
			// SimplePayload("picture","pubsub:test:picture",
			// "<picture xmlns='pubsub:test:picture'><title>"+msgBody+"</title><content>"+"www.baidu.com"+"</content></picture>");
			// id传的是发布的时间，将来通过id获取发布时间
			PayloadItem<SimplePayload> payloadItem = new PayloadItem<SimplePayload>("" + System.currentTimeMillis(), nodeId, simplePayload);
			MyLog.showLog("发布nodeid::" + nodeId);
			// 通过节点发布消息
			myNode.publish(payloadItem);

			MyLog.showLog("发布成功");
		} catch (NoResponseException e) {
			MyLog.showLog("发布失败1");
			e.printStackTrace();
		} catch (XMPPErrorException e) {
			MyLog.showLog("发布失败2");
			e.printStackTrace();
		} catch (NotConnectedException e) {
			MyLog.showLog("发布失败3");
			e.printStackTrace();
		}

	}

	/**
	 * 注册对已订阅节点的监听
	 */
	public static void registerPublishLinstener(final Context ctx) {
		ThreadUtil.runOnBackThread(new Runnable() {

			@Override
			public void run() {
				try {
					final ZoneMsgDao dao = ZoneMsgDao.getInstance(ctx);
					AbstractXMPPConnection connection = MyApp.connection;
					String pubSubAddress = "pubsub." + connection.getServiceName();
					PubSubManager pubSubManager = new PubSubManager(connection, pubSubAddress);
					List<Subscription> subscriptions;
					subscriptions = pubSubManager.getSubscriptions();
					for (Subscription subscription : subscriptions) {
						// 获取的是当前用户的Jid
						// MyLog.showLog("Jid::" + subscription.getJid());
						// //Jid::lizh@im2.daimaqiao.net/Smack
						final String nodeId = subscription.getNode();

						try {
							// 获取node节点
							eventNode = pubSubManager.getNode(nodeId);
							// node不存在时，会直接抛异常，所以在这里抓一下，假设node不存在，不会影响程序的运行
						} catch (Exception e) {
							MyLog.showLog("node不存在");
							e.printStackTrace();
						}

						if (eventNode == null) {
							MyLog.showLog("node==null");
							return;
						}

						ItemEventListener<Item> myItemEventLinstener = new ItemEventListener<Item>() {
							@Override
							public void handlePublishedItems(ItemPublishEvent<Item> items) {
								MyLog.showLog("到这儿没");
								PayloadItem<?> payloadItem = (PayloadItem<?>) items.getItems().get(0);
								String elementName = payloadItem.getPayload().getElementName(); // message
								// MyLog.showLog("elementName::" + elementName);
								// Date publishedDate =
								// items.getPublishedDate();
								// MyLog.showLog("Date::" + publishedDate);
								// Date一直是null不着为嘛
								String idStr = payloadItem.getId();
								long longDate = Long.parseLong(idStr);
								long queryTheLastMsgDate = dao.queryTheLastMsgDate();
								if (longDate > queryTheLastMsgDate) {

									MessageBean bean = new MessageBean();
									bean.setMsgDateLong(longDate);
									bean.setFromUser(nodeId);
									bean.setMsgMark(MyApp.username);
									String xml = payloadItem.getPayload().toXML().toString();
									// MyLog.showLog("elementName::" +
									// elementName +
									// "内容：" +
									// xml);
									if ("message".equals(elementName)) {
										String msg = MyXmlUtils.getzoneMsgs(ctx, xml);
										bean.setMsgBody(msg);
										MyLog.showLog("msg::" + msg);
										dao.insertMsg(bean);
									}
								}
								// Date date = new Date(longDate);
								// MyLog.showLog("idDate::" + date);
								// MyLog.showLog(items.getNodeId() + "::" +
								// xml);
								// MyLog.showLog(item.getElementName() + "---");
							}
						};
						// 为本节点设置监听
						eventNode.addItemEventListener(myItemEventLinstener);
					}
				} catch (NoResponseException e) {
					e.printStackTrace();
				} catch (XMPPErrorException e) {
					e.printStackTrace();
				} catch (NotConnectedException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 方法 接收所有已订阅的用户发布信息
	 */
	public static void receiveAllPublishMsg(final Context ctx) {
		ThreadUtil.runOnBackThread(new Runnable() {

			@Override
			public void run() {
				ZoneMsgDao dao = ZoneMsgDao.getInstance(ctx);
				AbstractXMPPConnection connection = MyApp.connection;
				String pubSubAddress = "pubsub." + connection.getServiceName();
				PubSubManager pubSubManager = new PubSubManager(connection, pubSubAddress);
				try {
					// 获取所有的已订阅的用户
					List<Subscription> subscriptions = pubSubManager.getSubscriptions();
					MyLog.showLog(subscriptions.size() + "");
					TreeMap<Long, MessageBean> map = new TreeMap<Long, MessageBean>();
					for (Subscription subscription : subscriptions) {
						// 获取的是当前用户的Jid
						// MyLog.showLog("Jid::" + subscription.getJid());
						// //Jid::lizh@im2.daimaqiao.net/Smack
						String nodeId = subscription.getNode();
						MyLog.showLog("订阅的nodeid::" + nodeId); // 目前设置的NodeId就是用户Jid
						try {
							// 获取node节点
							eventNode = pubSubManager.getNode(nodeId);
							// node不存在时，会直接抛异常，所以在这里抓一下，假设node不存在，不会影响程序的运行
						} catch (Exception e) {
							MyLog.showLog("node不存在");
							e.printStackTrace();
						}

						if (eventNode == null) {
							MyLog.showLog("node==null");
							return;
						}
						// 获取本节点所有的已发布信息
						List<Item> list = eventNode.getItems(15);
						long queryTheLastMsgDate = dao.queryTheLastMsgDate();
						for (Item item : list) {
							PayloadItem<?> payloadItem = (PayloadItem<?>) item;
							// MyLog.showLog("item::" + item.toXML());
							// String nodeId = item.getNode(); 这个也为null
							String id = payloadItem.getId();
							long longDate = Long.parseLong(id);
							// MyLog.showLog(id);
							if (longDate > queryTheLastMsgDate) {
								MessageBean bean = new MessageBean();
								bean.setMsgMark(MyApp.username);
								bean.setFromUser(nodeId);
								bean.setMsgDateLong(longDate);
								String elementName = payloadItem.getPayload().getElementName(); // message
								String xml = payloadItem.getPayload().toXML().toString();
								// MyLog.showLog("elementName::" + elementName +
								// "内容：" +
								// xml);
								if ("message".equals(elementName)) {
									String msg = MyXmlUtils.getzoneMsgs(ctx, xml);
									bean.setMsgBody(msg);
									MyLog.showLog("msg::" + msg);
									// dao.insertMsg(bean);
									map.put(longDate, bean);
									// MyLog.showLog("map::" + map);
								}
							}
						}
					}
					Set<Long> keySet = map.keySet();
					for (Long key : keySet) {
						MessageBean bean = map.get(key);
						dao.insertMsg(bean);
					}
				} catch (NoResponseException e) {
					e.printStackTrace();
				} catch (XMPPErrorException e) {
					e.printStackTrace();
				} catch (NotConnectedException e) {
					e.printStackTrace();
				}
			}
		});
	}
}
