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

import javax.swing.Action;
import javax.swing.JComboBox;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeEvent;
import edu.uchsc.ccp.knowtator.event.TeamAnnotationSelectedEvent;
import edu.uchsc.ccp.knowtator.event.TeamAnnotationSelectedListener;
import edu.uchsc.ccp.knowtator.exception.ActionNotFoundException;
import edu.uchsc.ccp.knowtator.ui.action.ActionFactory;

/**
 * Child of <code>SlotAndSpanEditPanel</code> used to show a side by side comparison
 *   during consensus mode of the slot and span edit panels.
 *   
 * Needed so the proper consensus annotation components are created, and the consensus
 *   annotation events are handled.
 * 
 * @author brantbarney
 */
public class ConsensusSlotAndSpanEditPanel extends SlotAndSpanEditPanel
										   implements TeamAnnotationSelectedListener {

	/**	Auto-generated UID */
	private static final long serialVersionUID = 1557544898858004522L;

	/**
	 * Creates a new instance of <code>ConsensusSlotAndSpanEditPanel</code>
	 * 
	 * @param manager
	 * @param annotationDisplay
	 * @param project
	 */
	public ConsensusSlotAndSpanEditPanel( KnowtatorManager manager,
										  AnnotationInstanceDisplay annotationDisplay,
										  Project project) {
		
		super(manager, annotationDisplay, project);
		
		//EventHandler.getInstance().addTeamAnnotationSelectedListener( this );
	}

	/**
	 * Overridden to return an instance of <code>ConsensusMentionDisplay</code> so
	 *   the proper listeners are registered to show the correct mentions during
	 *   the consensus mode comparison.
	 */
	@Override
	protected MentionDisplay createMentionDisplay() {
		return new ConsensusMentionDisplay(manager, project);
	}

	/**
	 * Overridden to use the <code>SelectedConsensusAnnotationComboBox</code>, so the proper
	 *   listeners are registered.
	 */
	@Override
	protected JComboBox createComboBox() {	
		return new SelectedConsensusAnnotationComboBox(manager);
	}

	/**
	 * Overridden to return the <code>AcceptConsensusAction</code>
	 */
	@Override
	protected Action createAcceptAction() {
		try {
			return ActionFactory.getInstance( manager ).getAction( ActionFactory.ACTION_ACCEPT_CONSENSUS_ANNOTATION );
		} catch (ActionNotFoundException e) {
			// Should probably handle this exception better
			e.printStackTrace();
			return null;
		}
	}	
	
	protected Action createClearAnnotationAction() {
		try {
			return ActionFactory.getInstance( manager ).getAction( ActionFactory.ACTION_CLEAR_CONSENSUS_ANNOTATION );
		} catch (ActionNotFoundException e) {
			// Should probably handle this exception better
			e.printStackTrace();
			return null;
		}
	}	

	
	@Override
	protected Action createDeleteAction() {
		try {
			return ActionFactory.getInstance( manager ).getAction( ActionFactory.ACTION_DELETE_CONSENSUS_ANNOTATION );
		} catch( ActionNotFoundException e ) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void annotationSelectionChanged(SelectedAnnotationChangeEvent sace) {				
		SimpleInstance selectedAnnotation = manager.getSelectedConsensusAnnotation();
	
		try {
			Action acceptAnnotationAction = ActionFactory.getInstance( manager ).getAction( ActionFactory.ACTION_ACCEPT_CONSENSUS_ANNOTATION );
						
			acceptAnnotationAction.setEnabled( manager.isConsensusMode() && (selectedAnnotation != null)  );
			
		} catch ( ActionNotFoundException e ) {
			e.printStackTrace();
		}		
		
		showConsensusIcon(selectedAnnotation);
	}

	public void teamAnnotationSelected(TeamAnnotationSelectedEvent evt) {
		if( evt.getSelectedTeamAnno() != null ) {
			disableAllComponents();
		} else {
			enableAllComponents();
		}
	}
}
