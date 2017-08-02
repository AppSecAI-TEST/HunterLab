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

package edu.uchsc.ccp.knowtator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.ui.FrameComparator;
import edu.uchsc.ccp.knowtator.event.AnnotationCreatedEvent;
import edu.uchsc.ccp.knowtator.event.AnnotationCreatedListener;
import edu.uchsc.ccp.knowtator.event.CurrentAnnotationsChangeEvent;
import edu.uchsc.ccp.knowtator.event.CurrentAnnotationsChangedListener;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.event.RefreshAnnotationsDisplayListener;
import edu.uchsc.ccp.knowtator.exception.ActionNotFoundException;
import edu.uchsc.ccp.knowtator.textsource.TextSource;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;
import edu.uchsc.ccp.knowtator.textsource.TextSourceChangeEvent;
import edu.uchsc.ccp.knowtator.textsource.TextSourceChangeListener;
import edu.uchsc.ccp.knowtator.ui.ColorFrameRenderer;
import edu.uchsc.ccp.knowtator.ui.KnowtatorTextPane;
import edu.uchsc.ccp.knowtator.ui.TextViewer;
import edu.uchsc.ccp.knowtator.ui.action.ActionFactory;
import edu.uchsc.ccp.knowtator.util.ConsensusAnnotations;
import edu.uchsc.ccp.knowtator.util.ConsensusException;
import edu.uchsc.ccp.knowtator.util.ConsensusSet;

public class KnowtatorManager implements CurrentAnnotationsChangedListener, AnnotationCreatedListener,
		TextSourceChangeListener {
	private KnowledgeBase kb;

	private KnowtatorProjectUtil kpu;

	private AnnotationUtil annotationUtil;

	private TextSourceUtil textSourceUtil;

	private SpanUtil spanUtil;

	private FilterUtil filterUtil;

	private MentionUtil mentionUtil;

	private DisplayColors displayColors;

	private BrowserTextUtil browserTextUtil;

	private KnowtatorTextPane textPane;

	private TextViewer textViewer;

	/*
	 * key is a class or instance that is annotated, value is a list of the
	 * annotations for that class or instance. Only annotations in
	 * partiallyFilteredAnnotations are included.
	 */
	java.util.Map<Frame, List<SimpleInstance>> frameAnnotationsMap;

	ColorFrameRenderer renderer;

	List<SimpleInstance> filteredAnnotations;

	List<SimpleInstance> visibleFilteredAnnotations;

	List<SimpleInstance> partiallyFilteredAnnotations;

	List<SimpleInstance> selectableFilteredAnnotations;

	Cls selectedCls;

	SimpleInstance selectedAnnotation;
	
	/** The selected annotation in the comparison view (the far right annotation) */
	SimpleInstance selectedConsensusAnnotation;

	/** the selectedAnnotation if not null else the last non-null selectedAnnotation */
	SimpleInstance lastSelectedAnnotation; 
	
	SimpleInstance lasteSelectedConsensusAnnotation;

	List<Span> selectedSpans;

	Comparator<SimpleInstance> annotationComparator;

	Frame fastAnnotateFrame = null;

	/** Set of Frames (classes) that will be shown in the fast annotate tool bar */
	Set<FrameID> fastAnnotateFrameSet = null;

	boolean fastAnnotateMode = false;

	SimpleInstance selectedFilter = null;

	boolean consensusMode = false;
	
	boolean requiredMode = false;

	ConsensusSet consensusSet = null;

	Logger logger = Logger.getLogger(KnowtatorManager.class);

	public KnowtatorManager(KnowtatorProjectUtil kpu) {
		this.kpu = kpu;
		this.kb = kpu.getKnowledgeBase();

		annotationUtil = new AnnotationUtil(this);
		textSourceUtil = new TextSourceUtil(annotationUtil, kpu);
		annotationUtil.setTextSourceUtil(textSourceUtil);
		mentionUtil = new MentionUtil(kpu);
		annotationUtil.setMentionUtil(mentionUtil);
		mentionUtil.setAnnotationUtil(annotationUtil);
		filterUtil = new FilterUtil(this);
		displayColors = new DisplayColors(this);
		browserTextUtil = new BrowserTextUtil(annotationUtil, mentionUtil, kpu);
		spanUtil = new SpanUtil(this);

		frameAnnotationsMap = new HashMap<Frame, List<SimpleInstance>>();
		renderer = new ColorFrameRenderer(this);

		filteredAnnotations = new ArrayList<SimpleInstance>();
		visibleFilteredAnnotations = new ArrayList<SimpleInstance>();
		partiallyFilteredAnnotations = new ArrayList<SimpleInstance>();
		selectableFilteredAnnotations = new ArrayList<SimpleInstance>();

		EventHandler.getInstance().setKnowtatorManager(this);
		EventHandler.getInstance().addCurrentAnnotationsChangedListener(this);
		EventHandler.getInstance().addAnnotationCreatedListener(this);

		fastAnnotateFrameSet = new LinkedHashSet<FrameID>();
		initListener();
	}

	private void initListener() {
		logger.debug("");

		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kb.getProject());
		configuration.addFrameListener(new FrameAdapter() {
			public void ownSlotValueChanged(FrameEvent frameEvent) {
				updateFilter(frameEvent.getSlot());
			}

			public void ownSlotAdded(FrameEvent frameEvent) {
				updateFilter(frameEvent.getSlot());
			}

			public void ownSlotRemoved(FrameEvent frameEvent) {
				updateFilter(frameEvent.getSlot());
			}

			private void updateFilter(Slot slot) {
				if (slot.equals(kpu.getSelectedFilterSlot())) {
					SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kpu.kb.getProject());
					SimpleInstance configurationFilter = (SimpleInstance) configuration.getDirectOwnSlotValue(kpu
							.getSelectedFilterSlot());
					// prevent infinite loop
					if (configurationFilter != null
							&& (selectedFilter == null || !selectedFilter.equals(configurationFilter))) {
						try {
							setSelectedFilter(configurationFilter);
						} catch (ConsensusException ce) {
							ce.printStackTrace();
						}
					}
				}
			}
		});

	}

	public KnowledgeBase getKnowledgeBase() {
		return kb;
	}

	public void setNotifyText(String text) {
		logger.debug("");
		EventHandler.getInstance().fireNotifyTextChanged(text);
	}

	public List<SimpleInstance> getCurrentAnnotationsForFrame(Frame frame) {
		logger.debug("");
		List<SimpleInstance> annotations = frameAnnotationsMap.get(frame);
		if (annotations == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(frameAnnotationsMap.get(frame));
	}

	public int getCurrentAnnotationCountForFrame(Frame frame) {
		int returnValue = 0;
		if (frameAnnotationsMap.containsKey(frame))
			returnValue = frameAnnotationsMap.get(frame).size();
		logger.debug("count = " + returnValue);
		return returnValue;
	}

	public int getConsolidatedAnnotationCountForFrame(Frame frame) {
		logger.debug("");
		int returnValue = 0;
		if (frameAnnotationsMap.containsKey(frame)) {
			List<SimpleInstance> frameAnnotations = frameAnnotationsMap.get(frame);
			SimpleInstance teamAnnotator = consensusSet.getTeamAnnotator();
			for (SimpleInstance frameAnnotation : frameAnnotations) {
				if (teamAnnotator.equals(annotationUtil.getAnnotator(frameAnnotation)))
					returnValue++;
			}
		}
		return returnValue;
	}

	/**
	 * @return the next annotation
	 * @see #selectNextAnnotation()
	 */
	public SimpleInstance getNextAnnotation() {
		logger.debug("");
		return getNextPreviousAnnotation(1);
	}

	/**
	 * 
	 * @return the previous annotation
	 * @see #selectPreviousAnnotation
	 */
	public SimpleInstance getPreviousAnnotation() {
		logger.debug("");
		return getNextPreviousAnnotation(-1);
	}

	private SimpleInstance getNextPreviousAnnotation(int delta) {
		logger.debug("");
		SimpleInstance mention = annotationUtil.getMention(selectedAnnotation);
		Frame mentionFrame = mentionUtil.getMentionFrame(mention);
		if (mentionFrame != null) {
			List<SimpleInstance> frameAnnotations = frameAnnotationsMap.get(mentionFrame);
			Comparator<SimpleInstance> annotationComparator = getAnnotationComparator();
			if (annotationComparator != null)
				Collections.sort(frameAnnotations, annotationComparator);
			else
				Collections.sort(frameAnnotations, new FrameComparator());

			for (int i = 0; i < frameAnnotations.size(); i++) {
				SimpleInstance frameAnnotation = frameAnnotations.get(i);
				if (frameAnnotation.equals(selectedAnnotation)) {
					if (i + delta >= 0 && i + delta < frameAnnotations.size())
						return frameAnnotations.get(i + delta);
				}
			}
			if (frameAnnotations.size() > 0) {
				if (delta > 0)
					return frameAnnotations.get(0);
				else
					return frameAnnotations.get(frameAnnotations.size() - 1);
			}
		}
		return selectedAnnotation;
	}	
	
	/**
	 * Finds out if the given annotation has any required slots that do not have
	 *   values filled in.
	 * 
	 * @param annotation The annotation to search
	 * 
	 * @return True if and only if the given annotation has at least one required
	 *            slot, and that slot does not have a value.
	 */
	public boolean hasUnsetRequiredSlots( SimpleInstance annotation ) {
		
		if( annotation == null ) {
			return false;
		}
		
		SimpleInstance mention = annotationUtil.getMention(annotation);	
		List<Slot> mentionSlots = mentionUtil.getMentionSlots( mention );		
			
		for( Slot slot : mentionSlots ) {
    		if( slot != null ) {
    			int minCard = slot.getMinimumCardinality();

        		SimpleInstance slotMention = mentionUtil.getSlotMention(mention, slot);
    			if (slotMention != null) {
    				List<Object> slotValues = mentionUtil.getSlotMentionValues(slotMention);
    				if( (slotValues == null || slotValues.isEmpty()) &&
        				(minCard > 0) ) {
        				
    					return true;    					    				
        			}
    			}
    		}
		}
				
		return false;
	}
	
	/**
	 * Gets the next annotation that has unset required slots below the currently 
	 * 	 selected annotation. If the currently selected annotation is null, 
	 *   the search is started from the beginning of the document.
	 * 
	 * @return The next required annotation, or null if no required annotation was found.
	 */
	public SimpleInstance getNextRequiredAnnotation() {	
		List<SimpleInstance> frameAnnotations = getCurrentFilteredAnnotations();

		boolean selectedAnnotationFound = false;
		
		for( SimpleInstance annotation : frameAnnotations ) {
			if( annotation.equals( selectedAnnotation ) ) {
				selectedAnnotationFound = true;
			}
			
			if( selectedAnnotationFound ) {
				if( hasUnsetRequiredSlots( annotation ) &&
					(!annotation.equals( selectedAnnotation)) ) {
					
					return annotation;
				}
			}
		}
		
		//The end of the document was reached, and the annotation was not found, so re-start the search from the
		//  beginning of the document
		for( SimpleInstance annotation : frameAnnotations ) {
			if( hasUnsetRequiredSlots( annotation ) ) {
				return annotation;
			}
		}

		//No annotation was found with required fields
		return null;
	}
	
	/**
	 * Retrieves the next required annotation (via <code>getNextRequiredAnnotation()</code>) 
	 *   and sets it as the currently selected annotation.
	 */
	public void selectNextRequiredAnnotation() {
		logger.debug( "Selected next required annotation" );
		SimpleInstance annotation = getNextRequiredAnnotation();
		setSelectedAnnotation( annotation );			
	}
	
	/**
	 * Counts the number of annotations that have required slots that do not have values
	 *   associated with those slots.
	 *   
	 * This is mainly used to help in calculating the percentages for required mode.
	 * 
	 * @return The count of annotations that have unset required slots.
	 */
	public int getRequiredAnnotationCount() {
		int requiredAnnotationCount = 0;
		
		List<SimpleInstance> frameAnnotations = getCurrentFilteredAnnotations();
		for( SimpleInstance annotation : frameAnnotations ) {
			if( !hasUnsetRequiredSlots(annotation ) ) {
				requiredAnnotationCount++;
			}
		}
		
		return requiredAnnotationCount;
	}
					
	/**
	 * Returns the annotations for the current text source that satisfy the
	 * currently selected filter.
	 */

	public List<SimpleInstance> getCurrentFilteredAnnotations() {
		logger.debug("");
		if (filteredAnnotations == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(filteredAnnotations);
	}

	public List<SimpleInstance> getVisibleFilteredAnnotations() {
		if (visibleFilteredAnnotations == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(visibleFilteredAnnotations);
	}

	/**
	 * returns the annotations for the current text source that satisfy the
	 * currently selected filter ignoring the type constraint.
	 */
	public List<SimpleInstance> getCurrentPartiallyFilteredAnnotations() {
		logger.debug("");
		if (partiallyFilteredAnnotations == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(partiallyFilteredAnnotations);
	}

	public List<SimpleInstance> getCurrentSelectableFilteredAnnotations() {
		logger.debug("");
		if (selectableFilteredAnnotations == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(selectableFilteredAnnotations);
	}

	public void updateCurrentAnnotations() {
		logger.debug("");
		TextSource currentTextSource = textSourceUtil.getCurrentTextSource();
		if (currentTextSource == null)
			return;
		Collection<SimpleInstance> annotations = annotationUtil.getAnnotations(currentTextSource);
						
		//This step is important for required mode. Previously, the slot mentions were lazily loaded,
		//  but for the required mode algorithm to work, *all* slot mentions must be initialized early.
		if( annotations != null ) {
    		for( SimpleInstance annotation : annotations ) {
    			SimpleInstance mention = annotationUtil.getMention(annotation);
    			mentionUtil.initializeSlotMentions( mention );
    		}
		}
		
		filteredAnnotations.clear();
		visibleFilteredAnnotations.clear();
		partiallyFilteredAnnotations.clear();
		selectableFilteredAnnotations.clear();

		if (annotations != null) {
			SimpleInstance currentFilter = getSelectedFilter();
			if (currentFilter != null) {
				partiallyFilteredAnnotations.addAll(filterUtil.filterAnnotations(annotations, currentFilter, true));
				filteredAnnotations.addAll(filterUtil.filterAnnotations(partiallyFilteredAnnotations, currentFilter));
				selectableFilteredAnnotations.addAll(filterUtil.filterAnnotations(filteredAnnotations, currentFilter,
						false, true));
			} else {
				filteredAnnotations.addAll(annotations);
				partiallyFilteredAnnotations.addAll(annotations);
				selectableFilteredAnnotations.addAll(annotations);
			}
		}

		EventHandler.getInstance().fireCurrentAnnotationsChanged();
		SimpleInstance selectedAnnotation = getSelectedAnnotation();
		if (!filteredAnnotations.contains(selectedAnnotation)) {
			setSelectedAnnotation(null);
		}
		if(!filteredAnnotations.contains(selectedConsensusAnnotation)) {
			setSelectedConsensusAnnotation(null);
		}
		Collections.sort(filteredAnnotations, spanUtil.comparator(browserTextUtil.comparator()));
		updateVisibleFilteredAnnotations();
		refreshAnnotationsDisplay(true);
	}

	/*
	 * this method would be faster if we did a binary search for the first
	 * visible annotation. However, this will take a bit of doing because there
	 * needs to be a comparator for the SimpleInstance's that make use of the
	 * spans
	 */
	public void updateVisibleFilteredAnnotations() {
		visibleFilteredAnnotations.clear();
		Span visibleSpan = getVisibleSpan();
		if (visibleSpan == null)
			return;
		logger
				.debug("visibleSpan = " + visibleSpan + "  filteredAnnotations = "
						+ selectableFilteredAnnotations.size());

		for (SimpleInstance filteredAnnotation : selectableFilteredAnnotations) {
			List<Span> spans = annotationUtil.getSpans(filteredAnnotation);
			if (spans != null && spans.size() > 0) {
				if (visibleSpan.contains(spans.get(0))) {
					visibleFilteredAnnotations.add(filteredAnnotation);
				}
			}
		}
		// we sort them by span length because that is how KnowtatorTextPane
		// needs them
		// so that spans are highlighted with the correct precedence.
		// we do it here because then we don't have to do it as often.
		Collections.sort(visibleFilteredAnnotations, spanUtil.lengthComparator());
		logger.debug("visible filtered annotations = " + visibleFilteredAnnotations.size());
	}
	
	
	/**
	 * Retrieves the next non team annotation after the currently selected annotation.
	 *  
	 * @return
	 */
	private SimpleInstance getNextNonTeamAnnotation() {
		logger.debug("");

		if (selectedAnnotation != null) {

			List<SimpleInstance> frameAnnotations = getCurrentFilteredAnnotations();

			boolean selectedAnnotationFound = false;
			
			for( SimpleInstance annotation : frameAnnotations ) {
				if( annotation.equals( selectedAnnotation ) ) {
					selectedAnnotationFound = true;
				}
				
				if( selectedAnnotationFound ) {
					if( !isTeamAnnotation( annotation ) ) {
						return annotation;
					}
				}
			}					
			
			if (frameAnnotations.size() > 0) {
				return frameAnnotations.get(0);				
			}
		}
		
		return selectedAnnotation;
	}
	
	/**
	 * Finds and automatically selects the next non team annotation (after the currently selected
	 *    annotation. This is to be used during consensus mode, so it will automatically select
	 *    the consensus annotation (on the right pane) as well, so the pair-wise comparison can
	 *    take place.
	 */
	public void selectNextNonTeamAnnotation() {
		SimpleInstance annotation = getNextNonTeamAnnotation();
			
		if( annotation != null ) {
			List<Span> spans = annotationUtil.getSpans( annotation );
			for (Span span : spans) {
				int averageSpan = (int)((span.getStart() + span.getEnd()) / 2);
				
				KnowtatorTextPane textPane = getTextPane();
				textPane.setPreviousOffset( averageSpan );
				List<SimpleInstance> annotationList = textPane.getAnnotationsAtFromFullDocument( averageSpan );

				if( annotationList.size() == 1 ) {
					setSelectedAnnotation(annotation);
				} else {
					setSelectedAnnotations( annotationList );
				}
				break;
			}
		}
	}
		

	/**
	 * This is the preferred way to obtain the currently selected cls (i.e. the
	 * cls selected in the annotation schema pane/tree.)
	 * 
	 * @return the currently selected cls.
	 */
	public Cls getSelectedCls() {
		logger.debug("");
		return selectedCls;
	}

	/**
	 * This is the preferred way to set the curently selected cls (i.e. the cls
	 * that is highlighted in the annotation schema pane/tree.) This method will
	 * call the appropriate event firing method in EventHandler.
	 * 
	 * @param cls
	 */
	public void setSelectedCls(Cls cls) {
		logger.debug("");
		logger.debug("selcected cls = \"" + cls.getBrowserText() + "\"");
		selectedCls = cls;
		EventHandler.getInstance().fireSelectedClsChanged(cls);
	}

	/**
	 * This is the preferred way to obtain the currently selected annotation
	 * 
	 * @return the currently selected annotation.
	 */
	public SimpleInstance getSelectedAnnotation() {
		logger.debug("");
		return selectedAnnotation;
	}

	/**
	 * @return the currently selected annotation if it is not null. Otherwise,
	 *         it returns the previously selected annotation or null if there
	 *         has never been an annotation selected.
	 */
	public SimpleInstance getLastSelectedAnnotation() {
		logger.debug("");
		return lastSelectedAnnotation;
	}
	
	/**
	 * This is the preferred way to obtain the currently selected consensus annotation
	 * 
	 * The consensus annotation is the 2nd annotation selected during consensus
	 *   mode enabling a side-by-side comparison.
	 * 
	 * @return the currently selected consensus annotation.
	 */
	public SimpleInstance getSelectedConsensusAnnotation() {
		logger.debug("");
		return selectedConsensusAnnotation;
	}

	/**
	 * This is the preferred way to set the currently selected annotation. This
	 * method will call the appropriate event firing method in EventHandler.
	 * 
	 * @param annotation
	 */
	public void setSelectedAnnotation(SimpleInstance annotation) {
		logger.debug("selected annotation=\"" + browserTextUtil.getBrowserText(annotation, 100) + "\"");		
		selectedAnnotation = annotation;
		if (annotation != null)
			lastSelectedAnnotation = annotation;
		EventHandler.getInstance().fireSelectedAnnotationChanged(annotation);
		refreshAnnotationsDisplay(true);
		setNotifyText(getBrowserTextUtil().getBrowserText(annotation));
		
		if( isTeamAnnotation( selectedAnnotation ) ) {
			EventHandler.getInstance().fireTeamAnnotationSelected( selectedAnnotation );
			setSelectedConsensusAnnotation( null );
		} else {
			EventHandler.getInstance().fireTeamAnnotationSelected( null );
		}
	}
	
	
	/**
	 * This is the preferred way to set the currently selected consensus annotation. This
	 * method will call the appropriate event firing method in EventHandler.
	 * 
	 * The consensus annotation is the 2nd annotation selected during consensus
	 *   mode enabling a side-by-side comparison.
	 * 
	 * @param consensusAnnotation
	 */
	public void setSelectedConsensusAnnotation(SimpleInstance consensusAnnotation) {
		logger.debug("selected consensus annotation=\"" + browserTextUtil.getBrowserText(consensusAnnotation, 100) + "\"");
		selectedConsensusAnnotation = consensusAnnotation;
		if (consensusAnnotation != null) {
			lasteSelectedConsensusAnnotation = consensusAnnotation;
		}
		
		EventHandler.getInstance().fireSelectedConsensusAnnotationChanged(consensusAnnotation);
		
		//TODO: Do we need to have specific consensus annotation events for the following?
		refreshAnnotationsDisplay(true);
		setNotifyText(getBrowserTextUtil().getBrowserText(consensusAnnotation));
	}
	
	/**
	 * Used during consensus mode to allow multiple selection of annotations at a time.
	 * 
	 * The first annotation in the given list will become the selected annotation, the
	 *   second annotation in the list will become the selected consensus annotation.
	 * 
	 * @param selectedAnnotations A List of annotations. Only the first 2 annotations
	 *           will be used.
	 */
	public void setSelectedAnnotations(List<SimpleInstance> selectedAnnotations) {
		if( selectedAnnotations.size() >= 2 ) {
			SimpleInstance firstAnnotation = (SimpleInstance)selectedAnnotations.get(0);
			SimpleInstance secondAnnotation = (SimpleInstance)selectedAnnotations.get(1);
						
			setSelectedAnnotation( firstAnnotation );
			setSelectedConsensusAnnotation( secondAnnotation );
			
			EventHandler.getInstance().fireMultipleAnnotationSelectionChanged(selectedAnnotations);
		}
	}

	/**
	 * The "next" annotation is defined as the annotation (if it exists) that is
	 * of the same type as the currently selected annotation that is 'next'
	 * according to the sort order defined by the currently selected comparator.
	 */
	public void selectNextAnnotation() {
		logger.debug("");
		setSelectedAnnotation(getNextAnnotation());
	}

	/**
	 * The "previous" annotation is defined as the annotation (if it exists)
	 * that is of the same type as the currently selected annotation that is
	 * 'next' according to the sort order defined by the currently selected
	 * comparator.
	 */
	public void selectPreviousAnnotation() {
		logger.debug("");
		setSelectedAnnotation(getPreviousAnnotation());
	}

	/**
	 * This is the preferred way to obtain the currently selected spans (i.e.
	 * the spans selected in the text pane.)
	 * 
	 * @return a list of currenly selected spans highlighted by the user
	 */
	public List<Span> getSelectedSpans() {
		logger.debug("");
		if (selectedSpans == null)
			return Collections.emptyList();
		return Collections.unmodifiableList(selectedSpans);
	}

	/**
	 * This is the preferred way to set the currently selected spans. This
	 * method will call the appropriate event firing method in EventHandler.
	 * 
	 * @param selectedSpans
	 * @see EventHandler#fireSelectedSpanChanged(List)
	 */
	public void setSelectedSpans(List<Span> selectedSpans) {
		if (selectedSpans == null) {
			this.selectedSpans.clear();
		} else {
			logger.debug("");
			if (logger.isDebugEnabled()) {
				StringBuffer sb = new StringBuffer();
				for (Span span : selectedSpans)
					sb.append("  " + span.getStart() + "|" + span.getEnd());
				logger.debug(sb.toString());
			}
			this.selectedSpans = new ArrayList<Span>(selectedSpans);
		}
		EventHandler.getInstance().fireSelectedSpanChanged(selectedSpans);
	}

	public List<SimpleInstance> getColorAssignments() {
		logger.debug("");
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kb.getProject());
		Collection<SimpleInstance> colorAssignments = (Collection<SimpleInstance>) configuration
				.getDirectOwnSlotValues(kpu.getColorAssignmentsSlot());
		List<SimpleInstance> returnValues = new ArrayList<SimpleInstance>();
		if (colorAssignments != null) {
			returnValues.addAll(colorAssignments);
		}
		return returnValues;
	}

	public List<Cls> getRootClses() {
		logger.debug("");
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kb.getProject());
		Collection<Cls> rootClses = (Collection<Cls>) configuration.getDirectOwnSlotValues(kpu.getRootClsesSlot());
		List<Cls> returnValues = new ArrayList<Cls>();
		if (rootClses == null || rootClses.size() == 0) {
			returnValues.add(kb.getRootCls());
		} else {
			returnValues.addAll(rootClses);
		}
		return returnValues;
	}
	
	public List<Cls> getFastAnnotateClses() {
		logger.debug("");
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kb.getProject());
		Collection<Cls> fastAnnotateClasses = (Collection<Cls>) configuration.getDirectOwnSlotValues(kpu.getFastAnnotateSlot());
		List<Cls> returnValues = new ArrayList<Cls>();
		if (fastAnnotateClasses == null || fastAnnotateClasses.size() == 0) {
			returnValues.add(kb.getRootCls());
		} else {
			returnValues.addAll(fastAnnotateClasses);
		}
		return returnValues;
	}
	
	public void setFastAnnotateClses( List<Cls> fastAnnotateClses ) {
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kb.getProject());
		configuration.setDirectOwnSlotValues(kpu.getFastAnnotateSlot(), fastAnnotateClses);		
	}
	
	public void removeFastAnnotateCls( Frame fastAnnotateCls ) {
		List<Cls> fastAnnotateClasses = getFastAnnotateClses();
		fastAnnotateClasses.remove( fastAnnotateCls );
		setFastAnnotateClses(fastAnnotateClasses);
	}
	
	public void addFastAnnotateCls( Cls fastAnnotateCls ) {
		List<Cls> fastAnnotateClasses = getFastAnnotateClses();
		if( fastAnnotateClasses != null && !fastAnnotateClasses.contains( fastAnnotateCls ) ) {
			fastAnnotateClasses.add( fastAnnotateCls );
			setFastAnnotateClses(fastAnnotateClasses);
		}
	}

	public SimpleInstance getSelectedAnnotator() {
		logger.debug("");
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kb.getProject());
		return (SimpleInstance) configuration.getDirectOwnSlotValue(kpu.getSelectedAnnotatorSlot());
	}

	public void setSelectedAnnotator(SimpleInstance annotator) {
		logger.debug("selected annotator = \"" + annotator.getBrowserText() + "\"");
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kb.getProject());
		configuration.setDirectOwnSlotValue(kpu.getSelectedAnnotatorSlot(), annotator);
	}

	public SimpleInstance getSelectedAnnotationSet() {
		logger.debug("");
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kb.getProject());
		return (SimpleInstance) configuration.getDirectOwnSlotValue(kpu.getSelectedAnnotationSetSlot());
	}

	public void setSelectedAnnotationSet(SimpleInstance annotationSet) {
		logger.debug("selected annotation set = \"" + annotationSet.getBrowserText() + "\"");
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kb.getProject());
		configuration.setDirectOwnSlotValue(kpu.getSelectedAnnotationSetSlot(), annotationSet);
	}

	public String getTokenRegex() {
		logger.debug("");
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kb.getProject());
		return (String) configuration.getDirectOwnSlotValue(kpu.getTokenRegexSlot());
	}

	public void currentAnnotationsChanged(CurrentAnnotationsChangeEvent cace) {
		logger.debug("");
		frameAnnotationsMap.clear();
		List<SimpleInstance> currentFilteredAnnotations = cace.getFilteredAnnotations();
		logger.debug("currentFilteredAnnotations.size()=" + currentFilteredAnnotations.size());
		for (SimpleInstance annotation : currentFilteredAnnotations) {
			SimpleInstance mention = annotationUtil.getMention(annotation);
			Frame annotatedFrame = mentionUtil.getMentionFrame(mention);
			if (!frameAnnotationsMap.containsKey(annotatedFrame))
				frameAnnotationsMap.put(annotatedFrame, new ArrayList<SimpleInstance>());
			frameAnnotationsMap.get(annotatedFrame).add(annotation);
		}
	}

	public void annotationCreated(AnnotationCreatedEvent event) {
		logger.debug("");
		SimpleInstance annotation = event.getCreatedAnnotation();
		SimpleInstance mention = annotationUtil.getMention(annotation);
		Frame annotatedFrame = mentionUtil.getMentionFrame(mention);
		if (!frameAnnotationsMap.containsKey(annotatedFrame))
			frameAnnotationsMap.put(annotatedFrame, new ArrayList<SimpleInstance>());
		frameAnnotationsMap.get(annotatedFrame).add(annotation);

		int insertionPoint = Collections.binarySearch(filteredAnnotations, annotation, spanUtil
				.comparator(browserTextUtil.comparator()));
		if (insertionPoint < 0) {
			insertionPoint = -(insertionPoint + 1);
		}
		filteredAnnotations.add(insertionPoint, annotation);
		partiallyFilteredAnnotations.add(annotation);
		selectableFilteredAnnotations.add(annotation);
		visibleFilteredAnnotations.add(annotation);
	}

	/**
	 * @param scrollToSelection please see javadoc for RefreshAnnotationsDisplayListener
	 * @see RefreshAnnotationsDisplayListener#refreshAnnotationsDisplay(boolean)
	 */
	public void refreshAnnotationsDisplay(boolean scrollToSelection) {
		logger.debug("");
		EventHandler.getInstance().fireRefreshAnnotationsDisplay(scrollToSelection);
	}

	public void deleteSelectedAnnotation() {
		logger.debug("");
		SimpleInstance nextAnnotation = getNextAnnotation();
		if((nextAnnotation != null) && nextAnnotation.equals(getSelectedAnnotation())) {
			nextAnnotation = null;
		}		
		
		deleteAnnotation(getSelectedAnnotation());
		setSelectedAnnotation( null );
		
		if( getNextAnnotationOnDelete() ) {
			setSelectedAnnotation(nextAnnotation);
		
			if( isConsensusMode() ) {
				setSelectedConsensusAnnotation( null );
				selectNextNonTeamAnnotation();
			}
		}
	}

	public void deleteAnnotation(SimpleInstance annotation) {
		logger.debug("");
		if (annotation != null) {
			annotationUtil.deleteMention((SimpleInstance) annotation);
			kb.deleteInstance(annotation);
			filteredAnnotations.remove(annotation);
			partiallyFilteredAnnotations.remove(annotation);
			selectableFilteredAnnotations.remove(annotation);
			visibleFilteredAnnotations.remove(annotation);
			EventHandler.getInstance().fireCurrentAnnotationsChanged();
		}
	}

	// TODO what should the cls of the new annotation be?
	public void duplicateSelectedAnnotation() {
		logger.debug("");
		SimpleInstance annotation = getSelectedAnnotation();
		if (annotation != null) {
			logger.debug("creating duplicate annotation");
			SimpleInstance duplicateAnnotation;
			try {
				SimpleInstance mention = mentionUtil.createMention(null);
				List<Span> spans = annotationUtil.getSpans(annotation);

				duplicateAnnotation = annotationUtil.createAnnotation(mention, getSelectedAnnotator(), spans,
						textSourceUtil.getCurrentTextSource(), getSelectedAnnotationSet());
				if (duplicateAnnotation == null)
					return;

				EventHandler.getInstance().fireAnnotationCreated(duplicateAnnotation);
				setSelectedAnnotation(duplicateAnnotation);
				refreshAnnotationsDisplay(true);
			}
			// TODO why is the parent component of this dialog null?
			catch (TextSourceAccessException tsae) {
				JOptionPane.showMessageDialog(null, "There was a problem retrieving the text from the text source: "
						+ tsae.getMessage(), "Text Source Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * calls createAnnotation(selectedClsOrInstance, true)
	 * 
	 * @param selectedClsOrInstance
	 *            - a Cls or Instance frame in Protege that specifies the type
	 *            of annotation to create. all other settings for the new
	 *            annotation will be derived from the current settings and
	 *            selections.
	 * @return the created annotation
	 */
	public SimpleInstance createAnnotation(Instance selectedClsOrInstance) {
		logger.debug("");
		return createAnnotation(selectedClsOrInstance, true);
	}

	/**
	 * @param selectedClsOrInstance
	 * @param selectCreatedAnnotation
	 *            determines whether the newly created annotation should be
	 *            selected. If the new annotation is a slot filler, then you
	 *            might not want it to be selected as the annotation of focus
	 *            for Knowtator.
	 * @return the created annotation or null if something went wrong.
	 */
	public SimpleInstance createAnnotation(Instance selectedClsOrInstance, boolean selectCreatedAnnotation) {
		logger.debug("creating annotation for \"" + selectedClsOrInstance.getBrowserText() + "\"");
		SimpleInstance createdAnnotation;
		try {
			SimpleInstance mention = mentionUtil.createMention(selectedClsOrInstance);
			List<Span> selectedSpans = getSelectedSpans();

			createdAnnotation = annotationUtil.createAnnotation(mention, getSelectedAnnotator(), selectedSpans,
					textSourceUtil.getCurrentTextSource(), getSelectedAnnotationSet());
			if (createdAnnotation == null)
				return null;

			EventHandler.getInstance().fireAnnotationCreated(createdAnnotation);
			if (selectCreatedAnnotation)
				setSelectedAnnotation(createdAnnotation);

			refreshAnnotationsDisplay(true);
			return createdAnnotation;
		}
		// TODO why is the parent component of this dialog null?
		catch (TextSourceAccessException tsae) {
			JOptionPane.showMessageDialog(null, "There was a problem retrieving the text from the text source: "
					+ tsae.getMessage(), "Text Source Error", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	public AnnotationUtil getAnnotationUtil() {
		logger.debug("");
		return annotationUtil;
	}

	public BrowserTextUtil getBrowserTextUtil() {
		logger.debug("");
		return browserTextUtil;
	}

	public DisplayColors getDisplayColors() {
		logger.debug("");
		return displayColors;
	}

	public FilterUtil getFilterUtil() {
		logger.debug("");
		return filterUtil;
	}

	public MentionUtil getMentionUtil() {
		logger.debug("");
		return mentionUtil;
	}

	public SpanUtil getSpanUtil() {
		logger.debug("");
		return spanUtil;
	}

	public TextSourceUtil getTextSourceUtil() {
		logger.debug("");
		return textSourceUtil;
	}

	public KnowtatorProjectUtil getKnowtatorProjectUtil() {
		logger.debug("");
		return kpu;
	}

	public ColorFrameRenderer getRenderer() {
		logger.debug("");
		return renderer;
	}

	public KnowtatorTextPane getTextPane() {
		logger.debug("");
		return textPane;
	}

	public void setTextPane(KnowtatorTextPane textPane) {
		logger.debug("");
		this.textPane = textPane;
	}

	public Comparator<SimpleInstance> getAnnotationComparator() {
		logger.debug("");
		return annotationComparator;
	}

	public Comparator<SimpleInstance> getPositionComparator(SimpleInstance annotation) {
		logger.debug("");
		return textViewer.comparator(annotation);
	}

	public void setAnnotationComparator(Comparator<SimpleInstance> annotationComparator) {
		logger.debug("");
		this.annotationComparator = annotationComparator;
	}

	/**
	 * 
	 * @return the fast annotation cls. May return null if it has not been set
	 *         yet (or set to null).
	 */
	public Frame getFastAnnotateFrame() {
		logger.debug("");
		return fastAnnotateFrame;
	}

	public void setFastAnnotateFrame(Frame fastAnnotateFrame) {
		logger.debug("");
		this.fastAnnotateFrame = fastAnnotateFrame;
		if (fastAnnotateFrame != null) {
			fastAnnotateFrameSet.add(fastAnnotateFrame.getFrameID());
			addFastAnnotateCls( (Cls)fastAnnotateFrame );
		}
				
		EventHandler.getInstance().fireFastAnnotateAddCls(fastAnnotateFrame);
		EventHandler.getInstance().fireFastAnnotateClsChange();
	}

	public void startFastAnnotate() {
		logger.debug("");
		fastAnnotateMode = true;
		EventHandler.getInstance().fireFastAnnotateStart();
	}

	/**
	 * This method allows you to start fast annotation mode and set the fast
	 * annotation class in one convenient method. If this method is called when
	 * KnowtatorManager.isFastAnnotateMode() is true, then this method has the
	 * same effect as calling setFastAnnotateCls.
	 * 
	 * @param fastAnnotateFrame
	 *            the class with which to annotate with
	 * @see #isFastAnnotateMode()
	 * @see #setFastAnnotateFrame(Frame)
	 */
	public void startFastAnnotate(Frame fastAnnotateFrame) {
		logger.debug("");
		setFastAnnotateFrame(fastAnnotateFrame);

		if (!fastAnnotateMode) {
			fastAnnotateMode = true;
			EventHandler.getInstance().fireFastAnnotateStart();
		}
	}

	/**
	 * Removes the given frame (class) from the fast annotate tool bar.
	 * 
	 * @param frame
	 *            The frame (or Cls) to be removed.
	 */
	public void removeFastAnnotateFrame(Frame frame) {
		if (frame != null) {
			fastAnnotateFrameSet.remove(frame.getFrameID());
			removeFastAnnotateCls( (Cls)frame );
		}
		EventHandler.getInstance().fireFastAnnotateRemoveCls(frame);
	}

	/**
	 * Finds out if the given frame is being shown in the fast annotate tool
	 * bar.
	 * 
	 * @param frame
	 *            The frame to lookup
	 * @return True if the frame (Cls) is being shown in the tool bar, returns
	 *         false otherwise.
	 */
	public boolean fastAnnotateToolBarContains(Frame frame) {
		if (frame != null) {
			return fastAnnotateFrameSet.contains(frame.getFrameID());
		} else {
			return false;
		}
	}

	public void quitFastAnnotate() {
		logger.debug("");
		fastAnnotateMode = false;
		EventHandler.getInstance().fireFastAnnotateQuit();
	}

	public boolean isFastAnnotateMode() {
		logger.debug("");
		return fastAnnotateMode;
	}

	public void setSelectedFilter(SimpleInstance filter) throws ConsensusException {				
		logger.debug("");
		
		//The filter is changed, so let's clear the annotation selection. This will properly
		//  reset the annotation detail panes for both the selected annotation details pane as well
		//  as the consensus annotation details pane
		setSelectedAnnotation(null);
		setSelectedConsensusAnnotation(null);
		
		if (filter == null)
			logger.debug("filter == null");
		selectedFilter = filter;
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kpu.kb.getProject());
		configuration.setDirectOwnSlotValue(kpu.getSelectedFilterSlot(), selectedFilter);
		updateCurrentAnnotations();

		if (!getActiveFilters().contains(filter))
			addActiveFilter(filter);

		if (FilterUtil.isConsensusFilter(filter)) {
			Set<SimpleInstance> consensusAnnotations = new HashSet<SimpleInstance>(getCurrentFilteredAnnotations());
			SimpleInstance consensusSetInstance = (SimpleInstance) getSelectedFilter().getOwnSlotValue(
					kpu.getFilterSetSlot());
			consensusSet = new ConsensusSet(consensusAnnotations, consensusSetInstance, this);
			consensusMode = true;
			kpu.displayAnnotationAuthor();
		} else {
			consensusMode = false;
		}
		EventHandler.getInstance().fireFilterChanged(filter, consensusMode);

		// List<SimpleInstance> filterAnnotators =
		// filterUtil.getAnnotators(filter);
		// if(filterAnnotators != null && filterAnnotators.size() > 0)
		// {
		// setSelectedAnnotator(filterAnnotators.get(0));
		// }

	}

	public SimpleInstance getSelectedFilter() {
		logger.debug("");
		if (selectedFilter != null)
			return selectedFilter;

		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kpu.kb.getProject());
		SimpleInstance configurationFilter = (SimpleInstance) configuration.getDirectOwnSlotValue(kpu
				.getSelectedFilterSlot());
		if (configurationFilter == null) {
			SimpleInstance showAllFilter = kpu.getShowAllFilter();
			try {
				setSelectedFilter(showAllFilter);
			} catch (ConsensusException ce) {
				ce.printStackTrace();
			} // should never throw a consensus exception here
			return showAllFilter;
		} else {
			try {
				setSelectedFilter(configurationFilter);
				return configurationFilter;
			} catch (ConsensusException ce) {
				SimpleInstance showAllFilter = kpu.getShowAllFilter();
				try {
					setSelectedFilter(showAllFilter);
				} catch (ConsensusException ex) {
					ex.printStackTrace();
				} // should never throw a consensus exception here
				return showAllFilter;
			}
		}
	}

	public List<SimpleInstance> getActiveFilters() {
		logger.debug("");
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kpu.kb.getProject());
		Collection<SimpleInstance> activeFilters = (Collection<SimpleInstance>) configuration
				.getDirectOwnSlotValues(kpu.getActiveFiltersSlot());
		List<SimpleInstance> returnValues = new ArrayList<SimpleInstance>();
		if (activeFilters != null && activeFilters.size() > 0) {
			returnValues.addAll(activeFilters);
		} else {
			SimpleInstance selectedFilter = null;
			selectedFilter = getSelectedFilter();
			returnValues.add(getSelectedFilter());
			addActiveFilter(selectedFilter);
		}
		if (returnValues.size() == 0) {
			returnValues.add(kpu.getShowAllFilter());
			returnValues.add(kpu.getShowNoneFilter());
		}
		return returnValues;
	}

	public void setActiveFilters(Collection<SimpleInstance> activeFilters) {
		logger.debug("");
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kpu.kb.getProject());
		configuration.setDirectOwnSlotValues(kpu.getActiveFiltersSlot(), activeFilters);
	}

	public void addActiveFilter(SimpleInstance filter) {
		logger.debug("");
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kpu.kb.getProject());
		configuration.addOwnSlotValue(kpu.getActiveFiltersSlot(), filter);
	}

	public boolean getFadeUnselectedAnnotations() {
		logger.debug("");
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kpu.kb.getProject());
		Boolean fadeUnselectedAnnotations = (Boolean) configuration.getDirectOwnSlotValue(kpu
				.getFadeUnselectedAnnotationsSlot());
		if (fadeUnselectedAnnotations == null)
			return false;
		return fadeUnselectedAnnotations;
	}
	
	public boolean getConsensusAcceptRecursive() {
		logger.debug("");
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kpu.kb.getProject());
		Boolean consensusAcceptRecursive = (Boolean) configuration.getDirectOwnSlotValue(kpu
				.getConsensusAcceptRecursiveSlot());
		if (consensusAcceptRecursive == null)
			return false;
		return consensusAcceptRecursive;
	}
	
	
	public boolean getNextAnnotationOnDelete() {
		logger.debug("");
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kpu.kb.getProject());
		Boolean nextAnnotationOnDelete = (Boolean) configuration.getDirectOwnSlotValue(kpu
				.getNextAnnotationOnDeleteSlot());
		if (nextAnnotationOnDelete == null) {
			return false;
		}
		
		return nextAnnotationOnDelete;
	}

	public Slot getSubtextSlot() {
		logger.debug("");
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kpu.kb.getProject());
		return (Slot) configuration.getDirectOwnSlotValue(kpu.getSubtextSlotSlot());
	}

	public void restartConsensusMode() throws ConsensusException {
		logger.debug("");
		if (isConsensusMode()) {
			consensusSet = ConsensusAnnotations.recreateConsensusAnnotations(textSourceUtil.getTextSourceInstance(
					textSourceUtil.getCurrentTextSource(), false), getSelectedAnnotationSet(), this);
		}
	}

	public boolean isConsensusMode() {
		logger.debug("");
		return consensusMode;
	}
	
	/**	 
	 * Finds out if Knowtator is currently in required mode or not
	 * 
	 * @return True if Knowtator is in required mode, returns false otherwise.
	 */
	public boolean isRequiredMode() {
		return requiredMode;
	}
	
	/**
	 * Sets the required mode property, toggling required mode
	 * 
	 * @param requiredMode
	 */
	public void setRequiredMode( boolean requiredMode ) {
		this.requiredMode = requiredMode;

		try {
			ActionFactory.getInstance( this ).getAction( ActionFactory.ACTION_REQUIRED_MODE_NEXT_ACTION ).setEnabled( requiredMode );
		} catch( ActionNotFoundException e ) {
			e.printStackTrace();
		}
		
		setSelectedAnnotation( null );
	}

	public int getConsensusModeProgress() {
		logger.debug("");
		if (isConsensusMode()) {
			List<SimpleInstance> annotations = getCurrentFilteredAnnotations();
			SimpleInstance teamAnnotator = consensusSet.getTeamAnnotator();
			int progress = 0;
			for (SimpleInstance annotation : annotations) {
				if (teamAnnotator.equals(annotationUtil.getAnnotator(annotation)))
					progress++;
			}
			return progress;
		} else
			return -1;
	}	
	
	/**
	 * Finds out if the annotator of the given annotation is the team annotation.
	 *    
	 * @param annotation The annotation to find out if it is part of the team annotation.
	 * 
	 * @return Returns true if and only if the annotator of the given annotation is the consensus
	 *   set team annotator. 
	 */
	public boolean isTeamAnnotation( SimpleInstance annotation ) {
		if( consensusSet != null ) {
			SimpleInstance teamAnnotator = consensusSet.getTeamAnnotator();
			if( teamAnnotator.equals( annotationUtil.getAnnotator( annotation ) ) ) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Filters the team annotations out of the given list of annotations
	 * 
	 * @param annotationList
	 * @return
	 */
	public List<SimpleInstance> filterTeamAnnotations( List<SimpleInstance> annotationList ) {
		
		List<SimpleInstance> returnList = new ArrayList<SimpleInstance>();
		
		for( SimpleInstance annotation : annotationList ) {
			if( !isTeamAnnotation( annotation ) ) {
				returnList.add( annotation );
			}
		}
		
		return returnList;
	}

	public void textSourceChanged(TextSourceChangeEvent event) {
		logger.debug("");
		updateCurrentAnnotations();
		setSelectedAnnotation(null);

		if (isConsensusMode()) {
			try {
				Set<SimpleInstance> consensusAnnotations = new HashSet<SimpleInstance>(getCurrentFilteredAnnotations());
				SimpleInstance consensusSetInstance = (SimpleInstance) getSelectedFilter().getOwnSlotValue(
						kpu.getFilterSetSlot());
				consensusSet = new ConsensusSet(consensusAnnotations, consensusSetInstance, this);
			} catch (ConsensusException ce) {
				ce.printStackTrace();
			}
		}
		refreshAnnotationsDisplay(true);
	}

	public void consolidateAnnotation(SimpleInstance deleteAnnotation, SimpleInstance consolidateAnnotation) {
		logger.debug("");
		if(isConsensusMode()) {
			consensusSet.consolidateAnnotations(consolidateAnnotation, deleteAnnotation);
			setSelectedAnnotation(consolidateAnnotation);
			setSelectedConsensusAnnotation( null );
		}
	}

	public void consolidateAnnotations() {
		logger.debug("");
		if(isConsensusMode()) {
			consensusSet.consolidateAnnotations();
		}
	}

	public void setTextViewer(TextViewer textViewer) {
		logger.debug("");
		this.textViewer = textViewer;
	}

	public boolean isAnnotationVisible(SimpleInstance annotation) {
		logger.debug("");
		return textViewer.isVisible(annotation);
	}

	public Span getVisibleSpan() {
		logger.debug("");
		if (textViewer == null)
			return null;
		return textViewer.getVisibleSpan();
	}

	public int getVerticalDistance(SimpleInstance annotation1, SimpleInstance annotation2) {
		logger.debug("");
		return textViewer.getVerticalDistance(annotation1, annotation2);
	}
	
	public boolean isSelectedAnnotation( SimpleInstance annotation ) {
		return selectedAnnotation == annotation;		
	}
	
	public boolean isSelectedConsensusAnnotation( SimpleInstance annotation ) {
		return selectedConsensusAnnotation == annotation;
	}
}
