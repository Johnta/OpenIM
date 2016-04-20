package com.open.im.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.open.im.R;
import com.open.im.db.OpenIMDao;
import com.open.im.utils.MyFileUtils;
import com.open.im.utils.MyUtils;

import java.io.File;

/**
 * Created by Administrator on 2016/4/12.
 */
public class CleanCacheActivity extends Activity implements View.OnClickListener {

    private ImageButton ib_back;
    private Button btn_clean;
    private CleanCacheActivity act;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clean_cache);
        act = this;
        initView();

        register();
    }

    private void register() {
        ib_back.setOnClickListener(this);
        btn_clean.setOnClickListener(this);
    }

    private void initView() {
        ib_back = (ImageButton) findViewById(R.id.ib_back);
        btn_clean = (Button) findViewById(R.id.btn_clean);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ib_back:
                finish();
                break;
            case R.id.btn_clean:
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/exiu/";
                File file = new File(filePath);
                //删除缓存文件
                MyFileUtils.deleteFile(file);
//                ChatDao chatDao = ChatDao.getInstance(act);
                OpenIMDao openIMDao = OpenIMDao.getInstance(act);
                //删除缓存信息
//                chatDao.deleteAllMsg();
                openIMDao.deleteAllMessage();
                //删除好友请求
//                chatDao.deleteAllSub();
                openIMDao.deleteAllSub();
                //清空VCard缓存
//                chatDao.deleteAllVcard();
                openIMDao.deleteAllVCard();
                MyUtils.showToast(act, "清空缓存成功");
                break;
        }
    }
}
