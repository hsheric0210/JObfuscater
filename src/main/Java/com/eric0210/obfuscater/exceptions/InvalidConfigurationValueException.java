package com.eric0210.obfuscater.exceptions;

public class InvalidConfigurationValueException extends RuntimeException
{
	public InvalidConfigurationValueException(String msg)
	{
		super(msg);
	}

	public InvalidConfigurationValueException(String value, Class expectedType, Class gotInstead)
	{
		super(String.format("Value %s was expected to be %s, got %s instead.", value, expectedType, gotInstead.getName()));
	}
}
