package com.open.im.receiver;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.roster.Roster;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Looper;
import android.view.WindowManager;

import com.open.im.app.MyApp;
import com.open.im.service.IMService;
import com.open.im.utils.MyLog;

/**
 * 监听收到好友请求 非四大组件弹窗
 *
 * @author Administrator
 */
public class MyAddFriendStanzaLinstener implements StanzaListener {

    private IMService imService;
    private AbstractXMPPConnection connection;

    public MyAddFriendStanzaLinstener(IMService imService) {
        this.imService = imService;
        connection = MyApp.connection;
    }

    @Override
    /**
     *
     */
    public void processPacket(Stanza packet) throws NotConnectedException {
        MyLog.showLog("好友监听");
        if (packet instanceof Presence) {
            Presence presence = (Presence) packet;
            final String from = presence.getFrom();
            Type type = presence.getType();
            MyLog.showLog("---" + type.name());
            if (type.equals(Presence.Type.subscribe)) { // 收到添加好友申请
                MyLog.showLog("收到好友邀请:" + from);
                Roster roster = Roster.getInstanceFor(connection);
                boolean isContains = roster.contains(from);
                MyLog.showLog("是否包含该好友::" + isContains);
                if (isContains) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(imService);
                builder.setMessage(from + "请求添加您为好友！");
                builder.setTitle("提示");
                builder.setPositiveButton("同意", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Presence response = new Presence(Type.subscribed);
                            response.setTo(from);
                            connection.sendStanza(response);
                        } catch (NotConnectedException e) {
                            e.printStackTrace();
                        }
                        MyLog.showLog("同意");
                    }
                });
                builder.setNegativeButton("拒绝", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Presence response = new Presence(Type.unsubscribed);
                            response.setTo(from);
                            connection.sendStanza(response);
                        } catch (NotConnectedException e) {
                            e.printStackTrace();
                        }
                        MyLog.showLog("拒绝");
                    }
                });
                MyLog.showLog("到这儿没");
                // create之前必须加上Looper.prepare();不然会报错 Can't create handler
                // inside thread that has not called Looper.prepare()
                // 必须加上这两句 1
                Looper.prepare();
                // 下面这两句将弹窗变成系统alert 要加权限android.permission.SYSTEM_ALERT_WINDOW
                AlertDialog dialog = builder.create();
                dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                dialog.show();
                MyLog.showLog("到这儿没2");
                // 必须加上这两句 2 show之后必须加上Looper.loop();，不然不显示
                Looper.loop();
            } else if (type.equals(Type.subscribed)) {
                Roster roster = Roster.getInstanceFor(connection);
                try {
                    //如果对方同意了好友请求，则创建好友，并且回复对方同意添加对方为好友
                    roster.createEntry(from, from.substring(0, from.indexOf("@")), null);
                    Presence response = new Presence(Type.subscribed);
                    response.setTo(from);
                    connection.sendStanza(response);
                } catch (NotLoggedInException e) {
                    e.printStackTrace();
                } catch (NoResponseException e) {
                    e.printStackTrace();
                } catch (XMPPErrorException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

