package com.eric0210.obfuscater.transformer.obfuscator.flow;

import java.util.*;
import java.util.stream.Stream;

import com.eric0210.obfuscater.config.ConfigurationSetting;
import com.eric0210.obfuscater.exceptions.InvalidConfigurationValueException;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.StringGenerator;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

public class FlowObfuscation extends Transformer
{
	public enum FlowObfuscationSetting
	{
		REPLACE_GOTO(Boolean.class, new GotoReplacer()),
		INSERT_BOGUS_JUMPS(Boolean.class, new BogusJumpInserter()),
		REARRANGE_BLOCKS(Boolean.class, new BlockRearranger()),
		FAKE_CATCH_BLOCKS(Boolean.class, new FakeCatchBlocks()),
		MUTILATE_NULL_CHECK(Boolean.class, new NullCheckMutilator()),
		COMBINE_TRY_WITH_CATCH(Boolean.class, new TryCatchCombiner());

		private final Class expectedType;
		private final FlowObfuscation flowObfuscation;


		FlowObfuscationSetting(Class expectedType, FlowObfuscation flowObfuscation)
		{
			this.expectedType = expectedType;
			this.flowObfuscation = flowObfuscation;
		}

		public Class getExpectedType()
		{
			return expectedType;
		}

		public FlowObfuscation getFlowObfuscation()
		{
			return flowObfuscation;
		}

		public String getName()
		{
			return name().toLowerCase();
		}
	}

	private static final Map<String, FlowObfuscationSetting> KEY_MAP = new HashMap<>();
	private static final Map<FlowObfuscation, FlowObfuscationSetting> FLOW_SETTING_MAP = new HashMap<>();
	private final List<FlowObfuscation> flowObfuscators = new ArrayList<>();

	static
	{
		FlowObfuscationSetting[] values = FlowObfuscationSetting.values();
		Stream.of(values).forEach(setting -> KEY_MAP.put(setting.getName(), setting));
		Stream.of(values).forEach(setting -> FLOW_SETTING_MAP.put(setting.getFlowObfuscation(), setting));
	}

	@Override
	public void transform()
	{
		flowObfuscators.forEach(flowObfuscator ->
		{
			flowObfuscator.init(this.obfuscator);
			flowObfuscator.transform();
		});
	}

	@Override
	public String getName()
	{
		return "Flow Obfuscation";
	}

	@Override
	public ExclusionType getExclusionType()
	{
		return ExclusionType.FLOW_OBFUSCATION;
	}

	@Override
	public Map<String, Object> getConfiguration()
	{
		Map<String, Object> config = new LinkedHashMap<>();
		flowObfuscators.forEach(obfuscator -> config.put(obfuscator.getFlowObfuscationSetting().getName(), true));
		return config;
	}

	@Override
	public void setConfiguration(Map<String, Object> config)
	{
		Stream.of(FlowObfuscationSetting.values()).filter(setting -> config.containsKey(setting.getName())).forEach(setting -> flowObfuscators.add(setting.getFlowObfuscation()));
	}

	@Override
	public void verifyConfiguration(Map<String, Object> config)
	{
		config.forEach((k, v) ->
		{
			FlowObfuscationSetting setting = KEY_MAP.get(k);

			if (setting == null)
				throw new InvalidConfigurationValueException(ConfigurationSetting.FLOW_OBFUSCATION.getName() + '.' + k + " is an invalid configuration key");
			if (!setting.getExpectedType().isInstance(v))
				throw new InvalidConfigurationValueException(ConfigurationSetting.FLOW_OBFUSCATION.getName() + '.' + k, setting.getExpectedType(), v.getClass());
		});
	}

	private FlowObfuscationSetting getFlowObfuscationSetting()
	{
		return FLOW_SETTING_MAP.get(this);
	}
}
