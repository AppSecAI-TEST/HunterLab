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

import java.util.EventObject;
import java.util.List;

import edu.stanford.smi.protege.model.SimpleInstance;

/**
 * This is an event that occurs when multiple annotations are selected at the
 *   same time. This is useful for the side-by-side comparison in consensus 
 *   mode.
 * 
 * @author brantbarney 
 */
public class MultipleAnnotationSelectionChangeEvent extends EventObject {
	
	/** Auto-generated UID */
	private static final long serialVersionUID = -4181089976475778388L;
	
	/** The list of annotations that were selected. */
	private List<SimpleInstance> selectedAnnotationsList;
	
	/**
	 * Creates a new instance of <code>MultipleAnnotationSelectionChangeEvent</code>
	 * 
	 * @param source
	 * @param selectedAnnotations
	 */
	public MultipleAnnotationSelectionChangeEvent(Object source,
			                                     List<SimpleInstance> selectedAnnotations) {
		super(source);
		this.selectedAnnotationsList = selectedAnnotations;		
	}
	
	/**
	 * Gets the list of selected annotations for this event
	 * 
	 * @return A List of all annotations selected as part of this event.
	 */
	public List<SimpleInstance> getSelectedAnnotations() {
		return selectedAnnotationsList;
	}
}
