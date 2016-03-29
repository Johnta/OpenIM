package com.open.im.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.open.im.app.MyApp;
import com.open.im.bean.MessageBean;
import com.open.im.bean.SubBean;
import com.open.im.utils.MyConstance;
import com.open.im.utils.MyPrintCursorUtils;

import java.util.ArrayList;
import java.util.List;

public class ChatDao {

	private Context ctx;
	private MyDBHelper helper;
	private static ChatDao instance;

	/**
	 * 构造私有 单例模式
	 * 
	 * @param ctx
	 */
	private ChatDao(Context ctx) {
		this.ctx = ctx;
		helper = new MyDBHelper(ctx, MyConstance.DB_NAME, 1);
	}

	public static synchronized ChatDao getInstance(Context ctx) {
		if (instance == null) {
			instance = new ChatDao(ctx);
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
		values.put(DBcolumns.MSG_TO, msg.getToUser());
		values.put(DBcolumns.MSG_TYPE, msg.getType());
		values.put(DBcolumns.MSG_BODY, msg.getMsgBody());
		values.put(DBcolumns.MSG_IMG, msg.getMsgImg());
		values.put(DBcolumns.MSG_DATE, msg.getMsgDateLong());
		values.put(DBcolumns.MSG_ISREADED, msg.getIsReaded());
		values.put(DBcolumns.MSG_MARK, msg.getMsgMark());
		values.put(DBcolumns.MSG_OWNER, msg.getMsgOwner());
		values.put(DBcolumns.MSG_STANZAID,msg.getMsgStanzaId());
		values.put(DBcolumns.MSG_RECEIPT,msg.getMsgReceipt());
		db.insert(DBcolumns.TABLE_MSG, null, values);
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
		String sql = "select " + DBcolumns.MSG_ID + " from " + DBcolumns.TABLE_MSG + " order by " + DBcolumns.MSG_ID + " desc limit 1";
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
	 * 方法 查询数据库中最新的一条信息
	 * @return
	 */
	public MessageBean queryTheLastMsg(){
		SQLiteDatabase db = helper.getWritableDatabase();
		String sql = "select * from " + DBcolumns.TABLE_MSG + " order by " + DBcolumns.MSG_ID + " desc limit 1";
		Cursor cursor = db.rawQuery(sql, null);
		MessageBean msg = null;
		while (cursor.moveToNext()) {
			msg = new MessageBean();
			msg.setMsgId(cursor.getInt(cursor.getColumnIndex(DBcolumns.MSG_ID)));
			msg.setFromUser(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_FROM)));
			msg.setToUser(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_TO)));
			msg.setType(cursor.getInt(cursor.getColumnIndex(DBcolumns.MSG_TYPE)));
			msg.setMsgBody(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_BODY)));
			msg.setMsgDateLong(cursor.getLong(cursor.getColumnIndex(DBcolumns.MSG_DATE)));
			msg.setIsReaded(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_ISREADED)));
			msg.setMsgMark(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_MARK)));
			msg.setMsgImg(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_IMG)));
			msg.setMsgOwner(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_OWNER)));
			msg.setMsgReceipt(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_RECEIPT)));
		}
		return msg;
	}

	/**
	 * 查询列表,每页返回15条,依据id逆序查询，将时间最早的记录添加进list的最前面
	 * 
	 * @return
	 */
	public ArrayList<MessageBean> queryMsg(String mark, int offset) {
		ArrayList<MessageBean> list = new ArrayList<MessageBean>();
		SQLiteDatabase db = helper.getWritableDatabase();
		String sql = "select * from " + DBcolumns.TABLE_MSG + " where " + DBcolumns.MSG_MARK + "=? order by " + DBcolumns.MSG_ID + " desc limit ?,?";
		String[] args = new String[] { mark, String.valueOf(offset), "15" };
		Cursor cursor = db.rawQuery(sql, args);
		MessageBean msg;
		while (cursor.moveToNext()) {
			msg = new MessageBean();
			msg.setMsgId(cursor.getInt(cursor.getColumnIndex(DBcolumns.MSG_ID)));
			msg.setFromUser(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_FROM)));
			msg.setToUser(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_TO)));
			msg.setType(cursor.getInt(cursor.getColumnIndex(DBcolumns.MSG_TYPE)));
			msg.setMsgBody(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_BODY)));
			msg.setMsgDateLong(cursor.getLong(cursor.getColumnIndex(DBcolumns.MSG_DATE)));
			msg.setIsReaded(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_ISREADED)));
			msg.setMsgMark(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_MARK)));
			msg.setMsgImg(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_IMG)));
			msg.setMsgOwner(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_OWNER)));
			msg.setMsgReceipt(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_RECEIPT)));
			list.add(0, msg);
		}
		return list;
	}

	/**
	 * 用于群组数据库更新的URI
	 */
	private Uri uri = Uri.parse("content://com.exiu.message");

	/**
	 * 查询与指定好友的所有聊天信息
	 * 
	 * @param mark
	 * @return
	 */
	public Cursor getAllMessages(String mark) {
		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor cursor = db.query(DBcolumns.TABLE_MSG, null, " msg_mark = ?", new String[] { mark }, null, null, null);

		// 为cursor 设置一个，接收通知的 uri
		cursor.setNotificationUri(ctx.getContentResolver(), uri);

		MyPrintCursorUtils.printCursor(cursor);
		return cursor;
	}

	/**
	 * 查询消息列表都跟那些人聊天了
	 * 
	 * @return cursor
	 */
	public Cursor getChattingFriend(String username) {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.query(true, DBcolumns.TABLE_MSG, new String[] { DBcolumns.MSG_ID, DBcolumns.MSG_FROM, DBcolumns.MSG_TO, DBcolumns.MSG_BODY, DBcolumns.MSG_DATE }, " msg_owner = ?",
				new String[] { MyApp.username }, DBcolumns.MSG_MARK, null, DBcolumns.MSG_ID + " desc", null);

		// 为cursor 设置一个，接收通知的 uri
		cursor.setNotificationUri(ctx.getContentResolver(), uri);
		MyPrintCursorUtils.printCursor(cursor);
		return cursor;
	}

	/**
	 * 查询消息列表都跟那些人聊天了
	 * 
	 * @return list
	 */
	public List<MessageBean> getChattingFriends(String username) {
		SQLiteDatabase db = helper.getReadableDatabase();
		List<MessageBean> list = new ArrayList<MessageBean>();
		Cursor cursor = db.query(true, DBcolumns.TABLE_MSG, null, " msg_owner = ?", new String[] { MyApp.username }, DBcolumns.MSG_MARK, null, DBcolumns.MSG_ID + " desc", null);
		while (cursor.moveToNext()) {
			MessageBean bean = new MessageBean();
			bean.setMsgId(cursor.getInt(cursor.getColumnIndex(DBcolumns.MSG_ID)));
			bean.setFromUser(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_FROM)));
			bean.setToUser(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_TO)));
			bean.setMsgDateLong(cursor.getLong(cursor.getColumnIndex(DBcolumns.MSG_DATE)));
			bean.setMsgBody(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_BODY)));
			bean.setType(cursor.getInt(cursor.getColumnIndex(DBcolumns.MSG_TYPE)));
			bean.setMsgMark(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_MARK)));
			bean.setMsgOwner(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_OWNER)));
			bean.setIsReaded(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_ISREADED)));
			bean.setMsgStanzaId(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_STANZAID)));
			list.add(bean);
		}
		// 为cursor 设置一个，接收通知的 uri
		cursor.setNotificationUri(ctx.getContentResolver(), uri);
		// MyPrintCursorUtils.printCursor(cursor);
		return list;
	}

	/**
	 * 根据msgmark，删除与指定好友的聊天记录
	 * 
	 * @return
	 */
	public int deleteMsgByMark(String mark) {
		SQLiteDatabase db = helper.getWritableDatabase();
		int row = db.delete(DBcolumns.TABLE_MSG, DBcolumns.MSG_MARK + " = ?", new String[] { mark });
		// 发出通知，群组数据库发生变化了
		ctx.getContentResolver().notifyChange(uri, null);
		return row;
	}

	/**
	 * 清空消息数据库
	 * @return
	 */
	public void deleteAllMsg() {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL("delete from " + DBcolumns.TABLE_MSG);
		ctx.getContentResolver().notifyChange(uri, null);
	}
	/**
	 * 清空好友申请数据库
	 * @return
	 */
	public void deleteAllSub() {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL("delete from " + DBcolumns.TABLE_SUB);
//		ctx.getContentResolver().notifyChange(uri, null);
	}

	/**
	 * 更新数据库 将消息状态标为已读
	 */
	public int updateMsgByMark(String mark) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBcolumns.MSG_ISREADED, 1);
		int update = db.update(DBcolumns.TABLE_MSG, values, DBcolumns.MSG_MARK + " = ?", new String[] { mark });
		ctx.getContentResolver().notifyChange(uri, null);
		return update;
	}

	/**
	 * 根据收到的消息回执 修改指定消息的发送状态
	 * @param stanzaId
	 * @param receiptState
	 * @return
	 */
	public int updateMsgByReceipt(String stanzaId,String receiptState){
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBcolumns.MSG_RECEIPT, receiptState);
		int update = db.update(DBcolumns.TABLE_MSG, values, DBcolumns.MSG_STANZAID + " = ?", new String[]{stanzaId});
		ctx.getContentResolver().notifyChange(uri, null);
		return update;
	}

	/**
	 * 查询与指定好友之间有多少未读消息
	 * 
	 * @param mark
	 * @return
	 */
	public int queryUnreadMsgCount(String mark) {
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = db.query(DBcolumns.TABLE_MSG, new String[]{DBcolumns.MSG_ISREADED}, DBcolumns.MSG_MARK + " = ? and " + DBcolumns.MSG_ISREADED + " = ?", new String[]{mark, "0"}, null,
				null, null);
		MyPrintCursorUtils.printCursor(cursor);
		return cursor.getCount();
	}

	/**
	 * 根据stanzaID查询该条消息的发送状态
	 * @param stanzaId 消息唯一
	 * @return 消息状态   0 收到消息  1发送中 2已发送 3已送达 4发送失败
	 */
	public String queryReceiptState(String stanzaId) {
		SQLiteDatabase db = helper.getWritableDatabase();
		Cursor cursor = db.query(DBcolumns.TABLE_MSG, new String[]{DBcolumns.MSG_RECEIPT}, DBcolumns.MSG_STANZAID + " = ?", new String[]{stanzaId}, null,
				null, null);
		String receiptState = "";
		if (cursor.moveToNext()){
			receiptState = cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_RECEIPT));
		}
		return receiptState;
	}

	/**
	 * 添加好友申请
	 *
	 * @param msg
	 */
	public void insertSub(SubBean msg) {
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBcolumns.MSG_FROM, msg.getFrom());
		values.put(DBcolumns.MSG_TO,msg.getTo());
		values.put(DBcolumns.MSG_BODY,msg.getMsg());
		values.put(DBcolumns.MSG_DATE,msg.getDate());
		values.put(DBcolumns.SUB_STATE,msg.getState());
		db.insert(DBcolumns.TABLE_SUB, null, values);
		// 发出通知，群组数据库发生变化了
//		ctx.getContentResolver().notifyChange(uri, null);
	}

	/**
	 * 查询列表,每页返回15条,依据id逆序查询，将时间最早的记录添加进list的最前面
	 *
	 * @return
	 */
	public ArrayList<SubBean> querySub(String to, int offset) {
		ArrayList<SubBean> list = new ArrayList<SubBean>();
		SQLiteDatabase db = helper.getWritableDatabase();
		String sql = "select * from " + DBcolumns.TABLE_SUB + " where " + DBcolumns.MSG_TO + "=? order by " + DBcolumns.MSG_ID + " desc limit ?,?";
		String[] args = new String[] { to, String.valueOf(offset), "15" };
		Cursor cursor = db.rawQuery(sql, args);
		SubBean msg;
		while (cursor.moveToNext()) {
			msg = new SubBean();
			msg.setFrom(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_FROM)));
			msg.setTo(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_TO)));
			msg.setMsg(cursor.getString(cursor.getColumnIndex(DBcolumns.MSG_BODY)));
			msg.setDate(cursor.getLong(cursor.getColumnIndex(DBcolumns.MSG_DATE)));
			msg.setState(cursor.getString(cursor.getColumnIndex(DBcolumns.SUB_STATE)));
			list.add(0, msg);
		}
		return list;
	}

	/**
	 * 同意好友申请时，修改数据库中的好友请求状态
	 * @return
	 */
	public int updateSub(String from,String state){
		SQLiteDatabase db = helper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(DBcolumns.SUB_STATE, state);
		int update = db.update(DBcolumns.TABLE_SUB, values, DBcolumns.MSG_FROM + " = ?", new String[]{from});
//		ctx.getContentResolver().notifyChange(uri, null);
		return update;
	}
}
