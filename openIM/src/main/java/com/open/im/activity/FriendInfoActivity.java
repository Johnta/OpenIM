package com.open.im.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.bean.VCardBean;
import com.open.im.db.ChatDao;
import com.open.im.utils.MyUtils;
import com.open.im.utils.MyVCardUtils;
import com.open.im.utils.ThreadUtil;
import com.open.im.view.ActionItem;
import com.open.im.view.TitlePopup;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;

/**
 * Created by Administrator on 2016/3/21.
 */
public class FriendInfoActivity extends Activity implements View.OnClickListener {
    private static final int QUERY_SUCCESS = 100;
    private TextView tv_username, tv_sex, tv_bday, tv_phone, tv_desc;
    private ImageView iv_avatar;
    private RelativeLayout rl_title;
    private ImageView iv_more;
    private ImageButton ib_back;
    private FriendInfoActivity act;
    private TitlePopup friendPopup;
    private AbstractXMPPConnection connection;
    private String friendJid;
    private String friendName;
    private ChatDao chatDao;
    private VCardBean vCardBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pager_im_setting);

        initView();

        initData();
    }

    private void initData() {

        chatDao = ChatDao.getInstance(act);

        Intent intent = getIntent();
        friendJid = intent.getStringExtra("friendJid");
        friendName = friendJid.substring(0, friendJid.indexOf("@"));
        int type = intent.getIntExtra("type", 0);

        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                vCardBean = chatDao.queryVCard(friendJid);
                if (vCardBean == null) {
                    vCardBean = MyVCardUtils.queryVcard(friendJid);
                    vCardBean.setJid(friendJid);
                    chatDao.replaceVCard(vCardBean);
                }
                handler.sendEmptyMessage(QUERY_SUCCESS);
            }
        });

//        VCardBean vCardBean = MyVCardUtils.queryVcard(friendJid);


        if (type == 1) {
            friendPopup.addAction(new ActionItem(act, "刷新", R.mipmap.mm_title_btn_compose_normal));
            friendPopup.addAction(new ActionItem(act, "加为朋友", R.mipmap.mm_title_btn_receiver_normal));
        } else if (type == 2) {
            friendPopup.addAction(new ActionItem(act, "刷新", R.mipmap.mm_title_btn_compose_normal));
            friendPopup.addAction(new ActionItem(act, "清除记录", R.mipmap.mm_title_btn_receiver_normal));
            friendPopup.addAction(new ActionItem(act, "删除朋友", R.mipmap.mm_title_btn_keyboard_normal));
        }


        iv_more.setOnClickListener(this);
        ib_back.setOnClickListener(this);

        friendPopup.setItemOnClickListener(new TitlePopup.OnItemOnClickListener() {
            @Override
            public void onItemClick(ActionItem item, int position) {
                if ("加为朋友".equals(item.mTitle)) {
                    showAddDialog();
                } else if ("刷新".equals(item.mTitle)) {
                    ThreadUtil.runOnBackThread(new Runnable() {
                        @Override
                        public void run() {
                            vCardBean = MyVCardUtils.queryVcard(friendJid);
                            vCardBean.setJid(friendJid);
                            chatDao.replaceVCard(vCardBean);
                            handler.sendEmptyMessage(QUERY_SUCCESS);
                        }
                    });
                } else if ("清除记录".equals(item.mTitle)) {
                    chatDao.deleteMsgByMark(friendName + "#" + MyApp.username);
                    MyUtils.showToast(act, "删除好友聊天记录成功");
                    finish();
                } else if ("删除朋友".equals(item.mTitle)) {
                    chatDao.deleteMsgByMark(friendName + "#" + MyApp.username);
                    Roster roster = Roster.getInstanceFor(connection);
                    RosterEntry entry = roster.getEntry(friendJid);
                    try {
                        if (entry != null) {
                            roster.removeEntry(entry);
                            MyUtils.showToast(act, "删除好友成功");
                        }
                        finish();
                    } catch (SmackException.NotLoggedInException e) {
                        e.printStackTrace();
                    } catch (SmackException.NoResponseException e) {
                        e.printStackTrace();
                    } catch (XMPPException.XMPPErrorException e) {
                        e.printStackTrace();
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setMessage("添加为好友？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    /**
                     * 添加好友不再是直接创建好友了，而是先发出一个订阅请求，对方同意后，才创建好友
                     */
                    Presence presence = new Presence(Presence.Type.subscribe);
                    presence.setTo(friendJid);
                    //在此处可以设置请求好友时发送的验证信息
                    presence.setStatus("您好，我是...");
                    connection.sendStanza(presence);
                    finish();
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }


        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    private void initView() {
        act = this;
        connection = MyApp.connection;
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
        switch (v.getId()) {
            case R.id.iv_more:
                friendPopup.show(v);
                break;
            case R.id.ib_back:
                finish();
                break;
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case QUERY_SUCCESS:
                    tv_username.setText(vCardBean.getNickName());
                    tv_desc.setText(vCardBean.getDesc());
                    tv_bday.setText(vCardBean.getBday());
                    tv_sex.setText(vCardBean.getSex());
                    tv_phone.setText(vCardBean.getPhone());

//                    byte[] avatar = vCardBean.getAvatar();
//                    if (avatar != null){
//                        Bitmap bitmap = BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
//                        iv_avatar.setImageBitmap(bitmap);
//                    } else {
//                        iv_avatar.setImageResource(R.mipmap.wechat_icon);
//                    }

                    if (vCardBean.getBitmap() != null) {
                        iv_avatar.setImageBitmap(vCardBean.getBitmap());
                    } else {
                        iv_avatar.setImageResource(R.mipmap.wechat_icon);
                    }
                    break;
            }
        }
    };
}
