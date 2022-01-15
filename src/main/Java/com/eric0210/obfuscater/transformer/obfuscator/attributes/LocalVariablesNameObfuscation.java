package com.eric0210.obfuscater.transformer.obfuscator.attributes;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.StringGenerator;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

public class LocalVariablesNameObfuscation extends Transformer
{
	public enum Mode
	{
		Remove("Removed"),
		Obfuscate("Obfuscated"),
		Replace("Replaced");

		public final String resultMessage;

		Mode(final String desc)
		{
			this.resultMessage = desc;
		}
	}

	public static class Parameter
	{
		public final LocalVariablesNameObfuscation.Mode mode;
		public final String replaceTo;
		public final StringGenerator nameGenerator;

		public Parameter(final LocalVariablesNameObfuscation.Mode mode, final String value, final StringGenerator nameGenerator)
		{
			this.mode = mode;
			this.replaceTo = value;
			this.nameGenerator = nameGenerator;
		}
	}

	private final Parameter parameter;

	public LocalVariablesNameObfuscation(final Parameter param)
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
		this.getClassWrappers().parallelStream().filter(classWrapper -> !this.isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.classNode.methods.parallelStream().filter(methodNode -> methodNode.localVariables != null).forEach(methodNode ->
		{
			affected_instructions.addAndGet(methodNode.localVariables.size());
			switch (this.parameter.mode)
			{
				case Remove:
					methodNode.localVariables = null;
					break;
				case Obfuscate:
					methodNode.localVariables.forEach(localVariableNode ->
					{
						localVariableNode.name = this.parameter.nameGenerator.generate();
						localVariableNode.desc = 'L' + localVariableNode.name + ';';
					});
					break;
				case Replace:
					methodNode.localVariables.forEach(localVariableNode ->
					{
						localVariableNode.name = this.parameter.replaceTo;
						localVariableNode.desc = 'L' + localVariableNode.name + ';';
					});
					break;
			}
		}));
		Logger.stdOut(String.format("%s %d Local variable names" + (this.parameter.mode == Mode.Replace && this.parameter.replaceTo.length() < 32 ? " to \"" + this.parameter.replaceTo + '"' : "") + '.', this.parameter.mode.resultMessage, affected_instructions.get()));
	}

	@Override
	public final ExclusionType getExclusionType()
	{
		return ExclusionType.LOCAL_VARIABLES;
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
		return "Local variables";
	}
}
