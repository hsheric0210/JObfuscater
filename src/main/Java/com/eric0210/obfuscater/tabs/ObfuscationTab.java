package com.eric0210.obfuscater.tabs;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.eric0210.obfuscater.tabs.obfuscation.FlowObfuscationPanel;
import com.eric0210.obfuscater.tabs.obfuscation.InvokeDynamicPanel;
import com.eric0210.obfuscater.tabs.obfuscation.LineNumbersPanel;
import com.eric0210.obfuscater.tabs.obfuscation.MiscPanel;
import com.eric0210.obfuscater.tabs.obfuscation.NumberObfuscationPanel;
import com.eric0210.obfuscater.tabs.obfuscation.RenamerPanel;
import com.eric0210.obfuscater.tabs.obfuscation.StringEncryptionPanel;

public class ObfuscationTab extends JPanel
{
	private static final long serialVersionUID = 5270870038802393356L;

	public final StringEncryptionPanel stringEncryptionPanel;
	public final InvokeDynamicPanel invokeDynamicPanel;
	public final FlowObfuscationPanel flowObfuscationPanel;
	public final NumberObfuscationPanel numberObfuscationPanel;
	public final RenamerPanel renamerPanel;
	public final LineNumbersPanel lineNumbersPanel;
	public final MiscPanel miscPanel;

	public ObfuscationTab()
	{
		this.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Obfuscation", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		final GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]
		{
				0, 0
		};
		layout.rowHeights = new int[]
		{
				35, 16, 0, 36, 0, 222, 0, 59, 0
		};
		layout.columnWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		layout.rowWeights = new double[]
		{
				0.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0
		};
		this.setLayout(layout);

		final GridBagConstraints stringEncryptorPanelConstraints = new GridBagConstraints();
		stringEncryptorPanelConstraints.insets = new Insets(0, 0, 5, 0);
		stringEncryptorPanelConstraints.fill = GridBagConstraints.BOTH;
		stringEncryptorPanelConstraints.gridx = 0;
		stringEncryptorPanelConstraints.gridy = 0;
		this.add(this.stringEncryptionPanel = new StringEncryptionPanel(), stringEncryptorPanelConstraints);

		final GridBagConstraints invokeDynamicPanelConstraints = new GridBagConstraints();
		invokeDynamicPanelConstraints.insets = new Insets(0, 0, 5, 0);
		invokeDynamicPanelConstraints.fill = GridBagConstraints.BOTH;
		invokeDynamicPanelConstraints.gridx = 0;
		invokeDynamicPanelConstraints.gridy = 1;
		this.add(this.invokeDynamicPanel = new InvokeDynamicPanel(), invokeDynamicPanelConstraints);

		final GridBagConstraints flowObfuscationPanelConstraints = new GridBagConstraints();
		flowObfuscationPanelConstraints.insets = new Insets(0, 0, 5, 0);
		flowObfuscationPanelConstraints.fill = GridBagConstraints.BOTH;
		flowObfuscationPanelConstraints.gridx = 0;
		flowObfuscationPanelConstraints.gridy = 2;
		this.add(this.flowObfuscationPanel = new FlowObfuscationPanel(), flowObfuscationPanelConstraints);

		final GridBagConstraints gbc_numberObfuscationPanel = new GridBagConstraints();
		gbc_numberObfuscationPanel.insets = new Insets(0, 0, 5, 0);
		gbc_numberObfuscationPanel.fill = GridBagConstraints.BOTH;
		gbc_numberObfuscationPanel.gridx = 0;
		gbc_numberObfuscationPanel.gridy = 3;
		this.add(this.numberObfuscationPanel = new NumberObfuscationPanel(), gbc_numberObfuscationPanel);

		final GridBagConstraints renamerPanelConstraints = new GridBagConstraints();
		renamerPanelConstraints.insets = new Insets(0, 0, 5, 0);
		renamerPanelConstraints.fill = GridBagConstraints.BOTH;
		renamerPanelConstraints.gridx = 0;
		renamerPanelConstraints.gridy = 4;
		this.add(this.renamerPanel = new RenamerPanel(), renamerPanelConstraints);

		final GridBagConstraints lineNumbersPanelConstraints = new GridBagConstraints();
		lineNumbersPanelConstraints.insets = new Insets(0, 0, 5, 0);
		lineNumbersPanelConstraints.fill = GridBagConstraints.BOTH;
		lineNumbersPanelConstraints.gridx = 0;
		lineNumbersPanelConstraints.gridy = 5;
		this.add(this.lineNumbersPanel = new LineNumbersPanel(), lineNumbersPanelConstraints);

		final GridBagConstraints miscPanelConstraints = new GridBagConstraints();
		miscPanelConstraints.insets = new Insets(0, 0, 5, 0);
		miscPanelConstraints.fill = GridBagConstraints.BOTH;
		miscPanelConstraints.gridx = 0;
		miscPanelConstraints.gridy = 6;
		this.add(this.miscPanel = new MiscPanel(), miscPanelConstraints);
	}
}
