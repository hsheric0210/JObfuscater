package com.eric0210.obfuscater.exceptions;

public class ObfuscatorException extends RuntimeException
{
	public ObfuscatorException()
	{
		super();
	}

	public ObfuscatorException(String msg)
	{
		super(msg);
	}

	public ObfuscatorException(Throwable t)
	{
		super(t);
	}
}
