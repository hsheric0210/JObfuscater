package com.eric0210.obfuscater.transformer.obfuscator.number;

import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.utils.ASMUtils;
import com.eric0210.obfuscater.utils.RandomUtils;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Splits number constants into arithmetic operations.
 *
 * @author ItzSomebody
 */
public class ArithmeticObfuscator extends NumberObfuscation
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.stream().filter(methodWrapper -> !isExcluded(methodWrapper)).forEach(methodWrapper ->
		{
			MethodNode methodNode = methodWrapper.methodNode;
			int leeway = getSizeLeeway(methodNode);

			for (AbstractInsnNode insn : methodNode.instructions.toArray())
			{
				if (leeway < 10000)
					break;

				if (ASMUtils.isIntInsn(insn) && integerTamperingEnabled)
				{
					InsnList insns = obfuscateInt(ASMUtils.getIntegerFromInsn(insn));

					methodNode.instructions.insert(insn, insns);
					methodNode.instructions.remove(insn);

					counter.incrementAndGet();
				}
				else if (ASMUtils.isLongInsn(insn) && longTamperingEnabled)
				{
					InsnList insns = obfuscateLong(ASMUtils.getLongFromInsn(insn));

					methodNode.instructions.insert(insn, insns);
					methodNode.instructions.remove(insn);

					counter.incrementAndGet();
				}
				else if (ASMUtils.isFloatInsn(insn) && floatTamperingEnabled)
				{
					InsnList insns = obfuscateFloat(ASMUtils.getFloatFromInsn(insn));

					methodNode.instructions.insert(insn, insns);
					methodNode.instructions.remove(insn);

					counter.incrementAndGet();
				}
				else if (ASMUtils.isDoubleInsn(insn) && doubleTamperingEnabled)
				{
					InsnList insns = obfuscateDouble(ASMUtils.getDoubleFromInsn(insn));

					methodNode.instructions.insert(insn, insns);
					methodNode.instructions.remove(insn);

					counter.incrementAndGet();
				}
			}
		}));

		Logger.stdOut("Split " + counter.get() + " number constants into arithmetic instructions");
	}

	private InsnList obfuscateInt(int originalNum)
	{
		int current = randomInt(originalNum);

		InsnList insns = new InsnList();
		insns.add(ASMUtils.getNumberInsn(current));

		for (int i = 0, j = RandomUtils.getRandomInt(minRepeatCount, maxRepeatCount); i < j; i++)
		{
			int operand;

			switch (RandomUtils.getRandomInt(6))
			{
				case 0:
					operand = randomInt(current);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(IADD));

					current += operand;
					break;
				case 1:
					operand = randomInt(current);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(ISUB));

					current -= operand;
					break;
				case 2:
					operand = RandomUtils.getRandomInt(1, 255);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(IMUL));

					current *= operand;
					break;
				case 3:
					operand = RandomUtils.getRandomInt(1, 255);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(IDIV));

					current /= operand;
					break;
				case 4:
				default:
					operand = RandomUtils.getRandomInt(1, 255);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(IREM));

					current %= operand;
					break;
			}
		}

		int correctionOperand = originalNum - current;
		insns.add(ASMUtils.getNumberInsn(correctionOperand));
		insns.add(new InsnNode(IADD));

		return insns;
	}

	private InsnList obfuscateLong(long originalNum)
	{
		long current = randomLong(originalNum);

		InsnList insns = new InsnList();
		insns.add(ASMUtils.getNumberInsn(current));

		for (int i = 0, j = RandomUtils.getRandomInt(minRepeatCount, maxRepeatCount); i < j; i++)
		{
			long operand;

			switch (RandomUtils.getRandomInt(6))
			{
				case 0:
					operand = randomLong(current);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(LADD));

					current += operand;
					break;
				case 1:
					operand = randomLong(current);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(LSUB));

					current -= operand;
					break;
				case 2:
					operand = RandomUtils.getRandomInt(1, 65535);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(LMUL));

					current *= operand;
					break;
				case 3:
					operand = RandomUtils.getRandomInt(1, 65535);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(LDIV));

					current /= operand;
					break;
				case 4:
				default:
					operand = RandomUtils.getRandomInt(1, 255);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(LREM));

					current %= operand;
					break;
			}
		}

		long correctionOperand = originalNum - current;
		insns.add(ASMUtils.getNumberInsn(correctionOperand));
		insns.add(new InsnNode(LADD));

		return insns;
	}

	private InsnList obfuscateFloat(float originalNum)
	{
		float current = randomFloat(originalNum);

		InsnList insns = new InsnList();
		insns.add(ASMUtils.getNumberInsn(current));

		for (int i = 0, j = RandomUtils.getRandomInt(minRepeatCount, maxRepeatCount); i < j; i++)
		{
			float operand;

			switch (RandomUtils.getRandomInt(6))
			{
				case 0:
					operand = randomFloat(current);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(FADD));

					current += operand;
					break;
				case 1:
					operand = randomFloat(current);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(FSUB));

					current -= operand;
					break;
				case 2:
					operand = RandomUtils.getRandomInt(1, 65535);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(FMUL));

					current *= operand;
					break;
				case 3:
					operand = RandomUtils.getRandomInt(1, 65535);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(FDIV));

					current /= operand;
					break;
				case 4:
				default:
					operand = RandomUtils.getRandomInt(1, 255);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(FREM));

					current %= operand;
					break;
			}
		}

		float correctionOperand = originalNum - current;
		insns.add(ASMUtils.getNumberInsn(correctionOperand));
		insns.add(new InsnNode(FADD));

		return insns;
	}

	private InsnList obfuscateDouble(double originalNum)
	{
		double current = randomDouble(originalNum);

		InsnList insns = new InsnList();
		insns.add(ASMUtils.getNumberInsn(current));

		for (int i = 0, j = RandomUtils.getRandomInt(minRepeatCount, maxRepeatCount); i < j; i++)
		{
			double operand;

			switch (RandomUtils.getRandomInt(6))
			{
				case 0:
					operand = randomDouble(current);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(DADD));

					current += operand;
					break;
				case 1:
					operand = randomDouble(current);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(DSUB));

					current -= operand;
					break;
				case 2:
					operand = RandomUtils.getRandomInt(1, 65535);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(DMUL));

					current *= operand;
					break;
				case 3:
					operand = RandomUtils.getRandomInt(1, 65535);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(DDIV));

					current /= operand;
					break;
				case 4:
				default:
					operand = RandomUtils.getRandomInt(1, 255);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(DREM));

					current %= operand;
					break;
			}
		}

		double correctionOperand = originalNum - current;
		insns.add(ASMUtils.getNumberInsn(correctionOperand));
		insns.add(new InsnNode(DADD));

		return insns;
	}
}
