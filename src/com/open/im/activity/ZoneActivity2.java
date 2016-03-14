package com.open.im.activity;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.Subscription;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.bean.ZoneMessageBean;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyPubSubUtils;
import com.open.im.utils.MyUtils;
import com.open.im.utils.MyXmlUtils;
import com.open.im.utils.ThreadUtil;

/**
 * 空间界面 订阅貌似是通过订阅NodeID实现的，目前设置的用户Jid为nodeId，通过nodeId获取LeafNode对象，然后用当前user订阅
 * 
 * @author Administrator
 * 
 */
public class ZoneActivity2 extends Activity {

	private static final int QUERY_SUCCESS = 1001;
	private ZoneActivity2 act;
	private TextView tv_title;
	private EditText et_msg;
	private TextView tv_send;
	private ListView lv_messages;
	private AbstractXMPPConnection connection;
	private String nodeId;
	private PubSubManager pubSubManager;
//	private ItemEventListener<Item> myItemEventLinstener;
	private LeafNode eventNode;
	private List<ZoneMessageBean> msgList;
	private ArrayAdapter<ZoneMessageBean> adapter;
	private List<Subscription> subscriptions;
	private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);

		init();
		initData();
		register();
	}

	/**
	 * 初始化数据，获取所有的已发布的信息，并设置监听，监听新发布的信息
	 */
	private void initData() {
		String pubSubAddress = "pubsub." + connection.getServiceName();
		pubSubManager = new PubSubManager(connection, pubSubAddress);
		pd = new ProgressDialog(this);
		pd.setMessage("正在加载中...");
		pd.show();
		ThreadUtil.runOnBackThread(new Runnable() {
			@Override
			public void run() {
				try {
					//获取所有的已订阅的用户
					subscriptions = pubSubManager.getSubscriptions();
					MyLog.showLog(subscriptions.size() + "");
					msgList = new ArrayList<ZoneMessageBean>();
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
						List<Item> list = eventNode.getItems();
						for (Item item : list) {
							ZoneMessageBean bean = new ZoneMessageBean();
							bean.setMsgFromJid(nodeId);
							PayloadItem<?> payloadItem = (PayloadItem<?>) item;
							// MyLog.showLog("item::" + item.toXML());
							// String nodeId = item.getNode(); 这个也为null
							String id = payloadItem.getId();
							long longDate = Long.parseLong(id);
							bean.setMsgLongDate(longDate);
							String elementName = payloadItem.getPayload().getElementName(); // message
							String xml = payloadItem.getPayload().toXML().toString();
							// MyLog.showLog("elementName::" + elementName +
							// "内容：" +
							// xml);
							if ("message".equals(elementName)) {
								String msg = MyXmlUtils.getzoneMsgs(act, xml);
								bean.setMsgBody(msg);
								MyLog.showLog("msg::" + msg);
							}
							msgList.add(bean);
						}
					}
					handler.sendEmptyMessage(QUERY_SUCCESS);
				} catch (NoResponseException e) {
					e.printStackTrace();
				} catch (XMPPErrorException e) {
					e.printStackTrace();
				} catch (NotConnectedException e) {
					e.printStackTrace();
				}
			}
		});
		// while(true);
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case QUERY_SUCCESS:
				MyLog.showLog("list::" + msgList);
				adapter = new ArrayAdapter<ZoneMessageBean>(act, 0, msgList) {
					@Override
					public View getView(int position, View convertView, ViewGroup parent) {
						ViewHolder vh = null;
						if (convertView == null) {
							convertView = View.inflate(act, R.layout.list_item_zone_msg, null);
							vh = new ViewHolder();
							TextView tv_username = (TextView) convertView.findViewById(R.id.tv_username);
							TextView tv_msg = (TextView) convertView.findViewById(R.id.tv_msg);
							TextView tv_date = (TextView) convertView.findViewById(R.id.tv_date);

							vh.tvName = tv_username;
							vh.tvMsgBody = tv_msg;
							vh.tvDate = tv_date;
							convertView.setTag(vh);
						} else {
							vh = (ViewHolder) convertView.getTag();
						}

						ZoneMessageBean bean = msgList.get(position);
						// vh.tvName.setText(bean.getMsgFromJid().substring(0,
						// bean.getMsgFromJid().indexOf("@")));
						vh.tvName.setText(bean.getMsgFromJid());
						vh.tvMsgBody.setText(bean.getMsgBody());

						long msgLongDate = bean.getMsgLongDate();
						String msgDate;
						if (DateUtils.isToday(msgLongDate)) { // 判断是否是今天
							msgDate = DateFormat.getTimeFormat(act).format(msgLongDate);
						} else {
							msgDate = DateFormat.getDateFormat(act).format(msgLongDate);
						}
						vh.tvDate.setText(msgDate);
						return convertView;
					}
				};
				lv_messages.setAdapter(adapter);
				if (pd != null && pd.isShowing()) {
					pd.dismiss();
				}
//				registerPublishLinstener();
				break;

			default:
				break;
			}
		};
	};

	private class ViewHolder {

		protected TextView tvDate;
		protected TextView tvMsgBody;
		protected TextView tvName;

	}

//	/**
//	 * 注册对已订阅节点的监听
//	 */
//	private void registerPublishLinstener() {
//		for (Subscription subscription : subscriptions) {
//			// 获取的是当前用户的Jid
//			// MyLog.showLog("Jid::" + subscription.getJid());
//			// //Jid::lizh@im2.daimaqiao.net/Smack
//			String nodeId = subscription.getNode();
//
//			try {
//				// 获取node节点
//				eventNode = pubSubManager.getNode(nodeId);
//				// node不存在时，会直接抛异常，所以在这里抓一下，假设node不存在，不会影响程序的运行
//			} catch (Exception e) {
//				MyLog.showLog("node不存在");
//				e.printStackTrace();
//			}
//
//			if (eventNode == null) {
//				MyLog.showLog("node==null");
//				return;
//			}
//
////			// eventNode.unsubscribe(connection.getUser());
////			// 订阅
////			try {
////				eventNode.subscribe(connection.getUser());
////			} catch (NoResponseException e) {
////				e.printStackTrace();
////			} catch (XMPPErrorException e) {
////				e.printStackTrace();
////			} catch (NotConnectedException e) {
////				e.printStackTrace();
////			}
//
//			myItemEventLinstener = new ItemEventListener<Item>() {
//
//				@Override
//				public void handlePublishedItems(ItemPublishEvent<Item> items) {
//					MyLog.showLog("到这儿没");
//					PayloadItem<?> item = (PayloadItem<?>) items.getItems().get(0);
//					String elementName = item.getPayload().getElementName(); // message
//					// MyLog.showLog("elementName::" + elementName);
//					// Date publishedDate = items.getPublishedDate();
//					// MyLog.showLog("Date::" + publishedDate); Date一直是null不着为嘛
//
//					String idStr = item.getId();
//					long longDate = Long.parseLong(idStr);
//					Date date = new Date(longDate);
//					MyLog.showLog("idDate::" + date);
//
//					// MyLog.showLog(items.getNodeId() + "::" + xml);
//					// MyLog.showLog(item.getElementName() + "---");
//				}
//			};
//			// 为本节点设置监听
//			eventNode.addItemEventListener(myItemEventLinstener);
//		}
//	}

	/**
	 * 发布按钮点击事件
	 */
	private void register() {
		tv_send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String msgBody = et_msg.getText().toString().trim();
				et_msg.setText("");
				if (TextUtils.isEmpty(msgBody)) {
					MyUtils.showToast(act, "内容不能为空");
					return;
				}
				// 发布消息
				MyPubSubUtils.publishMsg(nodeId, msgBody);
			}
		});
	}

	/**
	 * 初始化数据
	 */
	private void init() {
		act = this;
		connection = MyApp.connection;
		//nodeId是 lizh@im2.daimaqiao.net
		nodeId = MyApp.username + "@" + connection.getServiceName();
		// nodeId = "test_nodeId";
		tv_title = (TextView) findViewById(R.id.tv_title);
		et_msg = (EditText) findViewById(R.id.et_msg);
		tv_send = (TextView) findViewById(R.id.tv_send);
		lv_messages = (ListView) findViewById(R.id.lv_messages);

		tv_title.setText("我的空间");
	}

	@Override
	/**
	 * 界面销毁时，取消监听
	 */
	protected void onDestroy() {
		super.onDestroy();
//		if (eventNode != null) {
//			eventNode.removeItemEventListener(myItemEventLinstener);
//		}
	}
}
