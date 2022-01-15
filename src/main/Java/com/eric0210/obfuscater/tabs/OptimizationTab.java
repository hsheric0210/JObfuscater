package com.eric0210.obfuscater.tabs;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.eric0210.obfuscater.config.ObfuscationConfiguration;
import com.eric0210.obfuscater.transformer.optimizer.Optimizer;

public class OptimizationTab extends JPanel
{

	private static final long serialVersionUID = 4710074204506838706L;
	private final JCheckBox gotoGotoCheckBox;
	private final JCheckBox gotoReturnCheckBox;
	private final JCheckBox nopCheckBox;
	private final JCheckBox optimizationEnabledCheckBox;

	public OptimizationTab()
	{
		final GridBagLayout gbl_this = new GridBagLayout();
		gbl_this.columnWidths = new int[]
		{
				0, 0
		};
		gbl_this.rowHeights = new int[]
		{
				0, 0, 0
		};
		gbl_this.columnWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		gbl_this.rowWeights = new double[]
		{
				0.0, 0.0, Double.MIN_VALUE
		};
		this.setBorder(new TitledBorder("Optimizer"));
		this.setLayout(gbl_this);
		final JPanel optimizationSetupPanel = new JPanel();
		final GridBagConstraints gbc_optimizationSetupPanel = new GridBagConstraints();
		gbc_optimizationSetupPanel.fill = GridBagConstraints.BOTH;
		gbc_optimizationSetupPanel.gridx = 0;
		gbc_optimizationSetupPanel.gridy = 1;
		this.add(optimizationSetupPanel, gbc_optimizationSetupPanel);
		final GridBagLayout gbl_optimizationSetupPanel = new GridBagLayout();
		gbl_optimizationSetupPanel.columnWidths = new int[]
		{
				0, 0
		};
		gbl_optimizationSetupPanel.rowHeights = new int[]
		{
				0, 0, 0, 0
		};
		gbl_optimizationSetupPanel.columnWeights = new double[]
		{
				0.0, Double.MIN_VALUE
		};
		gbl_optimizationSetupPanel.rowWeights = new double[]
		{
				0.0, 0.0, 0.0, Double.MIN_VALUE
		};
		optimizationSetupPanel.setBorder(new TitledBorder("Setup"));
		optimizationSetupPanel.setLayout(gbl_optimizationSetupPanel);
		this.gotoGotoCheckBox = new JCheckBox("Inline Goto-Goto Sequences");
		final GridBagConstraints gbc_gotoGotoCheckBox = new GridBagConstraints();
		gbc_gotoGotoCheckBox.anchor = GridBagConstraints.WEST;
		gbc_gotoGotoCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_gotoGotoCheckBox.gridx = 0;
		gbc_gotoGotoCheckBox.gridy = 0;
		this.gotoGotoCheckBox.setEnabled(false);
		optimizationSetupPanel.add(this.gotoGotoCheckBox, gbc_gotoGotoCheckBox);
		this.gotoReturnCheckBox = new JCheckBox("Inline Goto-Return Sequences");
		final GridBagConstraints gbc_gotoReturnCheckBox = new GridBagConstraints();
		gbc_gotoReturnCheckBox.anchor = GridBagConstraints.WEST;
		gbc_gotoReturnCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_gotoReturnCheckBox.gridx = 0;
		gbc_gotoReturnCheckBox.gridy = 1;
		this.gotoReturnCheckBox.setEnabled(false);
		optimizationSetupPanel.add(this.gotoReturnCheckBox, gbc_gotoReturnCheckBox);
		this.nopCheckBox = new JCheckBox("Remove Nop Instructions");
		final GridBagConstraints gbc_nopCheckBox = new GridBagConstraints();
		gbc_nopCheckBox.anchor = GridBagConstraints.WEST;
		gbc_nopCheckBox.gridx = 0;
		gbc_nopCheckBox.gridy = 2;
		this.nopCheckBox.setEnabled(false);
		optimizationSetupPanel.add(this.nopCheckBox, gbc_nopCheckBox);
		this.optimizationEnabledCheckBox = new JCheckBox("Enabled");
		final GridBagConstraints gbc_optimizationEnabledCheckBox = new GridBagConstraints();
		gbc_optimizationEnabledCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_optimizationEnabledCheckBox.anchor = GridBagConstraints.WEST;
		gbc_optimizationEnabledCheckBox.gridx = 0;
		gbc_optimizationEnabledCheckBox.gridy = 0;
		this.optimizationEnabledCheckBox.addActionListener(e ->
		{
			final boolean enable = this.optimizationEnabledCheckBox.isSelected();
			this.gotoGotoCheckBox.setEnabled(enable);
			this.gotoReturnCheckBox.setEnabled(enable);
			this.nopCheckBox.setEnabled(enable);
		});
		this.add(this.optimizationEnabledCheckBox, gbc_optimizationEnabledCheckBox);
	}

	/**
	 * Returns an {@link Optimizer} setup accordingly to this {@link OptimizationTab}.
	 *
	 * @return an {@link Optimizer} setup accordingly to this {@link OptimizationTab}.
	 */
	public final Optimizer getOptimizer()
	{
		return this.optimizationEnabledCheckBox.isSelected() ? new Optimizer(new OptimizerSetup(this.nopCheckBox.isSelected(), this.gotoGotoCheckBox.isSelected(), this.gotoReturnCheckBox.isSelected())) : null;
	}

	/**
	 * Sets the tab settings accordingly with the provided {@link ObfuscationConfiguration}.
	 *
	 * @param info
	 *             the {@link ObfuscationConfiguration} used to determine the tab setup.
	 */
	public final void setSettings(final ObfuscationConfiguration info)
	{
		this.optimizationEnabledCheckBox.setSelected(false);
		this.nopCheckBox.setSelected(false);
		this.nopCheckBox.setEnabled(false);
		this.gotoReturnCheckBox.setSelected(false);
		this.gotoReturnCheckBox.setEnabled(false);
		this.gotoGotoCheckBox.setSelected(false);
		this.gotoGotoCheckBox.setEnabled(false);
		if (info.transformers != null)
			info.transformers.stream().filter(transformer -> transformer instanceof Optimizer).forEach(transformer ->
			{
				this.optimizationEnabledCheckBox.setSelected(true);
				this.nopCheckBox.setEnabled(true);
				this.gotoReturnCheckBox.setEnabled(true);
				this.gotoGotoCheckBox.setEnabled(true);
				final OptimizerSetup setup = ((Optimizer) transformer).getSetup();
				this.nopCheckBox.setSelected(setup.isNopRemoverEnabled());
				this.gotoReturnCheckBox.setSelected(setup.isGotoReturnEnabled());
				this.gotoGotoCheckBox.setSelected(setup.isGotoGotoEnabled());
			});
	}
}
