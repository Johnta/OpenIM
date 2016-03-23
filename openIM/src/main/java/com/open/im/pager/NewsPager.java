package com.open.im.pager;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.open.im.R;
import com.open.im.activity.ChatActivity;
import com.open.im.activity.MainActivity;
import com.open.im.adapter.SwipeAdapter;
import com.open.im.app.MyApp;
import com.open.im.bean.MessageBean;
import com.open.im.db.ChatDao;
import com.open.im.utils.MyLog;
import com.open.im.utils.ThreadUtil;
import com.open.im.view.SwipeListView;

public class NewsPager extends BasePager {

    private MainActivity act;
    private SwipeListView mListView;
    private List<MessageBean> list = new ArrayList<MessageBean>();
    private ChatDao dao;
    private static final int QUERY_SUCCESS = 100;
    private ProgressDialog pd;
    private Uri newsUri = Uri.parse("content://com.exiu.message");
    private SwipeAdapter mAdapter;

    public NewsPager(Context ctx) {
        super(ctx);
        act = (MainActivity) ctx;
        dao = ChatDao.getInstance(ctx);

        /**
         * 不知道为嘛 cursorAdapter在activity外使用就不能自动更新了 所以在这儿写了个内容观察者，观察数据库的URL
         * 如果数据库发生变化 就改变cursor 然后让adapter刷新cursor
         */
        ctx.getContentResolver().registerContentObserver(newsUri, true, new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                ThreadUtil.runOnBackThread(new Runnable() {
                    @Override
                    public void run() {
                        List<MessageBean> data = dao.getChatingFriends(MyApp.username);
                        list.clear();
                        for (MessageBean messageBean : data) {
                            list.add(messageBean);
//                            MyLog.showLog("messageBean::" + messageBean);
                        }
                        // 发送查询完成消息
                        handler.sendEmptyMessage(QUERY_SUCCESS);
                    }
                });
            }
        });
    }

    @Override
    public View initView() {
        View view = View.inflate(act, R.layout.pager_im_news, null);
        mListView = (SwipeListView) view.findViewById(R.id.listview);
        return view;
    }

    @Override
    public void initData() {

        pd = new ProgressDialog(act);
        pd.setMessage("加载中...");
        pd.show();

        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {

                list.clear();
                // cursor = dao.getChatingFriend(MyApp.username);
                List<MessageBean> data = dao.getChatingFriends(MyApp.username);
                for (MessageBean messageBean : data) {
                    list.add(messageBean);
                    MyLog.showLog("messageBean2::" + messageBean);
                }

                // 发送查询完成消息
                handler.sendEmptyMessage(QUERY_SUCCESS);
            }
        });
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                // 查询成功
                case QUERY_SUCCESS:
                    if (pd.isShowing()) {
                        pd.dismiss();
                    }
                    if (mAdapter == null) {
                        mAdapter = new SwipeAdapter(act, list, mListView.getRightViewWidth());
                    } else {
                        // 这个要求adapter对应的list是同一个对象才能生效，不同对象不能生效
                        mAdapter.notifyDataSetChanged();
                        MyLog.showLog("数据库改变");
                    }
                    mListView.setAdapter(mAdapter);

                    /**
                     * 条目点击事件，跳转到聊天详情界面
                     */
                    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            ListView listView = (ListView) parent;
                            MessageBean bean = (MessageBean) listView.getItemAtPosition(position);

                            String msgFrom = bean.getFromUser();
                            String msgTo = bean.getToUser();
                            String friendName = "";
                            if (msgFrom.equals(MyApp.username)) {
                                friendName = msgTo;
                            } else {
                                friendName = msgFrom;
                            }

                            Intent intent = new Intent(act, ChatActivity.class);
                            intent.putExtra("friendName", friendName);
                            act.startActivity(intent);
                        }
                    });

                    mAdapter.setOnRightItemClickListener(new SwipeAdapter.onRightItemClickListener() {

                        @Override
                        public void onRightItemClick(View v, int position) {
                            Toast.makeText(act, "删除第  " + (position + 1) + " 对话记录", Toast.LENGTH_SHORT).show();
                            list.remove(position);
                            mAdapter.notifyDataSetChanged();
                            mListView.hiddenRight(mListView.getChildAt(position));
                        }
                    });
                    break;
            }
        }

        ;
    };
}
