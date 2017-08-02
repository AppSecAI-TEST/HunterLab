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
import java.util.Collections;
import java.util.List;

import javax.swing.DefaultComboBoxModel;

import org.apache.log4j.Logger;

import edu.stanford.smi.protege.model.SimpleInstance;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.event.RefreshAnnotationsDisplayListener;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeEvent;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeListener;
import edu.uchsc.ccp.knowtator.event.SelectedConsensusAnnotationChangeListener;
import edu.uchsc.ccp.knowtator.textsource.TextSourceChangeEvent;
import edu.uchsc.ccp.knowtator.textsource.TextSourceChangeListener;

/**
 * Model containing data to be displayed in the annotation selection
 *   combo box in the SlotAndSpanEditPanel.
 * 
 * @author brantbarney 
 */
public class SelectedAnnotationComboBoxModel extends DefaultComboBoxModel 
											 implements TextSourceChangeListener,
											 	        SelectedAnnotationChangeListener,
											 	        SelectedConsensusAnnotationChangeListener,
											 	        RefreshAnnotationsDisplayListener {
		
	/** Auto-generated UID */
	private static final long serialVersionUID = -5517415141018030370L;

	/** List of annotations that will be displayed in the combo box. */
	protected List<SimpleInstance> annotationList;

	/** The currently selected annotation */
	protected SimpleInstance selectedAnnotation;	

	/** The KnowtatorManger instance. */
	protected KnowtatorManager manager;
	
	/** The logging instance. */
	private Logger logger = Logger.getLogger(SelectedAnnotationComboBoxModel.class);

	/**
	 * Creates a new instance of <code>SelectedAnnotationComboBoxModel</code>
	 * 
	 * @param manager The KnowtatorManger instance. 
	 */
	public SelectedAnnotationComboBoxModel(KnowtatorManager manager) {
		this.manager = manager;
		
		annotationList = new ArrayList<SimpleInstance>();

		//Kept here (instead of in registerListeners), as it applies to this as well as the child class
		manager.getTextSourceUtil().addTextSourceChangeListener(this);		    
		EventHandler.getInstance().addRefreshAnnotationsDisplayListener(this);
		
		registerListeners();
	}
	
	/**
	 * Registers the necessary listeners on this component.
	 * 
	 * Designed to be overridden in a child class to register the correct 
	 *   listeners for consensus mode.
	 */
	protected void registerListeners() {
		EventHandler.getInstance().addSelectedAnnotationChangeListener(this);
		EventHandler.getInstance().addSelectedConsensusAnnotationChangeListener(this);
	}

	@Override
	public void addElement(Object anObject) {
		annotationList.add((SimpleInstance) anObject);
		fireIntervalAdded(this, annotationList.size() - 1, annotationList.size() - 1);
		if (annotationList.size() == 1 && annotationList == null && anObject != null) {
			setSelectedItem(anObject);
		}
	}

	@Override
	public Object getElementAt(int index) {
		return annotationList.get(index);
	}

	@Override
	public int getSize() {
		return annotationList.size();
	}

	@Override
	public Object getSelectedItem() {
		return selectedAnnotation;
	} 

	@Override
	public int getIndexOf(Object anObject) {
		return annotationList.indexOf(anObject);
	}

	@Override
	public void setSelectedItem(Object anObject) {
		selectedAnnotation = (SimpleInstance) anObject;
		manager.setSelectedAnnotation((SimpleInstance) anObject);		

		fireContentsChanged(this, -1, -1);
	}

	@Override
	public void insertElementAt(Object anObject, int index) {
		if (anObject instanceof SimpleInstance) {
			annotationList.add(index, (SimpleInstance) anObject);
		} else {
			logger.debug("Cannot add object that is not of type SimpleInstance.");
		}
	}

	/**
	 * Used to update the annotation list when the text source is changed, as the annotation
	 *   list is dependent on the text source.
	 */
	public void textSourceChanged(TextSourceChangeEvent event) {
		annotationList = manager.getCurrentFilteredAnnotations();
		if (annotationList != null) {
			fireContentsChanged(this, 0, annotationList.size());
		} else {
			fireContentsChanged(this, -1, -1);
		}
	}

	/**
	 * Listens for annotation selection events and updates the selected item in the combo
	 *   box for the given annotation.
	 */
	public void annotationSelectionChanged(SelectedAnnotationChangeEvent sace) {
		if (selectedAnnotation != sace.getSelectedAnnotation()) {
			setSelectedItem(sace.getSelectedAnnotation());
		}
	}
	
	public void consensusAnnotationSelectionChanged(SelectedAnnotationChangeEvent sace) {
		SimpleInstance selectedAnno = sace.getSelectedAnnotation();
		if( selectedAnno != null ) {
			annotationList.remove( selectedAnno );
			
			fireContentsChanged(this, 0, annotationList.size());
		}
		refreshAnnotationsDisplay(true);
	}

	/**
	 * Implementation of RefreshAnnotationsDisplayListener used to refresh the selected
	 *   item based on the selected annotation in KnowtatorManager.
	 */
	public void refreshAnnotationsDisplay(boolean scrollToSelection) {
		annotationList = new ArrayList<SimpleInstance>( manager.getCurrentFilteredAnnotations() );
		if( selectedAnnotation != null ) {
			annotationList = filterByProximity( annotationList, selectedAnnotation, 5 );
		}
		
		SimpleInstance selectedConsensusAnno = manager.getSelectedConsensusAnnotation();
		if( selectedConsensusAnno != null ) {
			annotationList.remove( selectedConsensusAnno );
		}
		
		if (annotationList != null) {
			fireContentsChanged(this, 0, annotationList.size());
		} else {
			fireContentsChanged(this, -1, -1);
		}
				
		SimpleInstance tmpAnnotation = manager.getSelectedAnnotation();
		if( selectedAnnotation != tmpAnnotation ) {
			selectedAnnotation = tmpAnnotation;
			setSelectedItem(selectedAnnotation);
		}
	}
	
	protected List<SimpleInstance> filterByProximity( List<SimpleInstance> list, SimpleInstance selectedAnno, int proximityAnnotationCount ) {
		ArrayList<SimpleInstance> returnList = new ArrayList<SimpleInstance>();
		
		int selectedAnnotationIndex = list.indexOf( selectedAnno );
		
		if( selectedAnnotationIndex != -1 ) {
    		int count = 0;
    		
    		for( int i = selectedAnnotationIndex - 1; (count < proximityAnnotationCount) && (i >= 0); i--) {
    			returnList.add( list.get( i ) );
    			
    			count++;
    		}
    		
    		Collections.reverse( returnList );		
    		
    		count = 0;
    		
    		for( int i = selectedAnnotationIndex; (count < proximityAnnotationCount) && (i < list.size()); i++ ) {
    			returnList.add( list.get( i ) );
    			
    			count++;
    		}
		}
		
		return returnList;
	}
}