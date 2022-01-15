package com.eric0210.obfuscater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.eric0210.obfuscater.asm.ClassTree;
import com.eric0210.obfuscater.asm.ClassWrapper;
import com.eric0210.obfuscater.config.ObfuscationConfiguration;
import com.eric0210.obfuscater.exceptions.MissingClassException;
import com.eric0210.obfuscater.exceptions.ObfuscatorException;
import com.eric0210.obfuscater.transformer.misc.TrashClasses;
import com.eric0210.obfuscater.utils.FileUtils;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class Obfuscater
{
	public final ObfuscationConfiguration config;
	private final Map<String, ClassTree> hierarchy = new HashMap<>();
	public final Map<String, ClassWrapper> classes = new HashMap<>();
	public final Map<String, ClassWrapper> classPath = new HashMap<>();
	public final Map<String, byte[]> resources = new HashMap<>();

	public Obfuscater(final ObfuscationConfiguration config)
	{
		this.config = config;
	}

	public final void run()
	{
		this.loadClassPath();
		this.loadInput();
		this.buildInheritance();
		if (this.config.trashClasses > 0)
			this.config.transformers.add(0, new TrashClasses());
		if (this.config.transformers.isEmpty())
			throw new ObfuscatorException("No transformers are enabled.");

		Logger.stdOut("------------------------------------------------");
		this.config.transformers.stream().filter(Objects::nonNull).forEach(transformer ->
		{
			final long current = System.currentTimeMillis();
			Logger.stdOut(String.format("Running %s transformer.", transformer.getName()));
			transformer.init(this);
			transformer.transform();
			Logger.stdOut(String.format("Finished running %s transformer. [%dms]", transformer.getName(), System.currentTimeMillis() - current));
			Logger.stdOut("------------------------------------------------");
		});
		this.writeOutput();
	}

	private void writeOutput()
	{
		final File output = this.config.output;
		Logger.stdOut(String.format("Writing output to \"%s\".", output.getAbsolutePath()));
		if (output.exists())
			Logger.stdOut(String.format("Output file already exists, renamed to %s.", FileUtils.renameExistingFile(output)));
		try
		{
			final ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(output));
			this.classes.values().forEach(classWrapper ->
			{
				try
				{
					final ZipEntry entry = new ZipEntry(classWrapper.classNode.name + ".class");
					entry.setCompressedSize(config.compressionLevel);
					ClassWriter cw = new CustomClassWriter(ClassWriter.COMPUTE_FRAMES);
					cw.newUTF8(Constants.SIGNATURE + Constants.VERSION);
					try
					{
						classWrapper.classNode.accept(cw);
					}
					catch (final Throwable t)
					{
						Logger.stdErr(String.format("Error writing class %s. Skipping frames.", classWrapper.classNode.name + ".class"));
						Logger.stdErr(ObfuscatorGUI.getExceptionStackTrace(t));
						cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
						cw.newUTF8(Constants.SIGNATURE + Constants.VERSION);
						classWrapper.classNode.accept(cw);
					}
					zos.putNextEntry(entry);
					zos.write(cw.toByteArray());
					zos.closeEntry();
				}
				catch (final IOException t)
				{
					Logger.stdErr(String.format("Error writing class %s. Skipping.", classWrapper.classNode.name + ".class"));
					Logger.stdErr(ObfuscatorGUI.getExceptionStackTrace(t));
				}
			});

			this.resources.forEach((name, bytes) ->
			{
				try
				{
					final ZipEntry entry = new ZipEntry(name);
					entry.setCompressedSize(config.compressionLevel);
					zos.putNextEntry(entry);
					zos.write(bytes);
					zos.closeEntry();
				}
				catch (final IOException t)
				{
					Logger.stdErr(String.format("Error writing resource %s. Skipping.", name));
					Logger.stdErr(ObfuscatorGUI.getExceptionStackTrace(t));
				}
			});
			zos.setComment(this.config.jarFileComment);
			zos.close();
		}
		catch (IOException t)
		{
			Logger.stdErr("An error occurred while writing output file");
			Logger.stdErr(ObfuscatorGUI.getExceptionStackTrace(t));
			throw new ObfuscatorException(t);
		}
	}

	private void loadClassPath()
	{
		this.config.libraries.forEach(file ->
		{
			if (file.exists())
			{
				Logger.stdOut(String.format("Loading library \"%s\".", file.getAbsolutePath()));
				try
				{
					final ZipFile zipFile = new ZipFile(file);
					final Enumeration<? extends ZipEntry> entries = zipFile.entries();
					while (entries.hasMoreElements())
					{
						final ZipEntry entry = entries.nextElement();
						if (!entry.isDirectory() && entry.getName().endsWith(".class"))
							try
							{
								final ClassReader cr = new ClassReader(zipFile.getInputStream(entry));
								final ClassNode classNode = new ClassNode();
								cr.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
								final ClassWrapper classWrapper = new ClassWrapper(classNode, true);
								this.classPath.put(classWrapper.originalName, classWrapper);
							}
							catch (final IOException t)
							{
								Logger.stdErr(String.format("Error while loading library class \"%s\".", entry.getName().replace(".class", "")));
								t.printStackTrace();
							}
					}
					zipFile.close();
				}
				catch (final ZipException e)
				{
					Logger.stdErr(String.format("Library \"%s\" could not be opened as a zip file. Skipping.", file.getAbsolutePath()));
					Logger.stdErr(ObfuscatorGUI.getExceptionStackTrace(e));
				}
				catch (final IOException e)
				{
					Logger.stdErr(String.format("IOException happened while trying to load classes from \"%s\". Skipping.", file.getAbsolutePath()));
					Logger.stdErr(ObfuscatorGUI.getExceptionStackTrace(e));
				}
			}
			else
				Logger.stdWarn(String.format("Library \"%s\" could not be found and will be ignored.", file.getAbsolutePath()));
		});
	}

	private void loadInput()
	{
		final File input = this.config.input;
		if (input.exists())
		{
			Logger.stdOut(String.format("Loading input \"%s\".", input.getAbsolutePath()));
			try
			{
				if (!input.exists())
					input.createNewFile();
				final ZipFile zipFile = new ZipFile(input);
				final Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements())
				{
					final ZipEntry entry = entries.nextElement();
					if (!entry.isDirectory())
						if (entry.getName().endsWith(".class"))
							try
							{
								final ClassReader cr = new ClassReader(zipFile.getInputStream(entry));
								final ClassNode classNode = new ClassNode();
								cr.accept(classNode, ClassReader.SKIP_FRAMES);
								if (classNode.version <= Opcodes.V1_5)
									for (int i = 0, j = classNode.methods.size(); i < j; i++)
									{
										final MethodNode methodNode = classNode.methods.get(i);
										final JSRInlinerAdapter adapter = new JSRInlinerAdapter(methodNode, methodNode.access, methodNode.name, methodNode.desc, methodNode.signature, methodNode.exceptions.toArray(new String[0]));
										methodNode.accept(adapter);
										classNode.methods.set(i, adapter);
									}

								final ClassWrapper classWrapper = new ClassWrapper(classNode, false);
								this.classPath.put(classWrapper.originalName, classWrapper);
								this.classes.put(classWrapper.originalName, classWrapper);
							}
							catch (final IOException t)
							{
								Logger.stdWarn(String.format("Could not load %s as a class.", entry.getName()));
								this.resources.put(entry.getName(), FileUtils.toByteArray(zipFile.getInputStream(entry)));
							}
						else
							this.resources.put(entry.getName(), FileUtils.toByteArray(zipFile.getInputStream(entry)));
				}
				zipFile.close();
			}
			catch (final ZipException e)
			{
				Logger.stdErr(String.format("Input file \"%s\" could not be opened as a zip file.", input.getAbsolutePath()));
				Logger.stdErr(ObfuscatorGUI.getExceptionStackTrace(e));
				throw new BadInputException(e);
			}
			catch (final IOException e)
			{
				Logger.stdErr(String.format("IOException happened while trying to load classes from \"%s\".", input.getAbsolutePath()));
				Logger.stdErr(ObfuscatorGUI.getExceptionStackTrace(e));
				throw new BadInputException(e);
			}
		}
		else
		{
			Logger.stdErr(String.format("Unable to find \"%s\".", input.getAbsolutePath()));
			throw new InputNotFoundException(input.getAbsolutePath(), new FileNotFoundException(input.getAbsolutePath()));
		}
	}

	public final ClassTree getTree(final String ref)
	{
		if (!this.hierarchy.containsKey(ref))
		{
			final ClassWrapper wrapper = this.classPath.get(ref);
			if (wrapper == null)
				return null;
			this.buildHierarchy(wrapper, null);
		}
		return this.hierarchy.get(ref);
	}

	private void buildHierarchy(final ClassWrapper classWrapper, final ClassWrapper sub)
	{
		try
		{
			if (this.hierarchy.get(classWrapper.classNode.name) == null)
			{
				final ClassTree tree = new ClassTree(classWrapper);

				// Check super classes
				if (classWrapper.classNode.superName != null)
				{
					tree.parentClasses.add(classWrapper.classNode.superName);
					final ClassWrapper superClass = this.classPath.get(classWrapper.classNode.superName);
					if (superClass == null)
						throw new MissingClassException("The super class " + classWrapper.classNode.superName + " of " + classWrapper.originalName + " is missing in the classpath.", new ClassNotFoundException(classWrapper.classNode.superName));
					this.buildHierarchy(superClass, classWrapper);
				}

				// Check interfaces
				if (classWrapper.classNode.interfaces != null && !classWrapper.classNode.interfaces.isEmpty())
					classWrapper.classNode.interfaces.forEach(s ->
					{
						tree.parentClasses.add(s);
						final ClassWrapper interfaceClass = this.classPath.get(s);
						if (interfaceClass == null)
							throw new MissingClassException("The interface class " + s + " of " + classWrapper.originalName + " is missing in the classpath.", new ClassNotFoundException(s));
						this.buildHierarchy(interfaceClass, classWrapper);
					});
				this.hierarchy.put(classWrapper.classNode.name, tree);
			}

			if (sub != null)
				this.hierarchy.get(classWrapper.classNode.name).subClasses.add(sub.classNode.name);
		}
		catch (final MissingClassException ex)
		{
			Logger.stdErr("Found missing super class/interface while build hierarchy");
			Logger.stdErr(ObfuscatorGUI.getExceptionStackTrace(ex));
			throw new MissingClassException(ex.getMessage(), ex.getCause());
		}
	}

	private void buildInheritance()
	{
		this.classes.values().forEach(classWrapper -> this.buildHierarchy(classWrapper, null));
	}

	final class CustomClassWriter extends ClassWriter
	{
		CustomClassWriter(final int flags)
		{
			super(flags);
		}

		@Override
		protected String getCommonSuperClass(final String type1, final String type2)
		{
			if ("java/lang/Object".equals(type1) || "java/lang/Object".equals(type2))
				return "java/lang/Object";
			final String first = this.deriveCommonSuperName(type1, type2);
			final String second = this.deriveCommonSuperName(type2, type1);
			if (!"java/lang/Object".equals(first))
				return first;
			if (!"java/lang/Object".equals(second))
				return second;
			return this.getCommonSuperClass(this.returnClazz(type1).superName, this.returnClazz(type2).superName);
		}

		private String deriveCommonSuperName(String type1, final String type2)
		{
			ClassNode first = this.returnClazz(type1);
			final ClassNode second = this.returnClazz(type2);
			if (this.isAssignableFrom(type1, type2))
				return type1;
			else if (this.isAssignableFrom(type2, type1))
				return type2;
			else if (Modifier.isInterface(first.access) || Modifier.isInterface(second.access))
				return "java/lang/Object";
			else
			{
				String temp;
				do
				{
					temp = first.superName;
					first = this.returnClazz(temp);
				}
				while (!this.isAssignableFrom(temp, type2));
				return temp;
			}
		}

		private ClassNode returnClazz(final String ref)
		{
			final ClassWrapper clazz = Obfuscater.this.classPath.get(ref);
			if (clazz == null)
			{
				final MissingClassException mcs = new MissingClassException(ref + " does not exist in classpath!", new ClassNotFoundException(ref));
				Logger.stdErr("Found missing super class/interface while executing returnClazz(String);");
				Logger.stdErr(ObfuscatorGUI.getExceptionStackTrace(mcs));
				throw mcs;
			}
			return clazz.classNode;
		}

		private boolean isAssignableFrom(final String type1, final String type2)
		{
			if ("java/lang/Object".equals(type1))
				return true;
			if (type1.equals(type2))
				return true;
			this.returnClazz(type1);
			this.returnClazz(type2);
			final ClassTree firstTree = Obfuscater.this.getTree(type1);
			if (firstTree == null)
				throw new MissingClassException("Could not find " + type1 + " in the built class hierarchy", new ClassNotFoundException(type1));
			final Set<String> allChildren = new HashSet<>();
			final Deque<String> toProcess = new ArrayDeque<>(firstTree.subClasses);
			while (!toProcess.isEmpty())
			{
				final String s = toProcess.poll();
				if (allChildren.add(s))
				{
					this.returnClazz(s);
					final ClassTree tempTree = Obfuscater.this.getTree(s);
					toProcess.addAll(tempTree.subClasses);
				}
			}
			return allChildren.contains(type2);
		}
	}
}
