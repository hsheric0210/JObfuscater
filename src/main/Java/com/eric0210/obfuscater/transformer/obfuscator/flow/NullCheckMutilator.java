package com.eric0210.obfuscater.transformer.obfuscator.flow;

import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.utils.RandomUtils;

import org.objectweb.asm.tree.*;

/**
 * Replaces IFNONNULL and IFNULL with a semantically equivalent try-catch block. This relies on the fact that {@link NullPointerException} is thrown when a method is invoked upon null. FIXME: breaks some stuff.
 *
 * @author ItzSomebody
 */
public class NullCheckMutilator extends FlowObfuscation
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.stream().filter(methodWrapper -> !isExcluded(methodWrapper) && hasInstructions(methodWrapper.methodNode)).forEach(methodWrapper ->
		{
			MethodNode methodNode = methodWrapper.methodNode;

			int leeway = getSizeLeeway(methodNode);

			for (AbstractInsnNode insn : methodNode.instructions.toArray())
			{
				if (leeway < 10000)
					break;

				if (insn.getOpcode() == IFNULL || insn.getOpcode() == IFNONNULL)
				{
					JumpInsnNode jump = (JumpInsnNode) insn;

					LabelNode trapStart = new LabelNode();
					LabelNode trapEnd = new LabelNode();
					LabelNode catchStart = new LabelNode();
					LabelNode catchEnd = new LabelNode();

					InsnList insns = new InsnList();
					insns.add(trapStart);
					switch (RandomUtils.getRandomInt(1, 4))
					{
						case 0:
							insns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false));
							break;
						case 1:
							insns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false));
							break;
						case 2:
							insns.add(new InsnNode(ACONST_NULL));
							insns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z", false));
							break;
						case 3:
						default:
							insns.add(new MethodInsnNode(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false));
							break;
					}

					insns.add(new InsnNode(POP));
					insns.add(trapEnd);

					if (insn.getOpcode() == IFNONNULL)
						insns.add(new JumpInsnNode(GOTO, jump.label));
					else
						insns.add(new JumpInsnNode(GOTO, catchEnd));

					insns.add(catchStart);
					insns.add(new InsnNode(POP));

					if (insn.getOpcode() == IFNULL)
						insns.add(new JumpInsnNode(GOTO, jump.label));

					insns.add(catchEnd);

					methodNode.instructions.insert(insn, insns);
					methodNode.instructions.remove(insn);
					methodNode.tryCatchBlocks.add(new TryCatchBlockNode(trapStart, trapEnd, catchStart, "java/lang/NullPointerException"));
				}
			}
		}));

		Logger.stdOut("Mutilated " + counter.get() + " null checks");
	}
}
