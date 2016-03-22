package com.open.im.activity;


import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyUtils;
import com.open.im.view.ClearEditText;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.iqregister.AccountManager;

import java.util.Set;

/**
 * Created by Administrator on 2016/3/22.
 */
public class UpdatePasswordActivity extends Activity implements View.OnClickListener {

    private ImageButton ib_back;
    private ClearEditText et_pwd_old, et_pwd1, et_pwd2;
    private Button btn_save;
    private UpdatePasswordActivity act;

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
    }

    private void initView() {

        act = this;
        ib_back = (ImageButton) findViewById(R.id.ib_back);
        et_pwd_old = (ClearEditText) findViewById(R.id.et_pwd_old);
        et_pwd1 = (ClearEditText) findViewById(R.id.et_pwd1);
        et_pwd2 = (ClearEditText) findViewById(R.id.et_pwd2);
        btn_save = (Button) findViewById(R.id.btn_save);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_back:
                finish();
                break;
            case R.id.btn_save:
                String pwd_old = et_pwd_old.getText().toString().trim();
                String pwd1 = et_pwd1.getText().toString().trim();
                String pwd2 = et_pwd2.getText().toString().trim();

                if (TextUtils.isEmpty(pwd_old)) {
                    MyUtils.showToast(act, "请输入原始密码");
                    return;
                } else if (TextUtils.isEmpty(pwd1)) {
                    MyUtils.showToast(act, "请输入新密码");
                    return;
                } else if (TextUtils.isEmpty(pwd2)) {
                    MyUtils.showToast(act, "请输入确认密码");
                    return;
                } else if (!pwd1.equals(pwd2)) {
                    MyUtils.showToast(act, "确认密码与新密码不相符");
                    return;
                }

                AbstractXMPPConnection connection = MyApp.connection;
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
                    MyUtils.showToast(act,"修改密码成功");
                    finish();
                } catch (SmackException.NoResponseException e) {
                    e.printStackTrace();
                } catch (XMPPException.XMPPErrorException e) {
                    e.printStackTrace();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
