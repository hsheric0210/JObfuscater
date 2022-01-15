package com.eric0210.obfuscater.transformer.obfuscator.strings;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.asm.ClassWrapper;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.ASMUtils;
import com.eric0210.obfuscater.utils.RandomUtils;
import com.eric0210.obfuscater.utils.StringGenerator;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

public class StringEncryption extends Transformer
{
	public List<String> stringExclusions;
	public StringGenerator encryptorClassNameGenerator;
	public StringGenerator methodNameGenerator;
	public StringGenerator fieldNameGenerator;
	public EncryptionType encryptionType;
	public int repeatCount;
	public boolean injectDecryptor;

	public enum EncryptionType
	{
		Light("Simple XOR encryption"),
		Normal("XOR encryption based on key and member hashcodes"),
		Heavy("XOR encryption based on key and member hashcodes with advanced decryptor");

		public final String description;

		EncryptionType(final String desc)
		{
			this.description = desc;
		}
	}

	@Override
	public ExclusionType getExclusionType()
	{
		return ExclusionType.STRING_ENCRYPTION;
	}

	@Override
	public Map<String, Object> getConfiguration()
	{
		return null;
	}

	@Override
	public void setConfiguration(Map<String, Object> config)
	{

	}

	@Override
	public void verifyConfiguration(Map<String, Object> config)
	{

	}

	protected final boolean isStringExcluded(final String str)
	{
		return this.stringExclusions.stream().anyMatch(s -> str != null && !str.isEmpty() && (str.contains(s) || str.matches(s)));
	}

	@Override
	public final void transform()
	{
		final AtomicInteger affected_strings = new AtomicInteger();
		final MemberNames memberNames = new MemberNames();
		final EncryptionType mode = this.encryptionType;

		final ClassWrapper injected = RandomUtils.getRandomClassCanInject(this, this.getClassWrappers(), cw -> cw.classNode.methods.parallelStream().filter(m -> "<init>".equals(m.name)).count() == 1);
		final boolean inject = this.injectDecryptor && injected != null && mode != EncryptionType.Heavy; // Heavy mode is based on thread. Injecting thread in existing class is very-very hard. So we gave up. (xd)

		this.getClassWrappers().parallelStream().filter(classWrapper -> !this.isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.parallelStream().filter(methodWrapper -> !"<clinit>".equals(methodWrapper.originalName) && !this.isExcluded(methodWrapper) && this.hasInstructions(methodWrapper.methodNode)).forEach(methodWrapper ->
		{
			for (int i = 0, j = this.repeatCount; i < j; i++)
			{
				final MethodNode methodNode = methodWrapper.methodNode;
				int leeway = this.getSizeLeeway(methodNode);

				for (final AbstractInsnNode insn : methodNode.instructions.toArray())
				{
					if (leeway < 10000)
						break;

					if (insn instanceof LdcInsnNode)
					{
						final LdcInsnNode ldc = (LdcInsnNode) insn;
						if (ldc.cst instanceof String)
						{
							final String cst = (String) ldc.cst;
							if (!this.isStringExcluded(cst))
							{
								final int encryptionKey = RandomUtils.getRandomInt();

								final int callerClassHC = classWrapper.classNode.name.replace("/", ".").hashCode();
								final int callerMethodHC = methodNode.name.hashCode();
								final int decryptorClassHC = (inject ? injected.originalName : memberNames.decryptorClassName).replace("/", ".").hashCode();
								final int decryptorMethodHC = memberNames.decryptMethodName.hashCode();

								ldc.cst = encryptString(mode, cst, callerClassHC, callerMethodHC, decryptorClassHC, decryptorMethodHC, encryptionKey);

								switch (mode)
								{
									case Light:
									{
										methodNode.instructions.insert(insn, new MethodInsnNode(INVOKESTATIC, inject ? injected.originalName : memberNames.decryptorClassName, memberNames.decryptMethodName, "(Ljava/lang/String;I)Ljava/lang/String;", false));
										methodNode.instructions.insert(insn, ASMUtils.getNumberInsn(encryptionKey));
										break;
									}
									case Normal:
									case Heavy:
									{
										methodNode.instructions.insert(insn, new MethodInsnNode(INVOKESTATIC, inject ? injected.originalName : memberNames.decryptorClassName, memberNames.decryptMethodName, "(Ljava/lang/Object;I)Ljava/lang/String;", false));
										methodNode.instructions.insert(insn, ASMUtils.getNumberInsn(encryptionKey));
										break;
									}
								}

								if (mode == EncryptionType.Heavy)
									leeway -= 3;
								leeway -= 7;

								affected_strings.incrementAndGet();
							}
						}
					}
				}
			}
		}));

		// Let's create decryptor method
		final ClassNode decryptor = this.createDecryptor(mode, memberNames, inject, injected);

		if (!inject)
			this.getClasses().put(decryptor.name, new ClassWrapper(decryptor, false));

		Logger.stdOut(String.format("Encrypted %d strings with " + mode.description + ".", affected_strings.get()));

		if (inject)
			Logger.stdOut(String.format(" - String decryptor is injected on \"%s\".", injected.originalName.replaceAll("/", ".")));
		else
			Logger.stdOut(String.format(" - String decryptor is generated on \"%s\".", memberNames.decryptorClassName.replaceAll("/", ".")));
	}

	@Override
	public String getName()
	{
		return "String Encryption";
	}

	private static String encryptString(final EncryptionType type, final String msg, final int callerClassHC, final int callerMethodHC, final int decryptorClassHC, final int decryptorMethodHC, final int key)
	{
		final StringBuilder sb = new StringBuilder();
		switch (type)
		{
			case Light:
				for (final char c : msg.toCharArray())
					sb.append((char) (c ^ key)); // Simple XOR cipher
				break;
			case Normal:
			{
				final char[] chars = msg.toCharArray();
				for (int i = 0, j = chars.length; i < j; i++)
					switch (i % 8) // XOR cipher that changes key by index of character.
					{
						case 0:
							sb.append((char) (chars[i] ^ callerClassHC ^ key));
							break;
						case 1:
							sb.append((char) (chars[i] ^ "<clinit>".hashCode() ^ callerMethodHC));
							break;
						case 2:
							sb.append((char) (chars[i] ^ decryptorClassHC ^ callerClassHC));
							break;
						case 3:
							sb.append((char) (chars[i] ^ key ^ "<clinit>".hashCode()));
							break;
						case 4:
							sb.append((char) (chars[i] ^ callerMethodHC ^ decryptorClassHC));
							break;
						case 5:
							sb.append((char) (chars[i] ^ callerClassHC ^ "<clinit>".hashCode()));
							break;
						case 6:
							sb.append((char) (chars[i] ^ callerMethodHC ^ callerClassHC));
							break;
						case 7:
							sb.append((char) (chars[i] ^ key ^ callerMethodHC));
							break;
					}
				break;
			}
			case Heavy:
			{
				final char[] chars = msg.toCharArray();
				for (int i = 0, j = chars.length; i < j; i++)
					switch (i % 4) // little bit simple XOR cipher than normal mode.
					{
						case 0:
							sb.append((char) (key ^ callerClassHC ^ chars[i]));
							break;
						case 1:
							sb.append((char) (key ^ callerMethodHC ^ chars[i]));
							break;
						case 2:
							sb.append((char) (key ^ decryptorClassHC ^ chars[i]));
							break;
						case 3:
							sb.append((char) (key ^ decryptorMethodHC ^ chars[i]));
							break;
					}
				break;
			}
		}

		return sb.toString();
	}

	private static ClassNode createDecryptor(final EncryptionType type, final MemberNames memberNames, final boolean injectDecryptor, final ClassWrapper injectTo)
	{
		ClassNode cw = new ClassNode();
		FieldVisitor fv;
		MethodVisitor mv;

		switch (type)
		{
			case Light:
			{
				if (injectDecryptor)
					cw = injectTo.classNode;
				else
					cw.visit(V1_5, ACC_PUBLIC | ACC_FINAL | ACC_SUPER | ACC_SYNTHETIC, memberNames.decryptorClassName, null, "java/lang/Object", null);

				// Cache field
				fv = cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_SYNTHETIC, memberNames.cacheFieldName, "Ljava/util/HashMap;", null, null);
				fv.visitEnd();

				{
					// <clinit>
					final Optional<MethodNode> _clinit = injectTo.classNode.methods.parallelStream().filter(method -> "<clinit>".equalsIgnoreCase(method.name)).findFirst();
					if (injectDecryptor && _clinit.isPresent())
					{
						final MethodNode clinit = _clinit.get();

						final InsnList inserted = new InsnList();
						final Label l0 = new Label();
						inserted.add(new LabelNode(l0));
						inserted.add(new TypeInsnNode(NEW, "java/util/HashMap"));
						inserted.add(new InsnNode(DUP));
						inserted.add(new MethodInsnNode(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false));
						inserted.add(new FieldInsnNode(PUTSTATIC, injectTo.originalName, memberNames.cacheFieldName, "Ljava/util/HashMap;"));
						inserted.add(new InsnNode(RETURN));

						clinit.instructions.insertBefore(clinit.instructions.getFirst(), inserted);
						if (clinit.maxStack < 2)
							clinit.maxStack = 2;
					}
					else
					{
						mv = cw.visitMethod(ACC_STATIC | ACC_SYNTHETIC, "<clinit>", "()V", null, null);
						mv.visitCode();
						final Label l0 = new Label();
						mv.visitLabel(l0);
						mv.visitTypeInsn(NEW, "java/util/HashMap");
						mv.visitInsn(DUP);
						mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
						mv.visitFieldInsn(PUTSTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.cacheFieldName, "Ljava/util/HashMap;");
						mv.visitInsn(RETURN);
						mv.visitMaxs(2, 0);
						mv.visitEnd();
					}
				}

				{
					// <init>
					final Optional<MethodNode> _init = injectTo.classNode.methods.parallelStream().filter(method -> "<init>".equalsIgnoreCase(method.name)).findFirst();
					if (!injectDecryptor || !_init.isPresent())
					{
						mv = cw.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, "<init>", "()V", null, null);
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
				}

				{
					// Cache String Method
					mv = cw.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.putCacheMethodName, "(Ljava/lang/String;Ljava/lang/String;)V", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitFieldInsn(GETSTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.cacheFieldName, "Ljava/util/HashMap;");
					mv.visitVarInsn(ALOAD, 0);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
					mv.visitInsn(POP);
					final Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitInsn(RETURN);
					final Label l2 = new Label();
					mv.visitLabel(l2);
					mv.visitMaxs(3, 2);
					mv.visitEnd();
				}

				{
					// Return Cache Method
					mv = cw.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.getCacheMethodName, "(Ljava/lang/String;)Ljava/lang/String;", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitFieldInsn(GETSTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.cacheFieldName, "Ljava/util/HashMap;");
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
					mv.visitTypeInsn(CHECKCAST, "java/lang/String");
					mv.visitInsn(ARETURN);
					final Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitMaxs(2, 1);
					mv.visitEnd();
				}

				{
					// Cache Contains Method
					mv = cw.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.containsCacheMethodName, "(Ljava/lang/String;)Z", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitFieldInsn(GETSTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.cacheFieldName, "Ljava/util/HashMap;");
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "containsKey", "(Ljava/lang/Object;)Z", false);
					mv.visitInsn(IRETURN);
					final Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitMaxs(2, 1);
					mv.visitEnd();
				}

				{
					// Decrypt Method
					mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.decryptMethodName, "(Ljava/lang/String;I)Ljava/lang/String;", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
					mv.visitVarInsn(ILOAD, 1);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
					mv.visitVarInsn(ASTORE, 5);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitMethodInsn(INVOKESTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.containsCacheMethodName, "(Ljava/lang/String;)Z", false);
					final Label l1 = new Label();
					mv.visitJumpInsn(IFEQ, l1);
					final Label l2 = new Label();
					mv.visitLabel(l2);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitMethodInsn(INVOKESTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.getCacheMethodName, "(Ljava/lang/String;)Ljava/lang/String;", false);
					mv.visitInsn(ARETURN);
					mv.visitLabel(l1);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
					mv.visitVarInsn(ASTORE, 2);
					final Label l3 = new Label();
					mv.visitLabel(l3);
					mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
					mv.visitVarInsn(ASTORE, 3);
					final Label l4 = new Label();
					mv.visitLabel(l4);
					mv.visitInsn(ICONST_0);
					mv.visitVarInsn(ISTORE, 4);
					final Label l5 = new Label();
					mv.visitLabel(l5);
					final Label l6 = new Label();
					mv.visitJumpInsn(GOTO, l6);
					final Label l7 = new Label();
					mv.visitLabel(l7);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitVarInsn(ILOAD, 4);
					mv.visitInsn(CALOAD);
					mv.visitVarInsn(ILOAD, 1);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
					mv.visitInsn(POP);
					final Label l8 = new Label();
					mv.visitLabel(l8);
					mv.visitIincInsn(4, 1);
					mv.visitLabel(l6);
					mv.visitVarInsn(ILOAD, 4);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitJumpInsn(IF_ICMPLT, l7);
					final Label l9 = new Label();
					mv.visitLabel(l9);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
					mv.visitVarInsn(ASTORE, 4);
					final Label l10 = new Label();
					mv.visitLabel(l10);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitMethodInsn(INVOKESTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.putCacheMethodName, "(Ljava/lang/String;Ljava/lang/String;)V", false);
					final Label l11 = new Label();
					mv.visitLabel(l11);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitInsn(ARETURN);
					final Label l12 = new Label();
					mv.visitLabel(l12);
					mv.visitMaxs(3, 6);
					mv.visitEnd();
				}
				break;
			}
			case Normal:
			{
				if (injectDecryptor)
					cw = injectTo.classNode;
				else
					cw.visit(V1_5, ACC_PUBLIC | ACC_FINAL | ACC_SUPER | ACC_SYNTHETIC, memberNames.decryptorClassName, null, "java/lang/Object", null);

				{
					// Cache Field
					fv = cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_SYNTHETIC, memberNames.cacheFieldName, "Ljava/util/HashMap;", null, null);
					fv.visitEnd();
				}

				{
					// Key 1 Field
					fv = cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, memberNames.key1FieldName, "I", null, null);
					fv.visitEnd();
				}

				{
					// Key 2 Field
					fv = cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, memberNames.key2FieldName, "I", null, null);
					fv.visitEnd();
				}

				{
					// <clinit>
					final Optional<MethodNode> _clinit = injectTo.classNode.methods.parallelStream().filter(method -> "<clinit>".equalsIgnoreCase(method.name)).findFirst();
					if (injectDecryptor && _clinit.isPresent())
					{
						final MethodNode clinit = _clinit.get();

						final InsnList inserted = new InsnList();
						final Label l0 = new Label();
						inserted.add(new LabelNode(l0));
						inserted.add(new TypeInsnNode(NEW, "java/util/HashMap"));
						inserted.add(new InsnNode(DUP));
						inserted.add(new MethodInsnNode(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false));
						inserted.add(new FieldInsnNode(PUTSTATIC, injectTo.originalName, memberNames.cacheFieldName, "Ljava/util/HashMap;"));
						final Label l1 = new Label();
						inserted.add(new LabelNode(l1));
						inserted.add(new MethodInsnNode(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false));
						inserted.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Thread", "getStackTrace", "()[Ljava/lang/StackTraceElement;", false));
						inserted.add(new InsnNode(ICONST_1));
						inserted.add(new InsnNode(AALOAD));
						inserted.add(new VarInsnNode(ASTORE, 0));
						final Label l2 = new Label();
						inserted.add(new LabelNode(l2));
						inserted.add(new VarInsnNode(ALOAD, 0));
						inserted.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getClassName", "()Ljava/lang/String;", false));
						inserted.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false));
						inserted.add(new FieldInsnNode(PUTSTATIC, injectTo.originalName, memberNames.key1FieldName, "I"));
						final Label l3 = new Label();
						inserted.add(new LabelNode(l3));
						inserted.add(new VarInsnNode(ALOAD, 0));
						inserted.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName", "()Ljava/lang/String;", false));
						inserted.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false));
						inserted.add(new FieldInsnNode(PUTSTATIC, injectTo.originalName, memberNames.key2FieldName, "I"));
						final Label l4 = new Label();
						inserted.add(new LabelNode(l4));
						inserted.add(new InsnNode(RETURN));

						clinit.instructions.insertBefore(clinit.instructions.getFirst(), inserted);
						if (clinit.maxStack < 2)
							clinit.maxStack = 2;
						if (clinit.maxLocals < 1)
							clinit.maxLocals = 1;
					}
					else
					{
						mv = cw.visitMethod(ACC_STATIC | ACC_SYNTHETIC, "<clinit>", "()V", null, null);
						mv.visitCode();
						final Label l0 = new Label();
						mv.visitLabel(l0);
						mv.visitTypeInsn(NEW, "java/util/HashMap");
						mv.visitInsn(DUP);
						mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
						mv.visitFieldInsn(PUTSTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.cacheFieldName, "Ljava/util/HashMap;");
						final Label l1 = new Label();
						mv.visitLabel(l1);
						mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "getStackTrace", "()[Ljava/lang/StackTraceElement;", false);
						mv.visitInsn(ICONST_1);
						mv.visitInsn(AALOAD);
						mv.visitVarInsn(ASTORE, 0);
						final Label l2 = new Label();
						mv.visitLabel(l2);
						mv.visitVarInsn(ALOAD, 0);
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getClassName", "()Ljava/lang/String;", false);
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
						mv.visitFieldInsn(PUTSTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.key1FieldName, "I");
						final Label l3 = new Label();
						mv.visitLabel(l3);
						mv.visitVarInsn(ALOAD, 0);
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName", "()Ljava/lang/String;", false);
						mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
						mv.visitFieldInsn(PUTSTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.key2FieldName, "I");
						final Label l4 = new Label();
						mv.visitLabel(l4);
						mv.visitInsn(RETURN);
						mv.visitMaxs(2, 1);
						mv.visitEnd();
					}
				}

				{
					// <init>
					final Optional<MethodNode> _init = injectTo.classNode.methods.parallelStream().filter(method -> "<init>".equalsIgnoreCase(method.name)).findFirst();
					if (!injectDecryptor || !_init.isPresent())
					{
						mv = cw.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, "<init>", "()V", null, null);
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
				}

				{
					// Hash Method

					mv = cw.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.hashMethodName, "([C)I", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitInsn(ICONST_1);
					mv.visitVarInsn(ISTORE, 1);
					final Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitInsn(ICONST_0);
					mv.visitVarInsn(ISTORE, 2);
					final Label l2 = new Label();
					mv.visitLabel(l2);
					final Label l3 = new Label();
					mv.visitJumpInsn(GOTO, l3);
					final Label l4 = new Label();
					mv.visitLabel(l4);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitVarInsn(ILOAD, 2);
					mv.visitInsn(CALOAD);
					mv.visitVarInsn(ISTORE, 3);
					final Label l5 = new Label();
					mv.visitLabel(l5);
					mv.visitVarInsn(ILOAD, 3);
					mv.visitIntInsn(SIPUSH, 255);
					mv.visitInsn(IAND);
					mv.visitVarInsn(ISTORE, 4);
					final Label l6 = new Label();
					mv.visitLabel(l6);
					mv.visitVarInsn(ILOAD, 3);
					mv.visitIntInsn(SIPUSH, 255);
					mv.visitInsn(IOR);
					mv.visitVarInsn(ISTORE, 5);
					final Label l7 = new Label();
					mv.visitLabel(l7);
					mv.visitVarInsn(ILOAD, 3);
					mv.visitIntInsn(SIPUSH, 255);
					mv.visitInsn(IXOR);
					mv.visitVarInsn(ISTORE, 6);
					final Label l8 = new Label();
					mv.visitLabel(l8);
					mv.visitVarInsn(ILOAD, 4);
					mv.visitInsn(ICONST_4);
					mv.visitInsn(ISHL);
					mv.visitVarInsn(ILOAD, 5);
					mv.visitInsn(ICONST_4);
					mv.visitInsn(IUSHR);
					mv.visitInsn(IOR);
					mv.visitVarInsn(ISTORE, 7);
					final Label l9 = new Label();
					mv.visitLabel(l9);
					mv.visitVarInsn(ILOAD, 6);
					mv.visitInsn(ICONST_3);
					mv.visitInsn(ISHL);
					mv.visitVarInsn(ILOAD, 7);
					mv.visitIntInsn(BIPUSH, 6);
					mv.visitInsn(IUSHR);
					mv.visitInsn(IOR);
					mv.visitVarInsn(ISTORE, 8);
					final Label l10 = new Label();
					mv.visitLabel(l10);
					mv.visitVarInsn(ILOAD, 4);
					mv.visitVarInsn(ILOAD, 7);
					mv.visitInsn(ICONST_2);
					mv.visitInsn(ISHL);
					mv.visitVarInsn(ILOAD, 4);
					mv.visitInsn(ICONST_2);
					mv.visitInsn(IUSHR);
					mv.visitInsn(IOR);
					mv.visitInsn(IAND);
					mv.visitVarInsn(ISTORE, 4);
					final Label l11 = new Label();
					mv.visitLabel(l11);
					mv.visitVarInsn(ILOAD, 6);
					mv.visitVarInsn(ILOAD, 4);
					mv.visitInsn(ICONST_4);
					mv.visitInsn(ISHR);
					mv.visitVarInsn(ILOAD, 5);
					mv.visitInsn(ICONST_2);
					mv.visitInsn(ISHL);
					mv.visitInsn(IOR);
					mv.visitInsn(IOR);
					mv.visitVarInsn(ISTORE, 6);
					final Label l12 = new Label();
					mv.visitLabel(l12);
					mv.visitVarInsn(ILOAD, 5);
					mv.visitVarInsn(ILOAD, 8);
					mv.visitInsn(ICONST_4);
					mv.visitInsn(IUSHR);
					mv.visitVarInsn(ILOAD, 6);
					mv.visitIntInsn(BIPUSH, 6);
					mv.visitInsn(ISHL);
					mv.visitInsn(IOR);
					mv.visitInsn(IXOR);
					mv.visitVarInsn(ISTORE, 5);
					final Label l13 = new Label();
					mv.visitLabel(l13);
					mv.visitVarInsn(ILOAD, 7);
					mv.visitVarInsn(ILOAD, 5);
					mv.visitInsn(IADD);
					mv.visitVarInsn(ISTORE, 7);
					final Label l14 = new Label();
					mv.visitLabel(l14);
					mv.visitVarInsn(ILOAD, 4);
					mv.visitInsn(ICONST_5);
					mv.visitInsn(IUSHR);
					mv.visitVarInsn(ILOAD, 6);
					mv.visitInsn(ICONST_2);
					mv.visitInsn(ISHL);
					mv.visitInsn(IOR);
					mv.visitVarInsn(ISTORE, 8);
					final Label l15 = new Label();
					mv.visitLabel(l15);
					mv.visitVarInsn(ILOAD, 1);
					mv.visitVarInsn(ILOAD, 4);
					mv.visitVarInsn(ILOAD, 5);
					mv.visitInsn(IXOR);
					mv.visitVarInsn(ILOAD, 6);
					mv.visitInsn(IXOR);
					mv.visitVarInsn(ILOAD, 7);
					mv.visitInsn(IXOR);
					mv.visitVarInsn(ILOAD, 8);
					mv.visitInsn(IXOR);
					mv.visitInsn(IXOR);
					mv.visitVarInsn(ISTORE, 1);
					final Label l16 = new Label();
					mv.visitLabel(l16);
					mv.visitIincInsn(2, 1);
					mv.visitLabel(l3);
					mv.visitVarInsn(ILOAD, 2);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitJumpInsn(IF_ICMPLT, l4);
					final Label l17 = new Label();
					mv.visitLabel(l17);
					mv.visitVarInsn(ILOAD, 1);
					mv.visitInsn(IRETURN);
					final Label l18 = new Label();
					mv.visitLabel(l18);
					mv.visitMaxs(4, 9);
					mv.visitEnd();
				}

				{
					// getCache Method

					mv = cw.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.getCacheMethodName, "(I)Ljava/lang/String;", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitFieldInsn(GETSTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.cacheFieldName, "Ljava/util/HashMap;");
					mv.visitVarInsn(ILOAD, 0);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
					mv.visitTypeInsn(CHECKCAST, "java/lang/String");
					mv.visitInsn(ARETURN);
					final Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitMaxs(2, 1);
					mv.visitEnd();
				}

				{
					// putCache Method

					mv = cw.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.putCacheMethodName, "(Ljava/lang/String;I)V", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitFieldInsn(GETSTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.cacheFieldName, "Ljava/util/HashMap;");
					mv.visitVarInsn(ILOAD, 1);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
					mv.visitInsn(POP);
					final Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitInsn(RETURN);
					final Label l2 = new Label();
					mv.visitLabel(l2);
					mv.visitMaxs(3, 2);
					mv.visitEnd();
				}

				{
					// Decrypt Method

					mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.decryptMethodName, "(Ljava/lang/Object;I)Ljava/lang/String;", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitTypeInsn(CHECKCAST, "java/lang/String");
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
					mv.visitVarInsn(ASTORE, 2);
					final Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitMethodInsn(INVOKESTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.hashMethodName, "([C)I", false);
					mv.visitVarInsn(ISTORE, 3);
					final Label l2 = new Label();
					mv.visitLabel(l2);
					mv.visitVarInsn(ILOAD, 3);
					mv.visitMethodInsn(INVOKESTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.getCacheMethodName, "(I)Ljava/lang/String;", false);
					mv.visitVarInsn(ASTORE, 4);
					final Label l3 = new Label();
					mv.visitLabel(l3);
					mv.visitVarInsn(ALOAD, 4);
					final Label l4 = new Label();
					mv.visitJumpInsn(IFNULL, l4);
					final Label l5 = new Label();
					mv.visitLabel(l5);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitInsn(ARETURN);
					mv.visitLabel(l4);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "getStackTrace", "()[Ljava/lang/StackTraceElement;", false);
					mv.visitVarInsn(ASTORE, 5);
					final Label l6 = new Label();
					mv.visitLabel(l6);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitInsn(ICONST_2);
					mv.visitInsn(AALOAD);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getClassName", "()Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
					mv.visitVarInsn(ISTORE, 6);
					final Label l7 = new Label();
					mv.visitLabel(l7);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitInsn(ICONST_2);
					mv.visitInsn(AALOAD);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName", "()Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
					mv.visitVarInsn(ISTORE, 7);
					final Label l8 = new Label();
					mv.visitLabel(l8);
					mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
					mv.visitVarInsn(ASTORE, 8);
					final Label l9 = new Label();
					mv.visitLabel(l9);
					mv.visitInsn(ICONST_0);
					mv.visitVarInsn(ISTORE, 9);
					final Label l10 = new Label();
					mv.visitLabel(l10);
					final Label l11 = new Label();
					mv.visitJumpInsn(GOTO, l11);
					final Label l12 = new Label();
					mv.visitLabel(l12);
					mv.visitVarInsn(ILOAD, 9);
					mv.visitIntInsn(BIPUSH, 8);
					mv.visitInsn(IREM);
					final Label l13 = new Label();
					final Label l14 = new Label();
					final Label l15 = new Label();
					final Label l16 = new Label();
					final Label l17 = new Label();
					final Label l18 = new Label();
					final Label l19 = new Label();
					final Label l20 = new Label();
					final Label l21 = new Label();
					mv.visitTableSwitchInsn(0, 7, l21, l13, l14, l15, l16, l17, l18, l19, l20);
					mv.visitLabel(l13);
					mv.visitVarInsn(ALOAD, 8);
					mv.visitVarInsn(ILOAD, 1);
					mv.visitVarInsn(ILOAD, 6);
					mv.visitInsn(IXOR);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitVarInsn(ILOAD, 9);
					mv.visitInsn(CALOAD);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
					mv.visitInsn(POP);
					final Label l22 = new Label();
					mv.visitLabel(l22);
					mv.visitJumpInsn(GOTO, l21);
					mv.visitLabel(l14);
					mv.visitVarInsn(ALOAD, 8);
					mv.visitVarInsn(ILOAD, 7);
					mv.visitFieldInsn(GETSTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.key2FieldName, "I");
					mv.visitInsn(IXOR);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitVarInsn(ILOAD, 9);
					mv.visitInsn(CALOAD);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
					mv.visitInsn(POP);
					final Label l23 = new Label();
					mv.visitLabel(l23);
					mv.visitJumpInsn(GOTO, l21);
					mv.visitLabel(l15);
					mv.visitVarInsn(ALOAD, 8);
					mv.visitVarInsn(ILOAD, 6);
					mv.visitFieldInsn(GETSTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.key1FieldName, "I");
					mv.visitInsn(IXOR);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitVarInsn(ILOAD, 9);
					mv.visitInsn(CALOAD);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
					mv.visitInsn(POP);
					final Label l24 = new Label();
					mv.visitLabel(l24);
					mv.visitJumpInsn(GOTO, l21);
					mv.visitLabel(l16);
					mv.visitVarInsn(ALOAD, 8);
					mv.visitFieldInsn(GETSTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.key2FieldName, "I");
					mv.visitVarInsn(ILOAD, 1);
					mv.visitInsn(IXOR);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitVarInsn(ILOAD, 9);
					mv.visitInsn(CALOAD);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
					mv.visitInsn(POP);
					final Label l25 = new Label();
					mv.visitLabel(l25);
					mv.visitJumpInsn(GOTO, l21);
					mv.visitLabel(l17);
					mv.visitVarInsn(ALOAD, 8);
					mv.visitFieldInsn(GETSTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.key1FieldName, "I");
					mv.visitVarInsn(ILOAD, 7);
					mv.visitInsn(IXOR);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitVarInsn(ILOAD, 9);
					mv.visitInsn(CALOAD);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
					mv.visitInsn(POP);
					final Label l26 = new Label();
					mv.visitLabel(l26);
					mv.visitJumpInsn(GOTO, l21);
					mv.visitLabel(l18);
					mv.visitVarInsn(ALOAD, 8);
					mv.visitFieldInsn(GETSTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.key2FieldName, "I");
					mv.visitVarInsn(ILOAD, 6);
					mv.visitInsn(IXOR);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitVarInsn(ILOAD, 9);
					mv.visitInsn(CALOAD);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
					mv.visitInsn(POP);
					final Label l27 = new Label();
					mv.visitLabel(l27);
					mv.visitJumpInsn(GOTO, l21);
					mv.visitLabel(l19);
					mv.visitVarInsn(ALOAD, 8);
					mv.visitVarInsn(ILOAD, 6);
					mv.visitVarInsn(ILOAD, 7);
					mv.visitInsn(IXOR);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitVarInsn(ILOAD, 9);
					mv.visitInsn(CALOAD);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
					mv.visitInsn(POP);
					final Label l28 = new Label();
					mv.visitLabel(l28);
					mv.visitJumpInsn(GOTO, l21);
					mv.visitLabel(l20);
					mv.visitVarInsn(ALOAD, 8);
					mv.visitVarInsn(ILOAD, 7);
					mv.visitVarInsn(ILOAD, 1);
					mv.visitInsn(IXOR);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitVarInsn(ILOAD, 9);
					mv.visitInsn(CALOAD);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
					mv.visitInsn(POP);
					mv.visitLabel(l21);
					mv.visitIincInsn(9, 1);
					mv.visitLabel(l11);
					mv.visitVarInsn(ILOAD, 9);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitJumpInsn(IF_ICMPLT, l12);
					final Label l29 = new Label();
					mv.visitLabel(l29);
					mv.visitVarInsn(ALOAD, 8);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
					mv.visitVarInsn(ASTORE, 9);
					final Label l30 = new Label();
					mv.visitLabel(l30);
					mv.visitVarInsn(ALOAD, 9);
					mv.visitVarInsn(ILOAD, 3);
					mv.visitMethodInsn(INVOKESTATIC, injectDecryptor ? injectTo.originalName : memberNames.decryptorClassName, memberNames.putCacheMethodName, "(Ljava/lang/String;I)V", false);
					final Label l31 = new Label();
					mv.visitLabel(l31);
					mv.visitVarInsn(ALOAD, 9);
					mv.visitInsn(ARETURN);
					final Label l32 = new Label();
					mv.visitLabel(l32);
					mv.visitMaxs(4, 10);
					mv.visitEnd();
				}
				break;
			}
			case Heavy:
			{
				cw.visit(V1_5, ACC_PUBLIC | ACC_FINAL | ACC_SUPER | ACC_SYNTHETIC, memberNames.decryptorClassName, null, "java/lang/Thread", null);
				{
					// Info Field
					fv = cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_VOLATILE | ACC_SYNTHETIC, memberNames.infoFieldName, "[Ljava/lang/Object;", null, null);
					fv.visitEnd();
				}

				{
					// Cache Field
					fv = cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_SYNTHETIC, memberNames.cacheFieldName, "Ljava/util/HashMap;", null, null);
					fv.visitEnd();
				}

				{
					// <clinit>
					mv = cw.visitMethod(ACC_STATIC | ACC_SYNTHETIC, "<clinit>", "()V", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitTypeInsn(NEW, "java/util/HashMap");
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V", false);
					mv.visitFieldInsn(PUTSTATIC, memberNames.decryptorClassName, memberNames.cacheFieldName, "Ljava/util/HashMap;");
					mv.visitInsn(RETURN);
					mv.visitMaxs(2, 0);
					mv.visitEnd();
				}

				{
					// <init>
					mv = cw.visitMethod(ACC_PUBLIC | ACC_SYNTHETIC, "<init>", "()V", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Thread", "<init>", "()V", false);
					mv.visitInsn(RETURN);
					final Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitMaxs(1, 1);
					mv.visitEnd();
				}

				{
					// Thread run method
					mv = cw.visitMethod(ACC_PUBLIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, "run", "()V", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKESPECIAL, memberNames.decryptorClassName, memberNames.populateMethodName, "()V", false);
					final Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitInsn(RETURN);
					final Label l2 = new Label();
					mv.visitLabel(l2);
					mv.visitMaxs(1, 1);
					mv.visitEnd();
				}

				{
					// Populate Method
					mv = cw.visitMethod(ACC_PRIVATE | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.populateMethodName, "()V", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitInsn(ICONST_5);
					mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
					mv.visitFieldInsn(PUTSTATIC, memberNames.decryptorClassName, memberNames.infoFieldName, "[Ljava/lang/Object;");
					final Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKEVIRTUAL, memberNames.decryptorClassName, "getStackTrace", "()[Ljava/lang/StackTraceElement;", false);
					mv.visitVarInsn(ASTORE, 1);
					final Label l2 = new Label();
					mv.visitLabel(l2);
					mv.visitLdcInsn(Type.getType('L' + memberNames.decryptorClassName + ';'));
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getDeclaredMethods", "()[Ljava/lang/reflect/Method;", false);
					mv.visitVarInsn(ASTORE, 2);
					final Label l3 = new Label();
					mv.visitLabel(l3);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Runtime", "getRuntime", "()Ljava/lang/Runtime;", false);
					mv.visitVarInsn(ASTORE, 3);
					final Label l4 = new Label();
					mv.visitLabel(l4);
					mv.visitInsn(ICONST_0);
					mv.visitVarInsn(ISTORE, 4);
					final Label l5 = new Label();
					mv.visitLabel(l5);
					final Label l6 = new Label();
					mv.visitJumpInsn(GOTO, l6);
					final Label l7 = new Label();
					mv.visitLabel(l7);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitVarInsn(ILOAD, 4);
					mv.visitInsn(AALOAD);
					mv.visitVarInsn(ASTORE, 5);
					final Label l8 = new Label();
					mv.visitLabel(l8);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "getReturnType", "()Ljava/lang/Class;", false);
					mv.visitLdcInsn(Type.getType("Ljava/lang/String;"));
					final Label l9 = new Label();
					mv.visitJumpInsn(IF_ACMPNE, l9);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "getParameterTypes", "()[Ljava/lang/Class;", false);
					mv.visitInsn(ICONST_2);
					mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
					mv.visitInsn(DUP);
					mv.visitInsn(ICONST_0);
					mv.visitLdcInsn(Type.getType("Ljava/lang/Object;"));
					mv.visitInsn(AASTORE);
					mv.visitInsn(DUP);
					mv.visitInsn(ICONST_1);
					mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
					mv.visitInsn(AASTORE);
					mv.visitMethodInsn(INVOKESTATIC, "java/util/Arrays", "equals", "([Ljava/lang/Object;[Ljava/lang/Object;)Z", false);
					mv.visitJumpInsn(IFEQ, l9);
					final Label l10 = new Label();
					mv.visitLabel(l10);
					mv.visitFieldInsn(GETSTATIC, memberNames.decryptorClassName, memberNames.infoFieldName, "[Ljava/lang/Object;");
					mv.visitInsn(ICONST_4);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Method", "getName", "()Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					mv.visitInsn(AASTORE);
					final Label l11 = new Label();
					mv.visitLabel(l11);
					final Label l12 = new Label();
					mv.visitJumpInsn(GOTO, l12);
					mv.visitLabel(l9);
					mv.visitIincInsn(4, 1);
					mv.visitLabel(l6);
					mv.visitVarInsn(ILOAD, 4);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitInsn(ARRAYLENGTH);
					mv.visitJumpInsn(IF_ICMPLT, l7);
					mv.visitLabel(l12);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Runtime", "availableProcessors", "()I", false);
					mv.visitVarInsn(ISTORE, 4);
					final Label l13 = new Label();
					mv.visitLabel(l13);
					mv.visitFieldInsn(GETSTATIC, memberNames.decryptorClassName, memberNames.infoFieldName, "[Ljava/lang/Object;");
					mv.visitInsn(ICONST_0);
					mv.visitVarInsn(ILOAD, 4);
					mv.visitInsn(ICONST_1);
					mv.visitInsn(IADD);
					mv.visitVarInsn(ILOAD, 4);
					mv.visitInsn(IREM);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					mv.visitInsn(AASTORE);
					final Label l14 = new Label();
					mv.visitLabel(l14);
					mv.visitFieldInsn(GETSTATIC, memberNames.decryptorClassName, memberNames.infoFieldName, "[Ljava/lang/Object;");
					mv.visitInsn(ICONST_1);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitFieldInsn(GETSTATIC, memberNames.decryptorClassName, memberNames.infoFieldName, "[Ljava/lang/Object;");
					mv.visitInsn(ICONST_0);
					mv.visitInsn(AALOAD);
					mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
					mv.visitInsn(AALOAD);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getClassName", "()Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					mv.visitInsn(AASTORE);
					final Label l15 = new Label();
					mv.visitLabel(l15);
					mv.visitFieldInsn(GETSTATIC, memberNames.decryptorClassName, memberNames.infoFieldName, "[Ljava/lang/Object;");
					mv.visitInsn(ICONST_2);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitFieldInsn(GETSTATIC, memberNames.decryptorClassName, memberNames.infoFieldName, "[Ljava/lang/Object;");
					mv.visitInsn(ICONST_0);
					mv.visitInsn(AALOAD);
					mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
					mv.visitInsn(AALOAD);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName", "()Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					mv.visitInsn(AASTORE);
					final Label l16 = new Label();
					mv.visitLabel(l16);
					mv.visitFieldInsn(GETSTATIC, memberNames.decryptorClassName, memberNames.infoFieldName, "[Ljava/lang/Object;");
					mv.visitInsn(ICONST_3);
					mv.visitFieldInsn(GETSTATIC, memberNames.decryptorClassName, memberNames.infoFieldName, "[Ljava/lang/Object;");
					mv.visitInsn(ICONST_0);
					mv.visitInsn(AALOAD);
					mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
					mv.visitInsn(ICONST_1);
					mv.visitInsn(ISHL);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
					mv.visitInsn(AASTORE);
					final Label l17 = new Label();
					mv.visitLabel(l17);
					mv.visitInsn(RETURN);
					final Label l18 = new Label();
					mv.visitLabel(l18);
					mv.visitMaxs(5, 6);
					mv.visitEnd();
				}

				{
					// createInfo Method
					mv = cw.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.createInfoMethodName, "()V", null, new String[]
					{
							"java/lang/InterruptedException"
					});
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitTypeInsn(NEW, memberNames.decryptorClassName);
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, memberNames.decryptorClassName, "<init>", "()V", false);
					mv.visitVarInsn(ASTORE, 0);
					final Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKEVIRTUAL, memberNames.decryptorClassName, "start", "()V", false);
					final Label l2 = new Label();
					mv.visitLabel(l2);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKEVIRTUAL, memberNames.decryptorClassName, "join", "()V", false);
					final Label l3 = new Label();
					mv.visitLabel(l3);
					mv.visitInsn(RETURN);
					final Label l4 = new Label();
					mv.visitLabel(l4);
					mv.visitMaxs(2, 1);
					mv.visitEnd();
				}

				{
					// putCache Method
					mv = cw.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.putCacheMethodName, "(Ljava/lang/String;Ljava/lang/String;)V", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitFieldInsn(GETSTATIC, memberNames.decryptorClassName, memberNames.cacheFieldName, "Ljava/util/HashMap;");
					mv.visitVarInsn(ALOAD, 0);
					mv.visitVarInsn(ALOAD, 1);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", false);
					mv.visitInsn(POP);
					final Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitInsn(RETURN);
					final Label l2 = new Label();
					mv.visitLabel(l2);
					mv.visitMaxs(3, 2);
					mv.visitEnd();
				}

				{
					// getCache Method
					mv = cw.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.getCacheMethodName, "(Ljava/lang/String;)Ljava/lang/String;", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitFieldInsn(GETSTATIC, memberNames.decryptorClassName, memberNames.cacheFieldName, "Ljava/util/HashMap;");
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
					mv.visitTypeInsn(CHECKCAST, "java/lang/String");
					mv.visitInsn(ARETURN);
					final Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitMaxs(2, 1);
					mv.visitEnd();
				}

				{
					// containsCache Method
					mv = cw.visitMethod(ACC_PRIVATE | ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.containsCacheMethodName, "(Ljava/lang/String;)Z", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					mv.visitLabel(l0);
					mv.visitFieldInsn(GETSTATIC, memberNames.decryptorClassName, memberNames.cacheFieldName, "Ljava/util/HashMap;");
					mv.visitVarInsn(ALOAD, 0);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "containsKey", "(Ljava/lang/Object;)Z", false);
					mv.visitInsn(IRETURN);
					final Label l1 = new Label();
					mv.visitLabel(l1);
					mv.visitMaxs(2, 1);
					mv.visitEnd();
				}

				{
					// Decrypt Method
					mv = cw.visitMethod(ACC_PUBLIC | ACC_STATIC | ACC_FINAL | ACC_BRIDGE | ACC_SYNTHETIC, memberNames.decryptMethodName, "(Ljava/lang/Object;I)Ljava/lang/String;", null, null);
					mv.visitCode();
					final Label l0 = new Label();
					final Label l1 = new Label();
					final Label l2 = new Label();
					mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");
					final Label l3 = new Label();
					final Label l4 = new Label();
					final Label l5 = new Label();
					mv.visitTryCatchBlock(l3, l4, l5, "java/lang/Throwable");
					final Label l6 = new Label();
					final Label l7 = new Label();
					mv.visitTryCatchBlock(l6, l7, l5, "java/lang/Throwable");
					mv.visitLabel(l3);
					mv.visitVarInsn(ALOAD, 0);
					mv.visitTypeInsn(CHECKCAST, "java/lang/String");
					mv.visitVarInsn(ASTORE, 2);
					final Label l8 = new Label();
					mv.visitLabel(l8);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitMethodInsn(INVOKESTATIC, memberNames.decryptorClassName, memberNames.containsCacheMethodName, "(Ljava/lang/String;)Z", false);
					mv.visitJumpInsn(IFEQ, l6);
					final Label l9 = new Label();
					mv.visitLabel(l9);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitMethodInsn(INVOKESTATIC, memberNames.decryptorClassName, memberNames.getCacheMethodName, "(Ljava/lang/String;)Ljava/lang/String;", false);
					mv.visitLabel(l4);
					mv.visitInsn(ARETURN);
					mv.visitLabel(l6);
					mv.visitFieldInsn(GETSTATIC, memberNames.decryptorClassName, memberNames.infoFieldName, "[Ljava/lang/Object;");
					final Label l10 = new Label();
					mv.visitJumpInsn(IFNONNULL, l10);
					final Label l11 = new Label();
					mv.visitLabel(l11);
					mv.visitMethodInsn(INVOKESTATIC, memberNames.decryptorClassName, memberNames.createInfoMethodName, "()V", false);
					mv.visitLabel(l10);
					mv.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Thread", "getStackTrace", "()[Ljava/lang/StackTraceElement;", false);
					mv.visitVarInsn(ASTORE, 3);
					final Label l12 = new Label();
					mv.visitLabel(l12);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C", false);
					mv.visitVarInsn(ASTORE, 4);
					final Label l13 = new Label();
					mv.visitLabel(l13);
					mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
					mv.visitInsn(DUP);
					mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
					mv.visitVarInsn(ASTORE, 5);
					final Label l14 = new Label();
					mv.visitLabel(l14);
					mv.visitInsn(ICONST_0);
					mv.visitVarInsn(ISTORE, 6);
					mv.visitLabel(l0);
					mv.visitVarInsn(ILOAD, 6);
					mv.visitInsn(ICONST_4);
					mv.visitInsn(IREM);
					final Label l15 = new Label();
					final Label l16 = new Label();
					final Label l17 = new Label();
					final Label l18 = new Label();
					mv.visitTableSwitchInsn(0, 3, l1, l15, l16, l17, l18);
					mv.visitLabel(l15);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitVarInsn(ILOAD, 6);
					mv.visitInsn(CALOAD);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitFieldInsn(GETSTATIC, memberNames.decryptorClassName, memberNames.infoFieldName, "[Ljava/lang/Object;");
					mv.visitInsn(ICONST_3);
					mv.visitInsn(AALOAD);
					mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
					mv.visitIntInsn(SIPUSH, 255);
					mv.visitInsn(IAND);
					mv.visitInsn(AALOAD);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getClassName", "()Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
					mv.visitInsn(IXOR);
					mv.visitVarInsn(ILOAD, 1);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
					mv.visitInsn(POP);
					final Label l19 = new Label();
					mv.visitLabel(l19);
					final Label l20 = new Label();
					mv.visitJumpInsn(GOTO, l20);
					mv.visitLabel(l16);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitVarInsn(ILOAD, 6);
					mv.visitInsn(CALOAD);
					mv.visitVarInsn(ALOAD, 3);
					mv.visitFieldInsn(GETSTATIC, memberNames.decryptorClassName, memberNames.infoFieldName, "[Ljava/lang/Object;");
					mv.visitInsn(ICONST_3);
					mv.visitInsn(AALOAD);
					mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
					mv.visitIntInsn(SIPUSH, 255);
					mv.visitInsn(IAND);
					mv.visitInsn(AALOAD);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StackTraceElement", "getMethodName", "()Ljava/lang/String;", false);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
					mv.visitInsn(IXOR);
					mv.visitVarInsn(ILOAD, 1);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
					mv.visitInsn(POP);
					final Label l21 = new Label();
					mv.visitLabel(l21);
					mv.visitJumpInsn(GOTO, l20);
					mv.visitLabel(l17);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitVarInsn(ILOAD, 6);
					mv.visitInsn(CALOAD);
					mv.visitFieldInsn(GETSTATIC, memberNames.decryptorClassName, memberNames.infoFieldName, "[Ljava/lang/Object;");
					mv.visitInsn(ICONST_1);
					mv.visitInsn(AALOAD);
					mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
					mv.visitInsn(IXOR);
					mv.visitVarInsn(ILOAD, 1);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
					mv.visitInsn(POP);
					final Label l22 = new Label();
					mv.visitLabel(l22);
					mv.visitJumpInsn(GOTO, l20);
					mv.visitLabel(l18);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitVarInsn(ALOAD, 4);
					mv.visitVarInsn(ILOAD, 6);
					mv.visitInsn(CALOAD);
					mv.visitFieldInsn(GETSTATIC, memberNames.decryptorClassName, memberNames.infoFieldName, "[Ljava/lang/Object;");
					mv.visitInsn(ICONST_4);
					mv.visitInsn(AALOAD);
					mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
					mv.visitInsn(IXOR);
					mv.visitVarInsn(ILOAD, 1);
					mv.visitInsn(IXOR);
					mv.visitInsn(I2C);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false);
					mv.visitInsn(POP);
					mv.visitLabel(l1);
					mv.visitJumpInsn(GOTO, l20);
					mv.visitLabel(l2);
					mv.visitVarInsn(ASTORE, 7);
					final Label l23 = new Label();
					mv.visitLabel(l23);
					final Label l24 = new Label();
					mv.visitJumpInsn(GOTO, l24);
					mv.visitLabel(l20);
					mv.visitIincInsn(6, 1);
					final Label l25 = new Label();
					mv.visitLabel(l25);
					mv.visitJumpInsn(GOTO, l0);
					mv.visitLabel(l24);
					mv.visitVarInsn(ALOAD, 5);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
					mv.visitVarInsn(ASTORE, 7);
					final Label l26 = new Label();
					mv.visitLabel(l26);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitVarInsn(ALOAD, 7);
					mv.visitMethodInsn(INVOKESTATIC, memberNames.decryptorClassName, memberNames.putCacheMethodName, "(Ljava/lang/String;Ljava/lang/String;)V", false);
					final Label l27 = new Label();
					mv.visitLabel(l27);
					mv.visitVarInsn(ALOAD, 7);
					mv.visitLabel(l7);
					mv.visitInsn(ARETURN);
					mv.visitLabel(l5);
					mv.visitVarInsn(ASTORE, 2);
					final Label l28 = new Label();
					mv.visitLabel(l28);
					mv.visitVarInsn(ALOAD, 2);
					mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Throwable", "printStackTrace", "()V", false);
					final Label l29 = new Label();
					mv.visitLabel(l29);
					mv.visitInsn(ACONST_NULL);
					mv.visitInsn(ARETURN);
					final Label l30 = new Label();
					mv.visitLabel(l30);
					mv.visitMaxs(5, 8);
					mv.visitEnd();
				}
				break;
			}
		}

		cw.visitEnd();
		return cw;
	}

	private final class MemberNames
	{
		final String decryptorClassName;
		final String cacheFieldName;
		final String putCacheMethodName;
		final String getCacheMethodName;
		final String containsCacheMethodName;
		final String decryptMethodName;

		final String key1FieldName;
		final String key2FieldName;
		final String hashMethodName;

		final String infoFieldName;
		final String populateMethodName;
		final String createInfoMethodName;

		MemberNames()
		{
			this.decryptorClassName = StringEncryption.this.randomClassPath(StringEncryption.this.getClasses().keySet()) + (StringEncryption.this.isRenamerEnabled() ? StringEncryption.this.encryptorClassNameGenerator.generate() : "String");

			// 멤버 이름 충돌을 막기 위해 멤버 이름 앞에 '_____'을 붙였습니다. (단, decryptor method는 예외)
			this.cacheFieldName = StringEncryption.this.isRenamerEnabled() ? StringEncryption.this.fieldNameGenerator.generate(this.decryptorClassName) : "_____cache";

			this.putCacheMethodName = StringEncryption.this.isRenamerEnabled() ? StringEncryption.this.methodNameGenerator.generate(this.decryptorClassName) : "_____put";
			this.getCacheMethodName = StringEncryption.this.isRenamerEnabled() ? StringEncryption.this.methodNameGenerator.generate(this.decryptorClassName) : "_____get";
			this.containsCacheMethodName = StringEncryption.this.isRenamerEnabled() ? StringEncryption.this.methodNameGenerator.generate(this.decryptorClassName) : "_____contains";
			this.decryptMethodName = StringEncryption.this.isRenamerEnabled() ? StringEncryption.this.methodNameGenerator.generate(this.decryptorClassName) : "_decrypt";

			this.key1FieldName = StringEncryption.this.isRenamerEnabled() ? StringEncryption.this.fieldNameGenerator.generate(this.decryptorClassName) : "_____key1";
			this.key2FieldName = StringEncryption.this.isRenamerEnabled() ? StringEncryption.this.fieldNameGenerator.generate(this.decryptorClassName) : "_____key2";
			this.hashMethodName = StringEncryption.this.isRenamerEnabled() ? StringEncryption.this.methodNameGenerator.generate(this.decryptorClassName) : "_____hash";

			this.infoFieldName = StringEncryption.this.isRenamerEnabled() ? StringEncryption.this.fieldNameGenerator.generate(this.decryptorClassName) : "_____info";
			this.populateMethodName = StringEncryption.this.isRenamerEnabled() ? StringEncryption.this.methodNameGenerator.generate(this.decryptorClassName) : "_____populate";

			this.createInfoMethodName = StringEncryption.this.isRenamerEnabled() ? StringEncryption.this.methodNameGenerator.generate(this.decryptorClassName) : "_____createInfo";
		}
	}
}
