package com.eric0210.obfuscater.transformer.obfuscator;

import java.util.Map;

import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

/**
 * Renames bundled JAR resources to make their purpose less obvious.
 *
 * @author ItzSomebody
 */
public class ResourceRenamer extends Transformer
{
	@Override
	public void transform()
	{
		// TODO
	}

	@Override
	public ExclusionType getExclusionType()
	{
		return ExclusionType.RESOURCE_RENAMER;
	}

	@Override
	public String getName()
	{
		return "Resource Renamer";
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
