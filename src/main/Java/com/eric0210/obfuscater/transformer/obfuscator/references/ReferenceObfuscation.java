package com.eric0210.obfuscater.transformer.obfuscator.references;

import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

public class ReferenceObfuscation extends Transformer
{
	private boolean hideMethodsWithInvokedynamicEnabled;
	private boolean hideFieldsWithInvokedynamicEnabled;

	private boolean hideMethodsWithReflectionEnabled;
	private boolean hideFieldsWithReflectionEnabled;

	private boolean ignoreJava8ClassesForReflectionEnabled;

	@Override
	public void transform()
	{
		// TODO
	}

	@Override
	public String getName()
	{
		return "Reference obfuscation";
	}

	@Override
	public ExclusionType getExclusionType()
	{
		return ExclusionType.REFERENCE_OBFUSCATION;
	}
}
