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

import java.util.ArrayList;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.uchsc.ccp.knowtator.textsource.filelines.FileLineTextSource;

public class GeneRIFTextSource extends FileLineTextSource {

	public static final String CLS_NAME = "generifs text source";

	public static final String ENTREZ_GENE_SLOT_NAME = "generif_entrez_gene";

	public static final String PMID_SLOT_NAME = "generif_pmid";

	int lineNumber;

	String[] entrezIDs;

	String pmid;

	public GeneRIFTextSource(String id, String[] entrezIDs, String pmid, String text) {
		this.name = id;
		this.entrezIDs = entrezIDs;
		this.pmid = pmid;
		this.text = text;
		this.protegeClsName = CLS_NAME;
	}

	public Instance createTextSourceInstance(KnowledgeBase knowledgeBase) {
		Cls textSourceCls = knowledgeBase.getCls(getProtegeClsName());

		Instance textSourceInstance = knowledgeBase.createInstance(getName(), textSourceCls, true);

		ArrayList entrezGeneInstances = new ArrayList();

		for (int i = 0; i < entrezIDs.length; i++) {

			Instance entrezGeneInstance = knowledgeBase.getInstance(entrezIDs[i]);
			entrezGeneInstances.add(entrezGeneInstance);
		}

		textSourceInstance.setOwnSlotValues(knowledgeBase.getSlot(ENTREZ_GENE_SLOT_NAME), entrezGeneInstances);
		textSourceInstance.setOwnSlotValue(knowledgeBase.getSlot(PMID_SLOT_NAME), pmid);
		return textSourceInstance;
	}

	public String[] getEntrezIDs() {
		return this.entrezIDs;
	}

}
