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
 *   Brant Barney
 */

package edu.uchsc.ccp.knowtator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.event.SpanEditEvent;
import edu.uchsc.ccp.knowtator.event.SpanEditListener;
import edu.uchsc.ccp.knowtator.textsource.TextSource;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;
import edu.uchsc.ccp.knowtator.util.ProtegeUtil;

/**
 * Authors: Philip V. Ogren Created: September, 2004 Description: This class
 * aids in creating, editing and retrieving annotation intances as well as the
 * supporting annotation classes. The annotation model is defined in
 * annotations.pprj. This project must be included when the annotator plug-in is
 * used. Todo: AnnotationUtil should really not have an instance of knowtator as
 * a member variable. I need to create the appropriate listeners, so that
 * Knowtator can take care of itself as it see fits. Changes: 02/28/2005 added
 * annotation filter functionality 05/02/2005 package changed to ...knowtator
 * 8/11/2005 pvo added getReferencedAnnotations method 10/04/2005 Changed
 * signature of main createAnnotations method so that a string for the spanned
 * text can be passed in. This removes the necessity of gathering the spanned
 * text from the actual text source which may be an unnecessary task if you are
 * copying an annotation (e.g. as in MergeAnnotations). This change had a small
 * ripple effect on other methods: setSpans, updateAnnotationText 10/04/2005
 * Changed name of deleteAnnotationMention to deleteMention (I just didn't like
 * the name!)
 */

public class AnnotationUtil implements SpanEditListener {

	KnowtatorManager manager;

	KnowtatorProjectUtil kpu;

	KnowledgeBase kb;

	Project project;

	TextSourceUtil textSourceUtil;

	MentionUtil mentionUtil;

	Map<SimpleInstance, java.util.List<Span>> annotationSpansCache;

	public AnnotationUtil(KnowtatorManager manager) {
		this.manager = manager;
		kpu = manager.getKnowtatorProjectUtil();
		kb = manager.getKnowledgeBase();
		this.project = kb.getProject();
		annotationSpansCache = new HashMap<SimpleInstance, java.util.List<Span>>();
		// EventHandler.getInstance().addSpanEditListener(this); //not needed
		// because SpanUtil updates list retrieved from getSpans()
	}

	public void setTextSourceUtil(TextSourceUtil textSourceUtil) {
		this.textSourceUtil = textSourceUtil;
	}

	public void setMentionUtil(MentionUtil mentionUtil) {
		this.mentionUtil = mentionUtil;
	}

	/**
	 * This method deletes the mention of annotation.
	 * 
	 * Although it is not possible with the current interface to associate a
	 * mention with more than one annotation, there is no other reason why this
	 * can't happen. If a mention is associated with more than one annotation,
	 * then the mention will not be deleted.
	 * 
	 * @param annotation
	 *            the mention of this annotation will be deleted via
	 *            mentionUtil.deleteMention.
	 * 
	 */

	public void deleteMention(SimpleInstance annotation) {
		SimpleInstance mention = getMention(annotation);

		if (mention != null) {
			mentionUtil.deleteMention(mention);
		}

	}

	public void spanEditted(SpanEditEvent see) {
		annotationSpansCache.remove(see.getAnnotation());
	}

	/**
	 * Returns a list of Span objects that correspond to the span values given
	 * to an annotation instance. The spans will be ordered as defined by the
	 * Comparable interface implementation in Span. If a span value from an
	 * annotation does not parse (i.e. has a string value that is not in the
	 * correct span format), then an InvalidSpanException will be thrown.
	 * 
	 * @param annotation
	 *            the spans for the annotation will be returned
	 * @return a list of spans corresponding to the annotation's
	 *         kpu.getAnnotationSpanSlot()
	 */
	public java.util.List<Span> getSpans(SimpleInstance annotation) throws InvalidSpanException {
		return getSpans(annotation, false);
	}

	/**
	 * Returns a list of Span objects that correspond to the span values given
	 * to an annotation instance. The spans will be ordered as defined by the
	 * Comparable interface implementation in Span. If a span value from an
	 * annotation does not parse (i.e. has a string value that is not in the
	 * correct span format), then an InvalidSpanException will be thrown.
	 * 
	 * @param annotation
	 *            the spans for the annotation will be returned
	 * @return a list of spans corresponding to the annotation's
	 *         kpu.getAnnotationSpanSlot()
	 */
	public java.util.List<Span> getSpans(SimpleInstance annotation, boolean ignoreCache) throws InvalidSpanException {
		if (annotation == null)
			return Collections.emptyList();
		if (annotationSpansCache.containsKey(annotation) && !ignoreCache) {
			return annotationSpansCache.get(annotation);
		}

		Collection<String> spanStrings = ProtegeUtil.castStrings(annotation.getOwnSlotValues(kpu.annotationSpanSlot));

		ArrayList<Span> spans = new ArrayList<Span>();
		for (String spanString : spanStrings) {
			spans.add(Span.parseSpan(spanString));
		}

		Collections.sort(spans);
		annotationSpansCache.put(annotation, spans);
		return spans;
	}

	/**
	 * Sets the spans slot for an annotation instance. If a
	 * 
	 * @param annotation
	 *            set the span slot for the passed in annotation
	 * @param spans
	 *            a list of spans for the annotation. This will typically be a
	 *            list of 1 span object, but may occasionally have a 2 or more
	 *            spans.
	 * @param spannedText
	 *            if null, then the text from the text source for the annotation
	 *            will be used to determine the spannedText. If the spannedText
	 *            is already known, then it may be useful to pass in the
	 *            spannedText. Passing in an empty string will cause the
	 *            spannedText to be an empty string.
	 */
	public void setSpans(SimpleInstance annotation, java.util.List<Span> spans, String spannedText)
			throws TextSourceAccessException {
		ArrayList<String> spanStrings = new ArrayList<String>(spans.size());
		for (Span span : spans) {
			spanStrings.add(span.toString());
		}
		annotation.setOwnSlotValues(kpu.annotationSpanSlot, spanStrings);
		if (spannedText == null && spans.size() > 0)
			updateSpannedText(annotation, spans);
		else {
			setText(annotation, spannedText);
		}
		annotationSpansCache.put(annotation, spans);
		
		//Fire the span edited event. Needed so the consensus mode red border can be updated
		//  when the span is changed from the span widget.
		EventHandler.getInstance().fireSpanEditted(annotation);
	}

	/**
	 * updates the spanned text of an annotation of a text source. This is does
	 * this by calling TextSource.getText(spans). If TextSource cannot be found
	 * using TextSourceUtil.getTextSource(TextSource), then a
	 * TextSourceAccessException is thrown.
	 * 
	 * @param annotation
	 *            the annotation whose spanned text is being updated
	 * @param spans
	 *            the spans corresponding to offsets in the text source.
	 */
	private void updateSpannedText(SimpleInstance annotation, java.util.List<Span> spans)
			throws TextSourceAccessException {
		SimpleInstance textSource = getTextSource(annotation);
		if (textSource != null) {
			TextSource ts = textSourceUtil.getTextSource(textSource);
			String spannedText = ts.getText(spans);
			setText(annotation, spannedText);
		}
	}

	/**
	 * Returns the "annotation" instances associated with a TextSource if the
	 * TextSource does not have a corresponding "text source" instance in the
	 * knowledgebase, then null is returned. If the "text source" instance does
	 * exist then all of the "annotation" instances are found and returned. If
	 * none are found, an empty array of instances is returned.
	 */

	public List<SimpleInstance> getAnnotations(TextSource textSource) {
		SimpleInstance textSourceInstance = textSourceUtil.getTextSourceInstance(textSource, false);
		if (textSourceInstance == null)
			return null;
		else
			return getAnnotations(textSourceInstance);
	}

	public List<SimpleInstance> getAnnotations(String textSourceName) {
		SimpleInstance textSourceInstance = (SimpleInstance) kb.getInstance(textSourceName);
		if (textSourceInstance != null) {
			return getAnnotations(textSourceInstance);
		} else {
			return null;
		}
	}

	/**
	 * Does the work of finding all "annotation" instances that have the
	 * textSourceInstance as the value of "annotation_text_source" slot.
	 */
	public List<SimpleInstance> getAnnotations(SimpleInstance textSourceInstance) {
		Collection<Instance> annotations = kb.getInstances(kpu.getAnnotationCls());
		List<SimpleInstance> returnValues = new ArrayList<SimpleInstance>();
		for (Instance ann : annotations) {
			SimpleInstance annotation = (SimpleInstance) ann;
			SimpleInstance ts = getTextSource(annotation);
			if (ts != null && ts.equals(textSourceInstance)) {
				returnValues.add(annotation);
			}
		}
		return returnValues;
	}

	public SimpleInstance createAnnotation(Cls annotationCls, java.util.List<Span> spans, String spannedText,
			String textSourceName) throws TextSourceAccessException {
		return createAnnotation(annotationCls, spans, spannedText, textSourceName, null, null);
	}

	public SimpleInstance createAnnotation(Cls annotationCls, java.util.List<Span> spans, String spannedText,
			String textSourceName, SimpleInstance annotator, SimpleInstance set) throws TextSourceAccessException {
		SimpleInstance mention = mentionUtil.createMention(annotationCls);
		SimpleInstance textSource = kb.getSimpleInstance(textSourceName);
		if (textSource == null)
			textSource = kb.createSimpleInstance(new FrameID(textSourceName), CollectionUtilities.createCollection(kpu.getTextSourceCls()),
					true);

		Collection<SimpleInstance> annotationSets = new ArrayList<SimpleInstance>();
		if (set != null) {
			annotationSets.add(set);
		}
		return createAnnotation(mention, annotator, spans, spannedText, textSource, annotationSets);
	}

	/**
	 * If the textSource does not have a corresponding instance in the kb, then
	 * one will be created.
	 */

	public SimpleInstance createAnnotation(SimpleInstance mention, SimpleInstance annotator,
			java.util.List<Span> spans, TextSource textSource, SimpleInstance annotationSet)
			throws TextSourceAccessException {
		SimpleInstance textSourceInstance = textSourceUtil.getTextSourceInstance(textSource, true);

		return createAnnotation(mention, annotator, spans, textSourceInstance, annotationSet);
	}

	public SimpleInstance createAnnotation(SimpleInstance mention, SimpleInstance annotator,
			java.util.List<Span> spans, SimpleInstance textSourceInstance, SimpleInstance annotationSet)
			throws TextSourceAccessException {
		Collection<SimpleInstance> annotationSets = new ArrayList<SimpleInstance>();
		if (annotationSet != null) {
			annotationSets.add(annotationSet);
		}

		return createAnnotation(mention, annotator, spans, null, textSourceInstance, annotationSets);
	}

	public SimpleInstance createAnnotation(SimpleInstance mention, SimpleInstance annotator,
			java.util.List<Span> spans, String spannedText, SimpleInstance textSourceInstance,
			java.util.Collection<SimpleInstance> annotationSets) throws TextSourceAccessException {
		SimpleInstance annotationInstance = kb.createSimpleInstance(new FrameID(null),  CollectionUtilities
				.createCollection(kpu.annotationCls), true);
		if (mention != null) {
			annotationInstance.setOwnSlotValue(kpu.annotatedMentionSlot, mention);
		}

		if (annotator != null) {
			annotationInstance.setOwnSlotValue(kpu.annotationAnnotatorSlot, annotator);
		}

		if (annotationSets != null && annotationSets.size() > 0) {
			annotationInstance.setOwnSlotValues(kpu.setSlot, annotationSets);
		}

		if (textSourceInstance != null) {
			annotationInstance.setOwnSlotValue(kpu.annotationTextSourceSlot, textSourceInstance);
		}

		setSpans(annotationInstance, spans, spannedText);

		return annotationInstance;

	}

	/**
	 * Creates an annotation. Overloaded to include the creation date in the
	 * annotation. Designed to be used for creating the annotation while
	 * importing an XML file.
	 * 
	 * @param creationDate
	 *            A String representation of the date (time stamp) the
	 *            annotation was created.
	 * 
	 * @throws TextSourceAccessException
	 */
	public SimpleInstance createAnnotation(SimpleInstance mention, SimpleInstance annotator,
			java.util.List<Span> spans, String spannedText, SimpleInstance textSourceInstance,
			java.util.Collection<SimpleInstance> annotationSets, String creationDate) throws TextSourceAccessException {

		SimpleInstance annotationInstance = createAnnotation(mention, annotator, spans, spannedText,
				textSourceInstance, annotationSets);

		if (creationDate != null) {
			setCreationDate(annotationInstance, creationDate);
		}

		return annotationInstance;
	}

	/**
	 * This method returns true if two annotations have exactly the same spans.
	 */
	public boolean compareSpans(SimpleInstance annotation1, SimpleInstance annotation2) {
		try {
			java.util.List<Span> spans1 = getSpans(annotation1);
			java.util.List<Span> spans2 = getSpans(annotation2);

			return Span.spansMatch(spans1, spans2);
		} catch (InvalidSpanException ise) {
			return false;
		}
	}

	public boolean compareSpans(List<SimpleInstance> annotations) {
		for (int i = 1; i < annotations.size(); i++) {
			if (!compareSpans(annotations.get(0), annotations.get(i)))
				return false;
		}
		return true;
	}

	public boolean isAnnotation(SimpleInstance annotation) {
		if (annotation == null)
			return false;

		Cls type = annotation.getDirectType();
		if (type == null)
			return false;
		if (type.equals(kpu.getAnnotationCls())) {
			return true;
		}
		return type.getSuperclasses().contains(kpu.getAnnotationCls());
	}

	public boolean hasTeamAnnotator(SimpleInstance annotation) {
		SimpleInstance annotator = getAnnotator(annotation);
		if (annotator == null)
			return false;
		Cls annotatorType = annotator.getDirectType();
		if (annotatorType == null)
			return false;
		if (annotatorType.equals(kpu.getTeamAnnotatorCls()))
			return true;
		return annotatorType.getSuperclasses().contains(kpu.getTeamAnnotatorCls());

	}

	public void setProjectAnnotator(SimpleInstance annotation) {
		if (isAnnotation(annotation)) {
			setAnnotator(annotation, manager.getSelectedAnnotator());
		}
	}

	public void setProjectAnnotationSet(SimpleInstance annotation) {
		if (isAnnotation(annotation)) {
			setSet(annotation, manager.getSelectedAnnotationSet());
		}
	}

	/**
	 * Returns all annotations that are related to the passed in annotation. If
	 * an annotation is the slot value of another annotation (or more correctly
	 * if the mention of an annotation is a slot mention's value of another
	 * annotation's mention).
	 * 
	 * This method recursively gathers all annotations that are related (not
	 * just directly related).
	 */

	public Set<SimpleInstance> getRelatedAnnotations(SimpleInstance annotation) {
		Set<SimpleInstance> referencedAnnotations = new HashSet<SimpleInstance>();
		_getRelatedAnnotations(annotation, referencedAnnotations);
		return referencedAnnotations;
	}

	private void _getRelatedAnnotations(SimpleInstance annotation, Set<SimpleInstance> referencedAnnotations) {
		SimpleInstance mentionInstance = getMention(annotation);
		if (mentionInstance != null) {
			List<SimpleInstance> referencedMentions = mentionUtil.getRelatedMentions(mentionInstance);
			for (SimpleInstance referencedMention : referencedMentions) {
				SimpleInstance ann = mentionUtil.getMentionAnnotation(referencedMention);
				if (!referencedAnnotations.contains(ann)) {
					referencedAnnotations.add(ann);
					_getRelatedAnnotations(ann, referencedAnnotations);
				}
			}
		}
	}

	public List<SimpleInstance> retrieveAllAnnotations() {
		Collection<SimpleInstance> instances = ProtegeUtil.castSimpleInstances(kpu.getAnnotationCls().getInstances());
		List<SimpleInstance> annotations = new ArrayList<SimpleInstance>(instances);
		return annotations;
	}

	public void setAnnotator(SimpleInstance annotation, SimpleInstance annotator) {
		if (isAnnotation(annotation) && annotator != null) {
			annotation.setOwnSlotValue(kpu.getAnnotationAnnotatorSlot(), annotator);
		}
	}

	public void setSet(SimpleInstance annotation, SimpleInstance set) {
		Collection<SimpleInstance> sets = new ArrayList<SimpleInstance>();
		sets.add(set);
		setSets(annotation, sets);
	}

	public void setSets(SimpleInstance annotation, Collection<SimpleInstance> sets) {
		if (sets == null) {
			sets = Collections.emptyList();
		}
		if (isAnnotation(annotation)) {
			annotation.setOwnSlotValues(kpu.getSetSlot(), sets);
		}
	}

	public Set<SimpleInstance> getSets(SimpleInstance annotation) {
		if (isAnnotation(annotation)) {
			Collection<SimpleInstance> sets = ProtegeUtil.castSimpleInstances(annotation.getOwnSlotValues(kpu
					.getSetSlot()));
			if (sets != null) {
				return new HashSet<SimpleInstance>(sets);
			}
		}
		return Collections.emptySet();
	}

	public SimpleInstance getTextSource(SimpleInstance annotation) {
		if (isAnnotation(annotation)) {
			SimpleInstance textSource = (SimpleInstance) annotation.getOwnSlotValue(kpu.getAnnotationTextSourceSlot());
			return textSource;
		}
		return null;
	}

	public void setTextSource(SimpleInstance annotation, SimpleInstance textSource) {
		if (isAnnotation(annotation)) {
			annotation.setOwnSlotValue(kpu.getAnnotationTextSourceSlot(), textSource);
		}
	}

	public String getText(SimpleInstance annotation) {
		if (isAnnotation(annotation)) {
			return (String) annotation.getOwnSlotValue(kpu.getAnnotationTextSlot());
		}
		return null;
	}

	public void setText(SimpleInstance annotation, String spannedText) {
		if (isAnnotation(annotation)) {
			annotation.setOwnSlotValue(kpu.getAnnotationTextSlot(), spannedText);
		}
	}

	public boolean spansOverlap(SimpleInstance annotation1, SimpleInstance annotation2) {
		try {
			List<Span> spans1 = getSpans(annotation1);
			List<Span> spans2 = getSpans(annotation2);
			return Span.intersects(spans1, spans2);
		} catch (InvalidSpanException ise) {
			return false;
		}
	}

	/**
	 * @return the size of the span associated with the annotation. If the
	 *         annotation has more than one span, then the sum of the size of
	 *         the spans is returned.
	 * @throws InvalidSpanException
	 *             if the annotation has a badly formed span, then an exception
	 *             will be thrown.
	 */

	public int getSize(SimpleInstance annotation) throws InvalidSpanException {
		List<Span> spans = getSpans(annotation);
		int size = 0;
		for (Span span : spans) {
			size += span.getSize();
		}
		return size;
	}

	/**
	 * This method returns the shortest annotation - that is the annotation
	 * whose span is the shortest. If an annotation has more than one span, then
	 * its size is the sum of the size of each of its spans.
	 * 
	 * @param annotations
	 * @return will only return one annotation. In the case of a tie, will
	 *         return the first annotation with the smallest size encountered
	 *         during iteration. Returns null if annotation.size() == 0 or if
	 *         each of the annotations has poorly formed spans (very unlikely).
	 */

	public SimpleInstance getShortestAnnotation(Collection<SimpleInstance> annotations) {
		if (annotations.size() == 0)
			return null;

		SimpleInstance shortestAnnotation = null;
		int shortestAnnotationLength = -1;

		for (SimpleInstance annotation : annotations) {
			try {
				int annotationSize = getSize(annotation);
				if (shortestAnnotationLength == -1 || annotationSize < shortestAnnotationLength) {
					shortestAnnotation = annotation;
					shortestAnnotationLength = annotationSize;
				}
			} catch (InvalidSpanException ise) {
				continue;
			}
		}
		return shortestAnnotation;
	}

	public SimpleInstance getMention(SimpleInstance annotation) {
		if (isAnnotation(annotation)) {
			SimpleInstance mention = (SimpleInstance) annotation.getOwnSlotValue(kpu.getAnnotatedMentionSlot());
			return mention;
		}
		return null;
	}

	public void setMention(SimpleInstance annotation, SimpleInstance mention) {
		if (isAnnotation(annotation) && mentionUtil.isMention(mention)) {
			annotation.setOwnSlotValue(kpu.getAnnotatedMentionSlot(), mention);
		}
	}

	public SimpleInstance getAnnotator(SimpleInstance annotation) {
		if (isAnnotation(annotation)) {
			return (SimpleInstance) annotation.getOwnSlotValue(kpu.getAnnotationAnnotatorSlot());
		}
		return null;
	}

	public String getComment(SimpleInstance annotation) {
		if (isAnnotation(annotation)) {
			return (String) annotation.getOwnSlotValue(kpu.getAnnotationCommentSlot());
		}
		return null;
	}

	public void setComment(SimpleInstance annotation, String comment) {
		if (isAnnotation(annotation)) {
			annotation.setOwnSlotValue(kpu.getAnnotationCommentSlot(), comment);
		}
	}

	public String getCreationDate(SimpleInstance annotation) {
		if (isAnnotation(annotation)) {
			return (String) annotation.getOwnSlotValue(kpu.getAnnotationCreationDateSlot());
		}
		return null;
	}

	public void setCreationDate(SimpleInstance annotation, String creationDate) {
		if (isAnnotation(annotation)) {
			annotation.setOwnSlotValue(kpu.getAnnotationCreationDateSlot(), creationDate);
		}
	}

}
