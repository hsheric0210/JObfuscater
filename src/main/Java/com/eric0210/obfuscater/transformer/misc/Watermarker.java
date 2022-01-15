/*
 * Copyright (C) 2018 ItzSomebody This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>
 */
package com.eric0210.obfuscater.transformer.misc;

import java.util.*;

import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.asm.ClassWrapper;
import com.eric0210.obfuscater.config.ConfigurationSetting;
import com.eric0210.obfuscater.exceptions.InvalidConfigurationValueException;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.ASMUtils;
import com.eric0210.obfuscater.utils.ConfigUtils;
import com.eric0210.obfuscater.utils.RandomUtils;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Embeds a watermark into random classes.
 *
 * @author ItzSomebody.
 */
public class Watermarker extends Transformer
{
	public enum WatermarkerSetting
	{
		MESSAGE(String.class),
		KEY(String.class);

		private final Class expectedType;

		WatermarkerSetting(Class expectedType)
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

	private final Map<String, WatermarkerSetting> KEY_MAP = new HashMap<>();
	private String message;
	private String key;

	@Override
	public final void transform()
	{
		final ArrayList<ClassWrapper> classWrappers = new ArrayList<>(this.getClassWrappers());
		for (int i = 0; i < 3; i++)
		{ // Two extra injections helps with reliability of watermark to be extracted
			final Stack<Character> watermark = this.cipheredWatermark();
			while (!watermark.isEmpty())
			{
				ClassWrapper classWrapper;
				int counter = 0;
				do
				{
					classWrapper = classWrappers.get(RandomUtils.getRandomInt(classWrappers.size()));
					counter++;
					if (counter > 20)
						throw new IllegalStateException("Obfuscater couldn't find any methods to embed a watermark in after " + counter + " tries.");
				}
				while (classWrapper.classNode.methods.isEmpty());

				final MethodNode methodNode = classWrapper.classNode.methods.get(RandomUtils.getRandomInt(classWrapper.classNode.methods.size()));

				if (this.hasInstructions(methodNode))
					methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), createInstructions(watermark, methodNode));
			}
		}
		Logger.stdOut("Successfully embedded watermark.");
	}

	private static InsnList createInstructions(final Stack<Character> watermark, final MethodNode methodNode)
	{
		final int offset = methodNode.maxLocals;
		final int xorKey = RandomUtils.getRandomInt();
		final int watermarkChar = watermark.pop() ^ xorKey;
		final int indexXorKey = RandomUtils.getRandomInt();
		final int watermarkIndex = watermark.size() ^ indexXorKey;
		final InsnList instructions = new InsnList();
		instructions.add(ASMUtils.getNumberInsn(xorKey));
		instructions.add(ASMUtils.getNumberInsn(watermarkChar));
		instructions.add(ASMUtils.getNumberInsn(indexXorKey));
		instructions.add(ASMUtils.getNumberInsn(watermarkIndex));
		// Local variable x where x is the max locals allowed in method can be the top
		// of a long or double so we add 1
		instructions.add(new VarInsnNode(ISTORE, offset + 1));
		instructions.add(new VarInsnNode(ISTORE, offset + 2));
		instructions.add(new VarInsnNode(ISTORE, offset + 3));
		instructions.add(new VarInsnNode(ISTORE, offset + 4));
		return instructions;
	}

	// Really weak cipher, lul.
	private Stack<Character> cipheredWatermark()
	{
		final char[] messageChars = this.message.toCharArray();
		final char[] keyChars = this.key.toCharArray();
		final Stack<Character> returnThis = new Stack<>();
		for (int i = 0, j = messageChars.length; i < j; i++)
			returnThis.push((char) (messageChars[i] ^ keyChars[i % keyChars.length]));
		return returnThis;
	}

	@Override
	public final ExclusionType getExclusionType()
	{
		return null;
	}

	@Override
	public final String getName()
	{
		return "Watermarker";
	}

	@Override
	public Map<String, Object> getConfiguration()
	{
		Map<String, Object> config = new LinkedHashMap<>();

		message = (ConfigUtils.getValueOrDefault(WatermarkerSetting.MESSAGE.getName(), config, null));
		key = (ConfigUtils.getValueOrDefault(WatermarkerSetting.KEY.getName(), config, null));

		return config;
	}

	@Override
	public void setConfiguration(Map<String, Object> config)
	{
		config.put(WatermarkerSetting.MESSAGE.getName(), message);
		config.put(WatermarkerSetting.KEY.getName(), key);
	}

	@Override
	public void verifyConfiguration(Map<String, Object> config)
	{
		config.forEach((k, v) ->
		{
			WatermarkerSetting setting = KEY_MAP.get(k);

			if (setting == null)
				throw new InvalidConfigurationValueException(ConfigurationSetting.WATERMARK.getName() + '.' + k + " is an invalid configuration key");
			if (!setting.getExpectedType().isInstance(v))
				throw new InvalidConfigurationValueException(ConfigurationSetting.WATERMARK.getName() + '.' + k, setting.getExpectedType(), v.getClass());

		});
	}
}
