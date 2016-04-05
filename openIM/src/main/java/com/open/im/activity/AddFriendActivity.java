package com.open.im.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.bean.VCardBean;
import com.open.im.db.ChatDao;
import com.open.im.utils.MyBitmapUtils;
import com.open.im.utils.MyUserSearchUtils;
import com.open.im.utils.MyUtils;
import com.open.im.utils.MyVCardUtils;
import com.open.im.utils.ThreadUtil;

import org.jivesoftware.smackx.search.ReportedData.Row;

import java.util.ArrayList;
import java.util.List;

public class AddFriendActivity extends Activity {

    private static final int QUERY_SUCCESS = 1000;
    private Button btn_search;
    private ImageButton ib_back;
    private EditText et_search_key;
    private AddFriendActivity act;
    private String friendJid;
    private ListView ll_search_list;
    private List<String> friendJids;
    private List<VCardBean> list;
    private ChatDao chatDao;
    private MyBitmapUtils bitmapUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        //初始化操作
        init();

        //点击事件注册监听
        register();
    }

    /**
     * 点击事件监听
     */
    private void register() {

        ib_back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_search.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                final String searchKey = et_search_key.getText().toString().trim();
                if (TextUtils.isEmpty(searchKey)) {
                    MyUtils.showToast(act, "用户名不能为空");
                    return;
                }
                ThreadUtil.runOnBackThread(new Runnable() {
                    @Override
                    public void run() {
                        List<Row> searchUsers = MyUserSearchUtils.searchUsers(searchKey);
                        if (searchUsers == null || searchUsers.size() == 0) {
                            MyUtils.showToast(act, "没有找到好友");
                            return;
                        }
                        for (Row row : searchUsers) {
                            friendJids = row.getValues("jid");
                        }
                        if (friendJids != null && friendJids.size() != 0) {
                            list = new ArrayList<VCardBean>();
                            for (String friendJid : friendJids) {
                                VCardBean vCardBean = chatDao.queryVCard(friendJid);
                                if (vCardBean == null) {
                                    vCardBean = MyVCardUtils.queryVcard(friendJid);
                                    vCardBean.setJid(friendJid);
                                    chatDao.replaceVCard(vCardBean);
                                }
                                list.add(vCardBean);
                            }
                            handler.sendEmptyMessage(QUERY_SUCCESS);
                        }
                    }
                });
            }
        });
    }


    /**
     * 初始化操作
     */
    private void init() {
        act = this;

        btn_search = (Button) findViewById(R.id.btn_search);
        ib_back = (ImageButton) findViewById(R.id.ib_back);
        et_search_key = (EditText) findViewById(R.id.et_search_key);
        ll_search_list = (ListView) findViewById(R.id.ll_search_list);
        ll_search_list.setVisibility(View.GONE);

        chatDao = ChatDao.getInstance(act);
        bitmapUtils = new MyBitmapUtils(act);
    }

    private MyAdapter mAdapter;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case QUERY_SUCCESS:
                    ll_search_list.setVisibility(View.VISIBLE);
                    if (mAdapter == null) {
                        mAdapter = new MyAdapter();
                    }
                    ll_search_list.setAdapter(mAdapter);
                    ll_search_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            friendJid = friendJids.get(position);
                            Intent intent = new Intent(act, FriendInfoActivity.class);
                            intent.putExtra("friendJid", friendJid);
                            intent.putExtra("type", 1);
                            startActivity(intent);
//                            finish();
                        }
                    });
                    break;
            }
        }
    };

    private class ViewHolder {
        TextView tv_title;
        TextView tv_msg;
        ImageView iv_icon;
    }

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return friendJids.size();
        }

        @Override
        public Object getItem(int position) {
            return friendJids.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh;
            if (convertView == null) {
                vh = new ViewHolder();
                convertView = View.inflate(act, R.layout.list_item_news, null);
                vh.iv_icon = (ImageView) convertView.findViewById(R.id.iv_icon);
                vh.tv_title = (TextView) convertView.findViewById(R.id.tv_title);
                vh.tv_msg = (TextView) convertView.findViewById(R.id.tv_msg);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            VCardBean vCardBean = list.get(position);
            vh.tv_title.setText(vCardBean.getNickName());
            String avatarUrl = vCardBean.getAvatarUrl();
            if (avatarUrl != null) {
                vh.iv_icon.setTag(0);
                bitmapUtils.display(vh.iv_icon, avatarUrl);
            } else {
                vh.iv_icon.setImageResource(R.mipmap.wechat_icon);
            }
            vh.tv_msg.setText(vCardBean.getDesc());
            return convertView;
        }
    }
}
