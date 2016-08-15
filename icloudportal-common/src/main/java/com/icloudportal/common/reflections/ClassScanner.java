
package com.icloudportal.common.reflections;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import com.axelor.internal.asm.AnnotationVisitor;
import com.axelor.internal.asm.ClassReader;
import com.axelor.internal.asm.ClassVisitor;
import com.axelor.internal.asm.Opcodes;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

/**
 * {@link ClassScanner}使用ASM和guava的ClassPath API查询基于父类或者注解的类
 * 
 */
final class ClassScanner {

	private static final int ASM_FLAGS = ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES;
	private static final String IGNORE_OBJECT = "java/lang/Object";
	
	private ClassLoader loader;
	
	private Map<String, Collector> collectors = Maps.newConcurrentMap();
	private Set<String> packages = Sets.newLinkedHashSet();
	private Set<Pattern> pathPatterns = Sets.newLinkedHashSet();

	/**
	 * 用户给定的{@link ClassLoader}创建一个 {@link ClassScanner} 实例<br>
	 * <br>
	 * 可选的包名用来限制扫描哪些包
	 * 
	 * @param loader
	 *            用来扫描的{@link ClassLoader}
	 * @param packages
	 *            限制扫描范围的包名
	 */
	public ClassScanner(ClassLoader loader, String... packages) {
		this.loader = loader;
		if (packages != null) {
			for (String name : packages) {
				this.packages.add(name);
			}
		}
	}
	
	/**
	 * 通过给定URL模式进行查找
	 *
	 * @param pattern
	 *           URL模式
	 * @return 类扫描实例
	 */
	public ClassScanner byURL(String pattern) {
		Preconditions.checkNotNull(pattern, "pattern must not be null");
		pathPatterns.add(Pattern.compile(pattern));
		return this;
	}

	@SuppressWarnings("all")
	public <T> ImmutableSet<Class<? extends T>> getSubTypesOf(Class<T> type) {
		ImmutableSet.Builder<Class<? extends T>> builder = ImmutableSet.builder();
		
		Set<String> types;
		try {
			types = getSubTypesOf(type.getName());
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}
		
		for (String sub : types) {
			try {
				Class<?> found = loader.loadClass(sub);
				builder.add((Class) found);
			} catch (Throwable e) {
			}
		}
		return builder.build();
	}
	
	public ImmutableSet<Class<?>> getTypesAnnotatedWith(Class<?> annotation) {
		ImmutableSet.Builder<Class<?>> builder = ImmutableSet.builder();
		
		if (collectors.isEmpty()) {
			try {
				scan();
			} catch (IOException e) {
				throw Throwables.propagate(e);
			}
		}

		for (String klass : collectors.keySet()) {
			Set<String> my = collectors.get(klass).annotations;
			if (my == null) {
				continue;
			}
			if (my.contains(annotation.getName())) {
				try {
					builder.add(loader.loadClass(klass));
				} catch (Throwable e) {
				}
			}
		}
		return builder.build();
	}
	
	private Set<String> getSubTypesOf(String type) throws IOException {

		Set<String> types = Sets.newHashSet();

		if (collectors.isEmpty()) {
			scan();
		}
		
		for (String klass : collectors.keySet()) {
			Set<String> my = collectors.get(klass).superNames;
			if (my == null) {
				continue;
			}
			if (my.contains(type)) {
				types.add(klass);
				types.addAll(getSubTypesOf(klass));
			}
		}
		
		return types;
	}
	
	private void scan() throws IOException {
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<Future<?>> futures = Lists.newArrayList();
		Set<ClassInfo> infos = Sets.newHashSet();
		
		if (packages.isEmpty()) {
			infos = ClassPath.from(loader).getTopLevelClasses();
		} else {
			for (String pkg : packages) {
				infos.addAll(ClassPath.from(loader).getTopLevelClassesRecursive(pkg));
			}
		}
		
		try {
			for (final ClassInfo info : infos) {
				futures.add(executor.submit(new Callable<Object>() {
					@Override
					public Object call() throws Exception {
						scan(info.getName());
						return info.getName();
					}
				}));
			}
			
			for (Future<?> future : futures) {
				try {
					future.get();
				} catch (Exception e) {
				}
			}
		} finally {
			executor.shutdown();
		}
	}

	private void scan(final String type) throws ClassNotFoundException {

		if (collectors.containsKey(type) ||  Object.class.getName().equals(type)) {
			return;
		}

		URL resource = loader.getResource(type.replace('.', '/') + ".class");
		boolean matched = pathPatterns.isEmpty();
		for (Pattern pathPattern : pathPatterns) {
			matched = pathPattern.matcher(resource.getFile()).matches();
			if (matched) break;
		}

		if (!matched) {
			return;
		}

		try {
			InputStream stream = resource.openStream();
			try {
				BufferedInputStream in = new BufferedInputStream(stream);
				ClassReader reader = new ClassReader(in);
				Collector collector = new Collector();
				reader.accept(collector, ASM_FLAGS);
				collectors.put(type, collector);
				if (collector.superNames != null) {
					for (String base : collector.superNames) { scan(base); }
				}
			} finally {
				stream.close();
			}
		} catch (NullPointerException | IOException e) {
			throw new ClassNotFoundException(type);
		}
	}
	
	static class Collector extends ClassVisitor {
		
		private Set<String> superNames;
		private Set<String> annotations;
		
		public Collector() {
			super(Opcodes.ASM4);
		}
		
		private void acceptSuper(String name) {
			if (name == null) {
				return;
			}
			if (superNames == null) {
				superNames = Sets.newHashSet();
			}
			superNames.add(name.replace("/", "."));
		}
		
		private void acceptAnnotation(String name) {
			if (name == null || IGNORE_OBJECT.equals(name)) {
				return;
			}
			if (annotations == null) {
				annotations = Sets.newHashSet();
			}
			annotations.add(name.replace("/", ".").substring(1, name.length() - 1));
		}

		@Override
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			acceptSuper(superName);
			if (interfaces != null) {
				for (String iface : interfaces) {
					acceptSuper(iface);
				}
			}
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			acceptAnnotation(desc);
			return null;
		}
	}
}
