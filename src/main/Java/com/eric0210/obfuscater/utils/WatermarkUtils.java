package com.eric0210.obfuscater.utils;

import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public final class WatermarkUtils
{
	public static String extractIds(final ZipFile zipFile, final String key) throws Throwable
	{
		final ArrayList<String> ids = new ArrayList<>();
		final Enumeration<? extends ZipEntry> entries = zipFile.entries();
		final Map<String, ClassNode> classes = new HashMap<>();
		try
		{
			while (entries.hasMoreElements())
			{
				final ZipEntry entry = entries.nextElement();
				if (!entry.isDirectory() && entry.getName().endsWith(".class"))
					try
					{
						final ClassReader cr = new ClassReader(zipFile.getInputStream(entry));
						final ClassNode classNode = new ClassNode();
						cr.accept(classNode, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
						classes.put(classNode.name, classNode);
					}
					catch (final IOException e)
					{
						e.printStackTrace();
					}
					catch (final Throwable t)
					{
						// Ignored
					}
			}
		}
		finally
		{
			zipFile.close();
		}
		final Map<Integer, Character> embedMap = new LinkedHashMap<>();
		for (final ClassNode classNode : classes.values())
			for (final MethodNode methodNode : classNode.methods)
				for (final AbstractInsnNode insn : methodNode.instructions.toArray())
					if (ASMUtils.isIntInsn(insn) && ASMUtils.isIntInsn(insn.getNext()) && ASMUtils.isIntInsn(insn.getNext().getNext()) && ASMUtils.isIntInsn(insn.getNext().getNext().getNext()) && insn.getNext().getNext().getNext().getNext() != null && insn.getNext().getNext().getNext().getNext().getOpcode() == Opcodes.ISTORE && insn.getNext().getNext().getNext().getNext().getNext() != null && insn.getNext().getNext().getNext().getNext().getNext().getOpcode() == Opcodes.ISTORE && insn.getNext().getNext().getNext().getNext().getNext().getNext() != null && insn.getNext().getNext().getNext().getNext().getNext().getNext().getOpcode() == Opcodes.ISTORE && insn.getNext().getNext().getNext().getNext().getNext().getNext().getNext() != null && insn.getNext().getNext().getNext().getNext().getNext().getNext().getNext().getOpcode() == Opcodes.ISTORE)
					{
						final char character = (char) (ASMUtils.getIntegerFromInsn(insn) ^ ASMUtils.getIntegerFromInsn(insn.getNext()));
						final int index = ASMUtils.getIntegerFromInsn(insn.getNext().getNext()) ^ ASMUtils.getIntegerFromInsn(insn.getNext().getNext().getNext());
						embedMap.put(index, character);
					}
		if (enoughInfo(embedMap))
			return decrypt(constructString(embedMap), key);

		return "No IDs found.";
	}

	private static boolean enoughInfo(final Map<Integer, Character> embedMap)
	{
		if (embedMap.size() < 1)
			return false;
		for (int i = 0, j = embedMap.size(); i < j; i++)
			if (!embedMap.containsKey(i))
				return false;
		return true;
	}

	private static String constructString(final Map<Integer, Character> embedMap)
	{
		final StringBuilder sb = new StringBuilder();
		for (int i = 0, j = embedMap.size(); i < j; i++)
			sb.append((char) embedMap.get(i));
		return sb.toString();
	}

	private static String decrypt(final String enc, final String key)
	{
		final char[] messageChars = enc.toCharArray();
		final char[] keyChars = key.toCharArray();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0, j = messageChars.length; i < j; i++)
			sb.append((char) ((int) messageChars[i] ^ (int) keyChars[i % keyChars.length]));
		return sb.toString();
	}

	private WatermarkUtils()
	{
	}
}
