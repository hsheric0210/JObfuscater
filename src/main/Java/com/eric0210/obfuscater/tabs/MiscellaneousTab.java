package com.eric0210.obfuscater.tabs;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class MiscellaneousTab extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2392158185242186765L;

	public MiscellaneousTab()
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
		this.setLayout(gbl_this);
		final JPanel miscOtherPanel = new JPanel();
		final GridBagConstraints gbc_miscOtherPanel = new GridBagConstraints();
		gbc_miscOtherPanel.fill = GridBagConstraints.BOTH;
		gbc_miscOtherPanel.gridx = 0;
		gbc_miscOtherPanel.gridy = 1;
		this.add(miscOtherPanel, gbc_miscOtherPanel);
		final GridBagLayout gbl_miscOtherPanel = new GridBagLayout();
		gbl_miscOtherPanel.columnWidths = new int[]
		{
				0, 0, 0
		};
		gbl_miscOtherPanel.rowHeights = new int[]
		{
				32, 0, 0
		};
		gbl_miscOtherPanel.columnWeights = new double[]
		{
				0.0, 1.0, Double.MIN_VALUE
		};
		gbl_miscOtherPanel.rowWeights = new double[]
		{
				1.0, 1.0, Double.MIN_VALUE
		};
		miscOtherPanel.setBorder(new TitledBorder("Other"));
		miscOtherPanel.setLayout(gbl_miscOtherPanel);
		final JButton garbagCollectorButton = new JButton("Garbage Collector");
		final GridBagConstraints gbc_garbagCollectorButton = new GridBagConstraints();
		gbc_garbagCollectorButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_garbagCollectorButton.insets = new Insets(0, 5, 0, 5);
		gbc_garbagCollectorButton.gridx = 0;
		gbc_garbagCollectorButton.gridy = 0;
		garbagCollectorButton.addActionListener(e -> SwingUtilities.invokeLater(() ->
		{
			final double before = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000;
			Runtime.getRuntime().gc();
			JOptionPane.showMessageDialog(null, "before: " + before + "MB, after: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000 + "MB.");
		}));
		miscOtherPanel.add(garbagCollectorButton, gbc_garbagCollectorButton);
	}
}
