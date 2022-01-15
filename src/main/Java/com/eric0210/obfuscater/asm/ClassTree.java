package com.eric0210.obfuscater.asm;

import java.util.HashSet;

public class ClassTree
{
	public final ClassWrapper classWrapper;
	public final HashSet<String> parentClasses = new HashSet<>();
	public final HashSet<String> subClasses = new HashSet<>();

	public ClassTree(final ClassWrapper classWrapper)
	{
		this.classWrapper = classWrapper;
	}
}
