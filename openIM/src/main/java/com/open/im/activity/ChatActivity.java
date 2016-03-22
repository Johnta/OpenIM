package com.open.im.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.adapter.ChatLVAdapter;
import com.open.im.app.MyApp;
import com.open.im.baidumap.BaiduMapActivity;
import com.open.im.bean.FileBean;
import com.open.im.bean.ImageBean;
import com.open.im.bean.LocationBean;
import com.open.im.bean.LongMsgBean;
import com.open.im.bean.MessageBean;
import com.open.im.bean.RecordBean;
import com.open.im.db.ChatDao;
import com.open.im.utils.MyAudioRecordUtils;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyCopyUtils;
import com.open.im.utils.MyFileUtils;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyMD5Encoder;
import com.open.im.utils.MyPicUtils;
import com.open.im.utils.MyUtils;
import com.open.im.utils.ThreadUtil;
import com.open.im.view.XListView;
import com.open.im.view.XListView.IXListViewListener;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;

public class ChatActivity extends FragmentActivity implements OnClickListener, OnItemClickListener, IXListViewListener, EmojiconGridFragment.OnEmojiconClickedListener,
        EmojiconsFragment.OnEmojiconBackspaceClickedListener {

    private TextView tv_title, tv_send;
    private EditText et_msg;
    private XListView mListView;
    private LinearLayout ll_record_window;
    private ChatActivity act;
    private ChatManager cm;
    private String friendName;
    private Chat chatTo;
    private ChatDao chatDao;
    private SharedPreferences sp;
    private ProgressDialog pd;
    private List<MessageBean> data;
    private ArrayList<MessageBean> data2;
    private ChatLVAdapter adapter;
    private static final int QUERY_SUCCESS = 100;
    private static final int LOAD_SUCCESS = 101;
    private static final int PIC_RESULT = 1000;
    private static final int CAMERA_RESULT = 1001;
    private static final int FILE_RESULT = 1002;
    private static final int BAIDU_MAP = 1003;
    private String username;
    private ImageView iv_add;
    private GridView gv_more;
    private String[] names = new String[]{"拍照", "图片", "位置"};
    private int[] iconIds = new int[]{R.mipmap.options_camera, R.mipmap.options_picture, R.mipmap.options_location};

    private MyGridViewAdapter myGridViewAdapter;
    private ImageView image_face;

    private boolean isShosrt;
    private static final int POLL_INTERVAL = 300;

    private Uri newsUri = Uri.parse("content://com.exiu.message");

    private ImageView iv_say;
    private String imagePath;
    private TextView tv_say;
    private LinearLayout del_re;
    private LinearLayout voice_rcd_hint_rcding;
    private LinearLayout voice_rcd_hint_loading;
    private LinearLayout voice_rcd_hint_tooshort;
    private ImageView img1;
    private long startVoiceT;
    private long endVoiceT;
    private ImageView volume;
    private ImageButton ib_back;
    private Fragment f_emojicons;
    private String msgMark;
    private String friendJid;
    private ImageView iv_keyboard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // 初始化
        init();
        // 注册监听
        register();
        // 初始化数据
        initData();

    }

    /**
     * 查询数据库 初始化聊天数据 收到的消息先存到数据 发出的消息发出后存到数据库 显示界面直接跟数据库关联 数据库改变 listview改变
     */
    private void initData() {

        myGridViewAdapter = new MyGridViewAdapter();
        gv_more.setAdapter(myGridViewAdapter);
        gv_more.setOnItemClickListener(this);

        pd = new ProgressDialog(act);
        pd.setMessage("加载中...");
        pd.show();
        new Thread() {
            public void run() {
                // 首次进入页面 0偏移查询15条聊天信息
                data = chatDao.queryMsg(msgMark, 0);
                // 发送查询完成消息
                handler.sendEmptyMessage(QUERY_SUCCESS);
            }

            ;
        }.start();
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                // 查询成功
                case QUERY_SUCCESS:
                    if (pd.isShowing()) {
                        pd.dismiss();
                    }
                    if (adapter == null) {
                        adapter = new ChatLVAdapter(act, data, friendJid);
                    }
                    mListView.setAdapter(adapter);
                    mListView.setXListViewListener(act);
                    // 设置默认显示在listView的最后一行
                    mListView.setSelection(adapter.getCount() - 1);
                    break;
                case LOAD_SUCCESS:
                    // ListView头布局隐藏 刷新adapter
                    onLoad();
                    if (data2 != null && data2.size() != 0) {
                        adapter.notifyDataSetChanged();
                        mListView.setSelection(data2.size() - 1);
                    }
                    break;
            }
        }

        ;
    };

    /**
     * 包含 拍照 图片 地图的gridview
     *
     * @author Administrator
     */
    private class MyGridViewAdapter extends BaseAdapter {

        @Override
        /**
         * 告诉系统，有多少个条目
         */
        public int getCount() {
            return names.length;
        }

        @Override
        public Object getItem(int position) {
            return names[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        /**
         * 根据 position 返回一个 对应的条目的view
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(act, R.layout.grid_item, null);
            }
            ImageView icon = (ImageView) convertView.findViewById(R.id.iv_grid_item);
            TextView name = (TextView) convertView.findViewById(R.id.tv_name_grid_item);
            // 设置图标
            icon.setBackgroundResource(iconIds[position]);
            // 设置名称
            name.setText(names[position]);
            return convertView;
        }
    }

    /**
     * 事件监听
     */
    private void register() {
        /**
         * 监听editText输入框变化 当输入框有内容时，显示发送按钮隐藏更多按钮
         */
        et_msg.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (et_msg.length() >= 1) {
                    tv_send.setVisibility(View.VISIBLE);
                    iv_add.setVisibility(View.GONE);
                } else {
                    tv_send.setVisibility(View.GONE);
                    iv_add.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        // listview设置触摸时间，触摸时，隐藏一些空间
        mListView.setOnTouchListener(new OnTouchListener() {
            @SuppressLint("NewApi")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (f_emojicons.isVisible()) {
                        getSupportFragmentManager().beginTransaction().hide(f_emojicons).commit();
                    }
                    if (gv_more.isShown()) {
                        gv_more.setVisibility(View.GONE);
                    }
                    hideSoftInputView();
                }
                return false;
            }
        });
        /**
         * 返回按钮点击事件
         */
        ib_back.setOnClickListener(this);
        /**
         * 输入框点击事件
         */
        et_msg.setOnClickListener(this);

        /**
         * 发消息
         */
        tv_send.setOnClickListener(this);
        /**
         * 打开摄像头等
         */
        iv_add.setOnClickListener(this);
        /**
         * 表情框
         */
        image_face.setOnClickListener(this);
        /**
         * 语音图标点击事件
         */
        iv_say.setOnClickListener(this);
        /**
         * 键盘图标点击事件
         */
        iv_keyboard.setOnClickListener(this);
        /**
         * 长按说话
         */
        tv_say.setOnTouchListener(new OnTouchListener() {

            private String audioPath;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) { // 按下
                    MyLog.showLog("按下");
                    isShosrt = false;
                    ll_record_window.setVisibility(View.VISIBLE);
                    voice_rcd_hint_loading.setVisibility(View.VISIBLE);
                    voice_rcd_hint_rcding.setVisibility(View.GONE);
                    voice_rcd_hint_tooshort.setVisibility(View.GONE);
                    // handler发送延时消息，300毫秒后做runnable里面的事情
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            if (!isShosrt) {
                                voice_rcd_hint_loading.setVisibility(View.GONE);
                                voice_rcd_hint_rcding.setVisibility(View.VISIBLE);
                            }
                        }
                    }, 300);

                    img1.setVisibility(View.VISIBLE);
                    del_re.setVisibility(View.GONE);
                    // 记录录音开始的时间
                    startVoiceT = System.currentTimeMillis();
                    // 开始录音
                    audioPath = MyAudioRecordUtils.startRecord();
                    // 录音开始，悬浮窗根据声音大小显示不同的图片
                    handler.postDelayed(mPollTask, POLL_INTERVAL);
                } else if (event.getAction() == MotionEvent.ACTION_UP) { // 抬起
                    MyLog.showLog("抬起");
                    voice_rcd_hint_rcding.setVisibility(View.GONE);
                    // 停止录音
                    MyAudioRecordUtils.stopRecord();
                    // 停止录音后，就不要根据声音显示不同的图片了
                    handler.removeCallbacks(mPollTask);
                    // 记录录音结束时的时间
                    endVoiceT = System.currentTimeMillis();
                    // 录音时长
                    final int time = (int) ((endVoiceT - startVoiceT) / 1000);
                    MyLog.showLog("start::" + startVoiceT);
                    MyLog.showLog("end::" + endVoiceT);
                    MyLog.showLog("录音时长:" + time);
                    // 如果录音时长过短，小于1秒，则认为事件太短，不发送
                    if (time < 1) {
                        isShosrt = true;
                        voice_rcd_hint_loading.setVisibility(View.GONE);
                        voice_rcd_hint_rcding.setVisibility(View.GONE);
                        voice_rcd_hint_tooshort.setVisibility(View.VISIBLE);
                        // 发延迟消息，500毫秒后，提示录音时间过短的控件消失
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                voice_rcd_hint_tooshort.setVisibility(View.GONE);
                                ll_record_window.setVisibility(View.GONE);
                                isShosrt = false;
                            }
                        }, 500);
                    } else {
                        // 开子线程上传文件
                        ThreadUtil.runOnBackThread(new Runnable() {
                            @Override
                            public void run() {
                                // TODO 录音文件上传
                                FileBean bean = MyFileUtils.upLoadByHttpClient(audioPath);
                                String result = bean.getResult();
                                String uri = MyConstance.HOMEURL + result;
                                try {
                                    insert2DB(uri, 2); // 2表示type，表示是录音文件
                                    File file = new File(audioPath);
                                    String json = getRecordJson(uri, file.length(), time);
                                    Message message = new Message();
                                    message.setBody(json);
                                    message.setTo(friendName);

                                    // 给message添加消息回执字段
                                    DeliveryReceiptRequest.addTo(message);

                                    // 通过会话对象发送消息
                                    // 创建会话对象时已经指定接收者了
                                    chatTo.sendMessage(message);
                                } catch (NotConnectedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
                return false;
            }
        });

        /**
         * 观察者模式，观察数据库改变，数据库改变时，查询最新改变的信息存到list中，并通知adapter刷新
         */
        ContentObserver observer = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                ThreadUtil.runOnBackThread(new Runnable() {
                    @Override
                    public void run() {
                        MessageBean lastMsg = chatDao.queryTheLastMsg();
                        data.add(lastMsg);
                        handler.sendEmptyMessage(QUERY_SUCCESS);
                    }
                });
            }
        };
        act.getContentResolver().registerContentObserver(newsUri, true, observer);
    }

    /**
     * 不含大图地址普通插入
     *
     * @param msgBody
     * @param type
     * @return
     */
    private void insert2DB(String msgBody, int type) {
        insert2DB(msgBody, "", type);
    }

    /**
     * 含大图地址插入
     *
     * @param msgBody
     */
    private void insert2DB(String msgBody, String imgPath, int type) {
        // 封装消息内容等信息的bean
        MessageBean msg = new MessageBean();
        // MyLog.showLog(username);
        msg.setFromUser(sp.getString("username", ""));
        msg.setToUser(friendName);
        msg.setIsComing(1); // 1表示发出消息
        msg.setIsReaded("1"); // 1表示已读 0表示未读 我发送出去的消息 我肯定是已读的
        msg.setMsgBody(msgBody);
        msg.setMsgImg(imgPath); // 大图地址
        msg.setMsgDateLong(new Date().getTime());
        msg.setMsgMark(msgMark); // 标记跟谁聊天
        msg.setType(type); // 消息的类型 普通文本 图片 位置信息 语音
        msg.setMsgOwner(username);
        // 插入数据库
        chatDao.insertMsg(msg);
    }

    /**
     * 获取发送的图片的json
     *
     * @param uri        图片大图地址
     * @param thumbnail  图片缩影图地址
     * @param size       图片大小
     * @param resolution 图片分辨率
     * @return
     */
    private String getImageJson(String uri, String thumbnail, long size, String resolution) {
        String json = "";
        ImageBean imageBean = new ImageBean();
        imageBean.setResolution(resolution);
        imageBean.setSize(size);
        imageBean.setType("image");
        imageBean.setUri(uri);
        imageBean.setThumbnail(thumbnail);
        json = imageBean.toJson();
        return json;
    }

    /**
     * 发送出去的语音的json
     *
     * @param uri    语音地址
     * @param size   语音大小
     * @param length 语音持续时长
     * @return
     */
    private String getRecordJson(String uri, long size, int length) {
        String json = "";
        RecordBean recordBean = new RecordBean();
        recordBean.setType("voice");
        recordBean.setUri(uri);
        recordBean.setSize(size);
        recordBean.setLength(length);
        json = recordBean.toJson();
        return json;
    }

    /**
     * 获取发送出去的长文本的json
     *
     * @param uri
     * @param size
     * @return
     */
    private String getLongMsgJson(String uri, long size) {
        String json = "";
        LongMsgBean longMsgBean = new LongMsgBean();
        longMsgBean.setType("text");
        longMsgBean.setUri(uri);
        longMsgBean.setSize(size);
        json = longMsgBean.toJson();
        return json;
    }

    /**
     * 发送出去的位置的json
     *
     * @param uri         地图截图的Uri
     * @param longitude   经度
     * @param latitude    纬度
     * @param accuracy    精确度
     * @param manner      方法方式
     * @param description 描述信息
     * @return
     */
    private String getLocationJson(String uri, double longitude, double latitude, float accuracy, String manner, String description) {
        String json = "";
        LocationBean locationBean = new LocationBean();
        locationBean.setType("location");
        locationBean.setUri(uri);
        locationBean.setLongitude(longitude);
        locationBean.setLatitude(latitude);
        locationBean.setAccuracy(accuracy);
        locationBean.setManner(manner);
        locationBean.setDescription(description);
        json = locationBean.toJson();
        return json;
    }

    /**
     * 初始化
     */
    @SuppressLint("NewApi")
    private void init() {
        act = this;
        tv_title = (TextView) findViewById(R.id.tv_title);
        et_msg = (EditText) findViewById(R.id.et_msg);
        tv_send = (TextView) findViewById(R.id.tv_send);
        mListView = (XListView) findViewById(R.id.lv_messages);
        mListView.setPullLoadEnable(false);// 设置让它上拉，FALSE为不让上拉，便不加载更多数据
        iv_add = (ImageView) findViewById(R.id.iv_add);
        iv_say = (ImageView) findViewById(R.id.iv_say);
        iv_keyboard = (ImageView) findViewById(R.id.iv_keyboard);
        tv_say = (TextView) findViewById(R.id.tv_say);
        ib_back = (ImageButton) findViewById(R.id.ib_back);

        gv_more = (GridView) findViewById(R.id.gv_more);

        image_face = (ImageView) findViewById(R.id.image_face);

        FragmentManager supportFragmentManager = getSupportFragmentManager();
        /**
         * 一个事务只能提交一次 如果需要多次提交 就要开多个事务
         */
        FragmentTransaction beginTransaction = supportFragmentManager.beginTransaction();
        f_emojicons = supportFragmentManager.findFragmentById(R.id.emojicons);
        beginTransaction.hide(f_emojicons).commit();

        // 下面的都是 录音时会弹的那个悬浮窗的控件(其实也不是悬浮窗，是布局里的以前gone掉了，显示出来)
        ll_record_window = (LinearLayout) findViewById(R.id.ll_record_window);
        volume = (ImageView) this.findViewById(R.id.volume);
        img1 = (ImageView) this.findViewById(R.id.img1);
        del_re = (LinearLayout) this.findViewById(R.id.del_re);
        voice_rcd_hint_rcding = (LinearLayout) findViewById(R.id.voice_rcd_hint_rcding);
        voice_rcd_hint_loading = (LinearLayout) findViewById(R.id.voice_rcd_hint_loading);
        voice_rcd_hint_tooshort = (LinearLayout) findViewById(R.id.voice_rcd_hint_tooshort);

        // 设置聊天标题
        friendName = getIntent().getStringExtra("friendName");
        friendJid = friendName + "@" + MyApp.connection.getServiceName();
        tv_title.setText(friendName);

        sp = getSharedPreferences(MyConstance.SP_NAME, 0);
        username = sp.getString("username", null);

        msgMark = friendName + "#" + username;

        chatDao = ChatDao.getInstance(act);

        // 获得会话管理者
        cm = ChatManager.getInstanceFor(MyApp.connection);
        // 创建会话对象
        if (chatTo == null) {
            chatTo = cm.createChat(friendJid);
        }
    }

    /**
     * 隐藏软键盘
     */
    public void hideSoftInputView() {
        InputMethodManager manager = ((InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE));
        if (getWindow().getAttributes().softInputMode != WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN) {
            if (getCurrentFocus() != null) {
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onClick(View v) {
        FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
        switch (v.getId()) {
            case R.id.et_msg: // 输入框点击事件
                if (f_emojicons.isVisible()) {
                    beginTransaction.hide(f_emojicons);
                }
                break;
            case R.id.tv_send: // 发送按钮点击事件
                String msgBody = et_msg.getText().toString().trim();
                et_msg.setText("");
                if (TextUtils.isEmpty(msgBody)) {
                    MyUtils.showToast(act, "消息不能为空");
                    return;
                }
                try {
                    insert2DB(msgBody, 0);
                    Message message = new Message();
                    message.setBody(msgBody);
                    // 给message添加消息回执字段
                    DeliveryReceiptRequest.addTo(message);
                    // 通过会话对象发送消息
                    // 创建会话对象时已经指定接收者了
                    chatTo.sendMessage(message);
                } catch (NotConnectedException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.iv_add: // 更多按钮点击事件
                if (gv_more.isShown()) {
                    gv_more.setVisibility(View.GONE);
                } else {
                    gv_more.setVisibility(View.VISIBLE);
                    if (f_emojicons.isVisible()) {
                        beginTransaction.hide(f_emojicons);
                    }
                }
                break;
            case R.id.image_face: // 表情按钮点击事件
                // 隐藏软键盘
                hideSoftInputView();
                if (f_emojicons.isVisible()) {
                    beginTransaction.hide(f_emojicons);
                } else {
                    beginTransaction.show(f_emojicons);
                }
                if (gv_more.isShown()) {
                    gv_more.setVisibility(View.GONE);
                }
                if (tv_say.isShown()) {
                    tv_say.setVisibility(View.GONE);
                    et_msg.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.iv_say: // 语音图标点击事件
                iv_say.setVisibility(View.GONE);
                iv_keyboard.setVisibility(View.VISIBLE);
                image_face.setVisibility(View.GONE);
                if (f_emojicons.isVisible()) {
                    beginTransaction.hide(f_emojicons);
                }
                et_msg.setVisibility(View.GONE);
                tv_say.setVisibility(View.VISIBLE);
                break;
            case R.id.iv_keyboard:
                iv_keyboard.setVisibility(View.GONE);
                iv_say.setVisibility(View.VISIBLE);
                image_face.setVisibility(View.VISIBLE);
                if (f_emojicons.isVisible()) {
                    beginTransaction.hide(f_emojicons);
                }
                et_msg.setVisibility(View.VISIBLE);
                tv_say.setVisibility(View.GONE);

                break;
            case R.id.ib_back:
                finish();
                break;
        }
        // 提交事务
        beginTransaction.commit();
    }

    @Override
    /**
     * gridview的条目点击事件  摄像头 图库 地图定位
     */
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        switch (position) {
            case 0:
                /**
                 * 打开摄像头
                 */
                // startActivityForResult(new Intent(act, CameraActivity.class),
                // CAMERA_RESULT);
                /**
                 * 打开系统相机
                 */
                openSysCamera();
                break;
            case 1: // 打开图库
                // intent.setType("image/*");
                // intent.setAction(Intent.ACTION_GET_CONTENT);
                // startActivityForResult(intent, PIC_RESULT);
                // 打开系统图库
                startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), PIC_RESULT);
                break;
            // case 2: // 打开文件系统
            // intent.setAction(Intent.ACTION_VIEW);
            // intent.addCategory("android.intent.category.DEFAULT");
            // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Uri uri = Uri.fromFile(Environment.getExternalStorageDirectory());
            // intent.setDataAndType(uri, "*/*");
            // startActivityForResult(intent, FILE_RESULT);
            // break;
            case 2: // 打开百度地图定位
                intent = new Intent(act, BaiduMapActivity.class);
                startActivityForResult(intent, BAIDU_MAP);
                break;
            default:
                break;
        }
        if (gv_more != null && gv_more.isShown()) {
            gv_more.setVisibility(View.GONE);
        }
    }

    /**
     * 方法 打开系统照相机
     */
    private void openSysCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + "PicTest_" + System.currentTimeMillis() + ".jpg";
        File file = new File(imagePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        // file:///storage/sdcard1/DCIM/PicTest_1456124316144.jpg
        Uri photoUri = Uri.fromFile(file);
        // 设置系统相机拍摄照片完成后图片文件的存放地址
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(intent, CAMERA_RESULT);
    }

    @Override
    /**
     * 回调
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PIC_RESULT && resultCode == RESULT_OK && null != data) {
            Uri selectedImageUri = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            final String imagePath = cursor.getString(columnIndex);
            cursor.close();
            /**
             * 上传 复制到缓存文件夹 发送链接
             */
            sendImage(imagePath);
        } else if (requestCode == CAMERA_RESULT && resultCode == RESULT_OK) {
            // 直接打开摄像头 拍照时 获取照片路径
            // final String photoPath = data.getDataString();

            sendImage(imagePath);

        } else if (requestCode == BAIDU_MAP && resultCode == RESULT_OK && null != data) {
            double latitude = data.getDoubleExtra("latitude", 0);
            double longitude = data.getDoubleExtra("longitude", 0);
            String locationAddress = data.getStringExtra("address");
            String locationName = data.getStringExtra("name");
            String snapShotPath = data.getStringExtra("snapshotpath");
            String str = "location#" + latitude + "#" + longitude + "#" + locationAddress + "#" + locationName + "#" + snapShotPath;
            // latitude: 34.81948577553742 纬度
            // longitude: 113.69074905237336 经度
            // locationAddress: 河南省郑州市金水区国泰路
            // locationName: [位置]
            try {
                insert2DB(str, 3);
                String json = getLocationJson(snapShotPath, longitude, latitude, 0.0f, "百度地图定位", locationAddress);
                Message message = new Message();
                message.setBody(json);
                message.setTo(friendName);
                // 给message添加消息回执字段
                DeliveryReceiptRequest.addTo(message);
                // 通过会话对象发送消息
                // 创建会话对象时已经指定接收者了
                chatTo.sendMessage(message);
            } catch (NotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 上传服务器 复制到缓存文件夹 发送链接
     *
     * @param imagePath
     */
    private void sendImage(final String imagePath) {
        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {// 压缩图片
                // 通过文件路径获取文件名
                String pictureName = imagePath.substring(imagePath.lastIndexOf(File.separator) + 1);
                // 压缩图片
                Bitmap smallBitmap = MyPicUtils.getSmallBitmap(imagePath);
                // 压缩文件保存的文件夹路径
                String compressDirPath = Environment.getExternalStorageDirectory() + "/exiu/cache/compress/";
                // 将压缩后的图片保存并返回保存路径
                String compressPath = MyPicUtils.saveFile(smallBitmap, compressDirPath, pictureName, 80);
                // TODO 上传的是压缩后的图片
                // String imageUrl = MyFileUtils.uploadFile(compressPath);

                FileBean bean = MyFileUtils.upLoadByHttpClient(compressPath);
                String result = bean.getResult();
                String result2 = bean.getThumbnail();
                // 文件名是 URL用MD5加密
                String uri = MyConstance.HOMEURL + result;
                String saveName = MyMD5Encoder.encode(uri) + ".jpg";
                // 缓存保存路径
                String cachePath = Environment.getExternalStorageDirectory() + "/exiu/cache/image/" + saveName;
                // 发送文件后，把压缩后的图片复制到缓存文件夹，以返回的文件名命名
                MyCopyUtils.copyImage(compressPath, cachePath);
                // 让文件能从图库中被找到
                MyFileUtils.scanFileToPhotoAlbum(act, cachePath);
                // MyLog.showLog("压缩路径:" + compressPath);
                // MyLog.showLog("缓存路径:" + cachePath);
                try {
                    String thumbnail = MyConstance.HOMEURL + result2;
                    insert2DB(uri, thumbnail, 1);
                    File file = new File(cachePath);
                    String json = getImageJson(uri, thumbnail, file.length(), "a*b");
                    Message message = new Message();
                    message.setBody(json);
                    message.setTo(friendName);
                    // 给message添加消息回执字段
                    DeliveryReceiptRequest.addTo(message);
                    // 通过会话对象发送消息
                    // 创建会话对象时已经指定接收者了
                    chatTo.sendMessage(message);
                } catch (NotConnectedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Runnable mPollTask = new Runnable() {
        public void run() {
            double amp = MyAudioRecordUtils.getAmplitude();
            updateDisplay(amp);
            handler.postDelayed(mPollTask, POLL_INTERVAL);
        }
    };

    /**
     * 根据振幅显示不同的图片
     *
     * @param signalEMA
     */
    private void updateDisplay(double signalEMA) {
        switch ((int) signalEMA) {
            case 0:
            case 1:
                volume.setImageResource(R.mipmap.amp1);
                break;
            case 2:
            case 3:
                volume.setImageResource(R.mipmap.amp2);
                break;
            case 4:
            case 5:
                volume.setImageResource(R.mipmap.amp3);
                break;
            case 6:
            case 7:
                volume.setImageResource(R.mipmap.amp4);
                break;
            case 8:
            case 9:
                volume.setImageResource(R.mipmap.amp5);
                break;
            case 10:
            case 11:
                volume.setImageResource(R.mipmap.amp6);
                break;
            default:
                volume.setImageResource(R.mipmap.amp7);
                break;
        }
    }

    // /**
    // * 判断某个界面是否在前台
    // *
    // * @param context
    // * @param className
    // * 某个界面名称
    // */
    // private boolean isForeground(Context context, String className) {
    // if (context == null || TextUtils.isEmpty(className)) {
    // return false;
    // }
    //
    // ActivityManager am = (ActivityManager)
    // context.getSystemService(Context.ACTIVITY_SERVICE);
    // List<RunningTaskInfo> list = am.getRunningTasks(1);
    // if (list != null && list.size() > 0) {
    // ComponentName cpn = list.get(0).topActivity;
    // if (className.equals(cpn.getClassName())) {
    // return true;
    // }
    // }
    //
    // return false;
    // }

    @Override
    /**
     * 界面可见时，将与此人的消息标为已读
     */
    protected void onStart() {
        super.onStart();
        int update = chatDao.updateMsgByMark(msgMark);
        // MyLog.showLog("已读::" + update);
    }

    @Override
    /**
     * 界面不可见时，将与此人的消息标为已读
     */
    protected void onStop() {
        super.onStop();
        chatDao.updateMsgByMark(msgMark);
    }

    @Override
    /**
     * 方法 下了刷新 查询更多的消息
     */
    public void onRefresh() {
        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                // TODO 模拟加载数据耗时
                SystemClock.sleep(2000);
                data2 = chatDao.queryMsg(msgMark, data.size());
                data.addAll(0, data2);
                handler.sendEmptyMessage(LOAD_SUCCESS);
            }
        });
    }

    @Override
    public void onLoadMore() {
        // TODO Auto-generated method stub

    }

    /**
     * 停止刷新，
     */
    private void onLoad() {
        mListView.stopRefresh();
        mListView.stopLoadMore();
        mListView.setRefreshTime("刚刚");
    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(et_msg);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(et_msg, emojicon);
    }
}
