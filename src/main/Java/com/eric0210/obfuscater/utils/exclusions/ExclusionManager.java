package com.eric0210.obfuscater.utils.exclusions;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExclusionManager
{
	private final List<Exclusion> exclusions = new ArrayList<>();

	public final List<Exclusion> getExclusions()
	{
		return this.exclusions;
	}

	public final void addExclusion(final Exclusion exclusion)
	{
		this.exclusions.add(exclusion);
	}

	public final boolean isExcluded(final String pattern, final ExclusionType type)
	{
		Optional<Exclusion> result = exclusions.stream().filter(exclusion -> exclusion.getExclusionType() == type || exclusion.getExclusionType() == ExclusionType.GLOBAL).findFirst();

		return result.isPresent() && result.get().matches(pattern);
	}
}
