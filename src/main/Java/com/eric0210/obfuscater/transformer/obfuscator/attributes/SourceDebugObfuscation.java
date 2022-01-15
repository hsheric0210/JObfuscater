package com.eric0210.obfuscater.transformer.obfuscator.attributes;

import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.StringGenerator;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

public class SourceDebugObfuscation extends Transformer
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
		public final SourceDebugObfuscation.Mode mode;
		public final String replaceTo;
		public final StringGenerator nameGenerator;

		public Parameter(final SourceDebugObfuscation.Mode mode, final String value, final StringGenerator nameGenerator)
		{
			this.mode = mode;
			this.replaceTo = value;
			this.nameGenerator = nameGenerator;
		}
	}

	private final Parameter parameter;

	public SourceDebugObfuscation(final Parameter param)
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
		final AtomicInteger affects = new AtomicInteger();
		this.getClassWrappers().stream().filter(classWrapper -> !this.isExcluded(classWrapper)).forEach(classWrapper ->
		{
			classWrapper.classNode.sourceDebug = this.parameter.mode == Mode.Remove ? null : this.parameter.mode == Mode.Randomize ? this.parameter.nameGenerator.generate() : this.parameter.replaceTo;
			affects.incrementAndGet();
		});
		Logger.stdOut(String.format("%s %d SourceDebug attributes" + (this.parameter.mode == Mode.Replace && this.parameter.replaceTo.length() < 32 ? " to \"" + this.parameter.replaceTo + '"' : "") + '.', this.parameter.mode.resultMessage, affects.get()));
	}

	@Override
	public final ExclusionType getExclusionType()
	{
		return ExclusionType.SOURCE_DEBUG;
	}

	@Override
	public final String getName()
	{
		return "SourceDebug";
	}
}
