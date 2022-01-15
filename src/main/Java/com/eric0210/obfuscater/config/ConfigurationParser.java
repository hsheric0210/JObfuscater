/*
 * Radon - An open-source Java obfuscator
 * Copyright (C) 2019 ItzSomebody
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

import static com.eric0210.obfuscater.utils.ConfigUtils.getValue;
import static com.eric0210.obfuscater.utils.ConfigUtils.getValueOrDefault;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.eric0210.obfuscater.exceptions.InvalidConfigurationValueException;
import com.eric0210.obfuscater.exceptions.ObfuscatorException;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.FileUtils;
import com.eric0210.obfuscater.utils.exclusions.Exclusion;
import com.eric0210.obfuscater.utils.exclusions.ExclusionManager;

import org.yaml.snakeyaml.Yaml;

/**
 * Parses a YAML file into an {@link ObfuscationConfiguration}.
 *
 * @author ItzSomebody
 */
public class ConfigurationParser
{
	private final Map<String, ConfigurationSetting> keyLookup = new HashMap<>();
	private Map<String, Object> config;

	/**
	 * Loads the provided {@link InputStream} as a YAML file and verifies it as a configuration.
	 *
	 * @param in
	 *           the {@link InputStream} of the YAML file.
	 */
	@SuppressWarnings("unchecked")
	public ConfigurationParser(InputStream in)
	{
		Stream.of(ConfigurationSetting.values()).forEach(setting -> keyLookup.put(setting.getName(), setting));

		// Loads the YAML file into a Map.
		config = new Yaml().load(in);

		// Verifies the top-level of the configuration.
		config.forEach((k, v) ->
		{
			if (!keyLookup.containsKey(k))
				throw new ObfuscatorException(k + " is not a valid configuration setting.");

			ConfigurationSetting setting = ConfigurationSetting.valueOf(k.toUpperCase());
			if (!setting.expectedType.isAssignableFrom(v.getClass()))
				throw new InvalidConfigurationValueException(k, setting.expectedType, v.getClass());
		});
	}

	/**
	 * Return the input file.
	 *
	 * @return the input file.
	 */
	private File getInput()
	{
		return new File((String) getValue(ConfigurationSetting.INPUT.getName(), config));
	}

	/**
	 * Returns the output file.
	 *
	 * @return the output file.
	 */
	private File getOutput()
	{
		return new File((String) getValue(ConfigurationSetting.OUTPUT.getName(), config));
	}

	/**
	 * Returns the library files.
	 *
	 * @return the library files.
	 */
	private List<File> getLibraries()
	{
		ArrayList<File> libraries = new ArrayList<>();
		List<?> libs = getValue(ConfigurationSetting.LIBRARIES.getName(), config);

		if (libs != null)
			libs.forEach(lib ->
			{
				String s = (String) lib;
				File libFile = new File(s);
				if (libFile.isDirectory())
					FileUtils.getSubDirectoryFiles(libFile, libraries);
				else
					libraries.add(libFile);

			});

		return libraries;
	}

	/**
	 * Creates and returns a new {@link ExclusionManager}.
	 *
	 * @return a new {@link ExclusionManager}.
	 */
	private ExclusionManager getExclusionManager()
	{
		ExclusionManager manager = new ExclusionManager();

		List<String> regexPatterns = getValueOrDefault(ConfigurationSetting.EXCLUSIONS.getName(), config, new ArrayList<>());
		regexPatterns.forEach(regexPattern -> manager.addExclusion(new Exclusion(regexPattern)));

		return manager;
	}

	/**
	 * Returns a {@link ArrayList} of {@link Transformer}s.
	 *
	 * @return a {@link ArrayList} of {@link Transformer}s.
	 */
	@SuppressWarnings("unchecked")
	private List<Transformer> getTransformers()
	{
		ArrayList<Transformer> transformers = new ArrayList<>();

		config.forEach((k, v) ->
		{
			ConfigurationSetting setting = keyLookup.get(k);

			if (setting != null)
			{
				if (!setting.expectedType.isInstance(v))
					throw new InvalidConfigurationValueException(setting.getName(), setting.expectedType, v.getClass());

				Transformer transformer = setting.transformer;

				if (transformer != null)
				{
					if (v instanceof Boolean)
						transformers.add(transformer);
					else if (v instanceof Map)
					{
						transformer.verifyConfiguration((Map<String, Object>) v);
						transformer.setConfiguration((Map<String, Object>) v);
						transformers.add(transformer);
					}
				}
			}
		});

		return transformers;
	}

	/**
	 * Returns the number of trash classes Radon should generate.
	 *
	 * @return the number of trash classes Radon should generate.
	 */
	private int getnTrashClasses()
	{
		return getValueOrDefault(ConfigurationSetting.TRASH_CLASSES.getName(), config, 0);
	}

	/**
	 * Returns the number of characters Radon should generate in random strings.
	 *
	 * @return the number of characters Radon should generate in random strings.
	 */
	private int getRandomizedStringLength()
	{
		return getValueOrDefault(ConfigurationSetting.RANDOMIZED_STRING_LENGTH.getName(), config, 8);
	}

//	/**
//	 * Returns the {@link DictionaryType}.
//	 *
//	 * @return the {@link DictionaryType}.
//	 */
//	private DictionaryType getDictionaryType()
//	{
//		String type = getValueOrDefault(ConfigurationSetting.DICTIONARY.getName(), config, "spaces");
//
//		return DictionaryType.valueOf(type.toUpperCase());
//	}

	/**
	 * Returns the level of compression Radon should apply to the output.
	 *
	 * @return the level of compression Radon should apply to the output.
	 */
	private int getCompressionLevel()
	{
		return getValueOrDefault(ConfigurationSetting.COMPRESSION_LEVEL.getName(), config, 9);
	}

	/**
	 * Returns true if Radon should verify the output.
	 *
	 * @return true if Radon should verify the output.
	 */
	private boolean isVerify()
	{
		return getValueOrDefault(ConfigurationSetting.VERIFY.getName(), config, false);
	}

	/**
	 * Creates a new {@link ObfuscationConfiguration} from the provided configuration.
	 *
	 * @return a new {@link ObfuscationConfiguration} from the provided configuration.
	 */
	public ObfuscationConfiguration createObfuscatorConfiguration()
	{
		final ObfuscationConfiguration _config = new ObfuscationConfiguration();

		_config.input = getInput();
		_config.output = getOutput();
		_config.libraries = getLibraries();
		_config.transformers = getTransformers();
		_config.exclusionManager = getExclusionManager();
		_config.trashClasses = getnTrashClasses();
//		configuration.setRandomizedStringLength(getRandomizedStringLength());
//		configuration.setDictionaryType(getDictionaryType());
		_config.compressionLevel = getCompressionLevel();
		_config.verify = isVerify();

		return _config;
	}
}
