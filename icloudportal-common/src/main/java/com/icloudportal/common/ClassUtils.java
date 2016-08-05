
package com.icloudportal.common;

import java.io.InputStream;
import java.net.URL;

/**
 * 此类提供了处理类和资源的静态辅助方法。
 * 
 */
public final class ClassUtils {

	/**
	 * 与{@link Class#forName(String)}一样，但是当类未找到时抛出异常
	 * {@link IllegalArgumentException}。
	 * 
	 * @param name
	 *            要查找的类的名字
	 * @return 找到的类
	 */
	public static Class<?> findClass(String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(name, e);
		}
	}

	/**
	 * 查找给定名称的资源。
	 * 
	 * @param name
	 *            资源名称
	 * @return 用于读取资源的{@link URL}或者null
	 * @see ClassLoader#getResource(String)
	 */
	public static URL getResource(String name) {
		return Thread.currentThread().getContextClassLoader().getResource(name);
	}

	/**
	 * 返回用于读取指定资源的输入流。
	 * 
	 * @param name
	 *            资源名称
	 * @return 用于读取资源的输入流或者null
	 * @see ClassLoader#getResourceAsStream(String)
	 */
	public static InputStream getResourceStream(String name) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
	}
}
