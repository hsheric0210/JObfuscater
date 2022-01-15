package com.eric0210.obfuscater.transformer;

import java.util.*;

import com.eric0210.obfuscater.Obfuscater;
import com.eric0210.obfuscater.asm.ClassWrapper;
import com.eric0210.obfuscater.asm.FieldWrapper;
import com.eric0210.obfuscater.asm.MethodWrapper;
import com.eric0210.obfuscater.utils.RandomUtils;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.CodeSizeEvaluator;
import org.objectweb.asm.tree.MethodNode;

public abstract class Transformer implements Opcodes
{
	protected Obfuscater obfuscator;
	private static final HashSet<String> used_class_names_nongenerated = new HashSet<>();

	public final void init(final Obfuscater obfuscatorInstance)
	{
		this.obfuscator = obfuscatorInstance;

		used_class_names_nongenerated.clear();
	}

	protected final boolean isRenamerEnabled()
	{
		return this.obfuscator.config.renamerEnabled;
	}

	protected final boolean isHideCodeEnabled()
	{
		return this.obfuscator.config.hideCodeEnabled;
	}

	public final boolean isExcluded(final String str)
	{
		return this.obfuscator.config.exclusionManager.isExcluded(str, this.getExclusionType());
	}

	public final boolean isExcluded(final ClassWrapper classWrapper)
	{
		return this.isExcluded(classWrapper.originalName);
	}

	public final boolean isExcluded(final MethodWrapper methodWrapper)
	{
		return this.isExcluded(methodWrapper.owner.originalName + '.' + methodWrapper.originalName + methodWrapper.originalDescription);
	}

	public final boolean isExcluded(final FieldWrapper fieldWrapper)
	{
		return this.isExcluded(fieldWrapper.owner.originalName + '.' + fieldWrapper.originalName + '.' + fieldWrapper.originalDescription);
	}

	protected final int getSizeLeeway(final MethodNode method)
	{
		final CodeSizeEvaluator sizeEvaluator = new CodeSizeEvaluator(null);
		method.accept(sizeEvaluator);

		// Max allowed method size is 65535
		// https://docs.oracle.com/javase/specs/jvms/se10/html/jvms-4.html#jvms-4.7.3
		return 65535 - sizeEvaluator.getMaxSize();
	}

	protected final boolean hasInstructions(final MethodNode methodNode)
	{
		return methodNode.instructions != null && methodNode.instructions.size() > 0;
	}

	protected final boolean hasInstructions(final MethodWrapper methodWrapper)
	{
		return hasInstructions(methodWrapper.methodNode);
	}

	protected final long tookThisLong(final long from)
	{
		return System.currentTimeMillis() - from;
	}

	public final String randomClassPath(final Collection<String> classNames)
	{
		final ArrayList<String> list = new ArrayList<>(classNames);
		list.removeIf(classname -> used_class_names_nongenerated.contains(classname));
		final String first = list.get(RandomUtils.getRandomInt(classNames.size()));

		final int lastindexsep = first.lastIndexOf('/');
		if (lastindexsep == -1)
			return "/";
		return first.substring(0, lastindexsep) + '/';
	}

	protected final Map<String, ClassWrapper> getClasses()
	{
		return this.obfuscator.classes;
	}

	protected final Collection<ClassWrapper> getClassWrappers()
	{
		return this.obfuscator.classes.values();
	}

	protected final Map<String, ClassWrapper> getClassPath()
	{
		return this.obfuscator.classPath;
	}

	protected final Map<String, byte[]> getResources()
	{
		return this.obfuscator.resources;
	}

	public abstract void transform();

	public abstract String getName();

	public abstract ExclusionType getExclusionType();

	public abstract Map<String, Object> getConfiguration();

	public abstract void setConfiguration(Map<String, Object> config);

	public abstract void verifyConfiguration(Map<String, Object> config);

	/**
	 * Insertion sorts the provided {@link List<Transformer>} using the {@link ExclusionType} ordinal as the priority key. O(n^2) here we come \o/
	 *
	 * @param transformers
	 *                     @link List<Transformer>} to be sorted.
	 */
	public static void sort(List<Transformer> transformers)
	{
		if (transformers.size() < 2) // Already sorted
			return;

		for (int i = 1; i < transformers.size(); i++)
		{
			Transformer transformer = transformers.get(i);
			int key = transformer.getExclusionType().ordinal();

			int j = i - 1;
			while (j >= 0 && transformers.get(j).getExclusionType().ordinal() > key)
			{
				transformers.set(j + 1, transformers.get(j));
				j -= 1;
			}

			transformers.set(j + 1, transformer);
		}
	}
}
