package com.eric0210.obfuscater.transformer.shrinkers;

import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.utils.AccessUtils;

import org.objectweb.asm.tree.ClassNode;

public class DeprecatedAccessRemover extends Shrinker
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper)).forEach(classWrapper ->
		{
			ClassNode classNode = classWrapper.classNode;

			if (AccessUtils.isDeprecated(classNode.access))
			{
				classNode.access &= ~ACC_DEPRECATED;
				counter.incrementAndGet();
			}

			classWrapper.methods.stream().filter(methodWrapper -> !isExcluded(methodWrapper) && AccessUtils.isDeprecated(methodWrapper.methodNode.access)).forEach(methodWrapper ->
			{
				methodWrapper.methodNode.access &= ~ACC_DEPRECATED;
				counter.incrementAndGet();
			});

			classWrapper.fields.stream().filter(fieldWrapper -> !isExcluded(fieldWrapper) && AccessUtils.isDeprecated(fieldWrapper.fieldNode.access)).forEach(fieldWrapper ->
			{
				fieldWrapper.fieldNode.access &= ~ACC_DEPRECATED;
				counter.incrementAndGet();
			});
		});
	}

	@Override
	public String getName()
	{
		return "Useless Access Flags Remover";
	}
}
