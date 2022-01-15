package com.eric0210.obfuscater.transformer.shrinkers;

import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.utils.AccessUtils;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

public class SyntheticAccessRemover extends Shrinker
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper)).forEach(classWrapper ->
		{
			ClassNode classNode = classWrapper.classNode;

			if (AccessUtils.isSynthetic(classNode.access))
			{
				classNode.access &= ~ACC_SYNTHETIC;
				counter.incrementAndGet();
			}

			classWrapper.methods.stream().filter(methodWrapper -> !isExcluded(methodWrapper)).forEach(methodWrapper ->
			{
				MethodNode methodNode = methodWrapper.methodNode;

				if (AccessUtils.isSynthetic(methodNode.access) || AccessUtils.isBridge(methodNode.access))
				{
					methodNode.access &= ~(ACC_SYNTHETIC | ACC_BRIDGE);
					counter.incrementAndGet();
				}
			});

			classWrapper.fields.stream().filter(fieldWrapper -> !isExcluded(fieldWrapper)).forEach(fieldWrapper ->
			{
				FieldNode fieldNode = fieldWrapper.fieldNode;

				if (AccessUtils.isSynthetic(fieldNode.access))
				{
					fieldNode.access &= ~ACC_SYNTHETIC;
					counter.incrementAndGet();
				}
			});
		});
	}

	@Override
	public String getName()
	{
		return "Synthetic Access Remover";
	}
}
