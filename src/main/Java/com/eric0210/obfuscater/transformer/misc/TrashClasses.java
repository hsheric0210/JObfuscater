/*
 * Copyright (C) 2018 ItzSomebody This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>
 */
package com.eric0210.obfuscater.transformer.misc;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Constants;
import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.ASMUtils;
import com.eric0210.obfuscater.utils.RandomUtils;
import com.eric0210.obfuscater.utils.StringGenerator;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

/**
 * Not really a transformer. This "transformer" generates unused classes full of random bytecode.
 *
 * @author ItzSomebody
 */
public class TrashClasses extends Transformer
{
	private static final ArrayList<String> DESCRIPTORS = new ArrayList<>();
	static
	{
		DESCRIPTORS.add("Z"); // Boolean
		DESCRIPTORS.add("C"); // Character
		DESCRIPTORS.add("B"); // Byte
		DESCRIPTORS.add("S"); // Short
		DESCRIPTORS.add("I"); // Integer
		DESCRIPTORS.add("F"); // Float
		DESCRIPTORS.add("J"); // Long
		DESCRIPTORS.add("D"); // Double
		DESCRIPTORS.add("V"); // Void
	}

	@Override
	public final void transform()
	{
		final AtomicInteger counter = new AtomicInteger();
		final ArrayList<String> classNames = new ArrayList<>(this.getClassPath().keySet());
		for (int i = 0, j = classNames.size() % 20; i < j; i++)
			DESCRIPTORS.add('L' + classNames.get(RandomUtils.getRandomInt(classNames.size())) + ';');
		for (int i = 0, j = this.obfuscator.config.trashClasses; i < j; i++)
		{
			final ClassNode classNode = this.generateClass(i);
			final ClassWriter cw = new ClassWriter(0);
			cw.newUTF8("ERIC_OBF_" + Constants.VERSION);
			classNode.accept(cw);
			this.getResources().put(classNode.name + ".class", cw.toByteArray());
			counter.incrementAndGet();
		}

		Logger.stdOut(String.format("Generated %d trash classes.", this.obfuscator.config.trashClasses));
	}

	private ClassNode generateClass(final int order)
	{
		final ClassNode classNode = this.createClass(this.randomClassPath(this.getClasses().keySet()) + (this.isRenamerEnabled() ? StringGenerator.RENAMER_CLASSNAME_GENERATOR.generate() : "Class_" + order));
		final int methodsToGenerate = RandomUtils.getRandomInt(1, 3) + 2;
		for (int i = 0; i < methodsToGenerate; i++)
			classNode.methods.add(this.generateMethod(classNode, i));
		return classNode;
	}

	private ClassNode createClass(final String className)
	{
		final ClassNode classNode = new ClassNode();
		classNode.visit(49, ACC_SUPER + ACC_PUBLIC, className, null, "java/lang/Object", null);
		final MethodVisitor mv = classNode.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
		classNode.visitEnd();
		return classNode;
	}

	private MethodNode generateMethod(final ClassNode classNode, final int order)
	{
		final String randDesc = this.generateDescriptor();
		final MethodNode method = new MethodNode(ACC_STATIC + ACC_PRIVATE, this.isRenamerEnabled() ? StringGenerator.RENAMER_METHODNAME_GENERATOR.generate(classNode.name) : "method" + order, randDesc, null, null);
		final int instructions = RandomUtils.getRandomInt(1, 30) + 30;
		final InsnList insns = new InsnList();
		for (int i = 0; i < instructions; ++i)
			insns.add(this.junkInstructions());
		final Type returnType = Type.getReturnType(randDesc);
		switch (returnType.getSort())
		{
			case Type.VOID:
			{
				insns.add(new InsnNode(RETURN));
				break;
			}
			case Type.BOOLEAN:
			case Type.CHAR:
			case Type.BYTE:
			case Type.SHORT:
			case Type.INT:
			{
				if (RandomUtils.getRandomInt(1, 10) % 2 == 1)
					insns.add(new InsnNode(ICONST_0));
				else
					insns.add(new InsnNode(ICONST_1));
				insns.add(new InsnNode(IRETURN));
				break;
			}
			case Type.FLOAT:
			{
				insns.add(ASMUtils.getNumberInsn(RandomUtils.getRandomFloat()));
				insns.add(new InsnNode(FRETURN));
				break;
			}
			case Type.LONG:
			{
				insns.add(ASMUtils.getNumberInsn(RandomUtils.getRandomLong()));
				insns.add(new InsnNode(LRETURN));
				break;
			}
			case Type.DOUBLE:
			{
				insns.add(ASMUtils.getNumberInsn(RandomUtils.getRandomDouble()));
				insns.add(new InsnNode(DRETURN));
				break;
			}
			default:
			{
				insns.add(new VarInsnNode(ALOAD, RandomUtils.getRandomInt(1, 30)));
				insns.add(new InsnNode(ARETURN));
				break;
			}
		}
		method.instructions = insns;
		return method;
	}

	private String generateDescriptor()
	{
		final StringBuilder sb = new StringBuilder("(");
		for (int i = 0, j = RandomUtils.getRandomInt(1, 7); i < j; i++)
			sb.append(DESCRIPTORS.get(RandomUtils.getRandomInt(DESCRIPTORS.size())));
		sb.append(')');
		sb.append(DESCRIPTORS.get(RandomUtils.getRandomInt(DESCRIPTORS.size())));
		return sb.toString();
	}

	private AbstractInsnNode junkInstructions()
	{
		final int index = RandomUtils.getRandomInt(1, 20);
		switch (index)
		{
			case 0:
				return new MethodInsnNode(INVOKESTATIC, this.isRenamerEnabled() ? StringGenerator.RENAMER_CLASSNAME_GENERATOR.generate() : "Class_" + RandomUtils.getRandomInt(1, 128), this.isRenamerEnabled() ? StringGenerator.RENAMER_METHODNAME_GENERATOR.generate() : "method" + RandomUtils.getRandomInt(1, 128), "(Ljava/lang/String;)V", false);
			case 1:
				return new FieldInsnNode(GETFIELD, this.isRenamerEnabled() ? StringGenerator.RENAMER_CLASSNAME_GENERATOR.generate() : "Class_" + RandomUtils.getRandomInt(1, 128), this.isRenamerEnabled() ? StringGenerator.RENAMER_METHODNAME_GENERATOR.generate() : "method" + RandomUtils.getRandomInt(1, 128), "I");
			case 2:
				return new InsnNode(RandomUtils.getRandomInt(1, 16));
			case 3:
				return new VarInsnNode(ALOAD, RandomUtils.getRandomInt(1, 30));
			case 4:
				return new IntInsnNode(BIPUSH, RandomUtils.getRandomInt(1, 255));
			case 5:
				return new IntInsnNode(SIPUSH, RandomUtils.getRandomInt(1, 25565));
			case 6:
			case 7:
			case 8:
				return new InsnNode(RandomUtils.getRandomInt(1, 5));
			case 9:
				return new LdcInsnNode(new StringGenerator().generate());
			case 10:
				return new IincInsnNode(RandomUtils.getRandomInt(1, 16), RandomUtils.getRandomInt(1, 16));
			case 11:
				return new MethodInsnNode(INVOKESPECIAL, this.isRenamerEnabled() ? StringGenerator.RENAMER_CLASSNAME_GENERATOR.generate() : "Class_" + RandomUtils.getRandomInt(1, 128), this.isRenamerEnabled() ? StringGenerator.RENAMER_METHODNAME_GENERATOR.generate() : "method" + RandomUtils.getRandomInt(1, 128), "()V", false);
			case 12:
				return new MethodInsnNode(INVOKEVIRTUAL, this.isRenamerEnabled() ? StringGenerator.RENAMER_CLASSNAME_GENERATOR.generate() : "Class_" + RandomUtils.getRandomInt(1, 128), this.isRenamerEnabled() ? StringGenerator.RENAMER_METHODNAME_GENERATOR.generate() : "method" + RandomUtils.getRandomInt(1, 128), "(Ljava/lang/Object;)Ljava/lang/Object;", false);
			case 13:
				return new VarInsnNode(ILOAD, RandomUtils.getRandomInt(1, 30));
			case 14:
				return new InsnNode(ATHROW);
			case 15:
				return new MethodInsnNode(INVOKEINTERFACE, StringGenerator.RENAMER_CLASSNAME_GENERATOR.generate(), StringGenerator.RENAMER_METHODNAME_GENERATOR.generate(), "(I)I", false);
			case 16:
				final Handle handle = new Handle(6, this.isRenamerEnabled() ? StringGenerator.RENAMER_CLASSNAME_GENERATOR.generate() : "Class_" + RandomUtils.getRandomInt(1, 128), this.isRenamerEnabled() ? StringGenerator.RENAMER_METHODNAME_GENERATOR.generate() : "method" + RandomUtils.getRandomInt(1, 128), this.isRenamerEnabled() ? StringGenerator.RENAMER_METHODNAME_GENERATOR.generate() : "method" + RandomUtils.getRandomInt(1, 128), false);
				return new InvokeDynamicInsnNode(this.isRenamerEnabled() ? StringGenerator.RENAMER_METHODNAME_GENERATOR.generate() : "method" + RandomUtils.getRandomInt(1, 128), this.isRenamerEnabled() ? StringGenerator.RENAMER_METHODNAME_GENERATOR.generate() : "method" + RandomUtils.getRandomInt(1, 128), handle, RandomUtils.getRandomInt(1, 5), RandomUtils.getRandomInt(1, 5), RandomUtils.getRandomInt(1, 5), RandomUtils.getRandomInt(1, 5), RandomUtils.getRandomInt(1, 5));
			case 17:
				return new IntInsnNode(ANEWARRAY, RandomUtils.getRandomInt(1, 30));
			case 18:
				return new VarInsnNode(ASTORE, RandomUtils.getRandomInt(1, 30));
			case 19:
			default:
				return new VarInsnNode(ISTORE, RandomUtils.getRandomInt(1, 30));
		}
	}

	@Override
	public final ExclusionType getExclusionType()
	{
		return null;
	}

	@Override
	public Map<String, Object> getConfiguration()
	{
		return null;
	}

	@Override
	public void setConfiguration(Map<String, Object> config)
	{
		// Not needed
	}

	@Override
	public void verifyConfiguration(Map<String, Object> config)
	{
		// Not needed
	}

	@Override
	public final String getName()
	{
		return "Trash classes";
	}
}
