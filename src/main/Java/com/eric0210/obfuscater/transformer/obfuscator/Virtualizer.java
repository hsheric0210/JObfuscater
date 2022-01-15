package com.eric0210.obfuscater.transformer.obfuscator;

import java.util.Map;

import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

/**
 * Translates Java bytecode instructions into a custom bytecode instruction set which can (theoretically) only be understood by the obfuscator's embeddable virtual machine.
 *
 * @author ItzSomebody
 */
public class Virtualizer extends Transformer
{
	@Override
	public void transform()
	{
		// TODO
	}

	@Override
	public ExclusionType getExclusionType()
	{
		return null; // FIXME
	}

	@Override
	public String getName()
	{
		return "Virtualizer";
	}

	@Override
	public Map<String, Object> getConfiguration()
	{
		return null; // TODO
	}

	@Override
	public void setConfiguration(Map<String, Object> config)
	{
		// TODO
	}

	@Override
	public void verifyConfiguration(Map<String, Object> config)
	{
		// TODO
	}
}
