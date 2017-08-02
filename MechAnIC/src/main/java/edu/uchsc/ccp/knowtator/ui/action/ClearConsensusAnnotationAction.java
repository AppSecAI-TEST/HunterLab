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
 *   Brant Barney (Original Author)
 */

package edu.uchsc.ccp.knowtator.ui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.event.FilterChangedEvent;
import edu.uchsc.ccp.knowtator.event.FilterChangedListener;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeEvent;
import edu.uchsc.ccp.knowtator.event.SelectedConsensusAnnotationChangeListener;

/**
 * Action to be called when accepting the annotation in the left most pane of the consensus
 *  comparison panel. 
 * 
 * @author brant
 */
public class ClearConsensusAnnotationAction extends AbstractAction 
											implements SelectedConsensusAnnotationChangeListener,
												       FilterChangedListener {

	/** */
	private static final long serialVersionUID = 1L;
	
	private KnowtatorManager manager;

	/**
	 * Creates a new instance of <code>AcceptAction</code>
	 */
	public ClearConsensusAnnotationAction( KnowtatorManager manager ) {	
		super( "clear right annotation" );
		
		this.manager = manager;
		
		putValue(SHORT_DESCRIPTION, "Clear the annotation on the right (without deleting it) so that no annotation appears there.  ");	
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
		
		EventHandler.getInstance().addFilterChangedListener( this );
		EventHandler.getInstance().addSelectedConsensusAnnotationChangeListener( this );
	}
	
	public void actionPerformed(ActionEvent evt ) {
		manager.setSelectedConsensusAnnotation(null);
	}

	public void consensusAnnotationSelectionChanged(SelectedAnnotationChangeEvent sace) {
		setEnabled( manager.isConsensusMode() && (sace.getSelectedAnnotation() != null) );
	}

	public void filterChanged(FilterChangedEvent event) {
		setEnabled( event.isConsensusMode() );
	}
}
