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
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.MentionUtil;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.event.FilterChangedEvent;
import edu.uchsc.ccp.knowtator.event.FilterChangedListener;
import edu.uchsc.ccp.knowtator.event.MultipleAnnotationSelectionChangeEvent;
import edu.uchsc.ccp.knowtator.event.MultipleAnnotationSelectionChangeListener;
import edu.uchsc.ccp.knowtator.event.RefreshAnnotationsDisplayListener;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeEvent;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeListener;
import edu.uchsc.ccp.knowtator.event.SelectedConsensusAnnotationChangeListener;
import edu.uchsc.ccp.knowtator.event.SlotMentionValueChangedListener;
import edu.uchsc.ccp.knowtator.event.SpanEditEvent;
import edu.uchsc.ccp.knowtator.event.SpanEditListener;

/**
 * Wrapper JPanel that contains 2 <code>AnnotationDetailsPanel</code>
 *   panels. This is needed in order to compare annotation details side by side
 *   in consensus mode. 
 * 
 * @author Brant Barney
 */
public class AnnotationComparisonPanel extends JPanel 
                                       implements MultipleAnnotationSelectionChangeListener,
                                       			  SelectedAnnotationChangeListener,
                                       			  SelectedConsensusAnnotationChangeListener,
                                                  FilterChangedListener,
                                                  SpanEditListener,
                                                  RefreshAnnotationsDisplayListener,
                                                  SlotMentionValueChangedListener {

	/** */
	private static final long serialVersionUID = 1L;
	
	private Logger logger = Logger.getLogger(AnnotationComparisonPanel.class);
	
	KnowtatorProjectUtil kpu;
	KnowtatorManager manager;
	Project project;

	/** Annotation details panel on the left side of the comparison for consensus mode.  */
	AnnotationDetailsPanel annoDetailsPanel;
	
	/** The right most panel showing the annotation details comparison for consensus mode. */
	ConsensusAnnotationDetailsPanel consensusAnnoDetailsPanel;
	
	JSplitPane splitPane;	

	/**
	 * Creates a new instance of <code>AnnotationComparisonPanel</code>
	 * 
	 * @param kpu
	 * @param manager
	 * @param project
	 */
	public AnnotationComparisonPanel( KnowtatorProjectUtil kpu,
			   					      KnowtatorManager manager,
			   					      Project project ) {

		this.kpu = kpu;
		this.manager = manager;
		this.project = project;
		
		initialize();
	}
	
	private void initialize() {
		annoDetailsPanel = new AnnotationDetailsPanel(kpu, manager, project);
		consensusAnnoDetailsPanel = new ConsensusAnnotationDetailsPanel(kpu, manager, project);
		
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, annoDetailsPanel, consensusAnnoDetailsPanel);
		splitPane.setOneTouchExpandable(true);
		
		setLayout(new BorderLayout());
		add(splitPane, BorderLayout.CENTER);
		
		EventHandler handler = EventHandler.getInstance();
		handler.addMultipleAnnotationSelectionChangeListener(this);
		handler.addFilterChangedListener(this);
		handler.addSelectedAnnotationChangeListener(this);
		handler.addSelectedConsensusAnnotationChangeListener(this);
		handler.addSpanEditListener(this);
		handler.addRefreshAnnotationsDisplayListener(this);
		handler.addSlotValueChangedListener(this);
	}

	public void multipleAnnotationSelectionChanged(MultipleAnnotationSelectionChangeEvent evt) {
		List<SimpleInstance> selectedAnnotations = evt.getSelectedAnnotations();
		if( selectedAnnotations.size() >= 2 ) {
			SimpleInstance selectedAnnotation = selectedAnnotations.get(0);
			SimpleInstance selectedConsensusAnnotation = selectedAnnotations.get(1);
			
			compareAndHighlightAnnotations(selectedAnnotation, selectedConsensusAnnotation);
		}			
	}
	
	private void compareAndHighlightAnnotations( SimpleInstance selectedAnnotation, 
											     SimpleInstance selectedConsensusAnnotation ) {
		
		AnnotationUtil annotationUtil = manager.getAnnotationUtil();
				
		List<SimpleInstance> selectedAnnotations = new ArrayList<SimpleInstance>();
		selectedAnnotations.add( selectedAnnotation );
		selectedAnnotations.add( selectedConsensusAnnotation );
		
		//Comparing spans
		//System.out.println( "Comparing Spans: " + annotationUtil.compareSpans(selectedAnnotations) );
		if( !annotationUtil.compareSpans(selectedAnnotations) ) {
			//The spans are not equal, so let's highlight the span widgets
			//System.out.println( "Found unequal spans, now highlighting the span widgets" );
			annoDetailsPanel.highlightSpanWidget();	
			consensusAnnoDetailsPanel.highlightSpanWidget();
		} else {
			annoDetailsPanel.resetSpanWidgetHighlight();
			consensusAnnoDetailsPanel.resetSpanWidgetHighlight();
		}
				
		//Comparing Slot Mentions		
		AnnotationUtil annoUtil = manager.getAnnotationUtil();
		SimpleInstance mention1 = annoUtil.getMention(selectedAnnotation);			
		SimpleInstance mention2 = annoUtil.getMention(selectedConsensusAnnotation);
		
		MentionUtil mentionUtil = manager.getMentionUtil();
		if( mentionUtil.equals(mention1, mention2, false, false) ) {
			//System.out.println( "Mentions are equal" );
			annoDetailsPanel.resetMentionDisplayHighlight();
			consensusAnnoDetailsPanel.resetMentionDisplayHighlight();
		} else {
			//System.out.println( "Mentions are not equal" );
			annoDetailsPanel.highlightMentionDisplay();
			consensusAnnoDetailsPanel.highlightMentionDisplay();
		}
		
		//Comparing Classes
		Cls cls1 = mentionUtil.getMentionCls(mention1);
		Cls cls2 = mentionUtil.getMentionCls(mention2);
				
		if( ((cls1 != null) && cls1.equals(cls2)) ||
			((cls1 == null) && (cls2 == null)) ) {
			annoDetailsPanel.resetMentionClsHighlight();
			consensusAnnoDetailsPanel.resetMentionClsHighlight();
		} else {
			//Classes were not equal, they must be highlighted in red
			annoDetailsPanel.highlightMentionCls();
			consensusAnnoDetailsPanel.highlightMentionCls();
		}		
	}

	public void annotationSelectionChanged(SelectedAnnotationChangeEvent sace) {
		compareSelectedAnnotations();
	}
		
	public void consensusAnnotationSelectionChanged(SelectedAnnotationChangeEvent sace) {
		annotationSelectionChanged(sace);
	}

	private void compareSelectedAnnotations() {
		SimpleInstance selectedAnnotation = manager.getSelectedAnnotation();
		SimpleInstance selectedConsensusAnnotation = manager.getSelectedConsensusAnnotation();
		
		if( (selectedAnnotation != null) && (selectedConsensusAnnotation != null) ) {
			compareAndHighlightAnnotations(selectedAnnotation, selectedConsensusAnnotation);
		} else if( manager.isConsensusMode() &&
				   ((selectedAnnotation != null) && (selectedConsensusAnnotation == null)) ) {
			annoDetailsPanel.resetMentionClsHighlight();
			annoDetailsPanel.resetMentionDisplayHighlight();
			annoDetailsPanel.resetSpanWidgetHighlight();
		}
	}

	public void spanEditted(SpanEditEvent see) {
		compareSelectedAnnotations();
	}

	public void refreshAnnotationsDisplay(boolean scrollToSelection) {
		compareSelectedAnnotations();		
	}
	
	public void slotMentionValueChanged() {
		compareSelectedAnnotations();
		
		if( manager.isRequiredMode() ) {
			if( manager.hasUnsetRequiredSlots( manager.getSelectedAnnotation() ) ) {
				annoDetailsPanel.highlightMentionDisplay();
			} else {
				annoDetailsPanel.resetMentionDisplayHighlight();
			}
		}
	}

	public void filterChanged( FilterChangedEvent event ) {
		if( event.isConsensusMode() ) {
			logger.debug( "Filter changed to consensus mode" );
			remove(annoDetailsPanel);
			
			splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, annoDetailsPanel, consensusAnnoDetailsPanel);
			splitPane.setOneTouchExpandable(true);			
			add(splitPane, BorderLayout.CENTER);			
		} else {
			logger.debug( "Filter changed out of consensus mode" );
			remove(splitPane);
			
			add(annoDetailsPanel, BorderLayout.CENTER);
		}
	}
}
