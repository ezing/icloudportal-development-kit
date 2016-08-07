
package com.icloudportal.common;

import static java.nio.file.FileVisitOption.FOLLOW_LINKS;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.EnumSet;

import com.google.common.base.Preconditions;

/**
 * 这个类提供了一些辅助处理文件的方法。
 * 
 */
public final class FileUtils {

	/**
	 * 从给定的路径元素得到一个文件。
	 * 
	 * @param first
	 *            第一路径元素
	 * @param more
	 *            附加路径元素
	 * @return 文件
	 */
	public static File getFile(String first, String... more) {
		Preconditions.checkNotNull(first, "first element must not be null");
		File file = new File(first);
		if (more != null) {
			for (String name : more) {
				file = new File(file, name);
			}
		}
		return file;
	}

	/**
	 * 从给定的路径元素得到一个文件。
	 * 
	 * @param directory
	 *            父目录
	 * @param next
	 *            下一个路径元素
	 * @param more
	 *            附加路径元素
	 * @return 文件
	 */
	public static File getFile(File directory, String next, String... more) {
		Preconditions.checkNotNull(directory, "directory must not be null");
		Preconditions.checkNotNull(next, "next element must not be null");
		File file = new File(directory, next);
		if (more != null) {
			for (String name : more) {
				file = new File(file, name);
			}
		}
		return file;
	}

	/**
	 * 将源目录复制到目标目录。
	 * 
	 * @param source
	 *            源目录
	 * @param target
	 *            目标目录
	 * @throws IOException
	 *             当复制时发送IO错误
	 */
	public static void copyDirectory(File source, File target) throws IOException {
		copyDirectory(source.toPath(), target.toPath());
	}
	
	/**
	 * 将源目录复制到目标目录。
	 * 
	 * @param source
	 *            源目录
	 * @param target
	 *            目标目录
	 * @throws IOException
	 *             当复制时发送IO错误
	 */
	public static void copyDirectory(Path source, Path target) throws IOException {
		if (!Files.isDirectory(source)) {
			throw new IOException("Invalid source directory: " + source);
		}
		if (Files.exists(target) && !Files.isDirectory(target)) {
			throw new IOException("Invalid target directory: " + target);
		}
		if (!Files.exists(target)) {
			Files.createDirectories(target);
		}
		final DirCopier copier = new DirCopier(source, target);
		final EnumSet<FileVisitOption> opts = EnumSet.of(FOLLOW_LINKS);
		Files.walkFileTree(source, opts, Integer.MAX_VALUE, copier);
	}
	
	/**
	 * 递归删除给定的目录。
	 * 
	 * @param directory
	 *            要删除的目录
	 * @throws IOException
	 *             删除不成功时抛出的异常
	 */
	public static void deleteDirectory(File directory) throws IOException {
		deleteDirectory(directory.toPath());
	}
	
	/**
	 * 递归删除给定的目录。
	 * 
	 * @param directory
	 *            要删除的目录
	 * @throws IOException
	 *             删除不成功时抛出的异常
	 */
	public static void deleteDirectory(Path directory) throws IOException {
		if (!Files.isDirectory(directory)) {
			throw new IOException("Invalid directory: " + directory);
		}
		final DirCleaner cleaner = new DirCleaner();
		final EnumSet<FileVisitOption> opts = EnumSet.of(FOLLOW_LINKS);
		Files.walkFileTree(directory, opts, Integer.MAX_VALUE, cleaner);
	}

	static class DirCopier extends SimpleFileVisitor<Path> {
		
		private final Path source;
		private final Path target;
		
		DirCopier(Path source, Path target) {
			this.source = source;
			this.target = target;
		}
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			final Path dest = target.resolve(source.relativize(file));
			Files.copy(file, dest, COPY_ATTRIBUTES, REPLACE_EXISTING);
			return CONTINUE;
		}
		
		@Override
		public FileVisitResult preVisitDirectory(Path dir,
				BasicFileAttributes attrs) throws IOException {
			Path dest = target.resolve(source.relativize(dir));
			try {
				Files.copy(dir, dest, COPY_ATTRIBUTES);
			} catch (FileAlreadyExistsException e) {
			}
			return CONTINUE;
		}
		
		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc)
				throws IOException {
			if (exc == null) {
				Path dest = target.resolve(source.relativize(dir));
				try {
					FileTime time = Files.getLastModifiedTime(dir);
					Files.setLastModifiedTime(dest, time);
				} catch (IOException e) {
				}
			}
			return CONTINUE;
		}
	}
	
	static class DirCleaner extends SimpleFileVisitor<Path> {
		
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			Files.delete(file);
			return CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc)
				throws IOException {
			Files.delete(dir);
			return CONTINUE;
		}
	}
}
