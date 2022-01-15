package com.eric0210.obfuscater.tabs.obfuscation;

import java.awt.*;
import java.util.Objects;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.eric0210.obfuscater.transformer.obfuscator.references.InvokeDynamic;
import com.eric0210.obfuscater.utils.StringGenerator;

public class InvokeDynamicPanel extends JPanel
{
	private static final long serialVersionUID = -7212029343915755621L;

	private final JComboBox<InvokeDynamic.ObfuscationMode> invokeDynamicComboBox;
	private final JCheckBox invokeDynamicCheckBox;
	private JCheckBox injectDecryptor;

	public InvokeDynamicPanel()
	{
		this.setSize(1000, 1000);
		this.setBorder(new TitledBorder("invokedynamic"));

		final GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]
		{
				84, 0, 0, 0
		};
		layout.rowHeights = new int[]
		{
				23, 0
		};
		layout.columnWeights = new double[]
		{
				1.0, 1.0, 0.0, Double.MIN_VALUE
		};
		layout.rowWeights = new double[]
		{
				0.0, Double.MIN_VALUE
		};
		this.setLayout(layout);

		this.invokeDynamicCheckBox = new JCheckBox("Enabled");
		final GridBagConstraints gbc_invokeDynamicCheckBox = new GridBagConstraints();
		gbc_invokeDynamicCheckBox.anchor = GridBagConstraints.NORTHWEST;
		gbc_invokeDynamicCheckBox.insets = new Insets(0, 0, 0, 5);
		gbc_invokeDynamicCheckBox.gridx = 0;
		gbc_invokeDynamicCheckBox.gridy = 0;
		this.add(this.invokeDynamicCheckBox, gbc_invokeDynamicCheckBox);
		this.invokeDynamicCheckBox.setToolTipText("Replaces some Op-Codes with invokedynamics");

		this.injectDecryptor = new JCheckBox("Inject invokedynamic decryptor on existing class");
		this.injectDecryptor.setEnabled(false);
		final GridBagConstraints gbc_injectDecryptor = new GridBagConstraints();
		gbc_injectDecryptor.anchor = GridBagConstraints.NORTHEAST;
		gbc_injectDecryptor.insets = new Insets(0, 0, 0, 5);
		gbc_injectDecryptor.gridx = 1;
		gbc_injectDecryptor.gridy = 0;
		this.add(this.injectDecryptor, gbc_injectDecryptor);

		this.invokeDynamicComboBox = new JComboBox<>();
		this.invokeDynamicComboBox.setModel(new DefaultComboBoxModel<>(InvokeDynamic.ObfuscationMode.values()));
		final GridBagConstraints gbc_invokeDynamicComboBox = new GridBagConstraints();
		gbc_invokeDynamicComboBox.anchor = GridBagConstraints.NORTHEAST;
		gbc_invokeDynamicComboBox.gridx = 2;
		gbc_invokeDynamicComboBox.gridy = 0;
		this.add(this.invokeDynamicComboBox, gbc_invokeDynamicComboBox);
		this.invokeDynamicComboBox.setToolTipText("invokestatic, invokevirtual: Replaces invokestatic, invokevirtual and invokeinterface with invokedynamic instructions.    \r\ninvokestatic, invokevirtual anti-javadeobf: This essentially does the same thing as Light mode, but adds a small deterrent against\n samczun's java-deobfuscator project.    \r\nall statics and invokes: Replaces getstatic, getfield, putstatic, putfield, invokestatic, invokevirtual, invokeinterface and invokespecial\n with invokedynamics.");
		this.invokeDynamicComboBox.setEnabled(false);
		this.invokeDynamicCheckBox.addActionListener(e ->
		{
			final boolean enabled = this.invokeDynamicCheckBox.isSelected();
			this.invokeDynamicComboBox.setEnabled(enabled);
			this.injectDecryptor.setEnabled(enabled);
		});
	}

	public final InvokeDynamic getInvokeDynamicTransformer()
	{
		return this.invokeDynamicCheckBox.isSelected() ? new InvokeDynamic(new InvokeDynamic.Parameter((InvokeDynamic.ObfuscationMode) Objects.requireNonNull(this.invokeDynamicComboBox.getSelectedItem()), StringGenerator.RENAMER_CLASSNAME_GENERATOR, StringGenerator.RENAMER_METHODNAME_GENERATOR, StringGenerator.RENAMER_FIELDNAME_GENERATOR, this.injectDecryptor.isSelected())) : null;
	}
}
