package com.eric0210.obfuscater.tabs.obfuscation;

import java.awt.*;
import java.util.Arrays;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.eric0210.obfuscater.tabs.StringGeneratorPanel;
import com.eric0210.obfuscater.transformer.obfuscator.attributes.LocalVariablesNameObfuscation;
import com.eric0210.obfuscater.transformer.obfuscator.Renamer;
import com.eric0210.obfuscater.utils.StringGenerator;
import com.eric0210.obfuscater.utils.StringGeneratorParameter;

public class RenamerPanel extends JPanel
{
	private static final long serialVersionUID = 7647133636623765174L;

	private final JCheckBox renamerEnabledCheckBox;

	private final JPanel renamerPackagePanel;
	private final JCheckBox renamerPackage;
	private final StringGeneratorPanel renamerPackageStringGeneratorPanel;

	private final JPanel renamerClassPanel;
	private final JCheckBox renamerClass;
	private final StringGeneratorPanel renamerClassStringGeneratorPanel;

	private final JPanel renamerFieldPanel;
	private final JCheckBox renamerField;
	private final StringGeneratorPanel renamerFieldStringGeneratorPanel;

	private final JPanel renamerMethodPanel;
	private final JCheckBox renamerMethod;
	private final StringGeneratorPanel renamerMethodStringGeneratorPanel;

	private final JCheckBox localVarsCheckBox;
	private final JRadioButton localVarsRemove;
	private JRadioButton localVarsObfuscate;
	private final JRadioButton localVarsReplace;
	private JTextField localVarsReplaceTo;
	private StringGeneratorPanel localVarsNameGeneratorPanel;

	private final JCheckBox renamerRepackageCheckBox;
	private final JTextField renamerRepackageField;
	private final JCheckBox renamerAdaptResources;
	private final JTextField renamerResourcesField;
	private JRadioButton renamerRepackageObfuscateRadioButton;
	private JPanel localVarsPanel;

	public RenamerPanel()
	{
		this.setSize(10000, 10000);
		this.setBorder(new TitledBorder("Renamer"));

		final GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]
		{
				0, 312, 0, 0, 0, 0, 0
		};
		layout.rowHeights = new int[]
		{
				0, 0, 0, 0, 0, 0
		};
		layout.columnWeights = new double[]
		{
				0.0, 0.0, 0.0, 0.0, 1.0, 1.0, Double.MIN_VALUE
		};
		layout.rowWeights = new double[]
		{
				0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE
		};
		this.setLayout(layout);

		this.renamerRepackageCheckBox = new JCheckBox("Re-package");
		this.renamerRepackageCheckBox.setToolTipText("Re-packages all classes in a package. (Note: If you set StringGeneratorPresets to Custom mode and set random string generator custom charaters to same case characters, it will overwrites the packages without file deletion. it seems like the restriction of java zip libraries.)");
		this.renamerRepackageCheckBox.addActionListener(e ->
		{
			if (this.renamerRepackageCheckBox.isSelected() && this.renamerRepackageCheckBox.isSelected())
				this.renamerRepackageObfuscateRadioButton.setEnabled(true);
			else
				this.renamerRepackageObfuscateRadioButton.setEnabled(false);
		});

		this.renamerClassPanel = new JPanel();
		this.renamerClassPanel.setEnabled(false);
		this.renamerClassPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Classes", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		final GridBagConstraints gbc_renamerClassPanel = new GridBagConstraints();
		gbc_renamerClassPanel.gridwidth = 2;
		gbc_renamerClassPanel.insets = new Insets(0, 0, 5, 5);
		gbc_renamerClassPanel.fill = GridBagConstraints.BOTH;
		gbc_renamerClassPanel.gridx = 1;
		gbc_renamerClassPanel.gridy = 0;
		this.add(this.renamerClassPanel, gbc_renamerClassPanel);
		final GridBagLayout gbl_renamerClassPanel = new GridBagLayout();
		gbl_renamerClassPanel.columnWidths = new int[]
		{
				0, 0
		};
		gbl_renamerClassPanel.rowHeights = new int[]
		{
				0, 0, 0
		};
		gbl_renamerClassPanel.columnWeights = new double[]
		{
				0.0, Double.MIN_VALUE
		};
		gbl_renamerClassPanel.rowWeights = new double[]
		{
				0.0, 0.0, Double.MIN_VALUE
		};
		this.renamerClassPanel.setLayout(gbl_renamerClassPanel);

		this.renamerClass = new JCheckBox("Rename Classes");
		this.renamerClass.setSelected(true);
		this.renamerClass.setEnabled(false);
		final GridBagConstraints gbc_renamerClass = new GridBagConstraints();
		gbc_renamerClass.anchor = GridBagConstraints.SOUTHWEST;
		gbc_renamerClass.insets = new Insets(0, 0, 5, 0);
		gbc_renamerClass.gridx = 0;
		gbc_renamerClass.gridy = 0;
		this.renamerClassPanel.add(this.renamerClass, gbc_renamerClass);

		this.renamerClassStringGeneratorPanel = new StringGeneratorPanel("Class Name Generator", false, Arrays.asList(StringGeneratorParameter.StringGeneratorPresets.ALL));
		final GridBagConstraints gbc_renamerClassStringGeneratorPanel = new GridBagConstraints();
		gbc_renamerClassStringGeneratorPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_renamerClassStringGeneratorPanel.gridx = 0;
		gbc_renamerClassStringGeneratorPanel.gridy = 1;
		this.renamerClassPanel.add(this.renamerClassStringGeneratorPanel, gbc_renamerClassStringGeneratorPanel);
		this.renamerClassStringGeneratorPanel.setEnabled(false);

		this.renamerClass.addActionListener(e -> this.renamerClassStringGeneratorPanel.setEnabled(this.renamerClass.isSelected()));

		this.renamerMethodPanel = new JPanel();
		this.renamerMethodPanel.setEnabled(false);
		this.renamerMethodPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Methods", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		final GridBagConstraints gbc_renamerMethodPanel = new GridBagConstraints();
		gbc_renamerMethodPanel.insets = new Insets(0, 0, 5, 5);
		gbc_renamerMethodPanel.fill = GridBagConstraints.BOTH;
		gbc_renamerMethodPanel.gridx = 3;
		gbc_renamerMethodPanel.gridy = 0;
		this.add(this.renamerMethodPanel, gbc_renamerMethodPanel);
		final GridBagLayout gbl_renamerMethodPanel = new GridBagLayout();
		gbl_renamerMethodPanel.columnWidths = new int[]
		{
				0, 0
		};
		gbl_renamerMethodPanel.rowHeights = new int[]
		{
				0, 0, 0
		};
		gbl_renamerMethodPanel.columnWeights = new double[]
		{
				0.0, Double.MIN_VALUE
		};
		gbl_renamerMethodPanel.rowWeights = new double[]
		{
				0.0, 0.0, Double.MIN_VALUE
		};
		this.renamerMethodPanel.setLayout(gbl_renamerMethodPanel);

		this.renamerMethod = new JCheckBox("Rename Methods");
		this.renamerMethod.setSelected(true);
		this.renamerMethod.setEnabled(false);
		final GridBagConstraints gbc_renamerMethod = new GridBagConstraints();
		gbc_renamerMethod.anchor = GridBagConstraints.SOUTHWEST;
		gbc_renamerMethod.insets = new Insets(0, 0, 5, 0);
		gbc_renamerMethod.gridx = 0;
		gbc_renamerMethod.gridy = 0;
		this.renamerMethodPanel.add(this.renamerMethod, gbc_renamerMethod);

		this.renamerMethodStringGeneratorPanel = new StringGeneratorPanel("Method Name Generator", true, null);
		final GridBagConstraints gbc_renamerMethodStringGeneratorPanelConstraints = new GridBagConstraints();
		gbc_renamerMethodStringGeneratorPanelConstraints.anchor = GridBagConstraints.NORTHWEST;
		gbc_renamerMethodStringGeneratorPanelConstraints.gridx = 0;
		gbc_renamerMethodStringGeneratorPanelConstraints.gridy = 1;
		this.renamerMethodPanel.add(this.renamerMethodStringGeneratorPanel, gbc_renamerMethodStringGeneratorPanelConstraints);
		this.renamerMethodStringGeneratorPanel.setEnabled(false);

		this.renamerMethod.addActionListener(e -> this.renamerMethodStringGeneratorPanel.setEnabled(this.renamerMethod.isSelected()));

		this.renamerPackagePanel = new JPanel();
		this.renamerPackagePanel.setEnabled(false);
		this.renamerPackagePanel.setBorder(new TitledBorder(null, "Packages", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		final GridBagConstraints gbc_renamerPackagePanel = new GridBagConstraints();
		gbc_renamerPackagePanel.gridwidth = 2;
		gbc_renamerPackagePanel.insets = new Insets(0, 0, 5, 5);
		gbc_renamerPackagePanel.fill = GridBagConstraints.BOTH;
		gbc_renamerPackagePanel.gridx = 1;
		gbc_renamerPackagePanel.gridy = 1;
		this.add(this.renamerPackagePanel, gbc_renamerPackagePanel);
		final GridBagLayout gbl_renamerPackagePanel = new GridBagLayout();
		gbl_renamerPackagePanel.columnWidths = new int[]
		{
				0, 0
		};
		gbl_renamerPackagePanel.rowHeights = new int[]
		{
				0, 0, 0
		};
		gbl_renamerPackagePanel.columnWeights = new double[]
		{
				0.0, Double.MIN_VALUE
		};
		gbl_renamerPackagePanel.rowWeights = new double[]
		{
				0.0, 0.0, Double.MIN_VALUE
		};
		this.renamerPackagePanel.setLayout(gbl_renamerPackagePanel);

		this.renamerPackage = new JCheckBox("Rename Packages");
		this.renamerPackage.setSelected(true);
		this.renamerPackage.setEnabled(false);
		final GridBagConstraints gbc_renamerPackage = new GridBagConstraints();
		gbc_renamerPackage.anchor = GridBagConstraints.SOUTHWEST;
		gbc_renamerPackage.insets = new Insets(0, 0, 5, 0);
		gbc_renamerPackage.gridx = 0;
		gbc_renamerPackage.gridy = 0;
		this.renamerPackagePanel.add(this.renamerPackage, gbc_renamerPackage);

		this.renamerPackageStringGeneratorPanel = new StringGeneratorPanel("Package Obfuscater", false, Arrays.asList(StringGeneratorParameter.StringGeneratorPresets.ALL));
		final GridBagConstraints gbc_renamerPackageStringGeneratorPanel = new GridBagConstraints();
		gbc_renamerPackageStringGeneratorPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_renamerPackageStringGeneratorPanel.gridx = 0;
		gbc_renamerPackageStringGeneratorPanel.gridy = 1;
		this.renamerPackagePanel.add(this.renamerPackageStringGeneratorPanel, gbc_renamerPackageStringGeneratorPanel);
		this.renamerPackageStringGeneratorPanel.setEnabled(false);

		this.renamerPackage.addActionListener(e -> this.renamerPackageStringGeneratorPanel.setEnabled(this.renamerPackage.isSelected()));

		this.renamerFieldPanel = new JPanel();
		this.renamerFieldPanel.setEnabled(false);
		this.renamerFieldPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Fields", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		final GridBagConstraints gbc_renamerFieldPanelConstraints = new GridBagConstraints();
		gbc_renamerFieldPanelConstraints.gridwidth = 2;
		gbc_renamerFieldPanelConstraints.anchor = GridBagConstraints.NORTHWEST;
		gbc_renamerFieldPanelConstraints.insets = new Insets(0, 0, 5, 5);
		gbc_renamerFieldPanelConstraints.gridx = 3;
		gbc_renamerFieldPanelConstraints.gridy = 1;
		this.add(this.renamerFieldPanel, gbc_renamerFieldPanelConstraints);
		final GridBagLayout gbl_renamerFieldPanel = new GridBagLayout();
		gbl_renamerFieldPanel.columnWidths = new int[]
		{
				0, 0
		};
		gbl_renamerFieldPanel.rowHeights = new int[]
		{
				0, 0, 0
		};
		gbl_renamerFieldPanel.columnWeights = new double[]
		{
				0.0, Double.MIN_VALUE
		};
		gbl_renamerFieldPanel.rowWeights = new double[]
		{
				0.0, 0.0, Double.MIN_VALUE
		};
		this.renamerFieldPanel.setLayout(gbl_renamerFieldPanel);

		this.renamerField = new JCheckBox("Rename Fields");
		this.renamerField.setSelected(true);
		this.renamerField.setEnabled(false);
		final GridBagConstraints gbc_renamerFieldConstraints = new GridBagConstraints();
		gbc_renamerFieldConstraints.anchor = GridBagConstraints.SOUTHWEST;
		gbc_renamerFieldConstraints.insets = new Insets(0, 0, 5, 0);
		gbc_renamerFieldConstraints.gridx = 0;
		gbc_renamerFieldConstraints.gridy = 0;
		this.renamerFieldPanel.add(this.renamerField, gbc_renamerFieldConstraints);

		this.renamerFieldStringGeneratorPanel = new StringGeneratorPanel("Field Name Generator", true, null);
		this.renamerFieldStringGeneratorPanel.setEnabled(false);
		final GridBagConstraints gbc_renamerFieldStringGeneratorPanelConstraints = new GridBagConstraints();
		gbc_renamerFieldStringGeneratorPanelConstraints.anchor = GridBagConstraints.NORTHWEST;
		gbc_renamerFieldStringGeneratorPanelConstraints.gridx = 0;
		gbc_renamerFieldStringGeneratorPanelConstraints.gridy = 1;
		this.renamerFieldPanel.add(this.renamerFieldStringGeneratorPanel, gbc_renamerFieldStringGeneratorPanelConstraints);

		this.renamerField.addActionListener(e -> this.renamerFieldStringGeneratorPanel.setEnabled(this.renamerField.isSelected()));

		this.localVarsPanel = new JPanel();
		this.localVarsPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Local Variables", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		this.localVarsPanel.setEnabled(false);
		final GridBagConstraints gbc_localVariablesPanel = new GridBagConstraints();
		gbc_localVariablesPanel.gridwidth = 3;
		gbc_localVariablesPanel.insets = new Insets(0, 0, 5, 5);
		gbc_localVariablesPanel.fill = GridBagConstraints.BOTH;
		gbc_localVariablesPanel.gridx = 1;
		gbc_localVariablesPanel.gridy = 2;
		this.add(this.localVarsPanel, gbc_localVariablesPanel);

		final GridBagLayout gbl_localVariablesPanel = new GridBagLayout();
		gbl_localVariablesPanel.columnWidths = new int[]
		{
				74, 0, 0, 0, 0, 0, 0, 0, 0, 0
		};
		gbl_localVariablesPanel.rowHeights = new int[]
		{
				0, 0, 0
		};
		gbl_localVariablesPanel.columnWeights = new double[]
		{
				1.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE
		};
		gbl_localVariablesPanel.rowWeights = new double[]
		{
				1.0, 1.0, Double.MIN_VALUE
		};
		this.localVarsPanel.setLayout(gbl_localVariablesPanel);

		this.localVarsCheckBox = new JCheckBox("Rename Local variables");
		this.localVarsCheckBox.setToolTipText("Obfuscates local variable names by changing their names and descriptions, or removing them entirely.");
		this.localVarsCheckBox.setEnabled(false);
		final GridBagConstraints gbc_localVarCheckBox = new GridBagConstraints();
		gbc_localVarCheckBox.anchor = GridBagConstraints.NORTHWEST;
		gbc_localVarCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_localVarCheckBox.gridx = 0;
		gbc_localVarCheckBox.gridy = 0;
		this.localVarsPanel.add(this.localVarsCheckBox, gbc_localVarCheckBox);

		this.localVarsRemove = new JRadioButton("Remove");
		this.localVarsRemove.setSelected(true);
		this.localVarsRemove.setEnabled(false);
		final GridBagConstraints gbc_localVarsRemove = new GridBagConstraints();
		gbc_localVarsRemove.anchor = GridBagConstraints.NORTHWEST;
		gbc_localVarsRemove.insets = new Insets(0, 0, 5, 5);
		gbc_localVarsRemove.gridx = 1;
		gbc_localVarsRemove.gridy = 0;
		this.localVarsPanel.add(this.localVarsRemove, gbc_localVarsRemove);

		this.localVarsReplace = new JRadioButton("Replace");
		this.localVarsReplace.setEnabled(false);
		this.localVarsReplace.addActionListener(e ->
		{
			if (this.localVarsReplace.isSelected())
			{
				this.localVarsRemove.setSelected(false);
				this.localVarsObfuscate.setSelected(false);
				this.localVarsReplaceTo.setEnabled(true);
				this.localVarsNameGeneratorPanel.setEnabled(false);
			}
			else
				this.localVarsReplace.setSelected(true);
		});
		final GridBagConstraints gbc_localVarsReplace = new GridBagConstraints();
		gbc_localVarsReplace.anchor = GridBagConstraints.NORTHEAST;
		gbc_localVarsReplace.insets = new Insets(0, 0, 5, 5);
		gbc_localVarsReplace.gridx = 4;
		gbc_localVarsReplace.gridy = 0;
		this.localVarsPanel.add(this.localVarsReplace, gbc_localVarsReplace);

		this.localVarsReplaceTo = new JTextField();
		this.localVarsReplaceTo.setEnabled(false);
		this.localVarsReplaceTo.setColumns(10);
		final GridBagConstraints gbc_localVarsReplaceTo = new GridBagConstraints();
		gbc_localVarsReplaceTo.anchor = GridBagConstraints.NORTHWEST;
		gbc_localVarsReplaceTo.insets = new Insets(0, 0, 5, 5);
		gbc_localVarsReplaceTo.gridx = 5;
		gbc_localVarsReplaceTo.gridy = 0;
		gbc_localVarsReplaceTo.ipadx = 100;
		this.localVarsPanel.add(this.localVarsReplaceTo, gbc_localVarsReplaceTo);

		this.localVarsObfuscate = new JRadioButton("Obfuscate");
		this.localVarsObfuscate.addActionListener(e ->
		{
			if (this.localVarsObfuscate.isSelected())
			{
				this.localVarsReplaceTo.setEnabled(false);
				this.localVarsRemove.setSelected(false);
				this.localVarsReplace.setSelected(false);
				this.localVarsNameGeneratorPanel.setEnabled(true);
			}
			else
				this.localVarsObfuscate.setSelected(true);
		});
		this.localVarsObfuscate.setEnabled(false);
		final GridBagConstraints gbc_localVarsObfuscate = new GridBagConstraints();
		gbc_localVarsObfuscate.anchor = GridBagConstraints.NORTHEAST;
		gbc_localVarsObfuscate.insets = new Insets(0, 0, 5, 5);
		gbc_localVarsObfuscate.gridx = 6;
		gbc_localVarsObfuscate.gridy = 0;
		this.localVarsPanel.add(this.localVarsObfuscate, gbc_localVarsObfuscate);
		this.localVarsNameGeneratorPanel = new StringGeneratorPanel("Local Variables Name Obfuscation", true, null);
		this.localVarsNameGeneratorPanel.setEnabled(false);
		final GridBagConstraints gbc_localVarsStringGeneratorPanel = new GridBagConstraints();
		gbc_localVarsStringGeneratorPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_localVarsStringGeneratorPanel.insets = new Insets(0, 0, 5, 5);
		gbc_localVarsStringGeneratorPanel.gridx = 7;
		gbc_localVarsStringGeneratorPanel.gridy = 0;
		this.localVarsPanel.add(this.localVarsNameGeneratorPanel, gbc_localVarsStringGeneratorPanel);

		this.localVarsCheckBox.addActionListener(e ->
		{
			this.localVarsRemove.setEnabled(this.localVarsCheckBox.isSelected());
			this.localVarsObfuscate.setEnabled(this.localVarsCheckBox.isSelected());
			this.localVarsReplace.setEnabled(this.localVarsCheckBox.isSelected());
			if (this.localVarsCheckBox.isSelected())
			{
				this.localVarsReplaceTo.setEnabled(this.localVarsReplace.isSelected());
				this.localVarsNameGeneratorPanel.setEnabled(this.localVarsObfuscate.isSelected());
			}
			else
			{
				this.localVarsReplaceTo.setEnabled(false);
				this.localVarsNameGeneratorPanel.setEnabled(false);
			}
		});

		this.localVarsRemove.addActionListener(e ->
		{
			if (this.localVarsRemove.isSelected())
			{
				this.localVarsReplaceTo.setEnabled(false);
				this.localVarsObfuscate.setSelected(false);
				this.localVarsReplace.setSelected(false);
				this.localVarsNameGeneratorPanel.setEnabled(false);
			}
			else
				this.localVarsRemove.setSelected(true);
		});

		this.renamerRepackageCheckBox.setEnabled(false);
		final GridBagConstraints gbc_renamerRepackageCheckBoxConstraints = new GridBagConstraints();
		gbc_renamerRepackageCheckBoxConstraints.anchor = GridBagConstraints.NORTHWEST;
		gbc_renamerRepackageCheckBoxConstraints.insets = new Insets(0, 0, 5, 5);
		gbc_renamerRepackageCheckBoxConstraints.gridx = 0;
		gbc_renamerRepackageCheckBoxConstraints.gridy = 3;
		this.add(this.renamerRepackageCheckBox, gbc_renamerRepackageCheckBoxConstraints);

		this.renamerRepackageField = new JTextField();
		this.renamerRepackageField.setEditable(false);
		this.renamerRepackageField.setToolTipText("You can use \\ or / to path separator.");
		this.renamerRepackageField.setColumns(10);
		final GridBagConstraints gbc_renamerRepackageFieldConstraints = new GridBagConstraints();
		gbc_renamerRepackageFieldConstraints.anchor = GridBagConstraints.NORTH;
		gbc_renamerRepackageFieldConstraints.insets = new Insets(0, 0, 5, 5);
		gbc_renamerRepackageFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbc_renamerRepackageFieldConstraints.gridx = 1;
		gbc_renamerRepackageFieldConstraints.gridy = 3;
		gbc_renamerRepackageFieldConstraints.ipadx = 100;
		this.add(this.renamerRepackageField, gbc_renamerRepackageFieldConstraints);

		this.renamerRepackageObfuscateRadioButton = new JRadioButton("Obfuscate Packages Name");
		this.renamerRepackageObfuscateRadioButton.setToolTipText("Obfuscates a package to re-package.");
		this.renamerRepackageObfuscateRadioButton.setEnabled(false);
		this.renamerRepackageObfuscateRadioButton.addActionListener(e ->
		{
			this.renamerRepackageField.setEnabled(!this.renamerRepackageObfuscateRadioButton.isSelected());
			if (!this.renamerPackage.isSelected())
				this.renamerPackage.setSelected(true);
		});
		final GridBagConstraints gbc_renamerRepackageObfuscateRadioButtonConstraints = new GridBagConstraints();
		gbc_renamerRepackageObfuscateRadioButtonConstraints.anchor = GridBagConstraints.NORTHEAST;
		gbc_renamerRepackageObfuscateRadioButtonConstraints.insets = new Insets(0, 0, 5, 5);
		gbc_renamerRepackageObfuscateRadioButtonConstraints.gridx = 2;
		gbc_renamerRepackageObfuscateRadioButtonConstraints.gridy = 3;
		this.add(this.renamerRepackageObfuscateRadioButton, gbc_renamerRepackageObfuscateRadioButtonConstraints);

		this.renamerAdaptResources = new JCheckBox("Adapt Resources");
		this.renamerAdaptResources.setToolTipText("Adapt resources with changed class names.");
		this.renamerAdaptResources.setEnabled(false);
		final GridBagConstraints gbc_renamerAdaptResourcesConstraints = new GridBagConstraints();
		gbc_renamerAdaptResourcesConstraints.anchor = GridBagConstraints.NORTHWEST;
		gbc_renamerAdaptResourcesConstraints.insets = new Insets(0, 0, 0, 5);
		gbc_renamerAdaptResourcesConstraints.gridx = 0;
		gbc_renamerAdaptResourcesConstraints.gridy = 4;
		this.add(this.renamerAdaptResources, gbc_renamerAdaptResourcesConstraints);

		this.renamerResourcesField = new JTextField("META-INF/MANIFEST.MF");
		this.renamerResourcesField.setEditable(false);
		this.renamerResourcesField.setToolTipText("You can use ';' for path separator.");
		this.renamerResourcesField.setColumns(10);
		final GridBagConstraints gbc_renamerResourcesFieldConstraints = new GridBagConstraints();
		gbc_renamerResourcesFieldConstraints.insets = new Insets(0, 0, 0, 5);
		gbc_renamerResourcesFieldConstraints.anchor = GridBagConstraints.NORTH;
		gbc_renamerResourcesFieldConstraints.gridwidth = 3;
		gbc_renamerResourcesFieldConstraints.fill = GridBagConstraints.HORIZONTAL;
		gbc_renamerResourcesFieldConstraints.gridx = 1;
		gbc_renamerResourcesFieldConstraints.gridy = 4;
		this.add(this.renamerResourcesField, gbc_renamerResourcesFieldConstraints);

		this.renamerEnabledCheckBox = new JCheckBox("Enabled");
		this.renamerEnabledCheckBox.setToolTipText("Renames classes and their members.");
		final GridBagConstraints gbc_renamerEnabledCheckBox = new GridBagConstraints();
		gbc_renamerEnabledCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_renamerEnabledCheckBox.anchor = GridBagConstraints.NORTHWEST;
		gbc_renamerEnabledCheckBox.gridx = 0;
		gbc_renamerEnabledCheckBox.gridy = 0;
		this.renamerEnabledCheckBox.addActionListener(e ->
		{
			final boolean enabled = this.renamerEnabledCheckBox.isSelected();
			this.renamerRepackageCheckBox.setEnabled(enabled);
			this.renamerRepackageField.setEditable(enabled);
			this.renamerAdaptResources.setEnabled(enabled);
			this.renamerResourcesField.setEditable(enabled);

			this.renamerClassPanel.setEnabled(enabled);
			this.renamerClass.setEnabled(enabled);
			this.renamerClassStringGeneratorPanel.setEnabled(this.renamerClass.isSelected() && enabled);

			this.renamerMethodPanel.setEnabled(enabled);
			this.renamerMethod.setEnabled(enabled);
			this.renamerMethodStringGeneratorPanel.setEnabled(this.renamerMethod.isSelected() && enabled);

			this.renamerPackagePanel.setEnabled(enabled);
			this.renamerPackage.setEnabled(enabled);
			this.renamerPackageStringGeneratorPanel.setEnabled(this.renamerPackage.isSelected() && enabled);

			this.renamerFieldPanel.setEnabled(enabled);
			this.renamerField.setEnabled(enabled);
			this.renamerFieldStringGeneratorPanel.setEnabled(this.renamerField.isSelected() && enabled);

			this.renamerRepackageObfuscateRadioButton.setEnabled(this.renamerRepackageCheckBox.isSelected() && enabled);

			this.localVarsPanel.setEnabled(enabled);
			this.localVarsCheckBox.setEnabled(enabled);
			this.localVarsRemove.setEnabled(this.localVarsCheckBox.isSelected() && enabled);
			this.localVarsReplace.setEnabled(this.localVarsCheckBox.isSelected() && enabled);
			this.localVarsReplaceTo.setEnabled(this.localVarsCheckBox.isSelected() && enabled);
			this.localVarsObfuscate.setEnabled(this.localVarsCheckBox.isSelected() && enabled);
			this.localVarsNameGeneratorPanel.setEnabled(this.localVarsCheckBox.isSelected() && enabled);
		});
		this.add(this.renamerEnabledCheckBox, gbc_renamerEnabledCheckBox);
	}

	public final Renamer getRenamerTransformer()
	{
		if (this.renamerEnabledCheckBox.isSelected())
		{

			StringGenerator.RENAMER_CLASSNAME_GENERATOR = new StringGenerator().configure(this.renamerClassStringGeneratorPanel.getParameter());

			StringGenerator.RENAMER_METHODNAME_GENERATOR = new StringGenerator().configure(this.renamerMethodStringGeneratorPanel.getParameter());

			StringGenerator.RENAMER_FIELDNAME_GENERATOR = new StringGenerator().configure(this.renamerFieldStringGeneratorPanel.getParameter());

			final String[] resourcesToAdapt = this.renamerAdaptResources.isSelected() && this.renamerResourcesField.getText() != null && !this.renamerResourcesField.getText().isEmpty() ? this.renamerResourcesField.getText().split(";") : null;
			final String repackageName = this.renamerRepackageCheckBox.isSelected() && this.renamerRepackageField.getText() != null && !this.renamerRepackageField.getText().isEmpty() ? this.renamerRepackageField.getText() : null;

			return new Renamer(new Renamer.Parameter(resourcesToAdapt, this.renamerRepackageCheckBox.isSelected(), repackageName, this.renamerRepackageObfuscateRadioButton.isSelected(), this.renamerClass.isSelected(), StringGenerator.RENAMER_CLASSNAME_GENERATOR, this.renamerPackage.isSelected(), new StringGenerator().configure(this.renamerPackageStringGeneratorPanel.getParameter()), this.renamerMethod.isSelected(), StringGenerator.RENAMER_METHODNAME_GENERATOR, this.renamerField.isSelected(), StringGenerator.RENAMER_FIELDNAME_GENERATOR));
		}
		return null;
	}

	public final LocalVariablesNameObfuscation getLocalVariableTransformer()
	{
		final LocalVariablesNameObfuscation.Mode mode = this.localVarsRemove.isSelected() ? LocalVariablesNameObfuscation.Mode.Remove : this.localVarsObfuscate.isSelected() ? LocalVariablesNameObfuscation.Mode.Obfuscate : LocalVariablesNameObfuscation.Mode.Replace;
		return this.localVarsCheckBox.isSelected() ? new LocalVariablesNameObfuscation(new LocalVariablesNameObfuscation.Parameter(mode, this.localVarsReplaceTo.getText(), new StringGenerator().configure(this.localVarsNameGeneratorPanel.getParameter()))) : null;
	}
}
