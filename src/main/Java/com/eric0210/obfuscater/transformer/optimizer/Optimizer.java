package com.eric0210.obfuscater.transformer.optimizer;

import java.util.ArrayList;
import java.util.Map;

import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

public class Optimizer extends Transformer
{
	public boolean nopRemoverEnabled;
	public boolean gotoGotoEnabled;
	public boolean gotoReturnEnabled;

	@Override
	public void transform()
	{
		ArrayList<Optimizer> optimizers = new ArrayList<>();

		if (this.nopRemoverEnabled)
			optimizers.add(new NOPRemover());
		if (this.gotoGotoEnabled)
			optimizers.add(new GotoGotoInliner());
		if (this.gotoReturnEnabled)
			optimizers.add(new GotoReturnInliner());

		optimizers.forEach(shrinker ->
		{
			shrinker.init(this.obfuscator);
			shrinker.transform();
		});

	}

	@Override
	public String getName()
	{
		return "Optimizer";
	}

	public final ExclusionType getExclusionType()
	{
		return ExclusionType.OPTIMIZER;
	}

	@Override
	public Map<String, Object> getConfiguration()
	{
		return null;
	}

	@Override
	public void setConfiguration(Map<String, Object> config)
	{

	}

	@Override
	public void verifyConfiguration(Map<String, Object> config)
	{

	}
}
