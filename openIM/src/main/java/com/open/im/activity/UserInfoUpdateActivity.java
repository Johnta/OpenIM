package com.open.im.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.utils.MyUtils;
import com.open.im.view.ClearEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserInfoUpdateActivity extends BaseActivity implements OnClickListener {

    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.et_info)
    ClearEditText etInfo;

    private UserInfoUpdateActivity act;
    private Intent intent;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo_update);
        act = this;
        ButterKnife.bind(this);
        // 初始化数据
        initData();
    }

    private void initData() {
        intent = getIntent();
        type = intent.getIntExtra("type", -1);
        String info = intent.getStringExtra("info");
        switch (type) {
            case 2:
                tvTitle.setText("昵称");
                if (info != null && !"未填写".equals(info))
                    etInfo.setText(info);
                break;
            case 5:
                tvTitle.setText("地址");
                if (info != null && !"未填写".equals(info))
                    etInfo.setText(info);
                break;
            case 6:
                tvTitle.setText("邮箱");
                if (info != null && !"未填写".equals(info))
                    etInfo.setText(info);
                break;
            case 7:
                tvTitle.setText("电话");
                if (info != null && !"未填写".equals(info))
                    etInfo.setText(info);
                break;
            case 8:
                tvTitle.setText("签名");
                if (info != null && !"未填写".equals(info))
                    etInfo.setText(info);
                break;

            default:
                break;
        }

    }

    @OnClick({R.id.tv_cancel, R.id.tv_save})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tv_cancel) { // 取消按钮点击事件
            finish();
        } else if (id == R.id.tv_save) { // 保存按钮点击事件
            String info = etInfo.getText().toString().trim();
            if (TextUtils.isEmpty(info)) {
                MyUtils.showToast(act, "请填写正确的信息内容");
            } else {
                intent.setData(Uri.parse(info));
                setResult(type, intent);
                finish();
            }
        }
    }
}
