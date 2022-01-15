package com.eric0210.obfuscater.transformer.shrinkers;

import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;

import org.objectweb.asm.tree.ClassNode;

/**
 * Removes annotations visible to the runtime.
 *
 * @author ItzSomebody
 */
public class VisibleAnnotationsRemover extends Shrinker
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper)).forEach(classWrapper ->
		{
			ClassNode classNode = classWrapper.classNode;

			if (classNode.visibleAnnotations != null)
			{
				counter.addAndGet(classNode.visibleAnnotations.size());
				classNode.visibleAnnotations = null;
			}

			classWrapper.fields.stream().filter(fieldWrapper -> !isExcluded(fieldWrapper) && fieldWrapper.fieldNode.visibleAnnotations != null).forEach(fieldWrapper ->
			{
				counter.addAndGet(fieldWrapper.fieldNode.visibleAnnotations.size());
				fieldWrapper.fieldNode.visibleAnnotations = null;
			});

			classWrapper.methods.stream().filter(methodWrapper -> !isExcluded(methodWrapper) && methodWrapper.methodNode.visibleAnnotations != null).forEach(methodWrapper ->
			{
				counter.addAndGet(methodWrapper.methodNode.visibleAnnotations.size());
				methodWrapper.methodNode.visibleAnnotations = null;
			});
		});

		Logger.stdOut(String.format("Removed %d visible annotations.", counter.get()));
	}

	@Override
	public String getName()
	{
		return "Visible Annotations Remover";
	}
}
