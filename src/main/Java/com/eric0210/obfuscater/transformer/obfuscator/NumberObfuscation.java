package com.eric0210.obfuscater.transformer.obfuscator;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.ASMUtils;
import com.eric0210.obfuscater.utils.RandomUtils;
import com.eric0210.obfuscater.utils.StringGenerator;
import com.eric0210.obfuscater.utils.StringGeneratorParameter;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

import org.objectweb.asm.tree.*;

public class NumberObfuscation extends Transformer
{
	public StringGenerator classNameGenerator;
	public StringGenerator methodNameGenerator;
	public StringGenerator fieldNameGenerator;
	public int loopCount;

	public boolean zeroTamperingEnabled;
	public boolean integerTamperingEnabled;
	public boolean longTamperingEnabled;
	public boolean floatTamperingEnabled;
	public boolean doubleTamperingEnabled;

	public boolean xorOperationsEnabled;
	public boolean bitwiseOperationsEnabled;
	public boolean arithmeticOperationsEnabled;
	public boolean stringLengthOperationsEnabled;

	public boolean contextCheckingEnabled;

	private enum RandomObfuscationMethod
	{
		XOR,
		SimpleMath,
		StringLengthCalculation,
		bitwiseAND;

		public static RandomObfuscationMethod random(final List<RandomObfuscationMethod> choices)
		{
			return choices.get(RandomUtils.getRandomInt(choices.size()));
		}
	}

	protected final AtomicInteger affected_numbers = new AtomicInteger();
	protected final AtomicInteger xores_used = new AtomicInteger();
	protected final AtomicInteger simpleMaths_used = new AtomicInteger();
	protected final AtomicInteger stringLengthCalculations_used = new AtomicInteger();
	protected final AtomicInteger bitwiseAND_used = new AtomicInteger();
	protected final AtomicInteger bitwiseSHL_used = new AtomicInteger();

	@Override
	public final ExclusionType getExclusionType()
	{
		return ExclusionType.NUMBER_OBFUSCATION;
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

	@Override
	public void transform()
	{
		// Reset counters
		this.affected_numbers.set(0);
		this.xores_used.set(0);
		this.simpleMaths_used.set(0);
		this.stringLengthCalculations_used.set(0);
		this.bitwiseAND_used.set(0);
		this.bitwiseSHL_used.set(0);

		this.getClassWrappers().stream().filter(classWrapper -> !this.isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.stream().filter(methodWrapper -> !this.isExcluded(methodWrapper) && this.hasInstructions(methodWrapper.methodNode)).forEach(methodWrapper ->
		{
			for (int i = 0, j = this.loopCount; i < j; i++)
			{
				final MethodNode methodNode = methodWrapper.methodNode;
				int leeway = this.getSizeLeeway(methodNode);

				for (final AbstractInsnNode insn : methodNode.instructions.toArray())
				{
					if (leeway < 10000)
						break;

					if (ASMUtils.isIntInsn(insn))
					{
						// Integer
						final Map.Entry<InsnList, Integer> obf = this.obfuscateInteger(ASMUtils.getIntegerFromInsn(insn));
						if (obf.getKey().size() == 0 && obf.getValue() == 0)
							Logger.stdWarn("Skipped more than one instructions in " + classWrapper.originalName + '.' + methodNode.name);
						else
						{
							methodNode.instructions.insertBefore(insn, obf.getKey());
							methodNode.instructions.remove(insn);
						}

						leeway -= obf.getValue();
					}
					else if (ASMUtils.isLongInsn(insn))
					{
						// Long
						final Map.Entry<InsnList, Integer> obf = this.obfuscateLong(ASMUtils.getLongFromInsn(insn));
						if (obf.getKey().size() == 0 && obf.getValue() == 0)
							Logger.stdWarn("Skipped more than one instructions in " + classWrapper.originalName + '.' + methodNode.name);
						else
						{
							methodNode.instructions.insertBefore(insn, obf.getKey());
							methodNode.instructions.remove(insn);
						}

						leeway -= obf.getValue();
					}
				}
			}
		}));
		final StringBuilder operationsBuilder = new StringBuilder();
		if (this.xorOperationsEnabled)
			operationsBuilder.append(", ").append(this.xores_used.get()).append(" xor encryptions");
		if (this.arithmeticOperationsEnabled)
			operationsBuilder.append(", ").append(this.simpleMaths_used.get()).append(" simple math sequences");
		if (this.stringLengthOperationsEnabled)
			operationsBuilder.append(", ").append(this.stringLengthCalculations_used.get()).append(" string length calculations");
		if (this.bitwiseOperationsEnabled)
		{
			operationsBuilder.append(", ").append(this.bitwiseAND_used.get()).append(" bitwise AND operations");
			operationsBuilder.append(", ").append(this.bitwiseSHL_used.get()).append(" bitwise shift left operations");
		}

		Logger.stdOut(String.format("Obfuscated %d numbers with %s.", this.affected_numbers.get(), operationsBuilder.toString().substring(2)));
	}

	private Map.Entry<InsnList, Integer> obfuscateInteger(int value)
	{
		final InsnList insnList = new InsnList();
		int leeway = 0;

		if (value == 0 && this.zeroTamperingEnabled)
		{
			final int randomInt = RandomUtils.getRandomInt(100);
			final Map.Entry<InsnList, Integer> obf = this.obfuscateInteger(randomInt);
			final Map.Entry<InsnList, Integer> obf2 = this.obfuscateInteger(randomInt);
			insnList.add(obf.getKey());
			insnList.add(obf2.getKey());
			insnList.add(new InsnNode(ICONST_M1));
			insnList.add(new InsnNode(IXOR));
			insnList.add(new InsnNode(IAND));

			this.xores_used.incrementAndGet();
			this.affected_numbers.incrementAndGet();
			return this.newSimpleEntry(insnList, 3 + obf.getValue() + obf2.getValue());
		}

		if (this.bitwiseOperationsEnabled)
		{
			final int[] shiftOutput = splitToLShiftInteger(value);

			if (shiftOutput[1] > 0)
			{
				final Map.Entry<InsnList, Integer> obf = this.obfuscateInteger(shiftOutput[0]);
				final Map.Entry<InsnList, Integer> obf2 = this.obfuscateInteger(shiftOutput[1]);
				insnList.add(obf.getKey());
				insnList.add(obf2.getKey());
				insnList.add(new InsnNode(ISHL));

				this.bitwiseSHL_used.incrementAndGet();
				this.affected_numbers.incrementAndGet();
				return this.newSimpleEntry(insnList, 1 + obf.getValue() + obf2.getValue());
			}
		}

		final boolean negative = value < 0;
		if (negative)
			value = -value;

		final List<RandomObfuscationMethod> choices = new ArrayList<>();

		if (this.xorOperationsEnabled)
			choices.add(RandomObfuscationMethod.XOR);
		if (this.arithmeticOperationsEnabled)
			choices.add(RandomObfuscationMethod.SimpleMath);
		if (this.stringLengthOperationsEnabled && value > 16 && value < 128)
			choices.add(RandomObfuscationMethod.StringLengthCalculation);
		if (this.bitwiseOperationsEnabled && Math.abs(value) > 0xFF)
			choices.add(RandomObfuscationMethod.bitwiseAND);

		final RandomObfuscationMethod method = RandomObfuscationMethod.random(choices);

		if (method == null)
			return this.newSimpleEntry(new InsnList(), 0); // It will raise exception

		switch (method)
		{
			case XOR:
			{
				final int xorkey = RandomUtils.getRandomInt();
				final int encrypted = value ^ xorkey;

				switch (RandomUtils.getRandomInt(1, 2))
				{
					case 0:
					{
						// Simple XOR only
						if (RandomUtils.getRandomBoolean())
						{
							// reverse
							insnList.add(ASMUtils.getNumberInsn(xorkey));
							insnList.add(ASMUtils.getNumberInsn(encrypted));
						}
						else
						{
							insnList.add(ASMUtils.getNumberInsn(encrypted));
							insnList.add(ASMUtils.getNumberInsn(xorkey));
						}
						insnList.add(new InsnNode(IXOR));

						leeway += 5;
						break;
					}
					case 1:
					{
						// XOR + fake instructions
						final boolean reverse = RandomUtils.getRandomBoolean();
						if (reverse)
							insnList.add(ASMUtils.getNumberInsn(xorkey));
						else
							insnList.add(ASMUtils.getNumberInsn(encrypted));
						insnList.add(ASMUtils.getNumberInsn(RandomUtils.getRandomInt()));
						insnList.add(new InsnNode(SWAP));
						insnList.add(new InsnNode(DUP_X1));
						insnList.add(new InsnNode(POP2));
						if (reverse)
							insnList.add(ASMUtils.getNumberInsn(encrypted));
						else
							insnList.add(ASMUtils.getNumberInsn(xorkey));

						insnList.add(new InsnNode(IXOR));
						leeway += 10;
						break;
					}
				}

				this.xores_used.incrementAndGet();
				this.affected_numbers.incrementAndGet();
				break;
			}

			case SimpleMath:
			{
				switch (RandomUtils.getRandomInt(1, 3))
				{
					case 0:
					{
						final int value1 = RandomUtils.getRandomInt(1, 255) + 20;
						final int value2 = RandomUtils.getRandomInt(1, value1) + value1;
						final int value3 = value - value1 + value2; // You kids say algebra is useless???
						insnList.add(ASMUtils.getNumberInsn(value1));
						insnList.add(ASMUtils.getNumberInsn(value2));
						insnList.add(new InsnNode(ISUB));
						insnList.add(ASMUtils.getNumberInsn(value3));
						insnList.add(new InsnNode(IADD));

						leeway += 8;
						break;
					}
					case 1:
					{
						final int value1 = RandomUtils.getRandomInt(1, 255) + 20;
						final int value2 = RandomUtils.getRandomInt(1, value1) + value1;
						final int value3 = RandomUtils.getRandomInt(1, value2 + 1);
						final int value4 = value - value1 + value2 - value3;

						insnList.add(ASMUtils.getNumberInsn(value1));
						insnList.add(ASMUtils.getNumberInsn(value2));
						insnList.add(new InsnNode(ISUB));
						insnList.add(ASMUtils.getNumberInsn(value3));
						insnList.add(new InsnNode(IADD));
						insnList.add(ASMUtils.getNumberInsn(value4));
						insnList.add(new InsnNode(IADD));

						leeway += 10;
						break;
					}
					case 2:
					{
						final int value1 = RandomUtils.getRandomInt(1, 255) + 20;
						final int value2 = RandomUtils.getRandomInt(1, value1) + value1;
						final int value3 = RandomUtils.getRandomInt(1, value2 + 1);
						final int value4 = RandomUtils.getRandomInt(1, value3 + 1);
						final int value5 = value - value1 + value2 - value3 + value4;

						insnList.add(ASMUtils.getNumberInsn(value1));
						insnList.add(ASMUtils.getNumberInsn(value2));
						insnList.add(new InsnNode(ISUB));
						insnList.add(ASMUtils.getNumberInsn(value3));
						insnList.add(new InsnNode(IADD));
						insnList.add(ASMUtils.getNumberInsn(value4));
						insnList.add(new InsnNode(ISUB));
						insnList.add(ASMUtils.getNumberInsn(value5));
						insnList.add(new InsnNode(IADD));

						leeway += 12;
						break;
					}
				}

				this.simpleMaths_used.incrementAndGet();
				this.affected_numbers.incrementAndGet();
				break;
			}

			case StringLengthCalculation:
			{
				insnList.add(new LdcInsnNode(new StringGenerator().configure(new StringGeneratorParameter().setPattern(StringGeneratorParameter.StringGeneratorPresets.WHITE_SPACES.getPattern()).setLength(value, value)).generate()));
				insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false));

				leeway += 20;

				this.stringLengthCalculations_used.incrementAndGet();
				this.affected_numbers.incrementAndGet();
				break;
			}

			case bitwiseAND:
			{
				final int[] and = splitToAndInteger(value);
				final Map.Entry<InsnList, Integer> obf = this.obfuscateInteger(and[0]);
				final Map.Entry<InsnList, Integer> obf2 = this.obfuscateInteger(and[1]);
				insnList.add(obf.getKey());
				insnList.add(obf2.getKey());
				insnList.add(new InsnNode(IAND));

				leeway += 1 + obf.getValue() + obf2.getValue();

				this.bitwiseAND_used.incrementAndGet();
				this.affected_numbers.incrementAndGet();
				break;
			}
		}

		if (negative)
		{
			insnList.add(new InsnNode(INEG));
			leeway++;
		}

		return this.newSimpleEntry(insnList, leeway);
	}

	private Map.Entry<InsnList, Integer> obfuscateLong(long value)
	{
		final InsnList insnList = new InsnList();

		int leeway = 0;

		if (value == 0 && this.zeroTamperingEnabled)
		{
			final long randomLong = RandomUtils.getRandomLong(100);
			final Map.Entry<InsnList, Integer> obf = this.obfuscateLong(randomLong);
			final Map.Entry<InsnList, Integer> obf2 = this.obfuscateLong(randomLong);
			insnList.add(obf.getKey());
			insnList.add(obf2.getKey());
			insnList.add(new InsnNode(LCONST_1));
			insnList.add(new InsnNode(LNEG));
			insnList.add(new InsnNode(LXOR));
			insnList.add(new InsnNode(LAND));

			this.xores_used.incrementAndGet();
			this.affected_numbers.incrementAndGet();
			return this.newSimpleEntry(insnList, 4 + obf.getValue() + obf2.getValue());
		}

//		if (this.shl)
//		{
//			long[] shiftOutput = splitToLShiftLong(value);
//
//			if (shiftOutput[1] > 0)
//			{
//				Map.Entry<InsnList, Integer> obf = obfuscateLong(shiftOutput[0]);
//				Map.Entry<InsnList, Integer> obf2 = obfuscateLong(shiftOutput[1]);
//				insnList.add(obf.getKey());
//				insnList.add(obf2.getKey());
//				insnList.add(new InsnNode(LSHL));
//				return newSimpleEntry(insnList, 1 + obf.getValue() + obf2.getValue());
//			}
//		}

		final boolean negative = value < 0;
		if (negative)
			value = -value;

		final List<RandomObfuscationMethod> choices = new ArrayList<>();

		if (this.xorOperationsEnabled)
			choices.add(RandomObfuscationMethod.XOR);
		if (this.arithmeticOperationsEnabled)
			choices.add(RandomObfuscationMethod.SimpleMath);
		if (this.stringLengthOperationsEnabled && value < 0x40)
			choices.add(RandomObfuscationMethod.StringLengthCalculation);
		if (this.bitwiseOperationsEnabled && Math.abs(value) > 0xFF)
			choices.add(RandomObfuscationMethod.bitwiseAND);

		final RandomObfuscationMethod method = RandomObfuscationMethod.random(choices);

		if (method == null)
			return this.newSimpleEntry(new InsnList(), 0); // It will raise exception

		switch (method)
		{
			case XOR:
			{
				final long xorkey = RandomUtils.getRandomLong();
				final long encrypted = value ^ xorkey;

				insnList.add(ASMUtils.getNumberInsn(RandomUtils.getRandomLong()));
				insnList.add(ASMUtils.getNumberInsn(xorkey));
				insnList.add(new InsnNode(DUP2_X2));
				insnList.add(new InsnNode(POP2));
				insnList.add(new InsnNode(POP2));
				insnList.add(ASMUtils.getNumberInsn(encrypted));
				insnList.add(new InsnNode(LXOR));

				leeway += 15;

				this.xores_used.incrementAndGet();
				this.affected_numbers.incrementAndGet();
				break;
			}

			case SimpleMath:
			{
				switch (RandomUtils.getRandomInt(1, 3))
				{
					case 0:
					{
						final long value1 = RandomUtils.getRandomInt(1, 255) + 20;
						final long value2 = RandomUtils.getRandomInt(1, (int) value1) + value1;
						final long value3 = value - value1 + value2;

						insnList.add(ASMUtils.getNumberInsn(value1));
						insnList.add(ASMUtils.getNumberInsn(value2));
						insnList.add(new InsnNode(LSUB));
						insnList.add(ASMUtils.getNumberInsn(value3));
						insnList.add(new InsnNode(LADD));

						leeway += 15;
						break;
					}
					case 1:
					{
						final long value1 = RandomUtils.getRandomInt(1, 255) + 20;
						final long value2 = RandomUtils.getRandomInt(1, (int) value1) + value1;
						final long value3 = RandomUtils.getRandomInt(1, (int) value2 + 1);
						final long value4 = value - value1 + value2 - value3;

						insnList.add(ASMUtils.getNumberInsn(value1));
						insnList.add(ASMUtils.getNumberInsn(value2));
						insnList.add(new InsnNode(LSUB));
						insnList.add(ASMUtils.getNumberInsn(value3));
						insnList.add(new InsnNode(LADD));
						insnList.add(ASMUtils.getNumberInsn(value4));
						insnList.add(new InsnNode(LADD));

						leeway += 17;
						break;
					}
					case 2:
					{
						final long value1 = RandomUtils.getRandomInt(1, 255) + 20;
						final long value2 = RandomUtils.getRandomInt(1, (int) value1) + value1;
						final long value3 = RandomUtils.getRandomInt(1, (int) value2 + 1);
						final long value4 = RandomUtils.getRandomInt(1, (int) value3 + 1);
						final long value5 = value - value1 + value2 - value3 + value4;

						insnList.add(ASMUtils.getNumberInsn(value1));
						insnList.add(ASMUtils.getNumberInsn(value2));
						insnList.add(new InsnNode(LSUB));
						insnList.add(ASMUtils.getNumberInsn(value3));
						insnList.add(new InsnNode(LADD));
						insnList.add(ASMUtils.getNumberInsn(value4));
						insnList.add(new InsnNode(LSUB));
						insnList.add(ASMUtils.getNumberInsn(value5));
						insnList.add(new InsnNode(LADD));

						leeway += 20;
						break;
					}
				}

				this.simpleMaths_used.incrementAndGet();
				this.affected_numbers.incrementAndGet();
				break;
			}

			case StringLengthCalculation:
			{
				insnList.add(new LdcInsnNode(new StringGenerator().configure(new StringGeneratorParameter().setPattern(StringGeneratorParameter.StringGeneratorPresets.WHITE_SPACES.getPattern()).setLength((int) value, (int) value)).generate()));
				insnList.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false));
				insnList.add(new InsnNode(I2L));

				this.stringLengthCalculations_used.incrementAndGet();
				this.affected_numbers.incrementAndGet();
				break;
			}

			case bitwiseAND:
			{
				final long[] and = splitToAndLong(value);
				final Map.Entry<InsnList, Integer> obf = this.obfuscateLong(and[0]);
				final Map.Entry<InsnList, Integer> obf2 = this.obfuscateLong(and[1]);
				insnList.add(obf.getKey());
				insnList.add(obf2.getKey());
				insnList.add(new InsnNode(LAND));

				leeway += 1 + obf.getValue() + obf2.getValue();

				this.bitwiseAND_used.incrementAndGet();
				this.affected_numbers.incrementAndGet();
				break;
			}
		}

		if (negative)
		{
			insnList.add(new InsnNode(LNEG));
			leeway++;
		}

		return this.newSimpleEntry(insnList, leeway);
	}

	/**
	 * @author superblaubeere27
	 */
	private static int[] splitToAndInteger(final int number)
	{
		final int number2 = RandomUtils.getRandomInt(Short.MAX_VALUE) & ~number;

		return new int[]
		{
				~number2, number2 | number
		};
	}

	/**
	 * @author superblaubeere27
	 */
	private static long[] splitToAndLong(final long number)
	{
		final long number2 = RandomUtils.getRandomInt(Short.MAX_VALUE) & ~number;

		return new long[]
		{
				~number2, number2 | number
		};
	}

	/**
	 * @author superblaubeere27
	 */
	private static int[] splitToLShiftInteger(int number)
	{
		int shift = 0;

		while ((number & ~0x7ffffffffffffffEL) == 0 && number != 0)
		{
			number = number >> 1;
			shift++;
		}
		return new int[]
		{
				number, shift
		};
	}

// not working
//	private static long[] splitToLShiftLong(long number)
//	{
//		int shift = 0;
//
//		while ((number & ~0x7ffffffffffffffEL) == 0 && number != 0)
//		{
//			number = number >> 1;
//			shift++;
//		}
//		return new long[]
//		{
//				number, shift
//		};
//	}

	@Override
	public String getName()
	{
		return "Number Obfuscation";
	}

	private <K, V> Map.Entry<K, V> newSimpleEntry(final K key, final V value)
	{
		return new AbstractMap.SimpleEntry<>(key, value);
	}
}
