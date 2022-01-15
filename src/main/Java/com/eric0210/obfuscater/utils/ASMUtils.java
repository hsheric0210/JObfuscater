package com.eric0210.obfuscater.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Predicate;

import com.eric0210.obfuscater.ObfuscatorGUI;
import com.eric0210.obfuscater.exceptions.ObfuscatorException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

public final class ASMUtils
{
	public static boolean isInstruction(final AbstractInsnNode insn)
	{
		return !(insn instanceof FrameNode) && !(insn instanceof LineNumberNode) && !(insn instanceof LabelNode);
	}

	public static AbstractInsnNode getNext(final AbstractInsnNode node)
	{
		AbstractInsnNode next = node.getNext();
		while (!isInstruction(next))
			next = next.getNext();
		return next;
	}

	public static ClassNode toNode(final String className) throws IOException
	{
		final ClassReader classReader = new ClassReader(ObfuscatorGUI.class.getResourceAsStream('/' + className.replace('.', '/') + ".class"));
		final ClassNode classNode = new ClassNode();
		classReader.accept(classNode, 0);
		return classNode;
	}

	public static boolean isReturn(final int opcode)
	{
		return opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN;
	}

	public static boolean hasAnnotations(final ClassNode classNode)
	{
		return classNode.visibleAnnotations != null && !classNode.visibleAnnotations.isEmpty() || classNode.invisibleAnnotations != null && !classNode.invisibleAnnotations.isEmpty();
	}

	public static boolean hasAnnotations(final MethodNode methodNode)
	{
		return methodNode.visibleAnnotations != null && !methodNode.visibleAnnotations.isEmpty() || methodNode.invisibleAnnotations != null && !methodNode.invisibleAnnotations.isEmpty();
	}

	public static boolean hasAnnotations(final FieldNode fieldNode)
	{
		return fieldNode.visibleAnnotations != null && !fieldNode.visibleAnnotations.isEmpty() || fieldNode.invisibleAnnotations != null && !fieldNode.invisibleAnnotations.isEmpty();
	}

	public static boolean isIntInsn(final AbstractInsnNode insn)
	{
		if (insn == null)
			return false;
		final int opcode = insn.getOpcode();
		return opcode >= Opcodes.ICONST_M1 && opcode <= Opcodes.ICONST_5 || opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH || insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof Integer;
	}

	public static boolean isLongInsn(final AbstractInsnNode insn)
	{
		final int opcode = insn.getOpcode();
		return opcode == Opcodes.LCONST_0 || opcode == Opcodes.LCONST_1 || insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof Long;
	}

	public static boolean isFloatInsn(final AbstractInsnNode insn)
	{
		final int opcode = insn.getOpcode();
		return opcode >= Opcodes.FCONST_0 && opcode <= Opcodes.FCONST_2 || insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof Float;
	}

	public static boolean isDoubleInsn(final AbstractInsnNode insn)
	{
		final int opcode = insn.getOpcode();
		return opcode >= Opcodes.DCONST_0 && opcode <= Opcodes.DCONST_1 || insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof Double;
	}

	public static AbstractInsnNode getNumberInsn(final int number)
	{
		if (number >= -1 && number <= 5)
			return new InsnNode(number + 3);
		else if (number >= -128 && number <= 127)
			return new IntInsnNode(Opcodes.BIPUSH, number);
		else if (number >= -32768 && number <= 32767)
			return new IntInsnNode(Opcodes.SIPUSH, number);
		else
			return new LdcInsnNode(number);
	}

	public static AbstractInsnNode getNumberInsn(final long number)
	{
		if (number >= 0 && number <= 1)
			return new InsnNode((int) (number + 9));

		return new LdcInsnNode(number);
	}

	public static AbstractInsnNode getNumberInsn(final float number)
	{
		if (number >= 0 && number <= 2)
			return new InsnNode((int) (number + 11));

		return new LdcInsnNode(number);
	}

	public static AbstractInsnNode getNumberInsn(final double number)
	{
		if (number >= 0 && number <= 1)
			return new InsnNode((int) (number + 14));

		return new LdcInsnNode(number);
	}

	public static int getIntegerFromInsn(final AbstractInsnNode insn)
	{
		final int opcode = insn.getOpcode();
		if (opcode >= Opcodes.ICONST_M1 && opcode <= Opcodes.ICONST_5)
			return opcode - 3;
		else if (insn instanceof IntInsnNode && insn.getOpcode() != Opcodes.NEWARRAY)
			return ((IntInsnNode) insn).operand;
		else if (insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof Integer)
			return (Integer) ((LdcInsnNode) insn).cst;

		throw new ObfuscatorException("Unexpected instruction");
	}

	public static long getLongFromInsn(final AbstractInsnNode insn)
	{
		final int opcode = insn.getOpcode();
		if (opcode >= Opcodes.LCONST_0 && opcode <= Opcodes.LCONST_1)
			return opcode - 9;
		else if (insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof Long)
			return (Long) ((LdcInsnNode) insn).cst;
		throw new ObfuscatorException("Unexpected instruction");
	}

	public static float getFloatFromInsn(final AbstractInsnNode insn)
	{
		final int opcode = insn.getOpcode();
		if (opcode >= Opcodes.FCONST_0 && opcode <= Opcodes.FCONST_2)
			return opcode - 11;
		else if (insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof Float)
			return (Float) ((LdcInsnNode) insn).cst;
		throw new ObfuscatorException("Unexpected instruction");
	}

	public static double getDoubleFromInsn(final AbstractInsnNode insn)
	{
		final int opcode = insn.getOpcode();
		if (opcode >= Opcodes.DCONST_0 && opcode <= Opcodes.DCONST_1)
			return opcode - 14;
		else if (insn instanceof LdcInsnNode && ((LdcInsnNode) insn).cst instanceof Double)
			return (Double) ((LdcInsnNode) insn).cst;
		throw new ObfuscatorException("Unexpected instruction");
	}

	private static final Printer printer = new Textifier();
	private static final TraceMethodVisitor methodPrinter = new TraceMethodVisitor(printer);
	private static final HashMap<Type, String> TYPE_TO_WRAPPER = new HashMap<>();
	static
	{
		TYPE_TO_WRAPPER.put(Type.INT_TYPE, "java/lang/Integer");
		TYPE_TO_WRAPPER.put(Type.VOID_TYPE, "java/lang/Void");
		TYPE_TO_WRAPPER.put(Type.BOOLEAN_TYPE, "java/lang/Boolean");
		TYPE_TO_WRAPPER.put(Type.CHAR_TYPE, "java/lang/Character");
		TYPE_TO_WRAPPER.put(Type.BYTE_TYPE, "java/lang/Byte");
		TYPE_TO_WRAPPER.put(Type.SHORT_TYPE, "java/lang/Short");
		TYPE_TO_WRAPPER.put(Type.FLOAT_TYPE, "java/lang/Float");
		TYPE_TO_WRAPPER.put(Type.LONG_TYPE, "java/lang/Long");
		TYPE_TO_WRAPPER.put(Type.DOUBLE_TYPE, "java/lang/Double");
	}

	public static String prettyprint(final AbstractInsnNode insnNode)
	{
		insnNode.accept(methodPrinter);
		final StringWriter sw = new StringWriter();
		printer.print(new PrintWriter(sw));
		printer.getText().clear();
		return sw.toString().trim();
	}

	public static String prettyprint(final InsnList insnNode)
	{
		insnNode.accept(methodPrinter);
		final StringWriter sw = new StringWriter();
		printer.print(new PrintWriter(sw));
		printer.getText().clear();
		return sw.toString().trim();
	}

	public static String prettyprint(final MethodNode insnNode)
	{
		insnNode.accept(methodPrinter);
		final StringWriter sw = new StringWriter();
		printer.print(new PrintWriter(sw));
		printer.getText().clear();
		return sw.toString().trim();
	}

	public static AbstractInsnNode getWrapperMethod(final Type type)
	{
		if (type.getSort() != Type.VOID && TYPE_TO_WRAPPER.containsKey(type))
			return new MethodInsnNode(Opcodes.INVOKESTATIC, TYPE_TO_WRAPPER.get(type), "valueOf", '(' + type.toString() + ")L" + TYPE_TO_WRAPPER.get(type) + ';', false);
		return new InsnNode(Opcodes.NOP);
	}

	public static AbstractInsnNode getTypeNode(final Type type)
	{
		if (TYPE_TO_WRAPPER.containsKey(type))
			return new FieldInsnNode(Opcodes.GETSTATIC, TYPE_TO_WRAPPER.get(type), "TYPE", "Ljava/lang/Class;");
		return new LdcInsnNode(type);
	}

	public static AbstractInsnNode getUnWrapMethod(final Type type)
	{
		if (TYPE_TO_WRAPPER.containsKey(type))
		{
			final String internalName = getInternalName(type);
			return new MethodInsnNode(Opcodes.INVOKESTATIC, TYPE_TO_WRAPPER.get(type), internalName + "Value", "(L" + TYPE_TO_WRAPPER.get(type) + ";)" + type.toString(), false);
		}
		return new InsnNode(Opcodes.NOP);
	}

	// mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf",
	// "(I)Ljava/lang/Integer;", false);
	public static boolean isIntegerNumber(final AbstractInsnNode ain)
	{
		if (ain.getOpcode() == Opcodes.BIPUSH || ain.getOpcode() == Opcodes.SIPUSH)
			return true;
		if (ain.getOpcode() >= Opcodes.ICONST_M1 && ain.getOpcode() <= Opcodes.ICONST_5)
			return true;
		if (ain instanceof LdcInsnNode)
		{
			final LdcInsnNode ldc = (LdcInsnNode) ain;
			return ldc.cst instanceof Integer;
		}
		return false;
	}

	public static int getIntValue(final AbstractInsnNode node)
	{
		if (node.getOpcode() >= Opcodes.ICONST_M1 && node.getOpcode() <= Opcodes.ICONST_5)
			return node.getOpcode() - 3;
		if (node.getOpcode() == Opcodes.SIPUSH || node.getOpcode() == Opcodes.BIPUSH)
			return ((IntInsnNode) node).operand;
		if (node instanceof LdcInsnNode && ((LdcInsnNode) node).cst instanceof Integer)
			return (int) ((LdcInsnNode) node).cst;
		throw new ObfuscatorException(node + " isn't an integer node");
	}

	public static MethodInsnNode toCallNode(final MethodNode method, final ClassNode classNode)
	{
		return new MethodInsnNode(Modifier.isStatic(method.access) ? Opcodes.INVOKESTATIC : Opcodes.INVOKEVIRTUAL, classNode.name, method.name, method.desc, false);
	}

	public static InsnList removeFromOpcode(final InsnList insnList, final int code)
	{
		for (final AbstractInsnNode node : insnList.toArray().clone())
			if (node.getOpcode() == code)
				insnList.remove(node);
		return insnList;
	}

	public static boolean isConditionalGoto(final AbstractInsnNode abstractInsnNode)
	{
		return abstractInsnNode.getOpcode() >= Opcodes.IFEQ && abstractInsnNode.getOpcode() <= Opcodes.IF_ACMPNE;
	}

	public static int getFreeSlot(final MethodNode method)
	{
		int max = Arrays.stream(method.instructions.toArray()).filter(ain -> ain instanceof VarInsnNode).mapToInt(ain -> ((VarInsnNode) ain).var).filter(ain -> ain >= 0).max().orElse(0);
		return max + 1;
	}

	public static MethodNode getMethod(final ClassNode classNode, final String name)
	{
		return classNode.methods.stream().filter(method -> method.name.equals(name)).findFirst().orElse(null);
	}

	public static int getInvertedJump(final int opcode)
	{
		int i = -1;
		switch (opcode)
		{
			case Opcodes.IFEQ:
				i = Opcodes.IFNE;
				break;
			case Opcodes.IFNE:
				i = Opcodes.IFEQ;
				break;
			case Opcodes.IF_ACMPEQ:
				i = Opcodes.IF_ACMPNE;
				break;
			case Opcodes.IF_ACMPNE:
				i = Opcodes.IF_ACMPEQ;
				break;
		}
		return i;
	}

	public static boolean isMethodValid(final MethodNode method)
	{
		return !Modifier.isNative(method.access) && !Modifier.isAbstract(method.access) && method.instructions.size() != 0;
	}

	public static boolean isClassValid(final ClassNode node)
	{
		return (node.access & Opcodes.ACC_ENUM) == 0 && (node.access & Opcodes.ACC_INTERFACE) == 0;
	}

	public static AbstractInsnNode methodCall(final ClassNode classNode, final MethodNode methodNode)
	{
		int opcode = Opcodes.INVOKEVIRTUAL;
		if (Modifier.isInterface(classNode.access))
			opcode = Opcodes.INVOKEINTERFACE;
		if (Modifier.isStatic(methodNode.access))
			opcode = Opcodes.INVOKESTATIC;
		if (!methodNode.name.isEmpty() && methodNode.name.charAt(0) == '<')
			opcode = Opcodes.INVOKESPECIAL;
		return new MethodInsnNode(opcode, classNode.name, methodNode.name, methodNode.desc, false);
	}

	public static void insertOn(final InsnList instructions, final Predicate<AbstractInsnNode> predicate, final InsnList toAdd)
	{
		for (final AbstractInsnNode abstractInsnNode : instructions.toArray())
			if (predicate.test(abstractInsnNode))
				instructions.insertBefore(abstractInsnNode, toAdd);
	}

	public static InsnList nullPush()
	{
		final InsnList insns = new InsnList();
		insns.add(new InsnNode(Opcodes.ACONST_NULL));
		return insns;
	}

	public static InsnList notNullPush()
	{
		throw new RuntimeException("Not implemented");
		// InsnList insns = new InsnList();
		// insns.add(new LdcInsnNode(Math.random() * 100));
		// insns.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double",
		// "valueOf", "(D)Ljava/lang/Double;", false));
		// insns.add(new TypeInsnNode());
		// insns.add(new LdcInsnNode(Type.getType("Ljava/lang/System;")));
		// insns.add(new FieldInsnNode(""));
		// return insns;
	}

	public static InsnList debugString(final String s)
	{
		final InsnList insns = new InsnList();
		insns.add(new LdcInsnNode(s));
		insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/String", "length", "()I", false));
		insns.add(new InsnNode(Opcodes.POP));
		return insns;
	}

	// public static int getTypeLoad(Type argumentType) {
	// if (argumentType.getOpcode()) {
	//
	// }
	//
	// return NodeUtils.TYPE_TO_LOAD.get(argumentType);
	// }
	public static String getInternalName(final Type type)
	{
		switch (type.toString())
		{
			case "V":
				return "void";
			case "Z":
				return "boolean";
			case "C":
				return "char";
			case "B":
				return "byte";
			case "S":
				return "short";
			case "I":
				return "int";
			case "F":
				return "float";
			case "J":
				return "long";
			case "D":
				return "double";
			default:
				throw new IllegalArgumentException("Type not known.");
		}
	}

	private ASMUtils()
	{
	}
}
