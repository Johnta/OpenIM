package com.open.im.db;

import android.database.sqlite.SQLiteDatabase;

import com.open.im.bean.MessageBean;
import com.open.im.bean.SubBean;
import com.open.im.bean.VCardBean;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.Collection;

/**
 * 使用LitePal操作数据库
 * Created by Administrator on 2016/4/18.
 */
public class OpenIMDao {

    private static OpenIMDao instance;

    private OpenIMDao() {
        SQLiteDatabase db = Connector.getDatabase();
    }

    /**
     * 单例模式
     *
     * @return openIMDao对象
     */
    public static synchronized OpenIMDao getInstance() {
        if (instance == null) {
            instance = new OpenIMDao();
        }
        return instance;
    }

    /**
     * 保存成组的VCard信息
     *
     * @param list 成组的VCardBean
     */
    private void saveAllVCard(Collection<VCardBean> list) {
        DataSupport.saveAll(list);
    }

    /**
     * 保存一条VCard信息到数据库
     * @param vCardBean
     */
    private void saveSingleVCard(VCardBean vCardBean) {
        vCardBean.save();
    }
    /**
     * 保存一条聊天信息到数据库
     * @param messageBean
     */
    private void saveSingleMessage(MessageBean messageBean){
        messageBean.save();
    }
    /**
     * 保存一条好友订阅信息到数据库
     * @param subBean
     */
    private void saveSingleSub(SubBean subBean){
        subBean.save();
    }
}
