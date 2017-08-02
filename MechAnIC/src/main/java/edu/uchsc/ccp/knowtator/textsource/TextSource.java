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

import java.util.List;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.uchsc.ccp.knowtator.Span;

/**
 * We recommend that instead of implementing this interface that you extend
 * DefaultTextSource. If you decide to implement this interface directly we
 * encourage you to look at the code for DefaultTextSource to help you.
 */

public interface TextSource {
	/**
	 * @return the text associated with the TextSource. The text returned will
	 *         be shown in the Knowtator text viewer.
	 */
	public String getText() throws TextSourceAccessException;

	/**
	 * @return the name associated with the TextSource. This should be thought
	 *         of as an identifier for the TextSource. Implementations of
	 *         TextSourceCollection should make sure that name collisions do not
	 *         occur.
	 */

	public String getName();

	/**
	 * Every implementation of TextSource corresponds to a subclass of
	 * "knowtator text source" which is a class defined knowtator.pprj (which is
	 * the project that must be included in order to run Knowtator.)
	 */

	public String getProtegeClsName();

	/**
	 * This is the generic method for creating a "knowtator text source"
	 * instance from a TextSource object. All instances of
	 * "knowtator text source" can be created in a very simplistic way from a
	 * TextSource object. An instance of the class specified by the name
	 * returned by getProtegeClsName() is created with the name returned by
	 * getName()
	 * 
	 * @return may return null if textSource.getProtegeClsName() is not a known
	 *         cls name. Otherwise the new Protege instance that has been
	 *         created.
	 */
	public Instance createTextSourceInstance(KnowledgeBase knowledgeBase);

	public int hashCode();

	public boolean equals(Object object);

	public String toString();

	/**
	 * Returns the text corresponding to the spans passed in. Typically, a
	 * single span will be passed in and the text corresponding to that text
	 * will be returned. If there are multiple spans, some extra decisions need
	 * to be made such as what the delimiter between spans will be and whether
	 * the spans should be sorted first.
	 */
	public String getText(List<Span> spans) throws TextSourceAccessException;

}
