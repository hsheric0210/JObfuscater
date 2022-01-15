package com.eric0210.obfuscater.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.eric0210.obfuscater.asm.ClassWrapper;
import com.eric0210.obfuscater.transformer.Transformer;

public final class RandomUtils
{
	public static int getRandomInt()
	{
		return ThreadLocalRandom.current().nextInt();
	}

	public static int getRandomInt(final int bounds)
	{
		return ThreadLocalRandom.current().nextInt(bounds);
	}

	public static int getRandomInt(final int origin, final int bounds)
	{
		if (origin == bounds)
			return origin;
		return ThreadLocalRandom.current().nextInt(origin, bounds);
	}

	public static long getRandomLong()
	{
		return ThreadLocalRandom.current().nextLong();
	}

	public static long getRandomLong(final long bounds)
	{
		return ThreadLocalRandom.current().nextLong(1, bounds);
	}

	public static float getRandomFloat()
	{
		return ThreadLocalRandom.current().nextFloat();
	}

	public static float getRandomFloat(float bounds)
	{
		return (float) ThreadLocalRandom.current().nextDouble(bounds);
	}

	public static double getRandomDouble()
	{
		return ThreadLocalRandom.current().nextDouble();
	}

	public static double getRandomDouble(double bounds)
	{
		return ThreadLocalRandom.current().nextDouble(bounds);
	}

	public static boolean getRandomBoolean()
	{
		return ThreadLocalRandom.current().nextBoolean();
	}

	public static String getRandomArray(final String... s)
	{
		return s.length > 0 ? s[getRandomInt(s.length)] : "";
	}

	private RandomUtils()
	{
	}

	public static int getRandomIntWithExclusion(final int startInclusive, final int endExclusive, final Collection<Integer> exclusions)
	{
		final List<Integer> list = IntStream.range(startInclusive, endExclusive).boxed().collect(Collectors.toCollection(() -> new ArrayList<>(endExclusive - startInclusive)));

		if (exclusions != null)
			list.removeIf(exclusions::contains);

		if (list.isEmpty())
			return -1;

		return list.get(getRandomInt(list.size()));
	}

	public static final ClassWrapper getRandomClassCanInject(Transformer transformer, Collection<ClassWrapper> classWrappers, Predicate<ClassWrapper> apred)
	{
		final Predicate<ClassWrapper> pred = classWrapper -> !transformer.isExcluded(classWrapper) && AccessUtils.canCreateStaticField(classWrapper.classNode);
		final Predicate<ClassWrapper> p = apred != null ? pred.and(apred) : pred;
		if (classWrappers.stream().filter(p).count() == 0)
			return null;
		return (ClassWrapper) classWrappers.stream().filter(p).toArray()[RandomUtils.getRandomInt(classWrappers.stream().filter(p).toArray().length)];
	}
}
