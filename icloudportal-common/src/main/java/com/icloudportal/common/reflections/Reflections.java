
package com.icloudportal.common.reflections;

/**
 * {@link Reflections}提供简捷的方法查找资源和类。
 * 
 */
public final class Reflections {

	private Reflections() {
	}

	/**
	 * 返回{@link ClassFinder} 去查找给定类的子类。
	 * 
	 * @param <T>
	 *            要去查找的类
	 * @param type
	 *            父类
	 * @param loader
	 *            查找所用的{@link ClassLoader}
	 * 
	 * @return {@link ClassFinder}实例
	 */
	public static <T> ClassFinder<T> findSubTypesOf(Class<T> type, ClassLoader loader) {
		return new ClassFinder<>(type, loader);
	}

	/**
	 * 返回{@link ClassFinder} 去查找给定类的子类。
	 * 
	 * @param <T>
	 *            要去查找的类
	 * @param type
	 *            父类
	 * 
	 * @return {@link ClassFinder}实例
	 */
	public static <T> ClassFinder<T> findSubTypesOf(Class<T> type) {
		return new ClassFinder<>(type);
	}

	/**
	 * 返回{@link ClassFinder}去查找类.
	 * 
	 * @param loader
	 *            查找所用的{@link ClassLoader}
	 * 
	 * @return {@link ClassFinder}实例
	 */
	public static ClassFinder<?> findTypes(ClassLoader loader) {
		return findSubTypesOf(Object.class, loader);
	}

	/**
	 * 返回查找类的 {@link ClassFinder} 。
	 * 
	 * @return  {@link ClassFinder}实例
	 */
	public static ClassFinder<?> findTypes() {
		return findSubTypesOf(Object.class);
	}

	/**
	 * 返回查找资源的{@link ResourceFinder}。
	 * 
	 * @param loader
	 *            查找所用的{@link ClassLoader}
	 * 
	 * @return  {@link ResourceFinder}实例
	 */
	public static ResourceFinder findResources(ClassLoader loader) {
		return  new ResourceFinder(loader);
	}

	/**
	 * 返回查找资源的{@link ResourceFinder}。
	 * 
	 * @return {@link ResourceFinder}实例
	 */
	public static ResourceFinder findResources() {
		return new ResourceFinder();
	}
}
