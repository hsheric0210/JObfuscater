package com.eric0210.obfuscater.utils;

import java.lang.reflect.Modifier;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public class VariableProvider
{
	private int max = 0;
	private int argumentSize;

	private VariableProvider()
	{

	}

	public VariableProvider(final MethodNode method)
	{
		this();

		if (!Modifier.isStatic(method.access))
			this.registerExisting(0);

		for (final Type argumentType : Type.getArgumentTypes(method.desc))
			this.registerExisting(argumentType.getSize() + this.max - 1);

		this.argumentSize = this.max;

		for (final AbstractInsnNode abstractInsnNode : method.instructions.toArray())
			if (abstractInsnNode instanceof VarInsnNode)
				this.registerExisting(((VarInsnNode) abstractInsnNode).var);
	}

	private void registerExisting(final int var)
	{
		if (var >= this.max)
			this.max = var + 1;
	}

	public final boolean isUnallocated(final int var)
	{
		return var >= this.max;
	}

	public final boolean isArgument(final int var)
	{
		return var < this.argumentSize;
	}

	public final int allocateVar()
	{
		return this.max++;
	}

}
