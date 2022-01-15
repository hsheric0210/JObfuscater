package com.eric0210.obfuscater.tabs;

import java.awt.*;
import java.util.Collection;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.eric0210.obfuscater.utils.StringGeneratorParameter;

public class StringGeneratorPanel extends JPanel
{
	private static final long serialVersionUID = 93860214700282644L;
	private final JTextField patternField;
	private final JPanel patternFieldPanel;
	private final JPanel patternPresetsComboBoxPanel;
	private final JComboBox<String> patternPresetComboBox;
	private final JPanel optionsPanel;
	private final JPanel prefixFieldPanel;
	private final JTextField prefixField;
	private final JPanel suffixFieldPanel;
	private final JTextField suffixField;
	private final MaxMinPanel stringLengthPanel;
	private JCheckBox duplicateGenerationAllowed;

	public StringGeneratorPanel(final String title, final boolean duplicateAllowed, final Collection<StringGeneratorParameter.StringGeneratorPresets> excludedPresets)
	{
		this.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), title, TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]
		{
				0, 0, 0, 0
		};
		gridBagLayout.rowHeights = new int[]
		{
				74, 0, 0, 0
		};
		gridBagLayout.columnWeights = new double[]
		{
				1.0, 1.0, 1.0, Double.MIN_VALUE
		};
		gridBagLayout.rowWeights = new double[]
		{
				0.0, 0.0, 0.0, Double.MIN_VALUE
		};
		this.setLayout(gridBagLayout);
		this.prefixFieldPanel = new JPanel();
		this.prefixFieldPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Prefix", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		final GridBagConstraints gbc_prefixFieldPanel = new GridBagConstraints();
		gbc_prefixFieldPanel.anchor = GridBagConstraints.NORTH;
		gbc_prefixFieldPanel.insets = new Insets(0, 0, 5, 5);
		gbc_prefixFieldPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_prefixFieldPanel.gridx = 0;
		gbc_prefixFieldPanel.gridy = 0;
		this.add(this.prefixFieldPanel, gbc_prefixFieldPanel);
		final GridBagLayout gbl_prefixFieldPanel = new GridBagLayout();
		gbl_prefixFieldPanel.columnWidths = new int[]
		{
				116, 0
		};
		gbl_prefixFieldPanel.rowHeights = new int[]
		{
				21, 0
		};
		gbl_prefixFieldPanel.columnWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		gbl_prefixFieldPanel.rowWeights = new double[]
		{
				0.0, Double.MIN_VALUE
		};
		this.prefixFieldPanel.setLayout(gbl_prefixFieldPanel);
		this.prefixField = new JTextField();
		final GridBagConstraints gbc_prefixField = new GridBagConstraints();
		gbc_prefixField.fill = GridBagConstraints.BOTH;
		gbc_prefixField.gridx = 0;
		gbc_prefixField.gridy = 0;
		gbc_prefixField.ipadx = 15;
		this.prefixFieldPanel.add(this.prefixField, gbc_prefixField);
		this.prefixField.setColumns(10);
		this.optionsPanel = new JPanel();
		this.optionsPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Dictionary", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		final GridBagConstraints gbc_optionsPanel = new GridBagConstraints();
		gbc_optionsPanel.anchor = GridBagConstraints.NORTH;
		gbc_optionsPanel.insets = new Insets(0, 0, 5, 5);
		gbc_optionsPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_optionsPanel.gridx = 1;
		gbc_optionsPanel.gridy = 0;
		this.add(this.optionsPanel, gbc_optionsPanel);
		final GridBagLayout gbl_optionsPanel = new GridBagLayout();
		gbl_optionsPanel.columnWidths = new int[]
		{
				0, 0, 0
		};
		gbl_optionsPanel.rowHeights = new int[]
		{
				0, 0, 0
		};
		gbl_optionsPanel.columnWeights = new double[]
		{
				1.0, 1.0, Double.MIN_VALUE
		};
		gbl_optionsPanel.rowWeights = new double[]
		{
				1.0, 1.0, Double.MIN_VALUE
		};
		this.optionsPanel.setLayout(gbl_optionsPanel);
		this.patternFieldPanel = new JPanel();
		final GridBagConstraints gbc_dictionaryFieldPanel = new GridBagConstraints();
		gbc_dictionaryFieldPanel.anchor = GridBagConstraints.NORTH;
		gbc_dictionaryFieldPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_dictionaryFieldPanel.insets = new Insets(0, 0, 5, 5);
		gbc_dictionaryFieldPanel.gridx = 0;
		gbc_dictionaryFieldPanel.gridy = 0;
		this.optionsPanel.add(this.patternFieldPanel, gbc_dictionaryFieldPanel);
		this.patternFieldPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Allowed Characters", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		final GridBagLayout gbl_dictionaryFieldPanel = new GridBagLayout();
		gbl_dictionaryFieldPanel.columnWidths = new int[]
		{
				0, 0
		};
		gbl_dictionaryFieldPanel.rowHeights = new int[]
		{
				0, 0
		};
		gbl_dictionaryFieldPanel.columnWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		gbl_dictionaryFieldPanel.rowWeights = new double[]
		{
				0.0, Double.MIN_VALUE
		};
		this.patternFieldPanel.setLayout(gbl_dictionaryFieldPanel);
		this.patternField = new JTextField();
		final GridBagConstraints gbc_dictionaryField = new GridBagConstraints();
		gbc_dictionaryField.anchor = GridBagConstraints.NORTH;
		gbc_dictionaryField.fill = GridBagConstraints.HORIZONTAL;
		gbc_dictionaryField.gridx = 0;
		gbc_dictionaryField.gridy = 0;
		gbc_dictionaryField.ipadx = 30;
		this.patternFieldPanel.add(this.patternField, gbc_dictionaryField);
		this.patternField.setColumns(10);
		this.patternPresetsComboBoxPanel = new JPanel();
		final GridBagConstraints gbc_dictionaryPresetsComboBoxPanel = new GridBagConstraints();
		gbc_dictionaryPresetsComboBoxPanel.anchor = GridBagConstraints.NORTH;
		gbc_dictionaryPresetsComboBoxPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_dictionaryPresetsComboBoxPanel.insets = new Insets(0, 0, 5, 0);
		gbc_dictionaryPresetsComboBoxPanel.gridx = 1;
		gbc_dictionaryPresetsComboBoxPanel.gridy = 0;
		this.optionsPanel.add(this.patternPresetsComboBoxPanel, gbc_dictionaryPresetsComboBoxPanel);
		this.patternPresetsComboBoxPanel.setBorder(new TitledBorder(null, "Presets", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		final GridBagLayout gbl_dictionaryPresetsComboBoxPanel = new GridBagLayout();
		gbl_dictionaryPresetsComboBoxPanel.columnWidths = new int[]
		{
				30, 0
		};
		gbl_dictionaryPresetsComboBoxPanel.rowHeights = new int[]
		{
				21, 0
		};
		gbl_dictionaryPresetsComboBoxPanel.columnWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		gbl_dictionaryPresetsComboBoxPanel.rowWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		this.patternPresetsComboBoxPanel.setLayout(gbl_dictionaryPresetsComboBoxPanel);
		this.patternPresetComboBox = new JComboBox<>();
		this.patternPresetComboBox.addActionListener(e ->
		{
			final String current = String.valueOf(this.patternPresetComboBox.getSelectedItem());
			final StringGeneratorParameter.StringGeneratorPresets preset = StringGeneratorParameter.StringGeneratorPresets.fromName(current);
			if (preset != null)
				this.patternField.setText(preset.getPattern());
		});
		final GridBagConstraints gbc_dictionaryPresetComboBox = new GridBagConstraints();
		gbc_dictionaryPresetComboBox.fill = GridBagConstraints.BOTH;
		gbc_dictionaryPresetComboBox.gridx = 0;
		gbc_dictionaryPresetComboBox.gridy = 0;
		gbc_dictionaryPresetComboBox.ipadx = 10;
		this.patternPresetsComboBoxPanel.add(this.patternPresetComboBox, gbc_dictionaryPresetComboBox);
		this.suffixFieldPanel = new JPanel();
		this.suffixFieldPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Suffix", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		final GridBagConstraints gbc_rsgsuffixPanel = new GridBagConstraints();
		gbc_rsgsuffixPanel.insets = new Insets(0, 0, 5, 0);
		gbc_rsgsuffixPanel.anchor = GridBagConstraints.NORTH;
		gbc_rsgsuffixPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_rsgsuffixPanel.gridx = 2;
		gbc_rsgsuffixPanel.gridy = 0;
		this.add(this.suffixFieldPanel, gbc_rsgsuffixPanel);
		final GridBagLayout gbl_rsgsuffixPanel = new GridBagLayout();
		gbl_rsgsuffixPanel.columnWidths = new int[]
		{
				0, 0
		};
		gbl_rsgsuffixPanel.rowHeights = new int[]
		{
				0, 0
		};
		gbl_rsgsuffixPanel.columnWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		gbl_rsgsuffixPanel.rowWeights = new double[]
		{
				0.0, Double.MIN_VALUE
		};
		this.suffixFieldPanel.setLayout(gbl_rsgsuffixPanel);
		this.suffixField = new JTextField();
		final GridBagConstraints gbc_suffixField = new GridBagConstraints();
		gbc_suffixField.fill = GridBagConstraints.BOTH;
		gbc_suffixField.gridx = 0;
		gbc_suffixField.gridy = 0;
		gbc_suffixField.ipadx = 15;
		this.suffixFieldPanel.add(this.suffixField, gbc_suffixField);
		this.suffixField.setColumns(10);
		this.stringLengthPanel = new MaxMinPanel("String Length", 1, 32767, 16);
		final GridBagConstraints gbc_stringlengthPanel = new GridBagConstraints();
		gbc_stringlengthPanel.insets = new Insets(0, 0, 5, 0);
		gbc_stringlengthPanel.anchor = GridBagConstraints.NORTH;
		gbc_stringlengthPanel.gridwidth = 3;
		gbc_stringlengthPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_stringlengthPanel.gridx = 0;
		gbc_stringlengthPanel.gridy = 1;
		this.add(this.stringLengthPanel, gbc_stringlengthPanel);

		if (duplicateAllowed)
		{
			this.duplicateGenerationAllowed = new JCheckBox("Duplicate Generation Allowed");
			final GridBagConstraints gbc_duplicateGenerationAllowed = new GridBagConstraints();
			gbc_duplicateGenerationAllowed.gridwidth = 4;
			gbc_duplicateGenerationAllowed.gridx = 0;
			gbc_duplicateGenerationAllowed.gridy = 2;
			this.add(this.duplicateGenerationAllowed, gbc_duplicateGenerationAllowed);
		}

		for (final StringGeneratorParameter.StringGeneratorPresets preset : StringGeneratorParameter.StringGeneratorPresets.values())
			if (excludedPresets == null || !excludedPresets.contains(preset))
				this.patternPresetComboBox.addItem(preset.getName());
		final String presetName = String.valueOf(this.patternPresetComboBox.getSelectedItem());
		final StringGeneratorParameter.StringGeneratorPresets defaultPreset = StringGeneratorParameter.StringGeneratorPresets.fromName(presetName);
		if (defaultPreset != null)
			this.patternField.setText(defaultPreset.getPattern());
	}

	@Override
	public final void setEnabled(final boolean enabled)
	{
		super.setEnabled(enabled);
		this.patternField.setEnabled(enabled);
		this.patternFieldPanel.setEnabled(enabled);
		this.patternPresetComboBox.setEnabled(enabled);
		this.patternPresetsComboBoxPanel.setEnabled(enabled);
		this.stringLengthPanel.setEnabled(enabled);
		this.optionsPanel.setEnabled(enabled);
		this.prefixField.setEnabled(enabled);
		this.prefixFieldPanel.setEnabled(enabled);
		this.suffixField.setEnabled(enabled);
		this.suffixFieldPanel.setEnabled(enabled);
		this.stringLengthPanel.setEnabled(enabled);
		if (this.duplicateGenerationAllowed != null)
			this.duplicateGenerationAllowed.setEnabled(enabled);
	}

	public final StringGeneratorParameter getParameter()
	{
		return new StringGeneratorParameter().setPattern(this.patternField.getText()).setPrefixSuffix(this.prefixField.getText(), this.suffixField.getText()).setLength(this.stringLengthPanel.getMax(), this.stringLengthPanel.getMin()).setDuplicateGenerationEnabled(this.duplicateGenerationAllowed != null && this.duplicateGenerationAllowed.isSelected());
	}
}
