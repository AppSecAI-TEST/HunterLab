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

package edu.uchsc.ccp.knowtator.ui.renderer;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;

import edu.stanford.smi.protege.model.SimpleInstance;
import edu.uchsc.ccp.knowtator.BrowserTextUtil;
import edu.uchsc.ccp.knowtator.DisplayColors;
import edu.uchsc.ccp.knowtator.KnowtatorManager;

/**
 * Custom renderer used to customize the display of the annotation selection
 *   combo box. It sets the proper text (via browserTextUtil), and adds an icon 
 *   to the renderer label. 
 *   
 * @author brantbarney 
 */
public class SelectedAnnotationComboBoxRenderer extends DefaultListCellRenderer {

	/** Auto-generated UID */
	private static final long serialVersionUID = -5744906526479256691L;
	
	private BrowserTextUtil browserTextUtil;	
	private DisplayColors displayColors;
	
	/**
	 * Creates a new instance of <code>SelectedAnnotationComboBoxRenderer</code>
	 * 
	 * @param manager The KnowtatorManger instance
	 */
	public SelectedAnnotationComboBoxRenderer(KnowtatorManager manager) {
		displayColors = manager.getDisplayColors();
		browserTextUtil = manager.getBrowserTextUtil();
	}

	@Override
	public Component getListCellRendererComponent( JList list,
												   Object value,
												   int index, 
												   boolean isSelected,
												   boolean cellHasFocus ) {
		
		JLabel label = (JLabel)super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		String text = browserTextUtil.getBrowserText( (SimpleInstance)value, 50);
		String toolTipText = browserTextUtil.getBrowserText( (SimpleInstance)value );
		
		if( "".equals(text ) ) {
			String noAnnoLabel = "no annotation selected"; 
			text = noAnnoLabel;
			toolTipText = noAnnoLabel;
		}
		
		label.setText( text );
		label.setIcon(displayColors.getIcon(displayColors.getColor((SimpleInstance)value)));			
		label.setToolTipText( toolTipText );
		
		return label;
	}
}