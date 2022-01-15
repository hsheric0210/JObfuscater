package com.eric0210.obfuscater.transformer.obfuscator;

import java.util.Map;

import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

/**
 * Encrypts bundled resources to prevent viewing.
 *
 * @author ItzSomebody
 */
public class ResourceEncryption extends Transformer
{
	@Override
	public void transform()
	{
		// TODO
	}

	@Override
	public ExclusionType getExclusionType()
	{
		return ExclusionType.RESOURCE_ENCRYPTION;
	}

	@Override
	public String getName()
	{
		return "Resource encryption";
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
