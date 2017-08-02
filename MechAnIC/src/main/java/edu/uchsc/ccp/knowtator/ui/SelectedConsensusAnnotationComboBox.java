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

import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeEvent;
import edu.uchsc.ccp.knowtator.event.SelectedConsensusAnnotationChangeListener;
import edu.uchsc.ccp.knowtator.ui.model.SelectedConsensusAnnotationComboBoxModel;

/**
 * Child of <code>SelectedAnnotationComboBox</code> used to register the correct models
 *   and listeners for the comparison consensus annotation.
 * 
 * @author brant
 */
public class SelectedConsensusAnnotationComboBox extends SelectedAnnotationComboBox 
                                                 implements SelectedConsensusAnnotationChangeListener {

	/** Auto-generated UID */
	private static final long serialVersionUID = 3671154758091587865L;

	/**
	 * Creates a new instance of <code>SelectedConsensusAnnotationComboBox</code>
	 * 
	 * @param manager
	 */
	public SelectedConsensusAnnotationComboBox(KnowtatorManager manager) {
		super(manager);
		
		setModel( new SelectedConsensusAnnotationComboBoxModel( manager ) );
	}

	@Override
	protected void registerListeners() {
		EventHandler.getInstance().addSelectedConsensusAnnotationChangeListener( this );
	}

	public void consensusAnnotationSelectionChanged(SelectedAnnotationChangeEvent sace) {
		super.annotationSelectionChanged(sace);		
	}
}
