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
package com.eric0210.obfuscater.transformer.shrinkers;

import java.util.*;
import java.util.stream.Stream;

import com.eric0210.obfuscater.config.ConfigurationSetting;
import com.eric0210.obfuscater.exceptions.InvalidConfigurationValueException;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

/**
 * Abstract class for shrinking transformers.
 *
 * @author ItzSomebody
 */
public class Shrinker extends Transformer
{
	public enum ShrinkerSetting
	{
		REMOVE_DEPRECATED(Boolean.class, new DeprecatedAccessRemover()),
		REMOVE_INNER_CLASSES(Boolean.class, new InnerClassesRemover()),
		REMOVE_INVISIBLE_ANNOTATIONS(Boolean.class, new InvisibleAnnotationsRemover()),
		REMOVE_INVISIBLE_PARAMETER_ANNOTATIONS(Boolean.class, new InvisibleParameterAnnotationsRemover()),
		REMOVE_INVISIBLE_TYPE_ANNOTATIONS(Boolean.class, new InvisibleTypeAnnotationsRemover()),
		REMOVE_LINE_NUMBERS(Boolean.class, new LineNumberRemover()),
		REMOVE_LOCAL_VARIABLES(Boolean.class, new LocalVariableRemover()),
		REMOVE_OUTER_METHOD(Boolean.class, new OuterMethodRemover()),
		REMOVE_SIGNATURE(Boolean.class, new SignatureRemover()),
		REMOVE_SOURCE_DEBUG(Boolean.class, new SourceDebugRemover()),
		REMOVE_SOURCE_FILE(Boolean.class, new SourceFileRemover()),
		REMOVE_SYNTHETIC(Boolean.class, new SyntheticAccessRemover()),
		REMOVE_UNKNOWN_ATTRIBUTES(Boolean.class, new UnknownAttributesRemover()),
		REMOVE_VISIBLE_ANNOTATIONS(Boolean.class, new VisibleAnnotationsRemover()),
		REMOVE_VISIBLE_PARAMETER_ANNOTATIONS(Boolean.class, new VisibleParameterAnnotationsRemover()),
		REMOVE_VISIBLE_TYPE_ANNOTATIONS(Boolean.class, new VisibleTypeAnnotationsRemover());

		private final Class expectedType;
		private final Shrinker shrinker;

		ShrinkerSetting(Class expectedType, Shrinker shrinker)
		{
			this.expectedType = expectedType;
			this.shrinker = shrinker;
		}

		public Class getExpectedType()
		{
			return expectedType;
		}

		public Shrinker getShrinker()
		{
			return shrinker;
		}

		public String getName()
		{
			return name().toLowerCase();
		}
	}

	private static final Map<String, ShrinkerSetting> KEY_MAP = new HashMap<>();
	private static final Map<Shrinker, ShrinkerSetting> SHRINKER_SETTING_MAP = new HashMap<>();
	private final List<Shrinker> shrinkers = new ArrayList<>();

	static
	{
		ShrinkerSetting[] values = ShrinkerSetting.values();
		Stream.of(values).forEach(setting -> KEY_MAP.put(setting.getName(), setting));
		Stream.of(values).forEach(setting -> SHRINKER_SETTING_MAP.put(setting.getShrinker(), setting));
	}

	@Override
	public void transform()
	{
		shrinkers.forEach(shrinker ->
		{
			shrinker.init(this.obfuscator);
			shrinker.transform();
		});
	}

	@Override
	public String getName()
	{
		return "Shrinker";
	}

	@Override
	public final ExclusionType getExclusionType()
	{
		return ExclusionType.SHRINKER;
	}

	@Override
	public Map<String, Object> getConfiguration()
	{
		Map<String, Object> config = new LinkedHashMap<>();
		shrinkers.forEach(shrinker -> config.put(shrinker.getShrinkerSetting().getName(), true));
		return config;
	}

	@Override
	public void setConfiguration(Map<String, Object> config)
	{
		Stream.of(ShrinkerSetting.values()).filter(setting -> config.containsKey(setting.getName())).forEach(setting -> shrinkers.add(setting.getShrinker()));
	}

	@Override
	public void verifyConfiguration(Map<String, Object> config)
	{
		config.forEach((k, v) ->
		{
			ShrinkerSetting setting = KEY_MAP.get(k);

			if (setting == null)
				throw new InvalidConfigurationValueException(ConfigurationSetting.SHRINKER.getName() + '.' + k + " is an invalid configuration key");
			if (!setting.getExpectedType().isInstance(v))
				throw new InvalidConfigurationValueException(ConfigurationSetting.SHRINKER.getName() + '.' + k, setting.getExpectedType(), v.getClass());
		});
	}

	private ShrinkerSetting getShrinkerSetting()
	{
		return SHRINKER_SETTING_MAP.get(this);
	}
}
