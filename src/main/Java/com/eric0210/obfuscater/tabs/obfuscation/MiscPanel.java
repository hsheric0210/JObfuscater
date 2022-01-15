package com.eric0210.obfuscater.tabs.obfuscation;

import java.awt.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.eric0210.obfuscater.tabs.StringGeneratorPanel;
import com.eric0210.obfuscater.transformer.obfuscator.attributes.SignatureObfuscation;
import com.eric0210.obfuscater.transformer.obfuscator.MemberShuffler;
import com.eric0210.obfuscater.transformer.obfuscator.attributes.SourceDebugObfuscation;
import com.eric0210.obfuscater.transformer.obfuscator.attributes.SourceFileObfuscation;
import com.eric0210.obfuscater.transformer.obfuscator.attributes.HideCode;
import com.eric0210.obfuscater.utils.StringGenerator;

public class MiscPanel extends JPanel
{
	private static final long serialVersionUID = -1584976159326000220L;

	private final JCheckBox sourceNameEnabledCheckBox;
	private final JRadioButton sourceNameRemoveButton;
	private JRadioButton sourceNameReplaceButton;
	private JTextField sourceNameReplaceTo;
	private JRadioButton sourceNameRandomizeButton;
	private StringGeneratorPanel sourceNameStringGeneratorPanel;

	private final JCheckBox sourceDebugEnabledCheckBox;
	private final JRadioButton sourceDebugRemoveButton;
	private JRadioButton sourceDebugReplaceButton;
	private JTextField sourceDebugReplaceTo;
	private JRadioButton sourceDebugRandomizeButton;
	private StringGeneratorPanel sourceDebugStringGeneratorPanel;

	private final JCheckBox signObfuscationEnabledCheckBox;
	private JRadioButton signObfuscationRemoveButton;
	private JRadioButton signObfuscationReplaceButton;
	private JTextField signObfuscationReplaceTo;
	private JRadioButton signObfuscationRandomizeButton;
	private StringGeneratorPanel signObfuscationStringGeneratorPanel;

	private final JCheckBox hideCodeCheckBox;

	private final JCheckBox shufflerCheckBox;

	private final JPanel trashclassPanel;
	private final JSpinner trashClassesSpinner;

	private final JPanel jarFileCommentPanel;
	private final JTextArea jarFileComment;

	public MiscPanel()
	{
		this.setSize(10000, 10000);
		this.setBorder(new TitledBorder("Miscellaneous"));

		final GridBagLayout layout = new GridBagLayout();
		layout.columnWidths = new int[]
		{
				241, 0, 0
		};
		layout.rowHeights = new int[]
		{
				0, 0, 0, 0, 0, 0
		};
		layout.columnWeights = new double[]
		{
				0.0, 1.0, Double.MIN_VALUE
		};
		layout.rowWeights = new double[]
		{
				0.0, 0.0, 0.0, 0.0, 0.0, 0.0
		};
		this.setLayout(layout);

		this.sourceNameEnabledCheckBox = new JCheckBox("Class SourceFile Attributes");
		final GridBagConstraints gbc_sourceNameCheckBox = new GridBagConstraints();
		gbc_sourceNameCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_sourceNameCheckBox.anchor = GridBagConstraints.NORTHWEST;
		gbc_sourceNameCheckBox.gridx = 0;
		gbc_sourceNameCheckBox.gridy = 0;
		this.add(this.sourceNameEnabledCheckBox, gbc_sourceNameCheckBox);
		this.sourceNameEnabledCheckBox.setToolTipText("Obfuscate the sourcefile attribute by either randomizing the data, or removing it altogether.");
		this.sourceNameRemoveButton = new JRadioButton("Remove");
		final GridBagConstraints gbc_sourceNameRemove = new GridBagConstraints();
		gbc_sourceNameRemove.anchor = GridBagConstraints.NORTHEAST;
		gbc_sourceNameRemove.insets = new Insets(0, 0, 5, 5);
		gbc_sourceNameRemove.gridx = 1;
		gbc_sourceNameRemove.gridy = 0;
		this.add(this.sourceNameRemoveButton, gbc_sourceNameRemove);
		this.sourceNameRemoveButton.addActionListener(e ->
		{
			if (this.sourceNameRemoveButton.isSelected())
			{
				this.sourceNameReplaceTo.setEnabled(false);
				this.sourceNameRandomizeButton.setSelected(false);
				this.sourceNameReplaceButton.setSelected(false);
				this.sourceNameStringGeneratorPanel.setEnabled(false);
			}
			else
				this.sourceNameRemoveButton.setSelected(true);
		});
		this.sourceNameRemoveButton.setSelected(true);
		this.sourceNameRemoveButton.setEnabled(false);
		this.sourceNameReplaceButton = new JRadioButton("Replace");
		final GridBagConstraints gbc_sourceNameReplace = new GridBagConstraints();
		gbc_sourceNameReplace.anchor = GridBagConstraints.NORTHEAST;
		gbc_sourceNameReplace.insets = new Insets(0, 0, 5, 5);
		gbc_sourceNameReplace.gridx = 3;
		gbc_sourceNameReplace.gridy = 0;
		this.add(this.sourceNameReplaceButton, gbc_sourceNameReplace);
		this.sourceNameReplaceButton.addActionListener(e ->
		{
			if (this.sourceNameReplaceButton.isSelected())
			{
				this.sourceNameRemoveButton.setSelected(false);
				this.sourceNameRandomizeButton.setSelected(false);
				this.sourceNameReplaceTo.setEnabled(true);
				this.sourceNameStringGeneratorPanel.setEnabled(false);
			}
			else
				this.sourceNameReplaceButton.setSelected(true);
		});
		this.sourceNameReplaceButton.setEnabled(false);
		this.sourceNameReplaceTo = new JTextField();
		final GridBagConstraints gbc_sourceNameReplaceTo = new GridBagConstraints();
		gbc_sourceNameReplaceTo.anchor = GridBagConstraints.NORTHWEST;
		gbc_sourceNameReplaceTo.insets = new Insets(0, 0, 5, 5);
		gbc_sourceNameReplaceTo.gridx = 4;
		gbc_sourceNameReplaceTo.gridy = 0;
		this.add(this.sourceNameReplaceTo, gbc_sourceNameReplaceTo);
		this.sourceNameReplaceTo.setEnabled(false);
		this.sourceNameReplaceTo.setColumns(10);
		this.sourceNameRandomizeButton = new JRadioButton("Randomize");
		final GridBagConstraints gbc_sourceNameRandomize = new GridBagConstraints();
		gbc_sourceNameRandomize.anchor = GridBagConstraints.NORTHEAST;
		gbc_sourceNameRandomize.insets = new Insets(0, 0, 5, 5);
		gbc_sourceNameRandomize.gridx = 5;
		gbc_sourceNameRandomize.gridy = 0;
		this.add(this.sourceNameRandomizeButton, gbc_sourceNameRandomize);
		this.sourceNameRandomizeButton.addActionListener(e ->
		{
			if (this.sourceNameRandomizeButton.isSelected())
			{
				this.sourceNameReplaceTo.setEnabled(false);
				this.sourceNameRemoveButton.setSelected(false);
				this.sourceNameReplaceButton.setSelected(false);
				this.sourceNameStringGeneratorPanel.setEnabled(true);
			}
			else
				this.sourceNameRandomizeButton.setSelected(true);
		});
		this.sourceNameRandomizeButton.setEnabled(false);
		this.sourceNameStringGeneratorPanel = new StringGeneratorPanel("SourceName Attribute Randomization", true, null);
		this.sourceNameStringGeneratorPanel.setEnabled(false);
		final GridBagConstraints gbc_sourceNameStringGeneratorPanel = new GridBagConstraints();
		gbc_sourceNameStringGeneratorPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_sourceNameStringGeneratorPanel.insets = new Insets(0, 0, 5, 0);
		gbc_sourceNameStringGeneratorPanel.gridx = 6;
		gbc_sourceNameStringGeneratorPanel.gridy = 0;
		this.add(this.sourceNameStringGeneratorPanel, gbc_sourceNameStringGeneratorPanel);
		this.sourceDebugEnabledCheckBox = new JCheckBox("Class SourceDebug Attributes");
		final GridBagConstraints gbc_sourceDebugCheckBox = new GridBagConstraints();
		gbc_sourceDebugCheckBox.anchor = GridBagConstraints.NORTHWEST;
		gbc_sourceDebugCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_sourceDebugCheckBox.gridx = 0;
		gbc_sourceDebugCheckBox.gridy = 1;
		this.add(this.sourceDebugEnabledCheckBox, gbc_sourceDebugCheckBox);
		this.sourceDebugEnabledCheckBox.setToolTipText("Obfuscate the sourcedebug attribute");
		this.sourceDebugRemoveButton = new JRadioButton("Remove");
		final GridBagConstraints gbc_sourceDebugRemove = new GridBagConstraints();
		gbc_sourceDebugRemove.anchor = GridBagConstraints.NORTHEAST;
		gbc_sourceDebugRemove.insets = new Insets(0, 0, 5, 5);
		gbc_sourceDebugRemove.gridx = 1;
		gbc_sourceDebugRemove.gridy = 1;
		this.add(this.sourceDebugRemoveButton, gbc_sourceDebugRemove);
		this.sourceDebugRemoveButton.addActionListener(e ->
		{
			if (this.sourceDebugRemoveButton.isSelected())
			{
				this.sourceDebugReplaceTo.setEnabled(false);
				this.sourceDebugRandomizeButton.setSelected(false);
				this.sourceDebugReplaceButton.setSelected(false);
				this.sourceDebugStringGeneratorPanel.setEnabled(false);
			}
			else
				this.sourceDebugRemoveButton.setSelected(true);
		});
		this.sourceDebugRemoveButton.setSelected(true);
		this.sourceDebugRemoveButton.setEnabled(false);
		this.sourceDebugReplaceButton = new JRadioButton("Replace");
		this.sourceDebugReplaceButton.addActionListener(e ->
		{
			if (this.sourceDebugReplaceButton.isSelected())
			{
				this.sourceDebugRemoveButton.setSelected(false);
				this.sourceDebugRandomizeButton.setSelected(false);
				this.sourceDebugReplaceTo.setEnabled(true);
				this.sourceDebugStringGeneratorPanel.setEnabled(false);
			}
			else
				this.sourceDebugReplaceButton.setSelected(true);
			this.sourceDebugStringGeneratorPanel.setEnabled(false);
		});
		final GridBagConstraints gbc_sourceDebugReplace = new GridBagConstraints();
		gbc_sourceDebugReplace.anchor = GridBagConstraints.NORTHEAST;
		gbc_sourceDebugReplace.insets = new Insets(0, 0, 5, 5);
		gbc_sourceDebugReplace.gridx = 3;
		gbc_sourceDebugReplace.gridy = 1;
		this.add(this.sourceDebugReplaceButton, gbc_sourceDebugReplace);

		this.sourceDebugReplaceButton.setEnabled(false);
		this.sourceDebugReplaceTo = new JTextField();
		final GridBagConstraints gbc_sourceDebugReplaceTo = new GridBagConstraints();
		gbc_sourceDebugReplaceTo.anchor = GridBagConstraints.NORTHWEST;
		gbc_sourceDebugReplaceTo.insets = new Insets(0, 0, 5, 5);
		gbc_sourceDebugReplaceTo.gridx = 4;
		gbc_sourceDebugReplaceTo.gridy = 1;
		this.add(this.sourceDebugReplaceTo, gbc_sourceDebugReplaceTo);
		this.sourceDebugReplaceTo.setEnabled(false);
		this.sourceDebugReplaceTo.setColumns(10);
		this.sourceDebugRandomizeButton = new JRadioButton("Randomize");
		this.sourceDebugRandomizeButton.addActionListener(e ->
		{
			if (this.sourceDebugRandomizeButton.isSelected())
			{
				this.sourceDebugReplaceTo.setEnabled(false);
				this.sourceDebugRemoveButton.setSelected(false);
				this.sourceDebugReplaceButton.setSelected(false);
				this.sourceDebugStringGeneratorPanel.setEnabled(true);
			}
			else
				this.sourceDebugRandomizeButton.setSelected(true);
		});
		final GridBagConstraints gbc_sourceDebugRandomize = new GridBagConstraints();
		gbc_sourceDebugRandomize.anchor = GridBagConstraints.NORTHEAST;
		gbc_sourceDebugRandomize.insets = new Insets(0, 0, 5, 5);
		gbc_sourceDebugRandomize.gridx = 5;
		gbc_sourceDebugRandomize.gridy = 1;
		this.add(this.sourceDebugRandomizeButton, gbc_sourceDebugRandomize);

		this.sourceDebugRandomizeButton.setEnabled(false);
		this.sourceDebugStringGeneratorPanel = new StringGeneratorPanel("SourceDebug Attribute Randomization", true, null);
		this.sourceDebugStringGeneratorPanel.setEnabled(false);
		final GridBagConstraints gbc_sourceDebugStringGeneratorPanel = new GridBagConstraints();
		gbc_sourceDebugStringGeneratorPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_sourceDebugStringGeneratorPanel.insets = new Insets(0, 0, 5, 0);
		gbc_sourceDebugStringGeneratorPanel.gridx = 6;
		gbc_sourceDebugStringGeneratorPanel.gridy = 1;
		this.add(this.sourceDebugStringGeneratorPanel, gbc_sourceDebugStringGeneratorPanel);
		this.signObfuscationEnabledCheckBox = new JCheckBox("Member Signature Attributes");
		this.signObfuscationEnabledCheckBox.addActionListener(e ->
		{
			this.signObfuscationRemoveButton.setEnabled(this.signObfuscationEnabledCheckBox.isSelected());
			this.signObfuscationRandomizeButton.setEnabled(this.signObfuscationEnabledCheckBox.isSelected());
			this.signObfuscationReplaceButton.setEnabled(this.signObfuscationEnabledCheckBox.isSelected());
			if (this.signObfuscationEnabledCheckBox.isSelected())
			{
				this.signObfuscationReplaceTo.setEnabled(this.signObfuscationReplaceButton.isSelected());
				this.signObfuscationStringGeneratorPanel.setEnabled(this.signObfuscationRandomizeButton.isSelected());
			}
			else
			{
				this.signObfuscationReplaceTo.setEnabled(false);
				this.signObfuscationStringGeneratorPanel.setEnabled(false);
			}
		});
		this.signObfuscationEnabledCheckBox.addActionListener(e -> this.signObfuscationStringGeneratorPanel.setEnabled(this.signObfuscationEnabledCheckBox.isSelected()));
		final GridBagConstraints gbc_signobfCheckBox = new GridBagConstraints();
		gbc_signobfCheckBox.anchor = GridBagConstraints.NORTHWEST;
		gbc_signobfCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_signobfCheckBox.gridx = 0;
		gbc_signobfCheckBox.gridy = 2;
		this.add(this.signObfuscationEnabledCheckBox, gbc_signobfCheckBox);
		this.signObfuscationEnabledCheckBox.setToolTipText("Replaces signature of class, method, field to random string. A known trick to work on JD, CFR, Procyon and Javap.");

		this.signObfuscationRemoveButton = new JRadioButton("Remove");
		this.signObfuscationRemoveButton.addActionListener(e ->
		{
			if (this.signObfuscationRemoveButton.isSelected())
			{
				this.signObfuscationReplaceTo.setEnabled(false);
				this.signObfuscationRandomizeButton.setSelected(false);
				this.signObfuscationReplaceButton.setSelected(false);
				this.signObfuscationStringGeneratorPanel.setEnabled(false);
			}
			else
				this.signObfuscationRemoveButton.setSelected(true);
		});
		this.signObfuscationRemoveButton.setSelected(true);
		this.signObfuscationRemoveButton.setEnabled(false);
		final GridBagConstraints gbc_signobfRemove = new GridBagConstraints();
		gbc_signobfRemove.anchor = GridBagConstraints.NORTHEAST;
		gbc_signobfRemove.insets = new Insets(0, 0, 5, 5);
		gbc_signobfRemove.gridx = 1;
		gbc_signobfRemove.gridy = 2;
		this.add(this.signObfuscationRemoveButton, gbc_signobfRemove);

		this.signObfuscationReplaceButton = new JRadioButton("Replace");
		this.signObfuscationReplaceButton.addActionListener(e ->
		{
			if (this.signObfuscationReplaceButton.isSelected())
			{
				this.signObfuscationRemoveButton.setSelected(false);
				this.signObfuscationRandomizeButton.setSelected(false);
				this.signObfuscationReplaceTo.setEnabled(true);
				this.signObfuscationStringGeneratorPanel.setEnabled(false);
			}
			else
				this.signObfuscationReplaceButton.setSelected(true);
		});
		this.signObfuscationReplaceButton.setEnabled(false);
		final GridBagConstraints gbc_signobfReplace = new GridBagConstraints();
		gbc_signobfReplace.anchor = GridBagConstraints.NORTHEAST;
		gbc_signobfReplace.insets = new Insets(0, 0, 5, 5);
		gbc_signobfReplace.gridx = 3;
		gbc_signobfReplace.gridy = 2;
		this.add(this.signObfuscationReplaceButton, gbc_signobfReplace);

		this.signObfuscationReplaceTo = new JTextField();
		this.signObfuscationReplaceTo.setEnabled(false);
		this.signObfuscationReplaceTo.setColumns(10);
		final GridBagConstraints gbc_signobfReplaceTo = new GridBagConstraints();
		gbc_signobfReplaceTo.anchor = GridBagConstraints.NORTHWEST;
		gbc_signobfReplaceTo.insets = new Insets(0, 0, 5, 5);
		gbc_signobfReplaceTo.gridx = 4;
		gbc_signobfReplaceTo.gridy = 2;
		this.add(this.signObfuscationReplaceTo, gbc_signobfReplaceTo);

		this.signObfuscationRandomizeButton = new JRadioButton("Randomize");
		this.signObfuscationRandomizeButton.addActionListener(e ->
		{
			if (this.signObfuscationRandomizeButton.isSelected())
			{
				this.signObfuscationReplaceTo.setEnabled(false);
				this.signObfuscationRemoveButton.setSelected(false);
				this.signObfuscationReplaceButton.setSelected(false);
				this.signObfuscationStringGeneratorPanel.setEnabled(true);
			}
			else
				this.signObfuscationRandomizeButton.setSelected(true);
		});
		this.signObfuscationRandomizeButton.setEnabled(false);
		final GridBagConstraints gbc_signobfRandomize = new GridBagConstraints();
		gbc_signobfRandomize.anchor = GridBagConstraints.NORTHEAST;
		gbc_signobfRandomize.insets = new Insets(0, 0, 5, 5);
		gbc_signobfRandomize.gridx = 5;
		gbc_signobfRandomize.gridy = 2;
		this.add(this.signObfuscationRandomizeButton, gbc_signobfRandomize);

		this.signObfuscationStringGeneratorPanel = new StringGeneratorPanel("Member Signature Randomization", true, null);
		this.signObfuscationStringGeneratorPanel.setEnabled(false);
		final GridBagConstraints gbc_signobfStringGeneratorPanel = new GridBagConstraints();
		gbc_signobfStringGeneratorPanel.anchor = GridBagConstraints.NORTHWEST;
		gbc_signobfStringGeneratorPanel.insets = new Insets(0, 0, 5, 0);
		gbc_signobfStringGeneratorPanel.gridx = 6;
		gbc_signobfStringGeneratorPanel.gridy = 2;
		this.add(this.signObfuscationStringGeneratorPanel, gbc_signobfStringGeneratorPanel);

		this.hideCodeCheckBox = new JCheckBox("Add Synthetic, Bridge Attributes (Hide Code)");
		final GridBagConstraints gbc_hideCodeCheckBox = new GridBagConstraints();
		gbc_hideCodeCheckBox.anchor = GridBagConstraints.WEST;
		gbc_hideCodeCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_hideCodeCheckBox.gridx = 0;
		gbc_hideCodeCheckBox.gridy = 3;
		this.add(this.hideCodeCheckBox, gbc_hideCodeCheckBox);
		this.hideCodeCheckBox.setToolTipText("Adds a synthetic modifier and bridge modifier if possible to attempt to hide code against some lower-quality\n decompilers.");
		this.shufflerCheckBox = new JCheckBox("Member Ordinal Shuffler");
		final GridBagConstraints gbc_shufflerCheckBox = new GridBagConstraints();
		gbc_shufflerCheckBox.anchor = GridBagConstraints.WEST;
		gbc_shufflerCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_shufflerCheckBox.gridx = 0;
		gbc_shufflerCheckBox.gridy = 4;
		this.add(this.shufflerCheckBox, gbc_shufflerCheckBox);
		this.shufflerCheckBox.setToolTipText("Randomizes the order of methods and fields in a class.");
		this.sourceDebugEnabledCheckBox.addActionListener(e ->
		{
			this.sourceDebugRemoveButton.setEnabled(this.sourceDebugEnabledCheckBox.isSelected());
			this.sourceDebugRandomizeButton.setEnabled(this.sourceDebugEnabledCheckBox.isSelected());
			this.sourceDebugReplaceButton.setEnabled(this.sourceDebugEnabledCheckBox.isSelected());
			if (this.sourceDebugEnabledCheckBox.isSelected())
			{
				this.sourceDebugReplaceTo.setEnabled(this.sourceDebugReplaceButton.isSelected());
				this.sourceDebugStringGeneratorPanel.setEnabled(this.sourceDebugRandomizeButton.isSelected());
			}
			else
			{
				this.sourceDebugReplaceTo.setEnabled(false);
				this.sourceDebugStringGeneratorPanel.setEnabled(false);
			}
		});

		this.sourceNameEnabledCheckBox.addActionListener(e ->
		{
			this.sourceNameRemoveButton.setEnabled(this.sourceNameEnabledCheckBox.isSelected());
			this.sourceNameRandomizeButton.setEnabled(this.sourceNameEnabledCheckBox.isSelected());
			this.sourceNameReplaceButton.setEnabled(this.sourceNameEnabledCheckBox.isSelected());
			if (this.sourceNameEnabledCheckBox.isSelected())
			{
				this.sourceNameReplaceTo.setEnabled(this.sourceNameReplaceButton.isSelected());
				this.sourceNameStringGeneratorPanel.setEnabled(this.sourceNameRandomizeButton.isSelected());
			}
			else
			{
				this.sourceNameReplaceTo.setEnabled(false);
				this.sourceNameStringGeneratorPanel.setEnabled(false);
			}
		});

		this.trashclassPanel = new JPanel();
		this.trashclassPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Trash classes", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		final GridBagConstraints gbc_trashclassPanel = new GridBagConstraints();
		gbc_trashclassPanel.insets = new Insets(0, 0, 0, 5);
		gbc_trashclassPanel.anchor = GridBagConstraints.NORTH;
		gbc_trashclassPanel.gridx = 0;
		gbc_trashclassPanel.gridy = 5;
		this.add(this.trashclassPanel, gbc_trashclassPanel);
		final GridBagLayout gbl_trashclassPanel = new GridBagLayout();
		gbl_trashclassPanel.columnWidths = new int[]
		{
				0
		};
		gbl_trashclassPanel.rowHeights = new int[]
		{
				22, 0
		};
		gbl_trashclassPanel.columnWeights = new double[]
		{
				Double.MIN_VALUE
		};
		gbl_trashclassPanel.rowWeights = new double[]
		{
				0.0, Double.MIN_VALUE
		};
		this.trashclassPanel.setLayout(gbl_trashclassPanel);

		this.trashClassesSpinner = new JSpinner();
		this.trashClassesSpinner.setToolTipText("generates unused classes full\n of random bytecode.");
		this.trashClassesSpinner.setModel(new SpinnerNumberModel(0, null, null, 1));
		final GridBagConstraints gbc_trashClassesSpinner = new GridBagConstraints();
		gbc_trashClassesSpinner.anchor = GridBagConstraints.WEST;
		gbc_trashClassesSpinner.fill = GridBagConstraints.VERTICAL;
		gbc_trashClassesSpinner.gridx = 0;
		gbc_trashClassesSpinner.gridy = 0;
		gbc_trashClassesSpinner.ipadx = 80;
		this.trashclassPanel.add(this.trashClassesSpinner, gbc_trashClassesSpinner);

		this.jarFileCommentPanel = new JPanel();
		final GridBagConstraints gbc_jarFileCommentPanel = new GridBagConstraints();
		gbc_jarFileCommentPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_jarFileCommentPanel.gridwidth = 6;
		gbc_jarFileCommentPanel.anchor = GridBagConstraints.NORTH;
		gbc_jarFileCommentPanel.gridx = 1;
		gbc_jarFileCommentPanel.gridy = 5;
		this.add(this.jarFileCommentPanel, gbc_jarFileCommentPanel);

		this.jarFileCommentPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Output file comments", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		final GridBagLayout gbl_jarFileCommentPanel = new GridBagLayout();
		gbl_jarFileCommentPanel.columnWidths = new int[]
		{
				0, 0
		};
		gbl_jarFileCommentPanel.rowHeights = new int[]
		{
				0, 0
		};
		gbl_jarFileCommentPanel.columnWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		gbl_jarFileCommentPanel.rowWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		this.jarFileCommentPanel.setLayout(gbl_jarFileCommentPanel);
		this.jarFileComment = new JTextArea();
		this.jarFileComment.setText("");
		final GridBagConstraints gbc_jarFileComment = new GridBagConstraints();
		gbc_jarFileComment.fill = GridBagConstraints.BOTH;
		gbc_jarFileComment.gridx = 0;
		gbc_jarFileComment.gridy = 0;
		this.jarFileCommentPanel.add(this.jarFileComment, gbc_jarFileComment);
	}

	public final SourceFileObfuscation getSourceNameTransformer()
	{
		final SourceFileObfuscation.Mode mode = this.sourceNameEnabledCheckBox.isSelected() ? SourceFileObfuscation.Mode.Remove : this.sourceNameReplaceButton.isSelected() ? SourceFileObfuscation.Mode.Replace : this.sourceNameRandomizeButton.isSelected() ? SourceFileObfuscation.Mode.Randomize : null;
		return this.sourceNameEnabledCheckBox.isSelected() ? new SourceFileObfuscation(new SourceFileObfuscation.Parameter(mode, this.sourceNameReplaceTo.getText(), new StringGenerator().configure(this.sourceNameStringGeneratorPanel.getParameter()))) : null;
	}

	public final SourceDebugObfuscation getSourceDebugTransformer()
	{
		final SourceDebugObfuscation.Mode mode = this.sourceDebugEnabledCheckBox.isSelected() ? SourceDebugObfuscation.Mode.Remove : this.sourceDebugReplaceButton.isSelected() ? SourceDebugObfuscation.Mode.Replace : this.sourceDebugRandomizeButton.isSelected() ? SourceDebugObfuscation.Mode.Randomize : null;
		return this.sourceDebugEnabledCheckBox.isSelected() ? new SourceDebugObfuscation(new SourceDebugObfuscation.Parameter(mode, this.sourceDebugReplaceTo.getText(), new StringGenerator().configure(this.sourceDebugStringGeneratorPanel.getParameter()))) : null;
	}

	public final HideCode getHideCodeTransformer()
	{
		return this.hideCodeCheckBox.isSelected() ? new HideCode() : null;
	}

	public final MemberShuffler getFieldShufflerTransformer()
	{
		return this.shufflerCheckBox.isSelected() ? new MemberShuffler() : null;
	}

	public final SignatureObfuscation getSignatureObfuscationTransformer()
	{
		final SignatureObfuscation.Mode mode = this.signObfuscationRemoveButton.isSelected() ? SignatureObfuscation.Mode.Remove : this.signObfuscationReplaceButton.isSelected() ? SignatureObfuscation.Mode.Replace : this.signObfuscationRandomizeButton.isSelected() ? SignatureObfuscation.Mode.Randomize : null;
		return this.signObfuscationEnabledCheckBox.isSelected() ? new SignatureObfuscation(new SignatureObfuscation.Parameter(mode, new StringGenerator().configure(this.signObfuscationStringGeneratorPanel.getParameter()), this.signObfuscationReplaceTo.getText())) : null;
	}

	public final int getTrashClasses()
	{
		return (int) this.trashClassesSpinner.getValue();
	}

	public final String getJarFileComment()
	{
		return this.jarFileComment.getText();
	}
}
