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

import edu.stanford.smi.protege.model.SimpleInstance;

/**
 * Event used when the consensus annotation is selected for a side by side comparison
 *  in consensus mode.
 * 
 * @author brantbarney 
 */
public class SelectedConsensusAnnotationChangeEvent extends SelectedAnnotationChangeEvent {

	/** Default UID */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance of <code>SelectedConsensusAnnotationChangeEvent</code>
	 * 
	 * @param source The source of the event, likely will be an instance of KnowtatorManager
	 * @param selectedAnnotation The annotation that was selected, and will be displayed
	 *         in the consensus annotation details panel
	 */
	public SelectedConsensusAnnotationChangeEvent(Object source, SimpleInstance selectedAnnotation) {
		super(source, selectedAnnotation);	
	}
}