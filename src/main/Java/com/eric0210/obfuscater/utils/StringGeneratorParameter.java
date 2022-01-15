package com.eric0210.obfuscater.utils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class StringGeneratorParameter
{
	private String pattern = "[a-z][A-Z]";
	private String prefix = "";
	private String suffix = "";
	private int maxLength = 16;

	private int minLength = 16;

	private boolean duplicateGenerationEnabled;

	public int getMaxLength()
	{
		return maxLength;
	}

	public StringGeneratorParameter setMaxLength(final int maxLength)
	{
		this.maxLength = maxLength;
		return this;
	}

	public int getMinLength()
	{
		return minLength;
	}

	public StringGeneratorParameter setMinLength(final int minLength)
	{
		this.minLength = minLength;
		return this;
	}

	public final String getPattern()
	{
		return this.pattern;
	}

	public final StringGeneratorParameter setPattern(final String pattern)
	{
		this.pattern = pattern;
		return this;
	}

	public final String getPrefix()
	{
		return this.prefix;
	}

	public final StringGeneratorParameter setPrefix(final String prefix)
	{
		this.prefix = prefix;
		return this;
	}

	public final String getSuffix()
	{
		return this.suffix;
	}

	public final StringGeneratorParameter setSuffix(final String suffix)
	{
		this.suffix = suffix;
		return this;
	}

	public boolean isDuplicateGenerationEnabled()
	{
		return duplicateGenerationEnabled;
	}

	public StringGeneratorParameter setDuplicateGenerationEnabled(final boolean duplicateGenerationEnabled)
	{
		this.duplicateGenerationEnabled = duplicateGenerationEnabled;
		return this;
	}

	public static final StringGeneratorParameter fromMap(final Map<String, Object> map)
	{
		return new StringGeneratorParameter().setPattern(String.valueOf(map.get("pattern"))).setPrefix(String.valueOf(map.get("prefix"))).setSuffix(String.valueOf(map.get("suffix"))).setMaxLength((int) map.get("maxlength")).setMinLength((int) map.get("minlength")).setDuplicateGenerationEnabled((boolean) map.get("duplicategenerationenabled"));
	}

	public final Map<String, Object> toMap()
	{
		LinkedHashMap map = new LinkedHashMap<>();
		map.put("pattern", this.getPattern());
		map.put("prefix", this.getPrefix());
		map.put("suffix", this.getSuffix());
		map.put("maxlength", this.getMaxLength());
		map.put("minlength", this.getMinLength());
		map.put("duplicategenerationenabled", this.isDuplicateGenerationEnabled());
		return map;
	}

	public enum StringGeneratorPresets
	{
		// 닮은꼴 문자
		lil("lIl", "Il"), // Which is very hard to find difference on many fonts
		extended_lil("lIl1l", "iIl1"),
		O("O", "0Oo〇◯"),
		X("X", "Xx×"),
		AAAAA("AAA", "AΑА"),
		Quotation_marks("Quotation Marks(따옴표)", "'\"‘’“”"),
		Yijing_Hexagram_Symbols("Yijing Hexagram Symbols", "[䷀-䷿]"),

		// 그 외 문자
		Alphabetical("Alphabetical(알파벳)", "[a-z][A-Z]"),
		alphabetical("alphabetical(알파벳 소문자)", "[a-z]"),
		Alphanumeric("Alphanumeric(알파벳 + 숫자)", "[a-z][A-Z][0-9]"),
		alphanumeric("alphanumeric(알파벳 소문자 + 숫자)", "[a-z][0-9]"),
		NUMERIC("Numeric(숫자)", "[0-9]"),
		WHITE_SPACES("White Spaces(공백)", "	 \u00A0\u3000\uFEFF[\u2000-\u200F]"),
		UNRECOGNIZED("Unrecognized(깨진 문자)", "[ꚬ-ꚳ]"), // Bamum letters which is not supported on many fonts
		Hangul("Hangul(한글)", "[가-힣]"), // 한글(완성형) 문자들
		Hanja("Hanja(한자)", "[一-龥][豈-廓]"), // 한자(완성형) 문자들
		Hiragana("Hiragana(히라가나)", "[\u3040-ゟ]"),
		Katakana("Katakana(가타카나)", "[゠-ヿ]"),
		Metric_unit_symbols("Metric unit symbols(SI 단위 문자)", "[㍱-㍹][㎀-㏟]"),
		ALL("All unicode characters(Warning! use under your own risk!)", "[!-\uFFFF]");

		final String name;
		private final String pattern;

		StringGeneratorPresets(final String name, final String pattern)
		{
			this.name = name;
			this.pattern = pattern;
		}

		public String getName()
		{
			return this.name;
		}

		public String getPattern()
		{
			return this.pattern;
		}

		public static StringGeneratorPresets fromName(final String name)
		{
			return Arrays.stream(values()).filter(preset -> preset.name.equals(name)).findFirst().orElse(null);
		}
	}

}
