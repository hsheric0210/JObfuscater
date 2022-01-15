package com.eric0210.obfuscater.transformer.obfuscator.flow;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.asm.ClassWrapper;
import com.eric0210.obfuscater.utils.RandomUtils;

import org.objectweb.asm.tree.*;

/**
 * Replaces GOTO instructions with an expression which is always true. This does nothing more than adding a one more edge to a control flow graph for every GOTO instruction present.
 *
 * @author ItzSomebody
 */
public class GotoReplacer extends FlowObfuscation
{
	private static final int PRED_ACCESS = ACC_PUBLIC | ACC_STATIC | ACC_SYNTHETIC;

	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		int fieldInjections = getClassWrappers().size() / 5;
		if (fieldInjections == 0)
			fieldInjections = 1;

		FieldNode[] predicates = new FieldNode[fieldInjections];
		for (int i = 0; i < fieldInjections; i++)
			predicates[i] = new FieldNode(PRED_ACCESS, "GOTOREPLACE_" + RandomUtils.getRandomInt(), "Z", null, null);

		ClassNode[] predicateClasses = new ClassNode[fieldInjections];
		ArrayList<ClassWrapper> wrappers = new ArrayList<>(getClassWrappers());
		for (int i = 0; i < fieldInjections; i++)
		{
			predicateClasses[i] = wrappers.get(RandomUtils.getRandomInt(1, wrappers.size())).classNode;

			if (predicateClasses[i].fields == null)
				predicateClasses[i].fields = new ArrayList<>(1);

			predicateClasses[i].fields.add(predicates[i]);
		}

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.stream().filter(methodWrapper -> !isExcluded(methodWrapper) && hasInstructions(methodWrapper.methodNode)).forEach(methodWrapper ->
		{
			MethodNode methodNode = methodWrapper.methodNode;

			int leeway = getSizeLeeway(methodNode);
			int varIndex = methodNode.maxLocals;
			methodNode.maxLocals++; // Prevents breaking of other transformers which rely on this field.

			for (AbstractInsnNode insn : methodNode.instructions.toArray())
			{
				if (leeway < 10000)
					break;

				if (insn.getOpcode() == GOTO)
				{
					methodNode.instructions.insertBefore(insn, new VarInsnNode(ILOAD, varIndex));
					methodNode.instructions.insertBefore(insn, new JumpInsnNode(IFEQ, ((JumpInsnNode) insn).label));
					methodNode.instructions.insert(insn, new InsnNode(ATHROW));
					methodNode.instructions.insert(insn, new InsnNode(ACONST_NULL));
					methodNode.instructions.remove(insn);

					leeway -= 10;
				}
			}

			int index = RandomUtils.getRandomInt(predicateClasses.length);

			methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), new VarInsnNode(ISTORE, varIndex));
			methodNode.instructions.insertBefore(methodNode.instructions.getFirst(), new FieldInsnNode(GETSTATIC, predicateClasses[index].name, predicates[index].name, "Z"));
		}));

		Logger.stdOut("Swapped " + counter.get() + " GOTO instructions");
	}
}
