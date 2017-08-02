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

package edu.uchsc.ccp.knowtator.ui;

import edu.stanford.smi.protege.model.Project;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeEvent;
import edu.uchsc.ccp.knowtator.event.SelectedConsensusAnnotationChangeListener;

/**
 * Child of <code>AnnotationInstanceDisplay</code> used during consensus mode for a 
 *   side by side comparison of the <code>AnnotationInstanceDisplay</code>.
 *   
 * This is needed so the correct listeners are registered and it received the events
 *   for only the selected consensus annotation. 
 * 
 * @author brantbarney 
 */
public class ConsensusAnnotationInstanceDisplay extends AnnotationInstanceDisplay
               									implements SelectedConsensusAnnotationChangeListener {

	/** Auto-generated UID */
	private static final long serialVersionUID = 1701991693481241990L;

	/**
	 * Creates a new instance of <code>ConsensusAnnotationInstanceDisplay</code>
	 * 
	 * @param kpu
	 * @param project
	 * @param showHeader
	 * @param showHeaderLabel
	 */
	public ConsensusAnnotationInstanceDisplay(KnowtatorProjectUtil kpu,
			                                  Project project,
			                                  boolean showHeader,
			                                  boolean showHeaderLabel) {
		
		super(kpu, project, showHeader, showHeaderLabel);
	}

	@Override
	protected void registerListeners() {
		EventHandler.getInstance().addSelectedConsensusAnnotationChangeListener(this);			
	}

	//@Override
	public void consensusAnnotationSelectionChanged(SelectedAnnotationChangeEvent sace) {
		//TODO: Does anything special need to happen here?
		super.annotationSelectionChanged(sace);			
	}
}
