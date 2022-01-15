package com.eric0210.obfuscater.transformer.obfuscator.flow;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.utils.ASMUtils;
import com.eric0210.obfuscater.utils.RandomUtils;

import org.objectweb.asm.tree.*;

/**
 * Combines the handler region of a try block into its trap region. The idea of this is from https://github.com/Janmm14/decompiler-vulnerabilities-and-bugs/blob/master/DVB/DVB-0004.md.
 * <p>
 * To achieve this, we first change the handler start to the trap start then we insert a condition at the start of the trap which indicates if execution should move into the trap or catch region.
 * <p>
 * FIXME: breaks almost everything
 *
 * @author ItzSombody
 */
public class TryCatchCombiner extends FlowObfuscation
{
	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.stream().filter(methodWrapper -> !isExcluded(methodWrapper)).forEach(methodWrapper ->
		{
			MethodNode methodNode = methodWrapper.methodNode;

			HashSet<LabelNode> starts = new HashSet<>();
			if (methodNode.tryCatchBlocks.stream().anyMatch(tcbn -> !starts.add(tcbn.start)))
				return;

			methodNode.tryCatchBlocks.stream().filter(tcbn -> tcbn.start != tcbn.handler).forEach(tcbn ->
			{
				int index = methodNode.maxLocals;
				methodNode.maxLocals++;

				LabelNode handler = tcbn.handler;
				LabelNode init = new LabelNode();
				LabelNode back = new LabelNode();

				tcbn.handler = tcbn.start;

				InsnList preTrap = new InsnList();
				preTrap.add(new InsnNode(ICONST_0));
				preTrap.add(new VarInsnNode(ISTORE, index));
				preTrap.add(new JumpInsnNode(GOTO, init));
				preTrap.add(back);

				InsnList initSub = new InsnList();
				initSub.add(init);
				initSub.add(new InsnNode(ACONST_NULL));
				initSub.add(new JumpInsnNode(GOTO, back));

				InsnList startCondition = new InsnList();
				startCondition.add(new VarInsnNode(ILOAD, index));
				startCondition.add(new JumpInsnNode(IFNE, handler));
				startCondition.add(new InsnNode(POP));
				startCondition.add(ASMUtils.getNumberInsn(RandomUtils.getRandomInt(1, 20)));
				startCondition.add(new VarInsnNode(ISTORE, index));

				InsnList resetCondition = new InsnList();
				resetCondition.add(new InsnNode(ICONST_0));
				resetCondition.add(new VarInsnNode(ISTORE, index));

				methodNode.instructions.insert(methodNode.instructions.getLast(), initSub);
				methodNode.instructions.insert(tcbn.start, startCondition);
				methodNode.instructions.insertBefore(tcbn.start, preTrap);
				methodNode.instructions.insertBefore(tcbn.end, resetCondition);

				counter.incrementAndGet();
			});
		}));

		Logger.stdOut("Combined " + counter.incrementAndGet() + " try blocks with their catches.");
	}
}
