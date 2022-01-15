package com.eric0210.obfuscater.transformer.obfuscator.flow;

import java.io.IOError;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.eric0210.obfuscater.Logger;
import com.eric0210.obfuscater.asm.ClassWrapper;
import com.eric0210.obfuscater.utils.RandomUtils;

import org.objectweb.asm.tree.*;

/**
 * Traps random instructions using a fake handler. Essentially the same thing as Zelix's exception obfuscation or Dasho's fake try catches.
 *
 * @author ItzSomebody
 */
public class FakeCatchBlocks extends FlowObfuscation
{
	private static final String[] HANDLER_NAMES =
	{
			RuntimeException.class.getName().replace('.', '/'), LinkageError.class.getName().replace('.', '/'), Error.class.getName().replace('.', '/'), Exception.class.getName().replace('.', '/'), Throwable.class.getName().replace('.', '/'), IllegalArgumentException.class.getName().replace('.', '/'), IllegalStateException.class.getName().replace('.', '/'), IllegalAccessError.class.getName().replace('.', '/'), InvocationTargetException.class.getName().replace('.', '/'), IOException.class.getName().replace('.', '/'), IOError.class.getName().replace('.', '/'),
	};

	@Override
	public void transform()
	{
		AtomicInteger counter = new AtomicInteger();

		int fakeHandlerClasses = getClassWrappers().size() / 5;
		if (fakeHandlerClasses == 0)
			fakeHandlerClasses = 1;

		ClassNode[] fakeHandlers = new ClassNode[fakeHandlerClasses];
		for (int i = 0; i < fakeHandlerClasses; i++)
		{
			ClassNode classNode = new ClassNode();
			classNode.superName = HANDLER_NAMES[RandomUtils.getRandomInt(HANDLER_NAMES.length)];
			classNode.name = "FAKECATCHBLOCKS_" + RandomUtils.getRandomInt();
			classNode.access = ACC_PUBLIC | ACC_SUPER;

			fakeHandlers[i] = classNode;
		}

		getClassWrappers().stream().filter(classWrapper -> !isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.stream().filter(methodWrapper -> !isExcluded(methodWrapper) && hasInstructions(methodWrapper.methodNode)).forEach(methodWrapper ->
		{
			MethodNode methodNode = methodWrapper.methodNode;

			int leeway = getSizeLeeway(methodNode);

			for (AbstractInsnNode insn : methodNode.instructions.toArray())
			{
				if (leeway < 10000)
					return;

				if (RandomUtils.getRandomInt(10) > 5)
				{
					LabelNode trapStart = new LabelNode();
					LabelNode trapEnd = new LabelNode();
					LabelNode catchStart = new LabelNode();
					LabelNode catchEnd = new LabelNode();

					InsnList catchBlock = new InsnList();
					catchBlock.add(catchStart);
					catchBlock.add(new InsnNode(DUP));
					catchBlock.add(new InsnNode(POP));
					catchBlock.add(new InsnNode(ATHROW));
					catchBlock.add(catchEnd);

					methodNode.instructions.insertBefore(insn, trapStart);
					methodNode.instructions.insert(insn, trapEnd);
					methodNode.instructions.insert(insn, new JumpInsnNode(GOTO, catchEnd));
					methodNode.instructions.insert(insn, catchBlock);

					methodNode.tryCatchBlocks.add(new TryCatchBlockNode(trapStart, trapEnd, catchStart, fakeHandlers[RandomUtils.getRandomInt(fakeHandlers.length)].name));

					leeway -= 15;
					counter.incrementAndGet();
				}
			}
		}));

		Stream.of(fakeHandlers).forEach(classNode -> getClasses().put(classNode.name, new ClassWrapper(classNode, false)));

		Logger.stdOut("Created " + fakeHandlerClasses + " fake handler classes");
		Logger.stdOut("Inserted " + counter.get() + " fake try catches");
	}
}
