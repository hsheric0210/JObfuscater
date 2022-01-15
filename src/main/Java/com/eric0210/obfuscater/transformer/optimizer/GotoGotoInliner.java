/*
 * Copyright (C) 2018 ItzSomebody This program is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>
 */
package com.eric0210.obfuscater.transformer.optimizer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.eric0210.obfuscater.Logger;

import com.eric0210.obfuscater.transformer.Transformer;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Normalizes goto-goto sequences by directing the first goto's target to the second goto's target.
 *
 * @author ItzSomebody.
 */
public class GotoGotoInliner extends Optimizer {
	@Override
	public final void transform()
	{
		final AtomicInteger affects = new AtomicInteger();
		final long current = System.currentTimeMillis();
		this.getClassWrappers().parallelStream().filter(classWrapper -> !this.isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.parallelStream().filter(methodWrapper -> !this.isExcluded(methodWrapper) && this.hasInstructions(methodWrapper.methodNode)).forEach(methodWrapper ->
		{
			final MethodNode methodNode = methodWrapper.methodNode;
			Stream.of(methodNode.instructions.toArray()).filter(insn -> insn.getOpcode() == GOTO).forEach(insn ->
			{
				final JumpInsnNode gotoJump = (JumpInsnNode) insn;
				final AbstractInsnNode insnAfterTarget = gotoJump.label.getNext();
				if (insnAfterTarget != null && insnAfterTarget.getOpcode() == GOTO)
				{
					final JumpInsnNode secGoto = (JumpInsnNode) insnAfterTarget;
					gotoJump.label = secGoto.label;
					affects.incrementAndGet();
				}
			});
		}));
		Logger.stdOut(String.format("Inlined %d GOTO -> GOTO sequences. [%dms]", affects.get(), this.tookThisLong(current)));
	}

	@Override
	public final String getName()
	{
		return "GOTO -> GOTO Inliner";
	}
}
