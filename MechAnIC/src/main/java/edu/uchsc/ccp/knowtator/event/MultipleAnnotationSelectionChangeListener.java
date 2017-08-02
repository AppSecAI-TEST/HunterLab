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
 * Copyright (C) 2005-2009.  All Rights Reserved.
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
 *   Brant Barney <brant.barney@hsc.utah.edu> (Original Author)
 */

package edu.uchsc.ccp.knowtator.event;

/**
 * Interface used to listen on <code>MultipleAnnotationSelectionChangeEvent</code>'s.
 * 
 * This event will be thrown whenever 2 or more annotations are selected at the same time. This
 *  can be essential for side-by-side comparisons between annotations, such as in consensus
 *  mode.
 * 
 * @author brantbarney 
 */
public interface MultipleAnnotationSelectionChangeListener {

	/**
	 * Called whenever 2 or more annotations are selected at the same time
	 * 
	 * @param evt The event object containing the list of annotations that were selected.
	 */
	public void multipleAnnotationSelectionChanged( MultipleAnnotationSelectionChangeEvent evt );
}
