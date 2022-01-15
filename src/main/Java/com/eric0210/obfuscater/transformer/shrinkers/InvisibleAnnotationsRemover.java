package com.eric0210.obfuscater.transformer.shrinkers;

import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;

import org.objectweb.asm.tree.ClassNode;

/**
 * Removes annotations invisible to the runtime from classes, methods and fields.
 *
 * @author ItzSomebody
 */
public class InvisibleAnnotationsRemover extends Shrinker
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper)).forEach(classWrapper ->
		{
			ClassNode classNode = classWrapper.classNode;

			if (classNode.invisibleAnnotations != null)
			{
				counter.addAndGet(classNode.invisibleAnnotations.size());
				classNode.invisibleAnnotations = null;
			}

			classWrapper.fields.stream().filter(fieldWrapper -> !isExcluded(fieldWrapper) && fieldWrapper.fieldNode.invisibleAnnotations != null).forEach(fieldWrapper ->
			{
				counter.addAndGet(fieldWrapper.fieldNode.invisibleAnnotations.size());
				fieldWrapper.fieldNode.invisibleAnnotations = null;
			});

			classWrapper.methods.stream().filter(methodWrapper -> !isExcluded(methodWrapper) && methodWrapper.methodNode.invisibleAnnotations != null).forEach(methodWrapper ->
			{
				counter.addAndGet(methodWrapper.methodNode.invisibleAnnotations.size());
				methodWrapper.methodNode.invisibleAnnotations = null;
			});
		});

		Logger.stdOut(String.format("Removed %d invisible annotations.", counter.get()));
	}

	@Override
	public String getName()
	{
		return "Invisible Annotations Remover";
	}
}
