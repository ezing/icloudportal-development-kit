
package com.icloudportal.common;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *  {@link Inflections} 定义单数、复数词形变化的规则。
 *
 */
public class Inflections {
	
	private static final Map<String, Inflections> INSTANCES = new ConcurrentHashMap<>();
	private static final String DEFAULT_LANG = "en";

	private Set<String> ignored = new HashSet<>();

	private List<Rule> singulars = new LinkedList<>();
	private List<Rule> plurals = new LinkedList<>();
	
	private Inflections() {
		
	}

	/**
	 * 获取默认语言－英语的词形变化{@link Inflections} 实例。
	 * 
	 * @return  {@link Inflections}实例
	 */
	public static Inflections getInstance() {
		return getInstance(DEFAULT_LANG);
	}
	
	/**
	 * 获取指定语言的词形变化{@link Inflections} 实例。
	 * 
	 * @param language
	 *            语言名称
	 * @return {@link Inflections}实例
	 */
	public static Inflections getInstance(String language) {
		Inflections instance = INSTANCES.get(language);
		if (instance == null) {
			instance = new Inflections();
			INSTANCES.put(language, instance);
		}
		return instance;
	}

	private String capitalize(String word) {
		return Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase();
	}

	/**
	 * 为词性变化添加需要忽略的单词。
	 * 
	 * @param words
	 *            要忽略的单词。
	 */
	public void ignore(String... words) {
		if (words != null) {
			for (String word : words) {
				ignored.add(word.toLowerCase());
			}
		}
	}

	/**
	 * 添加不规则的单数、复数单词
	 * 
	 * @param singular
	 *            单数单词
	 * @param plural
	 *            复数单词
	 */
	public void irregular(String singular, String plural) {
		plurals.add(0, new Rule(singular.toLowerCase(), plural.toLowerCase(), true));
		plurals.add(0, new Rule(capitalize(singular), capitalize(plural), true));
		singulars.add(0, new Rule(plural.toLowerCase(), singular.toLowerCase(), true));
		singulars.add(0, new Rule(capitalize(plural), capitalize(singular), true));
	}

	/**
	 * 添加复数转单数的规则。
	 * 
	 * @param pattern
	 *            匹配复数的模式
	 * @param replacement
	 *            替换文本
	 */
	public void singular(String pattern, String replacement) {
		singulars.add(0, new Rule(pattern, replacement, false));
	}

	/**
	 * 添加单数转复数的规则。
	 * 
	 * @param pattern
	 *            匹配单数的模式
	 * @param replacement
	 *            替换文本
	 */
	public void plural(String pattern, String replacement) {
		plurals.add(0, new Rule(pattern, replacement, false));
	}
	
	/**
	 * Apply the given list of inflection rules on the provided word.
	 * 
	 * @param word
	 *            the word on which to apply the rules
	 * @param rules
	 *            the inflection rules
	 * @return the inflected text
	 */
	protected String apply(String word, List<Rule> rules) {
		if (word == null || "".equals(word.trim())) return word;
		if (ignored.contains(word.toLowerCase())) return word;
		for (Rule rule : rules) {
			String result = rule.apply(word);
			if (result != null) return result;
		}
		return word;
	}
	
	/**
	 * 将给定的词转换为它的单数形式。
	 * 
	 * @param word
	 *            要转换的单词
	 * @return 转换后的文本
	 */
	public String singularize(String word) {
		return apply(word, singulars);
	}
	
	/**
	 * 将给定的词转换为它的复数形式。
	 * 
	 * @param word
	 *            要转换的单词
	 * @return 转换后的文本
	 */
	public String pluralize(String word) {
		return apply(word, plurals);
	}
	
	static class Rule {
		
		private String pattern;
		private String replacement;

		private Pattern regex;

		public Rule(String pattern, String replacement, boolean simple) {
			this.pattern = pattern;
			this.replacement = replacement;
			if (!simple) {
				this.regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			}
		}
		
		public String apply(String input) {
			if (input == null) return null;
			if (input.trim().equals("")) return null;
			if (regex == null) {
				return pattern.equals(input) ? replacement : null;
			}
			final Matcher matcher = regex.matcher(input);
			if (matcher.find()) {
				return matcher.replaceAll(replacement);
			}
			return null;
		}
		
		@Override
		public int hashCode() {
			return pattern.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == this) return true;
			if (obj == null) return false;
			if (obj instanceof Rule && pattern.equals(((Rule) obj).pattern)) {
				return true;
			}
			return false;
		}
	}
}
