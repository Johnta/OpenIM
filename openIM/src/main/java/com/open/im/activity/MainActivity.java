package com.open.im.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.db.OpenIMDao;
import com.open.im.pager.BasePager;
import com.open.im.pager.ContactPager;
import com.open.im.pager.NewsPager;
import com.open.im.pager.SelfPager;
import com.open.im.utils.MyAnimationUtils;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyNetUtils;
import com.open.im.utils.MyUtils;
import com.open.im.view.MyViewPager;

import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity implements OnClickListener {

    private static final int CONNECTING = 100;
    private static final int CONNECTION_SUCCESS = 101;
    private MyViewPager viewPager;
    private ImageButton ib_news, ib_contact, ib_setting;
    private MainActivity act;
    private MyAdapter adapter;
    private List<BasePager> pagers;
    private int lastPosition = 0;
    private TextView tv_title;
    private ImageView iv_add;
    private ImageView iv_minus;
    private LinearLayout ll_net;
    private BroadcastReceiver netReceiver;
    private ConnectionListener connectionListener;
    private XMPPTCPConnection connection;
    private RelativeLayout rl_state;
    private ImageView iv_loading;
    private AnimationDrawable an;
    private OpenIMDao openIMDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        act = this;

//        MyApp.addActivity(this);

        initView();

        initData();

        register();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case 201:  // 个人信息界面有修改返回
                if (lastPosition == 3) {
                    SelfPager selfPager = (SelfPager) pagers.get(2);
                    selfPager.queryInfo();
                    MyLog.showLog("个人信息修改");
                }
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (lastPosition == 1) {
            ContactPager contactPager = (ContactPager) pagers.get(1);
            contactPager.queryFriends();
        }
    }

    /**
     * 注册点击监听
     */
    private void register() {
        ib_news.setOnClickListener(this);
        ib_contact.setOnClickListener(this);
        ib_setting.setOnClickListener(this);

        /**
         * 注册网络连接监听
         */
        netReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                    boolean isConnected = MyNetUtils.isNetworkConnected(context);
                    if (isConnected) {
                        ll_net.setVisibility(View.GONE);
                        tv_title.setVisibility(View.VISIBLE);
                        rl_state.setVisibility(View.GONE);
                        an.stop();
                    } else {
                        ll_net.setVisibility(View.VISIBLE);
                        tv_title.setVisibility(View.GONE);
                        rl_state.setVisibility(View.VISIBLE);
                        an.start();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netReceiver, filter);
        //  连接状态监听
        if (connection != null) {
            connectionListener = new ConnectionListener() {
                @Override
                public void connected(XMPPConnection connection) {

                }

                @Override
                public void authenticated(XMPPConnection connection, boolean resumed) {

                }

                @Override
                public void connectionClosed() {

                }

                @Override
                public void connectionClosedOnError(Exception e) {

                }

                @Override
                public void reconnectionSuccessful() {
                    handler.sendEmptyMessage(CONNECTION_SUCCESS);
                    MyLog.showLog("主界面连接成功");
                }

                @Override
                public void reconnectingIn(int seconds) {
                    if (connection != null && !connection.isConnected()) {
                        handler.sendEmptyMessage(CONNECTING);
                    }
                    MyLog.showLog("当前线程::" + Thread.currentThread().getName());
                    MyLog.showLog("connectionState::" + connection.isConnected());
                }

                @Override
                public void reconnectionFailed(Exception e) {

                }
            };
            connection.addConnectionListener(connectionListener);
        }
    }

    /**
     * 初始化数据
     */
    private void initData() {

        connection = MyApp.connection;

        openIMDao = OpenIMDao.getInstance(act);

        pagers = new ArrayList<BasePager>();
        pagers.add(new NewsPager(act));
        pagers.add(new ContactPager(act));
        pagers.add(new SelfPager(act));

        adapter = new MyAdapter();
        viewPager.setAdapter(adapter);

        Intent intent = getIntent();
        if (intent != null) {
            int selection = intent.getIntExtra("selection", 0);
            if (selection == 3) {
                ib_setting.setEnabled(false);
                tv_title.setText("自己");
                iv_add.setVisibility(View.GONE);
                iv_minus.setVisibility(View.GONE);
                viewPager.setCurrentItem(3);
                lastPosition = 3;
            } else {
                ib_news.setEnabled(false);
                tv_title.setText("聊天");
                iv_add.setVisibility(View.GONE);
                iv_minus.setVisibility(View.VISIBLE);
                // 默认显示消息列表页面
                viewPager.setCurrentItem(0);
                lastPosition = 0;
            }
        } else {
            MyUtils.showToast(act, "intent为null");
        }
    }

    /**
     * 初始化控件
     */
    private void initView() {
        viewPager = (MyViewPager) findViewById(R.id.viewPager);
        ib_news = (ImageButton) findViewById(R.id.ib_news);
        ib_contact = (ImageButton) findViewById(R.id.ib_contact);
        ib_setting = (ImageButton) findViewById(R.id.ib_setting);

        tv_title = (TextView) findViewById(R.id.tv_title);
        rl_state = (RelativeLayout) findViewById(R.id.rl_state);
        iv_add = (ImageView) findViewById(R.id.iv_add);
        iv_minus = (ImageView) findViewById(R.id.iv_minus);
        iv_loading = (ImageView) findViewById(R.id.iv_loading);
        an = (AnimationDrawable) iv_loading.getDrawable();

        ll_net = (LinearLayout) findViewById(R.id.ll_net);
        ll_net.setVisibility(View.GONE);

        iv_add.setOnClickListener(this);
        iv_minus.setOnClickListener(this);
    }

    /**
     * viewPager设置的adapter 填充四个自定义pager
     *
     * @author Administrator
     */
    private class MyAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return pagers.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            View view = pagers.get(position).initView();
            pagers.get(position).initData();
            container.addView(view);

            return view;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // super.destroyItem(container, position, object);
            container.removeView((View) object);
        }

    }

    @Override
    /**
     * 处理点击事件
     */
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_news:
                if (0 != lastPosition) {
                    showPager(0, false, true, true);
                    iv_add.setVisibility(View.GONE);
                    iv_minus.setVisibility(View.VISIBLE);
                    rl_state.setVisibility(View.GONE);
                    tv_title.setVisibility(View.VISIBLE);
                    tv_title.setText("聊天");
                }
                break;
            case R.id.ib_contact:
                if (1 != lastPosition) {
                    showPager(1, true, false, true);
                    iv_add.setVisibility(View.VISIBLE);
                    iv_minus.setVisibility(View.GONE);
                    rl_state.setVisibility(View.GONE);
                    tv_title.setVisibility(View.VISIBLE);
                    tv_title.setText("朋友");
                }
                break;
            case R.id.ib_setting:
                if (3 != lastPosition) {
                    showPager(3, true, true, false);
                    iv_add.setVisibility(View.GONE);
                    iv_minus.setVisibility(View.GONE);
                    rl_state.setVisibility(View.GONE);
                    tv_title.setVisibility(View.VISIBLE);
                    tv_title.setText("自己");
                }
                break;
            case R.id.iv_add:
                act.startActivity(new Intent(act, AddFriendActivity.class));
                break;
            case R.id.iv_minus:
                MyAnimationUtils.rotate(iv_minus);
                openIMDao.deleteMessageByOwner(MyApp.username);
                break;
        }
    }

    /**
     * 根据点击位置 设置显示的pager
     *
     * @param item
     * @param b1
     * @param b2
     * @param b3
     */
    private void showPager(int item, boolean b1, boolean b2, boolean b3) {
        viewPager.setCurrentItem(item);
        lastPosition = item;
        ib_news.setEnabled(b1);
        ib_contact.setEnabled(b2);
        ib_setting.setEnabled(b3);
    }

    @Override
    protected void onDestroy() {
        if (netReceiver != null) {
            unregisterReceiver(netReceiver);
        }
        if (connectionListener != null && connection != null) {
            connection.removeConnectionListener(connectionListener);
        }
        super.onDestroy();
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CONNECTING:  //正在连接
                    tv_title.setVisibility(View.GONE);
                    rl_state.setVisibility(View.VISIBLE);
                    an.start();
                    break;
                case CONNECTION_SUCCESS:  //连接成功
                    tv_title.setVisibility(View.VISIBLE);
                    rl_state.setVisibility(View.GONE);
                    an.stop();
                    break;
            }
        }
    };
}
