package com.open.im.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.utils.MyUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UserSexUpdateActivity extends BaseActivity implements OnClickListener {

    @BindView(R.id.tv_cancel)
    TextView tvCancel;
    @BindView(R.id.tv_save)
    TextView tvSave;
    @BindView(R.id.rg_sex)
    RadioGroup rgSex;
    @BindView(R.id.tv_title)
    TextView tvTitle;

    private UserSexUpdateActivity act;
    private Intent intent;
    private String sex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usersex_update);
        ButterKnife.bind(this);

        initView();

        initData();

        register();
    }

    private void register() {
        rgSex.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb_male:
                        sex = "男";
                        break;
                    case R.id.rb_female:
                        sex = "女";
                        break;
                    case R.id.rb_secret:
                        sex = "保密";
                        break;
                }
            }
        });
    }

    private void initData() {
        intent = getIntent();
        sex = intent.getStringExtra("sex");
        MyUtils.showToast(act, sex);
        if ("男".equals(sex)) {
            rgSex.check(R.id.rb_male);
        } else if ("女".equals(sex)) {
            rgSex.check(R.id.rb_female);
        } else if ("保密".equals(sex)) {
            rgSex.check(R.id.rb_secret);
        } else {
            rgSex.clearCheck();
        }
    }

    private void initView() {
        act = this;
        tvTitle.setText("性别");
    }

    @OnClick({R.id.tv_cancel, R.id.tv_save})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                finish();
                break;
            case R.id.tv_save:
                if (sex != null) {
                    intent.setData(Uri.parse(sex));
                    setResult(3, intent);
                    finish();
                }
                break;
        }
    }
}
