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

package edu.uchsc.ccp.knowtator.textsource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.uchsc.ccp.knowtator.Span;

public class DefaultTextSource implements TextSource {

	public static final String CLS_NAME = "knowtator text source";

	protected String text;

	protected String name;

	protected String protegeClsName;

	/** Creates a new instance of TextSource */
	public DefaultTextSource() {
	}

	public DefaultTextSource(String name, String text) {
		this.text = text;
		this.name = name;
		this.protegeClsName = CLS_NAME;
	}

	public DefaultTextSource(String name, String text, String protegeClsName) {
		this.text = text;
		this.name = name;
		this.protegeClsName = protegeClsName;
	}

	/**
	 * It might be prudent for extensions of this class to override this method
	 * such that the member variable text is not used - esp. if the texts are
	 * large and there are many of them.
	 * 
	 */
	public String getText() throws TextSourceAccessException {
		return text;
	}

	public String getName() {
		return name;
	}

	public String getProtegeClsName() {
		return this.protegeClsName;
	}

	public Instance createTextSourceInstance(KnowledgeBase knowledgeBase) {
		Cls textSourceCls = knowledgeBase.getCls(getProtegeClsName());

		Instance textSourceInstance = knowledgeBase.createInstance(getName(), textSourceCls, true);
		return textSourceInstance;
	}

	public int hashCode() {
		return (text + "|" + name).hashCode();
	}

	public boolean equals(Object object) {
		if (object instanceof DefaultTextSource) {
			DefaultTextSource ts = (DefaultTextSource) object;
			try {
				if (name.equals(ts.getName()) && text.equals(ts.getText()))
					return true;
			} catch (Exception ex) {
				return false;
			}
		}

		return false;
	}

	public String toString() {
		return name;
	}

	public String getText(List<Span> annotationSpans) throws TextSourceAccessException {
		return getText(annotationSpans, getText());
	}

	public static String getText(List<Span> annotationSpans, String text) throws TextSourceAccessException {
		StringBuffer spanText = new StringBuffer();

		List<Span> spans = new ArrayList<Span>(annotationSpans);
		Collections.sort(spans);

		if (spans.size() > 0) {
			spanText.append(text.substring(Math.max(0, spans.get(0).getStart()), Math.min(text.length(), spans.get(0)
					.getEnd())));
		}
		if (spans.size() == 1)
			return spanText.toString();

		for (int i = 1; i < spans.size(); i++) {
			spanText.append(" ... ");
			spanText.append(text.substring(Math.max(0, spans.get(i).getStart()), Math.min(text.length(), spans.get(i)
					.getEnd())));
		}
		return spanText.toString();
	}

}
