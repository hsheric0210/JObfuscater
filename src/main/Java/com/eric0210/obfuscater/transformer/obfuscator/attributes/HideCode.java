package com.eric0210.obfuscater.transformer.obfuscator.attributes;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.ASMUtils;
import com.eric0210.obfuscater.utils.AccessUtils;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

public class HideCode extends Transformer
{
	public boolean hideClasses, hideMethods, hideFields;

	@Override
	public final void transform()
	{
		final AtomicInteger affected_classes = new AtomicInteger();
		final AtomicInteger affected_methods = new AtomicInteger();
		final AtomicInteger affected_fields = new AtomicInteger();
		final AtomicInteger synthetic_access_added = new AtomicInteger();
		final AtomicInteger bridges_added = new AtomicInteger();
		this.getClassWrappers().stream().filter(classWrapper -> !this.isExcluded(classWrapper)).map(classWrapper -> classWrapper.classNode).forEach(classNode ->
		{
			if (hideClasses && !AccessUtils.isSynthetic(classNode.access) && !ASMUtils.hasAnnotations(classNode))
			{
				classNode.access |= ACC_SYNTHETIC;
				synthetic_access_added.incrementAndGet();
				affected_classes.incrementAndGet();
			}
			if (hideMethods)
				classNode.methods.stream().filter(methodNode -> !ASMUtils.hasAnnotations(methodNode)).forEach(methodNode ->
				{
					boolean modified = false;
					if (!AccessUtils.isSynthetic(methodNode.access))
					{
						methodNode.access |= ACC_SYNTHETIC;
						modified = true;
						synthetic_access_added.incrementAndGet();
					}
					if (!AccessUtils.isBridge(methodNode.access) && !(!methodNode.name.isEmpty() && methodNode.name.charAt(0) == '<')) // can't apply bridge modifier for <init> and <clinit>
					{
						methodNode.access |= ACC_BRIDGE;
						modified = true;
						bridges_added.incrementAndGet();
					}
					if (modified)
						affected_methods.incrementAndGet();
				});

			if (hideFields && classNode.fields != null)
				classNode.fields.stream().filter(fieldNode -> !ASMUtils.hasAnnotations(fieldNode) && !AccessUtils.isSynthetic(fieldNode.access)).forEach(fieldNode ->
				{
					fieldNode.access |= ACC_SYNTHETIC;
					synthetic_access_added.incrementAndGet();
					affected_fields.incrementAndGet();
				});
		});
		Logger.stdOut(String.format("Hid %d classes, %d methods, %d fields members with %d synthetic access modifiers and %d bridge access modifiers.", affected_classes.get(), affected_methods.get(), affected_fields.get(), synthetic_access_added.get(), bridges_added.get()));
	}

	@Override
	public final ExclusionType getExclusionType()
	{
		return ExclusionType.HIDE_CODE;
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

	@Override
	public final String getName()
	{
		return "Hide Code";
	}
}
