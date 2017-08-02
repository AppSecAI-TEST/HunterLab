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

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.InstanceDisplay;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.TextSourceUtil;
import edu.uchsc.ccp.knowtator.textsource.TextSource;
import edu.uchsc.ccp.knowtator.textsource.TextSourceChangeEvent;
import edu.uchsc.ccp.knowtator.textsource.TextSourceChangeListener;

/**
 * Panel that contains all of the sub-panels showing all of the annotation
 *   details. It is shown at the right of the main Knowtator UI.
 * 
 * These sub panels include (from top to bottom):
 * 
 * <ol>
 * 		<li>SlotAndSpanEditPanel</li>
 * 	    <li>AnnotationInstanceDisplay</li>
 * 		<li>InstanceDisplay (for the text source comments)</li>
 * </ol>
 * 
 * Code was moved from Knowtator.java on May 13, 2009 in effort to duplicate
 *   this entire component for use in the new consensus mode feature.
 *   
 * @author Brant Barney 
 */
public class AnnotationDetailsPanel extends JPanel
                                    implements TextSourceChangeListener {

	/** Auto generated UID */
	private static final long serialVersionUID = 4141441804590207424L;
	
	/** UI component containing the text source comments. */
	protected InstanceDisplay textSourceInstanceDisplay;	
	
	protected Project project;	
	protected KnowtatorManager manager;	
	protected KnowtatorProjectUtil kpu;	
	private TextSourceUtil textSourceUtil;
	
	protected AnnotationInstanceDisplay instanceDisplay;
	protected SlotAndSpanEditPanel slotAndSpanEditPanel;
		
	/**
	 * Creates a new instance of <code>AnnotationDetailsPanel</code>
	 * 
	 * @param kpu The instance of KnowtatorProjectUtil
	 * @param manager The instance of KnowtatorManager
	 * @param project The current Knowtator project
	 */
	public AnnotationDetailsPanel( KnowtatorProjectUtil kpu,
								   KnowtatorManager manager,
			                       Project project) {
		this.kpu = kpu;
		this.manager = manager;
		this.project = project;		
		
		initialize();
	}
	
	/**
	 * Initializes all of the UI components
	 */
	private void initialize() {
		textSourceUtil = manager.getTextSourceUtil();			
		
		instanceDisplay = createAnnotationInstanceDisplay();
		textSourceInstanceDisplay = new InstanceDisplay(project, false, false);
		
		JSplitPane instanceDisplayAndTextSourceSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
				                                                          instanceDisplay, 
				                                                          textSourceInstanceDisplay);		
		instanceDisplayAndTextSourceSplitPane.setDividerLocation(400);
		instanceDisplayAndTextSourceSplitPane.setOneTouchExpandable(true);
		
		slotAndSpanEditPanel = createSlotAndSpanEditPanel();
		
		JSplitPane annoDetailsAndInstDispSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
				                                                    slotAndSpanEditPanel, 
				                                                    instanceDisplayAndTextSourceSplitPane);
		annoDetailsAndInstDispSplitPane.setDividerLocation(400);
		annoDetailsAndInstDispSplitPane.setOneTouchExpandable(true);
		
		setLayout( new BorderLayout() );
		add(annoDetailsAndInstDispSplitPane, BorderLayout.CENTER);
		
		textSourceUtil.addTextSourceChangeListener(this);
	}
	
	/**
	 * Creates the instance of <code>AnnotationInstanceDisplay</code> for use within
	 *   this panel. This is designed this way so it can be overridden in a child class
	 *   for customization and proper event listener registration
	 *   
	 * @return The <code>AnnotationInstanceDisplay</code> to be used in this panel
	 */
	protected AnnotationInstanceDisplay createAnnotationInstanceDisplay() {
		return new AnnotationInstanceDisplay(kpu, project, false, false);
	}
	
	/**
	 * Creates the instance of <code>SlotAndSpanEditPanel</code> for use within
	 *   this panel. This is designed this way so it can be overridden in a child class
	 *   for customization and proper event listener registration
	 * 	 
	 * @return The <code>SlotAndSpanEditPanel</code> instance to be used in this panel
	 */
	protected SlotAndSpanEditPanel createSlotAndSpanEditPanel() {
		return new SlotAndSpanEditPanel( manager, instanceDisplay, project );
	}
	
	/**
	 * Implementation of <code>TextSourceChangeListener</code>. Currently this listener
	 *   is registered in Knowtator.java, so it can be added at the correct time, and
	 *   cleaned up as well.
	 */
	public void textSourceChanged(TextSourceChangeEvent event) {
		TextSource currentTextSource = event.getTextSource();
		textSourceInstanceDisplay.setInstance(textSourceUtil.getTextSourceInstance(currentTextSource, false));
	}

	/**
	 * Highlights the span widget with a red border
	 */
	public void highlightSpanWidget() {
		instanceDisplay.highlightSpanWidget();
	}
	
	/**
	 * Resets the span widget border to null, so no border is shown.
	 */
	public void resetSpanWidgetHighlight() {
		instanceDisplay.resetSpanWidgetHighlight();
	}
	
	/**
	 * Highlights the slot mention display with a red border.
	 */
	public void highlightMentionDisplay() {
		slotAndSpanEditPanel.highlightMentionDisplay();
	}
	
	/**
	 * Resets the red border on the slot mention display (that was created from the 
	 *    <code>highlightMentionDisplay()</code> method.
	 */
	public void resetMentionDisplayHighlight() {
		slotAndSpanEditPanel.resetMentionDisplayHighlight();
	}
	
	/**
	 * Highlights the mention class with a red border.
	 */
	public void highlightMentionCls() {
		slotAndSpanEditPanel.highlightMentionCls();
	}
	
	/**
	 * Resets the red border on the mention class (that was created from the 
	 *    <code>highlightMentionCls()</code> method.
	 */
	public void resetMentionClsHighlight() {
		slotAndSpanEditPanel.resetMentionClsHighlight();
	}
}
