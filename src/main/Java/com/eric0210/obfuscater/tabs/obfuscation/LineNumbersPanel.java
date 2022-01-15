package com.eric0210.obfuscater.tabs.obfuscation;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import com.eric0210.obfuscater.tabs.MaxMinPanel;
import com.eric0210.obfuscater.transformer.obfuscator.attributes.LineNumbersObfuscation;

public class LineNumbersPanel extends JPanel
{
	private static final long serialVersionUID = 5867141176723549230L;

	private final JCheckBox lineNumbersEnabledCheckBox;
	private final JRadioButton removeButton;
	private JRadioButton randomizeButton;
	private MaxMinPanel randomizeRange;
	private JRadioButton replaceButton;
	private JSpinner replaceTo;

	public LineNumbersPanel()
	{
		this.setBorder(new TitledBorder("Line Number Instructions"));

		final GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]
		{
				288, 0, 0
		};
		layout.rowHeights = new int[]
		{
				0, 0
		};
		layout.columnWeights = new double[]
		{
				0.0, 1.0, Double.MIN_VALUE
		};
		layout.rowWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		this.setLayout(layout);

		this.lineNumbersEnabledCheckBox = new JCheckBox("Line Number Instructions");
		this.lineNumbersEnabledCheckBox.setToolTipText("Obfuscate line numbers by modify line number instructions.");
		final GridBagConstraints lineNumbersCheckBoxConstraints = new GridBagConstraints();
		lineNumbersCheckBoxConstraints.anchor = GridBagConstraints.NORTHWEST;
		lineNumbersCheckBoxConstraints.insets = new Insets(0, 0, 5, 5);
		lineNumbersCheckBoxConstraints.gridx = 0;
		lineNumbersCheckBoxConstraints.gridy = 0;
		this.add(this.lineNumbersEnabledCheckBox, lineNumbersCheckBoxConstraints);

		this.removeButton = new JRadioButton("Remove");
		this.removeButton.addActionListener(e ->
		{
			if (this.removeButton.isSelected())
			{
				this.replaceTo.setEnabled(false);
				this.randomizeButton.setSelected(false);
				this.replaceButton.setSelected(false);
				this.randomizeRange.setEnabled(false);
			}
			else
				this.removeButton.setSelected(true);
		});
		this.removeButton.setSelected(true);
		this.removeButton.setEnabled(false);
		final GridBagConstraints lineNumbersRemoveConstraints = new GridBagConstraints();
		lineNumbersRemoveConstraints.anchor = GridBagConstraints.NORTHEAST;
		lineNumbersRemoveConstraints.insets = new Insets(0, 0, 5, 5);
		lineNumbersRemoveConstraints.gridx = 1;
		lineNumbersRemoveConstraints.gridy = 0;
		this.add(this.removeButton, lineNumbersRemoveConstraints);

		this.replaceButton = new JRadioButton("Replace");
		this.replaceButton.setEnabled(false);
		this.replaceButton.addActionListener(e ->
		{
			if (this.replaceButton.isSelected())
			{
				this.removeButton.setSelected(false);
				this.randomizeButton.setSelected(false);
				this.replaceTo.setEnabled(true);
				this.randomizeRange.setEnabled(false);
			}
			else
				this.replaceButton.setSelected(true);
		});
		final GridBagConstraints lineNumbersReplaceConstraints = new GridBagConstraints();
		lineNumbersReplaceConstraints.anchor = GridBagConstraints.NORTHEAST;
		lineNumbersReplaceConstraints.insets = new Insets(0, 0, 5, 5);
		lineNumbersReplaceConstraints.gridx = 3;
		lineNumbersReplaceConstraints.gridy = 0;
		this.add(this.replaceButton, lineNumbersReplaceConstraints);

		this.replaceTo = new JSpinner();
		final GridBagConstraints lineNumbersReplaceToConstraints = new GridBagConstraints();
		lineNumbersReplaceToConstraints.anchor = GridBagConstraints.NORTHWEST;
		lineNumbersReplaceToConstraints.insets = new Insets(0, 0, 5, 5);
		lineNumbersReplaceToConstraints.gridx = 4;
		lineNumbersReplaceToConstraints.gridy = 0;
		lineNumbersReplaceToConstraints.ipadx = 50;
		this.add(this.replaceTo, lineNumbersReplaceToConstraints);

		this.replaceTo.setEnabled(false);
		this.randomizeButton = new JRadioButton("Randomize");
		this.randomizeButton.addActionListener(e ->
		{
			if (this.randomizeButton.isSelected())
			{
				this.replaceTo.setEnabled(false);
				this.removeButton.setSelected(false);
				this.replaceButton.setSelected(false);
				this.randomizeRange.setEnabled(true);
			}
			else
				this.randomizeButton.setSelected(true);
		});
		this.randomizeButton.setEnabled(false);
		final GridBagConstraints gbc_lineNumbersRandomize = new GridBagConstraints();
		gbc_lineNumbersRandomize.anchor = GridBagConstraints.NORTHEAST;
		gbc_lineNumbersRandomize.insets = new Insets(0, 0, 5, 5);
		gbc_lineNumbersRandomize.gridx = 5;
		gbc_lineNumbersRandomize.gridy = 0;
		this.add(this.randomizeButton, gbc_lineNumbersRandomize);

		this.randomizeRange = new MaxMinPanel("Line Number Randomization", 1, 32767, 32);
		this.randomizeRange.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Line Number Randomization", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		this.randomizeRange.setEnabled(false);
		final GridBagConstraints gbc_lineNumbersObfRange = new GridBagConstraints();
		gbc_lineNumbersObfRange.anchor = GridBagConstraints.NORTHWEST;
		gbc_lineNumbersObfRange.insets = new Insets(0, 0, 5, 0);
		gbc_lineNumbersObfRange.gridx = 6;
		gbc_lineNumbersObfRange.gridy = 0;
		this.add(this.randomizeRange, gbc_lineNumbersObfRange);

		this.lineNumbersEnabledCheckBox.addActionListener(e ->
		{
			this.removeButton.setEnabled(this.lineNumbersEnabledCheckBox.isSelected());
			this.randomizeButton.setEnabled(this.lineNumbersEnabledCheckBox.isSelected());
			this.replaceButton.setEnabled(this.lineNumbersEnabledCheckBox.isSelected());
			if (this.lineNumbersEnabledCheckBox.isSelected())
			{
				this.replaceTo.setEnabled(this.replaceButton.isSelected());
				this.randomizeRange.setEnabled(this.randomizeButton.isSelected());
			}
			else
			{
				this.replaceTo.setEnabled(false);
				this.randomizeRange.setEnabled(false);
			}
		});
	}

	public final LineNumbersObfuscation getLineNumberTransformer()
	{
		final LineNumbersObfuscation.Mode mode = this.removeButton.isSelected() ? LineNumbersObfuscation.Mode.Remove : this.randomizeButton.isSelected() ? LineNumbersObfuscation.Mode.Randomize : LineNumbersObfuscation.Mode.Replace;
		final int randRangeMin = this.randomizeRange.getMin();
		final int randRangeMax = this.randomizeRange.getMax();
		return this.lineNumbersEnabledCheckBox.isSelected() ? new LineNumbersObfuscation(new LineNumbersObfuscation.Parameter(mode, (int) this.replaceTo.getValue(), randRangeMin, randRangeMax)) : null;
	}
}
