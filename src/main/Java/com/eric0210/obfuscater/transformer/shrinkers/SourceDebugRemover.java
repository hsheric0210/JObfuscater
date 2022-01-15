package com.eric0210.obfuscater.transformer.shrinkers;

import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;

/**
 * Removes the sourcedebugextension attribute.
 *
 * @author ItzSomebody
 */
public class SourceDebugRemover extends Shrinker
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper) && classWrapper.classNode.sourceDebug != null).forEach(classWrapper ->
		{
			classWrapper.classNode.sourceDebug = null;
			counter.incrementAndGet();
		});

		Logger.stdOut(String.format("Remove %d source debug attributes.", counter.get()));
	}

	@Override
	public String getName()
	{
		return "Source debug";
	}
}
