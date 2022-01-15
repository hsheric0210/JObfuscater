package com.eric0210.obfuscater.utils;

public final class StringUtils
{
	public static int getByteStringLength(final String str)
	{
		final int charLength = str.length();
		int byteLength = 0;
		for (int i = 0; i < charLength; i++)
		{
			final char charValue = str.charAt(i);
			if (charValue >= 0x0001 && charValue <= 0x007F)
				byteLength++;
			else if (charValue <= 0x07FF)
				byteLength += 2;
			else
				byteLength += 3;
		}
		return byteLength;
	}

	@SuppressWarnings("StringOperationCanBeSimplified")
	public static String subStringByRistriction(final String string, final int bytelength)
	{
		String str = new String(string);
		if (str.length() > bytelength || StringUtils.getByteStringLength(str) > bytelength)
			do
				str = str.substring(0, str.length() - 1);
			while (str.length() > 65535 || StringUtils.getByteStringLength(str) > 65535);
		return str;
	}

	private StringUtils()
	{
	}
}
