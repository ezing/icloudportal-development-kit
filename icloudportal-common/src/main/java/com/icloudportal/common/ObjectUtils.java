
package com.icloudportal.common;

import java.util.Collection;
import java.util.Map;

/**
 * 这个类定义了静态辅助方法来处理对象。
 * 
 */
public final class ObjectUtils {
	
	/**
	 * 检查给定对象值是否为空。 <br>
	 * <br>
	 * 满足以下任一条件则对象值为空(empty):
	 * <ul>
	 * <li>值为null</li>
	 * <li>值是长度为0的字符串</li>
	 * <li>值是map或collection，并且元素数量为0</li>
	 * </ul>
	 * 
	 * @param value
	 *            要检查的对象值
	 * @return 如果为空返回true，否则返回false
	 */
	public static boolean isEmpty(Object value) {
		if (value == null) return true;
		if (value instanceof String && "".equals(value)) return true;
		if (value instanceof Map && ((Map<?, ?>) value).size() == 0) return true;
		if (value instanceof Collection && ((Collection<?>) value).size() == 0) return true;
		return false;
	}
}
