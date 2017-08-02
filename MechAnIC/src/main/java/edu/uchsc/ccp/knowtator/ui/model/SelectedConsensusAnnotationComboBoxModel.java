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
 * Copyright (C) 2005 - 2009.  All Rights Reserved.
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
 *   Brant Barney <brant.barney@hsc.utah.edu>
 */

package edu.uchsc.ccp.knowtator.ui.model;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.stanford.smi.protege.model.SimpleInstance;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeEvent;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeListener;
import edu.uchsc.ccp.knowtator.event.SelectedConsensusAnnotationChangeListener;

/**
 * Model containing the data to be shown in the annotation selection combo box
 *   in the consensus mode slot and span edit panel.
 *   
 * Extends from <code>SelectedAnnotationComboBoxModel</code> to override methods
 *   to provide specific implementations for the consensus mode panel.
 *  
 * @author brantbarney 
 */
public class SelectedConsensusAnnotationComboBoxModel extends SelectedAnnotationComboBoxModel
                                                      implements SelectedConsensusAnnotationChangeListener,
                                                      			 SelectedAnnotationChangeListener {
	
	/** Auto-generated UID */
	private static final long serialVersionUID = 1936124798950091519L;
	
	/** The logging instance. */
	private Logger logger = Logger.getLogger(SelectedConsensusAnnotationComboBoxModel.class);

	/**
	 * Creates a new instance of <code>SelectedConsensusAnnotationComboBoxModel</code>
	 * 
	 * @param manager The KnowtatorManager instance
	 */
	public SelectedConsensusAnnotationComboBoxModel(KnowtatorManager manager) {
		super(manager);		
	}

	/**
	 * Overridden to register the <code>SelectedConsensusAnnotationChangeListener</code>
	 */
	@Override
	protected void registerListeners() {
		EventHandler.getInstance().addSelectedConsensusAnnotationChangeListener(this);
		EventHandler.getInstance().addSelectedAnnotationChangeListener( this );
	}
	
	/**
	 * Overridden to set the selected <i>consensus</i> annotation
	 */
	@Override
	public void setSelectedItem(Object anObject) {
		selectedAnnotation = (SimpleInstance) anObject;
		manager.setSelectedConsensusAnnotation((SimpleInstance) anObject);

		fireContentsChanged(this, -1, -1);
	}

	@Override
	public void refreshAnnotationsDisplay(boolean scrollToSelection) {
		annotationList = new ArrayList<SimpleInstance>( manager.getCurrentFilteredAnnotations() );			
		
		//annotationList = manager.filterTeamAnnotations( annotationList );
		if( manager.getSelectedAnnotation() != null ) {
			annotationList = filterByProximity( annotationList, manager.getSelectedAnnotation(), 5 );
		}
		
		//Make sure the selected annotation in the left pane does is not displayed in the right pane (consensus mode pane)
		SimpleInstance selectedAnno = manager.getSelectedAnnotation();
		if( selectedAnno != null ) {
			annotationList.remove( selectedAnno );
		}
		
		if (annotationList != null) {
			fireContentsChanged(this, 0, annotationList.size());
		} else {
			fireContentsChanged(this, -1, -1);
		}
		
		logger.info( "Refreshing selected consensus annotation" );
		SimpleInstance tmpAnnotation = manager.getSelectedConsensusAnnotation();
		if ( selectedAnnotation != tmpAnnotation ) {
			selectedAnnotation = tmpAnnotation;		
			setSelectedItem(selectedAnnotation);
		}
	}

	@Override
	public void annotationSelectionChanged(SelectedAnnotationChangeEvent sace) {
		SimpleInstance selectedAnno = sace.getSelectedAnnotation();	
		if( selectedAnno != null ) {
			annotationList.remove( selectedAnno );
			
			fireContentsChanged(this, 0, annotationList.size());
		}
	}

	public void consensusAnnotationSelectionChanged(SelectedAnnotationChangeEvent sace) {
		super.annotationSelectionChanged(sace);
	}
}
