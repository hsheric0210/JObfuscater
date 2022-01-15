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
 * Splits integer and long constants into random bitwise operations.
 *
 * @author ItzSomebody
 */
public class BitwiseObfuscator extends NumberObfuscation
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.stream().filter(methodWrapper -> !isExcluded(methodWrapper) && hasInstructions(methodWrapper)).forEach(methodWrapper ->
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
			}
		}));

		Logger.stdOut("Split " + counter.get() + " number constants into bitwise instructions");
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
					insns.add(new InsnNode(IAND));

					current &= operand;
					break;
				case 1:
					operand = randomInt(current);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(IOR));

					current |= operand;
					break;
				case 2:
					operand = randomInt(current);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(IXOR));

					current &= operand;
					break;
				case 3:
					operand = RandomUtils.getRandomInt(1, 5);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(ISHL));

					current <<= operand;
					break;
				case 4:
					operand = RandomUtils.getRandomInt(1, 5);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(ISHR));

					current >>= operand;
					break;
				case 5:
				default:
					operand = RandomUtils.getRandomInt(1, 5);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(IUSHR));

					current >>>= operand;
					break;
			}
		}

		int correctionOperand = originalNum ^ current;
		insns.add(ASMUtils.getNumberInsn(correctionOperand));
		insns.add(new InsnNode(IXOR));

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
					insns.add(new InsnNode(LAND));

					current &= operand;
					break;
				case 1:
					operand = randomLong(current);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(LOR));

					current |= operand;
					break;
				case 2:
					operand = randomLong(current);

					insns.add(ASMUtils.getNumberInsn(operand));
					insns.add(new InsnNode(LXOR));

					current &= operand;
					break;
				case 3:
					operand = RandomUtils.getRandomInt(1, 32);

					insns.add(ASMUtils.getNumberInsn((int) operand));
					insns.add(new InsnNode(LSHL));

					current <<= operand;
					break;
				case 4:
					operand = RandomUtils.getRandomInt(1, 32);

					insns.add(ASMUtils.getNumberInsn((int) operand));
					insns.add(new InsnNode(LSHR));

					current >>= operand;
					break;
				case 5:
				default:
					operand = RandomUtils.getRandomInt(1, 32);

					insns.add(ASMUtils.getNumberInsn((int) operand));
					insns.add(new InsnNode(LUSHR));

					current >>>= operand;
					break;
			}
		}

		long correctionOperand = originalNum ^ current;
		insns.add(ASMUtils.getNumberInsn(correctionOperand));
		insns.add(new InsnNode(LXOR));

		return insns;
	}
}
