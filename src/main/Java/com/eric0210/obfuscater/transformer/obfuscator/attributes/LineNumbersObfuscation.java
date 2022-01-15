package com.eric0210.obfuscater.transformer.obfuscator.attributes;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.RandomUtils;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

import org.objectweb.asm.tree.LineNumberNode;

public class LineNumbersObfuscation extends Transformer
{
	public enum Mode
	{
		Remove("Removed"),
		Randomize("Randomized"),
		Replace("Replaced");

		public final String resultMessage;

		Mode(final String resultMessage)
		{
			this.resultMessage = resultMessage;
		}
	}

	public static class Parameter
	{
		public final LineNumbersObfuscation.Mode mode;
		public final int replaceTo;
		public final int randomize_min;
		public final int randomize_max;

		public Parameter(final LineNumbersObfuscation.Mode mode, final int value, final int randMin, final int randMax)
		{
			this.mode = mode;
			this.replaceTo = value;
			this.randomize_min = randMin;
			this.randomize_max = randMax;
		}
	}

	private final Parameter parameter;

	public LineNumbersObfuscation(final Parameter param)
	{
		this.parameter = param;
	}

	public final Mode getMode()
	{
		return this.parameter.mode;
	}

	@Override
	public final void transform()
	{
		final AtomicInteger affected_instructions = new AtomicInteger();
		this.getClassWrappers().parallelStream().filter(classWrapper -> !this.isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.classNode.methods.parallelStream().filter(this::hasInstructions).forEach(methodNode ->
		{
			Stream.of(methodNode.instructions.toArray()).filter(insn -> insn instanceof LineNumberNode).forEach(insn ->
			{
				switch (this.parameter.mode)
				{
					case Remove:
						methodNode.instructions.remove(insn);
						break;
					case Randomize:
						((LineNumberNode) insn).line = RandomUtils.getRandomInt(this.parameter.randomize_min, this.parameter.randomize_max);
						break;
					case Replace:
						((LineNumberNode) insn).line = this.parameter.replaceTo;
						break;
				}
				affected_instructions.incrementAndGet();
			});
		}));
		Logger.stdOut(String.format("%s %d line number instructions" + (this.parameter.mode == Mode.Replace ? " to " + this.parameter.replaceTo : "") + '.', this.parameter.mode.resultMessage, affected_instructions.get()));
	}

	@Override
	public final ExclusionType getExclusionType()
	{
		return ExclusionType.LINE_NUMBERS;
	}

	@Override
	public Map<String, Object> getConfiguration()
	{
		return null;
	}

	@Override
	public void setConfiguration(Map<String, Object> config)
	{

	}

	@Override
	public void verifyConfiguration(Map<String, Object> config)
	{

	}

	@Override
	public final String getName()
	{
		return "Line numbers";
	}
}
