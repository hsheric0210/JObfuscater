package com.eric0210.obfuscater.exceptions;

import com.eric0210.obfuscater.Logger;

public class MissingClassException extends RuntimeException
{
	private static final long serialVersionUID = 3109023559717965183L;

	public MissingClassException(final String message, final Throwable cause)
	{
		super(message, cause);
		Logger.stdOut("Do NOT report an issue about this exception unless you have absolutely made sure that the class reported missing exists in the library list you provided to Obfuscater.");
	}
}
