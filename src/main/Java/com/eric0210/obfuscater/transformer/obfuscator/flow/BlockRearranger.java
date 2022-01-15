package com.eric0210.obfuscater.transformer.obfuscator.flow;

import java.util.ArrayList;

import org.objectweb.asm.tree.*;

/**
 * This splits a method's block of code into two blocks: P1 and P2 and then inserting P2 behind P1.
 * <p>
 * P1->P2 becomes GOTO_P1->P2->P1->GOTO_P2
 * <p>
 * FIXME: breaks stuff.
 *
 * @author ItzSomebody
 */
public class BlockRearranger extends FlowObfuscation
{
	@Override
	public void transform()
	{
		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.stream().filter(methodWrapper -> !isExcluded(methodWrapper)).forEach(methodWrapper ->
		{
			MethodNode methodNode = methodWrapper.methodNode;

			if (methodNode.instructions.size() > 10)
			{
				LabelNode p1 = new LabelNode();
				LabelNode p2 = new LabelNode();

				AbstractInsnNode p2Start = methodNode.instructions.get((methodNode.instructions.size() - 1) / 2);
				AbstractInsnNode p2End = methodNode.instructions.getLast();

				AbstractInsnNode p1Start = methodNode.instructions.getFirst();

				ArrayList<AbstractInsnNode> insnNodes = new ArrayList<>();
				AbstractInsnNode currentInsn = p1Start;

				InsnList p1Block = new InsnList();

				while (currentInsn != p2Start)
				{
					insnNodes.add(currentInsn);

					currentInsn = currentInsn.getNext();
				}

				insnNodes.forEach(insn ->
				{
					methodNode.instructions.remove(insn);
					p1Block.add(insn);
				});

				p1Block.insert(p1);
				p1Block.add(new JumpInsnNode(GOTO, p2));

				methodNode.instructions.insert(p2End, p1Block);
				methodNode.instructions.insertBefore(p2Start, new JumpInsnNode(GOTO, p1));
				methodNode.instructions.insertBefore(p2Start, p2);
			}
		}));
	}
}
