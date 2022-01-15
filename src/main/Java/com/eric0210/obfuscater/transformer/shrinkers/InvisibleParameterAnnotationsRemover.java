package com.eric0210.obfuscater.transformer.shrinkers;

import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;

public class InvisibleParameterAnnotationsRemover extends Shrinker
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.stream().filter(methodWrapper -> !isExcluded(methodWrapper) && methodWrapper.methodNode.invisibleParameterAnnotations != null).forEach(methodWrapper ->
		{

			counter.addAndGet(methodWrapper.methodNode.invisibleAnnotableParameterCount);
			methodWrapper.methodNode.invisibleParameterAnnotations = null;
		}));

		Logger.stdOut(String.format("Removed %d invisible parameter annotations.", counter.get()));
	}

	@Override
	public String getName()
	{
		return "Invisible Parameter Annotations Remover";
	}
}
