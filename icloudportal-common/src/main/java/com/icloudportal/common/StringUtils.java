
package com.icloudportal.common;

/**
 * 此类提供{@link String}的静态辅助方法。
 * 
 */
public final class StringUtils {

	/**
	 * 检查给定的字符串值是否为空。如果字符串为null或者长度为0则值为空（empty）.
	 * 
	 * @param value
	 *            要检查的字符串值
	 * @return 如果为空则返回true，否则返回false
	 */
	public static boolean isEmpty(String value) {
		return value == null || value.length() == 0;
	}

	/**
	 * 检查给定的字符串值是否为空白. 如果字符串为空或者只包含空格，则值为空白
	 * 
	 * @param value
	 *            要检查的字符串值
	 * @return true 如果为空白则返回true，否则返回false
	 */
	public static boolean isBlank(String value) {
		if (isEmpty(value)) {
			return true;
		}
		for (int i = 0; i < value.length(); i++) {
			if (!Character.isWhitespace(value.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 从给定的文本中移除前导缩进空格。
	 * 
	 * @param text
	 *            要移除前导缩进空格的文本
	 * @return 移除前导缩进空格后的文本
	 */
	public static String stripIndent(String text) {
		if (isBlank(text)) {
			return text;
		}

		final String[] lines = text.split("\\n");
		final StringBuilder builder = new StringBuilder();

		int leading = -1;
		for (String line : lines) {
			if (isBlank(line)) { continue; }
			int index = 0;
			int length = line.length();
			if (leading == -1) {
				leading = length;
			}
			while(index < length && index < leading && Character.isWhitespace(line.charAt(index))) { index++; }
			if (leading > index) {
				leading = index;
			}
		}

		for(String line : lines) {
			if (!isBlank(line)) {
				builder.append(leading <= line.length() ? line.substring(leading) : "");
			}
			builder.append("\n");
		}

		return builder.toString();
	}
}
