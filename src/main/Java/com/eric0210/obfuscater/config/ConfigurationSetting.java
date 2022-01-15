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

package com.eric0210.obfuscater.config;

import java.util.List;
import java.util.Map;

import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.transformer.misc.Expiration;
import com.eric0210.obfuscater.transformer.misc.Watermarker;
import com.eric0210.obfuscater.transformer.obfuscator.*;
import com.eric0210.obfuscater.transformer.obfuscator.attributes.SignatureObfuscation;
import com.eric0210.obfuscater.transformer.obfuscator.attributes.HideCode;
import com.eric0210.obfuscater.transformer.obfuscator.flow.FlowObfuscation;
import com.eric0210.obfuscater.transformer.obfuscator.references.ReferenceObfuscation;
import com.eric0210.obfuscater.transformer.obfuscator.strings.StringEncryption;
import com.eric0210.obfuscater.transformer.optimizer.Optimizer;
import com.eric0210.obfuscater.transformer.shrinkers.Shrinker;

/**
 * An {@link Enum} containing all the allowed standalone configuration keys allowed.
 *
 * @author ItzSomebody
 */
public enum ConfigurationSetting
{
	INPUT(String.class, null),
	OUTPUT(String.class, null),
	LIBRARIES(List.class, null),
	EXCLUSIONS(List.class, null),
	STRING_ENCRYPTION(Map.class, new StringEncryption()),
	FLOW_OBFUSCATION(Map.class, new FlowObfuscation()),
	REFERENCE_OBFUSCATION(Map.class, new ReferenceObfuscation()),
	NUMBER_OBFUSCATION(Map.class, new NumberObfuscation()),
	ANTI_TAMPER(String.class, new AntiTamper()), // TODO
	VIRTUALIZER(Boolean.class, new Virtualizer()), // TODO: ;)
	RESOURCE_ENCRYPTION(Boolean.class, new ResourceEncryption()), // TODO
	RESOURCE_RENAMER(Boolean.class, new ResourceRenamer()), // TODO
	// CLASS_ENCRYPTION(Map.class, new ClassEncryption()), // Just kidding, lol
	HIDE_CODE(Map.class, new HideCode()),
	CRASHER(Boolean.class, new SignatureObfuscation()),
	EXPIRATION(Map.class, new Expiration()),
	WATERMARK(Map.class, new Watermarker()),
	OPTIMIZER(Map.class, new Optimizer()),
	SHRINKER(Map.class, new Shrinker()),
	MEMBER_SHUFFLER(Boolean.class, new MemberShuffler()),
	RENAMER(Map.class, new Renamer()),
	DICTIONARY(String.class, null),
	RANDOMIZED_STRING_LENGTH(Integer.class, null),
	COMPRESSION_LEVEL(Integer.class, null),
	VERIFY(Boolean.class, null),
	TRASH_CLASSES(Integer.class, null);

	public final Class expectedType;
	public final Transformer transformer;

	ConfigurationSetting(Class expectedType, Transformer transformer)
	{
		this.expectedType = expectedType;
		this.transformer = transformer;
	}

	public String getName()
	{
		return name().toLowerCase();
	}
}
