package com.open.im.receiver;

import android.content.Context;
import android.util.Xml;

import com.open.im.db.ChatDao;
import com.open.im.utils.MyLog;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;

/**
 * 消息回执监听
 * Created by Administrator on 2016/3/23.
 */
public class MyReceiptStanzaListener implements StanzaListener {

    private final ChatDao chatDao;

    public MyReceiptStanzaListener(Context ctx) {
        chatDao = ChatDao.getInstance(ctx);
    }

    @Override
    public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
        if (packet instanceof Message) {
            Message message = (Message) packet;
//            <presence to='lizh@openim.daimaqiao.net/Smack' from='vc@openim.daimaqiao.net' id='8UbDZ-82' xml:lang='en' type='subscribe'></presence>
//            MyLog.showLog("Message::" + message.toXML());
            // 通过命名空间获取拓展包
            ExtensionElement extension = message.getExtension(DeliveryReceipt.NAMESPACE);
            if (extension != null) {
                String receive = extension.toXML().toString();
                XmlPullParser xmlPullParser = Xml.newPullParser();
                String receiptid;
                try {
                    xmlPullParser.setInput(new StringReader(receive));
                    xmlPullParser.next();
                    receiptid = xmlPullParser.getAttributeValue(0);
                    String receiptFrom = message.getFrom();
                    boolean isFromServer = isFromServer(receiptFrom);
                    if (isFromServer) {
                        chatDao.updateMsgByReceipt(receiptid, "2"); // 2表示已发送到服务器 1表示发送中  0表示收到消息
                    } else {
                        chatDao.updateMsgByReceipt(receiptid, "3");  // 3表示已送达 4表示发送失败
                    }
                    MyLog.showLog("消息回执ID:" + receiptid);
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /**
     * 判断回执是否来自服务器
     * @param str
     * @return
     */
    private boolean isFromServer(String str) {
        if (str != null && str.contains("@ack.openim.daimaqiao.net")) {
            return true;
        }
        return false;
    }
}
