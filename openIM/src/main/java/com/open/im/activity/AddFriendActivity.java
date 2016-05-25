package com.open.im.activity;

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
import com.open.im.db.OpenIMDao;
import com.open.im.utils.MyAnimationUtils;
import com.open.im.utils.MyBitmapUtils;
import com.open.im.utils.MyUserSearchUtils;
import com.open.im.utils.MyUtils;
import com.open.im.utils.MyVCardUtils;
import com.open.im.utils.ThreadUtil;

import org.jivesoftware.smackx.search.ReportedData.Row;

import java.util.ArrayList;
import java.util.List;

public class AddFriendActivity extends BaseActivity implements OnClickListener {

    private static final int QUERY_SUCCESS = 1000;
    private Button btn_search;
    private ImageButton ib_back;
    private EditText et_search_key;
    private AddFriendActivity act;
    private String friendJid;
    private ListView ll_search_list;
    private List<String> friendJids;
    private List<VCardBean> list;
    private MyBitmapUtils bitmapUtils;
    private int type;
    private OpenIMDao openIMDao;
    private TextView tv_back;
    private ImageView iv_minus;

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
        ib_back.setOnClickListener(this);
        tv_back.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        iv_minus.setOnClickListener(this);
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
        tv_back = (TextView) findViewById(R.id.tv_back);
        iv_minus = (ImageView) findViewById(R.id.iv_minus);

        openIMDao = OpenIMDao.getInstance(act);
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
                            Intent intent = new Intent(act, UserInfoActivity.class);
                            intent.putExtra("friendJid", friendJid);
                            intent.putExtra("type", type);  //从添加好友进入好友详情  type = 1 陌生人 type = 2 好友
                            startActivity(intent);
                            if (type == 2) {
                                finish();
                            }
                        }
                    });
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_back:
            case R.id.ib_back:
                Intent intent = new Intent(act, MainActivity.class);
                intent.putExtra("selection",2);
                startActivity(intent);
                finish();
                break;
            case R.id.btn_search:
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
                                VCardBean vCardBean = openIMDao.findSingleVCard(friendJid);
                                if (vCardBean == null) {
                                    vCardBean = MyVCardUtils.queryVCard(friendJid);
                                    if (vCardBean != null) {
                                        vCardBean.setJid(friendJid);
                                        list.add(vCardBean);
                                    }
                                    type = 1;
                                } else {
                                    type = 2;
                                }
                            }
                            handler.sendEmptyMessage(QUERY_SUCCESS);
                        }
                    }
                });
                break;
            case R.id.iv_minus:
                // 旋转180度 不保存状态 补间动画
                MyAnimationUtils.rotate(iv_minus);
                if (list != null) {
                    list.clear();
                    mAdapter.notifyDataSetChanged();
                }
                et_search_key.setText("");
                break;
        }
    }

    private class ViewHolder {
        TextView tv_title;
        TextView tv_msg;
        ImageView iv_icon;
    }

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
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
            if (list != null && list.size() > 0) {
                VCardBean vCardBean = list.get(position);
                vh.tv_title.setText(vCardBean.getNick());
                String avatarUrl = vCardBean.getAvatar();
                if (avatarUrl != null) {
                    vh.iv_icon.setTag(0);
                    bitmapUtils.display(vh.iv_icon, avatarUrl);
                } else {
                    vh.iv_icon.setImageResource(R.mipmap.ic_launcher);
                }
                vh.tv_msg.setText(vCardBean.getDesc());
            }
            return convertView;
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(act, MainActivity.class);
        intent.putExtra("selection",2);
        startActivity(intent);
        super.onBackPressed();
    }
}
