package com.eric0210.obfuscater.transformer.obfuscator;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

public class MemberShuffler extends Transformer
{
	public boolean shuffleMethods, shuffleFields;

	@Override
	public final void transform()
	{
		final AtomicInteger affected_classes = new AtomicInteger();
		final AtomicInteger affected_methods = new AtomicInteger();
		final AtomicInteger affected_fields = new AtomicInteger();
		this.getClassWrappers().stream().filter(classWrapper -> !this.isExcluded(classWrapper)).forEach(classWrapper ->
		{
			// Class is modified?
			boolean modified = false;

			// Shuffle methods
			if (shuffleMethods && classWrapper.classNode.methods.size() > 1)
			{
				Collections.shuffle(classWrapper.classNode.methods);
				affected_methods.addAndGet(classWrapper.classNode.methods.size());
				modified = true;
			}

			// Shuffle fields
			if (shuffleFields && classWrapper.classNode.fields != null && classWrapper.classNode.fields.size() > 1)
			{
				Collections.shuffle(classWrapper.classNode.fields);
				affected_fields.addAndGet(classWrapper.classNode.fields.size());
				modified = true;
			}

			if (modified)
				affected_classes.incrementAndGet();
		});

		Logger.stdOut(String.format("Shuffled %d methods and %d fields in %d classes.", affected_methods.get(), affected_fields.get(), affected_classes.get()));
	}

	@Override
	public final ExclusionType getExclusionType()
	{
		return ExclusionType.SHUFFLER;
	}

	@Override
	public final String getName()
	{
		return "Member Ordinal Shuffler";
	}
}
