
package com.eric0210.obfuscater.tabs;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.eric0210.obfuscater.config.ObfuscationConfiguration;
import com.eric0210.obfuscater.transformer.shrinkers.ShrinkerSetup;

public class ShrinkingTab extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4283243811685863608L;
	private final JCheckBox attributesCheckBox;
	private final JCheckBox debugInfoCheckBox;
	private final JCheckBox invisibleAnnotationsCheckBox;
	private final JCheckBox visibleAnnotationsCheckBox;
	private final JCheckBox shrinkerEnabledCheckBox;

	public ShrinkingTab()
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
		this.setBorder(new TitledBorder("Shrinker"));
		this.setLayout(gbl_this);

		final JPanel shrinkerSetupPanel = new JPanel();
		final GridBagConstraints gbc_shrinkerSetupPanel = new GridBagConstraints();
		gbc_shrinkerSetupPanel.fill = GridBagConstraints.BOTH;
		gbc_shrinkerSetupPanel.gridx = 0;
		gbc_shrinkerSetupPanel.gridy = 1;
		this.add(shrinkerSetupPanel, gbc_shrinkerSetupPanel);
		final GridBagLayout gbl_shrinkerSetupPanel = new GridBagLayout();
		gbl_shrinkerSetupPanel.columnWidths = new int[]
		{
				0, 0
		};
		gbl_shrinkerSetupPanel.rowHeights = new int[]
		{
				0, 0, 0, 0, 0, 0, 0
		};
		gbl_shrinkerSetupPanel.columnWeights = new double[]
		{
				0.0, Double.MIN_VALUE
		};
		gbl_shrinkerSetupPanel.rowWeights = new double[]
		{
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE
		};
		shrinkerSetupPanel.setBorder(new TitledBorder("Setup"));
		shrinkerSetupPanel.setLayout(gbl_shrinkerSetupPanel);

		this.attributesCheckBox = new JCheckBox("Remove Attributes");
		final GridBagConstraints gbc_attributesCheckBox = new GridBagConstraints();
		gbc_attributesCheckBox.anchor = GridBagConstraints.WEST;
		gbc_attributesCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_attributesCheckBox.gridx = 0;
		gbc_attributesCheckBox.gridy = 0;
		this.attributesCheckBox.setEnabled(false);
		shrinkerSetupPanel.add(this.attributesCheckBox, gbc_attributesCheckBox);

		this.debugInfoCheckBox = new JCheckBox("Remove Unnecessary Debugging Information");
		final GridBagConstraints gbc_debugInfoCheckBox = new GridBagConstraints();
		gbc_debugInfoCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_debugInfoCheckBox.gridx = 0;
		gbc_debugInfoCheckBox.gridy = 1;
		this.debugInfoCheckBox.setEnabled(false);
		this.debugInfoCheckBox.setToolTipText("Removes inner classes, outer class, outer method, etc. informations");
		shrinkerSetupPanel.add(this.debugInfoCheckBox, gbc_debugInfoCheckBox);

		this.invisibleAnnotationsCheckBox = new JCheckBox("Remove Invisible Annotations");
		final GridBagConstraints gbc_invisibleAnnotationsCheckBox = new GridBagConstraints();
		gbc_invisibleAnnotationsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_invisibleAnnotationsCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_invisibleAnnotationsCheckBox.gridx = 0;
		gbc_invisibleAnnotationsCheckBox.gridy = 2;
		this.invisibleAnnotationsCheckBox.setEnabled(false);
		shrinkerSetupPanel.add(this.invisibleAnnotationsCheckBox, gbc_invisibleAnnotationsCheckBox);

		this.visibleAnnotationsCheckBox = new JCheckBox("Remove Visible Annotations");
		final GridBagConstraints gbc_visibleAnnotationsCheckBox = new GridBagConstraints();
		gbc_visibleAnnotationsCheckBox.anchor = GridBagConstraints.WEST;
		gbc_visibleAnnotationsCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_visibleAnnotationsCheckBox.gridx = 0;
		gbc_visibleAnnotationsCheckBox.gridy = 3;
		this.visibleAnnotationsCheckBox.setEnabled(false);
		shrinkerSetupPanel.add(this.visibleAnnotationsCheckBox, gbc_visibleAnnotationsCheckBox);

		this.shrinkerEnabledCheckBox = new JCheckBox("Enabled");
		final GridBagConstraints gbc_shrinkerEnabledCheckBox = new GridBagConstraints();
		gbc_shrinkerEnabledCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_shrinkerEnabledCheckBox.anchor = GridBagConstraints.WEST;
		gbc_shrinkerEnabledCheckBox.gridx = 0;
		gbc_shrinkerEnabledCheckBox.gridy = 0;
		this.shrinkerEnabledCheckBox.addActionListener(e ->
		{
			final boolean enable = this.shrinkerEnabledCheckBox.isSelected();
			this.attributesCheckBox.setEnabled(enable);
			this.debugInfoCheckBox.setEnabled(enable);
			this.invisibleAnnotationsCheckBox.setEnabled(enable);
			this.visibleAnnotationsCheckBox.setEnabled(enable);
		});
		this.add(this.shrinkerEnabledCheckBox, gbc_shrinkerEnabledCheckBox);
	}

	/**
	 * Returns an {@link ShrinkerDelegator} setup accordingly to this {@link ShrinkingTab}.
	 *
	 * @return an {@link ShrinkerDelegator} setup accordingly to this {@link ShrinkingTab}.
	 */
	public final ShrinkerDelegator getShrinker()
	{
		return this.shrinkerEnabledCheckBox.isSelected() ? new ShrinkerDelegator(new ShrinkerSetup(this.visibleAnnotationsCheckBox.isSelected(), this.invisibleAnnotationsCheckBox.isSelected(), this.attributesCheckBox.isSelected(), this.debugInfoCheckBox.isSelected())) : null;
	}

	/**
	 * Sets the tab settings accordingly with the provided {@link ObfuscationConfiguration}.
	 *
	 * @param info
	 *             the {@link ObfuscationConfiguration} used to determine the tab setup.
	 */
	public final void setSettings(final ObfuscationConfiguration info)
	{
		this.shrinkerEnabledCheckBox.setSelected(false);
		this.attributesCheckBox.setSelected(false);
		this.attributesCheckBox.setEnabled(false);
		this.debugInfoCheckBox.setSelected(false);
		this.debugInfoCheckBox.setEnabled(false);
		this.invisibleAnnotationsCheckBox.setSelected(false);
		this.invisibleAnnotationsCheckBox.setEnabled(false);
		this.visibleAnnotationsCheckBox.setSelected(false);
		this.visibleAnnotationsCheckBox.setEnabled(false);

		if (info.transformers != null)
			info.transformers.stream().filter(transformer -> transformer instanceof ShrinkerDelegator).forEach(transformer ->
			{
				this.shrinkerEnabledCheckBox.setSelected(true);
				this.attributesCheckBox.setEnabled(true);
				this.debugInfoCheckBox.setEnabled(true);
				this.invisibleAnnotationsCheckBox.setEnabled(true);
				this.visibleAnnotationsCheckBox.setEnabled(true);

				final ShrinkerSetup setup = ((ShrinkerDelegator) transformer).getSetup();
				this.attributesCheckBox.setSelected(setup.isRemoveAttributes());
				this.debugInfoCheckBox.setSelected(setup.isRemoveDebug());
				this.invisibleAnnotationsCheckBox.setSelected(setup.isRemoveInvisibleAnnotations());
				this.visibleAnnotationsCheckBox.setSelected(setup.isRemoveVisibleAnnotations());
			});
	}
}
