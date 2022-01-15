package com.eric0210.obfuscater.transformer.obfuscator.attributes;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.StringGenerator;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

/**
 * Sets the class, method, field signature to a random string. A known trick to work on JD, CFR, Procyon and Javap.
 *
 * @author ItzSomebody
 */
public class SignatureObfuscation extends Transformer
{

	public enum Mode
	{
		Remove("Removed"),
		Randomize("Randomized"),
		Replace("Replaced");

		final String resultMessage;

		Mode(final String resultMessage)
		{
			this.resultMessage = resultMessage;
		}
	}

	public SignatureObfuscation.Mode mode;
	public StringGenerator stringGenerator;
	public String signature;

	@Override
	public final void transform()
	{
		final AtomicInteger affected_classes = new AtomicInteger();
		final AtomicInteger affected_methods = new AtomicInteger();
		final AtomicInteger affected_fields = new AtomicInteger();
		final AtomicInteger affected_localvariables = new AtomicInteger();
		this.getClassWrappers().stream().filter(classWrapper -> !this.isExcluded(classWrapper) && classWrapper.classNode.signature == null).map(classWrapper -> classWrapper.classNode).forEach(classNode ->
		{
			switch (this.mode)
			{
				case Remove:
				{
					// classNode.signature = null;
					// Already null
					break;
				}
				case Randomize:
				{
					classNode.signature = this.stringGenerator.generate();
					break;
				}
				case Replace:
				{
					classNode.signature = this.signature;
					break;
				}
			}
			affected_classes.incrementAndGet();
			classNode.methods.forEach(methodNode ->
			{
				switch (this.mode)
				{
					case Remove:
					{
						methodNode.signature = null;
						break;
					}
					case Randomize:
					{
						methodNode.signature = this.stringGenerator.generate();
						break;
					}
					case Replace:
					{
						methodNode.signature = this.signature;
						break;
					}
				}
				affected_methods.incrementAndGet();
				if (methodNode.localVariables != null)
					methodNode.localVariables.forEach(localVarNode ->
					{
						switch (this.mode)
						{
							case Remove:
							{
								localVarNode.signature = null;
								break;
							}
							case Randomize:
							{
								localVarNode.signature = this.stringGenerator.generate();
								break;
							}
							case Replace:
							{
								localVarNode.signature = this.signature;
								break;
							}
						}
						affected_localvariables.incrementAndGet();
					});
			});
			classNode.fields.forEach(fieldNode ->
			{
				switch (this.mode)
				{
					case Remove:
					{
						fieldNode.signature = null;
						break;
					}
					case Randomize:
					{
						fieldNode.signature = this.stringGenerator.generate();
						break;
					}
					case Replace:
					{
						fieldNode.signature = this.signature;
						break;
					}
				}
				affected_fields.incrementAndGet();
			});
		});
		Logger.stdOut(this.mode.resultMessage + ' ' + affected_classes.get() + " class signatures, " + affected_methods.get() + " method signatures, " + affected_fields.get() + " field signatures, " + affected_localvariables.get() + " local variable signatures " + (this.mode == Mode.Replace && this.signature.length() < 32 ? " to " + this.signature : "") + '.');
	}

	@Override
	public final ExclusionType getExclusionType()
	{
		return ExclusionType.CRASHER;
	}

	@Override
	public Map<String, Object> getConfiguration()
	{
		return null;// TODO
	}

	@Override
	public void setConfiguration(Map<String, Object> config)
	{
		// TODO
	}

	@Override
	public void verifyConfiguration(Map<String, Object> config)
	{
		// TODO
	}

	@Override
	public final String getName()
	{
		return "Signature Obfuscation";
	}
}
