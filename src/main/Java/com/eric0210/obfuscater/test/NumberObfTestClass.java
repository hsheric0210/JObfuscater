package com.eric0210.obfuscater.test;

import com.eric0210.obfuscater.Logger;

public class NumberObfTestClass
{
	public static String[] pool;
	public final void test()
	{
		Logger.stdOut(null);
		Logger.stdOut("null: " + null);
		Logger.stdOut("null: " + null);
		Logger.stdOut("zero: " + 0 + ", L: " + 0L);
		Logger.stdOut("one: " + 1 + ", L: " + 1L);
		Logger.stdOut("two: " + 2 + ", L: " + 2L);
		Logger.stdOut("three: " + 3 + ", L: " + 3L);
		Logger.stdOut("four: " + 4 + ", L: " + 4L);
		Logger.stdOut("five: " + 5 + ", L: " + 5L);
		Logger.stdOut("six: " + 6 + ", L: " + 6L);
		Logger.stdOut("seven: " + 7 + ", L: " + 7L);
		Logger.stdOut("eight: " + 8 + ", L: " + 8L);
		Logger.stdOut("nine: " + 9 + ", L: " + 9L);
		Logger.stdOut("ten: " + 10 + ", L: " + 10L);
		Logger.stdOut("hundred: " + 100 + ", L: " + 100L);
		Logger.stdOut("thousand: " + 1000 + ", L: " + 1000L);
	}
}
