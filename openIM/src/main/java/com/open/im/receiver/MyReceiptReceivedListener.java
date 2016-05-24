package com.open.im.receiver;

import android.content.Context;

import com.open.im.db.OpenIMDao;
import com.open.im.utils.MyLog;

import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

/**
 * 消息回执监听
 * Created by lzh12 on 2016/5/24.
 */
public class MyReceiptReceivedListener implements ReceiptReceivedListener {

    private final OpenIMDao openIMDao;

    public MyReceiptReceivedListener(Context ctx){
        openIMDao = OpenIMDao.getInstance(ctx);
    }

    @Override
    public void onReceiptReceived(String fromJid, String toJid, String receiptId, Stanza receipt) {
        MyLog.showLog("收到回执::" + receiptId);
        boolean isFromServer = isFromServer(fromJid);
        if (isFromServer) {
            openIMDao.updateMessageReceipt(receiptId, "2");// 2表示已发送到服务器 1表示发送中  0表示收到消息
        } else {
            openIMDao.updateMessageReceipt(receiptId, "3");// 3表示已送达 4表示发送失败
        }
    }

    /**
     * 判断回执是否来自服务器
     *
     * @param str
     * @return
     */
    private static boolean isFromServer(String str) {
        if (str != null && str.contains("@ack.openim.top")) {
            return true;
        }
        return false;
    }
}
