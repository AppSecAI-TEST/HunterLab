/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Knowtator.
 *
 * The Initial Developer of the Original Code is University of Colorado.  
 * Copyright (C) 2005 - 2008.  All Rights Reserved.
 *
 * Knowtator was developed by the Center for Computational Pharmacology
 * (http://compbio.uchcs.edu) at the University of Colorado Health 
 *  Sciences Center School of Medicine with support from the National 
 *  Library of Medicine.  
 *
 * Current information about Knowtator can be obtained at 
 * http://knowtator.sourceforge.net/
 *
 * Contributor(s):
 *   Philip V. Ogren <philip@ogren.info> (Original Author) - pvo
 *   Angus Roberts - ar 
 */

/**
 * Changes
 * 2/8/2007   ar	updated getRecentDirectory see note at method
 * 2/8/2007	  ar 	updated save() see note at method
 *
 */
package edu.uchsc.ccp.knowtator.textsource.files;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.textsource.TextSource;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;
import edu.uchsc.ccp.knowtator.textsource.TextSourceCollection;
import edu.uchsc.ccp.knowtator.textsource.TextSourceIterator;

public class FileTextSourceCollection extends TextSourceCollection {
	public static final String DISPLAY_NAME = "Local Files";

	public static String CLS_NAME = "file text source";

	public static final String PROJECT_SETTING_RECENT_DIRECTORY = new String(
			"FileTextSourceCollection_RECENT_DIRECTORY");

	public static final String PROJECT_SETTING_RECENT_CHARSET = new String("FileTextSourceCollection_RECENT_CHARSET");

	File directory;

	Charset charset;

	String rootPath;

	String name;

	Map<String, TextSource> namesToTextSource;

	Map<TextSource, Integer> textSourceToIndex;

	List<TextSource> textSources;

	List<String> textSourceNames;

	public FileTextSourceCollection(String directoryName, Charset charset) throws IOException {
		this(new File(directoryName), charset);
	}

	public FileTextSourceCollection(File directory, Charset charset) throws IOException {
		this.directory = directory;
		this.charset = charset;
		name = directory.getName();
		rootPath = directory.getPath();

		List<File> files = new ArrayList<File>();
		collectFiles(directory, files);

		Collections.sort(files, new Comparator<File>() {
			public int compare(File file1, File file2) {
				return String.CASE_INSENSITIVE_ORDER.compare(file1.getName(), file2.getName());
			}
		});

		namesToTextSource = new HashMap<String, TextSource>();
		textSourceToIndex = new HashMap<TextSource, Integer>();
		textSources = new ArrayList<TextSource>(files.size());
		textSourceNames = new ArrayList<String>(files.size());

		for (File file : files) {
			TextSource textSource = new FileTextSource(file, rootPath, charset);
			String textSourceName = textSource.getName();
			namesToTextSource.put(textSourceName, textSource);
			textSources.add(textSource);
			textSourceNames.add(textSourceName);
			textSourceToIndex.put(textSource, textSources.size() - 1);
		}
		Collections.sort(textSourceNames);
	}

	/**
	 * This method allows a text source to be added to this TextSourceCollection
	 * on the fly.
	 * 
	 * @param fileToAdd
	 *            the file to add to this FileTextSourceCollection
	 */
	public void addTextSourceToCollection(File fileToAdd) {
		TextSource textSource = new FileTextSource(fileToAdd, rootPath, charset);
		String textSourceName = textSource.getName();
		namesToTextSource.put(textSourceName, textSource);
		textSources.add(textSource);
		textSourceNames.add(textSourceName);
		textSourceToIndex.put(textSource, textSources.size() - 1);
		Collections.sort(textSourceNames);
	}

	public File getDirectory() {
		return directory;
	}

	private void collectFiles(File file, List<File> files) throws IOException {
		if (file.isDirectory()) {
			if (!file.getName().equals(".svn") && !file.getName().equals("CVS")) {
				File[] dirFiles = file.listFiles();
				for (int i = 0; i < dirFiles.length; i++) {
					collectFiles(dirFiles[i], files);
				}
			}
		} else {
			files.add(file);
		}
	}

	public int size() {
		return textSources.size();
	}

	public TextSource get(int index) throws TextSourceAccessException {
		TextSource textSource = textSources.get(index);
		if (textSource != null)
			return textSource;
		throw new TextSourceAccessException("There is no text source at index=" + index
				+ " for text source collection = " + getName());
	}

	public TextSource get(String textSourceName) throws TextSourceAccessException {
		TextSource textSource = namesToTextSource.get(textSourceName);
		if (textSource != null)
			return textSource;
		throw new TextSourceAccessException("There is no text source for name='" + textSourceName
				+ "' for text source collection = " + getName());
	}

	public int getIndex(TextSource textSource) {
		Integer index = textSourceToIndex.get(textSource);
		if (index != null) {
			return index.intValue();
		}
		return -1;
	}

	public String getName() {
		return name;
	}

	public TextSource select(Component parent) {
		Object selection = JOptionPane.showInputDialog(parent, "Select text source", "Select text source",
				JOptionPane.INFORMATION_MESSAGE, null, textSourceNames.toArray(), textSourceNames.toArray()[0]);
		try {
			if (selection != null)
				return get((String) selection);
		} catch (TextSourceAccessException tsae) {
			return null;
		}
		return null;
	}

	public TextSource find(Component parent) {
		String searchString = JOptionPane.showInputDialog(parent, "Enter a regular expression for the name of a text source", "Find a text source",
			JOptionPane.PLAIN_MESSAGE);
		Pattern pattern = Pattern.compile(searchString);
		Matcher matcher;
		try {

			for (String textSourceName : textSourceNames) {
				matcher = pattern.matcher(textSourceName);
				if (matcher.find())
					return get(textSourceName);

			}
		} catch (TextSourceAccessException tsae) {
			tsae.printStackTrace();
		}

		JOptionPane.showMessageDialog(parent, "No text source found with a name matching the search string you provided.");
		return null;
	}

	/**
	 * contribution from Angus Roberts saves a relative path if the directory is
	 * under the protege project directory
	 */
	public void save(Project project) {
		// If the directory is contained in the project, save relative to the
		// project.
		// Otherwise, save the directory as given
		String directoryPath = directory.getAbsolutePath();
		String annotationProjectPath = new File(project.getProjectDirectoryURI().getPath()).getPath();

		if (directoryPath.startsWith(annotationProjectPath)) {
			directoryPath = directoryPath.substring(annotationProjectPath.length() + 1);
			project.setClientInformation(PROJECT_SETTING_RECENT_DIRECTORY, directoryPath);
		} else {
			project.setClientInformation(PROJECT_SETTING_RECENT_DIRECTORY, directory.getPath());
		}
		project.setClientInformation(PROJECT_SETTING_RECENT_CHARSET, charset.name());
	}

	public static TextSourceCollection open(Project project) {
		File recentDirectory = getRecentDirectory(project);
		Charset charset = getRecentCharset(project);
		if (recentDirectory != null && charset != null) {
			try {
				TextSourceCollection tsc = new FileTextSourceCollection(recentDirectory, charset);
				return tsc;
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * updated by Angus Roberts to look for the directory relative to the
	 * protege project directory
	 * 
	 * @param project
	 * @return
	 */
	private static File getRecentDirectory(Project project) {
		String recentPath = (String) project.getClientInformation(PROJECT_SETTING_RECENT_DIRECTORY);

		if (recentPath == null)
			return null;

		File recentDirectory = new File(recentPath);
		// if the path is not absolute, and If the directory can be found
		// below the protege project, restore from here.
		if (!recentDirectory.isAbsolute()) {
			String annotationProjectPath = new File(project.getProjectDirectoryURI()).getPath();
			String absoluteRecentPath = annotationProjectPath + File.separator + recentPath;
			recentDirectory = new File(absoluteRecentPath);
			if (recentDirectory.exists() && recentDirectory.isDirectory()) {
				return recentDirectory;
			} else {
				recentDirectory = new File(recentPath);
			}
		}

		if (recentDirectory.exists() && recentDirectory.isDirectory()) {
			return recentDirectory;
		}
		return null;
	}

	private static Charset getRecentCharset(Project project) {
		String charsetName = (String) project.getClientInformation(PROJECT_SETTING_RECENT_CHARSET);
		if (charsetName == null)
			return null;
		try {
			Charset charset = Charset.forName(charsetName);
			return charset;
		} catch (IllegalCharsetNameException icne) {
			return null;
		}
	}

	public static TextSourceCollection open(Project project, Component parent) {
		JFileChooser chooser = new JFileChooser();
		File homeDirectory = chooser.getCurrentDirectory();
		File recentDirectory = getRecentDirectory(project);
		if (recentDirectory != null) {
			chooser.setCurrentDirectory(recentDirectory);
		}
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showOpenDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file.equals(homeDirectory)) {
				JOptionPane
						.showMessageDialog(
								parent,
								"Choosing your home directory is not allowed. Please select a directory that contains only text files that you want to annotate.",
								"", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			try {
				FileTextSourceCollection tsc = new FileTextSourceCollection(file, Charset.forName("UTF-8"));
				return tsc;
			} catch (IOException ioe) {
				ioe.printStackTrace();
				JOptionPane.showMessageDialog(parent, "Unable to open directory as a text source collection", "",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		return null;
	}

	public static void createCls(KnowledgeBase kb) {
		if (kb.getCls(CLS_NAME) == null) {
			Cls textSourceParent = kb.getCls(KnowtatorProjectUtil.TEXT_SOURCE_CLS_NAME);
			List<Cls> parents = new ArrayList<Cls>();
			parents.add(textSourceParent);
			kb.createCls(CLS_NAME, parents);
		}
		return;
	}

	public TextSourceIterator iterator() {
		return new FileTextSourceIterator();
	}

	class FileTextSourceIterator implements TextSourceIterator {
		int tsIndex = -1;

		public TextSource next() throws TextSourceAccessException {
			try {
				return textSources.get(++tsIndex);
			} catch (IndexOutOfBoundsException ioobe) {
				throw new TextSourceAccessException(ioobe);
			}
		}

		public boolean hasNext() {
			return (tsIndex + 1) < textSources.size();
		}
	}
}

// String[] selectionValues = new String[] {"US-ASCII",
// "ISO-8859-1",
// "UTF-8",
// "UTF-16BE",
// "UTF-16LE",
// "UTF-16"};
//
// String selectionValue = (String) JOptionPane.showInputDialog(parent,"Select
// file encoding","File encoding selection",JOptionPane.PLAIN_MESSAGE, null,
// selectionValues, selectionValues[0]);
// if(selectionValue == null) return null;
// Charset charset = null;
// try
// {
// charset = Charset.forName(selectionValue);
// }
// catch(IllegalCharsetNameException icne)
// {
// JOptionPane.showMessageDialog(parent,"Invalid charset.", "Invalid charset
// error",JOptionPane.ERROR_MESSAGE);
// return null;
// }

