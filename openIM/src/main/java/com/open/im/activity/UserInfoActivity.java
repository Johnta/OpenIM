package com.open.im.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.bean.VCardBean;
import com.open.im.db.ChatDao;
import com.open.im.utils.MyPicUtils;
import com.open.im.utils.ThreadUtil;
import com.open.im.wheel.SelectBirthday;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 用户信息界面
 */
public class UserInfoActivity extends Activity {

    private static final int QUERY_SUCCESS = 100;
    private static final int SAVE_SUCCESS = 101;
    private ListView mListview;
    private UserInfoActivity act;
    private String[] items = {"头像", "昵称", "性别", "生日", "地址", "邮箱", "电话", "签名"};
    private VCard vCard;
    private String nickName;
    private String homeAddress;
    private String email;
    private String phone;
    private String sex;
    private String desc;
    private String bday;
    private Bitmap bitmap;
    private byte[] avatar;
    private ImageButton ib_back;
    protected SelectBirthday birth;
    private VCardManager vCardManager;
    private LinearLayout ll_root;

    /**
     * 头像用
     */
    private static final int PHOTO_REQUEST_TAKEPHOTO = 10;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 11;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 0;// 结果

    private File tempFile;
    private String dirPath = Environment.getExternalStorageDirectory() + "/exiu/cache/avatar/";
    private String friendJid;
    private AbstractXMPPConnection connection;
    private ProgressDialog pd;
    private ChatDao chatDao;
    private VCardBean vCardBean;

    // 创建一个以当前时间为名称的文件
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);

        // 初始化控件
        initView();
        // 初始化数据
        initData();

    }

    /**
     * 注册条目点击事件
     */
    private void register() {
        mListview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(act, UserInfoUpdateActivity.class);
                int type;
                switch (position) {
                    case 0: // 头像
                        showDialog();
                        break;
                    case 1: // 昵称
                        type = 1;
                        intent.putExtra("info", nickName);
                        intent.putExtra("type", type);
                        startActivityForResult(intent, type);
                        break;
                    case 2: // 性别
                        type = 2;
                        Intent sexIntent = new Intent(act, UserSexUpdateActivity.class);
                        sexIntent.putExtra("sex", sex);
                        startActivityForResult(sexIntent, type);
                        break;
                    case 3: // 生日
                        birth = new SelectBirthday(act, bday);
                        birth.showAtLocation(ll_root, Gravity.BOTTOM, 0, 0);
                        birth.setOnDismissListener(new OnDismissListener() {
                            @Override
                            public void onDismiss() {
                                ThreadUtil.runOnBackThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            /**
                                             * 重新加载一个vCard，如果还是使用以前的VCard，
                                             * 那么那个VCard对象里面存的日期还是原来的日期
                                             */
                                            vCard = vCardManager.loadVCard();
                                            bday = vCard.getField("BDAY");
                                        } catch (NoResponseException e) {
                                            e.printStackTrace();
                                        } catch (XMPPErrorException e) {
                                            e.printStackTrace();
                                        } catch (NotConnectedException e) {
                                            e.printStackTrace();
                                        }
                                        handler.sendEmptyMessage(QUERY_SUCCESS);
                                    }
                                });
                            }
                        });
                        break;
                    case 4: // 地址
                        type = 4;
                        intent.putExtra("info", homeAddress);
                        intent.putExtra("type", type);
                        startActivityForResult(intent, type);
                        break;
                    case 5: // 邮箱
                        type = 5;
                        intent.putExtra("info", email);
                        intent.putExtra("type", type);
                        startActivityForResult(intent, type);
                        break;
                    case 6: // 电话
                        type = 6;
                        intent.putExtra("info", phone);
                        intent.putExtra("type", type);
                        startActivityForResult(intent, type);
                        break;
                    case 7: // 签名
                        type = 7;
                        intent.putExtra("info", desc);
                        intent.putExtra("type", type);
                        startActivityForResult(intent, type);
                        break;
                }
            }
        });
    }

    /**
     * 方法 弹出一个对话框 让选择打开图库还是打开摄像头 修改头像用
     */
    private void showDialog() {
        new AlertDialog.Builder(this).setTitle("头像设置").setPositiveButton("拍照", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
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
        if (data != null && requestCode != 11) {
            info = data.getDataString();
            pd = new ProgressDialog(act);
            pd.setMessage("修改信息中，请稍后...");
            pd.show();

            switch (requestCode) {
                case 0:
                    savePic(data);
                    break;
                case 1:
                    if (!TextUtils.isEmpty(info)) {
                        vCard.setNickName(info);
                    }
                    break;
                case 2:
                    if (!TextUtils.isEmpty(info)) {
                        vCard.setField("SEX", info);
                    }
                    break;
                case 3:

                    break;
                case 4:
                    if (!TextUtils.isEmpty(info)) {
                        vCard.setField("HOME_ADDRESS", info);
                    }
                    break;
                case 5:
                    if (!TextUtils.isEmpty(info)) {
                        vCard.setEmailHome(info);
                    }
                    break;
                case 6:
                    if (!TextUtils.isEmpty(info)) {
                        vCard.setField("PHONE", info);
                    }
                    break;
                case 7:
                    if (!TextUtils.isEmpty(info)) {
                        vCard.setField("DESC", info);
                    }
                    break;
            }

            ThreadUtil.runOnBackThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        vCardManager.saveVCard(vCard);
                        handler.sendEmptyMessage(SAVE_SUCCESS);
                    } catch (NoResponseException e) {
                        e.printStackTrace();
                    } catch (XMPPErrorException e) {
                        e.printStackTrace();
                    } catch (NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
            });
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
            Bitmap photo = bundle.getParcelable("data");
            final String avatarPath = MyPicUtils.saveFile(photo, dirPath, getPhotoFileName(), 60);
            File avatarFile = new File(avatarPath);
            try {
                byte[] bys = getFileBytes(avatarFile);
                String encodedImage = StringUtils.encodeHex(bys);
                vCard.setAvatar(bys, encodedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据文件获取字节数组
     *
     * @param file
     * @return
     * @throws IOException
     */
    private static byte[] getFileBytes(File file) throws IOException {
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            int bytes = (int) file.length();
            byte[] buffer = new byte[bytes];
            int readBytes = bis.read(buffer);
            if (readBytes != buffer.length) {
                throw new IOException("Entire file not read");
            }
            return buffer;
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
    }

    /**
     * 初始化数据 查询VCard信息
     */
    private void initData() {

        chatDao = ChatDao.getInstance(act);
        vCardBean = chatDao.queryVCard(null);

        vCardManager = VCardManager.getInstanceFor(MyApp.connection);
        try {
            if (friendJid == null) {
                vCard = vCardManager.loadVCard();
                // 注册点击事件
                register();
            } else {
                vCard = vCardManager.loadVCard(friendJid);
            }
//			vCard = vCardManager.loadVCard("vb@" + MyApp.connection.getServiceName());
            // vCard.setField("HOME_ADDRESS", "河南郑州");
            // // vCard.setField("EMAIL","1365260037@qq.com");
            // vCard.setEmailHome("1365260037@qq.com");
            // vCard.setField("PHONE", "110");
            // vCard.setField("SEX", "男");
            // vCard.setField("DESC", "我是一只小蚂蚁~~~");
            // vCard.setField("BDAY", "1989-1-1");
            // //将信息保存到VCard中
            // vCardManager.saveVCard(vCard);
            queryVCard(vCard);
        } catch (NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPErrorException e) {
            e.printStackTrace();
        } catch (NotConnectedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 方法 查询VCard信息
     *
     * @param vCard
     */
    private void queryVCard(final VCard vCard) {
        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                nickName = vCard.getNickName();
                homeAddress = vCard.getField("HOME_ADDRESS");
                email = vCard.getEmailHome();
                phone = vCard.getField("PHONE");
                sex = vCard.getField("SEX");
                desc = vCard.getField("DESC");
                bday = vCard.getField("BDAY");
                avatar = vCard.getAvatar();
                if (avatar != null){
                    bitmap = BitmapFactory.decodeByteArray(avatar,0,avatar.length);
                }
                handler.sendEmptyMessage(QUERY_SUCCESS);
            }
        });
    }

    private void initView() {
        act = this;
        connection = MyApp.connection;
        friendJid = getIntent().getStringExtra("friendJid");
        ll_root = (LinearLayout) findViewById(R.id.ll_root);
        mListview = (ListView) findViewById(R.id.lv_userinfo);
        ib_back = (ImageButton) findViewById(R.id.ib_back);
        ib_back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private class ViewHolder {
        TextView item;
        TextView info;
        ImageView icon;
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case QUERY_SUCCESS:

                    pdDismiss();

                    // 为listView设置数据
                    mListview.setAdapter(new ArrayAdapter<String>(act, 0, items) {
                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            ViewHolder vh;
                            if (convertView == null) {
                                convertView = View.inflate(act, R.layout.list_item_userinfo, null);
                                vh = new ViewHolder();
                                vh.item = (TextView) convertView.findViewById(R.id.tv_item);
                                vh.info = (TextView) convertView.findViewById(R.id.tv_info);
                                vh.icon = (ImageView) convertView.findViewById(R.id.iv_icon);
                                convertView.setTag(vh);
                            } else {
                                vh = (ViewHolder) convertView.getTag();
                            }
                            vh.item.setText(items[position]);
                            if (position == 0) {
                                vh.icon.setVisibility(View.VISIBLE);
                                vh.info.setVisibility(View.GONE);
                            } else {
                                vh.icon.setVisibility(View.GONE);
                                vh.info.setVisibility(View.VISIBLE);
                            }

                            switch (position) {
                                case 0:
                                    if (bitmap != null) {
//                                        Bitmap bitmap = BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
                                        vh.icon.setImageBitmap(bitmap);
                                    } else {
                                        vh.icon.setImageResource(R.drawable.ic_launcher);
                                    }
                                    break;
                                case 1:
                                    if (TextUtils.isEmpty(nickName)) {
                                        vh.info.setText("未填写");
                                    } else {
                                        vh.info.setText(nickName);
                                    }
                                    break;
                                case 2:
                                    if (TextUtils.isEmpty(sex)) {
                                        vh.info.setText("未填写");
                                    } else {
                                        vh.info.setText(sex);
                                    }
                                    break;
                                case 3:
                                    if (TextUtils.isEmpty(bday)) {
                                        vh.info.setText("未填写");
                                    } else {
                                        vh.info.setText(bday);
                                    }
                                    break;
                                case 4:
                                    if (TextUtils.isEmpty(homeAddress)) {
                                        vh.info.setText("未填写");
                                    } else {
                                        vh.info.setText(homeAddress);
                                    }
                                    break;
                                case 5:
                                    if (TextUtils.isEmpty(email)) {
                                        vh.info.setText("未填写");
                                    } else {
                                        vh.info.setText(email);
                                    }
                                    break;
                                case 6:
                                    if (TextUtils.isEmpty(phone)) {
                                        vh.info.setText("未填写");
                                    } else {
                                        vh.info.setText(phone);
                                    }
                                    break;
                                case 7:
                                    if (TextUtils.isEmpty(desc)) {
                                        vh.info.setText("未填写");
                                    } else {
                                        vh.info.setText(desc);
                                    }
                                    break;
                            }
                            return convertView;
                        }
                    });
                    break;
                case SAVE_SUCCESS:
                    queryVCard(vCard);
                    break;
            }
        }
    };

    @Override
    /**
     * 页面不可见时，保存修改的信息到数据库
     */
    protected void onPause() {
        if (vCardBean != null) {
            vCardBean.setJid(MyApp.username + "@" + MyApp.connection.getServiceName());
            vCardBean.setNickName(nickName);
            vCardBean.setAvatar(avatar);
            vCardBean.setSex(sex);
            vCardBean.setBday(bday);
            vCardBean.setEmail(email);
            vCardBean.setHomeAddress(homeAddress);
            vCardBean.setPhone(phone);
            vCardBean.setDesc(desc);
            ThreadUtil.runOnBackThread(new Runnable() {
                @Override
                public void run() {
                    chatDao.replaceVCard(vCardBean);
                }
            });
        }
        super.onPause();
    }
}
