package com.open.im.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.open.im.R;
import com.open.im.utils.MyUtils;

import java.io.File;

/**
 * Created by Administrator on 2016/4/14.
 */
public class AvatarActivity extends Activity implements View.OnClickListener {

    private ImageButton ib_back;
    private ImageView iv_avatar;
    private ImageView iv_save;
    private ImageView iv_more;
    private AvatarActivity act;
    private int type;
    private PopupWindow popupWindow;
    private RelativeLayout rl_save;
    private RelativeLayout rl_pic;
    private RelativeLayout rl_camera;
    private File tempFile;

    /**
     * 头像用
     */
    private static final int PHOTO_REQUEST_TAKEPHOTO = 10;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 11;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 0;// 结果

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar);
        act = this;

        initView();

        initData();

        register();
    }

    private void initData() {
        String nickName = getIntent().getStringExtra("nickName");
        type = getIntent().getIntExtra("type",0);
        if (type == 0){
            iv_save.setVisibility(View.GONE);
            iv_more.setVisibility(View.VISIBLE);
            initPopupWindow();
        } else {
            iv_more.setVisibility(View.GONE);
            iv_save.setVisibility(View.VISIBLE);
        }
    }

    private void register() {
        ib_back.setOnClickListener(this);
        iv_save.setOnClickListener(this);
        iv_more.setOnClickListener(this);
    }

    private void initView() {
        ib_back = (ImageButton) findViewById(R.id.ib_back);
        iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
        iv_save = (ImageView) findViewById(R.id.iv_save);
        iv_more = (ImageView) findViewById(R.id.iv_more);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ib_back:
                finish();
                break;
            case R.id.iv_more:
                MyUtils.showToast(act,"弹出pop");
                showPop(iv_more);
                break;
            case R.id.iv_save:
                MyUtils.showToast(act,"保存图片");
                break;
            case R.id.rl_save:
                MyUtils.showToast(act,"保存图片");

                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
                break;
            case R.id.rl_pic:
                MyUtils.showToast(act,"打开图库");
                Intent picIntent = new Intent(Intent.ACTION_PICK, null);
                picIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(picIntent, PHOTO_REQUEST_GALLERY);
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
                break;
            case R.id.rl_camera:
                MyUtils.showToast(act,"打开相机");
                // 调用系统的拍照功能
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // 指定调用相机拍照后照片的储存路径
                tempFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + File.separator + "PicTest_" + System.currentTimeMillis() + ".jpg");
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(tempFile));
                startActivityForResult(cameraIntent, PHOTO_REQUEST_TAKEPHOTO);
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            startPhotoZoom(Uri.fromFile(tempFile), 150);
        } else if (requestCode == 11 && data != null) {
            startPhotoZoom(data.getData(), 150);
        }
    }

    /**
     * 初始化PopupWindow
     */
    private void initPopupWindow() {
        View view = View.inflate(act, R.layout.pop_item_avatar, null);
        popupWindow = new PopupWindow();
        popupWindow.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(view);

        rl_save = (RelativeLayout) view.findViewById(R.id.rl_save);
        rl_pic = (RelativeLayout) view.findViewById(R.id.rl_pic);
        rl_camera = (RelativeLayout) view.findViewById(R.id.rl_camera);
    }

    /**
     * PopupWindow显示
     *
     * @param v
     */
    private void showPop(View v) {
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());// 设置此项可点击Popupwindow外区域消失，注释则不消失

        // 设置出现位置
        int[] location = new int[2];
        v.getLocationOnScreen(location);
//        popupWindow.showAtLocation(v, Gravity.NO_GRAVITY,
//                location[0] + v.getWidth() / 2 - popupWindow.getWidth() / 2,
//                location[1] - popupWindow.getHeight());
        popupWindow.showAsDropDown(v);

        rl_save.setOnClickListener(this);
        rl_pic.setOnClickListener(this);
        rl_camera.setOnClickListener(this);
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
}
