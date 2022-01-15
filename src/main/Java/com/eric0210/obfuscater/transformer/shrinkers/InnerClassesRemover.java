package com.eric0210.obfuscater.transformer.shrinkers;

import com.eric0210.obfuscater.Logger;

import java.util.concurrent.atomic.AtomicInteger;

public class InnerClassesRemover extends Shrinker
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper) && classWrapper.classNode.innerClasses != null).forEach(classWrapper ->
		{
			counter.addAndGet(classWrapper.classNode.innerClasses.size());
			classWrapper.classNode.innerClasses = null;
		});

		Logger.stdOut(String.format("Removed %d inner classes.", counter.get()));
	}

	@Override
	public String getName()
	{
		return "Inner Classes Remover";
	}
}
