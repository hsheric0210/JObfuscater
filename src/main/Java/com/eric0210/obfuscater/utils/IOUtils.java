package com.eric0210.obfuscater.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.ObfuscatorGUI;
import com.eric0210.obfuscater.exceptions.ObfuscatorException;

public class IOUtils
{
	private IOUtils()
	{
	}

	public static byte[] toByteArray(final InputStream in)
	{
		try
		{
			final ByteArrayOutputStream out = new ByteArrayOutputStream();
			final byte[] buffer = new byte[1024];
			while (in.available() > 0)
			{
				final int data = in.read(buffer);
				out.write(buffer, 0, data);
			}
			in.close();
			out.close();
			return out.toByteArray();
		}
		catch (final OutOfMemoryError | IOException e)
		{
			Logger.stdErr(ObfuscatorGUI.getExceptionStackTrace(e));
			throw new ObfuscatorException(e);
		}

	}
}
