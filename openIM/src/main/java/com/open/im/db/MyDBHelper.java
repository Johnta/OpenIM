package com.open.im.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.open.im.utils.MyConstance;
import com.open.im.utils.MyLog;

import java.io.File;

/**
 * 创建数据库和表
 * @author Administrator
 *
 */
public class MyDBHelper extends SQLiteOpenHelper {

	private Context ctx;
	private int dbVersion;

	public MyDBHelper(Context context, String name, int version) {
		super(context, name, null, version);
		ctx = context;

		// 重新安装时 会清除以前的数据库 避免因数据库修改造成的崩溃
		SharedPreferences sp = ctx.getSharedPreferences(MyConstance.SP_NAME,0);
		dbVersion = sp.getInt("dbVersion", 0);
		if (dbVersion == 0){
			File dbFile = ctx.getDatabasePath(MyConstance.DB_NAME);
			if (dbFile.exists()){
				dbFile.delete();
				dbVersion = 1;
				sp.edit().putInt("dbVersion",dbVersion).apply();
			}
		}
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
				+ DBcolumns.MSG_DATE + " text,"
				+ DBcolumns.MSG_ISREAD + " text,"
				+ DBcolumns.MSG_STANZAID+ " text,"
				+ DBcolumns.VCARD_NICK+ " text,"
				+ DBcolumns.VCARD_AVATAR+ " text,"
				+ DBcolumns.MSG_RECEIPT+ " text"
				+ ");";
		String sql_sub = "Create table IF NOT EXISTS " + DBcolumns.TABLE_SUB
				+ "("
				+ DBcolumns.MSG_ID + " integer primary key autoincrement,"
				+ DBcolumns.MSG_FROM+ " text,"
				+ DBcolumns.MSG_TO + " text,"
				+ DBcolumns.SUB_STATE + " text,"
				+ DBcolumns.MSG_BODY + " text,"
				+ DBcolumns.MSG_DATE + " text,"
				+ DBcolumns.VCARD_AVATAR + " text,"
				+ DBcolumns.VCARD_NICK + " text,"
				+ DBcolumns.MSG_OWNER + " text"
				+ ");";
		String sql_vcard = "Create table IF NOT EXISTS " + DBcolumns.TABLE_VCARD
				+ "("
//				+ DBcolumns.VCARD_ID + " integer primary key autoincrement,"
				+ DBcolumns.VCARD_JID+ " text,"
				+ DBcolumns.VCARD_AVATAR+ " text,"
				+ DBcolumns.VCARD_NICK + " text,"
				+ DBcolumns.VCARD_SEX + " text,"
				+ DBcolumns.VCARD_BDAY + " text,"
				+ DBcolumns.VCARD_ADDRESS + " text,"
				+ DBcolumns.VCARD_EMAIL + " text,"
				+ DBcolumns.VCARD_PHONE + " text,"
				+ DBcolumns.VCARD_DESC + " text"
				+ ");";
//		String sql_multi_msg = "Create table IF NOT EXISTS " + DBcolumns.TABLE_MULTI_MSG
//				+ "("
//				+ DBcolumns.MSG_ID + " integer primary key autoincrement,"
//				+ DBcolumns.MSG_FROM+ " text,"
//				+ DBcolumns.MSG_STANZAID+ " text,"
////				+ DBcolumns.MSG_TO + " text,"
//				+ DBcolumns.MSG_MARK + " text,"
//				+ DBcolumns.MSG_TYPE + " text,"
//				+ DBcolumns.MSG_BODY + " text,"
//				+ DBcolumns.MSG_ISCOMING + " integer,"
//				+ DBcolumns.MSG_DATE + " text"
//				// + DBcolumns.MSG_BAK1 + " text,"
//				// + DBcolumns.MSG_BAK2 + " text,"
//				// + DBcolumns.MSG_BAK3 + " text,"
//				// + DBcolumns.MSG_BAK4 + " text,"
//				// + DBcolumns.MSG_BAK5 + " text,"
//				// + DBcolumns.MSG_BAK6 + " text
//				+ ");";
//
//		String sql_zone_msg = "Create table IF NOT EXISTS " + DBcolumns.TABLE_ZONE_MSG
//				+ "("
//				+ DBcolumns.MSG_ID + " integer primary key autoincrement,"
//				+ DBcolumns.MSG_FROM+ " text,"
////				+ DBcolumns.MSG_TO + " text,"
//				+ DBcolumns.MSG_MARK + " text,"
////				+ DBcolumns.MSG_TYPE + " text,"
//				+ DBcolumns.MSG_BODY + " text,"
////				+ DBcolumns.MSG_ISCOMING + " integer,"
//				+ DBcolumns.MSG_DATE + " text"
//				// + DBcolumns.MSG_BAK1 + " text,"
//				// + DBcolumns.MSG_BAK2 + " text,"
//				// + DBcolumns.MSG_BAK3 + " text,"
//				// + DBcolumns.MSG_BAK4 + " text,"
//				// + DBcolumns.MSG_BAK5 + " text,"
//				// + DBcolumns.MSG_BAK6 + " text
//				+ ");";

		db.execSQL(sql_msg);
		db.execSQL(sql_sub);
		db.execSQL(sql_vcard);
		/**
		 * 去重插入
		 */
		db.execSQL("CREATE UNIQUE INDEX "+DBcolumns.VCARD_ID+"  ON " + DBcolumns.TABLE_VCARD + " ("+ DBcolumns.VCARD_JID +"); ");
//		db.execSQL("CREATE UNIQUE INDEX "+DBcolumns.VCARD_ID+"  ON " + DBcolumns.TABLE_SUB + " ("+ DBcolumns.VCARD_JID +"); ");
//		db.execSQL(sql_multi_msg);
//		db.execSQL(sql_zone_msg);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		MyLog.showLog("数据库升级为:" + newVersion);
		String sql;
		/**
		 * 主要是考虑到夸版本升级，
		 * 比如有的用户一直不升级版本，数据库版本号一直是1，而客户端最新版本其实对应的数据库版本已经是4了，
		 * 那么我中途可能对数据库做了很多修改，通过这个for循环，可以迭代升级，不会发生错误
		 */
		for (int i = oldVersion + 1; i <= newVersion; i++) {
			switch (i) {
				case 2:
					sql = "ALTER TABLE " + DBcolumns.TABLE_MSG + " ADD COLUMN test text;";
					db.execSQL(sql);
					break;
				case 3:
					sql = "ALTER TABLE " + DBcolumns.TABLE_MSG + " ADD COLUMN test02 text;";
					db.execSQL(sql);
					break;
			}
		}
	}

}
