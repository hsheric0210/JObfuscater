package com.eric0210.obfuscater.asm;

import java.util.Map;

import org.objectweb.asm.commons.SimpleRemapper;

public class MemberRemapper extends SimpleRemapper
{
	public MemberRemapper(final Map<String, String> mappings)
	{
		super(mappings);
	}

	@Override
	public final String mapFieldName(final String owner, final String name, final String desc)
	{
		final String remappedName = this.map(owner + '.' + name + '.' + desc);
		return remappedName != null ? remappedName : name;
	}
}
