package com.open.im.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

import com.open.im.service.IMPushService;
import com.open.im.service.IMService;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyUtils;

/**
 * 收到广播时，判断IMService和IMPushService是否在运行中，若不在则启动服务
 * 若IMPushService在运行 并且 屏幕是锁屏状态 则唤醒CPU并立马释放
 */
public class TickAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MyLog.showLog("收到Alarm广播");
//		if(!MyNetUtils.isNetworkConnected(context)){
//			return;
//		}
        boolean isIMServiceRunning = MyUtils.isServiceRunning(context, "com.open.im.service.IMService");
        boolean isIMPushServiceRunning = MyUtils.isServiceRunning(context, "com.open.im.service.IMPushService");
        if (!isIMServiceRunning) {
            context.startService(new Intent(context, IMService.class));
        }
        if (!isIMPushServiceRunning) {
            context.startService(new Intent(context, IMPushService.class));
        } else {
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (!pm.isScreenOn()) {
                PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "cpu_tag");
                if (!wakeLock.isHeld()) {
                    wakeLock.acquire(30);
                }
//                wakeLock.release();
            }
        }
    }
}
