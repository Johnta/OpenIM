package com.open.im.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

import com.open.im.utils.MyNetUtils;


public class MyNetReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
			boolean isConnected = MyNetUtils.isNetworkConnected(context);
	        System.out.println("网络状态：" + isConnected);
	        System.out.println("wifi状态：" + MyNetUtils.isWifiConnected(context));
	        System.out.println("移动网络状态：" + MyNetUtils.isMobileConnected(context));
	        System.out.println("网络连接类型：" + MyNetUtils.getConnectedType(context));
	        if (isConnected) {
	        	Toast.makeText(context, "已经连接网络", Toast.LENGTH_LONG).show();
	        } else {
	        	Toast.makeText(context, "已经断开网络", Toast.LENGTH_LONG).show();
	        }
		}
	}
	
}
