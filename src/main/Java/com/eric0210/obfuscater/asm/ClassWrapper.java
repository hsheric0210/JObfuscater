package com.eric0210.obfuscater.asm;

import java.util.ArrayList;

import org.objectweb.asm.tree.ClassNode;

public class ClassWrapper
{
	public ClassNode classNode;
	public final String originalName;
	public final boolean libraryNode;
	public final ArrayList<MethodWrapper> methods = new ArrayList<>();
	public final ArrayList<FieldWrapper> fields = new ArrayList<>();

	public ClassWrapper(final ClassNode classNode, final boolean libraryNode)
	{
		this.classNode = classNode;
		this.originalName = classNode.name;
		this.libraryNode = libraryNode;
		final ClassWrapper instance = this;
		classNode.methods.forEach(methodNode -> this.methods.add(new MethodWrapper(methodNode, instance, methodNode.name, methodNode.desc)));
		if (classNode.fields != null)
			classNode.fields.forEach(fieldNode -> this.fields.add(new FieldWrapper(fieldNode, instance, fieldNode.name, fieldNode.desc)));
	}
}
