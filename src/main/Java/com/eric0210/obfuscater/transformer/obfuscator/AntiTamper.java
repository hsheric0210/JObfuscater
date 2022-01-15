package com.eric0210.obfuscater.transformer.obfuscator;

import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.ConfigUtils;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This applies some type of integrity-aware code. Currently, there are two types of anti-tampers: passive and active. The active anti-tamper will actively search for modifications to the JAR and crash the JVM. The passive anti-tamper will modify its
 * environment based on random components of the program.
 *
 * @author ItzSomebody
 */
public class AntiTamper extends Transformer
{
	private static final int PASSIVE = 1;
	private static final int ACTIVE = 2;
	private int type;

	@Override
	public void transform()
	{
		// TODO
	}

	@Override
	public String getName()
	{
		return "Anti-Tamper";
	}

	@Override
	public ExclusionType getExclusionType()
	{
		return ExclusionType.ANTI_TAMPER;
	}

	public void setType(String mode)
	{
		if (mode.equalsIgnoreCase("passive"))
			type = PASSIVE;
		else if (mode.equalsIgnoreCase("active"))
			type = ACTIVE;
	}

	@Override
	public Map<String, Object> getConfiguration()
	{
		Map<String, Object> config = new LinkedHashMap<>();

		config.put("mode", ((type == PASSIVE) ? "passive" : "active"));

		return config;
	}

	@Override
	public void setConfiguration(Map<String, Object> config)
	{
		setType(ConfigUtils.getValueOrDefault("mode", config, "passive"));
	}

	@Override
	public void verifyConfiguration(Map<String, Object> config)
	{
		// TODO
	}

}
