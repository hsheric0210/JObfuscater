package com.eric0210.obfuscater.tabs.obfuscation;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

import com.eric0210.obfuscater.transformer.obfuscator.NumberObfuscation;
import com.eric0210.obfuscater.utils.StringGenerator;
import javax.swing.JLabel;

public class NumberObfuscationPanel extends JPanel
{
	private static final long serialVersionUID = -1946419956559311265L;

	private final JSpinner numberObfuscationRepeatCountSpinner;
	private final JCheckBox numberObfuscationCheckBox;
	private final JLabel numberObfuscationRepeatCountSpinnerLabel;
	private final JCheckBox numberObfuscationZero;
	private final JCheckBox numberObfuscationXOR;
	private final JCheckBox numberObfuscationSimpleMath;
	private final JCheckBox numberObfuscationStringLength;
	private final JCheckBox numberObfuscationAnd;
	private final JCheckBox numberObfuscationShL;

	public NumberObfuscationPanel()
	{
		this.setSize(1920, 200);
		this.setBorder(new TitledBorder("Number Obfuscation"));

		final GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]
		{
				0, 0, 0, 0, 0
		};
		layout.rowHeights = new int[]
		{
				0, 0, 0, 0, 0, 0, 0
		};
		layout.columnWeights = new double[]
		{
				1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE
		};
		layout.rowWeights = new double[]
		{
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE
		};
		this.setLayout(layout);

		this.numberObfuscationCheckBox = new JCheckBox("Enabled");
		this.numberObfuscationCheckBox.setToolTipText("Obfuscates integer and long constants.");
		final GridBagConstraints numberObfuscationCheckBoxConstraints = new GridBagConstraints();
		numberObfuscationCheckBoxConstraints.anchor = GridBagConstraints.NORTHWEST;
		numberObfuscationCheckBoxConstraints.insets = new Insets(0, 0, 5, 5);
		numberObfuscationCheckBoxConstraints.gridx = 0;
		numberObfuscationCheckBoxConstraints.gridy = 0;
		this.add(this.numberObfuscationCheckBox, numberObfuscationCheckBoxConstraints);

		this.numberObfuscationRepeatCountSpinnerLabel = new JLabel("Repeat Count:");
		this.numberObfuscationRepeatCountSpinnerLabel.setEnabled(false);
		final GridBagConstraints gbc_repeatCountLabel = new GridBagConstraints();
		gbc_repeatCountLabel.anchor = GridBagConstraints.EAST;
		gbc_repeatCountLabel.insets = new Insets(0, 0, 5, 5);
		gbc_repeatCountLabel.gridx = 1;
		gbc_repeatCountLabel.gridy = 0;
		this.add(this.numberObfuscationRepeatCountSpinnerLabel, gbc_repeatCountLabel);

		this.numberObfuscationRepeatCountSpinner = new JSpinner();
		this.numberObfuscationRepeatCountSpinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
		this.numberObfuscationRepeatCountSpinner.setToolTipText("Repeat count of the number obfuscation. Increase this count to more obscured value and totally slow-downed program. (Warning: Not working with Advanced mode)");
		this.numberObfuscationRepeatCountSpinner.setEnabled(false);
		this.numberObfuscationRepeatCountSpinnerLabel.setLabelFor(this.numberObfuscationRepeatCountSpinner);
		final GridBagConstraints numberObfuscationRepeatCountSpinnerConstraints = new GridBagConstraints();
		numberObfuscationRepeatCountSpinnerConstraints.anchor = GridBagConstraints.NORTHWEST;
		numberObfuscationRepeatCountSpinnerConstraints.insets = new Insets(0, 0, 5, 5);
		numberObfuscationRepeatCountSpinnerConstraints.ipadx = 15;
		numberObfuscationRepeatCountSpinnerConstraints.gridx = 2;
		numberObfuscationRepeatCountSpinnerConstraints.gridy = 0;
		this.add(this.numberObfuscationRepeatCountSpinner, numberObfuscationRepeatCountSpinnerConstraints);

		this.numberObfuscationXOR = new JCheckBox("Obfuscate numbers with XOR encryption");
		this.numberObfuscationXOR.setSelected(true);
		this.numberObfuscationXOR.setEnabled(false);
		final GridBagConstraints gbc_numberObfuscationXOR = new GridBagConstraints();
		gbc_numberObfuscationXOR.anchor = GridBagConstraints.WEST;
		gbc_numberObfuscationXOR.insets = new Insets(0, 0, 5, 0);
		gbc_numberObfuscationXOR.gridx = 3;
		gbc_numberObfuscationXOR.gridy = 0;
		this.add(this.numberObfuscationXOR, gbc_numberObfuscationXOR);

		this.numberObfuscationSimpleMath = new JCheckBox("Obfuscate numbers with simple math");
		this.numberObfuscationSimpleMath.setEnabled(false);
		this.numberObfuscationSimpleMath.setSelected(true);
		final GridBagConstraints gbc_numberObfuscationSimpleMath = new GridBagConstraints();
		gbc_numberObfuscationSimpleMath.anchor = GridBagConstraints.WEST;
		gbc_numberObfuscationSimpleMath.insets = new Insets(0, 0, 5, 0);
		gbc_numberObfuscationSimpleMath.gridx = 3;
		gbc_numberObfuscationSimpleMath.gridy = 1;
		this.add(this.numberObfuscationSimpleMath, gbc_numberObfuscationSimpleMath);

		this.numberObfuscationStringLength = new JCheckBox("Obfuscate numbers with string length");
		this.numberObfuscationStringLength.setSelected(true);
		this.numberObfuscationStringLength.setEnabled(false);
		final GridBagConstraints gbc_numberObfuscationStringLength = new GridBagConstraints();
		gbc_numberObfuscationStringLength.anchor = GridBagConstraints.WEST;
		gbc_numberObfuscationStringLength.insets = new Insets(0, 0, 5, 0);
		gbc_numberObfuscationStringLength.gridx = 3;
		gbc_numberObfuscationStringLength.gridy = 2;
		this.add(this.numberObfuscationStringLength, gbc_numberObfuscationStringLength);

		this.numberObfuscationAnd = new JCheckBox("Obfuscate numbers with bitwise and operator");
		this.numberObfuscationAnd.setEnabled(false);
		final GridBagConstraints gbc_numberObfuscationAnd = new GridBagConstraints();
		gbc_numberObfuscationAnd.insets = new Insets(0, 0, 5, 0);
		gbc_numberObfuscationAnd.gridx = 3;
		gbc_numberObfuscationAnd.gridy = 3;
		this.add(this.numberObfuscationAnd, gbc_numberObfuscationAnd);

		this.numberObfuscationShL = new JCheckBox("Obfuscate numbers with shift left operator");
		this.numberObfuscationShL.setEnabled(false);
		final GridBagConstraints gbc_numberObfuscationShL = new GridBagConstraints();
		gbc_numberObfuscationShL.anchor = GridBagConstraints.WEST;
		gbc_numberObfuscationShL.insets = new Insets(0, 0, 5, 0);
		gbc_numberObfuscationShL.gridx = 3;
		gbc_numberObfuscationShL.gridy = 4;
		this.add(this.numberObfuscationShL, gbc_numberObfuscationShL);

		this.numberObfuscationZero = new JCheckBox("Obfuscate zero");
		this.numberObfuscationZero.setEnabled(false);
		this.numberObfuscationZero.setSelected(true);
		final GridBagConstraints gbc_numberObfuscationZero = new GridBagConstraints();
		gbc_numberObfuscationZero.anchor = GridBagConstraints.WEST;
		gbc_numberObfuscationZero.gridx = 3;
		gbc_numberObfuscationZero.gridy = 5;
		this.add(this.numberObfuscationZero, gbc_numberObfuscationZero);

		this.numberObfuscationCheckBox.addActionListener(a ->
		{
			final boolean e = this.numberObfuscationCheckBox.isSelected();
			this.numberObfuscationRepeatCountSpinner.setEnabled(e);
			this.numberObfuscationRepeatCountSpinnerLabel.setEnabled(e);
			this.numberObfuscationXOR.setEnabled(e);
			this.numberObfuscationSimpleMath.setEnabled(e);
			this.numberObfuscationStringLength.setEnabled(e);
			this.numberObfuscationAnd.setEnabled(e);
			this.numberObfuscationShL.setEnabled(e);
			this.numberObfuscationZero.setEnabled(e);
		});
	}

	public final NumberObfuscation getNumberObfuscationTransformer()
	{
		return this.numberObfuscationCheckBox.isSelected() ? new NumberObfuscation(new NumberObfuscation.Parameter(this.numberObfuscationZero.isSelected(), this.numberObfuscationXOR.isSelected(), this.numberObfuscationSimpleMath.isSelected(), this.numberObfuscationStringLength.isSelected(), this.numberObfuscationAnd.isSelected(), this.numberObfuscationShL.isSelected(), StringGenerator.RENAMER_CLASSNAME_GENERATOR, StringGenerator.RENAMER_METHODNAME_GENERATOR, StringGenerator.RENAMER_FIELDNAME_GENERATOR, (int) this.numberObfuscationRepeatCountSpinner.getValue())) : null;
	}
}
