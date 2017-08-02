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
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.event.TeamAnnotationSelectedEvent;
import edu.uchsc.ccp.knowtator.event.TeamAnnotationSelectedListener;

/**
 * Child of <code>AnnotationDetailsPanel</code> that will be shown directly
 *   to the right of the AnnotationDetailsPanel for side by side comparisons
 *   of the annotation attributes during consensus mode.
 *   
 * This was needed in order to register the correct listeners so both panels
 *   are updated with the correct annotations.
 * 
 * @author brantbarney 
 */
public class ConsensusAnnotationDetailsPanel extends AnnotationDetailsPanel 
										     implements TeamAnnotationSelectedListener {

	/** Auto-generated UID */
	private static final long serialVersionUID = -2047311242334840487L;

	/**
	 * Creates a new instance of <code>ConsensusAnnotationDetailsPanel</code>
	 * 
	 * @param kpu The Knowtator project utility instance
	 * @param manager The KnowtatorManager instance
	 * @param project The protege project
	 */
	public ConsensusAnnotationDetailsPanel( KnowtatorProjectUtil kpu, 
										    KnowtatorManager manager,
										    Project project ) {
		super(kpu, manager, project);
		
		//EventHandler.getInstance().addTeamAnnotationSelectedListener( this );
	}

	/**
	 * Overridden to return the <code>ConsensusAnnotationInstanceDisplay</code>
	 */
	@Override
	protected AnnotationInstanceDisplay createAnnotationInstanceDisplay() {
		return new ConsensusAnnotationInstanceDisplay(kpu, project, false, false);
	}

	/**
	 * Overridden to return the <code>ConsensusSlotAndSpanEditPanel</code>
	 */
	@Override
	protected SlotAndSpanEditPanel createSlotAndSpanEditPanel() {
		return new ConsensusSlotAndSpanEditPanel( manager, instanceDisplay, project );
	}
	
	public void teamAnnotationSelected(TeamAnnotationSelectedEvent evt) {
		textSourceInstanceDisplay.setEnabled( evt.getSelectedTeamAnno() == null );		
	}	
}
