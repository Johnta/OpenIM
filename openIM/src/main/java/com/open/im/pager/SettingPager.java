package com.open.im.pager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.bean.VCardBean;
import com.open.im.db.ChatDao;
import com.open.im.utils.MyBitmapUtils;
import com.open.im.utils.MyVCardUtils;
import com.open.im.utils.ThreadUtil;

public class SettingPager extends BasePager  {

    private static final int QUERY_SUCCESS = 100;
    private TextView tv_username, tv_sex, tv_bday, tv_phone,tv_desc;
    private ImageView iv_avatar;
    private VCardBean vCardBean;
    private ChatDao chatDao;
    private MyBitmapUtils bitmapUtils;

    public SettingPager(Context ctx) {
        super(ctx);
    }

    @Override
    public View initView() {
        View view = View.inflate(ctx, R.layout.pager_im_setting, null);
        tv_username = (TextView) view.findViewById(R.id.tv_username);
        tv_sex = (TextView) view.findViewById(R.id.tv_sex);
        tv_bday = (TextView) view.findViewById(R.id.tv_bday);
        tv_phone = (TextView) view.findViewById(R.id.tv_phone);
        tv_desc = (TextView) view.findViewById(R.id.tv_desc);
        iv_avatar = (ImageView) view.findViewById(R.id.iv_avatar);

        return view;
    }

    @Override
    public void initData() {
        bitmapUtils = new MyBitmapUtils(ctx);
        chatDao = ChatDao.getInstance(ctx);
        //测试Vcard数据库
        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                chatDao.getAllVCard();
            }
        });


        tv_username.setText(MyApp.username);
        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                String userJid = MyApp.username + "@" + MyApp.connection.getServiceName();
                vCardBean = chatDao.queryVCard(userJid);
                if (vCardBean == null){
                    vCardBean = MyVCardUtils.queryVcard(userJid);
                    vCardBean.setJid(userJid);
                    chatDao.replaceVCard(vCardBean);
                }
                handler.sendEmptyMessage(QUERY_SUCCESS);
            }
        });
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case QUERY_SUCCESS:
                    String avatarUrl = vCardBean.getAvatarUrl();
                    if (avatarUrl != null){
                        iv_avatar.setTag(0);
                        bitmapUtils.display(iv_avatar,avatarUrl);
                    } else {
                        iv_avatar.setImageResource(R.mipmap.wechat_icon);
                    }
                    tv_sex.setText(vCardBean.getSex());
                    tv_desc.setText(vCardBean.getDesc());
                    tv_bday.setText(vCardBean.getBday());
                    tv_phone.setText(vCardBean.getPhone());

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
}
