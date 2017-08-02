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
 *   Brant Barney <brant.barney@hsc.utah.edu>
 */
package edu.uchsc.ccp.knowtator.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.BrowserTextUtil;
import edu.uchsc.ccp.knowtator.FilterUtil;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.MentionUtil;
import edu.uchsc.ccp.knowtator.Span;
import edu.uchsc.ccp.knowtator.TextSourceUtil;

public class EventHandler {
	List<SelectedSpanChangeListener> selectedSpanChangeListeners;

	List<SelectedAnnotationChangeListener> selectedAnnotationChangeListeners;
	
	/** Collection containing the listeners for the selected consensus annotation. */
	List<SelectedConsensusAnnotationChangeListener> selectedConsensusAnnotationChangeListeners;

	List<SelectedClsChangeListener> selectedClsChangeListeners;

	List<AnnotationCreatedListener> annotationCreatedListeners;

	List<CurrentAnnotationsChangedListener> currentAnnotationsChangedListeners;

	List<RefreshAnnotationsDisplayListener> refreshAnnotationsDisplayListeners;

	List<FastAnnotateListener> fastAnnotateListeners;

	List<FilterChangedListener> filterChangedListeners;

	List<NotifyTextChangeListener> notifyTextChangeListeners;

	List<SpanEditListener> spanEditListeners;

	/** Collection of listeners listening for multiple annotation selection events.*/
	List<MultipleAnnotationSelectionChangeListener> multipleAnnotationSelectionChangeListeners;
	
	/** Collection of listeners registered for multiple annotation events */
	List<SlotMentionValueChangedListener> slotMentionValueChangedListeners;
	
	/** Collection of listeners that will be notified of team annotation selection events. */
	List<TeamAnnotationSelectedListener> teamAnnotationSelectedListeners;
	
	List<AcceptedAnnotationListener> acceptedAnnotationListeners;
	
	KnowledgeBase kb;

	KnowtatorManager manager;

	AnnotationUtil annotationUtil;

	MentionUtil mentionUtil;

	KnowtatorProjectUtil kpu;

	TextSourceUtil textSourceUtil;

	FilterUtil filterUtil;

	BrowserTextUtil browserTextUtil;

	Logger logger = Logger.getLogger(EventHandler.class);

	private static EventHandler instance = null;

	public static EventHandler getInstance() {
		if (instance == null) {
			instance = new EventHandler();
		}
		return instance;
	}

	protected EventHandler() {
		selectedSpanChangeListeners = new ArrayList<SelectedSpanChangeListener>();
		selectedAnnotationChangeListeners = new ArrayList<SelectedAnnotationChangeListener>();
		selectedConsensusAnnotationChangeListeners = new ArrayList<SelectedConsensusAnnotationChangeListener>();
		selectedClsChangeListeners = new ArrayList<SelectedClsChangeListener>();
		annotationCreatedListeners = new ArrayList<AnnotationCreatedListener>();
		currentAnnotationsChangedListeners = new ArrayList<CurrentAnnotationsChangedListener>();
		refreshAnnotationsDisplayListeners = new ArrayList<RefreshAnnotationsDisplayListener>();
		fastAnnotateListeners = new ArrayList<FastAnnotateListener>();
		filterChangedListeners = new ArrayList<FilterChangedListener>();
		notifyTextChangeListeners = new ArrayList<NotifyTextChangeListener>();
		spanEditListeners = new ArrayList<SpanEditListener>();
		multipleAnnotationSelectionChangeListeners = new ArrayList<MultipleAnnotationSelectionChangeListener>();
		slotMentionValueChangedListeners = new ArrayList<SlotMentionValueChangedListener>();
		teamAnnotationSelectedListeners = new ArrayList<TeamAnnotationSelectedListener>();
		acceptedAnnotationListeners = new ArrayList<AcceptedAnnotationListener>();
	}

	public void setKnowtatorManager(KnowtatorManager manager) {
		this.manager = manager;
		kb = manager.getKnowledgeBase();
		mentionUtil = manager.getMentionUtil();
		annotationUtil = manager.getAnnotationUtil();
		kpu = manager.getKnowtatorProjectUtil();
		textSourceUtil = manager.getTextSourceUtil();
		filterUtil = manager.getFilterUtil();
		browserTextUtil = manager.getBrowserTextUtil();
	}

	public void addRefreshAnnotationsDisplayListener(RefreshAnnotationsDisplayListener refreshSpanHighlightsListener) {
		refreshAnnotationsDisplayListeners.add(refreshSpanHighlightsListener);
	}

	public void removeRefreshSpanHighlightsListener(RefreshAnnotationsDisplayListener refreshSpanHighlightsListener) {
		refreshAnnotationsDisplayListeners.remove(refreshSpanHighlightsListener);
	}

	public void fireRefreshAnnotationsDisplay(boolean scrollToSelection) {
		logger.debug("");

		for (RefreshAnnotationsDisplayListener refreshAnnotationsDisplayListener : refreshAnnotationsDisplayListeners) {
			refreshAnnotationsDisplayListener.refreshAnnotationsDisplay(scrollToSelection);
		}
	}

	public void addCurrentAnnotationsChangedListener(CurrentAnnotationsChangedListener currentAnnotationsChangedListener) {
		currentAnnotationsChangedListeners.add(currentAnnotationsChangedListener);
	}

	public void removeCurrentAnnotationsChangedListener(
			CurrentAnnotationsChangedListener currentAnnotationsChangedListener) {
		currentAnnotationsChangedListeners.remove(currentAnnotationsChangedListener);
	}

	public void fireCurrentAnnotationsChanged() {
		logger.debug("");
		List<SimpleInstance> filteredAnnotations = manager.getCurrentFilteredAnnotations();
		List<SimpleInstance> partiallyFilteredAnnotations = manager.getCurrentPartiallyFilteredAnnotations();

		CurrentAnnotationsChangeEvent cace = new CurrentAnnotationsChangeEvent(manager, filteredAnnotations,
				partiallyFilteredAnnotations);
		for (CurrentAnnotationsChangedListener cacl : currentAnnotationsChangedListeners) {
			cacl.currentAnnotationsChanged(cace);
		}
	}

	public void addSelectedSpanChangeListener(SelectedSpanChangeListener selectedSpanChangeListener) {
		selectedSpanChangeListeners.add(selectedSpanChangeListener);
	}

	public void removeSelectedSpanChangeListener(SelectedSpanChangeListener selectedSpanChangeListener) {
		selectedSpanChangeListeners.remove(selectedSpanChangeListener);
	}

	/**
	 * The preferred way to update the currently selected spans is via
	 * KnowtatorManager.setSelectedSpans. Please use that method instead.
	 * 
	 * @param selectedSpans
	 * @see KnowtatorManager#setSelectedSpans(List)
	 */
	public void fireSelectedSpanChanged(List<Span> selectedSpans) {
		if (logger.isDebugEnabled()) {
			StringBuffer sb = new StringBuffer();
			for (Span span : selectedSpans)
				sb.append("  " + span.getStart() + "|" + span.getEnd());
			logger.debug(sb.toString());
		}

		SelectedSpanChangeEvent ssce = new SelectedSpanChangeEvent(manager, selectedSpans);
		for (SelectedSpanChangeListener sscl : selectedSpanChangeListeners) {
			sscl.spanSelectionChanged(ssce);
		}
	}

	public void addSelectedAnnotationChangeListener(SelectedAnnotationChangeListener selectedAnnotationChangeListener) {
		selectedAnnotationChangeListeners.add(selectedAnnotationChangeListener);
	}

	public void removeSelectedAnnotationChangeListener(SelectedAnnotationChangeListener selectedAnnotationChangeListener) {
		selectedAnnotationChangeListeners.remove(selectedAnnotationChangeListener);
	}
	
	/**
	 * The preferred way of updating the currently selected annotation is via
	 * KnowtatorManager.setSelectedAnnotation(). Please use that method instead.
	 * 
	 * @param selectedAnnotation
	 * @see KnowtatorManager#setSelectedAnnotationSet(SimpleInstance)
	 */
	public void fireSelectedAnnotationChanged(SimpleInstance selectedAnnotation) {
		logger.debug("selected annotation=\"" + browserTextUtil.getBrowserText(selectedAnnotation, 100) + "\"");
		SelectedAnnotationChangeEvent sace = new SelectedAnnotationChangeEvent(manager, selectedAnnotation);
		for (SelectedAnnotationChangeListener sacl : selectedAnnotationChangeListeners) {
			sacl.annotationSelectionChanged(sace);
		}
	}
	
	/**
	 * Add a listener on the consensus annotation selection change
	 * 
	 * @param selectedConsensusAnnotationChangeListener
	 */
	public void addSelectedConsensusAnnotationChangeListener(SelectedConsensusAnnotationChangeListener selectedConsensusAnnotationChangeListener) {
		selectedConsensusAnnotationChangeListeners.add(selectedConsensusAnnotationChangeListener);
	}

	/**
	 * Removes the given selected consensus annotation listener so it will no longer catch events.
	 * 
	 * @param selectedConsensusAnnotationChangeListener
	 */
	public void removeSelectedConsensusAnnotationChangeListener(SelectedConsensusAnnotationChangeListener selectedConsensusAnnotationChangeListener) {
		selectedConsensusAnnotationChangeListeners.remove(selectedConsensusAnnotationChangeListener);
	}
	
	/**
	 * The preferred way of updating the currently selected consensus annotation is via
	 * <code>KnowtatorManager.setSelectedConsensusAnnotation()</code>. Please use that method instead.
	 * 
	 * @param selectedConsensusAnnotation	 
	 */
	public void fireSelectedConsensusAnnotationChanged(SimpleInstance selectedConsensusAnnotation) {
		logger.debug("selected consensus annotation=\"" + browserTextUtil.getBrowserText(selectedConsensusAnnotation, 100) + "\"");
		SelectedConsensusAnnotationChangeEvent scace = new SelectedConsensusAnnotationChangeEvent(manager, selectedConsensusAnnotation);
		for (SelectedConsensusAnnotationChangeListener scacl : selectedConsensusAnnotationChangeListeners) {
			scacl.consensusAnnotationSelectionChanged(scace);
		}
	}

	public void addSelectedClsChangeListener(SelectedClsChangeListener selectedClsChangeListener) {
		selectedClsChangeListeners.add(selectedClsChangeListener);
	}

	public void removeSelectedClsChangeListener(SelectedClsChangeListener selectedClsChangeListener) {
		selectedClsChangeListeners.remove(selectedClsChangeListener);
	}

	/**
	 * The preferred way of updating the currently selected cls is via
	 * KnowtatorManager.setSelectedCls(). Please use that method instead.
	 * 
	 * @param selectedCls
	 * @see KnowtatorManager#setSelectedCls(Cls)
	 */
	public void fireSelectedClsChanged(Cls selectedCls) {
		logger.debug("selected cls = \"" + selectedCls.getBrowserText() + "\"");
		SelectedClsChangeEvent scce = new SelectedClsChangeEvent(manager, selectedCls);
		for (SelectedClsChangeListener sccl : selectedClsChangeListeners) {
			sccl.clsSelectionChanged(scce);
		}
	}

	public void addAnnotationCreatedListener(AnnotationCreatedListener annotationCreatedListener) {
		annotationCreatedListeners.add(annotationCreatedListener);
	}

	public void removeAnnotationCreatedListener(AnnotationCreatedListener annotationCreatedListener) {
		annotationCreatedListeners.remove(annotationCreatedListener);
	}

	/**
	 * The preferred way of creating an annotation is via
	 * KnowtatorManager.createAnnotation. Please use that method instead.
	 * 
	 * @param createdAnnotation
	 * @see KnowtatorManager#createAnnotation(Instance)
	 */
	public void fireAnnotationCreated(SimpleInstance createdAnnotation) {
		logger.debug("created annotation = \"" + browserTextUtil.getBrowserText(createdAnnotation, 100) + "\"");
		AnnotationCreatedEvent event = new AnnotationCreatedEvent(manager, createdAnnotation);
		for (AnnotationCreatedListener acl : annotationCreatedListeners) {
			acl.annotationCreated(event);
		}
	}

	public void addFastAnnotateListener(FastAnnotateListener fastAnnotateListener) {
		fastAnnotateListeners.add(fastAnnotateListener);
	}

	public void removeFastAnnotateListener(FastAnnotateListener fastAnnotateListener) {
		fastAnnotateListeners.remove(fastAnnotateListener);
	}
	
	public void addAcceptedAnnotationListener(AcceptedAnnotationListener acceptedAnnotationListener) {
		acceptedAnnotationListeners.add( acceptedAnnotationListener );
	}
	
	public void removeAcceptedAnnotationListener(AcceptedAnnotationListener acceptedAnnotationListener) {
		acceptedAnnotationListeners.remove( acceptedAnnotationListener );
	}
	
	public void fireAnnotationAccepted(SimpleInstance acceptedAnnotation) {
		AcceptedAnnotationEvent evt = new AcceptedAnnotationEvent( acceptedAnnotation );
		for (AcceptedAnnotationListener aal : acceptedAnnotationListeners) {
			aal.annotationAccepted( evt );
		}
	}

	public void fireFastAnnotateStart() {
		logger.debug("");
		for (FastAnnotateListener fastAnnotateListener : fastAnnotateListeners) {
			fastAnnotateListener.fastAnnotateStart();
		}
	}

	public void fireFastAnnotateQuit() {
		logger.debug("");
		for (FastAnnotateListener fastAnnotateListener : fastAnnotateListeners) {
			fastAnnotateListener.fastAnnotateQuit();
		}
	}

	public void fireFastAnnotateClsChange() {
		logger.debug("");
		for (FastAnnotateListener fastAnnotateListener : fastAnnotateListeners) {
			fastAnnotateListener.fastAnnotateClsChange();
		}
	}

	public void fireFastAnnotateAddCls(Frame frame) {
		logger.debug("Adding class: " + frame + " to fast annotate tool bar");
		for (FastAnnotateListener fastAnnotateListener : fastAnnotateListeners) {
			fastAnnotateListener.fastAnnotateAddCls(frame);
		}
	}

	public void fireFastAnnotateRemoveCls(Frame frame) {
		logger.debug("Removing class: " + frame + " from fast annotate tool bar");
		for (FastAnnotateListener fastAnnotateListener : fastAnnotateListeners) {
			fastAnnotateListener.fastAnnotateRemoveCls(frame);
		}
	}
	
	public void fireFastAnnotateRefreshToolbar() {
		logger.debug( "Refreshing fast annotate toolbar" );
		for (FastAnnotateListener fastAnnotateListener : fastAnnotateListeners) {
			fastAnnotateListener.fastAnnotateRefreshToolbar();
		}
	}

	public void addFilterChangedListener(FilterChangedListener filterChangedListener) {
		filterChangedListeners.add(filterChangedListener);
	}

	public void removeFilterChangedListener(FilterChangedListener filterChangedListener) {
		filterChangedListeners.remove(filterChangedListener);
	}

	public void fireFilterChanged(SimpleInstance filter, boolean consensusMode) {
		logger.debug(" consensus mode = " + consensusMode);
		FilterChangedEvent event = new FilterChangedEvent(manager, filter, consensusMode);
		for (FilterChangedListener filterChangedListener : filterChangedListeners) {
			filterChangedListener.filterChanged(event);
		}
	}

	public void addNotifyTextChangeListener(NotifyTextChangeListener notifyTextChangeListener) {
		notifyTextChangeListeners.add(notifyTextChangeListener);
	}

	public void removeNotifyTextChangeListner(NotifyTextChangeListener notifyTextChangeListener) {
		notifyTextChangeListeners.remove(notifyTextChangeListener);
	}

	public void fireNotifyTextChanged(String notifyText) {
		logger.debug(notifyText);
		for (NotifyTextChangeListener listener : notifyTextChangeListeners) {
			listener.notifyTextChanged(notifyText);
		}
	}

	public void addSpanEditListener(SpanEditListener spanEditListener) {
		spanEditListeners.add(spanEditListener);
	}

	public void removeSpanEditListner(SpanEditListener spanEditListener) {
		spanEditListeners.remove(spanEditListener);
	}

	public void fireSpanEditted(SimpleInstance annotation) {
		logger.debug(manager.getBrowserTextUtil().getBrowserText(annotation));
		SpanEditEvent see = new SpanEditEvent(manager, annotation);
		for (SpanEditListener listener : spanEditListeners) {
			listener.spanEditted(see);
		}
	}
	
	public void addMultipleAnnotationSelectionChangeListener(MultipleAnnotationSelectionChangeListener listener) {
		multipleAnnotationSelectionChangeListeners.add( listener );
	}
	
	public void removeMultipleAnnotationSelectionChangeListener(MultipleAnnotationSelectionChangeListener listener) {
		multipleAnnotationSelectionChangeListeners.remove( listener );
	}
	
	public void fireMultipleAnnotationSelectionChanged(List<SimpleInstance> selectedAnnotations) {
		MultipleAnnotationSelectionChangeEvent evt = new MultipleAnnotationSelectionChangeEvent( manager, selectedAnnotations );
		for( MultipleAnnotationSelectionChangeListener mascl : multipleAnnotationSelectionChangeListeners ) {
			mascl.multipleAnnotationSelectionChanged( evt );
		}
	}
	
	public void addSlotValueChangedListener(SlotMentionValueChangedListener listener) {
		slotMentionValueChangedListeners.add( listener );
	}
	
	public void removeSlotValueChangedListener(SlotMentionValueChangedListener listener) {
		slotMentionValueChangedListeners.remove( listener );
	}
	
	public void fireSlotValueChanged() {
		for( SlotMentionValueChangedListener svcl : slotMentionValueChangedListeners ) {
			svcl.slotMentionValueChanged();
		}
	}
	
	public void addTeamAnnotationSelectedListener(TeamAnnotationSelectedListener listener) {
		teamAnnotationSelectedListeners.add( listener );
	}
	
	public void removeTeamAnnotationSelectedListener(TeamAnnotationSelectedListener listener) {
		teamAnnotationSelectedListeners.remove( listener );
	}
	
	public void fireTeamAnnotationSelected( SimpleInstance teamAnnotation ) {
		TeamAnnotationSelectedEvent evt = new TeamAnnotationSelectedEvent( teamAnnotation );
		for( TeamAnnotationSelectedListener listener : teamAnnotationSelectedListeners ) {
			listener.teamAnnotationSelected( evt );
		}
	}
	

	public void dispose() {
		selectedSpanChangeListeners.clear();
		selectedAnnotationChangeListeners.clear();
		selectedConsensusAnnotationChangeListeners.clear();
		selectedClsChangeListeners.clear();
		annotationCreatedListeners.clear();
		currentAnnotationsChangedListeners.clear();
		refreshAnnotationsDisplayListeners.clear();
		fastAnnotateListeners.clear();
		filterChangedListeners.clear();
		notifyTextChangeListeners.clear();
		multipleAnnotationSelectionChangeListeners.clear();
		slotMentionValueChangedListeners.clear();
	}

}
