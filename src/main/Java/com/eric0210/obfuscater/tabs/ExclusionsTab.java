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
import java.util.List;

import javax.swing.*;

import com.eric0210.obfuscater.config.ObfuscationConfiguration;
import com.eric0210.obfuscater.utils.exclusions.Exclusion;
import com.eric0210.obfuscater.utils.exclusions.ExclusionManager;
import com.eric0210.obfuscater.utils.exclusions.ExclusionType;

/**
 * A {@link JPanel} containing the functions needed to set general exclusionManager via GUI.
 *
 * @author ItzSomebody
 */
public class ExclusionsTab extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4522391143349543314L;
	/**
	 * A {@link DefaultListModel<String>} containing all exclusionManager made in the exclusionManager tab of the GUI.
	 */
	private final DefaultListModel<String> exclusions;

	public ExclusionsTab()
	{
		final GridBagLayout gbl_this = new GridBagLayout();
		gbl_this.columnWidths = new int[]
		{
				0, 0, 0, 0, 0
		};
		gbl_this.rowHeights = new int[]
		{
				0, 0, 0
		};
		gbl_this.columnWeights = new double[]
		{
				0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE
		};
		gbl_this.rowWeights = new double[]
		{
				1.0, 0.0, Double.MIN_VALUE
		};
		this.setLayout(gbl_this);
		final JScrollPane exclusionScrollPane = new JScrollPane();
		final GridBagConstraints gbc_exclusionScrollPane = new GridBagConstraints();
		gbc_exclusionScrollPane.gridwidth = 4;
		gbc_exclusionScrollPane.insets = new Insets(5, 5, 5, 5);
		gbc_exclusionScrollPane.fill = GridBagConstraints.BOTH;
		gbc_exclusionScrollPane.gridx = 0;
		gbc_exclusionScrollPane.gridy = 0;
		this.add(exclusionScrollPane, gbc_exclusionScrollPane);
		this.exclusions = new DefaultListModel<>();
		final JList<String> exclusionList = new JList<>(this.exclusions);
		exclusionScrollPane.setViewportView(exclusionList);
		final JComboBox<String> exclusionComboBox = new JComboBox<>();
		final GridBagConstraints gbc_exclusionComboBox = new GridBagConstraints();
		gbc_exclusionComboBox.insets = new Insets(0, 5, 5, 5);
		gbc_exclusionComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_exclusionComboBox.gridx = 0;
		gbc_exclusionComboBox.gridy = 1;
		for (final ExclusionType exclusionType : ExclusionType.values())
			exclusionComboBox.addItem(exclusionType.getName());
		this.add(exclusionComboBox, gbc_exclusionComboBox);
		final JTextField exclusionField = new JTextField();
		final GridBagConstraints gbc_exclusionField = new GridBagConstraints();
		gbc_exclusionField.insets = new Insets(0, 0, 5, 5);
		gbc_exclusionField.fill = GridBagConstraints.HORIZONTAL;
		gbc_exclusionField.gridx = 1;
		gbc_exclusionField.gridy = 1;
		this.add(exclusionField, gbc_exclusionField);
		exclusionField.setColumns(10);
		final JButton exclusionAddButton = new JButton("Add");
		final GridBagConstraints gbc_exclusionAddButton = new GridBagConstraints();
		gbc_exclusionAddButton.insets = new Insets(0, 0, 5, 5);
		gbc_exclusionAddButton.gridx = 2;
		gbc_exclusionAddButton.gridy = 1;
		exclusionAddButton.addActionListener(e ->
		{
			if (exclusionField.getText() != null && !exclusionField.getText().isEmpty())
			{
				this.exclusions.addElement(exclusionComboBox.getItemAt(exclusionComboBox.getSelectedIndex()) + ": " + exclusionField.getText());
				exclusionField.setText(null);
			}
		});
		this.add(exclusionAddButton, gbc_exclusionAddButton);
		final JButton exclusionRemoveButton = new JButton("Remove");
		final GridBagConstraints gbc_exclusionRemoveButton = new GridBagConstraints();
		gbc_exclusionRemoveButton.insets = new Insets(0, 0, 5, 5);
		gbc_exclusionRemoveButton.gridx = 3;
		gbc_exclusionRemoveButton.gridy = 1;
		exclusionRemoveButton.addActionListener(e ->
		{
			final List<String> removeList = exclusionList.getSelectedValuesList();
			if (removeList.isEmpty())
				return;
			for (final String s : removeList)
				this.exclusions.removeElement(s);
		});
		this.add(exclusionRemoveButton, gbc_exclusionRemoveButton);
	}

	/**
	 * Creates and returns an {@link ExclusionManager} containing the exclusionManager made from this {@link ExclusionsTab}.
	 *
	 * @return an {@link ExclusionManager} containing the exclusionManager made from this {@link ExclusionsTab}.
	 */
	public final ExclusionManager getExclusions()
	{
		final ExclusionManager manager = new ExclusionManager();
		for (int i = 0, j = this.exclusions.size(); i < j; i++)
			manager.addExclusion(new Exclusion(this.exclusions.get(i)));
		return manager;
	}

	/**
	 * Sets the tab settings accordingly with the provided {@link ObfuscationConfiguration}.
	 *
	 * @param info
	 *             the {@link ObfuscationConfiguration} used to determine the tab setup.
	 */
	public final void setSettings(final ObfuscationConfiguration info)
	{
		this.exclusions.clear();
		if (info.exclusionManager != null)
		{
			final ExclusionManager manager = info.exclusionManager;
			manager.getExclusions().forEach(exclusion -> this.exclusions.addElement(exclusion.getExclusionType().getName() + ": " + exclusion.getPattern().pattern()));
		}
	}
}
