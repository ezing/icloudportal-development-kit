
package com.icloudportal.common.reflections;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ResourceInfo;

/**
 * {@link ResourceFinder} 类提供便捷的API查找资源。
 * 
 */
public final class ResourceFinder {
	
	private ClassLoader loader;
	private Set<Pattern> namePatterns = Sets.newLinkedHashSet();
	private Set<Pattern> pathPatterns = Sets.newLinkedHashSet();
	
	ResourceFinder() {

	}

	ResourceFinder(ClassLoader loader) {
		this.loader = loader;
	}

	/**
	 * 通过给定名字模式查找。
	 * 
	 * @param pattern
	 *            名字模式
	 * @return 资源查找类的实例
	 */
	public ResourceFinder byName(String pattern) {
		Preconditions.checkNotNull(pattern, "pattern must not be null");
		namePatterns.add(Pattern.compile(pattern));
		return this;
	}
	
	/**
	 * 通过给定URL模式查找。
	 * 
	 * @param pattern
	 *            URL模式
	 * @return 资源查找类的实例
	 */
	public ResourceFinder byURL(String pattern) {
		Preconditions.checkNotNull(pattern, "pattern must not be null");
		pathPatterns.add(Pattern.compile(pattern));
		return this;
	}

	/**
	 * 通过{@link Matcher#matches()} 的调用进行完全模式匹配查找资源
	 * 
	 * @return URL对象清单
	 */
	public ImmutableList<URL> match() {
		return find(false);
	}

	/**
	 * 通过{@link Matcher#find()}的调用进行部分匹配查找资源。
	 * 
	 * @return URL对象清单
	 */
	public ImmutableList<URL> find() {
		return find(true);
	}
	
	private ImmutableList<URL> find(boolean partial) {
		ImmutableList.Builder<URL> all = ImmutableList.builder();
		for (Pattern namePattern : namePatterns) {
			for (URL file : getResources(namePattern, loader, partial)) {
				if (pathPatterns.isEmpty()) {
					all.add(file);
					continue;
				}
				for (Pattern pathPattern : pathPatterns) {
					Matcher matcher = pathPattern.matcher(file.getFile());
					boolean matched = partial ? matcher.find() : matcher.matches();
					if (matched) {
						all.add(file);
					}
				}
			}
		}
		return all.build();
	}

	private static ImmutableList<URL> getResources(Pattern pattern, ClassLoader loader, boolean partial) {
		final ImmutableList.Builder<URL> builder = ImmutableList.builder();
		final ClassLoader classLoader = loader == null ? Thread.currentThread().getContextClassLoader() : loader;
		try {
			for (ResourceInfo info : ClassPath.from(classLoader).getResources()) {
				String name = info.getResourceName();
				Matcher matcher = pattern.matcher(name);
				boolean matched = partial ? matcher.find() : matcher.matches();
				if (matched) {
					Enumeration<URL> urls = classLoader.getResources(name);
					while (urls.hasMoreElements()) {
						builder.add(urls.nextElement());
					}
				}
			}
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
		return builder.build();
	}
}