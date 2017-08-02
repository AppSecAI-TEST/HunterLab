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
 * Copyright (C) 2005-2008.  All Rights Reserved.
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
 *   Philip V. Ogren <philip@ogren.info> (Original Author)
 *   Brant Barney
 */
package edu.uchsc.ccp.knowtator.widget;

import java.util.Collection;

import javax.swing.Action;
import javax.swing.JList;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.LabeledComponent;
import edu.stanford.smi.protege.widget.ClsFieldWidget;
import edu.uchsc.ccp.knowtator.DisplayColors;
import edu.uchsc.ccp.knowtator.Knowtator;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.ui.ColorFrameRenderer;

public class AnnotatedClassWidget extends ClsFieldWidget {
	static final long serialVersionUID = 0;

	public JList createList() {
		KnowtatorManager manager = (KnowtatorManager) getKnowledgeBase().getClientInformation(
				Knowtator.KNOWTATOR_MANAGER);
		ColorFrameRenderer renderer = manager.getRenderer();
		JList list = ComponentFactory.createSingleItemList(getDoubleClickAction());
		list.setCellRenderer(renderer);
		return list;
	}

	@Override
	protected void addButtons(LabeledComponent c) {
		//Only want to show the select button, the remove and view buttons should not be shown
		addButton(c, getSelectClsAction());
	}
		
	@Override
	public void valueChanged() {
		super.valueChanged();

		KnowtatorManager manager = (KnowtatorManager) getKnowledgeBase().getClientInformation(
				Knowtator.KNOWTATOR_MANAGER);
		DisplayColors displayColors = manager.getDisplayColors();
		displayColors.updateColors();
		
		//Need to fire an event to refresh the display. This allows the red border to highlight
		//  the annotation class when the class is changed.
		EventHandler handler = EventHandler.getInstance();
		handler.fireRefreshAnnotationsDisplay(false);
	}	
}
