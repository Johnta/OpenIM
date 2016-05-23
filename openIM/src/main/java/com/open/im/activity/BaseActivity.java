package com.open.im.activity;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.open.im.app.MyApp;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyNetUtils;
import com.open.im.utils.MyUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.ping.PingManager;

import java.util.List;

public class BaseActivity extends FragmentActivity {
    private BaseActivity act;
    private boolean isFocus = true;
    private BroadcastReceiver newConnectReceiver;
    private XMPPTCPConnection connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        act = this;
        MyApp.addActivity(this);

        connection = MyApp.connection;

        newConnectReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                doNewConnection();
            }
        };
        IntentFilter filter = new IntentFilter(MyConstance.NEW_CONNECTION_ACTION);
        registerReceiver(newConnectReceiver, filter);
    }

    /**
     * 接收创建新的connection对象的广播里的方法，子类如需使用需要重写
     */
    protected void doNewConnection() {
    }

    @Override
    protected void onResume() {

        if (!MyApp.isActive) {
            MyApp.isActive = true;
            MyLog.showLog("程序处于前台");
            PingManager pingManager = PingManager.getInstanceFor(connection);
            try {
                boolean isReachable = pingManager.pingMyServer();
                MyLog.showLog("isReachable::" + isReachable);
                if (!isReachable) {
                    if (MyNetUtils.isNetworkConnected(act) && isFocus) {
                        sendBroadcast(new Intent(MyConstance.APP_FOREGROUND_ACTION));
                    }
                }
            } catch (SmackException.NotConnectedException e) {
                if (MyNetUtils.isNetworkConnected(act) && isFocus) {
                    sendBroadcast(new Intent(MyConstance.APP_FOREGROUND_ACTION));
                }
                e.printStackTrace();
            }
        }

        MyLog.showLog("应用可见_connection::" + connection);
        if (connection != null) {
            MyLog.showLog("应用可见_connected::" + connection.isConnected());
            MyLog.showLog("应用可见_auth::" + connection.isAuthenticated());
            MyLog.showLog("应用可见_socket_closed::" + connection.isSocketClosed());
        }
        if (connection == null || !connection.isConnected() || !connection.isAuthenticated()) {
            MyUtils.showToast(act, "应用已断开链接");
            if (MyNetUtils.isNetworkConnected(act) && isFocus) {
                sendBroadcast(new Intent(MyConstance.APP_FOREGROUND_ACTION));
            }
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyLog.showLog("onPause");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        isFocus = hasFocus;
        MyLog.showLog("onWindowFocusChanged -----------------" + hasFocus);
    }

    @Override
    protected void onDestroy() {
        if (newConnectReceiver != null) {
            unregisterReceiver(newConnectReceiver);
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!isAppOnForeground()) {
            MyApp.isActive = false;
            MyLog.showLog("程序处于后台");
        }
    }

    /**
     * 程序是否在前台运行
     *
     * @return
     */
    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }
}