package com.eric0210.obfuscater;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public final class Logger
{
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("MM/dd/yyyy-HH:mm:ss");
	private static final SimpleDateFormat FILE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	private static final List<String> STRINGS = new ArrayList<>();

	public static void dumpLog()
	{
		if (STRINGS.isEmpty())
			return;
		final BufferedWriter bw;
		try
		{
			final StringBuilder sb = new StringBuilder("Obfuscation_");
			sb.append(FILE_FORMAT.format(new Date()));
			sb.append(".log");
			final File log = new File(sb.toString());
			if (!log.exists())
				log.createNewFile();
			bw = new BufferedWriter(new FileWriter(log));
			bw.append("##############################################");
			bw.newLine();
			bw.append("Eric's Advanced Java Obfuscater");
			bw.newLine();
			bw.append("##############################################");
			bw.newLine();
			bw.newLine();
			bw.newLine();
			bw.append("Version: ").append(Constants.VERSION);
			bw.newLine();
			bw.append("Contributors: ").append(Constants.CONTRIBUTORS);
			bw.newLine();
			STRINGS.forEach(s ->
			{
				try
				{
					bw.append(s);
					bw.newLine();
				}
				catch (IOException ioe)
				{
					ioe.printStackTrace();
				}
			});
			STRINGS.clear();
			bw.close();
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		catch (final Throwable t)
		{
			stdErr("Error occurred while writing log.");
			t.printStackTrace();
		}
	}

	public static void stdOut(final String string)
	{
		final String date = FORMAT.format(new Date(System.currentTimeMillis()));
		final String formatted = '[' + date + "] INFO: " + string;
		System.out.println(formatted);
		STRINGS.add(formatted);
	}

	public static void stdErr(final String string)
	{
		final String date = FORMAT.format(new Date(System.currentTimeMillis()));
		final String formatted = '[' + date + "] ERROR: " + string;
		System.out.println(formatted);
		STRINGS.add(formatted);
	}

	public static void stdWarn(final String string)
	{
		final String date = FORMAT.format(new Date(System.currentTimeMillis()));
		final String formatted = '[' + date + "] WARNING: " + string;
		System.out.println(formatted);
		STRINGS.add(formatted);
	}

	private Logger()
	{
	}
}
