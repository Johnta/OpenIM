package com.open.im.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 聊天会话界面的dbopenhelper
 * @author Administrator
 *
 */
public class MyDBHelper extends SQLiteOpenHelper {

	public MyDBHelper(Context context, String name, int version) {
		super(context, name, null, version);
	}

	@Override
	/**
	 * 创建存储聊天信息的表
	 */
	public void onCreate(SQLiteDatabase db) {
		String sql_msg = "Create table IF NOT EXISTS " + DBcolumns.TABLE_MSG
				+ "(" 
				+ DBcolumns.MSG_ID + " integer primary key autoincrement," 
				+ DBcolumns.MSG_OWNER+ " text," 
				+ DBcolumns.MSG_FROM+ " text," 
				+ DBcolumns.MSG_TO + " text," 
				+ DBcolumns.MSG_MARK + " text," 
				+ DBcolumns.MSG_TYPE + " text," 
				+ DBcolumns.MSG_BODY + " text,"
				+ DBcolumns.MSG_IMG + " text,"
				+ DBcolumns.MSG_ISCOMING + " integer,"
				+ DBcolumns.MSG_DATE + " text,"
				+ DBcolumns.MSG_ISREADED + " text"
				// + DBcolumns.MSG_BAK1 + " text,"
				// + DBcolumns.MSG_BAK2 + " text,"
				// + DBcolumns.MSG_BAK3 + " text,"
				// + DBcolumns.MSG_BAK4 + " text,"
				// + DBcolumns.MSG_BAK5 + " text,"
				// + DBcolumns.MSG_BAK6 + " text
				+ ");";
		String sql_multi_msg = "Create table IF NOT EXISTS " + DBcolumns.TABLE_MULTI_MSG
				+ "(" 
				+ DBcolumns.MSG_ID + " integer primary key autoincrement," 
				+ DBcolumns.MSG_FROM+ " text," 
				+ DBcolumns.MSG_STANZAID+ " text," 
//				+ DBcolumns.MSG_TO + " text," 
				+ DBcolumns.MSG_MARK + " text," 
				+ DBcolumns.MSG_TYPE + " text," 
				+ DBcolumns.MSG_BODY + " text,"
				+ DBcolumns.MSG_ISCOMING + " integer,"
				+ DBcolumns.MSG_DATE + " text"
				// + DBcolumns.MSG_BAK1 + " text,"
				// + DBcolumns.MSG_BAK2 + " text,"
				// + DBcolumns.MSG_BAK3 + " text,"
				// + DBcolumns.MSG_BAK4 + " text,"
				// + DBcolumns.MSG_BAK5 + " text,"
				// + DBcolumns.MSG_BAK6 + " text
				+ ");";
		
		String sql_zone_msg = "Create table IF NOT EXISTS " + DBcolumns.TABLE_ZONE_MSG
				+ "(" 
				+ DBcolumns.MSG_ID + " integer primary key autoincrement," 
				+ DBcolumns.MSG_FROM+ " text," 
//				+ DBcolumns.MSG_TO + " text," 
				+ DBcolumns.MSG_MARK + " text," 
//				+ DBcolumns.MSG_TYPE + " text," 
				+ DBcolumns.MSG_BODY + " text,"
//				+ DBcolumns.MSG_ISCOMING + " integer,"
				+ DBcolumns.MSG_DATE + " text"
				// + DBcolumns.MSG_BAK1 + " text,"
				// + DBcolumns.MSG_BAK2 + " text,"
				// + DBcolumns.MSG_BAK3 + " text,"
				// + DBcolumns.MSG_BAK4 + " text,"
				// + DBcolumns.MSG_BAK5 + " text,"
				// + DBcolumns.MSG_BAK6 + " text
				+ ");";

		db.execSQL(sql_msg);
		db.execSQL(sql_multi_msg);
		db.execSQL(sql_zone_msg);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
