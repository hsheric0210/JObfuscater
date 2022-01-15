package com.eric0210.obfuscater.test;

public class FlowObfErrorTestClass
{
	public void test()
	{
		String str = null;
		str = "null";
		String str2 = NumberObfTestClass.pool[0];
		str2 = "null2";
		System.out.println(str2);
	}
}
