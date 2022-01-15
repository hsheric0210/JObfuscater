package com.eric0210.obfuscater.utils.exclusions;

import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Exclusion
{
	private Pattern exclusion;
	private ExclusionType exclusionType;

	public Exclusion(final String exclusion)
	{
		Optional<ExclusionType> result = Stream.of(ExclusionType.values()).filter(type -> exclusion.startsWith(type.getName())).findFirst();

		if (result.isPresent())
		{
			initFields(exclusion, result.get());
			return;
		}
		this.exclusion = Pattern.compile(exclusion);
		this.exclusionType = ExclusionType.GLOBAL;
	}

	private void initFields(final String exclusion, final ExclusionType type)
	{
		this.exclusion = Pattern.compile(exclusion.substring(type.getName().length() + 2));
		this.exclusionType = type;
	}

	public final ExclusionType getExclusionType()
	{
		return this.exclusionType;
	}

	public final boolean matches(final String other)
	{
		return this.exclusion.matcher(other).matches();
	}

	public final Pattern getPattern()
	{
		return this.exclusion;
	}
}
