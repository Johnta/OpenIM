package com.open.im.utils;

import android.database.Cursor;

/**
 * 打印cursor的工具类
 * @author Administrator
 *
 */
public class MyPrintCursorUtils {

	public static void printCursor(Cursor cursor) {

		if (cursor == null) {
			System.out.println("cursor == null");
			return;
		}

		if (cursor.getCount() == 0) {
			System.out.println("cursor.getCount() == 0");
			return;
		}

		System.out.println("cursor.getCount() ::" + cursor.getCount());
		while (cursor.moveToNext()) {
			System.out.println("当前行下标：" + cursor.getPosition());
			int colCount = cursor.getColumnCount();
			for (int i = 0; i < colCount; i++) {
				String colName = cursor.getColumnName(i); // 获得该列的名称
				String value = cursor.getString(i); // 获得该列的值
				System.out.println(colName + " : " + value);
			}
		}
		// cursor.close();
	}
}
