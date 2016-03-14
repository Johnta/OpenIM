package com.open.im.activity;

import java.util.List;

import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smackx.search.ReportedData.Row;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyPubSubUtils;
import com.open.im.utils.MyUserSearchUtils;
import com.open.im.utils.MyUtils;

public class AddFriendActivity extends Activity {
	
	private Button btn_search;
	private ImageView iv_back;
	private EditText et_search_key;
	private TextView tv_search_result;
	private AddFriendActivity act;
	private String friendJid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		//初始化操作
		init();
		
		//点击事件注册监听
		register();
	}

	/**
	 * 点击事件监听
	 */
	private void register() {
		
		iv_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		btn_search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String searchKey = et_search_key.getText().toString().trim();
				if (TextUtils.isEmpty(searchKey)) {
					MyUtils.showToast(act, "用户名不能为空");
					return;
				}
				//查询用户是否存在
				boolean userExist = MyUserSearchUtils.isUserExist(searchKey);
				if (userExist) {
					//查找到用户 显示在搜索框下面
					List<Row> searchUsers = MyUserSearchUtils.searchUsers(searchKey);
					friendJid = searchUsers.get(0).getValues("jid").get(0);
					tv_search_result.setText(friendJid);
					
					//点击搜到的好友 弹出对话框 提示可以添加
					tv_search_result.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							AlertDialog.Builder builder = new Builder(act);
							builder.setMessage("添加为好友？");
							builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									tv_search_result.setText("");
									// 获得roster对象
									Roster roster = Roster.getInstanceFor(MyApp.connection);
									if (roster.getGroupCount() == 0) {
										//创建组
										roster.createGroup("我的好友");
									}
									try {
										//在组内添加用户   第一个参数jid第二个参数用户名name
										roster.createEntry(friendJid, searchKey, new String[]{"我的好友"});
										MyLog.showLog("friendjid::" + friendJid);
										//添加好友成功后 订阅该好友
										MyPubSubUtils.subscribeFriend(friendJid);
										finish();
									} catch (NotLoggedInException e) {
										e.printStackTrace();
									} catch (NoResponseException e) {
										e.printStackTrace();
									} catch (XMPPErrorException e) {
										e.printStackTrace();
									} catch (NotConnectedException e) {
										e.printStackTrace();
									}
								}

								
							});
							builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									
								}
							});
							builder.show();
						}
					});
					
				} else {
					MyUtils.showToast(act, "用户不存在");
				}
			}
		});
	}

	/**
	 * 初始化操作
	 */
	private void init() {
		act = this;
		
		btn_search = (Button) findViewById(R.id.btn_search);
		iv_back = (ImageView) findViewById(R.id.iv_back);
		et_search_key = (EditText) findViewById(R.id.et_search_key);
		tv_search_result = (TextView) findViewById(R.id.tv_search_result);
	}
}
