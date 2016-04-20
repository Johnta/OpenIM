package com.open.im.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.bean.VCardBean;
import com.open.im.db.ChatDao;
import com.open.im.service.IMService;
import com.open.im.utils.MyBitmapUtils;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyUtils;
import com.open.im.utils.ThreadUtil;
import com.open.im.utils.XMPPConnectionUtils;
import com.open.im.view.ClearEditText;
import com.open.im.view.MyDialog;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.io.IOException;

public class ReLoginActivity extends Activity implements OnClickListener {

    private static final int QUERY_SUCCESS = 1000;
    private static final int QUERY_FAIL = 1001;
    private ClearEditText et_pwd;
    private Button btn_login;
    private TextView tv_change;
    private ReLoginActivity act;
    private MyDialog pd;

    /**
     * 登录状态
     */
    private static final int LOGIN_SUCCESS = 0;
    private static final int FAIL_PASSWORD_ERROR = 1;
    private static final int FAIL_UNKNOWN_USER = 2;
    private static final int INTERNET_ERROR = 3;
    private static final int LOGIN_FAIL = 4;

    private SharedPreferences sp;
    private Intent service;
    private XMPPTCPConnection connection;
    private TextView tv_nick;
    private ImageView iv_avatar;
    private TextView tv_version;
    private String versionNameStr;
    private String userName;
    private VCardBean vCardBean;
    private MyBitmapUtils bitmapUtils;
    private ChatDao chatDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relogin);
        act = this;
        sp = getSharedPreferences(MyConstance.SP_NAME, 0);
        userName = sp.getString("username", "");
        /**
         * 获得包管理器，手机中所有应用，共用一个包管理器
         */
        PackageManager packageManager = act.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(act.getPackageName(), 0);
            versionNameStr = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        bitmapUtils = new MyBitmapUtils(act);
        chatDao = ChatDao.getInstance(act);
        initView();
        initData();
        register();
    }

    private void initData() {
        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                vCardBean =chatDao.queryVCard(userName + "@openim.daimaqiao.net");
                if (vCardBean != null) {
                    handler.sendEmptyMessage(QUERY_SUCCESS);
                } else {
                    handler.sendEmptyMessage(QUERY_FAIL);
                }
            }
        });
    }

    private void register() {
        // 登录注册键点击事件
        btn_login.setOnClickListener(this);
        tv_change.setOnClickListener(this);
    }

    /**
     * 界面初始化
     */
    private void initView() {

        tv_nick = (TextView) findViewById(R.id.tv_nick);
        et_pwd = (ClearEditText) findViewById(R.id.et_pwd);
        btn_login = (Button) findViewById(R.id.btn_login);
        tv_change = (TextView) findViewById(R.id.tv_change);
        iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
        tv_version = (TextView) findViewById(R.id.tv_version);
        tv_version.setText("OpenIM " + versionNameStr);

        TextPaint paint = tv_change.getPaint();
        //加下划线
        paint.setFlags(Paint.UNDERLINE_TEXT_FLAG);
        //设置字体为粗体
        paint.setFakeBoldText(true);

        //加下划线另一种方式
//		SpannableString content = new SpannableString("注册新用户");
//		content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
//		tv_change.setText(content);


    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            /**
             * 登录
             */
            case R.id.btn_login:
                final String password = et_pwd.getText().toString().trim();
                if (TextUtils.isEmpty(userName)) {
                    MyUtils.showToast(act, "用户名不能为空");
//				et_username.setError("用户名不能为空");
                    return;
                } else if (TextUtils.isEmpty(password)) {
                    MyUtils.showToast(act, "密码不能为空");
//				et_pwd.setError("密码不能为空");
                    return;
                }

                login(userName, password);

                break;
            /**
             * 切换用户
             */
            case R.id.tv_change:
                Intent intent = new Intent(act, LoginActivity.class);
                act.startActivity(intent);
                break;
        }
    }

    /**
     * 方法 登录
     */
    private void login(final String username, final String password) {
        sp.edit().putString("username", username).apply();
        sp.edit().putString("password", password).apply();
        XMPPConnectionUtils.initXMPPConnection();
        connection = MyApp.connection;
        pd = new MyDialog(act);
        pd.show();
        ThreadUtil.runOnBackThread(new Runnable() {

            @Override
            public void run() {
                MyLog.showLog(Thread.currentThread().getName() + "正在执行。。。");
                try {
                    if (!connection.isConnected()) {
                        connection.connect();
                    }
                    connection.setPacketReplyTimeout(60 * 1000);
                    connection.login(username, password);
                    MyApp.username = username;

                    service = new Intent(act, IMService.class);
                    act.startService(service);

                    Intent intent = new Intent(act, MainActivity.class);
                    act.startActivity(intent);
                    finish();

                    handler.sendEmptyMessage(LOGIN_SUCCESS);

                } catch (SmackException e) {
                    handler.sendEmptyMessage(INTERNET_ERROR);
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XMPPException e) {
                    if (e.getMessage().contains("not-authorized")) {
                        // 未注册异常
                        handler.sendEmptyMessage(FAIL_UNKNOWN_USER);
                    } else if (e.getMessage().contains("bad-auth")) {
                        // 密码错误异常
                        handler.sendEmptyMessage(FAIL_PASSWORD_ERROR);
                    } else {
                        handler.sendEmptyMessage(LOGIN_FAIL);
                    }
                    e.printStackTrace();
                }
            }
        });
    }

    private void pdDismiss() {
        if (pd != null && pd.isShowing() && act != null) {
            pd.dismiss();
        }
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            pdDismiss();
            switch (msg.what) {
                case LOGIN_SUCCESS:
                    MyUtils.showToast(act, "登录成功");
                    break;
                case INTERNET_ERROR:
                    MyUtils.showToast(act, "网络错误，请检查您的网络");
                    break;
                case FAIL_UNKNOWN_USER:
                    MyUtils.showToast(act, "用户未注册");
                    break;
                case FAIL_PASSWORD_ERROR:
                    MyUtils.showToast(act, "密码错误");
                    break;
                case LOGIN_FAIL:
                    MyUtils.showToast(act, "登录失败");
                    break;
                case QUERY_SUCCESS:
                    tv_nick.setText(vCardBean.getNick());
                    if (vCardBean.getAvatar() != null){
                        iv_avatar.setTag(-3);
                        bitmapUtils.display(iv_avatar, vCardBean.getAvatar());
                    } else {
                        iv_avatar.setImageResource(R.mipmap.ic_launcher);
                    }

                    break;
                case QUERY_FAIL:
                    startActivity(new Intent(act,LoginActivity.class));
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }
}
