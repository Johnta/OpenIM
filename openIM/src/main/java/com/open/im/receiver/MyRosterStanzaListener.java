package com.open.im.receiver;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Xml;

import com.open.im.bean.VCardBean;
import com.open.im.db.OpenIMDao;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyLog;
import com.open.im.utils.MyVCardUtils;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by Administrator on 2016/4/25.
 */
public class MyRosterStanzaListener implements StanzaListener {

    private OpenIMDao openIMDao;
    private XmlPullParser xmlPullParser;
    private ArrayList<VCardBean> list;
    private SharedPreferences sp;

    public MyRosterStanzaListener(Context ctx){
        openIMDao = OpenIMDao.getInstance(ctx);
        xmlPullParser = Xml.newPullParser();
        sp = ctx.getSharedPreferences(MyConstance.SP_NAME,0);
    }

    @Override
    public void processPacket(Stanza stanza) throws SmackException.NotConnectedException {
        if (stanza instanceof IQ){
            IQ iq = (IQ) stanza;
            if (RosterPacket.NAMESPACE.equals(iq.getChildElementNamespace())){
                String receive = iq.getChildElementXML().toString();
                MyLog.showLog("receive::" + receive);
                String rosterVer;
                String userJid;
                String subType;
                try {
                    xmlPullParser.setInput(new StringReader(receive));
                    while (xmlPullParser.getEventType() != XmlPullParser.END_DOCUMENT){
                        if (xmlPullParser.getEventType() == XmlPullParser.START_TAG){
                            String tagName = xmlPullParser.getName();
                            if ("query".equals(tagName)){
                                rosterVer = xmlPullParser.getAttributeValue(0);
                                sp.edit().putString(MyConstance.ROSTER_VER,rosterVer).apply();
                                list = new ArrayList<VCardBean>();
                                MyLog.showLog("rosterVer::" + rosterVer);
                            } else if ("item".equals(tagName)){
                                userJid = xmlPullParser.getAttributeValue(0);
                                subType = xmlPullParser.getAttributeValue(2);
                                if ("both".equals(subType)){
                                    VCardBean vCardBean = MyVCardUtils.queryVcard(userJid);
                                    vCardBean.setJid(userJid);
                                    list.add(vCardBean);
                                }
                                MyLog.showLog("userJid::" + userJid);
                                MyLog.showLog("subType::" + subType);
                            }
                        }
                        xmlPullParser.next();
                    }
                    MyLog.showLog("list::" + list);
                    openIMDao.saveAllVCard(list);
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
