package com.eric0210.obfuscater.utils;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public final class AccessUtils
{
	private AccessUtils()
	{
	}

	public static boolean isNative(final int access)
	{
		return (Opcodes.ACC_NATIVE & access) != 0;
	}

	public static boolean isSynthetic(final int access)
	{
		return (Opcodes.ACC_SYNTHETIC & access) != 0;
	}

	public static boolean isBridge(final int access)
	{
		return (Opcodes.ACC_BRIDGE & access) != 0;
	}

	public static boolean canCreateStaticField(final ClassNode node)
	{
		return (Opcodes.ACC_PUBLIC & node.access) != 0 && (Opcodes.ACC_ENUM & node.access) == 0 && (Opcodes.ACC_INTERFACE & node.access) == 0;
	}

	public static boolean isVarargs(int access)
	{
		return (Opcodes.ACC_VARARGS & access) != 0;
	}

	public static boolean isDeprecated(int access)
	{
		return (Opcodes.ACC_DEPRECATED & access) != 0;
	}

}
