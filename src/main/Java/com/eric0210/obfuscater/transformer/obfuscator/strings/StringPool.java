package com.eric0210.obfuscater.transformer.obfuscator.strings;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.asm.ClassWrapper;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.*;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

import org.objectweb.asm.tree.*;

public class StringPool extends Transformer
{
	public enum Mode
	{
		Local,
		Global,
		Random;
	}

	public static class Parameter
	{
		public final List<String> stringExclusions;
		public final StringGenerator encryptorClassNameGenerator;
		public final StringGenerator methodNameGenerator;
		public final StringGenerator fieldNameGenerator;
		public final Mode mode;

		public Parameter(final Mode mode, final List<String> exemptedStrings, final StringGenerator classnameGenerator, final StringGenerator methodNameGenerator, final StringGenerator fieldNameGenerator)
		{
			this.stringExclusions = new ArrayList<>(exemptedStrings);
			this.encryptorClassNameGenerator = classnameGenerator;
			this.methodNameGenerator = methodNameGenerator;
			this.fieldNameGenerator = fieldNameGenerator;
			this.mode = mode;
		}
	}

	protected final Parameter parameter;

	public StringPool(final Parameter setup)
	{
		this.parameter = setup;
	}

	protected final boolean isStringExcluded(final String str)
	{
		return this.parameter.stringExclusions.stream().anyMatch(s -> str != null && !str.isEmpty() && (str.contains(s) || str.matches(s)));
	}

	@Override
	public final void transform()
	{
		final AtomicInteger pooledStringCount = new AtomicInteger();

		switch (this.parameter.mode)
		{
			case Global:
			{
				final AtomicInteger stringCount = new AtomicInteger(0);

				this.getClassWrappers().parallelStream().filter(classWrapper -> !this.isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.parallelStream().filter(methodWrapper -> !this.isExcluded(methodWrapper) && this.hasInstructions(methodWrapper.methodNode)).map(methodWrapper -> methodWrapper.methodNode).forEach(methodNode ->
				{
					for (final AbstractInsnNode insn : methodNode.instructions.toArray())
						if (insn instanceof LdcInsnNode)
						{
							final Object cst = ((LdcInsnNode) insn).cst;
							if (cst instanceof String)
							{
								final String str = (String) cst;
								if (!this.isStringExcluded(str))
									stringCount.incrementAndGet();
							}
						}
				}));

				final Map<Integer, String> mappings = new HashMap<>(stringCount.get());
				final Set<Integer> alreadyUsed = new HashSet<>();

				final ClassNode classNode = new ClassNode();
				final String classPath = this.randomClassPath(this.getClasses().keySet()) + (this.isRenamerEnabled() ? this.parameter.encryptorClassNameGenerator.generate() : "Pool");

				final String fieldName = this.isRenamerEnabled() ? this.parameter.fieldNameGenerator.generate(classNode.name) : "stringpool";

				// We shouldn't use parallelStream() on here because type of mappings is HashMap which is have multi-thread issues.
				this.getClassWrappers().stream().filter(classWrapper -> !this.isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.stream().filter(methodWrapper -> !this.isExcluded(methodWrapper) && this.hasInstructions(methodWrapper.methodNode)).map(methodWrapper -> methodWrapper.methodNode).forEach(methodNode ->
				{
					for (final AbstractInsnNode insn : methodNode.instructions.toArray())
						if (insn instanceof LdcInsnNode)
						{
							final Object cst = ((LdcInsnNode) insn).cst;
							if (cst instanceof String)
							{
								final String str = (String) cst;
								if (!this.isStringExcluded(str))
								{
									final int indexNumber = RandomUtils.getRandomIntWithExclusion(0, stringCount.get(), alreadyUsed);
									mappings.put(indexNumber, str);
									methodNode.instructions.insertBefore(insn, new FieldInsnNode(GETSTATIC, classPath, fieldName, "[Ljava/lang/String;"));
									methodNode.instructions.insertBefore(insn, ASMUtils.getNumberInsn(indexNumber));
									methodNode.instructions.set(insn, new InsnNode(AALOAD));
									alreadyUsed.add(indexNumber);
									pooledStringCount.incrementAndGet();
								}
							}
						}
				}));

				if (!mappings.isEmpty())
				{
					final String methodName = this.isRenamerEnabled() ? this.parameter.methodNameGenerator.generate(classNode.name) : "poolString";

					classNode.visit(V1_5, ACC_PUBLIC + ACC_SUPER + ACC_SYNTHETIC, classPath, null, "java/lang/Object", null);

					classNode.methods.add(generateStringPoolMethod(classNode.name, methodName, fieldName, mappings));

					final MethodNode clinit = new MethodNode(ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC, "<clinit>", "()V", null, null);
					final InsnList insns = new InsnList();
					insns.add(new MethodInsnNode(INVOKESTATIC, classNode.name, methodName, "()V", false));
					insns.add(new InsnNode(RETURN));
					clinit.instructions = insns;
					classNode.methods.add(clinit);

					final FieldNode stringPoolField = new FieldNode(ACC_PUBLIC + ACC_STATIC + ACC_SYNTHETIC, fieldName, "[Ljava/lang/String;", null, null);
					classNode.fields.add(stringPoolField);

					this.getClasses().put(classNode.name, new ClassWrapper(classNode, false));
				}
				break;
			}
			case Local:
			{
				this.getClassWrappers().parallelStream().filter(classWrapper -> !this.isExcluded(classWrapper)).forEach(classWrapper ->
				{
					final Map<Integer, String> mappings = new HashMap<>();
					final String methodName = this.isRenamerEnabled() ? this.parameter.methodNameGenerator.generate(classWrapper.originalName) : "poolString";
					final String fieldName = this.isRenamerEnabled() ? this.parameter.fieldNameGenerator.generate(classWrapper.originalName) : "stringpool";

					final AtomicInteger stringSize = new AtomicInteger();
					final Set<Integer> alreadyUsed = new HashSet<>();
					classWrapper.methods.parallelStream().filter(methodWrapper -> !this.isExcluded(methodWrapper) && this.hasInstructions(methodWrapper.methodNode)).map(methodWrapper -> methodWrapper.methodNode).forEach(methodNode ->
					{
						for (final AbstractInsnNode insn : methodNode.instructions.toArray())
							if (insn instanceof LdcInsnNode)
							{
								final Object cst = ((LdcInsnNode) insn).cst;
								if (cst instanceof String)
								{
									final String str = (String) cst;
									if (!this.isStringExcluded(str))
										stringSize.incrementAndGet();
								}
							}

						for (final AbstractInsnNode insn : methodNode.instructions.toArray())
							if (insn instanceof LdcInsnNode)
							{
								final Object cst = ((LdcInsnNode) insn).cst;
								if (cst instanceof String)
								{
									final String str = (String) cst;
									if (!this.isStringExcluded(str))
									{
										final int indexNumber = RandomUtils.getRandomIntWithExclusion(0, stringSize.get(), alreadyUsed);
										mappings.put(indexNumber, str);
										methodNode.instructions.insertBefore(insn, new FieldInsnNode(GETSTATIC, classWrapper.classNode.name, fieldName, "[Ljava/lang/String;"));
										methodNode.instructions.insertBefore(insn, ASMUtils.getNumberInsn(indexNumber));
										methodNode.instructions.set(insn, new InsnNode(AALOAD));
										alreadyUsed.add(indexNumber);
										pooledStringCount.incrementAndGet();
									}
								}
							}
					});

					if (!mappings.isEmpty())
					{
						classWrapper.classNode.methods.add(generateStringPoolMethod(classWrapper.classNode.name, methodName, fieldName, mappings));

						MethodNode clinit = classWrapper.classNode.methods.stream().filter(methodNode -> "<clinit>".equals(methodNode.name)).findFirst().orElse(null);
						if (clinit == null)
						{
							clinit = new MethodNode(ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC, "<clinit>", "()V", null, null);
							final InsnList insns = new InsnList();
							insns.add(new MethodInsnNode(INVOKESTATIC, classWrapper.classNode.name, methodName, "()V", false));
							insns.add(new InsnNode(RETURN));
							clinit.instructions = insns;
							classWrapper.classNode.methods.add(clinit);
						}
						else
							clinit.instructions.insertBefore(clinit.instructions.getFirst(), new MethodInsnNode(INVOKESTATIC, classWrapper.classNode.name, methodName, "()V", false));

						final FieldNode stringPoolField = new FieldNode(ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC, fieldName, "[Ljava/lang/String;", null, null);
						if (classWrapper.classNode.fields == null)
							classWrapper.classNode.fields = new ArrayList<>();
						classWrapper.classNode.fields.add(stringPoolField);
					}
				});
				break;
			}
			case Random:
			{
				final AtomicInteger totalStringCount = new AtomicInteger(0);
				final AtomicInteger mappingsCount = new AtomicInteger();
				final Predicate<ClassWrapper> pred = classWrapper2 -> !this.isExcluded(classWrapper2) && AccessUtils.canCreateStaticField(classWrapper2.classNode);
				final int cwSize = evalClassesCount(pred);

				this.getClassWrappers().parallelStream().filter(classWrapper -> !this.isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.parallelStream().filter(methodWrapper -> !this.isExcluded(methodWrapper) && this.hasInstructions(methodWrapper.methodNode)).map(methodWrapper -> methodWrapper.methodNode).forEach(methodNode ->
				{
					for (final AbstractInsnNode insn : methodNode.instructions.toArray())
						if (insn instanceof LdcInsnNode)
						{
							final Object cst = ((LdcInsnNode) insn).cst;
							if (cst instanceof String)
							{
								final String str = (String) cst;
								if (!this.isStringExcluded(str))
									totalStringCount.incrementAndGet();
							}
						}
				}));
				final Map<String, Integer> allocations = new HashMap<>();
				final Map<String, Set<Integer>> usedindexes = new HashMap<>();
				final Map<String, HashMap<Integer, String>> mappings = new HashMap<>(cwSize);
				final Map<String, String> fieldNames = new HashMap<>(totalStringCount.get());

				// We shouldn't use parallelStream() on here because type of mappings is HashMap which is have multi-thread issues.
				this.getClassWrappers().stream().filter(pred).forEach(classWrapper ->
				{
					final int mod = totalStringCount.get() % cwSize;
					final int count = (totalStringCount.get() - mod) / cwSize;

					int _count = count;

					if (mod != 0)
					{
						_count += mod;

						totalStringCount.addAndGet(-mod);
					}
					allocations.put(classWrapper.originalName, _count);
				});

				this.getClassWrappers().stream().filter(classWrapper -> !this.isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.stream().filter(methodWrapper -> !this.isExcluded(methodWrapper) && this.hasInstructions(methodWrapper.methodNode)).map(methodWrapper -> methodWrapper.methodNode).forEach(methodNode ->
				{
					for (final AbstractInsnNode insn : methodNode.instructions.toArray())
						if (insn instanceof LdcInsnNode)
						{
							final Object cst = ((LdcInsnNode) insn).cst;
							if (cst instanceof String)
							{
								final String string = (String) cst;
								if (!this.isStringExcluded(string))
								{
									final String classPath = RandomUtils.getRandomClassCanInject(this, this.getClassWrappers(), classWrapper2 ->
									{
										if (!usedindexes.containsKey(classWrapper2.originalName))
											return true;
										return usedindexes.get(classWrapper2.originalName).size() < allocations.get(classWrapper2.originalName);
									}).originalName;
									final String fieldName = this.isRenamerEnabled() ? this.parameter.fieldNameGenerator.generate(classPath) : "stringpool";

									final int indexNumber = RandomUtils.getRandomIntWithExclusion(0, allocations.get(classPath), usedindexes.get(classPath));
									if (!mappings.containsKey(classPath))
										mappings.put(classPath, new HashMap<>(allocations.get(classPath)));
									mappings.get(classPath).put(indexNumber, string);

									methodNode.instructions.insertBefore(insn, new FieldInsnNode(GETSTATIC, classPath, fieldName, "[Ljava/lang/String;"));
									methodNode.instructions.insertBefore(insn, ASMUtils.getNumberInsn(indexNumber));
									methodNode.instructions.set(insn, new InsnNode(AALOAD));
									fieldNames.put(classPath, fieldName);
									pooledStringCount.incrementAndGet();

									if (!usedindexes.containsKey(classPath))
										usedindexes.put(classPath, new HashSet<>());
									usedindexes.get(classPath).add(indexNumber);
								}
							}
						}
				}));

				if (!mappings.isEmpty())
				{
					this.getClassWrappers().stream().filter(pred).forEach(classWrapper ->
					{
						final String methodName = this.isRenamerEnabled() ? this.parameter.methodNameGenerator.generate(classWrapper.originalName) : "poolString";
						final String fieldName = fieldNames.get(classWrapper.originalName);
						if (fieldName != null)
						{
							// classNode.methods.add(generateStringPools(classNode.name, methodName, fieldName, mappings));

							final Map<Integer, String> sortedMappings;
							final List<Integer> sortedMappingKeySet = new ArrayList<>();

							for (Map.Entry<String, HashMap<Integer, String>> entry : mappings.entrySet())
								if (entry.getKey().equalsIgnoreCase(classWrapper.originalName))
									sortedMappingKeySet.addAll(entry.getValue().keySet());

							sortedMappingKeySet.sort(Comparator.naturalOrder());

							sortedMappings = sortedMappingKeySet.stream().mapToInt(i -> i).boxed().collect(Collectors.toMap(i -> i, i -> mappings.get(classWrapper.originalName).get(i), (a, b) -> b));

							final MethodNode method = new MethodNode(ACC_PRIVATE + ACC_STATIC + ACC_BRIDGE + ACC_SYNTHETIC, methodName, "()V", null, null);
							method.visitCode();

							final int poolSize = sortedMappings.size();
							if (poolSize <= 5)
								method.visitInsn(poolSize + 3);
							else if (poolSize <= 127)
								method.visitIntInsn(BIPUSH, poolSize);
							else if (poolSize <= 32767)
								method.visitIntInsn(SIPUSH, poolSize);
							else
								method.visitLdcInsn(poolSize);
							method.visitTypeInsn(ANEWARRAY, "java/lang/String");

							for (int index = 0; index < poolSize; index++)
							{
								final String value = sortedMappings.get(index);
								method.visitInsn(DUP);
								if (index <= 5)
									method.visitInsn(index + 3);
								else if (index <= 127)
									method.visitIntInsn(BIPUSH, index);
								else if (index <= 32767)
									method.visitIntInsn(SIPUSH, index);
								else
									method.visitLdcInsn(index);
								if (value == null)
									method.visitInsn(ACONST_NULL);
								else
									method.visitLdcInsn(value);
								method.visitInsn(AASTORE);
							}
							method.visitFieldInsn(PUTSTATIC, classWrapper.originalName, fieldName, "[Ljava/lang/String;");
							method.visitInsn(RETURN);
							method.visitMaxs(3, 0);
							method.visitEnd();
							classWrapper.classNode.methods.add(method);

							MethodNode clinit = classWrapper.classNode.methods.stream().filter(methodNode2 -> "<clinit>".equals(methodNode2.name)).findFirst().orElse(null);
							if (clinit == null)
							{
								clinit = new MethodNode(ACC_PRIVATE + ACC_STATIC + ACC_SYNTHETIC, "<clinit>", "()V", null, null);
								final InsnList insns = new InsnList();
								insns.add(new MethodInsnNode(INVOKESTATIC, classWrapper.classNode.name, methodName, "()V", false));
								insns.add(new InsnNode(RETURN));
								clinit.instructions = insns;
								classWrapper.classNode.methods.add(clinit);
							}
							else
								clinit.instructions.insertBefore(clinit.instructions.getFirst(), new MethodInsnNode(INVOKESTATIC, classWrapper.classNode.name, methodName, "()V", false));

							if (classWrapper.classNode.fields == null)
								classWrapper.classNode.fields = new ArrayList<>();
							final FieldNode stringPoolField = new FieldNode(ACC_PUBLIC + ACC_STATIC + ACC_SYNTHETIC, fieldName, "[Ljava/lang/String;", null, null);
							classWrapper.classNode.fields.add(stringPoolField);
							mappingsCount.incrementAndGet();
						}
					});
					Logger.stdOut(String.format("generated %d mappings", mappingsCount.get()));
				}
				break;
			}
		}

		Logger.stdOut(String.format("Pooled %d strings.", pooledStringCount.get()));
	}

	private int evalClassesCount(final Predicate<ClassWrapper> pred)
	{
		return (int) this.getClassWrappers().parallelStream().distinct().filter(pred).count();
	}

	private static final MethodNode generateStringPoolMethod(final String className, final String methodName, final String fieldName, final Map<Integer, String> mappings)
	{
		final Map<Integer, String> sortedMappings;
		final List<Integer> sortedMappingKeySet = new ArrayList<>(mappings.keySet());

		sortedMappingKeySet.sort(Comparator.naturalOrder());

		sortedMappings = sortedMappingKeySet.stream().mapToInt(map -> map).boxed().collect(Collectors.toMap(map -> map, mappings::get, (a, b) -> b));

		final MethodNode method = new MethodNode(ACC_PRIVATE + ACC_STATIC + ACC_BRIDGE + ACC_SYNTHETIC, methodName, "()V", null, null);
		method.visitCode();
		final int numberOfStrings = mappings.size();
		if (numberOfStrings <= 5)
			method.visitInsn(numberOfStrings + 3);
		else if (numberOfStrings <= 127)
			method.visitIntInsn(BIPUSH, numberOfStrings);
		else if (numberOfStrings <= 32767)
			method.visitIntInsn(SIPUSH, numberOfStrings);
		else
			method.visitLdcInsn(numberOfStrings);
		method.visitTypeInsn(ANEWARRAY, "java/lang/String");

		for (int index = 0, j = mappings.size(); index < j; index++)
		{
			final String value = sortedMappings.get(index);
			method.visitInsn(DUP);
			if (index <= 5)
				method.visitInsn(index + 3);
			else if (index <= 127)
				method.visitIntInsn(BIPUSH, index);
			else if (index <= 32767)
				method.visitIntInsn(SIPUSH, index);
			else
				method.visitLdcInsn(index);
			if (value == null)
				method.visitInsn(ACONST_NULL);
			else
				method.visitLdcInsn(value);
			method.visitInsn(AASTORE);
		}

		method.visitFieldInsn(PUTSTATIC, className, fieldName, "[Ljava/lang/String;");
		method.visitInsn(RETURN);
		method.visitMaxs(3, 0);
		method.visitEnd();
		return method;
	}

	@Override
	public final ExclusionType getExclusionType()
	{
		return ExclusionType.STRING_POOL;
	}

	@Override
	public final String getName()
	{
		return "String pool";
	}
}
