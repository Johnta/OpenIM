package com.open.im.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
import com.open.im.db.OpenIMDao;
import com.open.im.utils.MyAnimationUtils;
import com.open.im.utils.MyBitmapUtils;
import com.open.im.utils.ThreadUtil;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.util.List;

/**
 * 好友申请列表页面
 * Created by Administrator on 2016/3/24.
 */
public class SubscribeActivity extends Activity implements View.OnClickListener {
    private SubscribeActivity act;
//    private ChatDao chatDao;
    private List<SubBean> subBeans;
    private final int QUERY_SUCCESS = 100;
    private ListView lv_subscribe;
    private ImageButton ib_back;
    private MyAdapter adapter;
    private XMPPTCPConnection connection;
    private MyBitmapUtils bitmapUtils;
    private ImageView iv_minus;
    private OpenIMDao openIMDao;

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
                String type = bean.getState();
                Intent intent = new Intent(act, UserInfoActivity.class);
                if ("0".equals(type)) {   //0 表示收到请求
                    intent.putExtra("type", 3);
                    intent.putExtra("friendJid", bean.getFromUser());
                } else if ("3".equals(type)) {  // 3表示发出的请求
                    intent.putExtra("type", 4);
                    intent.putExtra("friendJid", bean.getToUser());
                } else if ("1".equals(type)) {  // 1表示 同意添加对方为好友
                    intent.putExtra("type", 2);
                    intent.putExtra("friendJid", bean.getFromUser());
                } else if ("4".equals(type)) {   // 4表示 对方同意添加自己为好友
                    intent.putExtra("type", 2);
                    intent.putExtra("friendJid", bean.getToUser());
                } else if ("2".equals(type)){
                    intent.putExtra("type", 1); //  2表示已拒绝对方请求
                    intent.putExtra("friendJid", bean.getFromUser());
                } else if ("5".equals(type)){  // 5表示对方已拒绝
                    intent.putExtra("type", 1);
                    intent.putExtra("friendJid", bean.getToUser());
                }
                act.startActivity(intent);
                finish();
            }
        });
    }

    private void initData() {
//        chatDao = ChatDao.getInstance(act);
        openIMDao = OpenIMDao.getInstance(act);
        bitmapUtils = new MyBitmapUtils(act);
        connection = MyApp.connection;
        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {
//                subBeans = chatDao.querySub(MyApp.username, 0);
                subBeans = openIMDao.findSubByOwner(MyApp.username,15,0);
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
        switch (v.getId()) {
            case R.id.ib_back:
                finish();
                break;
            case R.id.iv_minus:
                // 旋转180度 不保存状态 补间动画
                MyAnimationUtils.rotate(iv_minus);
//                chatDao.deleteAllSub();
                subBeans.clear();
                openIMDao.deleteSubByOwner(MyApp.username);
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
            if (subBean.getAvatar() != null) {
                bitmapUtils.display(vh.avatar, subBean.getAvatar());
            } else {
                vh.avatar.setImageResource(R.mipmap.ic_launcher);
            }
            String state = subBean.getState();
            if ("0".equals(state)) {  //0 表示收到请求
                vh.state.setText("[陌生人请求]");
                vh.state.setTextColor(Color.RED);
            } else if ("1".equals(state)) {  // 1 表示已同意对方申请
                vh.state.setText("[已同意对方申请]");
                vh.state.setTextColor(Color.GREEN);
            } else if ("2".equals(state)) {  // 2 表示拒绝对方申请
                vh.state.setText("[已拒绝对方申请]");
                vh.state.setTextColor(Color.RED);
            } else if ("3".equals(state)) {  // 3表示添加新朋友
                vh.state.setText("[添加新朋友]");
                vh.state.setTextColor(Color.GREEN);
            } else if ("4".equals(state)) {  // 4表示对方已同意申请
                vh.state.setText("[对方已同意申请]");
                vh.state.setTextColor(Color.GREEN);
            } else if ("5".equals(state)) {  // 5表示对方已拒绝申请
                vh.state.setText("[对方已拒绝申请]");
                vh.state.setTextColor(Color.RED);
            }

//            vh.accept.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    try {
//                        Presence response = new Presence(Presence.Type.subscribed);
//                        response.setToUser(subFrom);
//                        connection.sendStanza(response);
//                        chatDao.updateSubFrom(subFrom, "1");
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
