package com.open.im.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.app.MyApp;
import com.open.im.bean.VCardBean;
import com.open.im.db.ChatDao;
import com.open.im.utils.MyPicUtils;
import com.open.im.utils.ThreadUtil;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
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
 * 批量修改个人信息 还没有使用
 * Created by Administrator on 2016/4/1.
 */
public class InfoActivity_2 extends Activity implements View.OnClickListener {

    private static final int QUERY_SUCCESS = 100;
    private static final int SAVE_SUCCESS = 101;
    private EditText et_nick, et_email, et_phone, et_address, et_desc;
    private TextView tv_username, tv_sex, tv_birthday;
    private LinearLayout ll_sex, ll_birthday;
    private ImageButton ib_back;
    private Button btn_save;
    private ImageView iv_camera;
    private InfoActivity_2 act;
    private ChatDao chatDao;
    private VCardBean vCardBean;

    private String nickName;
    private String homeAddress;
    private String email;
    private String phone;
    private String sex;
    private String desc;
    private String bday;
    private byte[] avatar;

    /**
     * 头像用
     */
    private static final int PHOTO_REQUEST_TAKEPHOTO = 10;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 11;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 0;// 结果

    private File tempFile;
    private String dirPath = Environment.getExternalStorageDirectory() + "/exiu/cache/avatar/";
    private VCard vCard;
    private VCardManager vCardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_2);
        act = this;
        initView();

        initData();

        register();
    }

    private void register() {
        btn_save.setOnClickListener(this);
        ib_back.setOnClickListener(this);
        ll_sex.setOnClickListener(this);
        ll_birthday.setOnClickListener(this);
        iv_camera.setOnClickListener(this);
    }

    private void initData() {
        chatDao = ChatDao.getInstance(act);
        vCardManager = VCardManager.getInstanceFor(MyApp.connection);
        try {
            vCard = vCardManager.loadVCard();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                vCardBean = chatDao.queryVCard(MyApp.username + "@" + MyApp.connection.getServiceName());
                nickName = vCardBean.getNickName();
                homeAddress = vCardBean.getHomeAddress();
                email = vCardBean.getEmail();
                bday = vCardBean.getBday();
                phone = vCardBean.getPhone();
                desc = vCardBean.getDesc();
//                avatar = vCardBean.getAvatar();
                handler.sendEmptyMessage(QUERY_SUCCESS);
            }
        });

    }

    private void initView() {
        et_nick = (EditText) findViewById(R.id.et_nick);
        et_email = (EditText) findViewById(R.id.et_email);
        et_phone = (EditText) findViewById(R.id.et_phone);
        et_address = (EditText) findViewById(R.id.et_address);
        et_desc = (EditText) findViewById(R.id.et_desc);

        tv_username = (TextView) findViewById(R.id.tv_username);
        tv_sex = (TextView) findViewById(R.id.tv_sex);
        tv_birthday = (TextView) findViewById(R.id.tv_birthday);

        ll_birthday = (LinearLayout) findViewById(R.id.ll_birthday);
        ll_sex = (LinearLayout) findViewById(R.id.ll_sex);

        ib_back = (ImageButton) findViewById(R.id.ib_back);
        btn_save = (Button) findViewById(R.id.btn_save);

        iv_camera = (ImageView) findViewById(R.id.iv_camera);
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case QUERY_SUCCESS:
                    if (avatar != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
                        iv_camera.setImageBitmap(bitmap);
                    } else {
                        iv_camera.setImageResource(R.mipmap.ic_launcher);
                    }
                    if (MyApp.username != null) {
                        tv_username.setText(MyApp.username);
                    }
                    if (nickName != null) {
                        et_nick.setText(nickName);
                    }
                    if (sex != null) {
                        tv_sex.setText(sex);
                    } else {
                        tv_sex.setText("未填写");
                    }
                    if (bday != null) {
                        tv_birthday.setText(bday);
                    } else {
                        tv_birthday.setText("未填写");
                    }
                    if (homeAddress != null) {
                        et_address.setText(homeAddress);
                    }
                    if (email != null) {
                        et_email.setText(email);
                    }
                    if (phone != null) {
                        et_phone.setText(phone);
                    }
                    if (desc != null) {
                        et_desc.setText(desc);
                    }
                    break;
                case SAVE_SUCCESS:
                    finish();
                    break;
            }
        }
    };

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.ib_back:
                finish();
                break;
            case R.id.btn_save:

                nickName = et_nick.getText().toString().trim();
                sex = tv_sex.getText().toString();
                bday = tv_birthday.getText().toString();
                email = et_email.getText().toString();
                homeAddress = et_address.getText().toString();
                desc = et_desc.getText().toString();
                phone = et_phone.getText().toString();

                vCard.setNickName(nickName);
                vCard.setField("SEX", sex);
                vCard.setField("HOME_ADDRESS", homeAddress);
                vCard.setEmailHome(email);
                vCard.setField("PHONE", phone);
                vCard.setField("DESC", desc);
                vCard.setField("BDAY",bday);

                if (vCardBean != null) {
                    vCardBean.setJid(MyApp.username + "@" + MyApp.connection.getServiceName());
                    vCardBean.setNickName(nickName);
//                    vCardBean.setAvatar(avatar);
                    vCardBean.setSex(sex);
                    vCardBean.setBday(bday);
                    vCardBean.setEmail(email);
                    vCardBean.setHomeAddress(homeAddress);
                    vCardBean.setPhone(phone);
                    vCardBean.setDesc(desc);
                    ThreadUtil.runOnBackThread(new Runnable() {
                        @Override
                        public void run() {
//                            chatDao.replaceVCard(vCardBean);
                            try {
                                vCardManager.saveVCard(vCard);
                                handler.sendEmptyMessage(SAVE_SUCCESS);
                            } catch (SmackException.NoResponseException e) {
                                e.printStackTrace();
                            } catch (XMPPException.XMPPErrorException e) {
                                e.printStackTrace();
                            } catch (SmackException.NotConnectedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                break;
            case R.id.ll_sex:

                break;
            case R.id.ll_birthday:
                break;
            case R.id.iv_camera:
                showDialog();
                break;
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            startPhotoZoom(Uri.fromFile(tempFile), 150);
        } else if (requestCode == 11) {
            startPhotoZoom(data.getData(), 150);
        } else if (requestCode == 0 && data != null) {
            savePic(data);
        }
    }
}
