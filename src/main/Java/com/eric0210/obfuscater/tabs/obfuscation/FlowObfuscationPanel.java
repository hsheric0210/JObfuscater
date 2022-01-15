package com.eric0210.obfuscater.tabs.obfuscation;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.eric0210.obfuscater.transformer.obfuscator.flow.FlowObfuscation;
import com.eric0210.obfuscater.utils.StringGenerator;

public class FlowObfuscationPanel extends JPanel
{
	private static final long serialVersionUID = 9167609325060650522L;
	private final JCheckBox flowObfuscationCheckBox;
	private final JTextField flowObfuscationFieldName;
	private final JCheckBox insertThrowNullCheckBox;
	private final JCheckBox insertFakeJumpCheckBox;
	private final JCheckBox insertAdvFakeJumpCheckBox;
	private final JCheckBox returnManglerCheckBox;
	private final JCheckBox switchManglerCheckBox;
	private final JCheckBox insertFakeTryCatchCheckBox;
	private final JCheckBox insertBadPopCheckBox;

	public FlowObfuscationPanel()
	{
		this.setSize(1920, 256);
		this.setBorder(new TitledBorder("Flow Obfuscation"));

		final GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]
		{
				0, 0, 0, 0, 0
		};
		layout.rowHeights = new int[]
		{
				0, 0, 0, 0, 0, 0, 0, 0
		};
		layout.columnWeights = new double[]
		{
				1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE
		};
		layout.rowWeights = new double[]
		{
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE
		};
		this.setLayout(layout);

		this.flowObfuscationCheckBox = new JCheckBox("Enabled");
		final GridBagConstraints gbc_flowCheckBox = new GridBagConstraints();
		gbc_flowCheckBox.anchor = GridBagConstraints.NORTHWEST;
		gbc_flowCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_flowCheckBox.gridx = 0;
		gbc_flowCheckBox.gridy = 0;
		this.add(this.flowObfuscationCheckBox, gbc_flowCheckBox);

		this.flowObfuscationCheckBox.setToolTipText("Insert Fake-Jumps, \"if (false) throw null;\"s");

		final JLabel flowFieldNameLabel = new JLabel("Flow Obfuscation Field Name:");
		flowFieldNameLabel.setEnabled(false);
		final GridBagConstraints gbc_flowFieldNameLabel = new GridBagConstraints();
		gbc_flowFieldNameLabel.insets = new Insets(0, 0, 5, 5);
		gbc_flowFieldNameLabel.anchor = GridBagConstraints.EAST;
		gbc_flowFieldNameLabel.gridx = 1;
		gbc_flowFieldNameLabel.gridy = 0;
		this.add(flowFieldNameLabel, gbc_flowFieldNameLabel);

		this.flowObfuscationFieldName = new JTextField();
		this.flowObfuscationFieldName.setEditable(false);
		this.flowObfuscationFieldName.setEnabled(false);
		this.flowObfuscationFieldName.setText("flow");
		final GridBagConstraints gbc_flowFieldName = new GridBagConstraints();
		gbc_flowFieldName.insets = new Insets(0, 0, 5, 5);
		gbc_flowFieldName.anchor = GridBagConstraints.NORTHWEST;
		gbc_flowFieldName.gridx = 2;
		gbc_flowFieldName.gridy = 0;
		this.add(this.flowObfuscationFieldName, gbc_flowFieldName);
		this.flowObfuscationFieldName.setColumns(10);

		this.insertThrowNullCheckBox = new JCheckBox("Insert throw-null sequences");
		this.insertThrowNullCheckBox.setSelected(true);
		this.insertThrowNullCheckBox.setEnabled(false);
		final GridBagConstraints gbc_insertThrowNull = new GridBagConstraints();
		gbc_insertThrowNull.anchor = GridBagConstraints.WEST;
		gbc_insertThrowNull.insets = new Insets(0, 0, 5, 0);
		gbc_insertThrowNull.gridx = 3;
		gbc_insertThrowNull.gridy = 0;
		this.add(this.insertThrowNullCheckBox, gbc_insertThrowNull);

		this.insertBadPopCheckBox = new JCheckBox("Insert fake pop sequences");
		this.insertBadPopCheckBox.setSelected(true);
		this.insertBadPopCheckBox.setEnabled(false);
		final GridBagConstraints gbc_insertBadPop = new GridBagConstraints();
		gbc_insertBadPop.anchor = GridBagConstraints.WEST;
		gbc_insertBadPop.insets = new Insets(0, 0, 5, 0);
		gbc_insertBadPop.gridx = 3;
		gbc_insertBadPop.gridy = 1;
		this.add(this.insertBadPopCheckBox, gbc_insertBadPop);

		this.insertFakeTryCatchCheckBox = new JCheckBox("Insert fake try-catch sequences");
		this.insertFakeTryCatchCheckBox.setSelected(true);
		this.insertFakeTryCatchCheckBox.setEnabled(false);
		final GridBagConstraints gbc_chckbxInsertFakeCatch = new GridBagConstraints();
		gbc_chckbxInsertFakeCatch.anchor = GridBagConstraints.WEST;
		gbc_chckbxInsertFakeCatch.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxInsertFakeCatch.gridx = 3;
		gbc_chckbxInsertFakeCatch.gridy = 2;
		this.add(this.insertFakeTryCatchCheckBox, gbc_chckbxInsertFakeCatch);

		this.insertFakeJumpCheckBox = new JCheckBox("Insert Fake-jump sequences");
		this.insertFakeJumpCheckBox.setEnabled(false);
		final GridBagConstraints gbc_insertFakeJump = new GridBagConstraints();
		gbc_insertFakeJump.anchor = GridBagConstraints.WEST;
		gbc_insertFakeJump.insets = new Insets(0, 0, 5, 0);
		gbc_insertFakeJump.gridx = 3;
		gbc_insertFakeJump.gridy = 3;
		this.add(this.insertFakeJumpCheckBox, gbc_insertFakeJump);

		this.insertAdvFakeJumpCheckBox = new JCheckBox("Insert Advanced Fake-jump sequences");
		this.insertAdvFakeJumpCheckBox.setEnabled(false);
		final GridBagConstraints gbc_insertAdvFakeJump = new GridBagConstraints();
		gbc_insertAdvFakeJump.anchor = GridBagConstraints.WEST;
		gbc_insertAdvFakeJump.insets = new Insets(0, 0, 5, 0);
		gbc_insertAdvFakeJump.gridx = 3;
		gbc_insertAdvFakeJump.gridy = 4;
		this.add(this.insertAdvFakeJumpCheckBox, gbc_insertAdvFakeJump);

		this.returnManglerCheckBox = new JCheckBox("Mangle return instructions");
		this.returnManglerCheckBox.setEnabled(false);
		final GridBagConstraints gbc_returnMangler = new GridBagConstraints();
		gbc_returnMangler.anchor = GridBagConstraints.WEST;
		gbc_returnMangler.insets = new Insets(0, 0, 5, 0);
		gbc_returnMangler.gridx = 3;
		gbc_returnMangler.gridy = 5;
		this.add(this.returnManglerCheckBox, gbc_returnMangler);

		this.switchManglerCheckBox = new JCheckBox("Mangle switch sequences");
		this.switchManglerCheckBox.setEnabled(false);
		final GridBagConstraints gbc_switchMangler = new GridBagConstraints();
		gbc_switchMangler.anchor = GridBagConstraints.WEST;
		gbc_switchMangler.gridx = 3;
		gbc_switchMangler.gridy = 6;
		this.add(this.switchManglerCheckBox, gbc_switchMangler);
		this.flowObfuscationCheckBox.addActionListener(e ->
		{
			this.flowObfuscationFieldName.setEnabled(this.flowObfuscationCheckBox.isSelected());
			this.flowObfuscationFieldName.setEditable(this.flowObfuscationCheckBox.isSelected());
			flowFieldNameLabel.setEnabled(this.flowObfuscationCheckBox.isSelected());
			this.insertThrowNullCheckBox.setEnabled(this.flowObfuscationCheckBox.isSelected());
			this.insertBadPopCheckBox.setEnabled(this.flowObfuscationCheckBox.isSelected());
			this.insertFakeTryCatchCheckBox.setEnabled(this.flowObfuscationCheckBox.isSelected());
			this.insertFakeJumpCheckBox.setEnabled(this.flowObfuscationCheckBox.isSelected());
			this.insertAdvFakeJumpCheckBox.setEnabled(this.flowObfuscationCheckBox.isSelected());
		});
	}

	public final FlowObfuscation getFlowObfuscationTransformer()
	{
		return this.flowObfuscationCheckBox.isSelected() ? new FlowObfuscation(new FlowObfuscation.Parameter(StringGenerator.RENAMER_FIELDNAME_GENERATOR, this.flowObfuscationFieldName.getText(), this.insertThrowNullCheckBox.isSelected(), this.insertBadPopCheckBox.isSelected(), this.insertFakeTryCatchCheckBox.isSelected(), this.insertFakeJumpCheckBox.isSelected(), this.insertAdvFakeJumpCheckBox.isSelected(), this.returnManglerCheckBox.isSelected(), this.switchManglerCheckBox.isSelected())) : null;
	}
}
