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
 * Copyright (C) 2005 - 2008.  All Rights Reserved.
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
 *   Brant Barney <brant.barney@hsc.utah.edu>
 */

package edu.uchsc.ccp.knowtator.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.ui.InstanceDisplay;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.widget.ClsWidget;
import edu.stanford.smi.protege.widget.FormWidget;
import edu.stanford.smi.protege.widget.SlotWidget;
import edu.uchsc.ccp.knowtator.InvalidSpanException;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.Span;
import edu.uchsc.ccp.knowtator.SpanWidget;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeEvent;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeListener;

public class AnnotationInstanceDisplay extends InstanceDisplay implements SelectedAnnotationChangeListener {
	
	/** Auto-generated UID */
	private static final long serialVersionUID = -2244801679905170357L;

	KnowtatorProjectUtil kpu;

	Project project;
	
	public AnnotationInstanceDisplay( KnowtatorProjectUtil kpu, 
						 			  Project project ) {
		super(project);
		this.kpu = kpu;
		this.project = project;
		
		registerListeners();
	}

	public AnnotationInstanceDisplay(KnowtatorProjectUtil kpu, 
								     Project project,
								     boolean showHeader,
								     boolean showHeaderLabel) {
		
		super(project, showHeader, showHeaderLabel);
		this.kpu = kpu;
		this.project = project;
		
		registerListeners();
	}
	
	/**
	 * Registers the necessary event listeners for this object.
	 * 
	 * This is designed to be overridden in a child class so they can listen for specific
	 *   events, such as the </code>SelectedConsensusAnnotationChangeEvent</code>
	 */
	protected void registerListeners() {
		EventHandler.getInstance().addSelectedAnnotationChangeListener(this);	
	}

	/**
	 * Returns a list Span objects corresponding to the span strings selected in
	 * the span widget If there are no spans selected, then the first span
	 * listed in the span widget is returned.
	 */
	public List<Span> getSelectedSpans() throws InvalidSpanException {
		List<Span> spans = new ArrayList<Span>();
		ClsWidget clsWidget = getFirstClsWidget(); // getCurrentClsWidget();
		SlotWidget slotWidget = clsWidget.getSlotWidget(kpu.getAnnotationSpanSlot());
		SpanWidget spanWidget = (SpanWidget) slotWidget;

		// spanWidget.addSelectionListener(listener)

		try {
			Collection<String> spanStrings = (Collection<String>) spanWidget.getSelection();
			for (String spanString : spanStrings) {
				Span span = Span.parseSpan(spanString);
				spans.add(span);
			}
			if (spans.size() == 0) {
				String spanString = (String) CollectionUtilities.getFirstItem(spanWidget.getValues());
				if (spanString != null) {
					Span span = Span.parseSpan(spanString);
					spans.add(span);
				}
			}
		} catch (ClassCastException cce) {
			throw new InvalidSpanException(cce.getMessage());
		}
		return spans;
	}

	public int[] getSelectedSpanIndices() throws InvalidSpanException {
		ClsWidget clsWidget = getFirstClsWidget();
		SlotWidget slotWidget = clsWidget.getSlotWidget(kpu.getAnnotationSpanSlot());
		SpanWidget spanWidget = (SpanWidget) slotWidget;
		return spanWidget.getSelectedIndices();
	}

	public void setSelectedSpanIndices(int[] selectedSpanIndices) {
		ClsWidget clsWidget = getFirstClsWidget();
		SlotWidget slotWidget = clsWidget.getSlotWidget(kpu.getAnnotationSpanSlot());
		SpanWidget spanWidget = (SpanWidget) slotWidget;
		spanWidget.setSelectedIndices(selectedSpanIndices);
	}

	public void annotationSelectionChanged(SelectedAnnotationChangeEvent sace) {
		SimpleInstance annotation = sace.getSelectedAnnotation();
		setInstance(annotation);			
	}
	
	/**
	 * Highlights the span widget with a red border
	 */
	public void highlightSpanWidget() {			
		FormWidget clsWidget = (FormWidget)getFirstClsWidget();
		if( clsWidget != null ) {
			clsWidget.highlightSlot( kpu.getAnnotationSpanSlot(), Color.red );
		}
	}
	
	/**
	 * Resets the span widget border to null, so no border is shown.
	 */
	public void resetSpanWidgetHighlight() {
		FormWidget clsWidget = (FormWidget)getFirstClsWidget();
		if( clsWidget != null ) {
			clsWidget.highlightSlot( kpu.getAnnotationSpanSlot(), null );
		}
	}
}
