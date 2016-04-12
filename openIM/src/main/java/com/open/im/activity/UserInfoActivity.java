package com.open.im.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.bean.FileBean;
import com.open.im.bean.SubBean;
import com.open.im.bean.VCardBean;
import com.open.im.db.ChatDao;
import com.open.im.utils.MyBitmapUtils;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyFileUtils;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyPicUtils;
import com.open.im.utils.MyUtils;
import com.open.im.utils.MyVCardUtils;
import com.open.im.utils.ThreadUtil;
import com.open.im.view.MyDialog;
import com.open.im.view.ZoomImageView;
import com.open.im.wheel.SelectBirthday;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 用户信息界面
 */
public class UserInfoActivity extends Activity implements OnClickListener {

    private static final int QUERY_SUCCESS = 100;
    private static final int SAVE_SUCCESS = 101;
    private ListView mListView;
    private UserInfoActivity act;
    private String[] items = {"头像:", "用户:", "昵称:", "性别:", "生日:", "地址:", "邮箱:", "电话:", "签名:"};
    private int[] icons = {R.mipmap.info_camera, R.mipmap.info_user, R.mipmap.info_nick, R.mipmap.info_sex, R.mipmap.info_birth, R.mipmap.info_nick, R.mipmap.info_nick, R.mipmap.info_phone, R.mipmap.info_desc};
    private VCard vCard;
    private String nickName;
    private String homeAddress;
    private String email;
    private String phone;
    private String sex;
    private String desc;
    private String bday;
    private ImageButton ib_back;
    protected SelectBirthday birth;
    private VCardManager vCardManager;
    private LinearLayout ll_root;
    private int type = 0;


    /**
     * 头像用
     */
    private static final int PHOTO_REQUEST_TAKEPHOTO = 10;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 11;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 0;// 结果

    private File tempFile;
    private String dirPath = Environment.getExternalStorageDirectory() + "/exiu/cache/avatar/";
    private XMPPTCPConnection connection;
    private MyDialog pd;
    private ChatDao chatDao;
    private VCardBean vCardBean;
    private Button btn_2;
    private Bitmap bitmap;
    private String avatarUrl;
    private MyBitmapUtils bitmapUtils;
    private String avatarPath;
    private String friendJid;
    private ImageView iv_flush;
    private TextView tv_title;
    private String friendName;
    private Button btn_1;

    // 创建一个以当前时间为名称的文件
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        // 初始化控件
        initView();
        // 初始化数据
        initData();

        register();
    }

    /**
     * 方法 点击小图时，加载大图片
     *
     * @param picPath
     */
    private void showImgDialog(final String picPath) {
        final AlertDialog dialog = new AlertDialog.Builder(act, R.style.Lam_Dialog_FullScreen).create();
        Window win = dialog.getWindow();
        win.setGravity(Gravity.FILL);
        // 隐藏手机最上面的状态栏
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.show();

        View view = View.inflate(act, R.layout.dialog_big_image, null);
        ZoomImageView imgView = (ZoomImageView) view.findViewById(R.id.iv_image);

        imgView.setTag(-2);

        if (picPath != null) {
            bitmapUtils.display(imgView, picPath);
        } else {
            imgView.setImageResource(R.drawable.ic_launcher);
        }
//        MyFileUtils.scanFileToPhotoAlbum(act, picPath);
        imgView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        win.setContentView(view);
    }

    /**
     * 注册条目点击事件
     */
    private void register() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Intent intent = new Intent(act, UserInfoUpdateActivity.class);
                int vCardType;
                switch (position) {
                    case 0: // 头像
                        if (type == 0) {
                            showDialog();
                        } else {
                            showImgDialog(avatarUrl);
                        }
                        break;
                    case 2: // 昵称
                        if (type == 0) {
                            vCardType = 2;
                            intent.putExtra("info", nickName);
                            intent.putExtra("type", vCardType);
                            startActivityForResult(intent, vCardType);
                        }
                        break;
                    case 3: // 性别
                        if (type == 0) {
                            vCardType = 3;
                            Intent sexIntent = new Intent(act, UserSexUpdateActivity.class);
                            sexIntent.putExtra("sex", sex);
                            startActivityForResult(sexIntent, vCardType);
                        }
                        break;
                    case 4: // 生日
                        if (type == 0) {
                            birth = new SelectBirthday(act, bday);
                            birth.showAtLocation(ll_root, Gravity.BOTTOM, 0, 0);
                            birth.setOnDismissListener(new PopupWindow.OnDismissListener() {
                                @Override
                                public void onDismiss() {
                                    MyLog.showLog("Birth:" + birth.getBirthday());
                                    if (birth.getBirthday() != null) {
                                        bday = birth.getBirthday();
                                        vCard.setField("BDAY", bday);
                                        vCardBean.setBday(bday);
                                        TextView tv_info = (TextView) mListView.getChildAt(position).findViewById(R.id.tv_info);
                                        tv_info.setText(bday);
                                    }
                                }
                            });
                        }
                        break;
                    case 5: // 地址
                        if (type == 0) {
                            vCardType = 5;
                            intent.putExtra("info", homeAddress);
                            intent.putExtra("type", vCardType);
                            startActivityForResult(intent, vCardType);
                        }
                        break;
                    case 6: // 邮箱
                        if (type == 0) {
                            vCardType = 6;
                            intent.putExtra("info", email);
                            intent.putExtra("type", vCardType);
                            startActivityForResult(intent, vCardType);
                        }
                        break;
                    case 7: // 电话
                        if (type == 0) {
                            vCardType = 7;
                            intent.putExtra("info", phone);
                            intent.putExtra("type", vCardType);
                            startActivityForResult(intent, vCardType);
                        }
                        break;
                    case 8: // 签名
                        if (type == 0) {
                            vCardType = 8;
                            intent.putExtra("info", desc);
                            intent.putExtra("type", vCardType);
                            startActivityForResult(intent, vCardType);
                        }
                        break;
                }
            }
        });
        btn_2.setOnClickListener(this);
        btn_1.setOnClickListener(this);
        ib_back.setOnClickListener(this);
        iv_flush.setOnClickListener(this);
    }

    /**
     * 方法 弹出一个对话框 让选择打开图库还是打开摄像头 修改头像用
     */
    private void showDialog() {
        new AlertDialog.Builder(this).setTitle("头像设置").setPositiveButton("拍照", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                // 调用系统的拍照功能
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 指定调用相机拍照后照片的储存路径
                tempFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + "PicTest_" + System.currentTimeMillis() + ".jpg");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
                startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
            }
        }).setNegativeButton("相册", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
            }
        }).show();
    }

    /**
     * 使用系统当前日期加以调整作为照片的名称
     *
     * @return
     */
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss", Locale.CHINA);
        return dateFormat.format(date) + ".jpg";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String info;
        if (requestCode == 10) {
            startPhotoZoom(Uri.fromFile(tempFile), 150);
        } else if (requestCode == 11 && data != null) {
            startPhotoZoom(data.getData(), 150);
        }
        if (data != null && requestCode != 11 && vCard != null) {
            info = data.getDataString();
            switch (requestCode) {
                case 0:
                    savePic(data);
                    ImageView iv_avatar = (ImageView) mListView.getChildAt(requestCode).findViewById(R.id.iv_avatar);
                    iv_avatar.setImageBitmap(bitmap);
                    break;
                case 2:
                    if (!TextUtils.isEmpty(info)) {
                        vCard.setNickName(info);
                        vCardBean.setNickName(info);
                        TextView tv_info = (TextView) mListView.getChildAt(requestCode).findViewById(R.id.tv_info);
                        tv_info.setText(info);
                    }
                    break;
                case 3:
                    if (!TextUtils.isEmpty(info)) {
                        vCard.setField("SEX", info);
                        vCardBean.setSex(info);
                        TextView tv_info = (TextView) mListView.getChildAt(requestCode).findViewById(R.id.tv_info);
                        tv_info.setText(info);
                    }
                    break;
                case 4:
                    break;
                case 5:
                    if (!TextUtils.isEmpty(info)) {
                        vCard.setField("HOME_ADDRESS", info);
                        vCardBean.setHomeAddress(info);
                        TextView tv_info = (TextView) mListView.getChildAt(requestCode).findViewById(R.id.tv_info);
                        tv_info.setText(info);
                    }
                    break;
                case 6:
                    if (!TextUtils.isEmpty(info)) {
                        vCard.setEmailHome(info);
                        vCardBean.setEmail(info);
                        TextView tv_info = (TextView) mListView.getChildAt(requestCode).findViewById(R.id.tv_info);
                        tv_info.setText(info);
                    }
                    break;
                case 7:
                    if (!TextUtils.isEmpty(info)) {
                        vCard.setField("PHONE", info);
                        vCardBean.setPhone(info);
                        TextView tv_info = (TextView) mListView.getChildAt(requestCode).findViewById(R.id.tv_info);
                        tv_info.setText(info);
                    }
                    break;
                case 8:
                    if (!TextUtils.isEmpty(info)) {
                        vCard.setField("DESC", info);
                        vCardBean.setDesc(info);
                        /**
                         * TODO 这地方还存在一个bug lv.getChildAt只能获取到可见的view 有可能会越界
                         */
                        TextView tv_info = (TextView) mListView.getChildAt(requestCode).findViewById(R.id.tv_info);
                        tv_info.setText(info);
                    }
                    break;
            }
        }
    }

    private void pdDismiss() {
        if (pd != null && pd.isShowing() && act != null) {
            pd.dismiss();
        }
    }

    /**
     * 方法 显示裁剪页面
     *
     * @param uri
     * @param size
     */
    private void startPhotoZoom(Uri uri, int size) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");

        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", size);
        intent.putExtra("outputY", size);
        intent.putExtra("return-data", true);

        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    /**
     * 截图并保存
     *
     * @param data
     */
    private void savePic(Intent data) {
        Bundle bundle = data.getExtras();
        if (bundle != null) {
            bitmap = bundle.getParcelable("data");
            avatarPath = MyPicUtils.saveFile(bitmap, dirPath, getPhotoFileName(), 60);
        }
    }

    /**
     * 初始化数据 查询VCard信息
     */
    private void initData() {
        pd = new MyDialog(act);
        bitmapUtils = new MyBitmapUtils(act);
        chatDao = ChatDao.getInstance(act);
        friendJid = getIntent().getStringExtra("friendJid");
        type = getIntent().getIntExtra("type", 0);
        switch (type) {
            case 0:  //个人修改信息界面
                btn_1.setVisibility(View.GONE);
                btn_2.setVisibility(View.VISIBLE);
                break;
            case 1:  // 陌生好友
                btn_1.setVisibility(View.GONE);
                btn_2.setVisibility(View.VISIBLE);
                break;
            case 2:  // 通讯录进入
                btn_1.setVisibility(View.VISIBLE);
                btn_2.setVisibility(View.VISIBLE);
                break;
            case 3:
                break;
        }
        if (friendJid == null) {
            if (connection != null && connection.isAuthenticated()) {
                vCardManager = VCardManager.getInstanceFor(connection);
            }
            ThreadUtil.runOnBackThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (vCardManager != null) {
                            vCard = vCardManager.loadVCard();
                        }
                    } catch (NoResponseException e) {
                        e.printStackTrace();
                    } catch (XMPPErrorException e) {
                        e.printStackTrace();
                    } catch (NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            friendName = friendJid.substring(0, friendJid.indexOf("@"));
        }
        if (connection != null && connection.isAuthenticated()) {
            queryVCard();
        }
    }

    /**
     * 方法 查询VCard信息
     */
    private void queryVCard() {
        pd.show();
        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                if (type == 0) {  // 个人信息修改界面
                    friendJid = MyApp.username + "@" + MyConstance.SERVICE_HOST;
                    vCardBean = chatDao.queryVCard(friendJid);
                    if (vCardBean == null) {
                        vCardBean = MyVCardUtils.queryVcard(null);
                        vCardBean.setJid(friendJid);
                        chatDao.replaceVCard(vCardBean);
                    }
                } else if (type == 1) {  // 查询的陌生人
                    vCardBean = chatDao.queryVCard(friendJid);
                    if (vCardBean == null) {
                        vCardBean = MyVCardUtils.queryVcard(friendJid);
                        vCardBean.setJid(friendJid);
                    }
                } else if (type == 2) {  // 从通讯录进入
                    vCardBean = chatDao.queryVCard(friendJid);
                    if (vCardBean == null) {
                        vCardBean = MyVCardUtils.queryVcard(friendJid);
                        vCardBean.setJid(friendJid);
                        chatDao.replaceVCard(vCardBean);
                    }
                }
                nickName = vCardBean.getNickName();
                homeAddress = vCardBean.getHomeAddress();
                email = vCardBean.getEmail();
                phone = vCardBean.getPhone();
                sex = vCardBean.getSex();
                desc = vCardBean.getDesc();
                bday = vCardBean.getBday();
                avatarUrl = vCardBean.getAvatarUrl();
                handler.sendEmptyMessage(QUERY_SUCCESS);
            }
        });
    }

    private void initView() {
        act = this;
        connection = MyApp.connection;
        ll_root = (LinearLayout) findViewById(R.id.ll_root);
        mListView = (ListView) findViewById(R.id.lv_userinfo);
        btn_2 = (Button) findViewById(R.id.btn_2);
        btn_1 = (Button) findViewById(R.id.btn_1);
        iv_flush = (ImageView) findViewById(R.id.iv_flush);
        ib_back = (ImageButton) findViewById(R.id.ib_back);
        tv_title = (TextView) findViewById(R.id.tv_title);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.ib_back:
                finish();
                break;
            case R.id.btn_1:
                Intent intent = new Intent(act,ChatActivity.class);
                intent.putExtra("friendName",friendName);
                startActivity(intent);
                break;
            case R.id.btn_2:
                if (type == 0) {
                    pd.show();
                    ThreadUtil.runOnBackThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (avatarPath != null) {
                                    FileBean bean = MyFileUtils.upLoadByHttpClient(avatarPath);
                                    if (bean != null) {
                                        avatarUrl = MyConstance.HOME_URL + bean.getResult();
                                        vCard.setField("AVATAR_URL", avatarUrl);
                                        vCardBean.setAvatarUrl(avatarUrl);
                                    }
                                }
                                if (vCardManager != null && vCard != null) {
                                    vCardManager.saveVCard(vCard);
                                    chatDao.replaceVCard(vCardBean);
                                    handler.sendEmptyMessage(SAVE_SUCCESS);
                                }
                            } catch (NoResponseException e) {
                                e.printStackTrace();
                            } catch (XMPPErrorException e) {
                                e.printStackTrace();
                            } catch (NotConnectedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } else if (type == 1) {
                    showAddDialog();
                } else if (type == 2) {
                    chatDao.deleteMsgByMark(friendName + "#" + MyApp.username);
                    Roster roster = Roster.getInstanceFor(connection);
                    RosterEntry entry = roster.getEntry(friendJid);
                    try {
                        if (entry != null) {
                            roster.removeEntry(entry);
                            MyUtils.showToast(act, "删除好友成功");
                        }
                        finish();
                    } catch (SmackException.NotLoggedInException e) {
                        e.printStackTrace();
                    } catch (SmackException.NoResponseException e) {
                        e.printStackTrace();
                    } catch (XMPPException.XMPPErrorException e) {
                        e.printStackTrace();
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.iv_flush:
                MyUtils.showToast(act, "刷新个人信息");
                if (connection != null && connection.isAuthenticated()) {
                    queryVCard();
                }
                break;
        }
    }

    /**
     * 方法 发出好友请求
     */
    private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(act);
        builder.setMessage("添加为好友？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                ThreadUtil.runOnBackThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            /**
                             * 添加好友不再是直接创建好友了，而是先发出一个订阅请求，对方同意后，才创建好友
                             */
                            Presence presence = new Presence(Presence.Type.subscribe);
                            presence.setTo(friendJid);
                            //在此处可以设置请求好友时发送的验证信息
                            presence.setStatus("您好，我是...");
                            connection.sendStanza(presence);

                            SubBean subBean = new SubBean();
                            subBean.setOwner(MyApp.username);
                            subBean.setFrom(MyApp.username + "@" + MyConstance.SERVICE_HOST);
                            subBean.setTo(friendJid);
                            subBean.setState("2");  // 2 表示发出好友申请
                            subBean.setDate(new Date().getTime());
                            subBean.setNick(nickName);
                            subBean.setAvatarUrl(avatarUrl);
                            subBean.setMsg(presence.getStatus());

                            chatDao.insertSub(subBean);

                            finish();
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    private class ViewHolder {
        public TextView item;
        public TextView info;
        public ImageView icon;
        public ImageView avatar;
        public ImageView back;
        public View bar;
    }

    private ArrayAdapter<String> mAdapter;
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case QUERY_SUCCESS:
                    if (type == 1) {
                        btn_2.setText("添加新朋友");
                        tv_title.setText(nickName);
                        btn_2.setBackgroundResource(R.drawable.btn_login_selector);
                    } else if (type == 0) {
                        btn_2.setText("保 存");
                        tv_title.setText("我的信息");
                        btn_2.setBackgroundResource(R.drawable.btn_login_selector);
                    } else if (type == 2) {
                        btn_2.setText("删除朋友");
                        tv_title.setText(nickName);
                        btn_2.setBackgroundResource(R.drawable.btn_delete_selector);
                    }

                    // 为listView设置数据
                    mAdapter = new ArrayAdapter<String>(act, 0, items) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            ViewHolder vh;
                            if (convertView == null) {
                                convertView = View.inflate(act, R.layout.list_item_userinfo, null);
                                vh = new ViewHolder();
                                vh.item = (TextView) convertView.findViewById(R.id.tv_item);
                                vh.info = (TextView) convertView.findViewById(R.id.tv_info);
                                vh.avatar = (ImageView) convertView.findViewById(R.id.iv_avatar);
                                vh.icon = (ImageView) convertView.findViewById(R.id.iv_icon);
                                vh.back = (ImageView) convertView.findViewById(R.id.iv_right_back);
                                vh.bar = convertView.findViewById(R.id.v_bar);
                                convertView.setTag(vh);
                            } else {
                                vh = (ViewHolder) convertView.getTag();
                            }
                            vh.item.setText(items[position]);
                            vh.icon.setImageResource(icons[position]);
                            if (position == 0) {
                                vh.avatar.setVisibility(View.VISIBLE);
                                vh.info.setVisibility(View.GONE);
                            } else {
                                vh.avatar.setVisibility(View.GONE);
                                vh.info.setVisibility(View.VISIBLE);
                            }
                            switch (position) {
                                case 0:
                                    if (avatarUrl != null) {
                                        vh.avatar.setTag(position);
                                        bitmapUtils.display(vh.avatar, avatarUrl);
                                    } else {
                                        vh.avatar.setImageResource(R.drawable.ic_launcher);
                                    }
                                    vh.bar.setVisibility(View.GONE);
                                    vh.back.setVisibility(View.VISIBLE);
                                    break;
                                case 1:
                                    if (TextUtils.isEmpty(friendJid)) {
                                        vh.info.setText("未填写");
                                    } else {
                                        vh.info.setText(friendJid.substring(0, friendJid.indexOf("@")));
                                    }
                                    vh.bar.setVisibility(View.VISIBLE);
                                    vh.back.setVisibility(View.GONE);
                                    break;
                                case 2:
                                    if (TextUtils.isEmpty(nickName)) {
                                        vh.info.setText("未填写");
                                    } else {
                                        vh.info.setText(nickName);
                                    }
                                    vh.bar.setVisibility(View.GONE);
                                    vh.back.setVisibility(View.VISIBLE);
                                    break;
                                case 3:
                                    if (TextUtils.isEmpty(sex)) {
                                        vh.info.setText("未填写");
                                    } else {
                                        vh.info.setText(sex);
                                    }
                                    vh.bar.setVisibility(View.VISIBLE);
                                    vh.back.setVisibility(View.VISIBLE);
                                    break;
                                case 4:
                                    if (TextUtils.isEmpty(bday)) {
                                        vh.info.setText("未填写");
                                    } else {
                                        vh.info.setText(bday);
                                    }
                                    vh.bar.setVisibility(View.VISIBLE);
                                    vh.back.setVisibility(View.VISIBLE);
                                    break;
                                case 5:
                                    if (TextUtils.isEmpty(homeAddress)) {
                                        vh.info.setText("未填写");
                                    } else {
                                        vh.info.setText(homeAddress);
                                    }
                                    vh.bar.setVisibility(View.GONE);
                                    vh.back.setVisibility(View.VISIBLE);
                                    break;
                                case 6:
                                    if (TextUtils.isEmpty(email)) {
                                        vh.info.setText("未填写");
                                    } else {
                                        vh.info.setText(email);
                                    }
                                    vh.bar.setVisibility(View.VISIBLE);
                                    vh.back.setVisibility(View.VISIBLE);
                                    break;
                                case 7:
                                    if (TextUtils.isEmpty(phone)) {
                                        vh.info.setText("未填写");
                                    } else {
                                        vh.info.setText(phone);
                                    }
                                    vh.bar.setVisibility(View.GONE);
                                    vh.back.setVisibility(View.VISIBLE);
                                    break;
                                case 8:
                                    if (TextUtils.isEmpty(desc)) {
                                        vh.info.setText("未填写");
                                    } else {
                                        vh.info.setText(desc);
                                    }
                                    vh.bar.setVisibility(View.VISIBLE);
                                    vh.back.setVisibility(View.VISIBLE);
                                    break;
                            }
                            if (type != 0) {
                                vh.back.setVisibility(View.GONE);
                            }
                            return convertView;
                        }
                    };
                    mListView.setAdapter(mAdapter);
                    pdDismiss();
                    break;
                case SAVE_SUCCESS:
                    if (connection != null && connection.isAuthenticated()) {
                        queryVCard();
                    }
                    break;
            }
        }
    };
}
