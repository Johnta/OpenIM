package com.open.im.pager;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.activity.MainActivity;
import com.open.im.activity.SubscribeActivity;
import com.open.im.activity.UserInfoActivity;
import com.open.im.app.MyApp;
import com.open.im.utils.MyLog;
import com.open.im.utils.PinyinComparator;
import com.open.im.utils.ThreadUtil;
import com.open.im.view.MyDialog;
import com.open.im.view.SideBar;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class ContactPager extends BasePager {

    private MyFriendAdapter mFriendAdapter;
    private Roster roster;
    private MainActivity act;
    private ListView lv_show_friends;
    private TextView mDialogText;
    private ArrayList<String> friendNames;
    private ArrayList<String> friendNicks;
    private String[] friends;
//    private String[] nicks;
    private MyDialog pd;
    private String[] others = {"陌生人"};
    private int[] othersId = {R.mipmap.a_1};

    private final static int LOAD_SUCCESS = 201;
    private ListView lv_others;

    public ContactPager(Context ctx) {
        super(ctx);
        act = (MainActivity) ctx;
    }

    @Override
    public View initView() {

        View view = View.inflate(act, R.layout.pager_im_constact, null);
        WindowManager mWindowManager = (WindowManager) act.getSystemService(Context.WINDOW_SERVICE);
        lv_show_friends = (ListView) view.findViewById(R.id.lv_show_friends);
        lv_others = (ListView) view.findViewById(R.id.lv_others);
        SideBar indexBar = (SideBar) view.findViewById(R.id.sideBar);
        indexBar.setListView(lv_show_friends);
        mDialogText = (TextView) View.inflate(act, R.layout.list_position, null);
        mDialogText.setVisibility(View.INVISIBLE);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_APPLICATION, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        mWindowManager.addView(mDialogText, lp);
        indexBar.setTextView(mDialogText);

        return view;
    }

    @Override
    /**
     * 初始化数据 设置adapter
     */
    public void initData() {

        MyOthersAdapter mOthersAdapter = new MyOthersAdapter();
        lv_others.setAdapter(mOthersAdapter);

        friendNames = new ArrayList<String>();
        friendNicks = new ArrayList<String>();

        roster = Roster.getInstanceFor(MyApp.connection);
        // 注册好友状态监听
        registerRosterListener();

        pd = new MyDialog(act);
        pd.show();
        //查询所有的好友
        queryFriends();
        // 注册ListView条目点击事件
        register();
    }

    /**
     * 查询好友
     */
    private void queryFriends() {
        friendNames.clear();
        friendNicks.clear();
        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                Collection<RosterEntry> users = roster.getEntries();
                if (users == null) {
                    return;
                }
                // 遍历获得所有组内所有好友的名称
                for (RosterEntry rosterEntry : users) {
                    String userJid = rosterEntry.getUser();
                    String friendName = userJid.substring(0, userJid.indexOf("@"));
//                    String nickName;
//                    VCardBean vCardBean = chatDao.queryVCard(userJid);
//                    if (vCardBean == null) {
//                        vCardBean = MyVCardUtils.queryVcard(userJid);
//                        vCardBean.setJid(userJid);
//                        chatDao.replaceVCard(vCardBean);
//                    }
//                    nickName = vCardBean.getNickName();
                    if (!friendNames.contains(friendName)) {
                        // 通讯录改为显示好友昵称
//                        friendNicks.add(nickName);
                        friendNames.add(friendName);
                    }
                }
                handler.sendEmptyMessage(LOAD_SUCCESS);
            }
        });
    }

    /**
     * 好友列表上面那些固定的信息的Adapter  如添加朋友
     */
    private class MyOthersAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return others.length;
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
            ViewHolder vh;
            if (convertView == null) {
                vh = new ViewHolder();
                convertView = View.inflate(act, R.layout.list_item_contact, null);
                vh.tvNick = (TextView) convertView.findViewById(R.id.tv_friend_name);
                vh.ivAvatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
                vh.tvCatalog = (TextView) convertView.findViewById(R.id.tv_log);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            vh.ivAvatar.setImageResource(othersId[position]);
            vh.tvNick.setText(others[position]);
            vh.tvCatalog.setVisibility(View.GONE);

            return convertView;
        }
    }

    /**
     * 好友列表的Adapter
     */
    private class MyFriendAdapter extends BaseAdapter implements SectionIndexer {

        public MyFriendAdapter() {
            Arrays.sort(friends, new PinyinComparator());
//            Arrays.sort(nicks,new PinyinComparator());
        }

        @Override
        public int getCount() {
            return friends.length;
        }

        @Override
        public Object getItem(int position) {
            if (friends.length != 0) {
                return friends[position];
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String friendName = friends[position];
            View view;
            ViewHolder vh;
            if (convertView == null) {
                vh = new ViewHolder();
                view = View.inflate(act, R.layout.list_item_contact, null);
                vh.tvNick = (TextView) view.findViewById(R.id.tv_friend_name);
                vh.tvCatalog = (TextView) view.findViewById(R.id.tv_log);
                vh.ivAvatar = (ImageView) view.findViewById(R.id.iv_avatar);
                view.setTag(vh);
            } else {
                view = convertView;
                vh = (ViewHolder) view.getTag();
            }
            String catalog = converterToFirstSpell(friendName).substring(0, 1).toUpperCase();
            if (position == 0) {
                vh.tvCatalog.setVisibility(View.VISIBLE);
                vh.tvCatalog.setText(catalog);
            } else {
                String lastCatalog = converterToFirstSpell(friends[position - 1]).substring(0, 1);
                if (catalog.equalsIgnoreCase(lastCatalog)) {
                    vh.tvCatalog.setVisibility(View.GONE);
                } else {
                    vh.tvCatalog.setVisibility(View.VISIBLE);
                    vh.tvCatalog.setText(catalog);
                }
            }

            vh.tvNick.setText(friends[position]);
            vh.ivAvatar.setImageResource(R.mipmap.ic_launcher);

            return view;
        }

        @Override
        public int getPositionForSection(int section) {
            for (int i = 0; i < friendNames.size(); i++) {
                String l = converterToFirstSpell(friendNames.get(i)).substring(0, 1);
                char firstChar = l.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getSectionForPosition(int position) {
            return 0;
        }

        @Override
        public Object[] getSections() {
            return null;
        }

    }

    static class ViewHolder {
        TextView tvCatalog;// 目录
        ImageView ivAvatar;// 头像
        TextView tvNick;// 昵称
    }

    /**
     * 汉字转换位汉语拼音首字母，英文字符不变
     *
     * @param chines 汉字
     * @return 拼音
     */
    public static String converterToFirstSpell(String chines) {
        String pinyinName = "";
        if (chines != null) {
            char[] nameChar = chines.toCharArray();
            HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
            defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
            defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
            for (int i = 0; i < nameChar.length; i++) {
                if (nameChar[i] > 128) {
                    try {
                        pinyinName += PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0].charAt(0);
                    } catch (BadHanyuPinyinOutputFormatCombination e) {
                        e.printStackTrace();
                    }
                } else {
                    pinyinName += nameChar[i];
                }
            }
        }
        return pinyinName;
    }

    private void register() {

        /**
         * 其他信息条目点击监听
         */
        lv_others.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent intent = new Intent(act, SubscribeActivity.class);
                        act.startActivity(intent);
                        break;
                }
            }
        });

        /**
         * 好友列表条目设置点击监听
         */
        lv_show_friends.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String friendName = friends[position];
                String friendJid = friendName + "@" + MyApp.connection.getServiceName();
                // 跳转到会话界面
                Intent intent = new Intent(act, UserInfoActivity.class);
                intent.putExtra("friendJid", friendJid);
                intent.putExtra("type",2);
                act.startActivity(intent);
            }
        });
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case LOAD_SUCCESS:
                    pdDismiss();
                    friends = (String[]) friendNames.toArray(new String[friendNames.size()]);
//                    nicks = (String[]) friendNicks.toArray(new String[friendNicks.size()]);
                    mFriendAdapter = new MyFriendAdapter();
                    lv_show_friends.setAdapter(mFriendAdapter);

                    /**
                     * listView滑动时，显示当前可见的第一条的拼音首字母
                     */
                    lv_show_friends.setOnScrollListener(new OnScrollListener() {

                        @Override
                        public void onScrollStateChanged(AbsListView view, int scrollState) {
                        }

                        @Override
                        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                            String firstItem = (String) mFriendAdapter.getItem(firstVisibleItem);
                            String converterToFirstSpell = converterToFirstSpell(firstItem);
                            if (!TextUtils.isEmpty(converterToFirstSpell)) {
                                String log = converterToFirstSpell.substring(0, 1).toUpperCase();
                                mDialogText.setText(log);
                            }
                        }
                    });

                    /**
                     * 判断何时显示提示首字母的view
                     */
                    lv_show_friends.setOnTouchListener(new OnTouchListener() {

                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if (event.getAction() == MotionEvent.ACTION_UP) {
                                mDialogText.setVisibility(View.INVISIBLE);
                            } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                mDialogText.setVisibility(View.VISIBLE);
                            }
                            return false;
                        }
                    });

                    break;

                default:
                    break;
            }
        }

        ;
    };

    private void pdDismiss() {
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

    /**
     * 注册好友列表监听
     */
    private void registerRosterListener() {
        roster.addRosterListener(new RosterListener() {
            @Override
            /**
             *
             */
            public void entriesAdded(Collection<String> addresses) {
                MyLog.showLog("1------添加好友");
                queryFriends();
            }

            @Override
            /**
             *
             */
            public void entriesUpdated(Collection<String> addresses) {
                MyLog.showLog("2------好友状态更新");
//                queryFriends();
            }

            @Override
            /**
             *删除好友回调
             */
            public void entriesDeleted(Collection<String> addresses) {
                MyLog.showLog("3-------删除好友");
                queryFriends();
            }

            @Override
            /**
             *好友状态改变时 回调
             */
            public void presenceChanged(Presence presence) {
                MyLog.showLog("4------好友状态改变");
            }
        });
    }
}
