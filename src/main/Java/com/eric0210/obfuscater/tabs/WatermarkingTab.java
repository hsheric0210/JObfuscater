
package com.eric0210.obfuscater.tabs;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.swing.*;
import javax.swing.border.TitledBorder;

import com.eric0210.obfuscater.config.ObfuscationConfiguration;
import com.eric0210.obfuscater.transformer.misc.Watermarker;
import com.eric0210.obfuscater.transformer.misc.WatermarkerSetup;
import com.eric0210.obfuscater.utils.WatermarkUtils;

public class WatermarkingTab extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5144383020892429919L;
	private final JTextField watermarkMessageField;
	private final JTextField watermarkKeyField;
	private final JCheckBox watermarkerEnabledCheckBox;

	public WatermarkingTab()
	{
		final GridBagLayout gbl_this = new GridBagLayout();
		gbl_this.columnWidths = new int[]
		{
				0, 0
		};
		gbl_this.rowHeights = new int[]
		{
				0, 0, 0, 0
		};
		gbl_this.columnWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		gbl_this.rowWeights = new double[]
		{
				0.0, 0.0, 1.0, Double.MIN_VALUE
		};
		this.setBorder(new TitledBorder("Watermarker"));
		this.setLayout(gbl_this);

		final JPanel watermarkerSetupPanel = new JPanel();
		final GridBagConstraints gbc_watermarkerSetupPanel = new GridBagConstraints();
		gbc_watermarkerSetupPanel.insets = new Insets(0, 0, 5, 0);
		gbc_watermarkerSetupPanel.fill = GridBagConstraints.BOTH;
		gbc_watermarkerSetupPanel.gridx = 0;
		gbc_watermarkerSetupPanel.gridy = 1;
		this.add(watermarkerSetupPanel, gbc_watermarkerSetupPanel);
		watermarkerSetupPanel.setBorder(new TitledBorder("Setup"));
		final GridBagLayout gbl_watermarkerSetupPanel = new GridBagLayout();
		gbl_watermarkerSetupPanel.columnWidths = new int[]
		{
				0, 0, 0
		};
		gbl_watermarkerSetupPanel.rowHeights = new int[]
		{
				0, 0, 0
		};
		gbl_watermarkerSetupPanel.columnWeights = new double[]
		{
				0.0, 1.0, Double.MIN_VALUE
		};
		gbl_watermarkerSetupPanel.rowWeights = new double[]
		{
				0.0, 0.0, Double.MIN_VALUE
		};
		watermarkerSetupPanel.setLayout(gbl_watermarkerSetupPanel);

		final JLabel watermarkMessageLabel = new JLabel("Message:");
		final GridBagConstraints gbc_watermarkMessageLabel = new GridBagConstraints();
		gbc_watermarkMessageLabel.anchor = GridBagConstraints.EAST;
		gbc_watermarkMessageLabel.insets = new Insets(0, 5, 5, 5);
		gbc_watermarkMessageLabel.gridx = 0;
		gbc_watermarkMessageLabel.gridy = 0;
		watermarkerSetupPanel.add(watermarkMessageLabel, gbc_watermarkMessageLabel);

		this.watermarkMessageField = new JTextField();
		final GridBagConstraints gbc_watermarkMessageField = new GridBagConstraints();
		gbc_watermarkMessageField.insets = new Insets(0, 0, 5, 5);
		gbc_watermarkMessageField.fill = GridBagConstraints.HORIZONTAL;
		gbc_watermarkMessageField.gridx = 1;
		gbc_watermarkMessageField.gridy = 0;
		this.watermarkMessageField.setEditable(false);
		watermarkerSetupPanel.add(this.watermarkMessageField, gbc_watermarkMessageField);
		this.watermarkMessageField.setColumns(10);

		final JLabel watermarkKeyLabel = new JLabel("Key:");
		final GridBagConstraints gbc_watermarkKeyLabel = new GridBagConstraints();
		gbc_watermarkKeyLabel.anchor = GridBagConstraints.EAST;
		gbc_watermarkKeyLabel.insets = new Insets(0, 0, 5, 5);
		gbc_watermarkKeyLabel.gridx = 0;
		gbc_watermarkKeyLabel.gridy = 1;
		watermarkerSetupPanel.add(watermarkKeyLabel, gbc_watermarkKeyLabel);

		this.watermarkKeyField = new JTextField();
		final GridBagConstraints gbc_watermarkKeyField = new GridBagConstraints();
		gbc_watermarkKeyField.insets = new Insets(0, 0, 5, 5);
		gbc_watermarkKeyField.fill = GridBagConstraints.HORIZONTAL;
		gbc_watermarkKeyField.gridx = 1;
		gbc_watermarkKeyField.gridy = 1;
		this.watermarkKeyField.setEditable(false);
		watermarkerSetupPanel.add(this.watermarkKeyField, gbc_watermarkKeyField);
		this.watermarkKeyField.setColumns(10);

		final JPanel watermarkerExtractor = new JPanel();
		final GridBagConstraints gbc_watermarkerExtractor = new GridBagConstraints();
		gbc_watermarkerExtractor.fill = GridBagConstraints.BOTH;
		gbc_watermarkerExtractor.gridx = 0;
		gbc_watermarkerExtractor.gridy = 2;
		watermarkerExtractor.setBorder(new TitledBorder("Extractor"));
		this.add(watermarkerExtractor, gbc_watermarkerExtractor);
		final GridBagLayout gbl_watermarkerExtractor = new GridBagLayout();
		gbl_watermarkerExtractor.columnWidths = new int[]
		{
				0, 0, 0, 0
		};
		gbl_watermarkerExtractor.rowHeights = new int[]
		{
				0, 0, 0, 0
		};
		gbl_watermarkerExtractor.columnWeights = new double[]
		{
				0.0, 1.0, 0.0, Double.MIN_VALUE
		};
		gbl_watermarkerExtractor.rowWeights = new double[]
		{
				0.0, 0.0, 1.0, Double.MIN_VALUE
		};
		watermarkerExtractor.setLayout(gbl_watermarkerExtractor);

		final JLabel watermarkExtractorInput = new JLabel("Input:");
		final GridBagConstraints gbc_watermarkExtractorInput = new GridBagConstraints();
		gbc_watermarkExtractorInput.anchor = GridBagConstraints.EAST;
		gbc_watermarkExtractorInput.insets = new Insets(0, 5, 5, 5);
		gbc_watermarkExtractorInput.gridx = 0;
		gbc_watermarkExtractorInput.gridy = 0;
		watermarkerExtractor.add(watermarkExtractorInput, gbc_watermarkExtractorInput);

		final JTextField watermarkExtractorInputField = new JTextField();
		final GridBagConstraints gbc_watermarkExtractorInputField = new GridBagConstraints();
		gbc_watermarkExtractorInputField.insets = new Insets(0, 0, 5, 5);
		gbc_watermarkExtractorInputField.fill = GridBagConstraints.HORIZONTAL;
		gbc_watermarkExtractorInputField.gridx = 1;
		gbc_watermarkExtractorInputField.gridy = 0;
		watermarkerExtractor.add(watermarkExtractorInputField, gbc_watermarkExtractorInputField);
		watermarkExtractorInputField.setColumns(10);

		final JButton watermarkExtractorInputButton = new JButton("Select");
		final GridBagConstraints gbc_watermarkExtractorInputButton = new GridBagConstraints();
		gbc_watermarkExtractorInputButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_watermarkExtractorInputButton.insets = new Insets(0, 0, 5, 5);
		gbc_watermarkExtractorInputButton.gridx = 2;
		gbc_watermarkExtractorInputButton.gridy = 0;
		watermarkExtractorInputButton.addActionListener(e ->
		{
			final JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			final int result = chooser.showOpenDialog(this);
			if (result == 0)
				SwingUtilities.invokeLater(() -> watermarkExtractorInputField.setText(chooser.getSelectedFile().getAbsolutePath()));
		});
		watermarkerExtractor.add(watermarkExtractorInputButton, gbc_watermarkExtractorInputButton);

		final JLabel watermarkExtractorKeyLabel = new JLabel("Key:");
		final GridBagConstraints gbc_watermarkExtractorKeyLabel = new GridBagConstraints();
		gbc_watermarkExtractorKeyLabel.anchor = GridBagConstraints.EAST;
		gbc_watermarkExtractorKeyLabel.insets = new Insets(0, 0, 5, 5);
		gbc_watermarkExtractorKeyLabel.gridx = 0;
		gbc_watermarkExtractorKeyLabel.gridy = 1;
		watermarkerExtractor.add(watermarkExtractorKeyLabel, gbc_watermarkExtractorKeyLabel);

		final JTextField watermarkExtractorKeyField = new JTextField();
		final GridBagConstraints gbc_watermarkExtractorKeyField = new GridBagConstraints();
		gbc_watermarkExtractorKeyField.insets = new Insets(0, 0, 5, 5);
		gbc_watermarkExtractorKeyField.fill = GridBagConstraints.HORIZONTAL;
		gbc_watermarkExtractorKeyField.gridx = 1;
		gbc_watermarkExtractorKeyField.gridy = 1;
		watermarkerExtractor.add(watermarkExtractorKeyField, gbc_watermarkExtractorKeyField);
		watermarkExtractorKeyField.setColumns(10);

		final JScrollPane watermarkExtractorScrollPane = new JScrollPane();
		final GridBagConstraints gbc_watermarkExtractorScrollPane = new GridBagConstraints();
		gbc_watermarkExtractorScrollPane.gridwidth = 3;
		gbc_watermarkExtractorScrollPane.insets = new Insets(0, 5, 5, 5);
		gbc_watermarkExtractorScrollPane.fill = GridBagConstraints.BOTH;
		gbc_watermarkExtractorScrollPane.gridx = 0;
		gbc_watermarkExtractorScrollPane.gridy = 2;
		watermarkerExtractor.add(watermarkExtractorScrollPane, gbc_watermarkExtractorScrollPane);

		final DefaultListModel<String> extractionList = new DefaultListModel<>();
		final JList<String> watermarkExtractorList = new JList<>(extractionList);
		watermarkExtractorScrollPane.setViewportView(watermarkExtractorList);

		final JButton watermarkExtractorButton = new JButton("Extract");
		final GridBagConstraints gbc_watermarkExtractorButton = new GridBagConstraints();
		gbc_watermarkExtractorButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_watermarkExtractorButton.insets = new Insets(0, 0, 5, 5);
		gbc_watermarkExtractorButton.gridx = 2;
		gbc_watermarkExtractorButton.gridy = 1;
		watermarkExtractorButton.addActionListener(e ->
		{
			extractionList.clear();
			final File file = new File(watermarkExtractorInputField.getText());
			if (!file.exists())
				throw new WatermarkExtractionException(String.format("Could not find input file %s.", watermarkExtractorInputField.getText()), new FileNotFoundException(watermarkExtractorInputField.getText()));

			try
			{
				final ZipFile zipFile = new ZipFile(file);
				final List<String> ids = WatermarkUtils.extractIds(zipFile, watermarkExtractorKeyField.getText());
				zipFile.close();

				for (final String id : ids)
					extractionList.addElement(id);
			}
			catch (final ZipException ze)
			{
				ze.printStackTrace();
				throw new WatermarkExtractionException("Could not load input file as a zip.");
			}
			catch (final IOException e1)
			{
				e1.printStackTrace();
			}
			catch (final Throwable t)
			{
				t.printStackTrace();
				throw new WatermarkExtractionException();
			}
		});
		watermarkerExtractor.add(watermarkExtractorButton, gbc_watermarkExtractorButton);

		this.watermarkerEnabledCheckBox = new JCheckBox("Enabled");
		final GridBagConstraints gbc_watermarkerEnabledCheckBox = new GridBagConstraints();
		gbc_watermarkerEnabledCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_watermarkerEnabledCheckBox.anchor = GridBagConstraints.WEST;
		gbc_watermarkerEnabledCheckBox.gridx = 0;
		gbc_watermarkerEnabledCheckBox.gridy = 0;
		this.watermarkerEnabledCheckBox.addActionListener(e ->
		{
			final boolean enable = this.watermarkerEnabledCheckBox.isSelected();

			this.watermarkMessageField.setEditable(enable);
			this.watermarkKeyField.setEditable(enable);
		});
		this.add(this.watermarkerEnabledCheckBox, gbc_watermarkerEnabledCheckBox);
	}

	/**
	 * Creates an {@link Watermarker} transformer setup accordingly to the information provided in this {@link WatermarkingTab}.
	 *
	 * @return an {@link Watermarker} transformer setup accordingly to the information provided in this {@link WatermarkingTab}.
	 */
	public final Watermarker getWatermarker()
	{
		return this.watermarkerEnabledCheckBox.isSelected() ? new Watermarker(new WatermarkerSetup(this.watermarkMessageField.getText(), this.watermarkKeyField.getText())) : null;
	}

	/**
	 * Sets the tab settings accordingly with the provided {@link ObfuscationConfiguration}.
	 *
	 * @param info
	 *             the {@link ObfuscationConfiguration} used to determine the tab setup.
	 */
	public final void setSettings(final ObfuscationConfiguration info)
	{
		this.watermarkerEnabledCheckBox.setSelected(false);
		this.watermarkMessageField.setText(null);
		this.watermarkMessageField.setEditable(false);
		this.watermarkKeyField.setText(null);
		this.watermarkKeyField.setEditable(false);

		if (info.transformers != null)
			info.transformers.stream().filter(transformer -> transformer instanceof Watermarker).forEach(transformer ->
			{
				this.watermarkerEnabledCheckBox.setSelected(true);
				this.watermarkMessageField.setEditable(true);
				this.watermarkKeyField.setEditable(true);

				final WatermarkerSetup setup = ((Watermarker) transformer).getSetup();

				this.watermarkMessageField.setText(setup.getMessage());
				this.watermarkKeyField.setText(setup.getKey());
			});
	}
}
