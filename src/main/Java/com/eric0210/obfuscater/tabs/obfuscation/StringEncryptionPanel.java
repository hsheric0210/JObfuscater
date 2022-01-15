package com.eric0210.obfuscater.tabs.obfuscation;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.eric0210.obfuscater.transformer.obfuscator.strings.StringEncryption;
import com.eric0210.obfuscater.transformer.obfuscator.strings.StringPool;
import com.eric0210.obfuscater.utils.StringGenerator;

public class StringEncryptionPanel extends JPanel
{
	private static final long serialVersionUID = -7180437883980295609L;
	public static final ArrayList<String> stringExclusions = new ArrayList<>();

	private final JComboBox<StringEncryption.EncryptionType> stringEncryptionTypeSelector;
	private final JCheckBox stringPoolCheckBox;
	private final JCheckBox stringEncryptionEnabledCheckBox;
	private final JSpinner stringEncryptionRepeatCountSpinner;
	private final JLabel stringEncryptionRepeatCountSpinnerLabel;
	private JComboBox stringPoolTypeSelector;
	private JCheckBox injectDecryptor;
	// private final JComboBox<StringPool.Mode> stringPoolTypeSelector;

	public StringEncryptionPanel()
	{
		this.setSize(1000, 1000);
		this.setBorder(new TitledBorder("String Encryption"));

		final GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]
		{
				0, 0, 0, 0, 0, 0, 0
		};
		layout.rowHeights = new int[]
		{
				0, 0, 0, 0
		};
		layout.columnWeights = new double[]
		{
				1.0, 0.0, 0.0, 1.0, 1.0, 0.0, Double.MIN_VALUE
		};
		layout.rowWeights = new double[]
		{
				0.0, 0.0, 0.0, Double.MIN_VALUE
		};
		this.setLayout(layout);

		this.stringEncryptionEnabledCheckBox = new JCheckBox("Enabled");
		this.stringEncryptionEnabledCheckBox.setToolTipText("Encrypts string literals");

		final GridBagConstraints stringEncryptionEnabledCheckBox_constraints = new GridBagConstraints();
		stringEncryptionEnabledCheckBox_constraints.gridwidth = 2;
		stringEncryptionEnabledCheckBox_constraints.anchor = GridBagConstraints.NORTHWEST;
		stringEncryptionEnabledCheckBox_constraints.insets = new Insets(0, 0, 5, 5);
		stringEncryptionEnabledCheckBox_constraints.gridx = 0;
		stringEncryptionEnabledCheckBox_constraints.gridy = 0;

		this.add(this.stringEncryptionEnabledCheckBox, stringEncryptionEnabledCheckBox_constraints);

		this.stringPoolCheckBox = new JCheckBox("Pool Strings");
		this.stringPoolCheckBox.setToolTipText("Takes all the strings in a class and pools them into a field. When the string is needed, the string pool field is called with an index number.");
		this.stringPoolCheckBox.setEnabled(false);
		this.stringPoolCheckBox.addActionListener(l -> this.stringPoolTypeSelector.setEnabled(this.stringPoolCheckBox.isSelected()));

		this.stringEncryptionTypeSelector = new JComboBox<StringEncryption.EncryptionType>();
		this.stringEncryptionTypeSelector.setModel(new DefaultComboBoxModel<StringEncryption.EncryptionType>(StringEncryption.EncryptionType.values()));
		this.stringEncryptionTypeSelector.setToolTipText("XOR: Encrypts string literals with a simple XOR algorithm.    \r\nBasic: Encrypts string literals using a key and caller context. Caching is down via a custom hashing algorithm.    \r\nMultiThread: Encrypts string literals using very basic multi-threading, a key, caller and name context.");
		this.stringEncryptionTypeSelector.setEnabled(false);

		final GridBagConstraints stringEncryptionTypeSelector_layout = new GridBagConstraints();
		stringEncryptionTypeSelector_layout.insets = new Insets(0, 0, 5, 5);
		stringEncryptionTypeSelector_layout.anchor = GridBagConstraints.WEST;
		stringEncryptionTypeSelector_layout.gridx = 2;
		stringEncryptionTypeSelector_layout.gridy = 0;

		this.add(this.stringEncryptionTypeSelector, stringEncryptionTypeSelector_layout);
		final GridBagConstraints gbc_stringPoolCheckBox = new GridBagConstraints();
		gbc_stringPoolCheckBox.gridwidth = 2;
		gbc_stringPoolCheckBox.anchor = GridBagConstraints.NORTHWEST;
		gbc_stringPoolCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_stringPoolCheckBox.gridx = 3;
		gbc_stringPoolCheckBox.gridy = 0;

		this.add(this.stringPoolCheckBox, gbc_stringPoolCheckBox);

		stringPoolTypeSelector = new JComboBox();
		stringPoolTypeSelector.setEnabled(false);
		stringPoolTypeSelector.setModel(new DefaultComboBoxModel(StringPool.Mode.values()));
		final GridBagConstraints gbc_stringPoolTypeSelector = new GridBagConstraints();
		gbc_stringPoolTypeSelector.insets = new Insets(0, 0, 5, 0);
		gbc_stringPoolTypeSelector.anchor = GridBagConstraints.WEST;
		gbc_stringPoolTypeSelector.gridx = 5;
		gbc_stringPoolTypeSelector.gridy = 0;
		this.add(stringPoolTypeSelector, gbc_stringPoolTypeSelector);

		this.stringEncryptionRepeatCountSpinnerLabel = new JLabel("Repeat Count:");
		this.stringEncryptionRepeatCountSpinnerLabel.setEnabled(false);
		final GridBagConstraints gbc_stringEncryptionRepeatCountSpinnerLabel = new GridBagConstraints();
		gbc_stringEncryptionRepeatCountSpinnerLabel.anchor = GridBagConstraints.EAST;
		gbc_stringEncryptionRepeatCountSpinnerLabel.insets = new Insets(0, 0, 5, 5);
		gbc_stringEncryptionRepeatCountSpinnerLabel.gridx = 1;
		gbc_stringEncryptionRepeatCountSpinnerLabel.gridy = 1;
		this.add(this.stringEncryptionRepeatCountSpinnerLabel, gbc_stringEncryptionRepeatCountSpinnerLabel);

		this.stringEncryptionRepeatCountSpinner = new JSpinner();
		this.stringEncryptionRepeatCountSpinner.setModel(new SpinnerNumberModel(1, 1, 5, 1));
		this.stringEncryptionRepeatCountSpinner.setEnabled(false);
		final GridBagConstraints gbc_stringEncryptionRepeatCountSpinner = new GridBagConstraints();
		gbc_stringEncryptionRepeatCountSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_stringEncryptionRepeatCountSpinner.insets = new Insets(0, 0, 5, 5);
		gbc_stringEncryptionRepeatCountSpinner.ipadx = 15;
		gbc_stringEncryptionRepeatCountSpinner.gridx = 2;
		gbc_stringEncryptionRepeatCountSpinner.gridy = 1;
		this.add(this.stringEncryptionRepeatCountSpinner, gbc_stringEncryptionRepeatCountSpinner);
		this.stringEncryptionRepeatCountSpinnerLabel.setLabelFor(this.stringEncryptionRepeatCountSpinner);

		injectDecryptor = new JCheckBox("Inject String decryptor on existing class");
		injectDecryptor.setEnabled(false);
		GridBagConstraints gbc_injectDecryptor = new GridBagConstraints();
		gbc_injectDecryptor.anchor = GridBagConstraints.EAST;
		gbc_injectDecryptor.insets = new Insets(0, 0, 0, 5);
		gbc_injectDecryptor.gridx = 1;
		gbc_injectDecryptor.gridy = 2;
		this.add(injectDecryptor, gbc_injectDecryptor);

		this.stringEncryptionEnabledCheckBox.addActionListener(e ->
		{
			final boolean enabled = this.stringEncryptionEnabledCheckBox.isSelected();
			this.stringEncryptionTypeSelector.setEnabled(enabled);
			this.stringPoolCheckBox.setEnabled(enabled);
			this.stringEncryptionRepeatCountSpinnerLabel.setEnabled(enabled);
			this.stringEncryptionRepeatCountSpinner.setEnabled(enabled);
			this.injectDecryptor.setEnabled(enabled);
			this.stringPoolTypeSelector.setEnabled(enabled && this.stringPoolCheckBox.isSelected());
		});
	}

	public final StringEncryption getStringEncryptionTransformer()
	{
		return this.stringEncryptionEnabledCheckBox.isSelected() ? new StringEncryption(new StringEncryption.Parameter((StringEncryption.EncryptionType) Objects.requireNonNull(this.stringEncryptionTypeSelector.getSelectedItem()), stringExclusions, StringGenerator.RENAMER_CLASSNAME_GENERATOR, StringGenerator.RENAMER_METHODNAME_GENERATOR, StringGenerator.RENAMER_FIELDNAME_GENERATOR, (int) this.stringEncryptionRepeatCountSpinner.getValue(), injectDecryptor.isSelected())) : null;
	}

	public final StringPool getStringPoolObfuscationTransformer()
	{
		return this.stringEncryptionEnabledCheckBox.isSelected() && this.stringPoolCheckBox.isSelected() ? new StringPool(new StringPool.Parameter((StringPool.Mode) Objects.requireNonNull(this.stringPoolTypeSelector.getSelectedItem()), stringExclusions, StringGenerator.RENAMER_CLASSNAME_GENERATOR, StringGenerator.RENAMER_METHODNAME_GENERATOR, StringGenerator.RENAMER_FIELDNAME_GENERATOR)) : null;
	}
}
