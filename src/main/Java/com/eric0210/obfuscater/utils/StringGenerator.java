package com.eric0210.obfuscater.utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StringGenerator
{
	public static StringGenerator RENAMER_CLASSNAME_GENERATOR;
	public static StringGenerator RENAMER_METHODNAME_GENERATOR;
	public static StringGenerator RENAMER_FIELDNAME_GENERATOR;

	private final Set<String> generated = new HashSet<>();
	private final Map<String, HashSet<String>> generated2 = new HashMap<>();

	private String pattern = "[a-z][A-Z]";
	private String prefix = "";
	private String suffix = "";
	private int minlength = 16;
	private int maxlength = 16;
	private boolean duplicateGenerationAllowed;

	public final StringGenerator configure(final StringGeneratorParameter param)
	{
		this.pattern = param.pattern;
		this.prefix = param.prefix;
		this.suffix = param.suffix;
		this.minlength = param.minLength;
		this.maxlength = param.maxLength;
		this.duplicateGenerationAllowed = param.duplicateGenerationEnabled;

		return this;
	}

	private final int getLength()
	{
		if (this.minlength == this.maxlength)
			return this.minlength;

		return RandomUtils.getRandomInt(Math.min(this.minlength, this.maxlength), Math.max(this.minlength, this.maxlength));
	}

	private final String generate0(final int length)
	{
		final String sb;
		if (this.pattern.isEmpty())
		{
			if (!this.prefix.isEmpty() || !this.suffix.isEmpty())
				return this.prefix + this.suffix;
			throw new IllegalArgumentException("More than one string generator parameters are empty. Please make sure fill it.");
		}

		if (this.pattern.length() == 1 && !this.duplicateGenerationAllowed)
			throw new IllegalArgumentException("The dictionary is only one character but duplicate generation are not allowed. This will cause infinite-loop in StringGenerator.");

		sb = IntStream.range(0, length).mapToObj(i -> String.valueOf(chooseChar(this.pattern))).collect(Collectors.joining("", this.prefix, this.suffix));
		return sb;
	}

	public final String generate()
	{
		String generated;

		do
			generated = this.generate0(this.getLength());
		while (!this.duplicateGenerationAllowed && this.generated.contains(generated));

		this.generated.add(generated);

		return generated;
	}

	public final String generate(final String classIdentifier)
	{
		if (classIdentifier == null || classIdentifier.isEmpty())
			return this.generate();

		String generated;

		do
			generated = this.generate0(this.getLength());
		while (!this.duplicateGenerationAllowed && this.generated2.containsKey(classIdentifier) && this.generated2.get(classIdentifier).contains(generated));

		if (!this.generated2.containsKey(classIdentifier))
			this.generated2.put(classIdentifier, new HashSet<>());

		if (this.generated2.get(classIdentifier) != null) // HeisenBug: NPE Protection Do not remove
			this.generated2.get(classIdentifier).add(generated);

		return generated;
	}

	public final void reset()
	{
		this.generated.clear();
		this.generated2.clear();
	}

	public final StringGeneratorParameter getParameter()
	{
		final StringGeneratorParameter param = new StringGeneratorParameter();
		param.pattern = this.pattern;
		param.maxLength = this.maxlength;
		param.minLength = this.minlength;
		param.prefix = this.prefix;
		param.suffix = this.suffix;
		return param;
	}

	private static final char chooseChar(final String pattern)
	{
		final List<Map.Entry<Character, Double>> candidates = new ArrayList<>();
		final int stringLength = pattern.length();
		int totalCount = 0;

		// Calculate total count
		for (int index = 0; index < stringLength;)
			// try to read [<startChar>-<endChar>] format
			if (pattern.length() >= 5 && pattern.charAt(index) == '[' && (index == 0 || pattern.charAt(index - 1) != '\\') && pattern.charAt(index + 4) == ']')
			{
				final char start = pattern.charAt(index + 1);
				final char end = pattern.charAt(index + 3);
				totalCount += end - start + 1;
				index += 5;
			}
			else
			{
				totalCount++;
				index++;
			}

		// Register candidates
		for (int index = 0; index < stringLength;)
			if (pattern.length() >= 5 && pattern.charAt(index) == '[' && (index == 0 || pattern.charAt(index - 1) != '\\') && pattern.charAt(index + 2) == '-' && pattern.charAt(index + 4) == ']')
			{
				final char start = pattern.charAt(index + 1);
				final char end = pattern.charAt(index + 3);
				final int length = end - start + 1;

				candidates.add(new AbstractMap.SimpleEntry((char) (start + RandomUtils.getRandomInt(length)), /* 1.0 / partCount */(double) length / totalCount)); // TODO: Insert chance patch code
				index += 5;
			}
			else
			{
				candidates.add(new AbstractMap.SimpleEntry(pattern.charAt(index), 1.0 / stringLength));
				index++;
			}

		if (candidates.isEmpty())
			return '\0';

		// 출처: https://skyfe.tistory.com/entry/확률을-적용한-랜덤값-선택하기 [철이의 컴노리]
		final double random = RandomUtils.getRandomDouble() * (Double.MAX_VALUE * 0.1); // Improve precision

		candidates.sort(Comparator.comparingDouble(Map.Entry::getValue));

		double cumulative = 0.0;

		for (final Map.Entry<Character, Double> candidate : candidates)
		{
			cumulative += candidate.getValue() * (Double.MAX_VALUE * 0.1); // Improve precision
			if (random <= cumulative)
				return candidate.getKey();
		}

		return '\0'; // Dead code
	}
}
