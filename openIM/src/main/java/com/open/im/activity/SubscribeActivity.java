package com.open.im.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.bean.SubBean;
import com.open.im.db.ChatDao;
import com.open.im.utils.MyLog;
import com.open.im.utils.ThreadUtil;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Presence;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/3/24.
 */
public class SubscribeActivity extends Activity {
    private SubscribeActivity act;
    private ChatDao chatDao;
    private ArrayList<SubBean> subBeans;
    private final int QUERY_SUCCESS = 100;
    private ListView lv_subscribe;
    private ImageButton ib_back;
    private MyAdapter adapter;
    private AbstractXMPPConnection connection;

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
        ib_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initData() {
        chatDao = ChatDao.getInstance(act);
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

    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return subBeans.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder vh;
            if (convertView == null){
                vh = new ViewHolder();
                convertView = View.inflate(act,R.layout.list_item_sub,null);
                vh.avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
                vh.name = (TextView) convertView.findViewById(R.id.tv_name);
                vh.reason = (TextView) convertView.findViewById(R.id.tv_reason);
                vh.state = (TextView) convertView.findViewById(R.id.tv_state);
                vh.accept = (Button) convertView.findViewById(R.id.btn_accept);
                convertView.setTag(vh);
            } else{
                vh = (ViewHolder) convertView.getTag();
            }

            SubBean subBean = subBeans.get(position);
            final String subFrom = subBean.getFrom();
            String from = subFrom.substring(0, subFrom.indexOf("@"));
            vh.name.setText(from);
            String state = subBean.getState();
            if("1".equals(state)) {  //1表示同意
                vh.accept.setVisibility(View.GONE);
                vh.state.setVisibility(View.VISIBLE);
            } else {  // 0 未处理  2 拒绝(目前没设置拒绝)
                vh.accept.setVisibility(View.VISIBLE);
                vh.state.setVisibility(View.GONE);
            }

            vh.accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Presence response = new Presence(Presence.Type.subscribed);
                        response.setTo(subFrom);
                        connection.sendStanza(response);
                        chatDao.updateSub(subFrom, "1");
                        vh.accept.setVisibility(View.GONE);
                        vh.state.setVisibility(View.VISIBLE);
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                    MyLog.showLog("同意");
                }
            });

            return convertView;
        }
    }

    private class ViewHolder{
        ImageView avatar;
        TextView name;
        TextView reason;
        TextView state;
        Button accept;
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case QUERY_SUCCESS:
                    if (adapter == null){
                        adapter = new MyAdapter();
                    }
                    lv_subscribe.setAdapter(adapter);
                    break;
            }
        }
    };
}
