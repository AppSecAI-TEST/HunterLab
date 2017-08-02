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

package edu.uchsc.ccp.knowtator.ui;

import javax.swing.JComboBox;

import edu.stanford.smi.protege.model.SimpleInstance;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeEvent;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeListener;
import edu.uchsc.ccp.knowtator.ui.model.SelectedAnnotationComboBoxModel;
import edu.uchsc.ccp.knowtator.ui.renderer.SelectedAnnotationComboBoxRenderer;

/**
 * Combo box used to show a list of annotations the user can select from.
 * 
 * @author brant 
 */
public class SelectedAnnotationComboBox extends JComboBox 
										implements SelectedAnnotationChangeListener {
	
	/**	Auto generated UID */
	private static final long serialVersionUID = -4174618308760933999L;
	
	protected KnowtatorManager manager;

	/**
	 * Creates a new instance of <code>SelectedAnnotationComboBox</code>
	 * 
	 * @param manager The KnowtatorManager instance
	 */
	public SelectedAnnotationComboBox( KnowtatorManager manager ) {
		super( new SelectedAnnotationComboBoxModel( manager ) );
		
		this.manager = manager;
		
		setRenderer( new SelectedAnnotationComboBoxRenderer( manager ) );
		
		registerListeners();
	}
	
	/**
	 * Registers the necessary listeners. Intended to be overridden in a child class.
	 */
	protected void registerListeners() {
		EventHandler.getInstance().addSelectedAnnotationChangeListener( this );
	}
	
	/**
	 * Concrete implementation of SelectedAnnotationChangeListener
	 */
	public void annotationSelectionChanged(SelectedAnnotationChangeEvent sace) {
		SimpleInstance selectedAnnotation = sace.getSelectedAnnotation();
		String text = manager.getBrowserTextUtil().getBrowserText( selectedAnnotation );
		setToolTipText( text );		
	}
}
