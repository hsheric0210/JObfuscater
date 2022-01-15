package com.eric0210.obfuscater.transformer.shrinkers;

import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;

import org.objectweb.asm.tree.ClassNode;

public class SignatureRemover extends Shrinker
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper)).forEach(classWrapper ->
		{
			ClassNode classNode = classWrapper.classNode;

			if (classNode.signature != null)
			{
				classNode.signature = null;
				counter.incrementAndGet();
			}

			classWrapper.methods.stream().filter(methodWrapper -> !isExcluded(methodWrapper) && methodWrapper.methodNode.signature != null).forEach(methodWrapper ->
			{
				methodWrapper.methodNode.signature = null;
				counter.incrementAndGet();
			});

			classWrapper.fields.stream().filter(fieldWrapper -> !isExcluded(fieldWrapper) && fieldWrapper.fieldNode.signature != null).forEach(fieldWrapper ->
			{
				fieldWrapper.fieldNode.signature = null;
				counter.incrementAndGet();
			});
		});

		Logger.stdOut(String.format("Removed %d signatures.", counter.get()));
	}

	@Override
	public String getName()
	{
		return "Signature Remover";
	}
}
