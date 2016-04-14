package com.open.im.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.open.im.R;
import com.open.im.utils.MyUtils;

/**
 * Created by Administrator on 2016/4/14.
 */
public class AvatarActivity extends Activity implements View.OnClickListener {

    private ImageButton ib_back;
    private ImageView iv_avatar;
    private ImageView iv_save;
    private ImageView iv_more;
    private AvatarActivity act;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar);
        act = this;

        initView();

        initData();

        register();
    }

    private void initData() {
        String nickName = getIntent().getStringExtra("nickName");
        type = getIntent().getIntExtra("type",0);
        if (type == 0){
            iv_save.setVisibility(View.GONE);
            iv_more.setVisibility(View.VISIBLE);
        } else {
            iv_more.setVisibility(View.GONE);
            iv_save.setVisibility(View.VISIBLE);
        }
    }

    private void register() {
        ib_back.setOnClickListener(this);
        iv_save.setOnClickListener(this);
        iv_more.setOnClickListener(this);
    }

    private void initView() {
        ib_back = (ImageButton) findViewById(R.id.ib_back);
        iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
        iv_save = (ImageView) findViewById(R.id.iv_save);
        iv_more = (ImageView) findViewById(R.id.iv_more);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ib_back:
                finish();
                break;
            case R.id.iv_more:
                MyUtils.showToast(act,"弹出pop");
                break;
            case R.id.iv_save:
                MyUtils.showToast(act,"保存图片");
                break;
        }
    }
}
