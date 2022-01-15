package com.eric0210.obfuscater.transformer.shrinkers;

import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;

public class OuterMethodRemover extends Shrinker
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper) && classWrapper.classNode.outerClass != null).forEach(classWrapper ->
		{
			classWrapper.classNode.outerClass = null;
			classWrapper.classNode.outerMethod = null;
			classWrapper.classNode.outerMethodDesc = null;

			counter.incrementAndGet();
		});

		Logger.stdOut(String.format("Removed %d outer methods.", counter.get()));
	}

	@Override
	public String getName()
	{
		return "Outer Method Remover";
	}
}
