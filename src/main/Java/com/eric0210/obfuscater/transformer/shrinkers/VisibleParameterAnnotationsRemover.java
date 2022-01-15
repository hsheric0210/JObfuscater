package com.eric0210.obfuscater.transformer.shrinkers;

import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;

/**
 * Strips out visible parameter annotations.
 *
 * @author ItzSomebody
 */
public class VisibleParameterAnnotationsRemover extends Shrinker
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.stream().filter(methodWrapper -> !isExcluded(methodWrapper) && methodWrapper.methodNode.visibleParameterAnnotations != null).forEach(methodWrapper ->
		{
			counter.addAndGet(methodWrapper.methodNode.visibleAnnotableParameterCount);
			methodWrapper.methodNode.visibleParameterAnnotations = null;
		}));

		Logger.stdOut(String.format("Removed %d visible parameter annotations.", counter.get()));
	}

	@Override
	public String getName()
	{
		return "Visible Parameter Annotations Remover";
	}
}
