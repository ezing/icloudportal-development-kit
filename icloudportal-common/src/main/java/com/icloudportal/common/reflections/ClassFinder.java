
package com.icloudportal.common.reflections;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * 查找给定父类的子类的辅助类
 * 
 */
public final class ClassFinder<T> {

	private Class<T> type;
	private ClassLoader loader;

	private Set<Class<? extends Annotation>> annotations = Sets.newLinkedHashSet();
	private Set<String> packages = Sets.newLinkedHashSet();
	private Set<String> pathPatterns = Sets.newLinkedHashSet();

	private boolean matchAll = true;

	ClassFinder(Class<T> type, ClassLoader loader) {
		this.type = type;
		this.loader = loader;
	}

	ClassFinder(Class<T> type) {
		this(type, Thread.currentThread().getContextClassLoader());
	}

	/**
	 * 通过给定URL模式进行查找。
	 *
	 * @param pattern
	 *            URL模式
	 * @return 与URL模式一致的查找类实例
	 */
	public ClassFinder<T> byURL(String pattern) {
		Preconditions.checkNotNull(pattern, "pattern must not be null");
		pathPatterns.add(pattern);
		return this;
	}

	/**
	 * 仅查找给定包名内的。
	 * 
	 * @param packageName
	 *            报名
	 * @return 与给定报名一致的查找类实例
	 */
	public ClassFinder<T> within(String packageName) {
		packages.add(packageName);
		return this;
	}

	/**
	 * 通过给定的类加载器查找
	 * 
	 * @param loader
	 *            类加载器
	 * @return 类查找实例
	 */
	public ClassFinder<?> using(ClassLoader loader) {
		this.loader = loader;
		return this;
	}
	
	/**
	 * 通过给定注解查找类。
	 * 
	 * @param annotation
	 *            要检测的注解
	 * @return 类查找实例
	 */
	public ClassFinder<T> having(final Class<? extends Annotation> annotation) {
		this.annotations.add(annotation);
		return this;
	}
	
	/**
	 * 当多次调用{@link #having(Class)}是, 是否只检测一个注解(默认是检测所有注解).
	 * 
	 * @return 类查找实例
	 */
	public ClassFinder<T> any() {
		this.matchAll = false;
		return this;
	}
	
	private boolean hasAnnotation(Class<?> cls) {
		boolean matched = false;
		for (Class<? extends Annotation> annotation : annotations) {
			if (cls.isAnnotationPresent(annotation)) {
				if (!matchAll) {
					return true;
				}
				matched = true;
			} else if (matchAll) {
				return false;
			}
		}
		return annotations.size() == 0 || matched;
	}

	/**
	 * 查找类。
	 * 
	 * @return 匹配的类的集合
	 */
	@SuppressWarnings("all")
	public ImmutableSet<Class<? extends T>> find() {
		final ImmutableSet.Builder<Class<? extends T>> builder = ImmutableSet.builder();
		final ClassScanner scanner = new ClassScanner(loader, packages.toArray(new String[] {}));

		for (String pattern : pathPatterns) {
			scanner.byURL(pattern);
		}

		if (Object.class == type && annotations.isEmpty()) {
			throw new IllegalStateException("please provide some annnotations.");
		}
		if (Object.class == type) {
			for (Class<?> a : annotations) {
				for (Class<?> c : scanner.getTypesAnnotatedWith(a)) {
					builder.add((Class) c);
				}
			}
			return builder.build();
		}
		final Set<Class<? extends T>> all = scanner.getSubTypesOf(type);
		for (Class<? extends T> cls : all) {
			if (hasAnnotation(cls)) {
				builder.add(cls);
			}
		}
		return builder.build();
	}
}