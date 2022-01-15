package com.eric0210.obfuscater;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipFile;

import javax.swing.*;

import com.eric0210.obfuscater.cli.CommandArgumentsParser;
import com.eric0210.obfuscater.config.ConfigurationParser;
import com.eric0210.obfuscater.config.ObfuscationConfiguration;
import com.eric0210.obfuscater.tabs.*;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.WatermarkUtils;

public final class ObfuscatorGUI extends JFrame
{
	private static final long serialVersionUID = 1683478630925482600L;
	private static final Dimension defaultDim = new Dimension(1200, 900);

	public static void main(final String... args)
	{
		// SPAMMING LOTS OF TEXT IS A NECESSITY
		Logger.stdOut("Version: " + Constants.VERSION);
		Logger.stdOut("Contributors: " + Constants.CONTRIBUTORS + "\n");

		// Registers the switches.
		CommandArgumentsParser.registerCommandSwitch("help", 0);
		CommandArgumentsParser.registerCommandSwitch("config", 1);
		CommandArgumentsParser.registerCommandSwitch("extract", 2);

		// Parse away!
		CommandArgumentsParser parser = new CommandArgumentsParser(args);

		// Switch handling.
		if (parser.containsSwitch("help"))
			showHelpMenu();
		else if (parser.containsSwitch("config"))
		{
			File file = new File(parser.getSwitchArgs("config")[0]);
			ConfigurationParser config;
			try
			{
				config = new ConfigurationParser(new FileInputStream(file));
			}
			catch (FileNotFoundException exc)
			{
				Logger.stdErr(String.format("Configuration \"%s\" file not found", file.getName()));
				return;
			}

			// Parse the config and let's start obfuscation.
			Obfuscater obf = new Obfuscater(config.createSessionFromConfig());
			obf.run();
		}
		else if (parser.containsSwitch("extract"))
		{
			// Watermark extraction.
			String[] switchArgs = parser.getSwitchArgs("extract");

			// Input file.
			File leaked = new File(switchArgs[0]);
			if (!leaked.exists())
			{
				Logger.stdErr("Input file not found");
				return;
			}

			try
			{
				// Extract the ids and stick them into the console.
				Logger.stdOut(WatermarkUtils.extractIds(new ZipFile(leaked), switchArgs[1]));
			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
		else
			showHelpMenu();

		Logger.dumpLog();
	}

	private static String getProgramName()
	{
		return new File(ObfuscatorGUI.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getName();
	}

	/**
	 * Prints help message into console.
	 */
	private static void showHelpMenu()
	{
		String name = getProgramName();
		Logger.stdOut(String.format("CLI Usage:\t\t\tjava -jar %s --config example.config", name));
		Logger.stdOut(String.format("Help Menu:\t\t\tjava -jar %s --help", name));
		Logger.stdOut(String.format("License:\t\t\tjava -jar %s --license", name));
		Logger.stdOut(String.format("Watermark Extraction:\tjava -jar %s --extract Input.jar exampleKey", name));
	}

	private ObfuscatorGUI()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (final ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
		{
			e.printStackTrace();
		}

		this.setTitle("Eric's Advanced Java Obfuscater");
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setSize(defaultDim);
		this.setMinimumSize(defaultDim);
		this.setPreferredSize(defaultDim);
		this.setLocationRelativeTo(null);

		final JTabbedPane tabsPane = new JTabbedPane(SwingConstants.TOP);
		this.getContentPane().add(tabsPane, BorderLayout.CENTER);

		final InputOutputTab inputOutputTab = new InputOutputTab();
		final JScrollPane inputOutputTabScrollPane = new JScrollPane(inputOutputTab);
		inputOutputTabScrollPane.getHorizontalScrollBar().setUnitIncrement(25);
		inputOutputTabScrollPane.getVerticalScrollBar().setUnitIncrement(25);
		tabsPane.addTab("Input-Output", null, inputOutputTabScrollPane, null);

		final ObfuscationTab obfuscationPanel = new ObfuscationTab();
		final JScrollPane obfuscationPanelScrollPane = new JScrollPane(obfuscationPanel);
		obfuscationPanelScrollPane.getHorizontalScrollBar().setUnitIncrement(25);
		obfuscationPanelScrollPane.getVerticalScrollBar().setUnitIncrement(25);
		tabsPane.addTab("Obfuscation", null, obfuscationPanelScrollPane, null);

		final OptimizationTab optimizationPanel = new OptimizationTab();
		final JScrollPane optimizationPanelScrollPane = new JScrollPane(optimizationPanel);
		optimizationPanelScrollPane.getHorizontalScrollBar().setUnitIncrement(25);
		optimizationPanelScrollPane.getVerticalScrollBar().setUnitIncrement(25);
		tabsPane.addTab("Optimization", null, optimizationPanelScrollPane, null);

		final ShrinkingTab shrinkingPanel = new ShrinkingTab();
		final JScrollPane shrinkingPanelScrollPane = new JScrollPane(shrinkingPanel);
		shrinkingPanelScrollPane.getHorizontalScrollBar().setUnitIncrement(25);
		shrinkingPanelScrollPane.getVerticalScrollBar().setUnitIncrement(25);
		tabsPane.addTab("Shrinking", null, shrinkingPanelScrollPane, null);

		final WatermarkingTab watermarkingPanel = new WatermarkingTab();
		final JScrollPane watermarkingPanelScrollPane = new JScrollPane(watermarkingPanel);
		watermarkingPanelScrollPane.getHorizontalScrollBar().setUnitIncrement(25);
		watermarkingPanelScrollPane.getVerticalScrollBar().setUnitIncrement(25);
		tabsPane.addTab("Watermarking", null, watermarkingPanelScrollPane, null);

		final MiscellaneousTab miscPanel = new MiscellaneousTab();
		final JScrollPane miscPanelScrollPane = new JScrollPane(miscPanel);
		miscPanelScrollPane.getHorizontalScrollBar().setUnitIncrement(25);
		miscPanelScrollPane.getVerticalScrollBar().setUnitIncrement(25);
		tabsPane.addTab("Miscellaneous", null, miscPanelScrollPane, null);

		final ExclusionsTab exclusionPanel = new ExclusionsTab();
		final JScrollPane exclusionPanelScrollPane = new JScrollPane(exclusionPanel);
		exclusionPanelScrollPane.getHorizontalScrollBar().setUnitIncrement(25);
		exclusionPanelScrollPane.getVerticalScrollBar().setUnitIncrement(25);
		tabsPane.addTab("Exclusions", null, exclusionPanelScrollPane, null);

		final ConsoleTab consolePanel = new ConsoleTab();
		final JScrollPane consolePanelScrollPane = new JScrollPane(consolePanel);
		consolePanelScrollPane.getHorizontalScrollBar().setUnitIncrement(25);
		consolePanelScrollPane.getVerticalScrollBar().setUnitIncrement(25);
		tabsPane.addTab("Console", null, consolePanelScrollPane, null);

		final JPanel bottomToolBar = new JPanel();
		this.getContentPane().add(bottomToolBar, BorderLayout.SOUTH);

		bottomToolBar.setLayout(new BorderLayout(0, 0));
		final JPanel toolBarPanel = new JPanel();
		bottomToolBar.add(toolBarPanel, BorderLayout.EAST);
		final JButton processButton = new JButton("Process");
		processButton.addActionListener(e ->
		{
			consolePanel.resetConsole();

			processButton.setText("Processing...");
			processButton.setEnabled(false);
			tabsPane.setSelectedIndex(7);
			new SwingWorker<Object, Object>()
			{
				@Override
				protected Object doInBackground()
				{
					try
					{
						final ObfuscationConfiguration sessionInfo = new ObfuscationConfiguration();
						sessionInfo.input = new File(inputOutputTab.getInputPath());
						sessionInfo.output = new File(inputOutputTab.getOutputPath());
						sessionInfo.libraries = inputOutputTab.getLibraries();
						final ArrayList<Transformer> transformers = new ArrayList<>();
						transformers.add(shrinkingPanel.getShrinker());
						transformers.add(optimizationPanel.getOptimizer());
						transformers.add(watermarkingPanel.getWatermarker());
						final Transformer renamer;
						transformers.add(renamer = obfuscationPanel.renamerPanel.getRenamerTransformer());
						if (renamer != null)
							sessionInfo.renamerEnabled = true;
						final Transformer hidecode;
						transformers.add(hidecode = obfuscationPanel.miscPanel.getHideCodeTransformer());
						if (hidecode != null)
							sessionInfo.hideCodeEnabled = true;
						transformers.add(obfuscationPanel.miscPanel.getSignatureObfuscationTransformer());
						transformers.add(obfuscationPanel.invokeDynamicPanel.getInvokeDynamicTransformer());
						transformers.add(obfuscationPanel.flowObfuscationPanel.getFlowObfuscationTransformer());
						transformers.add(obfuscationPanel.stringEncryptionPanel.getStringEncryptionTransformer());
						transformers.add(obfuscationPanel.stringEncryptionPanel.getStringPoolObfuscationTransformer());
						transformers.add(obfuscationPanel.numberObfuscationPanel.getNumberObfuscationTransformer());
						transformers.add(obfuscationPanel.renamerPanel.getLocalVariableTransformer());
						transformers.add(obfuscationPanel.lineNumbersPanel.getLineNumberTransformer());
						transformers.add(obfuscationPanel.miscPanel.getSourceNameTransformer());
						transformers.add(obfuscationPanel.miscPanel.getSourceDebugTransformer());
						transformers.add(obfuscationPanel.miscPanel.getFieldShufflerTransformer());
//						transformers.add(obfuscationPanel.getRenamerTransformer()); TODO: why i didn't do it? let's check this out.
//						transformers.add(miscPanel.getExpiration());
						sessionInfo.transformers = transformers;
						sessionInfo.exclusionManager = exclusionPanel.getExclusions();
						sessionInfo.jarFileComment = obfuscationPanel.miscPanel.getJarFileComment();
						sessionInfo.trashClasses = obfuscationPanel.miscPanel.getTrashClasses();
						final Obfuscater obf = new Obfuscater(sessionInfo);
						obf.run();
						Logger.dumpLog();
						JOptionPane.showMessageDialog(null, "Processed successfully.", "Done", JOptionPane.INFORMATION_MESSAGE);
					}
					catch (final HeadlessException e1)
					{
						e1.printStackTrace();
					}
					catch (final Throwable t)
					{
						t.printStackTrace();
						JOptionPane.showMessageDialog(null, "Error happened while processing, check the console for details.", "Error", JOptionPane.ERROR_MESSAGE);
					}
					finally
					{
						processButton.setText("Process");
						processButton.setEnabled(true);
					}
					return null;
				}
			}.execute();
		});
		toolBarPanel.add(processButton);
		this.pack();
		this.setVisible(true);
	}

	public static final String getExceptionStackTrace(final Throwable t)
	{
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.close();
		return sw.toString();
	}
}
