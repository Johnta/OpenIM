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
import com.open.im.utils.MyUtils;
import com.open.im.view.ClearEditText;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.iqregister.AccountManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 修改密码界面
 * Created by Administrator on 2016/3/22.
 */
public class UpdatePasswordActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.et_pwd_old)
    ClearEditText etPwdOld;
    @BindView(R.id.iv_lock_1)
    ImageView ivLock1;
    @BindView(R.id.et_pwd1)
    ClearEditText etPwd1;
    @BindView(R.id.iv_lock_2)
    ImageView ivLock2;

    private UpdatePasswordActivity act;
    private boolean showPwd1, showPwd2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_pwd);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        act = this;
        showPwd1 = false;
        showPwd2 = false;
    }

    @OnClick({R.id.ib_back, R.id.tv_back, R.id.iv_lock_1, R.id.iv_lock_2, R.id.btn_save})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_back:
            case R.id.tv_back:
                finish();
                break;
            case R.id.btn_save:
                String pwd_old = etPwdOld.getText().toString().trim();
                String pwd1 = etPwd1.getText().toString().trim();

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
                    accountManager.changePassword(pwd1);
                    MyUtils.showToast(act, "修改密码成功");
                    finish();
                } catch (SmackException.NoResponseException | SmackException.NotConnectedException | XMPPException.XMPPErrorException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.iv_lock_1:
                if (showPwd1) {  // 隐藏
                    etPwdOld.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivLock1.setImageResource(R.mipmap.login_lock);
                    showPwd1 = false;
                } else {  // 显示
                    etPwdOld.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivLock1.setImageResource(R.mipmap.login_unlock);
                    showPwd1 = true;
                }
                break;
            case R.id.iv_lock_2:
                if (showPwd2) {  // 隐藏
                    etPwd1.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    ivLock2.setImageResource(R.mipmap.login_lock);
                    showPwd2 = false;
                } else {  // 显示
                    etPwd1.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    ivLock2.setImageResource(R.mipmap.login_unlock);
                    showPwd2 = true;
                }
                break;
        }
    }
}
