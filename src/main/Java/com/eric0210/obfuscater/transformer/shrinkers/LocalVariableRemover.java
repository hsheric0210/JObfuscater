package com.eric0210.obfuscater.transformer.shrinkers;

import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;

import org.objectweb.asm.tree.MethodNode;

/**
 * Destroys the local variable table.
 *
 * @author ItzSomebody
 */
public class LocalVariableRemover extends Shrinker
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.stream().filter(methodWrapper -> !isExcluded(methodWrapper) && methodWrapper.methodNode.localVariables != null).forEach(methodWrapper ->
		{
			MethodNode methodNode = methodWrapper.methodNode;

			counter.addAndGet(methodNode.localVariables.size());
			methodNode.localVariables = null;
		}));

		Logger.stdOut(String.format("Removed %d local variables.", counter.get()));
	}

	@Override
	public String getName()
	{
		return "Local variables";
	}
}
