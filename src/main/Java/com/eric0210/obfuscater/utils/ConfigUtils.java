package com.eric0210.obfuscater.utils;

import java.util.Map;

public class ConfigUtils
{
	/**
	 * Returns the specified value from the provided map.
	 *
	 * @param  key
	 *             the key to lookup the value.
	 * @param  map
	 *             the map to lookup.
	 * @param  <T>
	 *             generic-typing because ItzSomebody is lazy.
	 * @return     the specified value from the provided map.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getValue(String key, Map<String, Object> map)
	{
		return (T) map.get(key);
	}

	// Laziness v2.0
	public static <T> T getValueOrDefault(String key, Map<String, Object> map, T defaultVal)
	{
		T t = getValue(key, map);

		if (t == null)
			return defaultVal;
		else
			return t;
	}
}
