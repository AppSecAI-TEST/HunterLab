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
package edu.uchsc.ccp.knowtator.textsource.generif;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.uchsc.ccp.knowtator.textsource.TextSource;
import edu.uchsc.ccp.knowtator.textsource.TextSourceCollection;
import edu.uchsc.ccp.knowtator.textsource.filelines.FileLinesTextSourceCollection;

/**
 * This TextSourceCollection implementation is useful for GeneRIFs stored in in
 * a single file line-by-line. The format of the file should be:
 * <code><br>id|gene_name|gene_symbol|text<br></code> Where id is some local
 * identifier (not provided by entrez) assigned to the geneRIF.
 */

public class GeneRIFTextSourceCollection extends FileLinesTextSourceCollection {
	public static final String DISPLAY_NAME = "GeneRIFs";

	public static final String CLS_NAME = "generifs text source";

	public static final String PROJECT_SETTING_RECENT_FILE = new String("GRIFTextSourceCollection_RECENT_FILE");

	public GeneRIFTextSourceCollection(String fileName) throws IOException {
		super(fileName);
	}

	public GeneRIFTextSourceCollection(File file) throws IOException {
		super(file);
	}

	protected TextSource createTextSource(String fileLine) {
		String id = fileLine.substring(0, fileLine.indexOf("|"));
		int entrezIdStart = id.length() + 1;
		String entrezId = fileLine.substring(entrezIdStart, fileLine.indexOf("|", entrezIdStart));
		String[] entrezIDs = entrezId.split(":");
		int pmidStart = entrezIdStart + entrezId.length() + 1;
		String pmid = fileLine.substring(pmidStart, fileLine.indexOf("|", pmidStart));
		int textStart = pmidStart + pmid.length() + 1;
		String text = fileLine.substring(textStart);

		return new GeneRIFTextSource(id, entrezIDs, pmid, text);
	}

	private static File getRecentFile(Project project) {
		String recentFileName = (String) project.getClientInformation(PROJECT_SETTING_RECENT_FILE);
		if (recentFileName != null) {
			File recentFile = new File(recentFileName);
			if (recentFile != null && recentFile.exists() && recentFile.isFile()) {
				return recentFile;
			}
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
			try {
				return new GeneRIFTextSourceCollection(file);
			} catch (IOException ioe) {
				ioe.printStackTrace();
				JOptionPane.showMessageDialog(parent, "Unable to open file as a text source collection", "",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		return null;
	}

	public void save(Project project) {
		project.setClientInformation(PROJECT_SETTING_RECENT_FILE, getFile().getPath());
	}

	public static TextSourceCollection open(Project project) {
		File recentFile = getRecentFile(project);
		if (recentFile != null) {
			try {
				TextSourceCollection tsc = new GeneRIFTextSourceCollection(recentFile);
				return tsc;
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
		return null;
	}

	public static void createCls(KnowledgeBase kb) {

		if (kb.getCls(CLS_NAME) == null) {
			Cls textSourceParent = kb.getCls(FileLinesTextSourceCollection.CLS_NAME);
			List<Cls> parents = new ArrayList<Cls>();
			parents.add(textSourceParent);
			Cls grifTextSourceCls = kb.createCls(CLS_NAME, parents);
			Slot pmidSlot = kb.createSlot("generif_pmid");
			pmidSlot.setValueType(ValueType.STRING);
			grifTextSourceCls.addDirectTemplateSlot(pmidSlot);

			Cls entrezGeneCls = kb.getCls("entrez_gene");
			if (entrezGeneCls == null) {
				entrezGeneCls = kb.createCls("entrez_gene", CollectionUtilities.createCollection(kb.getRootCls()));
				Slot entrezAliasSlot = kb.createSlot("entrez_alias");
				entrezAliasSlot.setAllowsMultipleValues(true);
				entrezAliasSlot.setValueType(ValueType.STRING);
				entrezGeneCls.addDirectTemplateSlot(entrezAliasSlot);
				Slot entrezGeneIDSlot = kb.createSlot("entrez_gene_id");
				entrezGeneIDSlot.setAllowsMultipleValues(false);
				entrezGeneIDSlot.setMinimumCardinality(1);
				entrezGeneIDSlot.setValueType(ValueType.STRING);
				entrezGeneCls.addDirectTemplateSlot(entrezGeneIDSlot);
				Slot entrezOfficialNameSlot = kb.createSlot("entrez_official_name");
				entrezOfficialNameSlot.setAllowsMultipleValues(false);
				entrezOfficialNameSlot.setValueType(ValueType.STRING);
				entrezGeneCls.addDirectTemplateSlot(entrezOfficialNameSlot);
				Slot entrezOfficialSymbolSlot = kb.createSlot("entrez_official_symbol");
				entrezOfficialSymbolSlot.setAllowsMultipleValues(false);
				entrezOfficialSymbolSlot.setValueType(ValueType.STRING);
				entrezGeneCls.addDirectTemplateSlot(entrezOfficialSymbolSlot);
				Slot entrezSpeciesSlot = kb.createSlot("entrez_species");
				entrezSpeciesSlot.setAllowsMultipleValues(false);
				entrezSpeciesSlot.setValueType(ValueType.STRING);
				entrezGeneCls.addDirectTemplateSlot(entrezSpeciesSlot);
			}
			Slot entrezGeneSlot = kb.createSlot("generif_entrez_gene");
			entrezGeneSlot.setValueType(ValueType.INSTANCE);
			entrezGeneSlot.setAllowedClses(CollectionUtilities.createCollection(entrezGeneCls));
			entrezGeneSlot.setAllowsMultipleValues(true);
			grifTextSourceCls.addDirectTemplateSlot(entrezGeneSlot);

		}
	}
}
