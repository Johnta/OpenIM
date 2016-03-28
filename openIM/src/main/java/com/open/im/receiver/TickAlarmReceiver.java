package com.open.im.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.open.im.service.IMService;
import com.open.im.utils.MyNetUtils;


public class TickAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if(MyNetUtils.isNetworkConnected(context) == false){
			return;
		}
		Intent startSrv = new Intent(context, IMService.class);
		context.startService(startSrv);
	}

}
