package com.eric0210.obfuscater.transformer.shrinkers;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.eric0210.obfuscater.Logger;

import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Removes line numbers.
 *
 * @author ItzSomebody.
 */
public class LineNumberRemover extends Shrinker
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		this.getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.stream().filter(methodWrapper -> !isExcluded(methodWrapper) && hasInstructions(methodWrapper)).forEach(methodWrapper ->
		{
			MethodNode methodNode = methodWrapper.methodNode;

			Stream.of(methodNode.instructions.toArray()).filter(insn -> insn instanceof LineNumberNode).forEach(insn ->
			{
				methodNode.instructions.remove(insn);
				counter.incrementAndGet();
			});
		}));

		Logger.stdOut(String.format("Removed %d line numbers.", counter.get()));
	}

	@Override
	public String getName()
	{
		return "Line numbers";
	}
}
