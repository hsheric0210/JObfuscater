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
import org.objectweb.asm.tree.MethodNode;

/**
 * Removes all NOPs found. Do note that ASM's MethodWriter will replace unreachable instructions with NOPs so you might find NOPs in your program even after you ran this transformer on it.
 *
 * @author ItzSomebody
 */
public class NOPRemover extends Optimizer {
	@Override
	public final void transform()
	{
		final AtomicInteger affects = new AtomicInteger();
		final long current = System.currentTimeMillis();
		this.getClassWrappers().stream().filter(classWrapper -> !this.isExcluded(classWrapper)).forEach(classWrapper -> classWrapper.methods.stream().filter(methodWrapper -> !this.isExcluded(methodWrapper) && this.hasInstructions(methodWrapper.methodNode)).forEach(methodWrapper ->
		{
			final MethodNode methodNode = methodWrapper.methodNode;
			Stream.of(methodNode.instructions.toArray()).filter(insn -> insn.getOpcode() == NOP).forEach(insn -> methodNode.instructions.remove(insn));
		}));
		Logger.stdOut(String.format("Removed %d NOP instructions. [%dms]", affects.get(), this.tookThisLong(current)));

	}

	@Override
	public final String getName()
	{
		return "NOP Remover";
	}
}
