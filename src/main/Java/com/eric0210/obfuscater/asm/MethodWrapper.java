package com.eric0210.obfuscater.asm;

import org.objectweb.asm.tree.MethodNode;

public class MethodWrapper
{
	public MethodNode methodNode;
	public final ClassWrapper owner;
	public final String originalName;
	public final String originalDescription;

	public MethodWrapper(final MethodNode methodNode, final ClassWrapper owner, final String originalName, final String originalDescription)
	{
		this.methodNode = methodNode;
		this.owner = owner;
		this.originalName = originalName;
		this.originalDescription = originalDescription;
	}
}
