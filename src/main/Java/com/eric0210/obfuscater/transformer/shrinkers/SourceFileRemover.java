package com.eric0210.obfuscater.transformer.shrinkers;

import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;

/**
 * Removes the sourcefile attribute.
 *
 * @author ItzSomebody
 */
public class SourceFileRemover extends Shrinker
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper) && classWrapper.classNode.sourceFile != null).forEach(classWrapper ->
		{
			classWrapper.classNode.sourceFile = null;
			counter.incrementAndGet();
		});

		Logger.stdOut(String.format("Removed %d source name attributes.", counter.get()));
	}

	@Override
	public String getName()
	{
		return "Source name";
	}
}
