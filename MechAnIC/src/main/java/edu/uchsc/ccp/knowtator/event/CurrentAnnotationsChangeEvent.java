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
 * Copyright (C) 2005-2008.  All Rights Reserved.
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
package edu.uchsc.ccp.knowtator.event;

import java.util.EventObject;
import java.util.List;

import edu.stanford.smi.protege.model.SimpleInstance;

public class CurrentAnnotationsChangeEvent extends EventObject {
	static final long serialVersionUID = 0;

	List<SimpleInstance> filteredAnnotations;

	List<SimpleInstance> partiallyFilteredAnnotations;

	/**
	 * @param source
	 *            will typically be an instance of KnowtatorManager
	 * @param filteredAnnotations
	 *            annotations that pass through the currently selected filter
	 * @param partiallyFilteredAnnotations
	 *            annotations that pass through the currently selected filter
	 *            where the type constraint of the filter is ignored.
	 */
	public CurrentAnnotationsChangeEvent(Object source, List<SimpleInstance> filteredAnnotations,
			List<SimpleInstance> partiallyFilteredAnnotations) {
		super(source);
		this.filteredAnnotations = filteredAnnotations;
		this.partiallyFilteredAnnotations = partiallyFilteredAnnotations;
	}

	public List<SimpleInstance> getFilteredAnnotations() {
		return this.filteredAnnotations;
	}

	public List<SimpleInstance> getPartiallyFilteredAnnotations() {
		return partiallyFilteredAnnotations;
	}
}
