package com.open.im.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.open.im.bean.MessageBean;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyPrintCursorUtils;

public class ZoneMsgDao {

	private Context ctx;
	private MyDBHelper helper;
	private static ZoneMsgDao instance;

	/**
	 * 构造私有 单例模式
	 * 
	 * @param ctx
	 */
	private ZoneMsgDao(Context ctx) {
		this.ctx = ctx;
		helper = new MyDBHelper(ctx, MyConstance.DB_NAME, 1);
	}

	public static synchronized ZoneMsgDao getInstance(Context ctx) {
		if (instance == null) {
			instance = new ZoneMsgDao(ctx);
		}
		return instance;
	}

	/**
	 * 添加新信息
	 * 
	 * @param msg
	 */
	public int insertMsg(MessageBean msg) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBcolumns.MSG_FROM, msg.getFromUser());
		values.put(DBcolumns.MSG_BODY, msg.getMsgBody());
		values.put(DBcolumns.MSG_DATE, msg.getMsgDateLong());
		values.put(DBcolumns.MSG_MARK, msg.getMsgMark());
		db.insert(DBcolumns.TABLE_ZONE_MSG, null, values);
		// 发出通知，群组数据库发生变化了
		ctx.getContentResolver().notifyChange(uri, null);
		// db.close();
		int msgid = queryTheLastMsgId();// 返回新插入记录的id
		return msgid;
	}

	/**
	 * 查询最新一条记录的id
	 * 
	 * @return
	 */
	public int queryTheLastMsgId() {
		SQLiteDatabase db = helper.getWritableDatabase();
		String sql = "select " + DBcolumns.MSG_ID + " from " + DBcolumns.TABLE_ZONE_MSG + " order by " + DBcolumns.MSG_DATE + " desc limit 1";
		String[] args = new String[] {};
		Cursor cursor = db.rawQuery(sql, args);
		int id = -1;
		if (cursor.moveToNext()) {
			id = cursor.getInt(cursor.getColumnIndex(DBcolumns.MSG_ID));
		}
		cursor.close();
		// db.close();
		return id;
	}

	/**
	 * 获取最后一条信息的时间 long
	 * 
	 * @return
	 */
	public long queryTheLastMsgDate() {
		SQLiteDatabase db = helper.getWritableDatabase();
		String sql = "select " + DBcolumns.MSG_DATE + " from " + DBcolumns.TABLE_ZONE_MSG + " order by " + DBcolumns.MSG_ID + " desc limit 1";
		String[] args = new String[] {};
		Cursor cursor = db.rawQuery(sql, args);
		long msgDateLong = 0;
		if (cursor.moveToNext()) {
			msgDateLong = cursor.getLong(cursor.getColumnIndex(DBcolumns.MSG_DATE));
		}
		cursor.close();
		// db.close();
		return msgDateLong;
	}

	/**
	 * 用于群组数据库更新的URI
	 */
	private Uri uri = Uri.parse("content://com.exiu.zonemessage");

	/**
	 * 查询与指定好友的所有聊天信息
	 * 
	 * @param mark
	 * @return
	 */
	public Cursor getAllMessages(String mark) {
		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor cursor = db.query(DBcolumns.TABLE_ZONE_MSG, null, " msg_mark = ?", new String[] { mark }, null, null, DBcolumns.MSG_DATE + " desc");

		// 为cursor 设置一个，接收通知的 uri
		cursor.setNotificationUri(ctx.getContentResolver(), uri);

		MyPrintCursorUtils.printCursor(cursor);
		return cursor;
	}
}
