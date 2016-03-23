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
            ExtensionElement extension = message.getExtension(DeliveryReceipt.NAMESPACE);
            if (extension != null) {
//              <received xmlns='urn:xmpp:receipts' id='Zd4ly-24'/>
                String receive = extension.toXML().toString();
                XmlPullParser xmlPullParser = Xml.newPullParser();
                String receiptid;
                try {
                    xmlPullParser.setInput(new StringReader(receive));
                    xmlPullParser.next();
                    receiptid = xmlPullParser.getAttributeValue(0);
                    chatDao.updateMsgByReceipt(receiptid,"4");  // 4表示已送达
                    MyLog.showLog("消息回执ID:" + receiptid);
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
