package com.open.im.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.bean.VCardBean;
import com.open.im.utils.MyVCardUtils;
import com.open.im.view.ActionItem;
import com.open.im.view.TitlePopup;

/**
 * Created by Administrator on 2016/3/21.
 */
public class FriendInfoActivity extends Activity implements View.OnClickListener {
    private TextView tv_username,tv_sex,tv_bday,tv_phone,tv_desc;
    private ImageView iv_avatar;
    private RelativeLayout rl_title;
    private ImageView iv_more;
    private ImageButton ib_back;
    private FriendInfoActivity act;
    private TitlePopup friendPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager_im_setting);
        
        initView();
        
        initData();
    }

    private void initData() {
        String friendJid = getIntent().getStringExtra("friendJid");
        VCardBean vCardBean = MyVCardUtils.queryVcard(friendJid);

        tv_username.setText(vCardBean.getNickName());
        tv_desc.setText(vCardBean.getDesc());
        tv_bday.setText(vCardBean.getBday());
        tv_sex.setText(vCardBean.getSex());
        tv_phone.setText(vCardBean.getPhone());

        friendPopup.addAction(new ActionItem(act, "刷新", R.mipmap.mm_title_btn_compose_normal));
        friendPopup.addAction(new ActionItem(act, "清除记录", R.mipmap.mm_title_btn_receiver_normal));
        friendPopup.addAction(new ActionItem(act, "删除朋友", R.mipmap.mm_title_btn_keyboard_normal));

        if (vCardBean.getBitmap() != null){
            iv_avatar.setImageBitmap(vCardBean.getBitmap());
        } else {
            iv_avatar.setImageResource(R.mipmap.wechat_icon);
        }

        iv_more.setOnClickListener(this);
        ib_back.setOnClickListener(this);
    }

    private void initView() {
        
        act = this;
        
        tv_username = (TextView) findViewById(R.id.tv_username);
        tv_sex = (TextView) findViewById(R.id.tv_sex);
        tv_bday = (TextView) findViewById(R.id.tv_bday);
        tv_phone = (TextView) findViewById(R.id.tv_phone);
        tv_desc = (TextView) findViewById(R.id.tv_desc);
        iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
        
        rl_title = (RelativeLayout) findViewById(R.id.rl_title);
        rl_title.setVisibility(View.VISIBLE);

        iv_more = (ImageView) findViewById(R.id.iv_more);
        ib_back = (ImageButton) findViewById(R.id.ib_back);

        friendPopup = new TitlePopup(act, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv_more:
                friendPopup.show(v);
                break;
            case R.id.ib_back:
                finish();
                break;
        }
    }
}
