package com.eric0210.obfuscater.transformer.obfuscator.number;

import java.util.*;
import java.util.stream.Stream;

import com.eric0210.obfuscater.config.ConfigurationSetting;
import com.eric0210.obfuscater.exceptions.InvalidConfigurationValueException;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.RandomUtils;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

/**
 * Abstract class for number obfuscation transformers.
 *
 * @author ItzSomebody
 */
public class NumberObfuscation extends Transformer
{
	public enum NumberObfuscationSetting
	{
		CONTEXT_CHECKING(new ContextCheckObfuscator()),
		ARITHMETIC_OPERATIONS(new ArithmeticObfuscator()),
		BITWISE_OPERATIONS(new BitwiseObfuscator()),
		STRINGLENGTH_OPERATIONS(new StringLengthObfuscator()),;

		private final NumberObfuscation numberObfuscation;

		NumberObfuscationSetting(NumberObfuscation numberObfuscation)
		{
			this.numberObfuscation = numberObfuscation;
		}

		public NumberObfuscation getNumberObfuscation()
		{
			return numberObfuscation;
		}

		public String getName()
		{
			return name().toLowerCase();
		}
	}

	private static final Map<String, NumberObfuscationSetting> KEY_MAP = new HashMap<>();
	private static final Map<NumberObfuscation, NumberObfuscationSetting> NUMBEROBF_SETTING_MAP = new HashMap<>();
	private final List<NumberObfuscation> numberObfuscators = new ArrayList<>();
	public boolean integerTamperingEnabled;
	public boolean longTamperingEnabled;
	public boolean floatTamperingEnabled;
	public boolean doubleTamperingEnabled;
	public int maxRepeatCount;
	public int minRepeatCount;

	static
	{
		NumberObfuscationSetting[] values = NumberObfuscationSetting.values();
		Stream.of(values).forEach(setting -> KEY_MAP.put(setting.getName(), setting));
		Stream.of(values).filter(setting -> setting.getNumberObfuscation() != null).forEach(setting -> NUMBEROBF_SETTING_MAP.put(setting.getNumberObfuscation(), setting));
	}

	@Override
	public void transform()
	{
		numberObfuscators.forEach(numberObfuscation ->
		{
			numberObfuscation.init(radon);
			numberObfuscation.transform();
		});
	}

	@Override
	public String getName()
	{
		return "Number obfuscation";
	}

	@Override
	public ExclusionType getExclusionType()
	{
		return ExclusionType.NUMBER_OBFUSCATION;
	}

	@Override
	public Map<String, Object> getConfiguration()
	{
		Map<String, Object> config = new LinkedHashMap<>();

		numberObfuscators.forEach(obfuscator -> config.put(obfuscator.getNumberObfuscationSetting().getName(), true));

		config.put(NumberObfuscationSetting.DOUBLE_TAMPERING.getName(), isDoubleTamperingEnabled());
		config.put(NumberObfuscationSetting.FLOAT_TAMPERING.getName(), isFloatTamperingEnabled());
		config.put(NumberObfuscationSetting.INTEGER_TAMPERING.getName(), isIntegerTamperingEnabled());
		config.put(NumberObfuscationSetting.LONG_TAMPERING.getName(), isLongTamperingEnabled());

		return config;
	}

	@Override
	public void setConfiguration(Map<String, Object> config)
	{
		Stream.of(NumberObfuscationSetting.values()).filter(setting -> setting.getNumberObfuscation() != null && config.containsKey(setting.getName())).forEach(setting -> numberObfuscators.add(setting.getNumberObfuscation()));

		setDoubleTamperingEnabled(getValueOrDefault(NumberObfuscationSetting.DOUBLE_TAMPERING.getName(), config, false));
		setFloatTamperingEnabled(getValueOrDefault(NumberObfuscationSetting.FLOAT_TAMPERING.getName(), config, false));
		setIntegerTamperingEnabled(getValueOrDefault(NumberObfuscationSetting.INTEGER_TAMPERING.getName(), config, false));
		setLongTamperingEnabled(getValueOrDefault(NumberObfuscationSetting.LONG_TAMPERING.getName(), config, false));
	}

	@Override
	public void verifyConfiguration(Map<String, Object> config)
	{
		config.forEach((k, v) ->
		{
			NumberObfuscationSetting setting = KEY_MAP.get(k);

			if (setting == null)
				throw new InvalidConfigurationValueException(ConfigurationSetting.NUMBER_OBFUSCATION.getName() + '.' + k + " is an invalid configuration key");
			if (!setting.expectedType.isInstance(v))
				throw new InvalidConfigurationValueException(ConfigurationSetting.NUMBER_OBFUSCATION.getName() + '.' + k, setting.getExpectedType(), v.getClass());
		});
	}

	protected static int randomInt(int bounds)
	{
		if (bounds <= 0)
			return RandomUtils.getRandomInt(Integer.MAX_VALUE);

		return RandomUtils.getRandomInt(bounds);
	}

	protected static long randomLong(long bounds)
	{
		if (bounds <= 0)
			return RandomUtils.getRandomLong(Long.MAX_VALUE);

		return RandomUtils.getRandomLong(bounds);
	}

	protected static float randomFloat(float bounds)
	{
		if (bounds <= 0)
			return RandomUtils.getRandomFloat(Float.MAX_VALUE);

		return RandomUtils.getRandomFloat(bounds);
	}

	protected static double randomDouble(double bounds)
	{
		if (bounds <= 0)
			return RandomUtils.getRandomDouble(Double.MAX_VALUE);

		return RandomUtils.getRandomDouble(bounds);
	}
	private NumberObfuscationSetting getNumberObfuscationSetting()
	{
		return NUMBEROBF_SETTING_MAP.get(this);
	}
}
