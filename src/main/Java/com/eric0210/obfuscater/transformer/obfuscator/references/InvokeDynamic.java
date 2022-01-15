package com.eric0210.obfuscater.transformer.obfuscator.references;

import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.asm.ClassWrapper;
import com.eric0210.obfuscater.asm.FieldWrapper;
import com.eric0210.obfuscater.exceptions.MissingClassException;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.utils.RandomUtils;
import com.eric0210.obfuscater.utils.StringGenerator;
import com.eric0210.obfuscater.utils.StringGeneratorParameter;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

public class InvokeDynamic extends Transformer
{
	public static class Parameter
	{
		public final ObfuscationMode mode;
		public final StringGenerator classNameGenerator;
		public final StringGenerator methodNameGenerator;
		public final StringGenerator fieldNameGenerator;
		public final boolean injectDecryptor;

		public Parameter(final ObfuscationMode mode, final StringGenerator classNameGenerator, final StringGenerator methodNameGenerator, final StringGenerator fieldNameGenerator, final boolean injectDecryptor)
		{
			this.mode = mode;
			this.classNameGenerator = classNameGenerator;
			this.methodNameGenerator = methodNameGenerator;
			this.fieldNameGenerator = fieldNameGenerator;
			this.injectDecryptor = injectDecryptor;
		}
	}

	public enum ObfuscationMode
	{
		invokestatic_and_invokevirtual,
		invokestatic_and_invokevirtual_with_antijdeobf,
		all_static_accesses_and_invokes
	}

	protected final Parameter parameter;

	public InvokeDynamic(final Parameter param)
	{
		this.parameter = param;
	}

	@Override
	public final void transform()
	{
		final AtomicInteger affected_method_accesses = new AtomicInteger();
		final AtomicInteger affected_field_accesses = new AtomicInteger();

		final MemberNames memberNames = new MemberNames();

		final ClassWrapper injected = RandomUtils.getRandomClassCanInject(this, this.getClassWrappers(), cw -> cw.classNode.methods.parallelStream().filter(m -> "<init>".equals(m.name)).count() == 1);
		final boolean inject = this.parameter.injectDecryptor && injected != null;

		if (this.parameter.mode == ObfuscationMode.all_static_accesses_and_invokes)
		{
			final Handle bsmHandle = new Handle(H_INVOKESTATIC, (inject ? injected.originalName : memberNames.className), memberNames.bootstrapMethodName, "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);

			this.getClassWrappers().parallelStream().filter(classWrapper -> !"java/lang/Enum".equals(classWrapper.classNode.superName) && !this.isExcluded(classWrapper) && classWrapper.classNode.version >= V1_7).forEach(classWrapper ->
			{
				final ClassNode classNode = classWrapper.classNode;
				classWrapper.methods.parallelStream().filter(methodWrapper -> !this.isExcluded(methodWrapper) && this.hasInstructions(methodWrapper.methodNode)).map(methodWrapper -> methodWrapper.methodNode).forEach(methodNode ->
				{
					for (final AbstractInsnNode insn : methodNode.instructions.toArray())
						if (insn instanceof MethodInsnNode)
						{
							final MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
							if (!"<init>".equals(methodInsnNode.name))
							{
								final boolean isStatic = methodInsnNode.getOpcode() == INVOKESTATIC;
								String newSig = isStatic ? methodInsnNode.desc : methodInsnNode.desc.replace("(", "(Ljava/lang/Object;");
								final Type returnType = Type.getReturnType(methodInsnNode.desc);
								final Type[] args = Type.getArgumentTypes(newSig);
								for (int i = 0, j = args.length; i < j; i++)
								{
									final Type arg = args[i];
									if (arg.getSort() == Type.OBJECT)
										args[i] = Type.getType("Ljava/lang/Object;");
								}
								newSig = Type.getMethodDescriptor(returnType, args);
								final StringBuilder sb = new StringBuilder();
								sb.append(methodInsnNode.owner.replace("/", ".")).append("<>").append(methodInsnNode.name).append("<>");
								switch (insn.getOpcode())
								{
									case INVOKESTATIC:
										sb.append("0<>").append(methodInsnNode.desc);
										break;

									case INVOKEINTERFACE:
									case INVOKEVIRTUAL:
										sb.append("1<>").append(methodInsnNode.desc);
										break;

									case INVOKESPECIAL:
										sb.append("2<>").append(methodInsnNode.desc).append("<>").append(classNode.name.replace("/", "."));
										break;

									default:
										break;
								}
								final InvokeDynamicInsnNode indy = new InvokeDynamicInsnNode(encrypt(sb.toString(), memberNames, inject, injected), newSig, bsmHandle);
								methodNode.instructions.set(insn, indy);
								if (returnType.getSort() == Type.ARRAY)
									methodNode.instructions.insert(indy, new TypeInsnNode(CHECKCAST, returnType.getInternalName()));
								affected_method_accesses.incrementAndGet();
							}
						}
						else if (insn instanceof FieldInsnNode && !"<init>".equals(methodNode.name))
						{
							final FieldInsnNode fieldInsnNode = (FieldInsnNode) insn;
							final ClassWrapper cw = this.getClassPath().get(fieldInsnNode.owner);
							if (cw == null)
								throw new MissingClassException(fieldInsnNode.owner + " does not exist in classpath", new ClassNotFoundException(fieldInsnNode.owner));
							final FieldWrapper fw = cw.fields.stream().filter(fieldWrapper -> fieldWrapper.fieldNode.name.equals(fieldInsnNode.name) && fieldWrapper.fieldNode.desc.equals(fieldInsnNode.desc)).findFirst().orElse(null);
							if (fw != null && Modifier.isFinal(fw.fieldNode.access))
								continue;
							final boolean isStatic = fieldInsnNode.getOpcode() == GETSTATIC || fieldInsnNode.getOpcode() == PUTSTATIC;
							final boolean isSetter = fieldInsnNode.getOpcode() == PUTFIELD || fieldInsnNode.getOpcode() == PUTSTATIC;
							String newSig = isSetter ? '(' + fieldInsnNode.desc + ")V" : "()" + fieldInsnNode.desc;
							if (!isStatic)
								newSig = newSig.replace("(", "(Ljava/lang/Object;");
							final StringBuilder sb = new StringBuilder();
							sb.append(fieldInsnNode.owner.replace("/", ".")).append("<>").append(fieldInsnNode.name).append("<>");
							switch (insn.getOpcode())
							{
								case GETSTATIC:
									sb.append('3');
									break;

								case GETFIELD:
									sb.append('4');
									break;

								case PUTSTATIC:
									sb.append('5');
									break;

								case PUTFIELD:
									sb.append('6');
									break;
								default:
									break;
							}

							final InvokeDynamicInsnNode indy = new InvokeDynamicInsnNode(encrypt(sb.toString(), memberNames, inject, injected), newSig, bsmHandle);
							methodNode.instructions.set(insn, indy);
							affected_field_accesses.incrementAndGet();
						}
				});
			});
		}
		else
		{
			final Handle bsmHandle = new Handle(Opcodes.H_INVOKESTATIC, (inject ? injected.originalName : memberNames.className), memberNames.bootstrapMethodName, "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);

			final StringGenerator bsmNameGenerator = new StringGenerator().configure(new StringGeneratorParameter().setPattern(StringGeneratorParameter.StringGeneratorPresets.ALL.getPattern()).setLength(32, 32).setDuplicateGenerationEnabled(true));

			this.getClassWrappers().stream().filter(classWrapper -> !"java/lang/Enum".equals(classWrapper.classNode.superName) && !this.isExcluded(classWrapper) && classWrapper.classNode.version >= V1_7).forEach(classWrapper -> classWrapper.methods.stream().filter(methodWrapper -> !this.isExcluded(methodWrapper) && this.hasInstructions(methodWrapper.methodNode)).forEach(methodWrapper ->
			{
				final MethodNode methodNode = methodWrapper.methodNode;
				for (final AbstractInsnNode insn : methodNode.instructions.toArray())
					if (insn instanceof MethodInsnNode && insn.getOpcode() != INVOKESPECIAL)
					{
						final MethodInsnNode methodInsnNode = (MethodInsnNode) insn;
						final boolean isStatic = methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC;
						final String newSig = isStatic ? methodInsnNode.desc : methodInsnNode.desc.replace("(", "(Ljava/lang/Object;");
						final Type returnType = Type.getReturnType(methodInsnNode.desc);
						final int opcode = isStatic ? 0 : 1;
						final InvokeDynamicInsnNode indy = new InvokeDynamicInsnNode(bsmNameGenerator.generate(), newSig, bsmHandle, opcode, encrypt(methodInsnNode.owner.replace("/", "."), 1029), encrypt(methodInsnNode.name, 2038), encrypt(methodInsnNode.desc, 1928));
						methodNode.instructions.set(insn, indy);
						if (returnType.getSort() == Type.ARRAY)
							methodNode.instructions.insert(indy, new TypeInsnNode(CHECKCAST, returnType.getInternalName()));
						affected_method_accesses.incrementAndGet();
					}
			}));
		}
		final ClassNode decryptor = createBootstrap(this.parameter.mode, memberNames, inject, injected);

		if (!inject)
			this.getClasses().put(decryptor.name, new ClassWrapper(decryptor, false));

		if (this.parameter.mode == ObfuscationMode.all_static_accesses_and_invokes)
			Logger.stdOut(String.format("Hid %d method and %d field accesses with invokedynamics.", affected_method_accesses.get(), affected_field_accesses.get()));
		else
			Logger.stdOut(String.format("Hid %d method invocations with invokedynamics.", affected_method_accesses.get()));

		if (inject)
			Logger.stdOut(String.format(" - InvokeDynamic decryptor is injected on \"%s\".", injected.originalName.replaceAll("/", ".")));
		else
			Logger.stdOut(String.format(" - InvokeDynamic decryptor is generated on \"%s\".", (inject ? injected.originalName : memberNames.className).replaceAll("/", ".")));
	}

	@Override
	public String getName()
	{
		return "invokedynamic";
	}

	private static String encrypt(final String message, final int encryptionKey)
	{
		final char[] chars = message.toCharArray();
		final char[] encryptedChars = new char[chars.length];
		for (int i = 0, j = chars.length; i < j; i++)
			encryptedChars[i] = (char) (chars[i] ^ encryptionKey);
		return new String(encryptedChars);
	}

	private static String encrypt(final String message, final MemberNames memberNames, final boolean injectDecryptor, final ClassWrapper injectTo)
	{
		final char[] chars = message.toCharArray();
		final StringBuilder sb = new StringBuilder();
		for (int i = 0, j = chars.length; i < j; i++)
			switch (i % 4)
			{
				case 0:
				case 2:
				{
					sb.append((char) (chars[i] ^ (injectDecryptor ? injectTo.originalName : memberNames.className).replace("/", ".").hashCode()));
					break;
				}
				case 1:
				{
					sb.append((char) (chars[i] ^ memberNames.bootstrapMethodName.hashCode()));
					break;
				}
				case 3:
				{
					sb.append((char) (chars[i] ^ memberNames.decryptorMethodName.hashCode()));
					break;
				}
			}
		return sb.toString();
	}

	private static final ClassNode createBootstrap(final ObfuscationMode mode, final MemberNames memberNames, final boolean injectDecryptor, final ClassWrapper injectTo)
	{
		ClassNode bsmHost = new ClassNode();

		if (injectDecryptor)
			bsmHost = injectTo.classNode;
		else
			bsmHost.visit(V1_7, ACC_PUBLIC | ACC_FINAL | ACC_SUPER | ACC_SYNTHETIC, (injectDecryptor ? injectTo.originalName : memberNames.className), null, "java/lang/Object", null);

		MethodVisitor mv;

		switch (mode)
		{
			case invokestatic_and_invokevirtual:
			{
				final Optional<MethodNode> _init = injectTo.classNode.methods.parallelStream().filter(method -> "<init>".equalsIgnoreCase(method.name)).findFirst();
				if (!injectDecryptor || !_init.isPresent())
				{
					mv = bsmHost.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, "<init>", "()V", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
					mv.visitInsn(RETURN);
					final Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitMaxs(1, 1);
					mv.visitEnd();
				}

				{
					// Decryptor method

					mv = bsmHost.visitMethod(ACC_PUBLIC | ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.bootstrapMethodName, "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					final Label l1 = new Label();
					final Label l2 = new Label();
					mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");
					mv.visitLabel(l0);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
					mv.visitVarInsn(ASTORE, 7);
					final Label l3 = new Label();
					mv.visitLabel(l3);
					mv.visitVarInsn(ALOAD, 7);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitIntInsn(NEWARRAY, T_CHAR);
					mv.visitVarInsn(ASTORE, 8);
					final Label l4 = new Label();
					mv.visitLabel(l4);
					mv.visitInsn(ICONST_0);
					mv.visitVarInsn(ISTORE, 9);
					final Label l5 = new Label();
					mv.visitLabel(l5);
					final Label l6 = new Label();
					mv.visitJumpInsn(GOTO, l6);
					final Label l7 = new Label();
					mv.visitLabel(l7);
					mv.visitFrame(F_APPEND, 3, new Object[]
					{
							"[C", "[C", INTEGER
					}, 0, null);
					mv.visitVarInsn(ALOAD, 8);
					mv.visitVarInsn(ILOAD, 9);
					mv.visitVarInsn(ALOAD, 7);
					mv.visitVarInsn(ILOAD, 9);
					mv.visitInsn(CALOAD);
					mv.visitIntInsn(SIPUSH, 1029);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitInsn(CASTORE);
					final Label l8 = new Label();
					mv.visitLabel(l8);
					mv.visitIincInsn(9, 1);
					mv.visitLabel(l6);
					mv.visitFrame(F_SAME, 0, null, 0, null);
					mv.visitVarInsn(ILOAD, 9);
					mv.visitVarInsn(ALOAD, 7);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitJumpInsn(IF_ICMPLT, l7);
					final Label l9 = new Label();
					mv.visitLabel(l9);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
					mv.visitVarInsn(ASTORE, 9);
					final Label l10 = new Label();
					mv.visitLabel(l10);
					mv.visitVarInsn(ALOAD, 9);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitIntInsn(NEWARRAY, T_CHAR);
					mv.visitVarInsn(ASTORE, 10);
					final Label l11 = new Label();
					mv.visitLabel(l11);
					mv.visitInsn(ICONST_0);
					mv.visitVarInsn(ISTORE, 11);
					final Label l12 = new Label();
					mv.visitLabel(l12);
					final Label l13 = new Label();
					mv.visitJumpInsn(GOTO, l13);
					final Label l14 = new Label();
					mv.visitLabel(l14);
					mv.visitFrame(F_FULL, 12, new Object[]
					{
							"java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "[C", "[C", "[C", "[C", INTEGER
					}, 0, new Object[]
					{});
					mv.visitVarInsn(ALOAD, 10);
					mv.visitVarInsn(ILOAD, 11);
					mv.visitVarInsn(ALOAD, 9);
					mv.visitVarInsn(ILOAD, 11);
					mv.visitInsn(CALOAD);
					mv.visitIntInsn(SIPUSH, 2038);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitInsn(CASTORE);
					final Label l15 = new Label();
					mv.visitLabel(l15);
					mv.visitIincInsn(11, 1);
					mv.visitLabel(l13);
					mv.visitFrame(F_SAME, 0, null, 0, null);
					mv.visitVarInsn(ILOAD, 11);
					mv.visitVarInsn(ALOAD, 9);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitJumpInsn(IF_ICMPLT, l14);
					final Label l16 = new Label();
					mv.visitLabel(l16);
					mv.visitVarInsn(ALOAD, 6);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
					mv.visitVarInsn(ASTORE, 11);
					final Label l17 = new Label();
					mv.visitLabel(l17);
					mv.visitVarInsn(ALOAD, 11);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitIntInsn(NEWARRAY, T_CHAR);
					mv.visitVarInsn(ASTORE, 12);
					final Label l18 = new Label();
					mv.visitLabel(l18);
					mv.visitInsn(ICONST_0);
					mv.visitVarInsn(ISTORE, 13);
					final Label l19 = new Label();
					mv.visitLabel(l19);
					final Label l20 = new Label();
					mv.visitJumpInsn(GOTO, l20);
					final Label l21 = new Label();
					mv.visitLabel(l21);
					mv.visitFrame(F_FULL, 14, new Object[]
					{
							"java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "[C", "[C", "[C", "[C", "[C", "[C", INTEGER
					}, 0, new Object[]
					{});
					mv.visitVarInsn(ALOAD, 12);
					mv.visitVarInsn(ILOAD, 13);
					mv.visitVarInsn(ALOAD, 11);
					mv.visitVarInsn(ILOAD, 13);
					mv.visitInsn(CALOAD);
					mv.visitIntInsn(SIPUSH, 1928);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitInsn(CASTORE);
					final Label l22 = new Label();
					mv.visitLabel(l22);
					mv.visitIincInsn(13, 1);
					mv.visitLabel(l20);
					mv.visitFrame(F_SAME, 0, null, 0, null);
					mv.visitVarInsn(ILOAD, 13);
					mv.visitVarInsn(ALOAD, 11);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitJumpInsn(IF_ICMPLT, l21);
					final Label l23 = new Label();
					mv.visitLabel(l23);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
					mv.visitVarInsn(ISTORE, 14);
					final Label l24 = new Label();
					mv.visitLabel(l24);
					mv.visitVarInsn(ILOAD, 14);
					final Label l25 = new Label();
					final Label l26 = new Label();
					final Label l27 = new Label();
					mv.visitTableSwitchInsn(0, 1, l27, l25, l26);
					mv.visitLabel(l25);
					mv.visitFrame(F_FULL, 15, new Object[]
					{
							"java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "[C", "[C", "[C", "[C", "[C", "[C", TOP, INTEGER
					}, 0, new Object[]
					{});
					mv.visitVarInsn(ALOAD, 0);
					mv.visitTypeInsn(CHECKCAST, "java/lang/invoke/MethodHandles$Lookup");
					mv.visitTypeInsn(NEW, "java/lang/String");
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 8);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false);
					mv.visitTypeInsn(NEW, "java/lang/String");
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 10);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
					mv.visitTypeInsn(NEW, "java/lang/String");
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 12);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
					mv.visitLdcInsn(Type.getType('L' + (injectDecryptor ? injectTo.originalName : memberNames.className) + ';'));
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findStatic", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false);
					mv.visitVarInsn(ASTORE, 13);
					final Label l28 = new Label();
					mv.visitLabel(l28);
					final Label l29 = new Label();
					mv.visitJumpInsn(GOTO, l29);
					mv.visitLabel(l26);
					mv.visitFrame(F_SAME, 0, null, 0, null);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitTypeInsn(CHECKCAST, "java/lang/invoke/MethodHandles$Lookup");
					mv.visitTypeInsn(NEW, "java/lang/String");
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 8);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false);
					mv.visitTypeInsn(NEW, "java/lang/String");
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 10);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
					mv.visitTypeInsn(NEW, "java/lang/String");
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 12);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
					mv.visitLdcInsn(Type.getType('L' + (injectDecryptor ? injectTo.originalName : memberNames.className) + ';'));
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findVirtual", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false);
					mv.visitVarInsn(ASTORE, 13);
					final Label l30 = new Label();
					mv.visitLabel(l30);
					mv.visitJumpInsn(GOTO, l29);
					mv.visitLabel(l27);
					mv.visitFrame(F_SAME, 0, null, 0, null);
					mv.visitTypeInsn(NEW, "java/lang/BootstrapMethodError");
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/BootstrapMethodError", "<init>", "()V", false);
					mv.visitInsn(ATHROW);
					mv.visitLabel(l29);
					mv.visitFrame(F_FULL, 15, new Object[]
					{
							"java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "[C", "[C", "[C", "[C", "[C", "[C", "java/lang/invoke/MethodHandle", INTEGER
					}, 0, new Object[]
					{});
					mv.visitVarInsn(ALOAD, 13);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitTypeInsn(CHECKCAST, "java/lang/invoke/MethodType");
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "asType", "(Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false);
					mv.visitVarInsn(ASTORE, 13);
					final Label l31 = new Label();
					mv.visitLabel(l31);
					mv.visitTypeInsn(NEW, "java/lang/invoke/ConstantCallSite");
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 13);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/invoke/ConstantCallSite", "<init>", "(Ljava/lang/invoke/MethodHandle;)V", false);
					mv.visitLabel(l1);
					mv.visitInsn(ARETURN);
					mv.visitLabel(l2);
					mv.visitFrame(F_FULL, 7, new Object[]
					{
							"java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object"
					}, 1, new Object[]
					{
							"java/lang/Exception"
					});
					mv.visitVarInsn(ASTORE, 7);
					final Label l32 = new Label();
					mv.visitLabel(l32);
					mv.visitTypeInsn(NEW, "java/lang/BootstrapMethodError");
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/BootstrapMethodError", "<init>", "()V", false);
					mv.visitInsn(ATHROW);
					final Label l33 = new Label();
					mv.visitLabel(l33);
					mv.visitMaxs(6, 15);
					mv.visitEnd();
				}
				break;
			}
			case invokestatic_and_invokevirtual_with_antijdeobf:
			{
				final Optional<MethodNode> _init = injectTo.classNode.methods.parallelStream().filter(method -> "<init>".equalsIgnoreCase(method.name)).findFirst();
				if (!injectDecryptor || !_init.isPresent())
				{
					mv = bsmHost.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, "<init>", "()V", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
					mv.visitInsn(RETURN);
					final Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitMaxs(1, 1);
					mv.visitEnd();
				}

				{
					// Decryptor method

					mv = bsmHost.visitMethod(ACC_PUBLIC | ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.bootstrapMethodName, "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					final Label l1 = new Label();
					final Label l2 = new Label();
					mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
					final Label l3 = new Label();
					final Label l4 = new Label();
					final Label l5 = new Label();
					mv.visitTryCatchBlock(l3, l4, l5, "java/lang/Exception");
					mv.visitLabel(l3);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
					mv.visitVarInsn(ASTORE, 7);
					final Label l6 = new Label();
					mv.visitLabel(l6);
					mv.visitVarInsn(ALOAD, 7);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitIntInsn(NEWARRAY, T_CHAR);
					mv.visitVarInsn(ASTORE, 8);
					final Label l7 = new Label();
					mv.visitLabel(l7);
					mv.visitInsn(ICONST_0);
					mv.visitVarInsn(ISTORE, 9);
					final Label l8 = new Label();
					mv.visitLabel(l8);
					final Label l9 = new Label();
					mv.visitJumpInsn(GOTO, l9);
					final Label l10 = new Label();
					mv.visitLabel(l10);
					mv.visitFrame(F_APPEND, 3, new Object[]
					{
							"[C", "[C", INTEGER
					}, 0, null);
					mv.visitVarInsn(ALOAD, 8);
					mv.visitVarInsn(ILOAD, 9);
					mv.visitVarInsn(ALOAD, 7);
					mv.visitVarInsn(ILOAD, 9);
					mv.visitInsn(CALOAD);
					mv.visitIntInsn(SIPUSH, 2893);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitInsn(CASTORE);
					final Label l11 = new Label();
					mv.visitLabel(l11);
					mv.visitIincInsn(9, 1);
					mv.visitLabel(l9);
					mv.visitFrame(F_SAME, 0, null, 0, null);
					mv.visitVarInsn(ILOAD, 9);
					mv.visitVarInsn(ALOAD, 7);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitJumpInsn(IF_ICMPLT, l10);
					final Label l12 = new Label();
					mv.visitLabel(l12);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
					mv.visitVarInsn(ASTORE, 9);
					final Label l13 = new Label();
					mv.visitLabel(l13);
					mv.visitVarInsn(ALOAD, 9);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitIntInsn(NEWARRAY, T_CHAR);
					mv.visitVarInsn(ASTORE, 10);
					final Label l14 = new Label();
					mv.visitLabel(l14);
					mv.visitInsn(ICONST_0);
					mv.visitVarInsn(ISTORE, 11);
					final Label l15 = new Label();
					mv.visitLabel(l15);
					final Label l16 = new Label();
					mv.visitJumpInsn(GOTO, l16);
					final Label l17 = new Label();
					mv.visitLabel(l17);
					mv.visitFrame(F_FULL, 12, new Object[]
					{
							"java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "[C", "[C", "[C", "[C", INTEGER
					}, 0, new Object[]
					{});
					mv.visitVarInsn(ALOAD, 10);
					mv.visitVarInsn(ILOAD, 11);
					mv.visitVarInsn(ALOAD, 9);
					mv.visitVarInsn(ILOAD, 11);
					mv.visitInsn(CALOAD);
					mv.visitIntInsn(SIPUSH, 2993);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitInsn(CASTORE);
					final Label l18 = new Label();
					mv.visitLabel(l18);
					mv.visitIincInsn(11, 1);
					mv.visitLabel(l16);
					mv.visitFrame(F_SAME, 0, null, 0, null);
					mv.visitVarInsn(ILOAD, 11);
					mv.visitVarInsn(ALOAD, 9);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitJumpInsn(IF_ICMPLT, l17);
					final Label l19 = new Label();
					mv.visitLabel(l19);
					mv.visitVarInsn(ALOAD, 6);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
					mv.visitVarInsn(ASTORE, 11);
					final Label l20 = new Label();
					mv.visitLabel(l20);
					mv.visitVarInsn(ALOAD, 11);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitIntInsn(NEWARRAY, T_CHAR);
					mv.visitVarInsn(ASTORE, 12);
					final Label l21 = new Label();
					mv.visitLabel(l21);
					mv.visitInsn(ICONST_0);
					mv.visitVarInsn(ISTORE, 13);
					final Label l22 = new Label();
					mv.visitLabel(l22);
					final Label l23 = new Label();
					mv.visitJumpInsn(GOTO, l23);
					final Label l24 = new Label();
					mv.visitLabel(l24);
					mv.visitFrame(F_FULL, 14, new Object[]
					{
							"java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "[C", "[C", "[C", "[C", "[C", "[C", INTEGER
					}, 0, new Object[]
					{});
					mv.visitVarInsn(ALOAD, 12);
					mv.visitVarInsn(ILOAD, 13);
					mv.visitVarInsn(ALOAD, 11);
					mv.visitVarInsn(ILOAD, 13);
					mv.visitInsn(CALOAD);
					mv.visitIntInsn(SIPUSH, 8372);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitInsn(CASTORE);
					final Label l25 = new Label();
					mv.visitLabel(l25);
					mv.visitIincInsn(13, 1);
					mv.visitLabel(l23);
					mv.visitFrame(F_SAME, 0, null, 0, null);
					mv.visitVarInsn(ILOAD, 13);
					mv.visitVarInsn(ALOAD, 11);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitJumpInsn(IF_ICMPLT, l24);
					final Label l26 = new Label();
					mv.visitLabel(l26);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
					mv.visitVarInsn(ISTORE, 14);
					final Label l27 = new Label();
					mv.visitLabel(l27);
					mv.visitVarInsn(ILOAD, 14);
					mv.visitIntInsn(SIPUSH, 256);
					mv.visitInsn(ISHL);
					mv.visitIntInsn(SIPUSH, 255);
					mv.visitInsn(IAND);
					mv.visitVarInsn(ISTORE, 14);
					final Label l28 = new Label();
					mv.visitLabel(l28);
					mv.visitVarInsn(ILOAD, 14);
					final Label l29 = new Label();
					final Label l30 = new Label();
					final Label l31 = new Label();
					mv.visitTableSwitchInsn(0, 1, l31, l29, l30);
					mv.visitLabel(l29);
					mv.visitFrame(F_FULL, 15, new Object[]
					{
							"java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "[C", "[C", "[C", "[C", "[C", "[C", TOP, INTEGER
					}, 0, new Object[]
					{});
					mv.visitVarInsn(ALOAD, 0);
					mv.visitTypeInsn(CHECKCAST, "java/lang/invoke/MethodHandles$Lookup");
					mv.visitTypeInsn(NEW, "java/lang/String");
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 8);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false);
					mv.visitTypeInsn(NEW, "java/lang/String");
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 10);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
					mv.visitTypeInsn(NEW, "java/lang/String");
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 12);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
					mv.visitLdcInsn(Type.getType('L' + (injectDecryptor ? injectTo.originalName : memberNames.className) + ';'));
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findStatic", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false);
					mv.visitVarInsn(ASTORE, 13);
					final Label l32 = new Label();
					mv.visitLabel(l32);
					final Label l33 = new Label();
					mv.visitJumpInsn(GOTO, l33);
					mv.visitLabel(l30);
					mv.visitFrame(F_SAME, 0, null, 0, null);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitTypeInsn(CHECKCAST, "java/lang/invoke/MethodHandles$Lookup");
					mv.visitTypeInsn(NEW, "java/lang/String");
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 8);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false);
					mv.visitTypeInsn(NEW, "java/lang/String");
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 10);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
					mv.visitTypeInsn(NEW, "java/lang/String");
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 12);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/String", "<init>", "([C)V", false);
					mv.visitLdcInsn(Type.getType('L' + (injectDecryptor ? injectTo.originalName : memberNames.className) + ';'));
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findVirtual", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false);
					mv.visitVarInsn(ASTORE, 13);
					final Label l34 = new Label();
					mv.visitLabel(l34);
					mv.visitJumpInsn(GOTO, l33);
					mv.visitLabel(l31);
					mv.visitFrame(F_SAME, 0, null, 0, null);
					mv.visitTypeInsn(NEW, "java/lang/BootstrapMethodError");
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/BootstrapMethodError", "<init>", "()V", false);
					mv.visitInsn(ATHROW);
					mv.visitLabel(l33);
					mv.visitFrame(F_FULL, 15, new Object[]
					{
							"java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "[C", "[C", "[C", "[C", "[C", "[C", "java/lang/invoke/MethodHandle", INTEGER
					}, 0, new Object[]
					{});
					mv.visitVarInsn(ALOAD, 13);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitTypeInsn(CHECKCAST, "java/lang/invoke/MethodType");
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "asType", "(Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false);
					mv.visitVarInsn(ASTORE, 13);
					mv.visitLabel(l0);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Runtime", "getRuntime", "()Ljava/lang/Runtime;", false);
					mv.visitMethodInsn(INVOKESTATIC, "java/util/concurrent/ThreadLocalRandom", "current", "()Ljava/util/concurrent/ThreadLocalRandom;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/concurrent/ThreadLocalRandom", "nextInt", "()I", false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(I)Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Runtime", "exec", "(Ljava/lang/String;)Ljava/lang/Process;", false);
					mv.visitInsn(POP);
					mv.visitLabel(l1);
					final Label l35 = new Label();
					mv.visitJumpInsn(GOTO, l35);
					mv.visitLabel(l2);
					mv.visitFrame(F_SAME1, 0, null, 1, new Object[]
					{
							"java/lang/Throwable"
					});
					mv.visitVarInsn(ASTORE, 15);
					mv.visitLabel(l35);
					mv.visitFrame(F_SAME, 0, null, 0, null);
					mv.visitTypeInsn(NEW, "java/lang/invoke/ConstantCallSite");
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 13);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/invoke/ConstantCallSite", "<init>", "(Ljava/lang/invoke/MethodHandle;)V", false);
					mv.visitLabel(l4);
					mv.visitInsn(ARETURN);
					mv.visitLabel(l5);
					mv.visitFrame(F_FULL, 7, new Object[]
					{
							"java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object", "java/lang/Object"
					}, 1, new Object[]
					{
							"java/lang/Exception"
					});
					mv.visitVarInsn(ASTORE, 7);
					final Label l36 = new Label();
					mv.visitLabel(l36);
					mv.visitVarInsn(ALOAD, 7);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Exception", "printStackTrace", "()V", false);
					final Label l37 = new Label();
					mv.visitLabel(l37);
					mv.visitTypeInsn(NEW, "java/lang/BootstrapMethodError");
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/BootstrapMethodError", "<init>", "()V", false);
					mv.visitInsn(ATHROW);
					final Label l38 = new Label();
					mv.visitLabel(l38);
					mv.visitMaxs(6, 16);
					mv.visitEnd();
				}
				break;
			}

			case all_static_accesses_and_invokes:
			{
				bsmHost.visitInnerClass("java/lang/invoke/MethodHandles$Lookup", "java/lang/invoke/MethodHandles", "Lookup", ACC_PUBLIC | ACC_STATIC | ACC_FINAL | ACC_SYNTHETIC);

				final Optional<MethodNode> _init = injectTo.classNode.methods.parallelStream().filter(method -> "<init>".equalsIgnoreCase(method.name)).findFirst();
				if (!injectDecryptor || !_init.isPresent())
				{
					mv = bsmHost.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, "<init>", "()V", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
					mv.visitInsn(RETURN);
					final Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitMaxs(1, 1);
					mv.visitEnd();
				}

				{
					// Decryptor method

					mv = bsmHost.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.decryptorMethodName, "(Ljava/lang/String;)Ljava/lang/String;", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "getStackTrace", "()[Ljava/lang/StackTraceElement;", false);
					mv.visitVarInsn(ASTORE, 1);
					final Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
					mv.visitVarInsn(ASTORE, 2);
					final Label l2 = new Label();
					mv.visitLabel(l2);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
					mv.visitVarInsn(ASTORE, 3);
					final Label l3 = new Label();
					mv.visitLabel(l3);
					mv.visitInsn(ICONST_0);
					mv.visitVarInsn(ISTORE, 4);
					final Label l4 = new Label();
					mv.visitLabel(l4);
					final Label l5 = new Label();
					mv.visitJumpInsn(GOTO, l5);
					final Label l6 = new Label();
					mv.visitLabel(l6);
					mv.visitVarInsn(ILOAD, 4);
					mv.visitInsn(ICONST_4);
					mv.visitInsn(IREM);
					final Label l7 = new Label();
					final Label l8 = new Label();
					final Label l9 = new Label();
					final Label l10 = new Label();
					final Label l11 = new Label();
					mv.visitTableSwitchInsn(0, 3, l11, l7, l8, l9, l10);
					mv.visitLabel(l7);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitVarInsn(ILOAD, 4);
					mv.visitInsn(CALOAD);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitInsn(ICONST_2);
					mv.visitInsn(AALOAD);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getClassName", "()Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
					mv.visitInsn(POP);
					final Label l12 = new Label();
					mv.visitLabel(l12);
					mv.visitJumpInsn(GOTO, l11);
					mv.visitLabel(l8);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitVarInsn(ILOAD, 4);
					mv.visitInsn(CALOAD);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitInsn(ICONST_2);
					mv.visitInsn(AALOAD);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName", "()Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
					mv.visitInsn(POP);
					final Label l13 = new Label();
					mv.visitLabel(l13);
					mv.visitJumpInsn(GOTO, l11);
					mv.visitLabel(l9);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitVarInsn(ILOAD, 4);
					mv.visitInsn(CALOAD);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitInsn(ICONST_1);
					mv.visitInsn(AALOAD);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getClassName", "()Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
					mv.visitInsn(POP);
					final Label l14 = new Label();
					mv.visitLabel(l14);
					mv.visitJumpInsn(GOTO, l11);
					mv.visitLabel(l10);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitVarInsn(ILOAD, 4);
					mv.visitInsn(CALOAD);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitInsn(ICONST_1);
					mv.visitInsn(AALOAD);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName", "()Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
					mv.visitInsn(POP);
					mv.visitLabel(l11);
					mv.visitIincInsn(4, 1);
					mv.visitLabel(l5);
					mv.visitVarInsn(ILOAD, 4);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitJumpInsn(IF_ICMPLT, l6);
					final Label l15 = new Label();
					mv.visitLabel(l15);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
					mv.visitInsn(ARETURN);
					final Label l16 = new Label();
					mv.visitLabel(l16);
					mv.visitMaxs(4, 5);
					mv.visitEnd();
				}

				{
					// Bootstrap method

					mv = bsmHost.visitMethod(ACC_PUBLIC | ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.bootstrapMethodName, "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					final Label l1 = new Label();
					final Label l2 = new Label();
					mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
					mv.visitLabel(l0);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitTypeInsn(CHECKCAST, "java/lang/String");
					mv.visitMethodInsn(INVOKESTATIC, (injectDecryptor ? injectTo.originalName : memberNames.className), memberNames.decryptorMethodName, "(Ljava/lang/String;)Ljava/lang/String;", false);
					mv.visitLdcInsn("<>");
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "split", "(Ljava/lang/String;)[Ljava/lang/String;", false);
					mv.visitVarInsn(ASTORE, 3);
					final Label l3 = new Label();
					mv.visitLabel(l3);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitInsn(ICONST_0);
					mv.visitInsn(AALOAD);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false);
					mv.visitVarInsn(ASTORE, 4);
					final Label l4 = new Label();
					mv.visitLabel(l4);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitInsn(ICONST_1);
					mv.visitInsn(AALOAD);
					mv.visitVarInsn(ASTORE, 5);
					final Label l5 = new Label();
					mv.visitLabel(l5);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitInsn(ICONST_2);
					mv.visitInsn(AALOAD);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "parseInt", "(Ljava/lang/String;)I", false);
					mv.visitVarInsn(ISTORE, 6);
					final Label l6 = new Label();
					mv.visitLabel(l6);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitTypeInsn(CHECKCAST, "java/lang/invoke/MethodHandles$Lookup");
					mv.visitVarInsn(ASTORE, 7);
					final Label l7 = new Label();
					mv.visitLabel(l7);
					mv.visitInsn(ACONST_NULL);
					mv.visitVarInsn(ASTORE, 8);
					final Label l8 = new Label();
					mv.visitLabel(l8);
					mv.visitVarInsn(ILOAD, 6);
					final Label l9 = new Label();
					final Label l10 = new Label();
					final Label l11 = new Label();
					final Label l12 = new Label();
					final Label l13 = new Label();
					final Label l14 = new Label();
					final Label l15 = new Label();
					final Label l16 = new Label();
					mv.visitTableSwitchInsn(0, 6, l16, l9, l10, l11, l12, l13, l14, l15);
					mv.visitLabel(l9);
					mv.visitVarInsn(ALOAD, 7);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitInsn(ICONST_3);
					mv.visitInsn(AALOAD);
					mv.visitLdcInsn(Type.getType('L' + (injectDecryptor ? injectTo.originalName : memberNames.className) + ';'));
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findStatic", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false);
					mv.visitVarInsn(ASTORE, 8);
					final Label l17 = new Label();
					mv.visitLabel(l17);
					mv.visitJumpInsn(GOTO, l16);
					mv.visitLabel(l10);
					mv.visitVarInsn(ALOAD, 7);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitInsn(ICONST_3);
					mv.visitInsn(AALOAD);
					mv.visitLdcInsn(Type.getType('L' + (injectDecryptor ? injectTo.originalName : memberNames.className) + ';'));
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findVirtual", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false);
					mv.visitVarInsn(ASTORE, 8);
					final Label l18 = new Label();
					mv.visitLabel(l18);
					mv.visitJumpInsn(GOTO, l16);
					mv.visitLabel(l11);
					mv.visitVarInsn(ALOAD, 7);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitInsn(ICONST_3);
					mv.visitInsn(AALOAD);
					mv.visitLdcInsn(Type.getType('L' + (injectDecryptor ? injectTo.originalName : memberNames.className) + ';'));
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getClassLoader", "()Ljava/lang/ClassLoader;", false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/invoke/MethodType", "fromMethodDescriptorString", "(Ljava/lang/String;Ljava/lang/ClassLoader;)Ljava/lang/invoke/MethodType;", false);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitInsn(ICONST_4);
					mv.visitInsn(AALOAD);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findSpecial", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;", false);
					mv.visitVarInsn(ASTORE, 8);
					final Label l19 = new Label();
					mv.visitLabel(l19);
					mv.visitJumpInsn(GOTO, l16);
					mv.visitLabel(l12);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitMethodInsn(INVOKESTATIC, (injectDecryptor ? injectTo.originalName : memberNames.className), memberNames.searchMethodName, "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
					mv.visitVarInsn(ASTORE, 9);
					final Label l20 = new Label();
					mv.visitLabel(l20);
					mv.visitVarInsn(ALOAD, 9);
					mv.visitJumpInsn(IFNULL, l16);
					final Label l21 = new Label();
					mv.visitLabel(l21);
					mv.visitVarInsn(ALOAD, 7);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitVarInsn(ALOAD, 9);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "getType", "()Ljava/lang/Class;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findStaticGetter", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;", false);
					mv.visitVarInsn(ASTORE, 8);
					final Label l22 = new Label();
					mv.visitLabel(l22);
					mv.visitJumpInsn(GOTO, l16);
					mv.visitLabel(l13);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitMethodInsn(INVOKESTATIC, (injectDecryptor ? injectTo.originalName : memberNames.className), memberNames.searchMethodName, "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
					mv.visitVarInsn(ASTORE, 9);
					final Label l23 = new Label();
					mv.visitLabel(l23);
					mv.visitVarInsn(ALOAD, 9);
					mv.visitJumpInsn(IFNULL, l16);
					final Label l24 = new Label();
					mv.visitLabel(l24);
					mv.visitVarInsn(ALOAD, 7);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitVarInsn(ALOAD, 9);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "getType", "()Ljava/lang/Class;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findGetter", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;", false);
					mv.visitVarInsn(ASTORE, 8);
					final Label l25 = new Label();
					mv.visitLabel(l25);
					mv.visitJumpInsn(GOTO, l16);
					mv.visitLabel(l14);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitMethodInsn(INVOKESTATIC, (injectDecryptor ? injectTo.originalName : memberNames.className), memberNames.searchMethodName, "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
					mv.visitVarInsn(ASTORE, 9);
					final Label l26 = new Label();
					mv.visitLabel(l26);
					mv.visitVarInsn(ALOAD, 9);
					mv.visitJumpInsn(IFNULL, l16);
					final Label l27 = new Label();
					mv.visitLabel(l27);
					mv.visitVarInsn(ALOAD, 7);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitVarInsn(ALOAD, 9);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "getType", "()Ljava/lang/Class;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findStaticSetter", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;", false);
					mv.visitVarInsn(ASTORE, 8);
					final Label l28 = new Label();
					mv.visitLabel(l28);
					mv.visitJumpInsn(GOTO, l16);
					mv.visitLabel(l15);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitMethodInsn(INVOKESTATIC, (injectDecryptor ? injectTo.originalName : memberNames.className), memberNames.searchMethodName, "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
					mv.visitVarInsn(ASTORE, 9);
					final Label l29 = new Label();
					mv.visitLabel(l29);
					mv.visitVarInsn(ALOAD, 9);
					mv.visitJumpInsn(IFNULL, l16);
					final Label l30 = new Label();
					mv.visitLabel(l30);
					mv.visitVarInsn(ALOAD, 7);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitVarInsn(ALOAD, 9);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "getType", "()Ljava/lang/Class;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandles$Lookup", "findSetter", "(Ljava/lang/Class;Ljava/lang/String;Ljava/lang/Class;)Ljava/lang/invoke/MethodHandle;", false);
					mv.visitVarInsn(ASTORE, 8);
					mv.visitLabel(l16);
					mv.visitTypeInsn(NEW, "java/lang/invoke/ConstantCallSite");
					mv.visitInsn(DUP);
					mv.visitVarInsn(ALOAD, 8);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitTypeInsn(CHECKCAST, "java/lang/invoke/MethodType");
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/invoke/MethodHandle", "asType", "(Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/MethodHandle;", false);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/invoke/ConstantCallSite", "<init>", "(Ljava/lang/invoke/MethodHandle;)V", false);
					mv.visitLabel(l1);
					mv.visitInsn(ARETURN);
					mv.visitLabel(l2);
					mv.visitVarInsn(ASTORE, 3);
					final Label l31 = new Label();
					mv.visitLabel(l31);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Throwable", "printStackTrace", "()V", false);
					final Label l32 = new Label();
					mv.visitLabel(l32);
					mv.visitInsn(ACONST_NULL);
					mv.visitInsn(ARETURN);
					final Label l33 = new Label();
					mv.visitLabel(l33);
					mv.visitMaxs(6, 10);
					mv.visitEnd();
				}

				{
					// Search method

					mv = bsmHost.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.searchMethodName, "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", "(Ljava/lang/Class<*>;Ljava/lang/String;)Ljava/lang/reflect/Field;", null);
					mv.visitCode();
					final Label l0 = new Label();
					final Label l1 = new Label();
					final Label l2 = new Label();
					mv.visitTryCatchBlock(l0, l1, l2, "java/lang/NoSuchFieldException");
					final Label l3 = new Label();
					final Label l4 = new Label();
					final Label l5 = new Label();
					mv.visitTryCatchBlock(l3, l4, l5, "java/lang/NoSuchFieldException");
					mv.visitLabel(l0);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
					mv.visitVarInsn(ASTORE, 2);
					final Label l6 = new Label();
					mv.visitLabel(l6);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitInsn(ICONST_1);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "setAccessible", "(Z)V", false);
					final Label l7 = new Label();
					mv.visitLabel(l7);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitLabel(l1);
					mv.visitInsn(ARETURN);
					mv.visitLabel(l2);
					mv.visitVarInsn(ASTORE, 2);
					mv.visitLabel(l3);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSuperclass", "()Ljava/lang/Class;", false);
					mv.visitVarInsn(ASTORE, 3);
					final Label l8 = new Label();
					mv.visitLabel(l8);
					mv.visitVarInsn(ALOAD, 3);
					final Label l9 = new Label();
					mv.visitJumpInsn(IFNONNULL, l9);
					final Label l10 = new Label();
					mv.visitLabel(l10);
					mv.visitTypeInsn(NEW, "java/lang/NoSuchFieldException");
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/NoSuchFieldException", "<init>", "()V", false);
					mv.visitInsn(ATHROW);
					mv.visitLabel(l9);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitMethodInsn(INVOKESTATIC, (injectDecryptor ? injectTo.originalName : memberNames.className), memberNames.searchMethodName, "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
					mv.visitInsn(DUP);
					mv.visitVarInsn(ASTORE, 4);
					final Label l11 = new Label();
					mv.visitLabel(l11);
					final Label l12 = new Label();
					mv.visitJumpInsn(IFNULL, l12);
					final Label l13 = new Label();
					mv.visitLabel(l13);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitLabel(l4);
					mv.visitInsn(ARETURN);
					mv.visitLabel(l5);
					mv.visitVarInsn(ASTORE, 3);
					final Label l14 = new Label();
					mv.visitLabel(l14);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getInterfaces", "()[Ljava/lang/Class;", false);
					mv.visitVarInsn(ASTORE, 4);
					final Label l15 = new Label();
					mv.visitLabel(l15);
					mv.visitVarInsn(ALOAD, 4);
					final Label l16 = new Label();
					mv.visitJumpInsn(IFNONNULL, l16);
					final Label l17 = new Label();
					mv.visitLabel(l17);
					mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "()V", false);
					mv.visitInsn(ATHROW);
					mv.visitLabel(l16);
					mv.visitInsn(ICONST_0);
					mv.visitVarInsn(ISTORE, 5);
					final Label l18 = new Label();
					mv.visitLabel(l18);
					final Label l19 = new Label();
					mv.visitJumpInsn(GOTO, l19);
					final Label l20 = new Label();
					mv.visitLabel(l20);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitVarInsn(ILOAD, 5);
					mv.visitInsn(AALOAD);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitMethodInsn(INVOKESTATIC, (injectDecryptor ? injectTo.originalName : memberNames.className), memberNames.searchMethodName, "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
					mv.visitInsn(DUP);
					mv.visitVarInsn(ASTORE, 6);
					final Label l21 = new Label();
					mv.visitLabel(l21);
					final Label l22 = new Label();
					mv.visitJumpInsn(IFNULL, l22);
					final Label l23 = new Label();
					mv.visitLabel(l23);
					mv.visitVarInsn(ALOAD, 6);
					mv.visitInsn(ARETURN);
					mv.visitLabel(l22);
					mv.visitIincInsn(5, 1);
					mv.visitLabel(l19);
					mv.visitVarInsn(ILOAD, 5);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitJumpInsn(IF_ICMPLT, l20);
					mv.visitLabel(l12);
					mv.visitInsn(ACONST_NULL);
					mv.visitInsn(ARETURN);
					final Label l24 = new Label();
					mv.visitLabel(l24);
					mv.visitMaxs(2, 7);
					mv.visitEnd();
				}
				break;
			}
		}
		bsmHost.visitEnd();
		return bsmHost;
	}

	private final class MemberNames
	{
		final String className;
		final String decryptorMethodName;
		final String bootstrapMethodName;
		final String searchMethodName;

		MemberNames()
		{
			this.className = InvokeDynamic.this.randomClassPath(InvokeDynamic.this.getClasses().keySet()) + (InvokeDynamic.this.isRenamerEnabled() ? InvokeDynamic.this.parameter.classNameGenerator.generate() : "Invoke");

			// 멤버 이름 충돌을 막기 위해 멤버 이름 앞에 '_____'을 붙였습니다. (단, bootstrap method는 예외)
			this.decryptorMethodName = InvokeDynamic.this.isRenamerEnabled() ? InvokeDynamic.this.parameter.methodNameGenerator.generate(this.className) : "_____decrypt";
			this.bootstrapMethodName = InvokeDynamic.this.isRenamerEnabled() ? InvokeDynamic.this.parameter.methodNameGenerator.generate(this.className) : "_bootstrap";
			this.searchMethodName = InvokeDynamic.this.isRenamerEnabled() ? InvokeDynamic.this.parameter.methodNameGenerator.generate(this.className) : "_____search";
		}
	}

	@Override
	public final ExclusionType getExclusionType()
	{
		return ExclusionType.INVOKEDYNAMIC;
	}
}
