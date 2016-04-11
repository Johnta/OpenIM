package com.open.im.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.bean.SubBean;
import com.open.im.db.ChatDao;
import com.open.im.utils.MyBitmapUtils;
import com.open.im.utils.MyLog;
import com.open.im.utils.ThreadUtil;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.util.ArrayList;

/**
 * 好友申请列表页面
 * Created by Administrator on 2016/3/24.
 */
public class SubscribeActivity extends Activity implements View.OnClickListener {
    private SubscribeActivity act;
    private ChatDao chatDao;
    private ArrayList<SubBean> subBeans;
    private final int QUERY_SUCCESS = 100;
    private ListView lv_subscribe;
    private ImageButton ib_back;
    private MyAdapter adapter;
    private XMPPTCPConnection connection;
    private MyBitmapUtils bitmapUtils;
    private ImageView iv_minus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_msg);
        act = this;
        initView();

        initData();

        register();
    }

    private void register() {
        ib_back.setOnClickListener(this);
        iv_minus.setOnClickListener(this);
        lv_subscribe.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SubBean bean = (SubBean) adapter.getItem(position);
                MyLog.showLog(bean.toString());
            }
        });
    }

    private void initData() {
        chatDao = ChatDao.getInstance(act);
        bitmapUtils = new MyBitmapUtils(act);
        connection = MyApp.connection;
        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                subBeans = chatDao.querySub(MyApp.username, 0);
                handler.sendEmptyMessage(QUERY_SUCCESS);
            }
        });
    }

    private void initView() {
        ib_back = (ImageButton) findViewById(R.id.ib_back);
        lv_subscribe = (ListView) findViewById(R.id.lv_subscribe);
        iv_minus = (ImageView) findViewById(R.id.iv_minus);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ib_back:
                finish();
                break;
            case R.id.iv_minus:
                chatDao.deleteAllSub();
                subBeans.clear();
                adapter.notifyDataSetChanged();
                break;
        }
    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return subBeans.size();
        }

        @Override
        public Object getItem(int position) {
            return subBeans.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder vh;
            if (convertView == null) {
                vh = new ViewHolder();
                convertView = View.inflate(act, R.layout.list_item_sub, null);
                vh.avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
                vh.avatar.setTag(position);
                vh.name = (TextView) convertView.findViewById(R.id.tv_name);
                vh.state = (TextView) convertView.findViewById(R.id.tv_state);
//                vh.state = (TextView) convertView.findViewById(R.id.tv_state);
//                vh.accept = (Button) convertView.findViewById(R.id.btn_accept);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            SubBean subBean = subBeans.get(position);
            vh.name.setText(subBean.getNick());
            if (subBean.getAvatarUrl() != null){
                bitmapUtils.display(vh.avatar,subBean.getAvatarUrl());
            } else {
                vh.avatar.setImageResource(R.mipmap.ic_launcher);
            }
            String state = subBean.getState();
            if ("0".equals(state)) {  //0 表示收到请求
                vh.state.setText("[陌生人请求]");
            } else if("2".equals(state)) {  // 2表发出的请求
                vh.state.setText("[添加新朋友]");
            }

//            vh.accept.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    try {
//                        Presence response = new Presence(Presence.Type.subscribed);
//                        response.setTo(subFrom);
//                        connection.sendStanza(response);
//                        chatDao.updateSub(subFrom, "1");
//                        vh.accept.setVisibility(View.GONE);
//                        vh.state.setVisibility(View.VISIBLE);
//                    } catch (SmackException.NotConnectedException e) {
//                        e.printStackTrace();
//                    }
//                    MyLog.showLog("同意");
//                }
//            });

            return convertView;
        }
    }

    private class ViewHolder {
        ImageView avatar;
        TextView name;
        TextView state;
//        TextView state;
//        Button accept;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case QUERY_SUCCESS:
                    if (adapter == null) {
                        adapter = new MyAdapter();
                    }
                    lv_subscribe.setAdapter(adapter);
                    break;
            }
        }
    };
}
