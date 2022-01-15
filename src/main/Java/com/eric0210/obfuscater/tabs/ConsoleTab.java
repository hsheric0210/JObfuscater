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
package com.eric0210.obfuscater.tabs;

import java.awt.*;
import java.io.OutputStream;
import java.io.PrintStream;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.eric0210.obfuscater.Constants;

/**
 * A {@link JPanel} containing a {@link JTextArea} which all System.out and System.err is redirected to.
 *
 * @author ItzSomebody
 */
public class ConsoleTab extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8797836546192853649L;
	/**
	 * The {@link JTextArea} System.out and System.err is redirected to.
	 */
	private final JTextArea consoleTextArea;

	/**
	 * The {@link PrintStream} System.out and System.err is redirected to.
	 */
	public final PrintStream printStream;

	public ConsoleTab()
	{
		final GridBagLayout gbl_this = new GridBagLayout();
		gbl_this.columnWidths = new int[]
		{
				0, 0
		};
		gbl_this.rowHeights = new int[]
		{
				0, 0
		};
		gbl_this.columnWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		gbl_this.rowWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		this.setBorder(new TitledBorder("Console"));
		this.setLayout(gbl_this);
		final JScrollPane consoleScrollPane = new JScrollPane();
		final GridBagConstraints gbc_consoleScrollPane = new GridBagConstraints();
		gbc_consoleScrollPane.fill = GridBagConstraints.BOTH;
		gbc_consoleScrollPane.gridx = 0;
		gbc_consoleScrollPane.gridy = 0;
		this.add(consoleScrollPane, gbc_consoleScrollPane);
		this.consoleTextArea = new JTextArea();
		this.consoleTextArea.setEditable(false);
		final StringBuilder bw = new StringBuilder();
		bw.append("##############################################\n");
		bw.append("Eric's Java Obfuscater\n");
		bw.append("##############################################\n");
		bw.append('\n');
		bw.append('\n');
		bw.append("Version: ").append(Constants.VERSION).append('\n');
		bw.append("Contributors: ").append(Constants.CONTRIBUTORS).append('\n');
		this.consoleTextArea.setText(bw.toString());
		consoleScrollPane.setViewportView(this.consoleTextArea);
		this.printStream = new PrintStream(new OutputStreamRedirect(this.consoleTextArea)); // This can't be closed until program is get shut-downed
		System.setOut(this.printStream);
		System.setErr(this.printStream);
	}

	/**
	 * Custom {@link OutputStream}.
	 */
	static final class OutputStreamRedirect extends OutputStream
	{
		/**
		 * {@link JTextArea} System.out and System.err is redirected to.
		 */
		private final JTextArea consoleOutput;

		OutputStreamRedirect(final JTextArea consoleOutput)
		{
			this.consoleOutput = consoleOutput;
		}

		@Override
		public void write(final int b)
		{
			this.consoleOutput.append(String.valueOf((char) b));
			this.consoleOutput.setCaretPosition(this.consoleOutput.getDocument().getLength());
		}
	}

	/**
	 * Clears the {@link JTextArea} System.out and System.err is redirected to.
	 */
	public final void resetConsole()
	{
		this.consoleTextArea.setText(null);
	}
}
