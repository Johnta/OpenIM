package com.open.im.pager;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.activity.ClientActivity;
import com.open.im.activity.MainActivity;
import com.open.im.activity.SettingActivity;
import com.open.im.activity.UserInfoActivity;
import com.open.im.app.MyApp;
import com.open.im.bean.VCardBean;
import com.open.im.db.OpenIMDao;
import com.open.im.utils.MyBitmapUtils;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyVCardUtils;
import com.open.im.utils.ThreadUtil;

public class SelfPager extends BasePager implements View.OnClickListener {

    private static final int QUERY_SUCCESS = 100;
    private TextView tv_username, tv_desc;
    private ImageView iv_avatar;
    private VCardBean vCardBean;
    //    private ChatDao chatDao;
    private MyBitmapUtils bitmapUtils;
    private RelativeLayout rl_setting;
    private RelativeLayout rl_client;
    private RelativeLayout rl_self;
    private MainActivity act;
    private OpenIMDao openIMDao;

    public SelfPager(Context ctx) {
        super(ctx);
        act = (MainActivity) ctx;
    }

    @Override
    public View initView() {
        View view = View.inflate(ctx, R.layout.pager_im_self, null);
        tv_username = (TextView) view.findViewById(R.id.tv_username);
        tv_desc = (TextView) view.findViewById(R.id.tv_desc);
        iv_avatar = (ImageView) view.findViewById(R.id.iv_avatar);
        rl_setting = (RelativeLayout) view.findViewById(R.id.rl_setting);
        rl_client = (RelativeLayout) view.findViewById(R.id.rl_client);
        rl_self = (RelativeLayout) view.findViewById(R.id.rl_self);
//        tv_sex = (TextView) view.findViewById(R.id.tv_sex);
//        tv_bday = (TextView) view.findViewById(R.id.tv_bday);
//        tv_phone = (TextView) view.findViewById(R.id.tv_phone);


        return view;
    }

    @Override
    public void initData() {
        bitmapUtils = new MyBitmapUtils(ctx);
//        chatDao = ChatDao.getInstance(ctx);
        openIMDao = OpenIMDao.getInstance(ctx);

//        //测试Vcard数据库
//        ThreadUtil.runOnBackThread(new Runnable() {
//            @Override
//            public void run() {
//                chatDao.getAllVCard();
//            }
//        });
        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                String userJid = MyApp.username + "@" + MyConstance.SERVICE_HOST;
//                vCardBean = chatDao.queryVCard(userJid);
                vCardBean = openIMDao.findSingleVCard(userJid);
                if (vCardBean == null) {
                    vCardBean = MyVCardUtils.queryVcard(userJid);
                    vCardBean.setJid(userJid);
                    openIMDao.saveSingleVCard(vCardBean);
//                    chatDao.replaceVCard(vCardBean);
                }
                handler.sendEmptyMessage(QUERY_SUCCESS);
            }
        });

        register();
    }

    /**
     * 注册条目点击事件
     */
    private void register() {
        rl_self.setOnClickListener(this);
        rl_setting.setOnClickListener(this);
        rl_client.setOnClickListener(this);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case QUERY_SUCCESS:
                    if (vCardBean.getNick() != null) {
                        tv_username.setText(vCardBean.getNick());
                    } else {
                        tv_username.setText(MyApp.username);
                    }
                    String avatarUrl = vCardBean.getAvatar();
                    if (avatarUrl != null) {
                        iv_avatar.setTag(0);
                        bitmapUtils.display(iv_avatar, avatarUrl);
                    } else {
                        iv_avatar.setImageResource(R.mipmap.ic_launcher);
                    }
//                    tv_sex.setText(vCardBean.getSex());
                    tv_desc.setText(vCardBean.getDesc());
//                    tv_bday.setText(vCardBean.getBday());
//                    tv_phone.setText(vCardBean.getPhone());

//                    byte[] avatar = vCardBean.getAvatar();
//                    if (avatar != null){
//                        Bitmap bitmap = BitmapFactory.decodeByteArray(avatar,0,avatar.length);
//                        iv_avatar.setImageBitmap(bitmap);
//                    } else {
//                        iv_avatar.setImageResource(R.mipmap.wechat_icon);
//                    }
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_self:
                act.startActivity(new Intent(act, UserInfoActivity.class));
                break;
            case R.id.rl_setting:
                act.startActivity(new Intent(act, SettingActivity.class));
                break;
            case R.id.rl_client:
                act.startActivity(new Intent(act, ClientActivity.class));
                break;
        }
    }
}
