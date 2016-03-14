package com.open.im.utils;

public class ExiuTokenHelpr {
	private final static String HEAD_TOKEN_NAME = "exiu-token";
	private static final String EXIU_APP_KEY = "d0ad69e32f454e20a9e1589d8e3a4147";

	public static String getHeaderKey() {
		return HEAD_TOKEN_NAME;
	}

	public static String getHeaderValue() {
		String timestatmp = String.valueOf(System.currentTimeMillis() / 1000);
		return new StringBuffer().append(1).append(",").append(timestatmp).append(",")
				.append(getMD5(new StringBuffer().append("1").append(timestatmp).append(EXIU_APP_KEY).toString()))
				.toString();
	}

	public static String getMD5(String content) {
		String s = null;
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			md.update(content.getBytes("utf-8"));
			byte tmp[] = md.digest();
			char str[] = new char[16 * 2];
			int k = 0;
			for (int i = 0; i < 16; i++) {
				byte byte0 = tmp[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			s = new String(str);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}
}
