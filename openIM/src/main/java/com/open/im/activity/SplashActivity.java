package com.open.im.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;

import com.open.im.R;
import com.open.im.service.IMService;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyUtils;
import com.open.im.utils.ThreadUtil;

public class SplashActivity extends Activity {

    private SharedPreferences sp;
    private final int GO_LOGIN = 101;
    private final int GO_RE_LOGIN = 103;
    private final int GO_MAIN = 102;
    private SplashActivity act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        act = this;
        sp = getSharedPreferences(MyConstance.SP_NAME, 0);
        login();
    }

    private void login() {
        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                // 手机从开机到现在的毫秒值
                long startTime = SystemClock.uptimeMillis();
                String username = sp.getString("username", "");
                String password = sp.getString("password", "");

                boolean imService = MyUtils.isServiceRunning(act, "com.open.im.service.IMService");

                long endTime = SystemClock.uptimeMillis();
                long passTime = endTime - startTime; // 联网的用时
                if (passTime < 1000) { // 联网很快，2秒内，就完成了
                    SystemClock.sleep(1000 - passTime);
                }

                if (imService) {
                    handler.sendEmptyMessage(GO_MAIN);
                } else {
                    if (TextUtils.isEmpty(username)) {
                        handler.sendEmptyMessage(GO_LOGIN);
                    } else {
                        if (TextUtils.isEmpty(password)){
                            handler.sendEmptyMessage(GO_RE_LOGIN);
                        } else {
                            startService(new Intent(act, IMService.class));
                            handler.sendEmptyMessage(GO_MAIN);
                        }
                    }
                }
            }
        });
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case GO_LOGIN:
                    // 跳转至登录页面
                    Intent intent = new Intent(act, LoginActivity.class);
                    startActivity(intent);
                    finish();// 结束当前activity
                    break;
                case GO_RE_LOGIN:
                    // 跳转至重新登录页面
                    Intent intent3 = new Intent(act, ReLoginActivity.class);
                    startActivity(intent3);
                    finish();// 结束当前activity
                    break;
                case GO_MAIN:
                    // 跳转至主页面
                    Intent intent2 = new Intent(act, MainActivity.class);
                    startActivity(intent2);
                    finish();// 结束当前activity
                    break;
            }
        }

        ;
    };
}
