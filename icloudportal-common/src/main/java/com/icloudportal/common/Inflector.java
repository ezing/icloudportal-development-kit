
package com.icloudportal.common;

import java.text.Normalizer;
import java.text.Normalizer.Form;

import com.google.common.base.CaseFormat;
import com.google.common.base.Preconditions;

/**
 * {@link Inflector}提供单词转换为复数、单数、标题、类名、表名的各种方法 
 * 
 */
public final class Inflector {

	private static final Inflections INFLECTIONS_EN = Inflections.getInstance();
	private static final Inflector INSTANCE = new Inflector();

	private Inflector() {
		initEnglishRules();
	}
	
	public static Inflector getInstance() {
		return INSTANCE;
	}

	/**
	 * 返回给定字符串的复数形式
	 * 
	 * <pre>
	 * inflection.pluralize(&quot;post&quot;); // &quot;posts&quot;
	 * inflection.pluralize(&quot;child&quot;); // &quot;children&quot;
	 * inflection.pluralize(&quot;octopus&quot;); // &quot;octopi&quot;
	 * inflection.pluralize(&quot;sheep&quot;); // &quot;sheep&quot;
	 * </pre>
	 * 
	 * @param word
	 *            要转换为复数的字符串
	 * @return 复数字符串
	 */
	public String pluralize(String word) {
		return INFLECTIONS_EN.pluralize(word);
	}
	
	/**
	 * 返回给定字符串的单数形式
	 * 
	 * <pre>
	 * inflection.singularize(&quot;posts&quot;); // &quot;post&quot;
	 * inflection.singularize(&quot;children&quot;); // &quot;child&quot;
	 * inflection.singularize(&quot;octopi&quot;); // &quot;octopus&quot;
	 * inflection.singularize(&quot;sheep&quot;); // &quot;sheep&quot;
	 * </pre>
	 * 
	 * @param word
	 *            要转换为单数的字符串
	 * @return 单数字符串
	 */
	public String singularize(String word) {
		return INFLECTIONS_EN.singularize(word);
	}
	
	/**
	 * 转换给定单词为驼峰格式。
	 * 
	 * <pre>
	 * inflection.camelcase(&quot;address_book&quot;, false); // &quot;AddressBook&quot;
	 * inflection.camelcase(&quot;address-book&quot;, true); // &quot;addressBook&quot;
	 * inflection.camelcase(&quot;Address book&quot;, false); // &quot;AddressBook&quot;
	 * </pre>
	 * 
	 * @param word
	 *            要转换的单词
	 * @param lower
	 *            是否创建小写驼峰
	 * @return 驼峰格式字符串
	 */
	public String camelize(String word, boolean lower) {
		final CaseFormat target = lower ? CaseFormat.LOWER_CAMEL : CaseFormat.UPPER_CAMEL;
		return CaseFormat.LOWER_UNDERSCORE.to(target, underscore(word));
	}
	
	/**
	 * 转换给定单词为驼峰格式。
	 * 
	 * <pre>
	 * inflection.camelcase(&quot;address_book&quot;); // &quot;AddressBook&quot;
	 * inflection.camelcase(&quot;address-book&quot;); // &quot;AddressBook&quot;
	 * inflection.camelcase(&quot;Address book&quot;); // &quot;AddressBook&quot;
	 * </pre>
	 * 
	 * @param word
	 *            要转换的单词
	 * @return 驼峰格式字符串
	 */
	public String camelize(String word) {
		return camelize(word, false);
	}
	
	/**
	 * 将驼峰格式字符串转换为下划线、小写形式。
	 * 
	 * <pre>
	 * inflection.underscore(&quot;AddressBook&quot;); // &quot;address_book&quot;
	 * inflection.underscore(&quot;address-book&quot;); // &quot;address_book&quot;
	 * inflection.underscore(&quot;Address book&quot;); // &quot;address_book&quot;
	 * </pre>
	 * 
	 * @param camelCase
	 *            驼峰格式字符串
	 * @return 转换后的字符串
	 */
	public String underscore(String camelCase) {
		Preconditions.checkNotNull(camelCase);
		return camelCase.trim()
				   .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
				   .replaceAll("([a-z\\d])([A-Z])", "$1_$2")
				   .replaceAll("[-\\s]+", "_")
				   .toLowerCase();
	}

	/**
	 * 将给定的单词转换成可读的形式。
	 * 
	 * <pre>
	 * inflection.humanize(&quot;contact_id&quot;); // &quot;Contact&quot;
	 * inflection.humanize(&quot;address_list&quot;); // &quot;Addresses&quot;
	 * inflection.humanize(&quot;_id&quot;); // &quot;Id&quot;
	 * </pre>
	 * 
	 * @param word
	 *            要转换的字符串
	 * @return 转换后的字符串
	 */
	public String humanize(String word) {
		Preconditions.checkNotNull(word);
		String result = underscore(word)
				.replaceAll("_id$", "")
				.replaceAll("\\A_+", "")
				.replaceAll("[_\\s]+", " ");
		return capitalize(result);
	}
	
	/**
	 * 将给定的字符串转换为更好的标题字符串。
	 * 
	 * <pre>
	 * inflection.titleize(&quot;address_book&quot;); // &quot;Address Book&quot;
	 * inflection.titleize(&quot;My address_book&quot;); // &quot;My Address Book&quot;
	 * </pre>
	 * 
	 * @param word
	 *            要转换的字符串
	 * @return 转换后的字符串
	 */
	public String titleize(String word) {
		return capitalize(humanize(underscore(word)));
	}
	
	/**
	 * 将给定词语转换为表名。
	 * 
	 * <pre>
	 * inflection.tableize(&quot;AddressBook&quot;); // &quot;address_books&quot;
	 * inflection.tableize(&quot;Contact&quot;); // &quot;contacts&quot;
	 * </pre>
	 * 
	 * @param camelCase
	 *            要转换的字符串
	 * @return 转换后的字符串
	 */
	public String tableize(String camelCase) {
		return pluralize(underscore(camelCase));
	}

	/**
	 * 将给定词语转换为类名。
	 * 
	 * <pre>
	 * inflection.tableize(&quot;address_books&quot;); // &quot;AddressBook&quot;
	 * inflection.tableize(&quot;contacts&quot;); // &quot;Contact&quot;
	 * </pre>
	 * 
	 * @param text
	 *            要转换的字符串
	 * @return 转换后的字符串
	 */
	public String classify(String text) {
		return camelize(underscore(singularize(text)));
	}
	
	/**
	 * 将给定词语转换为中划线格式。
	 * 
	 * <pre>
	 * inflection.tableize(&quot;address_books&quot;); // &quot;address-book&quot;
	 * inflection.tableize(&quot;AddressBook&quot;); // &quot;address-book&quot;
	 * </pre>
	 * 
	 * @param word
	 *           要转换的字符串
	 * @return 转换后的字符串
	 */
	public String dasherize(String word) {
		return CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_HYPHEN, underscore(word));
	}

	/**
	 * 将给定字符串的首字母变为大写。
	 * 
	 * @param word
	 *            要转换的字符串
	 * @return 转换后的字符串
	 */
	public String capitalize(String word) {
		Preconditions.checkNotNull(word);
		return Character.toUpperCase(word.charAt(0)) + word.substring(1);
	}
	
	/**
	 * 返回指定数字加上序数词后缀后的字符串。
	 * 
	 * <pre>
	 * inflection.ordinalize(1); // &quot;1st&quot;
	 * inflection.ordinalize(2); // &quot;2nd&quot;
	 * inflection.ordinalize(3); // &quot;3rd&quot;
	 * inflection.ordinalize(100); // &quot;100th&quot;
	 * inflection.ordinalize(103); // &quot;103rd&quot;
	 * inflection.ordinalize(10013); // &quot;10013th&quot;
	 * </pre>
	 * 
	 * @param number
	 *            需要加后缀的数字
	 * @return 转换后的字符串
	 */
	public String ordinalize(int number) {
		int mod100 = number % 100;
	    if (mod100 == 11 || mod100 == 12 || mod100 == 13) {
	      return String.valueOf(number) + "th";
	    }
		switch (number % 10) {
		case 1: return number + "st";
		case 2: return number + "nd";
		case 3: return number + "rd";
		}
		return number + "th";
	}

	/**
	 * 通过附加三个点的方式，将字符串缩短到指定长度。
	 * 
	 * @param text text to shorten
	 * @param length desired length 
	 * @return shortened text
	 */
	public String ellipsize(String text, int length) {
		Preconditions.checkNotNull(text);
		if (text.length() <= length) return text;
		if (length < 4) return "...";
		return text.substring(0, length - 3) + "...";
	}
	
	/**
	 * 将文本简化为无重音版本
	 * <p>
	 * 它利用
	 * {@link Normalizer#normalize(CharSequence, java.text.Normalizer.Form)}
	 * 和{@link Form#NFD} 一般化，然后将重音字符替换为它们的非重音字符
	 * 
	 * <pre>
	 * inflection.titleize(&quot;C'est au neuvième étage&quot;); // &quot;C'est au neuvieme etage&quot;
	 * @param text
	 *            the text to normalize
	 * @return normalized text
	 */
	public String simplify(String text) {
		Preconditions.checkNotNull(text);
		return Normalizer.normalize(text, Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}", "");
	}
	
	private void initEnglishRules() {
		
		Inflections inflect = INFLECTIONS_EN;
		
		inflect.plural("$", "s");
	    inflect.plural("s$", "s");
	    inflect.plural("^(ax|test)is$", "$1es");
	    inflect.plural("(octop|vir)us$", "$1i");
	    inflect.plural("(octop|vir)i$", "$1i");
	    inflect.plural("(alias|status)$", "$1es");
	    inflect.plural("(bu)s$", "$1ses");
	    inflect.plural("(buffal|tomat)o$", "$1oes");
	    inflect.plural("([ti])um$", "$1a");
	    inflect.plural("([ti])a$", "$1a");
	    inflect.plural("sis$", "ses");
	    inflect.plural("(?:([^f])fe|([lr])f)$", "$1$2ves");
	    inflect.plural("(hive)$", "$1s");
	    inflect.plural("([^aeiouy]|qu)y$", "$1ies");
	    inflect.plural("(x|ch|ss|sh)$", "$1es");
	    inflect.plural("(matr|vert|ind)(?:ix|ex)$", "$1ices");
	    inflect.plural("^(m|l)ouse$", "$1ice");
	    inflect.plural("^(m|l)ice$", "$1ice");
	    inflect.plural("^(ox)$", "$1en");
	    inflect.plural("^(oxen)$", "$1");
	    inflect.plural("(quiz)$", "$1zes");

		inflect.singular("s$", "");
	    inflect.singular("(ss)$", "$1");
	    inflect.singular("(n)ews$", "$1ews");
	    inflect.singular("([ti])a$", "$1um");
	    inflect.singular("((a)naly|(b)a|(d)iagno|(p)arenthe|(p)rogno|(s)ynop|(t)he)(sis|ses)$", "$1sis");
	    inflect.singular("(^analy)(sis|ses)$", "$1sis");
	    inflect.singular("([^f])ves$", "$1fe");
	    inflect.singular("(hive)s$", "$1");
	    inflect.singular("(tive)s$", "$1");
	    inflect.singular("([lr])ves$", "$1f");
	    inflect.singular("([^aeiouy]|qu)ies$", "$1y");
	    inflect.singular("(s)eries$", "$1eries");
	    inflect.singular("(m)ovies$", "$1ovie");
	    inflect.singular("(x|ch|ss|sh)es$", "$1");
	    inflect.singular("^(m|l)ice$", "$1ouse");
	    inflect.singular("(bus)(es)?$", "$1");
	    inflect.singular("(o)es$", "$1");
	    inflect.singular("(shoe)s$", "$1");
	    inflect.singular("(cris|test)(is|es)$", "$1is");
	    inflect.singular("^(a)x[ie]s$", "$1xis");
	    inflect.singular("(octop|vir)(us|i)$", "$1us");
	    inflect.singular("(alias|status)(es)?$", "$1");
	    inflect.singular("^(ox)en", "$1");
	    inflect.singular("(vert|ind)ices$", "$1ex");
	    inflect.singular("(matr)ices$", "$1ix");
	    inflect.singular("(quiz)zes$", "$1");
	    inflect.singular("(database)s$", "$1");

	    inflect.irregular("person", "people");
	    inflect.irregular("man", "men");
	    inflect.irregular("child", "children");
	    inflect.irregular("sex", "sexes");
	    inflect.irregular("move", "moves");
	    inflect.irregular("zombie", "zombies");
	    inflect.irregular("stadium", "stadiums");
	    
	    inflect.ignore("equipment information rice money species series fish sheep jeans police data".split(" "));
	}
}
