package com.open.im.activity;


import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyUtils;
import com.open.im.view.ClearEditText;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.iqregister.AccountManager;

import java.util.Set;

/**
 * 修改密码界面
 * Created by Administrator on 2016/3/22.
 */
public class UpdatePasswordActivity extends BaseActivity implements View.OnClickListener {

    private ImageButton ib_back;
    private ClearEditText et_pwd_old, et_pwd1;
    private Button btn_save;
    private UpdatePasswordActivity act;
    private ImageView iv_lock_1;
    private ImageView iv_lock_2;
    private boolean showPwd1, showPwd2;
    private TextView tv_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_pwd);

        initView();

        initData();
    }

    private void initData() {
        ib_back.setOnClickListener(this);
        btn_save.setOnClickListener(this);
        iv_lock_1.setOnClickListener(this);
        iv_lock_2.setOnClickListener(this);
        tv_back.setOnClickListener(this);
    }

    private void initView() {

        act = this;
        ib_back = (ImageButton) findViewById(R.id.ib_back);
        et_pwd_old = (ClearEditText) findViewById(R.id.et_pwd_old);
        et_pwd1 = (ClearEditText) findViewById(R.id.et_pwd1);
        btn_save = (Button) findViewById(R.id.btn_save);
        iv_lock_1 = (ImageView) findViewById(R.id.iv_lock_1);
        iv_lock_2 = (ImageView) findViewById(R.id.iv_lock_2);
        tv_back = (TextView) findViewById(R.id.tv_back);

        showPwd1 = false;
        showPwd2 = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_back:
            case R.id.tv_back:
                finish();
                break;
            case R.id.btn_save:
                String pwd_old = et_pwd_old.getText().toString().trim();
                String pwd1 = et_pwd1.getText().toString().trim();

                if (TextUtils.isEmpty(pwd_old)) {
                    MyUtils.showToast(act, "请输入原始密码");
                    return;
                } else if (TextUtils.isEmpty(pwd1)) {
                    MyUtils.showToast(act, "请输入新密码");
                    return;
                } else if (pwd1.length() < 6) {
                    MyUtils.showToast(act, "密码长度必须大于等于6位");
                    return;
                }

                XMPPTCPConnection connection = MyApp.connection;
                AccountManager accountManager = AccountManager.getInstance(connection);
                try {
                    Set<String> accountAttributes = accountManager.getAccountAttributes();
                    String password = accountManager.getAccountAttribute("password");
                    MyLog.showLog("password::" + password);
                    for (String str : accountAttributes
                            ) {
                        MyLog.showLog("str::" + str);
                    }
                    accountManager.changePassword(pwd1);
                    MyUtils.showToast(act, "修改密码成功");
                    finish();
                } catch (SmackException.NoResponseException e) {
                    e.printStackTrace();
                } catch (XMPPException.XMPPErrorException e) {
                    e.printStackTrace();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.iv_lock_1:
                if (showPwd1) {  // 隐藏
                    et_pwd_old.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    iv_lock_1.setImageResource(R.mipmap.login_lock);
                    showPwd1 = false;
                } else {  // 显示
                    et_pwd_old.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    iv_lock_1.setImageResource(R.mipmap.login_unlock);
                    showPwd1 = true;
                }
                break;
            case R.id.iv_lock_2:
                if (showPwd2) {  // 隐藏
                    et_pwd1.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    iv_lock_2.setImageResource(R.mipmap.login_lock);
                    showPwd2 = false;
                } else {  // 显示
                    et_pwd1.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    iv_lock_2.setImageResource(R.mipmap.login_unlock);
                    showPwd2 = true;
                }
                break;
        }
    }
}
