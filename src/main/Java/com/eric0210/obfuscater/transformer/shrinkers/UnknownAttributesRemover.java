package com.eric0210.obfuscater.transformer.shrinkers;

import com.eric0210.obfuscater.Logger;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.tree.ClassNode;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Removes all unknown attributes from the classes.
 *
 * @author ItzSomebody
 */
public class UnknownAttributesRemover extends Shrinker
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> isExcluded(classWrapper) && classWrapper.classNode.attrs != null).forEach(classWrapper ->
		{
			ClassNode classNode = classWrapper.classNode;

			Stream.of(classNode.attrs.toArray(new Attribute[0])).filter(Attribute::isUnknown).forEach(attr ->
			{
				classNode.attrs.remove(attr);
				counter.incrementAndGet();
			});
		});

		Logger.stdOut(String.format("Removed %d attributes.", counter.get()));
	}

	@Override
	public String getName()
	{
		return "Attributes Remover";
	}
}
