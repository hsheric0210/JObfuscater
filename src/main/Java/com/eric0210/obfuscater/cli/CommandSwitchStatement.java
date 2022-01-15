/*
 * Copyright (C) 2019 ItzSomebody
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.eric0210.obfuscater.cli;

public class CommandSwitchStatement
{
	private final String name;
	private final int nArgs;

	public CommandSwitchStatement(String name, int nArgs)
	{
		this.name = name;
		this.nArgs = nArgs;
	}

	public String getName()
	{
		return name;
	}

	public int getnArgs()
	{
		return nArgs;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof CommandSwitchStatement)
			return ((CommandSwitchStatement) obj).getName().equals(this.getName()) && ((CommandSwitchStatement) obj).getnArgs() == this.getnArgs();

		return false;
	}
}
