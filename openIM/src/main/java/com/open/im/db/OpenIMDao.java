package com.open.im.db;

import android.content.Context;

import com.open.im.bean.MessageBean;
import com.open.im.bean.SubBean;
import com.open.im.bean.VCardBean;
import com.open.im.utils.MyConstance;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 使用LitePal操作数据库
 * Created by Administrator on 2016/4/18.
 */
public class OpenIMDao {

    private static OpenIMDao instance;
    private final Context ctx;

    private OpenIMDao(Context ctx) {
        this.ctx = ctx;
        // 创建数据库
        Connector.getDatabase();
    }

    /**
     * 单例模式
     *
     * @return openIMDao对象
     */
    public static synchronized OpenIMDao getInstance(Context ctx) {
        if (instance == null) {
            instance = new OpenIMDao(ctx);
        }
        return instance;
    }

    /**====================================== 操作VCard ==========================================*/

    /**
     * 保存成组的VCard信息
     *
     * @param list 成组的VCardBean
     */
    private void saveAllVCard(Collection<VCardBean> list) {
        deleteAllVCard();
        DataSupport.saveAll(list);
    }

    /**
     * 保存一条VCard信息到数据库
     *
     * @param vCardBean
     */
    private void saveSingleVCard(VCardBean vCardBean) {
        vCardBean.save();
    }

    /**
     * 更新单个的VCard信息
     *
     * @param vCardBean
     */
    private void updateSingleVCard(VCardBean vCardBean) {
        VCardBean singleVCard = findSingleVCard(vCardBean.getJid());
        if (singleVCard != null) {
            singleVCard.delete();
        }
        vCardBean.save();
    }

    /**
     * 删除指定的一条VCard
     *
     * @param userJid
     */
    private void deleteSingleVCard(String userJid) {
        VCardBean singleVCard = findSingleVCard(userJid);
        if (singleVCard != null) {
            singleVCard.delete();
        }
    }

    /**
     * 删除VCard表中所有的数据
     */
    private void deleteAllVCard() {
        DataSupport.deleteAll(VCardBean.class);
    }

    /**
     * 根据userJid查询相应的VCard信息
     *
     * @param userJid
     * @return
     */
    private VCardBean findSingleVCard(String userJid) {
        List<VCardBean> vCardBeans = DataSupport.where(DBcolumns.VCARD_JID + " = ?", userJid).find(VCardBean.class);
        if (vCardBeans != null && vCardBeans.size() > 0) {
            return vCardBeans.get(0);
        }
        return null;
    }

    /**
     * 查询所有的VCard信息
     *
     * @return
     */
    private List<VCardBean> findAllVCard() {
        return DataSupport.findAll(VCardBean.class);
    }

    /**====================================== 操作聊天信息 ==========================================*/

    /**
     * 保存一条聊天信息到数据库
     *
     * @param messageBean
     */
    private void saveSingleMessage(MessageBean messageBean) {
        messageBean.save();
        // 发出通知，群组数据库发生变化了
        ctx.getContentResolver().notifyChange(MyConstance.URI_MSG, null);
    }

    /**
     * 删除指定的聊天信息
     *
     * @param stanzaId
     */
    private void deleteSingleMessage(String stanzaId) {
        MessageBean singleMessage = findSingleMessage(stanzaId);
        if (singleMessage != null) {
            singleMessage.delete();
            // 发出通知，群组数据库发生变化了
            ctx.getContentResolver().notifyChange(MyConstance.URI_MSG, null);
        }
    }

    /**
     * 通过消息的stanzaId查找指定的消息
     *
     * @param stanzaId
     * @return
     */
    private MessageBean findSingleMessage(String stanzaId) {
        List<MessageBean> messageBeans = DataSupport.where(DBcolumns.MSG_STANZAID + " = ?", stanzaId).find(MessageBean.class);
        if (messageBeans != null && messageBeans.size() > 0) {
            return messageBeans.get(0);
        }
        return null;
    }

    /**
     * 通过mark查询聊天信息  每次查询5条
     *
     * @param offset 查询偏移量
     * @param mark
     * @return
     */
    private List<MessageBean> findMessageByMark(String mark, int offset) {
        List<MessageBean> messageBeans = DataSupport.where(DBcolumns.MSG_MARK + " = ?", mark).order(DBcolumns.MSG_ID + " desc").limit(5).offset(offset).find(MessageBean.class);
        if (messageBeans != null) {
            Collections.reverse(messageBeans);
        }
        return messageBeans;
    }

    /**
     * 通过mark删除与指定人的聊天消息
     *
     * @param mark
     */
    private void deleteMessageByMark(String mark) {
        DataSupport.deleteAll(MessageBean.class, DBcolumns.MSG_MARK + " = ?", mark);
        // 发出通知，群组数据库发生变化了
        ctx.getContentResolver().notifyChange(MyConstance.URI_MSG, null);
    }

    /**
     * 通过mark删除当前用户的所有的聊天信息
     *
     * @param owner
     */
    private void deleteMessageByOwner(String owner) {
        DataSupport.deleteAll(MessageBean.class, DBcolumns.MSG_OWNER + " = ?", owner);
        // 发出通知，群组数据库发生变化了
        ctx.getContentResolver().notifyChange(MyConstance.URI_MSG, null);
    }

    /**
     * 删除所有的聊天消息
     */
    private void deleteAllMessage() {
        DataSupport.deleteAll(MessageBean.class);
        // 发出通知，群组数据库发生变化了
        ctx.getContentResolver().notifyChange(MyConstance.URI_MSG, null);
    }

    /**
     * TODO 去重查询  正在聊天的会话  不知道写的对不对
     */
    private List<MessageBean> queryConversation(String owner) {
        return DataSupport.where("select distinct * from " + DBcolumns.TABLE_MSG + " where " + DBcolumns.MSG_OWNER + " = ? order by " + DBcolumns.MSG_ID + " desc", owner).find(MessageBean.class);
    }

    /**
     * 修改与指定好友的聊天消息为已读状态
     *
     * @param mark
     */
    private void updateMessageRead(String mark) {
        MessageBean messageBean = new MessageBean();
        messageBean.setIsRead("1");
        messageBean.updateAll(DBcolumns.MSG_MARK + " = ?", mark);
        // 发出通知，群组数据库发生变化了
        ctx.getContentResolver().notifyChange(MyConstance.URI_MSG, null);
    }

    /**
     * 修改指定消息的回执状态
     *
     * @param stanzaId
     * @param receiptState
     */
    private void updateMessageReceipt(String stanzaId, String receiptState) {
        MessageBean messageBean = new MessageBean();
        messageBean.setMsgReceipt(receiptState);
        messageBean.updateAll(DBcolumns.MSG_STANZAID + " = ?", stanzaId);
        // 发出通知，群组数据库发生变化了
        ctx.getContentResolver().notifyChange(MyConstance.URI_MSG, null);
    }

    /**
     * 查询与指定好友的未读消息个数
     *
     * @param mark
     * @return
     */
    private int queryUnreadMessageCount(String mark) {
        return DataSupport.where(DBcolumns.MSG_ISREAD + " = ? and " + DBcolumns.MSG_MARK + " = ?", "0", mark).count(MessageBean.class);
    }

    /**
     * 查询指定消息的回执状态
     *
     * @param stanzaId 消息唯一标识
     * @return 消息状态   0 收到消息  1发送中 2已发送 3已送达 4发送失败
     */
    private String queryMessageReceipt(String stanzaId) {
        List<MessageBean> messageBeans = DataSupport.where(DBcolumns.MSG_STANZAID + " = ?", stanzaId).select(DBcolumns.MSG_RECEIPT).find(MessageBean.class);
        return messageBeans.get(0).getMsgReceipt();
    }


    /**====================================== 操作好友申请 ==========================================*/

    /**
     * 保存一条好友订阅信息到数据库
     *
     * @param subBean
     */
    private void saveSingleSub(SubBean subBean) {
        SubBean singleSub = findSingleSub(subBean.getMark());
        if (singleSub != null) {
            singleSub.delete();
        }
        subBean.save();
    }

    /**
     * 根据mark查询sub信息
     *
     * @param mark
     * @return
     */
    private SubBean findSingleSub(String mark) {
        List<SubBean> subBeans = DataSupport.where(DBcolumns.MSG_MARK + " = ?", mark).find(SubBean.class);
        if (subBeans != null && subBeans.size() > 0) {
            return subBeans.get(0);
        }
        return null;
    }

    /**
     * 根据mark删除指定的sub信息
     *
     * @param mark
     */
    private void deleteSingleSub(String mark) {
        DataSupport.deleteAll(SubBean.class, DBcolumns.MSG_MARK + " = ?", mark);
    }

    /**
     * 删除所有的好友申请 清空表
     */
    private void deleteAllSub() {
        DataSupport.deleteAll(SubBean.class);
    }

    /**
     * 删除指定用户对应的所有的订阅申请
     *
     * @param owner
     */
    private void deleteSubByOwner(String owner) {
        DataSupport.deleteAll(SubBean.class, DBcolumns.MSG_OWNER + " = ?", owner);
    }

    /**
     * 根据mark修改好友的订阅状态
     *
     * @param mark
     * @param subState 0 收到请求  1 同意对方请求 2 发出请求  3 对方同意请求
     */
    private void undateSubByMark(String mark, String subState) {
        SubBean subBean = new SubBean();
        subBean.setSubState(subState);
        subBean.updateAll(DBcolumns.MSG_MARK + " = ?", mark);
    }

    /**
     * 倒序查询最新的指定条数的好友订阅信息
     *
     * @param owner
     * @param offset
     * @param limit
     * @return
     */
    private List<SubBean> findSubByOwner(String owner, int limit, int offset) {
        List<SubBean> subBeans = DataSupport.where(DBcolumns.MSG_OWNER + " = ?", owner).order(DBcolumns.MSG_ID + " desc").limit(limit).offset(offset).find(SubBean.class);
        if (subBeans != null) {
            Collections.reverse(subBeans);
        }
        return subBeans;
    }
}
