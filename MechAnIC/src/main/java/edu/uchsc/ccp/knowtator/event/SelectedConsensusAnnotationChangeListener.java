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

import java.util.EventListener;

import edu.uchsc.ccp.knowtator.KnowtatorManager;

/**
 * Listener used to catch events when the consensus annotation is selected. This is used while in consensus
 *   mode and two or more annotations are selected at the same time so they can be viewed side by side.
 *   
 * This one was added so the annotation details panels can receive different events. The first panel will use the
 *   regular <code>SelectedAnnotationChangeListener</code>, and the second panel (ConsensusAnnotationDetailsPanel) will
 *   use this listener.
 * 
 * @author brantbarney 
 */
public interface SelectedConsensusAnnotationChangeListener extends EventListener {
	/**
	 * To get the currently selected consensus annotation at any time call
	 * KnowtatorManager.getSelectedConsensusAnnotation().
	 * 
	 * @see KnowtatorManager#getSelectedConsensusAnnotation()
	 */
	public void consensusAnnotationSelectionChanged(SelectedAnnotationChangeEvent sace);
}
