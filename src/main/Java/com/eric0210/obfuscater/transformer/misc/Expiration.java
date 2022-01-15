/*
 * Copyright (C) 2018 ItzSomebody
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.eric0210.obfuscater.transformer.misc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.config.ConfigurationSetting;
import com.eric0210.obfuscater.exceptions.InvalidConfigurationValueException;
import com.eric0210.obfuscater.exceptions.ObfuscatorException;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.ConfigUtils;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

import org.objectweb.asm.Label;
import org.objectweb.asm.tree.*;

/**
 * Inserts an expiration block of instructions in each constructor method.
 *
 * @author ItzSomebody
 */
public class Expiration extends Transformer
{
	public enum ExpirationSetting
	{
		EXPIRATION_DATE(String.class),
		INJECT_JOPTIONPAN(Boolean.class),
		EXPIRATION_MESSAGE(String.class);

		private final Class expectedType;

		ExpirationSetting(Class expectedType)
		{
			this.expectedType = expectedType;
		}

		public Class getExpectedType()
		{
			return expectedType;
		}

		public String getName()
		{
			return name().toLowerCase();
		}
	}

	private static final Map<String, ExpirationSetting> KEY_MAP = new HashMap<>();

	static
	{
		Stream.of(ExpirationSetting.values()).forEach(setting -> KEY_MAP.put(setting.getName(), setting));
	}

	private String message;
	private long expires;
	private boolean injectJOptionPaneEnabled;

	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> !this.isExcluded(classWrapper)).forEach(classWrapper ->
		{
			ClassNode classNode = classWrapper.classNode;

			classNode.methods.stream().filter(methodNode -> "<init>".equals(methodNode.name)).forEach(methodNode ->
			{
				InsnList expirationCode = createExpirationInstructions();
				methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), expirationCode);
				counter.incrementAndGet();
			});
		});

		Logger.stdOut(String.format("Added %d expiration code blocks.", counter.get()));
	}

	private InsnList createExpirationInstructions()
	{
		InsnList insnList = new InsnList();
		LabelNode injectedLabel = new LabelNode(new Label());

		insnList.add(new TypeInsnNode(NEW, "java/util/Date"));
		insnList.add(new InsnNode(DUP));
		insnList.add(new MethodInsnNode(INVOKESPECIAL, "java/util/Date", "<init>", "()V", false));
		insnList.add(new TypeInsnNode(NEW, "java/util/Date"));
		insnList.add(new InsnNode(DUP));
		insnList.add(new LdcInsnNode(this.expires));
		insnList.add(new MethodInsnNode(INVOKESPECIAL, "java/util/Date", "<init>", "(J)V", false));
		insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/util/Date", "after", "(Ljava/util/Date;)Z", false));
		insnList.add(new JumpInsnNode(IFEQ, injectedLabel));
		insnList.add(new TypeInsnNode(NEW, "java/lang/Throwable"));
		insnList.add(new InsnNode(DUP));
		insnList.add(new LdcInsnNode(this.message));
		if (this.injectJOptionPaneEnabled)
		{
			insnList.add(new InsnNode(DUP));
			insnList.add(new InsnNode(ACONST_NULL));
			insnList.add(new InsnNode(SWAP));
			insnList.add(new MethodInsnNode(INVOKESTATIC, "javax/swing/JOptionPane", "showMessageDialog", "(Ljava/awt/Component;Ljava/lang/Object;)V", false));
		}
		insnList.add(new MethodInsnNode(INVOKESPECIAL, "java/lang/Throwable", "<init>", "(Ljava/lang/String;)V", false));
		insnList.add(new InsnNode(ATHROW));
		insnList.add(injectedLabel);

		return insnList;
	}

	@Override
	public ExclusionType getExclusionType()
	{
		return ExclusionType.EXPIRATION;
	}

	@Override
	public String getName()
	{
		return "Expiration";
	}

	@Override
	public Map<String, Object> getConfiguration()
	{
		Map<String, Object> config = new LinkedHashMap<>();

		config.put(ExpirationSetting.EXPIRATION_DATE.getName(), this.expires);
		config.put(ExpirationSetting.INJECT_JOPTIONPAN.getName(), this.injectJOptionPaneEnabled);
		config.put(ExpirationSetting.EXPIRATION_MESSAGE.getName(), this.message);

		return config;
	}

	@Override
	public void setConfiguration(Map<String, Object> config) {
		try {
			this.expires = new SimpleDateFormat("MM/dd/yyyy").parse(ConfigUtils.getValueOrDefault(ExpirationSetting.EXPIRATION_DATE.getName(), config, "12/31/2020")).getTime();
		} catch (ParseException e) {
			Logger.stdErr("Error while parsing time.");
			throw new ObfuscatorException(e);
		}
		this.injectJOptionPaneEnabled = ConfigUtils.getValueOrDefault(ExpirationSetting.INJECT_JOPTIONPAN.getName(), config, false);
		this.message = ConfigUtils.getValueOrDefault(ExpirationSetting.EXPIRATION_MESSAGE.getName(), config, "Your trial has expired!");
	}

	@Override
	public void verifyConfiguration(Map<String, Object> config)
	{
		config.forEach((k, v) ->
		{
			ExpirationSetting setting = KEY_MAP.get(k);

			if (setting == null)
				throw new InvalidConfigurationValueException(ConfigurationSetting.EXPIRATION.getName() + '.' + k + " is an invalid configuration key");
			if (!setting.getExpectedType().isInstance(v))
				throw new InvalidConfigurationValueException(ConfigurationSetting.EXPIRATION.getName() + '.' + k, setting.getExpectedType(), v.getClass());
		});
	}
}
