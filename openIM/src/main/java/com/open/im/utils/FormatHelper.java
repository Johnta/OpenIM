package com.open.im.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class FormatHelper {

	public static String formatDistance(double distance) {
		return FormatHelper.formatDistance(distance, 2);
	}

	public static String formatDistance(double distance, int dotCount) {
		int distanceInMeter = (int) distance;
		if (distanceInMeter > 1000) {
			return String.format("%." + dotCount + "fkm", distanceInMeter / 1000.0);
		} else {
			return distanceInMeter + "m";
		}
	}

	public static String formatJson(String uglyJSONString) {
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(uglyJSONString);
		String prettyJsonString = gson.toJson(je);
		return prettyJsonString;
	}

	/**
	 * 格式化粉丝数，千分位以“,”分隔
	 * 
	 * @param num
	 * @return
	 */
	public static String formatFansNum(String num) {
		StringBuffer sb = new StringBuffer();
		for (int i = num.length() - 1; i >= 0; i--) {
			sb.append(num.charAt(i));
			if ((num.length() - i) % 3 == 0) {
				sb.append(",");
			}
		}
		String result = sb.reverse().toString();
		if (result.startsWith(",")) {
			result = result.substring(1);
		}
		return result;
	}

	/**
	 * 格式化Double类型
	 * 
	 * @param input
	 * @return
	 */
	public static Double formatDouble(Double input) {
		return Double.valueOf(String.format("%.2f", input));
	}

	/**
	 * 2015-11-19 pangli
	 * 提供精确的小数位四舍五入处理。
	 * 
	 * @param input
	 *            需要四舍五入的数字
	 * @param scale
	 *            小数点后保留几位
	 * @return 四舍五入后的结果
	 */

	public static double round(Double input, int scale) {
		if (scale < 0) {
			throw new IllegalArgumentException("The scale must be a positive integer or zero");
		}
		BigDecimal b = null == input ? new BigDecimal("0.0") : new BigDecimal(Double.toString(input));
		BigDecimal one = new BigDecimal("1");
		return b.divide(one, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

	/**
	 * double格式化1位小数，转为string
	 * @param d
	 * @return
	 */
	public static String formatDouble1(double d) {
		return new DecimalFormat("#.0").format(d);
	}

	/**
	 * 2015-11-19 pangli
	 * 提供精确的小数位四舍五入处理。
	 * @param input 需要四舍五入的数字
	 * @return 四舍五入后的结果
	 */
	public static double round(Double input) {
		BigDecimal b = new BigDecimal(input);
		return b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
	}

}
