package com.open.im.activity;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.service.IMService;
import com.open.im.utils.MyConstance;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 个人设置界面
 * Created by Administrator on 2016/4/8.
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener {

    private SettingActivity act;
    private NotificationManager notificationManager;
    private SharedPreferences sp;
//    private OpenIMDao openIMDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        act = this;
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        sp = getSharedPreferences(MyConstance.SP_NAME, 0);
//        openIMDao = OpenIMDao.getInstance(act);
    }
    @Override
    public void onBackPressed() {
//        Intent intent = new Intent(act, MainActivity.class);
//        intent.putExtra("selection",3);
//        startActivity(intent);
        super.onBackPressed();
    }

    @OnClick({R.id.ib_back, R.id.tv_back, R.id.rl_change_pwd, R.id.rl_clean, R.id.btn_logout})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_change_pwd:
                act.startActivity(new Intent(act, UpdatePasswordActivity.class));
                break;
            case R.id.rl_clean:
                act.startActivity(new Intent(act, CleanCacheActivity.class));
                break;
            case R.id.btn_logout:
                // 注销登录时，退出应用，关闭服务
                IMService.getInstance().stopSelf();
                notificationManager.cancel(MyConstance.NOTIFY_ID_MSG);
                notificationManager.cancel(MyConstance.NOTIFY_ID_SUB);
                // 注销时清空密码
                sp.edit().putString("password","").apply();
                // 删除RosterVersion
                sp.edit().putString(MyConstance.ROSTER_VER,"").apply();
                // 注销时删除本账号的VCard信息
//                openIMDao.deleteSingleVCard(MyApp.username + "@" + MyConstance.SERVICE_HOST);
                // 跳转到重新登录界面
                Intent loginIntent = new Intent(act, ReLoginActivity.class);
                act.startActivity(loginIntent);
                act.finish();
                MyApp.clearActivity();
                break;
            case R.id.tv_back:
            case R.id.ib_back:
//                Intent intent = new Intent(act, MainActivity.class);
//                intent.putExtra("selection",3);
//                startActivity(intent);
                finish();
                break;
        }
    }
}
