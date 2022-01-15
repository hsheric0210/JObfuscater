package com.eric0210.obfuscater.transformer.obfuscator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.ObfuscatorGUI;
import com.eric0210.obfuscater.asm.*;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.AccessUtils;
import com.eric0210.obfuscater.utils.FileUtils;
import com.eric0210.obfuscater.utils.StringGenerator;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

import org.objectweb.asm.Handle;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.*;

public class Renamer extends Transformer
{
	public boolean randomizeClassname;
	public StringGenerator classNameGenerator;
	public boolean randomizePackage;
	public StringGenerator packageNameGenerator;
	public boolean randomizeMethodname;
	public StringGenerator methodNameGenerator;
	public boolean randomizeFieldname;
	public StringGenerator fieldNameGenerator;

	public boolean repackage;
	public String repackageName;
	public boolean obfuscateRepackage;

	public String[] resourcesToAdapt;

	private final HashMap<String, String> mappings = new HashMap<>();
	private final HashMap<Map.Entry<String, Integer>, String> package_mappings = new HashMap<>();

	@Override
	public final void transform()
	{
		Logger.stdOut("Generating mappings.");
		long current = System.currentTimeMillis();
		final AtomicInteger methodMappings = new AtomicInteger();
		final AtomicInteger fieldMappings = new AtomicInteger();
		final AtomicInteger packageMappings = new AtomicInteger();

		this.getClassWrappers().forEach(classWrapper ->
		{
			// Generate mappings for methods
			if (this.randomizeMethodname)
				classWrapper.methods.stream().filter(methodWrapper -> !AccessUtils.isNative(methodWrapper.methodNode.access) && !methodWrapper.methodNode.name.equals("main") && !methodWrapper.methodNode.name.equals("premain") && !(!methodWrapper.methodNode.name.isEmpty() && methodWrapper.methodNode.name.charAt(0) == '<')).filter(methodWrapper -> this.canRenameMethodTree(new HashSet<>(), methodWrapper, classWrapper.originalName)).forEach(methodWrapper ->
				{
					this.renameMethodTree(new HashSet<>(), methodWrapper, classWrapper.originalName, this.methodNameGenerator.generate(classWrapper.originalName));
					methodMappings.incrementAndGet();
				});

			// Generate mappings for fields
			if (this.randomizeFieldname)
				classWrapper.fields.stream().filter(fieldWrapper -> this.canRenameFieldTree(new HashSet<>(), fieldWrapper, classWrapper.originalName)).forEach(fieldWrapper ->
				{
					this.renameFieldTree(new HashSet<>(), fieldWrapper, classWrapper.originalName, this.fieldNameGenerator.generate(classWrapper.originalName));
					fieldMappings.incrementAndGet();
				});

			if (this.repackage)
			{
				if (!this.isExcluded(classWrapper))
				{
					String repackageName = this.repackageName;
					if (this.obfuscateRepackage)
						repackageName = this.packageNameGenerator.generate();
					final String classname = this.randomizeClassname ? this.classNameGenerator.generate() : this.getClassname(classWrapper.originalName);
					this.mappings.put(classWrapper.originalName, Optional.ofNullable(repackageName).map(name -> name + '/' + classname).orElse(classname));
					packageMappings.incrementAndGet();
				}
			}
			else if (!this.isExcluded(classWrapper))
			{
				final String[] packagePieces = classWrapper.originalName.split("/");
				final int packageCount = packagePieces.length - 1;
				final StringBuilder pathBuilder = new StringBuilder();

				// Generate mappings for each package piece
				for (int i = 0; i < packageCount; i++)
				{
					final String currentPackagePiece = packagePieces[i];

					if (!this.package_mappings.containsKey(this.newSimpleEntry(currentPackagePiece, i)))
						if (this.randomizePackage)
							this.package_mappings.put(this.newSimpleEntry(currentPackagePiece, i), this.packageNameGenerator.generate());
						else
							this.package_mappings.put(this.newSimpleEntry(currentPackagePiece, i), currentPackagePiece);

					pathBuilder.append(this.package_mappings.get(this.newSimpleEntry(currentPackagePiece, i)));
					pathBuilder.append('/');
				}

				// Generate mappings for the class
				if (this.randomizeClassname)
					pathBuilder.append(this.classNameGenerator.generate());
				else
					pathBuilder.append(this.getClassname(classWrapper.originalName));

				this.mappings.put(classWrapper.originalName, pathBuilder.toString());
				packageMappings.incrementAndGet();
			}
		});

		Logger.stdOut(String.format("Finished generate %d method and %d field and %d package mappings. [%dms]", methodMappings.get(), fieldMappings.get(), packageMappings.get(), this.tookThisLong(current)));
		Logger.stdOut("Applying mappings.");
		current = System.currentTimeMillis();

		// Apply mapping
		final Remapper remapper = new MemberRemapper(this.mappings);
		for (final ClassWrapper classWrapper : new ArrayList<>(this.getClassWrappers()))
		{
			final ClassNode classNode = classWrapper.classNode;
			final ClassNode copy = new ClassNode();
			classNode.accept(new ClassRemapper(copy, remapper));
			for (int i = 0, j = copy.methods.size(); i < j; i++)
			{
				classWrapper.methods.get(i).methodNode = copy.methods.get(i);

				// Workaround for lambdas
				// TODO: Fix lambdas + interface
				for (final AbstractInsnNode insn : classWrapper.methods.get(i).methodNode.instructions.toArray())
					if (insn instanceof InvokeDynamicInsnNode)
					{
						final InvokeDynamicInsnNode invdyn = (InvokeDynamicInsnNode) insn;
						if (invdyn.bsm.getOwner().equals("java/lang/invoke/LambdaMetafactory"))
						{
							final Handle handle = (Handle) invdyn.bsmArgs[1];
							final String newName = this.mappings.get(handle.getOwner() + '.' + handle.getName() + handle.getDesc());
							if (newName != null)
							{
								invdyn.name = newName;
								invdyn.bsm = new Handle(handle.getTag(), handle.getOwner(), newName, handle.getDesc(), false);
							}
						}
					}
			}

			if (copy.fields != null)
				for (int i = 0, j = copy.fields.size(); i < j; i++)
					classWrapper.fields.get(i).fieldNode = copy.fields.get(i);

			classWrapper.classNode = copy;
			this.getClasses().remove(classWrapper.originalName);
			this.getClasses().put(classWrapper.classNode.name, classWrapper);
			this.getClassPath().put(classWrapper.classNode.name, classWrapper);
		}
		Logger.stdOut(String.format("Mapped %d members. [%dms]", this.mappings.size(), this.tookThisLong(current)));
		current = System.currentTimeMillis();

		// Fix screw ups in resources.
		Logger.stdOut("Attempting to map class names in resources");

		final AtomicInteger affected_resources = new AtomicInteger();
		this.getResources().forEach((resName, byteArray) ->
		{
			if (this.resourcesToAdapt != null)
				for (final String s : this.resourcesToAdapt)
				{
					// LoggerUtils.stdOut("Found resource " + s);
					final Pattern pattern = Pattern.compile(s);
					if (pattern.matcher(resName).matches())
					{
						String resData = new String(byteArray);
						for (final Map.Entry<String, String> entry : this.mappings.entrySet())
						{
							String original = entry.getKey().replace("/", ".");
							original = original.replace("\\", ".");
							if (resData.contains(original))
							{
								// Regex that ensures that class names that match words in the manifest don't break the manifest. Example: name == Main
								if ("META-INF/MANIFEST.MF".equals(resName) || "plugin.yml".equals(resName) || "bungee.yml".equals(resName))
									resData = resData.replaceAll("(?<=[: ])" + original, entry.getValue().replace("/", ".").replace("\\", "."));
								else
									resData = resData.replace(original, entry.getValue().replace("/", ".").replace("\\", "."));
								affected_resources.incrementAndGet();
							}
						}
						this.getResources().put(resName, resData.getBytes(StandardCharsets.UTF_8));
					}
				}
		});
		Logger.stdOut(String.format("Adapted %d names in resources. [%dms]", affected_resources.get(), this.tookThisLong(current)));
		this.dumpMappings();
	}

	private boolean canRenameMethodTree(final HashSet<ClassTree> visited, final MethodWrapper methodWrapper, final String owner)
	{
		final ClassTree tree = this.obfuscator.getTree(owner);
		if (tree == null)
			return false;
		if (!visited.contains(tree))
		{
			visited.add(tree);
			if (this.isExcluded(owner + '.' + methodWrapper.originalName + methodWrapper.originalDescription))
				return false;
			if (this.mappings.containsKey(owner + '.' + methodWrapper.originalName + methodWrapper.originalDescription))
				return true;
			if (!methodWrapper.owner.originalName.equals(owner) && tree.classWrapper.libraryNode)
				for (final MethodNode mn : tree.classWrapper.classNode.methods)
					if (mn.name.equals(methodWrapper.originalName) && mn.desc.equals(methodWrapper.originalDescription))
						return false;
			for (final String parent : tree.parentClasses)
				if (parent != null && !this.canRenameMethodTree(visited, methodWrapper, parent))
					return false;
			return tree.subClasses.stream().noneMatch(sub -> sub != null && !this.canRenameMethodTree(visited, methodWrapper, sub));
		}
		return true;
	}

	private void renameMethodTree(final HashSet<ClassTree> visited, final MethodWrapper MethodWrapper, final String className, final String newName)
	{
		final ClassTree tree = this.obfuscator.getTree(className);
		if (!tree.classWrapper.libraryNode && !visited.contains(tree))
		{
			this.mappings.put(className + '.' + MethodWrapper.originalName + MethodWrapper.originalDescription, newName);
			visited.add(tree);
			for (final String parentClass : tree.parentClasses)
				this.renameMethodTree(visited, MethodWrapper, parentClass, newName);
			for (final String subClass : tree.subClasses)
				this.renameMethodTree(visited, MethodWrapper, subClass, newName);
		}
	}

	private boolean canRenameFieldTree(final HashSet<ClassTree> visited, final FieldWrapper fieldWrapper, final String owner)
	{
		final ClassTree tree = this.obfuscator.getTree(owner);
		if (tree == null)
			return false;
		if (!visited.contains(tree))
		{
			visited.add(tree);
			if (this.isExcluded(owner + '.' + fieldWrapper.originalName + '.' + fieldWrapper.originalDescription))
				return false;
			if (this.mappings.containsKey(owner + '.' + fieldWrapper.originalName + '.' + fieldWrapper.originalDescription))
				return true;
			if (!fieldWrapper.owner.originalName.equals(owner) && tree.classWrapper.libraryNode)
				for (final FieldNode fn : tree.classWrapper.classNode.fields)
					if (fieldWrapper.originalName.equals(fn.name) && fieldWrapper.originalDescription.equals(fn.desc))
						return false;
			for (final String parent : tree.parentClasses)
				if (parent != null && !this.canRenameFieldTree(visited, fieldWrapper, parent))
					return false;
			return tree.subClasses.stream().noneMatch(sub -> sub != null && !this.canRenameFieldTree(visited, fieldWrapper, sub));
		}
		return true;
	}

	private void renameFieldTree(final HashSet<ClassTree> visited, final FieldWrapper fieldWrapper, final String owner, final String newName)
	{
		final ClassTree tree = this.obfuscator.getTree(owner);
		if (!tree.classWrapper.libraryNode && !visited.contains(tree))
		{
			this.mappings.put(owner + '.' + fieldWrapper.originalName + '.' + fieldWrapper.originalDescription, newName);
			visited.add(tree);
			for (final String parentClass : tree.parentClasses)
				this.renameFieldTree(visited, fieldWrapper, parentClass, newName);
			for (final String subClass : tree.subClasses)
				this.renameFieldTree(visited, fieldWrapper, subClass, newName);
		}
	}

	private void dumpMappings()
	{
		final long current = System.currentTimeMillis();
		Logger.stdOut("Dumping mappings.");
		final File file = new File("mappings.map");
		if (file.exists())
			FileUtils.renameExistingFile(file);
		try
		{
			file.createNewFile();
			final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			this.mappings.forEach((oldName, newName) ->
			{
				try
				{
					bw.append(oldName).append(" -> ").append(newName).append('\n');
				}
				catch (final IOException ioe)
				{
					Logger.stdErr(String.format("Ran into an error trying to append \"%s -> %s\"", oldName, newName));
					Logger.stdErr(ObfuscatorGUI.getExceptionStackTrace(ioe));
				}
			});
			bw.close();
			Logger.stdOut(String.format("Finished dumping mappings at %s. [%dms]", file.getAbsolutePath(), this.tookThisLong(current)));
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		catch (final Throwable t)
		{
			Logger.stdErr("Ran into an error trying to create the mappings file.");
			Logger.stdErr(ObfuscatorGUI.getExceptionStackTrace(t));
		}
		if (!this.package_mappings.isEmpty())
		{
			Logger.stdOut("Dumping package mappings.");
			final File file2 = new File("package_mappings.map");
			if (file2.exists())
				FileUtils.renameExistingFile(file2);
			try
			{
				file2.createNewFile();
				final BufferedWriter bw = new BufferedWriter(new FileWriter(file2));
				this.package_mappings.forEach((directoryIdentifier, renamedDirectory) ->
				{
					final String desc = "(folder name: " + directoryIdentifier.getKey() + ", directoryDepth: " + directoryIdentifier.getValue() + ')';
					try
					{
						bw.append(desc).append(" -> ").append(renamedDirectory).append('\n');
					}
					catch (final IOException ioe)
					{
						Logger.stdErr(String.format("Ran into an error trying to append \"%s -> %s\"", desc, renamedDirectory));
						Logger.stdErr(ObfuscatorGUI.getExceptionStackTrace(ioe));
					}
				});
				bw.close();
				Logger.stdOut(String.format("Finished dumping package mappings at %s. [%dms]", file2.getAbsolutePath(), this.tookThisLong(current)));
			}
			catch (final IOException e)
			{
				e.printStackTrace();
			}
			catch (final Throwable t)
			{
				Logger.stdErr("Ran into an error trying to create the package mappings file.");
				Logger.stdErr(ObfuscatorGUI.getExceptionStackTrace(t));
			}
		}
	}

	@Override
	public final ExclusionType getExclusionType()
	{
		return ExclusionType.RENAMER;
	}

	@Override
	public final String getName()
	{
		return "Renamer";
	}

	private <K, V> Map.Entry<K, V> newSimpleEntry(final K key, final V value)
	{
		return new AbstractMap.SimpleEntry<>(key, value);
	}

	private String getClassname(final String originalName)
	{
		return originalName.substring(originalName.lastIndexOf('/') + 1);
	}
}
