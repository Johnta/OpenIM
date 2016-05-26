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
import com.open.im.app.MyApp;
import com.open.im.bean.VCardBean;
import com.open.im.db.OpenIMDao;
import com.open.im.utils.MyAnimationUtils;
import com.open.im.utils.MyBitmapUtils;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyUserSearchUtils;
import com.open.im.utils.MyUtils;
import com.open.im.utils.MyVCardUtils;
import com.open.im.utils.ThreadUtil;
import com.open.im.view.CircularImage;
import com.rockerhieu.emojicon.EmojiconTextView;

import org.jivesoftware.smackx.search.ReportedData.Row;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AddFriendActivity extends BaseActivity implements OnClickListener {

    private static final int QUERY_SUCCESS = 1000;
    @BindView(R.id.ib_back)
    ImageButton ibBack;
    @BindView(R.id.tv_back)
    TextView tvBack;
    @BindView(R.id.iv_minus)
    ImageView ivMinus;
    @BindView(R.id.et_search_key)
    EditText etSearchKey;
    @BindView(R.id.btn_search)
    Button btnSearch;
    @BindView(R.id.ll_search_list)
    ListView llSearchList;
    private AddFriendActivity act;
    private String friendJid;
    private List<String> friendJids;
    private List<VCardBean> list;
    private MyBitmapUtils bitmapUtils;
    private int type;
    private OpenIMDao openIMDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        //初始化操作
        init();
    }

    /**
     * 初始化操作
     */
    private void init() {
        act = this;
        ButterKnife.bind(this);
        llSearchList.setVisibility(View.GONE);

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
                    llSearchList.setVisibility(View.VISIBLE);
                    if (mAdapter == null) {
                        mAdapter = new MyAdapter();
                    }
                    llSearchList.setAdapter(mAdapter);
                    llSearchList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

    @OnClick({R.id.ib_back, R.id.tv_back, R.id.iv_minus, R.id.btn_search})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ib_back:
            case R.id.tv_back:
                finish();
                break;
            case R.id.iv_minus:
                // 旋转180度 不保存状态 补间动画
                MyAnimationUtils.rotate(ivMinus);
                if (list != null) {
                    list.clear();
                    mAdapter.notifyDataSetChanged();
                }
                etSearchKey.setText("");
                break;
            case R.id.btn_search:
                final String searchKey = etSearchKey.getText().toString().trim();
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
                            list = new ArrayList<>();
                            for (String friendJid : friendJids) {
                                VCardBean vCardBean = openIMDao.findSingleVCard(friendJid);
                                if (vCardBean == null) {
                                    vCardBean = MyVCardUtils.queryVCard(friendJid);
                                    if (vCardBean != null) {
                                        vCardBean.setJid(friendJid);
                                    }
                                    type = 1;
                                } else {
                                    if (friendJid.equals(MyApp.username + "@" + MyConstance.SERVICE_HOST)) {
                                        type = 0;
                                    } else {
                                        type = 2;
                                    }
                                }
                                list.add(vCardBean);
                            }
                            handler.sendEmptyMessage(QUERY_SUCCESS);
                        }
                    }
                });
                break;
        }
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
                convertView = View.inflate(act, R.layout.list_item_news, null);
                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            if (list != null && list.size() > 0) {
                VCardBean vCardBean = list.get(position);
                vh.tvTitle.setText(vCardBean.getNick());
                String avatarUrl = vCardBean.getAvatar();
                if (avatarUrl != null) {
                    vh.ivIcon.setTag(0);
                    bitmapUtils.display(vh.ivIcon, avatarUrl);
                } else {
                    vh.ivIcon.setImageResource(R.mipmap.ic_launcher);
                }
                vh.tvMsg.setText(vCardBean.getDesc());
            }
            return convertView;
        }
    }

    class ViewHolder {
        @BindView(R.id.iv_icon)
        CircularImage ivIcon;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_msg)
        EmojiconTextView tvMsg;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

//    @Override
//    public void onBackPressed() {
//        Intent intent = new Intent(act, MainActivity.class);
//        intent.putExtra("selection",2);
//        startActivity(intent);
//        super.onBackPressed();
//    }
}
