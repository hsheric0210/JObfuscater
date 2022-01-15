package com.eric0210.obfuscater.asm;

import org.objectweb.asm.tree.FieldNode;

public class FieldWrapper
{
	public FieldNode fieldNode;
	public final ClassWrapper owner;
	public final String originalName;
	public final String originalDescription;

	public FieldWrapper(final FieldNode fieldNode, final ClassWrapper owner, final String originalName, final String originalDescription)
	{
		this.fieldNode = fieldNode;
		this.owner = owner;
		this.originalName = originalName;
		this.originalDescription = originalDescription;
	}
}
