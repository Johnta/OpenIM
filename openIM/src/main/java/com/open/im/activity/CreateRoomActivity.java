package com.open.im.activity;

import java.util.List;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.open.im.R;
import com.open.im.app.MyApp;

public class CreateRoomActivity extends Activity {
	private EditText et_roomname;
	private EditText et_pwd;
	private EditText et_nick;
	private Button btn_create;
	private AbstractXMPPConnection connection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_room);

		init();

		register();
	}

	/**
	 * 初始化控件
	 */
	private void init() {
		et_roomname = (EditText) findViewById(R.id.et_roomname);
//		et_pwd = (EditText) findViewById(R.id.et_pwd);
		et_nick = (EditText) findViewById(R.id.et_nick);
		btn_create = (Button) findViewById(R.id.btn_create);
		connection = MyApp.connection;
	}

	/**
	 * 注册创建按钮点击监听
	 */
	private void register() {
		btn_create.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!connection.isConnected()) {
					throw new NullPointerException("服务器连接失败，请先连接服务器");
				}
				// 创建聊天室
				createChatRoom();
			}

		});
	}

	/**
	 * 方法 创建聊天室
	 */
	private void createChatRoom() {
		String roomName = et_roomname.getText().toString().trim();
//		String userNick = et_nick.getText().toString().trim();
//		String roompwd = et_pwd.getText().toString().trim();
		if (TextUtils.isEmpty(roomName)) {
			et_roomname.setError("房间名不能为空");
			return;
		} 
//		else if (TextUtils.isEmpty(roompwd)) {
//			et_pwd.setError("请输入房间密码");
//			return;
//		} 
//		else if (TextUtils.isEmpty(userNick)) {
//			et_nick.setError("请输入你在房间的昵称");
//			return;
//		}
		// 获取聊天室管理者
		MultiUserChatManager multiUserChatManager = MultiUserChatManager.getInstanceFor(connection);
		// 通过聊天室管理者获取聊天室对象 参数必须加上 @conference.
		MultiUserChat multiUserChat = multiUserChatManager
		// 必须加上"@conference."
				.getMultiUserChat(roomName + "@conference." + connection.getServiceName());
		try {
			// 通过聊天室对象创建聊天室并加入聊天室 参数为用户在聊天室显示的昵称
			multiUserChat.createOrJoin(MyApp.username);
			finish();
			// 获得聊天室的配置表单 C语言模式
			Form form = multiUserChat.getConfigurationForm();
			// Form form2 = multiUserChat.getRegistrationForm();
			// Form submitform2 = form2.createAnswerForm();
			// List<FormField> fields2 = form2.getFields();
			// for (FormField formField : fields2) {
			// MyLog.showLog(formField.getVariable() + "------");
			// }
			// 根据获得的表单 创建需要提交的表单
			Form submitForm = form.createAnswerForm();
			List<FormField> fields = form.getFields();
			for (FormField formField : fields) {
				// 设置默认值为答复
				submitForm.setDefaultAnswer(formField.getVariable());
				// FORM_TYPE
				// muc#roomconfig_roomname
				// muc#roomconfig_roomdesc
				// muc#roomconfig_persistentroom 持久聊天室 true
				// muc#roomconfig_publicroom
				// public_list
				// muc#roomconfig_passwordprotectedroom
				// muc#roomconfig_roomsecret
				// muc#roomconfig_maxusers
				// muc#roomconfig_whois
				// muc#roomconfig_presencebroadcast
				// muc#roomconfig_membersonly
				// muc#roomconfig_moderatedroom
				// members_by_default
				// muc#roomconfig_changesubject
				// allow_private_messages
				// allow_private_messages_from_visitors
				// allow_query_users
				// muc#roomconfig_allowinvites
				// muc#roomconfig_allowvisitorstatus
				// muc#roomconfig_allowvisitornickchange
				// muc#roomconfig_allowvoicerequests
				// muc#roomconfig_voicerequestmininterval
				// muc#roomconfig_captcha_whitelist
				// MyLog.showLog(formField.getVariable());
			}

			// 设置提交的表单为默认值后，修改提交的表单的内容 然后提交

			// 设置房间名称
			submitForm.setAnswer("muc#roomconfig_roomname", roomName);
			// 设置房间描述
			submitForm.setAnswer("muc#roomconfig_roomdesc", "房间描述");
			// 设置房间拥有者 但是4.1.5里面好像没有这个字段 muc#roomconfig_roomowners
			// 4.1.5默认创建者是拥有者
			// List<String> owners = new ArrayList<String>();
			// owners.add(connection.getUser());
			// submitForm.setAnswer("muc#roomconfig_roomowners", owners);
			// 设置聊天室是持久聊天室
			submitForm.setAnswer("muc#roomconfig_persistentroom", true);
			// 设置聊天室是否只是对成员开放
			submitForm.setAnswer("muc#roomconfig_membersonly", false);
			// 允许拥有者邀请其他人加入
			submitForm.setAnswer("muc#roomconfig_allowinvites", true);
			// 设置进入是否需要密码
			submitForm.setAnswer("muc#roomconfig_passwordprotectedroom", false);
			// 设置进入密码
//			submitForm.setAnswer("muc#roomconfig_roomsecret", roompwd);
			// 提交表单数据
			multiUserChat.sendConfigurationForm(submitForm);
			// multiUserChat.join(userNick);
		} catch (NoResponseException e) {
			e.printStackTrace();
		} catch (XMPPErrorException e) {
			e.printStackTrace();
		} catch (SmackException e) {
			e.printStackTrace();
		}
	}
}
