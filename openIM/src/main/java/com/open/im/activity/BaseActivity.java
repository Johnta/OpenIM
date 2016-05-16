package com.open.im.activity;

import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        act = this;
        MyApp.addActivity(this);
    }

    @Override
    protected void onResume() {
        MyLog.showLog("应用可见_connection::" + MyApp.connection);
        MyLog.showLog("应用可见_connected::" + MyApp.connection.isConnected());
        MyLog.showLog("应用可见_auth::" + MyApp.connection.isAuthenticated());
        MyLog.showLog("应用可见_socket_closed::" + MyApp.connection.isSocketClosed());
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