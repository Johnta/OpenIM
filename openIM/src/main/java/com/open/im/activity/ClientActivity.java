package com.open.im.activity;

import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.open.im.R;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyUtils;
import com.open.im.utils.QRCodeUtil;
import com.open.im.utils.ThreadUtil;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

/**
 * 关于客户端界面
 * Created by Administrator on 2016/4/13.
 */
public class ClientActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.tv_version)
    TextView tvVersion;
    @BindView(R.id.iv_qrcode)
    ImageView ivQrcode;
    @BindView(R.id.tv_share)
    TextView tvShare;
    private ClientActivity act;
    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        ButterKnife.bind(this);
        act = this;
        initView();
    }

    private void initView() {
        /**
         * 获得包管理器，手机中所有应用，共用一个包管理器
         */
        PackageManager packageManager = act.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(act.getPackageName(), 0);
            String versionNameStr = packageInfo.versionName;
            tvVersion.setText("OpenIM " + versionNameStr);
            filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/exiu/OpenIM_" + versionNameStr + ".jpg";
            MyUtils.showToast(act, "当前版本号:" + versionNameStr);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists()) {
                ivQrcode.setImageBitmap(BitmapFactory.decodeFile(filePath));
            } else {
                //如果二维码不存在 则创建 存在则直接显示
                createQRCode();
            }
        }
    }

    /**
     * 根据链接创建二维码并设置显示
     */
    public void createQRCode() {
        //二维码图片较大时，生成图片、保存文件的时间可能较长，因此放在新线程中
        ThreadUtil.runOnBackThread(new Runnable() {
            @Override
            public void run() {
                boolean success = QRCodeUtil.createQRImage(MyConstance.CLIENT_URL, 600, 600,
                        BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher),
                        filePath);

                if (success) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ivQrcode.setImageBitmap(BitmapFactory.decodeFile(filePath));
                        }
                    });
                }
            }
        });
    }

    /**
     * 方法 点击二维码 单独显示二维码
     */
    private void showImgDialog() {
        final AlertDialog dialog = new AlertDialog.Builder(act, R.style.Lam_Dialog_FullScreen).create();
        Window win = dialog.getWindow();
        win.setGravity(Gravity.FILL);
        // 隐藏手机最上面的状态栏
        win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        dialog.show();

        View view = View.inflate(act, R.layout.dialog_qrcode, null);
        ImageView imgView = (ImageView) view.findViewById(R.id.iv_qrcode);

        imgView.setImageBitmap(BitmapFactory.decodeFile(filePath));

        imgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
        win.setContentView(view);
    }

    @Override
    public void onBackPressed() {
//        Intent intent = new Intent(act, MainActivity.class);
//        intent.putExtra("selection",3);
//        startActivity(intent);
        super.onBackPressed();
    }

    @OnClick({R.id.ib_back, R.id.tv_back, R.id.iv_qrcode, R.id.tv_share})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_qrcode:
                showImgDialog();
                break;
            case R.id.ib_back:
            case R.id.tv_back:
//                Intent intent = new Intent(act, MainActivity.class);
//                intent.putExtra("selection",3);
//                startActivity(intent);
                finish();
                break;
            case R.id.tv_share:
                showShare();
                break;
        }
    }

    private void showShare() {
        ShareSDK.initSDK(this);
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

        // 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle(getString(R.string.share));
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl(MyConstance.CLIENT_URL);
        // text是分享文本，所有平台都需要这个字段
        oks.setText("欢迎使用OpenIM，点击下载");
        // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
        oks.setImagePath(filePath);//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl(MyConstance.CLIENT_URL);
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl(MyConstance.CLIENT_URL);

        // 启动分享GUI
        oks.show(this);
    }

}
