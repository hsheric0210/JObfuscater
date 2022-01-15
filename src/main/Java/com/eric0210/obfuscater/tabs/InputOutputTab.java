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
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.eric0210.obfuscater.config.ObfuscationConfiguration;

public class InputOutputTab extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1006217998731895419L;
	private final JTextField inputField;
	private final JTextField outputField;
	private final DefaultListModel<String> libraryList;
	private File lastPath;

	public InputOutputTab()
	{
		final GridBagLayout gbl_this = new GridBagLayout();
		gbl_this.columnWidths = new int[]
		{
				0, 0
		};
		gbl_this.rowHeights = new int[]
		{
				0, 378, 0
		};
		gbl_this.columnWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		gbl_this.rowWeights = new double[]
		{
				0.0, 1.0, Double.MIN_VALUE
		};
		this.setLayout(gbl_this);
		final JPanel inputOutputPanel = new JPanel();
		final GridBagConstraints gbc_inputOutputPanel = new GridBagConstraints();
		gbc_inputOutputPanel.insets = new Insets(0, 0, 5, 0);
		gbc_inputOutputPanel.fill = GridBagConstraints.BOTH;
		gbc_inputOutputPanel.gridx = 0;
		gbc_inputOutputPanel.gridy = 0;
		inputOutputPanel.setBorder(new TitledBorder("Input-Output"));
		this.add(inputOutputPanel, gbc_inputOutputPanel);
		final GridBagLayout gbl_inputOutputPanel = new GridBagLayout();
		gbl_inputOutputPanel.columnWidths = new int[]
		{
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
		};
		gbl_inputOutputPanel.rowHeights = new int[]
		{
				0, 0, 0
		};
		gbl_inputOutputPanel.columnWeights = new double[]
		{
				0.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE
		};
		gbl_inputOutputPanel.rowWeights = new double[]
		{
				0.0, 0.0, Double.MIN_VALUE
		};
		inputOutputPanel.setLayout(gbl_inputOutputPanel);
		final JLabel inputLabel = new JLabel("Input:");
		final GridBagConstraints gbc_inputLabel = new GridBagConstraints();
		gbc_inputLabel.anchor = GridBagConstraints.EAST;
		gbc_inputLabel.insets = new Insets(5, 5, 5, 5);
		gbc_inputLabel.gridx = 0;
		gbc_inputLabel.gridy = 0;
		inputOutputPanel.add(inputLabel, gbc_inputLabel);
		this.inputField = new JTextField();
		final GridBagConstraints gbc_inputField = new GridBagConstraints();
		gbc_inputField.gridwidth = 17;
		gbc_inputField.insets = new Insets(5, 0, 5, 5);
		gbc_inputField.fill = GridBagConstraints.BOTH;
		gbc_inputField.gridx = 1;
		gbc_inputField.gridy = 0;
		inputOutputPanel.add(this.inputField, gbc_inputField);
		this.inputField.setColumns(10);
		final JButton inputButton = new JButton("Select");
		final GridBagConstraints gbc_inputButton = new GridBagConstraints();
		gbc_inputButton.fill = GridBagConstraints.BOTH;
		gbc_inputButton.insets = new Insets(5, 0, 5, 5);
		gbc_inputButton.gridx = 18;
		gbc_inputButton.gridy = 0;
		inputButton.addActionListener(e ->
		{
			final JFileChooser chooser = new JFileChooser();
			if (this.inputField.getText() != null && !this.inputField.getText().isEmpty())
				chooser.setSelectedFile(new File(this.inputField.getText()));
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (this.lastPath != null)
				chooser.setCurrentDirectory(this.lastPath);
			final int result = chooser.showOpenDialog(this);
			if (result == 0)
				SwingUtilities.invokeLater(() ->
				{
					this.inputField.setText(chooser.getSelectedFile().getAbsolutePath());
					this.lastPath = chooser.getSelectedFile();
				});
		});
		inputOutputPanel.add(inputButton, gbc_inputButton);
		final JLabel outputLabel = new JLabel("Output:");
		final GridBagConstraints gbc_outputLabel = new GridBagConstraints();
		gbc_outputLabel.anchor = GridBagConstraints.EAST;
		gbc_outputLabel.insets = new Insets(0, 5, 5, 5);
		gbc_outputLabel.gridx = 0;
		gbc_outputLabel.gridy = 1;
		inputOutputPanel.add(outputLabel, gbc_outputLabel);
		this.outputField = new JTextField();
		final GridBagConstraints gbc_outputField = new GridBagConstraints();
		gbc_outputField.gridwidth = 17;
		gbc_outputField.insets = new Insets(0, 0, 5, 5);
		gbc_outputField.fill = GridBagConstraints.BOTH;
		gbc_outputField.gridx = 1;
		gbc_outputField.gridy = 1;
		inputOutputPanel.add(this.outputField, gbc_outputField);
		this.outputField.setColumns(10);
		final JButton outputButton = new JButton("Select");
		final GridBagConstraints gbc_outputButton = new GridBagConstraints();
		gbc_outputButton.fill = GridBagConstraints.BOTH;
		gbc_outputButton.insets = new Insets(0, 0, 5, 5);
		gbc_outputButton.gridx = 18;
		gbc_outputButton.gridy = 1;
		outputButton.addActionListener(e ->
		{
			final JFileChooser chooser = new JFileChooser();
			if (this.outputField.getText() != null && !this.outputField.getText().isEmpty())
				chooser.setSelectedFile(new File(this.outputField.getText()));
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (this.lastPath != null)
				chooser.setCurrentDirectory(this.lastPath);
			final int result = chooser.showOpenDialog(this);
			if (result == 0)
				SwingUtilities.invokeLater(() ->
				{
					this.outputField.setText(chooser.getSelectedFile().getAbsolutePath());
					this.lastPath = chooser.getSelectedFile();
				});
		});
		inputOutputPanel.add(outputButton, gbc_outputButton);
		final JPanel librariesPanel = new JPanel();
		final GridBagConstraints gbc_librariesPanel = new GridBagConstraints();
		gbc_librariesPanel.fill = GridBagConstraints.BOTH;
		gbc_librariesPanel.gridx = 0;
		gbc_librariesPanel.gridy = 1;
		librariesPanel.setBorder(new TitledBorder("Libraries"));
		this.add(librariesPanel, gbc_librariesPanel);
		final GridBagLayout gbl_librariesPanel = new GridBagLayout();
		gbl_librariesPanel.columnWidths = new int[]
		{
				500, 33
		};
		gbl_librariesPanel.rowHeights = new int[]
		{
				0, 0
		};
		gbl_librariesPanel.columnWeights = new double[]
		{
				1.0, 0.0
		};
		gbl_librariesPanel.rowWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		librariesPanel.setLayout(gbl_librariesPanel);
		final JScrollPane librariesPane = new JScrollPane();
		final GridBagConstraints gbc_librariesPane = new GridBagConstraints();
		gbc_librariesPane.insets = new Insets(0, 0, 0, 5);
		gbc_librariesPane.fill = GridBagConstraints.BOTH;
		gbc_librariesPane.gridx = 0;
		gbc_librariesPane.gridy = 0;
		librariesPanel.add(librariesPane, gbc_librariesPane);
		this.libraryList = new DefaultListModel<>();
		final String jreHome = System.getProperty("java.home");
		if (jreHome != null)
		{
			this.libraryList.addElement(jreHome + "/lib/rt.jar");
			this.libraryList.addElement(jreHome + "/lib/jce.jar");
			// adds default jre libraries
		}
		final JList<String> librariesJList = new JList<>(this.libraryList);
		librariesPane.setViewportView(librariesJList);
		final JPanel librariesButtonPanel = new JPanel();
		final GridBagConstraints gbc_librariesButtonPanel = new GridBagConstraints();
		gbc_librariesButtonPanel.fill = GridBagConstraints.BOTH;
		gbc_librariesButtonPanel.gridx = 1;
		gbc_librariesButtonPanel.gridy = 0;
		librariesPanel.add(librariesButtonPanel, gbc_librariesButtonPanel);
		final GridBagLayout gbl_librariesButtonPanel = new GridBagLayout();
		gbl_librariesButtonPanel.columnWidths = new int[]
		{
				0, 0
		};
		gbl_librariesButtonPanel.rowHeights = new int[]
		{
				0, 0, 0, 0, 0
		};
		gbl_librariesButtonPanel.columnWeights = new double[]
		{
				0.0, Double.MIN_VALUE
		};
		gbl_librariesButtonPanel.rowWeights = new double[]
		{
				0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE
		};
		librariesButtonPanel.setLayout(gbl_librariesButtonPanel);
		final JButton librariesAddButton = new JButton("Add");
		final GridBagConstraints gbc_librariesAddButton = new GridBagConstraints();
		gbc_librariesAddButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_librariesAddButton.insets = new Insets(0, 0, 5, 5);
		gbc_librariesAddButton.gridx = 0;
		gbc_librariesAddButton.gridy = 0;
		librariesAddButton.addActionListener(e ->
		{
			final JFileChooser chooser = new JFileChooser();
			if (this.inputField.getText() != null && !this.inputField.getText().isEmpty())
				chooser.setSelectedFile(new File(this.inputField.getText()));
			chooser.setMultiSelectionEnabled(true);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (this.lastPath != null)
				chooser.setCurrentDirectory(this.lastPath);
			final int result = chooser.showOpenDialog(this);
			if (result == 0)
				SwingUtilities.invokeLater(() ->
				{
					for (final File file : chooser.getSelectedFiles())
						this.libraryList.addElement(file.getAbsolutePath());
					this.lastPath = chooser.getSelectedFile();
				});
		});
		librariesButtonPanel.add(librariesAddButton, gbc_librariesAddButton);
		final JButton librariesRemoveButton = new JButton("Remove");
		final GridBagConstraints gbc_librariesRemoveButton = new GridBagConstraints();
		gbc_librariesRemoveButton.insets = new Insets(0, 0, 5, 5);
		gbc_librariesRemoveButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_librariesRemoveButton.gridx = 0;
		gbc_librariesRemoveButton.gridy = 1;
		librariesRemoveButton.addActionListener(e ->
		{
			final List<String> removeList = librariesJList.getSelectedValuesList();
			if (removeList.isEmpty())
				return;
			for (final String s : removeList)
				this.libraryList.removeElement(s);
		});
		librariesButtonPanel.add(librariesRemoveButton, gbc_librariesRemoveButton);
		final JButton librariesResetButton = new JButton("Reset");
		final GridBagConstraints gbc_librariesResetButton = new GridBagConstraints();
		gbc_librariesResetButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_librariesResetButton.insets = new Insets(0, 0, 5, 5);
		gbc_librariesResetButton.gridx = 0;
		gbc_librariesResetButton.gridy = 2;
		librariesResetButton.addActionListener(e ->
		{
			this.libraryList.clear();
			final String javaHome = System.getProperty("java.home");
			if (javaHome != null)
			{
				this.libraryList.addElement(String.join(File.separator, javaHome, "lib", "rt.jar"));
				this.libraryList.addElement(String.join(File.separator, javaHome, "lib", "jce.jar"));
			}
		});
		librariesButtonPanel.add(librariesResetButton, gbc_librariesResetButton);
	}

	/**
	 * Gets and returns the specified input file path as a {@link String}.
	 *
	 * @return the specified input file path as a {@link String}.
	 */
	public final String getInputPath()
	{
		return this.inputField.getText();
	}

	/**
	 * Gets and returns the specified output file path as a {@link String}.
	 *
	 * @return the specified output file path as a {@link String}.
	 */
	public final String getOutputPath()
	{
		return this.outputField.getText();
	}

	/**
	 * Gets and returns the specified libraries as a {@link List<File>}.
	 *
	 * @return the specified libraries as a {@link List<File>}.
	 */
	public final List<File> getLibraries()
	{
		final ArrayList<File> libs = new ArrayList<>();
		for (int i = 0, j = this.libraryList.size(); i < j; i++)
			libs.add(new File(this.libraryList.get(i)));
		return libs;
	}

	/**
	 * Sets the tab settings accordingly with the provided {@link ObfuscationConfiguration}.
	 *
	 * @param info
	 *             the {@link ObfuscationConfiguration} used to determine the tab setup.
	 */
	public final void setSettings(final ObfuscationConfiguration info)
	{
		this.inputField.setText(null);
		this.outputField.setText(null);
		this.libraryList.clear();
		if (info.input != null)
			this.inputField.setText(info.input.getAbsolutePath());
		if (info.output != null)
			this.outputField.setText(info.output.getAbsolutePath());
		if (info.libraries != null)
			info.libraries.forEach(file -> this.libraryList.addElement(file.getAbsolutePath()));
	}
}
