package com.open.im.utils;

import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {

	private static String YAN = "igweruvy&$%^%#%$#%iuhgfy&*^&*UTYUIHIytrrtrex";

	/**
	 * md5 加密算法
	 * 
	 * @param password
	 * @return
	 */
	public static String md5Encrypt(String password) {
		try {
			// 获得md5加密工具
			MessageDigest digest = MessageDigest.getInstance("md5");

			StringBuffer sb = new StringBuffer();

			password = password + YAN; // 加密码明文加盐

			// 输入的是明文字节，返回的，就是加密以后的字节
			byte[] bytes = digest.digest(password.getBytes());

			for (byte b : bytes) {
				// 将 byte 转换为无符号的整数
				int i = b & 0xff; // 让 b 和 0xff 按位相与
				// 将整数转换为16进制字符
				String str = Integer.toHexString(i);
				if (str.length() == 1) {
					sb.append("0" + str);
				} else {
					sb.append(str);
				}
			}

			return sb.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 计算指定文件的md5值
	 * 
	 * @param filePath
	 * @return
	 */
	public static String getFileMd5(String filePath) {

		try {
			// 获得md5加密工具
			MessageDigest digest = MessageDigest.getInstance("md5");

			StringBuffer sb = new StringBuffer();

			FileInputStream fin = new FileInputStream(filePath);

			int len = -1;
			byte[] buffer = new byte[512];
			while ((len = fin.read(buffer)) != -1) {

				digest.update(buffer, 0, len); // 将文件内容更新至加密器
			}

			byte[] bytes = digest.digest(); // 计算md5值

			for (byte b : bytes) {
				// 将 byte 转换为无符号的整数
				int i = b & 0xff; // 让 b 和 0xff 按位相与
				// 将整数转换为16进制字符
				String str = Integer.toHexString(i);
				if (str.length() == 1) {
					sb.append("0" + str);
				} else {
					sb.append(str);
				}
			}

			return sb.toString();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
