package com.eric0210.obfuscater.transformer.shrinkers;

import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;

import org.objectweb.asm.tree.ClassNode;

/**
 * Strips out visible parameter annotations.
 *
 * @author ItzSomebody
 */
public class VisibleTypeAnnotationsRemover extends Shrinker
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper)).forEach(classWrapper ->
		{
			ClassNode classNode = classWrapper.classNode;

			if (classNode.visibleTypeAnnotations != null)
			{
				counter.addAndGet(classNode.visibleTypeAnnotations.size());
				classNode.visibleTypeAnnotations = null;
			}

			classWrapper.fields.stream().filter(fieldWrapper -> !isExcluded(fieldWrapper) && fieldWrapper.fieldNode.visibleTypeAnnotations != null).forEach(fieldWrapper ->
			{
				counter.addAndGet(fieldWrapper.fieldNode.visibleTypeAnnotations.size());
				fieldWrapper.fieldNode.visibleTypeAnnotations = null;
			});

			classWrapper.methods.stream().filter(methodWrapper -> !isExcluded(methodWrapper) && methodWrapper.methodNode.visibleTypeAnnotations != null).forEach(methodWrapper ->
			{
				counter.addAndGet(methodWrapper.methodNode.visibleTypeAnnotations.size());
				methodWrapper.methodNode.visibleTypeAnnotations = null;
			});
		});

		Logger.stdOut(String.format("Removed %d visible type annotations.", counter.get()));
	}

	@Override
	public String getName()
	{
		return "Visible Type Annotations Remover";
	}
}
