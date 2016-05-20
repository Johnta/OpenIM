package com.open.im.activity;

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

public class BaseActivity extends FragmentActivity {
    private BaseActivity act;
    private boolean isFocus = true;
    private BroadcastReceiver newConnectReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        act = this;
        MyApp.addActivity(this);

        newConnectReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                doNewConnection();
            }
        };
        IntentFilter filter = new IntentFilter(MyConstance.NEW_CONNECTION_ACTION);
        registerReceiver(newConnectReceiver,filter);
    }

    /**
     * 接收创建新的connection对象的广播里的方法，子类如需使用需要重写
     */
    protected void doNewConnection() {
    }

    @Override
    protected void onResume() {
        MyLog.showLog("应用可见_connection::" + MyApp.connection);
        if (MyApp.connection != null) {
            MyLog.showLog("应用可见_connected::" + MyApp.connection.isConnected());
            MyLog.showLog("应用可见_auth::" + MyApp.connection.isAuthenticated());
            MyLog.showLog("应用可见_socket_closed::" + MyApp.connection.isSocketClosed());
        }
        if (MyApp.connection == null || !MyApp.connection.isConnected() || !MyApp.connection.isAuthenticated()) {
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

    //    /**
//     * 程序是否在前台运行
//     *
//     * @return
//     */
//    public boolean isAppOnForeground() {
//        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
//        String packageName = getApplicationContext().getPackageName();
//
//        List<RunningAppProcessInfo> appProcesses = activityManager
//                .getRunningAppProcesses();
//        if (appProcesses == null)
//            return false;
//
//        for (RunningAppProcessInfo appProcess : appProcesses) {
//            if (appProcess.processName.equals(packageName)
//                    && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//                return true;
//            }
//        }
//        return false;
//    }
}