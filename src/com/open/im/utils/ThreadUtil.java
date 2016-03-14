package com.open.im.utils;

import android.os.Handler;

public class ThreadUtil {

	/**
	 * 子线程运行
	 * @param r
	 */
	public static void runOnBackThread(Runnable r) {
		new Thread(r).start();
	}

	public static Handler handler = new Handler();

	/**
	 * 主线程运行
	 * @param r
	 */
	public static void runOnUIThread(Runnable r) {
		handler.post(r);
	}
}
