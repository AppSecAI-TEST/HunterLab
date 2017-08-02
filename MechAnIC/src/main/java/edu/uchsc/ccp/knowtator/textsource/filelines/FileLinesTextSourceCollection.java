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
 *   Philip V. Ogren <philip@ogren.info> (Original Author)
 */

package edu.uchsc.ccp.knowtator.textsource.filelines;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import edu.uchsc.ccp.util.io.FileReadingUtil;

public class FileLinesTextSourceCollection extends TextSourceCollection {

	public static final String DISPLAY_NAME = "Lines from file";

	public static final String CLS_NAME = "file line text source";

	public static final String PROJECT_SETTING_RECENT_FILE = new String("FileLinesTextSourceCollection_RECENT_FILE");

	File file;

	char nameChar;

	String name;

	protected Map<String, TextSource> namesToTextSource;

	protected Map<TextSource, Integer> textSourceToIndex;

	List<TextSource> textSources;

	List<String> textSourceNames;

	public FileLinesTextSourceCollection(String fileName) throws IOException {
		this(new File(fileName));
	}

	public FileLinesTextSourceCollection(File file) throws IOException {
		this.file = file;
		name = file.getName();
		namesToTextSource = new HashMap<String, TextSource>();
		textSourceToIndex = new HashMap<TextSource, Integer>();

		String[] fileLines = FileReadingUtil.readFileLines(new FileInputStream(file));

		textSources = new ArrayList<TextSource>(fileLines.length);
		textSourceNames = new ArrayList<String>(fileLines.length);

		for (int i = 0; i < fileLines.length; i++) {
			TextSource textSource = createTextSource(fileLines[i]);
			String name = textSource.getName();
			namesToTextSource.put(name, textSource);
			textSourceToIndex.put(textSource, i);

			textSources.add(textSource);
			textSourceNames.add(name);
		}
		// Collections.sort(textSourceNames);
	}

	protected TextSource createTextSource(String fileLine) {
		String name = fileLine.substring(0, fileLine.indexOf("|"));
		String text = fileLine.substring(name.length() + 1) + " ";
		return new FileLineTextSource(name, text);
	}

	public File getFile() {
		return this.file;
	}

	public TextSource get(int index) throws TextSourceAccessException {
		TextSource textSource = textSources.get(index);
		if (textSource != null)
			return textSources.get(index);
		throw new TextSourceAccessException("There is no text source at index=" + index
				+ " for text source collection = " + getName());
	}

	public int size() {
		return textSources.size();
	}

	public String getName() {
		return name;
	}

	public int getIndex(TextSource textSource) {
		return textSourceToIndex.get(textSource);
	}

	public TextSource get(String name) throws TextSourceAccessException {
		TextSource textSource = (TextSource) namesToTextSource.get(name);
		if (textSource != null)
			return textSource;
		throw new TextSourceAccessException("There is no text source for name='" + name
				+ "' for text source collection = " + getName());
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

	public void save(Project project) {
		String filePath = getFile().getPath();
		String projectPath = new File(project.getProjectDirectoryURI().getPath()).getPath();

		if (filePath.startsWith(projectPath)) {
			filePath = filePath.substring(projectPath.length() + 1);
		}
		project.setClientInformation(PROJECT_SETTING_RECENT_FILE, filePath);
	}

	public static TextSourceCollection open(Project project) {
		File recentFile = getRecentFile(project);
		if (recentFile != null) {
			try {
				TextSourceCollection tsc = new FileLinesTextSourceCollection(recentFile);
				return tsc;
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return null;
	}

	private static File getRecentFile(Project project) {
		String path = (String) project.getClientInformation(PROJECT_SETTING_RECENT_FILE);
		if (path == null)
			return null;

		File recentFile = new File(path);
		if (!recentFile.isAbsolute()) {
			// the path of the annotation project files
			String annotationProjectPath = new File(project.getProjectDirectoryURI()).getPath();
			String recentPath = annotationProjectPath + File.separator + path;
			recentFile = new File(recentPath);
			if (recentFile != null && recentFile.exists() && recentFile.isFile()) {
				return recentFile;
			} else
				recentFile = new File(path); // set it back to what it was
											 // before trying to resolve to
											 // annotation project path.
		}
		if (recentFile != null && recentFile.exists() && recentFile.isFile()) {
			return recentFile;
		}
		return null;
	}

	public static TextSourceCollection open(Project project, Component parent) {
		JFileChooser chooser = new JFileChooser();
		File recentFile = getRecentFile(project);
		if (recentFile != null) {
			chooser.setSelectedFile(recentFile);
		}
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = chooser.showOpenDialog(parent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			if (file.exists()) {
				try {
					return new FileLinesTextSourceCollection(file);
				} catch (IOException ioe) {
					ioe.printStackTrace();
					JOptionPane.showMessageDialog(parent, "Unable to open file as a text source collection: "
							+ ioe.getMessage(), "", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(parent, "The file: '" + file.getPath() + "' was not found.");
			}
		}
		return null;
	}

	public TextSourceIterator iterator() {
		return new FileLinesTextSourceIterator();
	}

	class FileLinesTextSourceIterator implements TextSourceIterator {
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

	public static void createCls(KnowledgeBase kb) {

		if (kb.getCls(CLS_NAME) == null) {
			Cls textSourceParent = kb.getCls(KnowtatorProjectUtil.TEXT_SOURCE_CLS_NAME);
			ArrayList parents = new ArrayList();
			parents.add(textSourceParent);
			kb.createCls(CLS_NAME, parents);
		}
		return;
	}

}
