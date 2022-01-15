package com.eric0210.obfuscater.config;

import java.io.File;
import java.util.List;
import com.eric0210.obfuscater.transformer.Transformer;
import com.eric0210.obfuscater.utils.exclusions.ExclusionManager;

public class ObfuscationConfiguration
{
	public File input;
	public File output;
	public List<File> libraries;
	public List<Transformer> transformers;
	public ExclusionManager exclusionManager;
	public int trashClasses;
	public String jarFileComment;

	public boolean renamerEnabled;
	public boolean hideCodeEnabled;

	public int compressionLevel;
	public boolean verify;
}
