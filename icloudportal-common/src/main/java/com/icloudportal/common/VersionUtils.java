
package com.icloudportal.common;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.io.CharStreams;

/**
 * 提供辅助方法找到icloudportal项目的版本信息。
 *
 */
public final class VersionUtils {

	private static Version version;

	private static final String VERSION_FILE = "icloudportal-version.txt";
	private static final Pattern VERSION_PATTERN = Pattern.compile("^(\\d+)\\.(\\d+)\\.(\\d+)(?:\\-rc(\\d+))?$");
	private static final Pattern VERSION_SPEC_PATTERN = Pattern.compile("(~)?((\\d+)\\.(\\d+)\\.(\\d+)(?:\\-rc(\\d+))?)");

	/**
	 * 此类存储icloudportal模块的版本细节。
	 *
	 */
	public static class Version {

		public final String version;

		// feature version (major.minor)
		public final String feature;

		public final int major;

		public final int minor;

		public final int patch;

		public final int rc;

		Version(String version) {
			final Matcher matcher = VERSION_PATTERN.matcher(version.trim());
			if (!matcher.matches()) {
				throw new IllegalStateException("Invalid version string.");
			}
			this.version = version.trim();
			this.major = Integer.parseInt(matcher.group(1));
			this.minor = Integer.parseInt(matcher.group(2));
			this.patch = Integer.parseInt(matcher.group(3));
			this.rc = matcher.group(4) != null ? Integer.parseInt(matcher.group(4)) : 0;
			this.feature = String.format("%s.%s", major, minor);
		}

		/**
		 * 检查给定版本规格是否与当前版本相匹配。
		 *
		 * <p>
		 * 版本规格可以是确切的版本号；
		 * 也可以是版本号加前缀<code>~</code>，匹配所有后续版本
		 * </p>
		 * 
		 * <p>
		 * 例如当前版本 <code>3.0.4</code>，则:
		 * </p>
		 * 
		 * <ul>
		 * <li> 3.0.4 (匹配)</li>
		 * <li> 3.0.0 (不匹配)</li>
		 * <li> ~3.0.0 (匹配)</li>
		 * <li> ~3.0.1 (匹配)</li>
		 * <li> ~3.0.5 (不匹配)</li>
		 * </ul>
		 *
		 * @param spec
		 *            要去检查的版本规格
		 * @return 如果匹配则返回true，否则返回false
		 */
		public boolean matches(String spec) {
			if (spec == null || spec.trim().length() == 0) {
				return true;
			}
			Matcher matcher = VERSION_SPEC_PATTERN.matcher(spec);
			if (!matcher.matches()) {
				return false;
			}
			boolean all = matcher.group(1) != null;
			Version ver = new Version(matcher.group(2));
			if (ver.version.equals(version)) return true;
			if (all && ver.major == major && ver.minor == minor && ver.patch <= patch) {
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			return version;
		}
	}

	/**
	 * 获取icloudportal SDK 版本.
	 *
	 * @return {@link Version}实例
	 */
	public static Version getVersion() {
		if (version == null) {
			version = getVersion(VERSION_FILE);
		}
		return version;
	}

	private static Version getVersion(String file) {
		try (InputStream is = ClassUtils.getResourceStream(file)) {
			String version = CharStreams.toString(new InputStreamReader(is));
			return new Version(version);
		} catch (Exception e) {
			throw new IllegalStateException("Unable to read version details.", e);
		}
	}
}
