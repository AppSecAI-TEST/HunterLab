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

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import edu.stanford.smi.protege.model.SimpleInstance;
import edu.uchsc.ccp.knowtator.KnowtatorManager;

/**
 * Action designed to automatically select the next annotation that contains
 *   at least one required slot that does not currently have a value.
 *   
 * Used only when in required mode.
 * 
 * @author brant 
 */
@SuppressWarnings("serial")
public class RequiredModeNextAction extends AbstractAction {
	
	KnowtatorManager manager;
	
	public RequiredModeNextAction( KnowtatorManager manager ) {
		super( "Next Required Annotation" );
		
		this.manager = manager;
		
		putValue(SHORT_DESCRIPTION, "Advance to the next annotation with unset required slots");	
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));		
		
		setEnabled( false );
	}
	
	public void actionPerformed( ActionEvent evt ) {
		SimpleInstance nextAnnotation = manager.getNextRequiredAnnotation();
		manager.setSelectedAnnotation( nextAnnotation );		
	}
}
