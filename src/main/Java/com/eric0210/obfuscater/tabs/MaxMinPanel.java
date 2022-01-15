package com.eric0210.obfuscater.tabs;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class MaxMinPanel extends JPanel
{
	private static final long serialVersionUID = -6846018370703982218L;
	private final JPanel maxPanel;
	private final JPanel minPanel;
	private final JSpinner maxSpinner;
	private final JSpinner minSpinner;

	public MaxMinPanel(final String title, final int required, final int limit, final int _default)
	{
		this.setBorder(new TitledBorder(null, title, TitledBorder.LEADING, TitledBorder.TOP, null, null));
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]
		{
				0, 0, 0
		};
		gridBagLayout.rowHeights = new int[]
		{
				0, 0
		};
		gridBagLayout.columnWeights = new double[]
		{
				1.0, 1.0, Double.MIN_VALUE
		};
		gridBagLayout.rowWeights = new double[]
		{
				0.0, Double.MIN_VALUE
		};
		this.setLayout(gridBagLayout);
		this.maxPanel = new JPanel();
		this.maxPanel.setBorder(new TitledBorder(null, "Max", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		final GridBagConstraints gbc_maxPanel = new GridBagConstraints();
		gbc_maxPanel.insets = new Insets(0, 0, 0, 5);
		gbc_maxPanel.fill = GridBagConstraints.BOTH;
		gbc_maxPanel.gridx = 0;
		gbc_maxPanel.gridy = 0;
		this.add(this.maxPanel, gbc_maxPanel);
		final GridBagLayout gbl_maxPanel = new GridBagLayout();
		gbl_maxPanel.columnWidths = new int[]
		{
				0, 0
		};
		gbl_maxPanel.rowHeights = new int[]
		{
				0, 0
		};
		gbl_maxPanel.columnWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		gbl_maxPanel.rowWeights = new double[]
		{
				0.0, Double.MIN_VALUE
		};
		this.maxPanel.setLayout(gbl_maxPanel);
		this.maxSpinner = new JSpinner();
		this.maxSpinner.setModel(new SpinnerNumberModel(_default, required, limit, 1));

		final GridBagConstraints gbc_maxSpinner = new GridBagConstraints();
		gbc_maxSpinner.fill = GridBagConstraints.BOTH;
		gbc_maxSpinner.gridx = 0;
		gbc_maxSpinner.gridy = 0;
		this.maxPanel.add(this.maxSpinner, gbc_maxSpinner);
		this.minPanel = new JPanel();
		this.minPanel.setBorder(new TitledBorder(null, "Min", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		final GridBagConstraints gbc_minPanel = new GridBagConstraints();
		gbc_minPanel.fill = GridBagConstraints.BOTH;
		gbc_minPanel.gridx = 1;
		gbc_minPanel.gridy = 0;
		this.add(this.minPanel, gbc_minPanel);
		final GridBagLayout gbl_minPanel = new GridBagLayout();
		gbl_minPanel.columnWidths = new int[]
		{
				0, 0
		};
		gbl_minPanel.rowHeights = new int[]
		{
				0, 0
		};
		gbl_minPanel.columnWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		gbl_minPanel.rowWeights = new double[]
		{
				0.0, Double.MIN_VALUE
		};
		this.minPanel.setLayout(gbl_minPanel);
		this.minSpinner = new JSpinner();
		this.maxSpinner.addChangeListener(e ->
		{
			if ((int) this.minSpinner.getValue() > (int) this.maxSpinner.getValue())
				this.maxSpinner.setValue(this.minSpinner.getValue());
		});
		this.minSpinner.addChangeListener(e ->
		{
			if ((int) this.minSpinner.getValue() > (int) this.maxSpinner.getValue())
				this.minSpinner.setValue(this.maxSpinner.getValue());
		});
		this.minSpinner.setModel(new SpinnerNumberModel(_default, required, limit, 1));
		final GridBagConstraints gbc_minSpinner = new GridBagConstraints();
		gbc_minSpinner.fill = GridBagConstraints.BOTH;
		gbc_minSpinner.gridx = 0;
		gbc_minSpinner.gridy = 0;
		this.minPanel.add(this.minSpinner, gbc_minSpinner);
	}

	@Override
	public final void setEnabled(final boolean enabled)
	{
		super.setEnabled(enabled);
		this.minPanel.setEnabled(enabled);
		this.maxPanel.setEnabled(enabled);
		this.minSpinner.setEnabled(enabled);
		this.maxSpinner.setEnabled(enabled);
	}

	public final int getMax()
	{
		return (int) this.maxSpinner.getValue();
	}

	public final int getMin()
	{
		return (int) this.minSpinner.getValue();
	}
}
