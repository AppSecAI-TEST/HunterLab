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
package edu.uchsc.ccp.knowtator.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.FilterUtil;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.MentionUtil;

/**
 * 
 * @author Philip V. Ogren
 * 
 *        This class does not represent a consensus set of annotations. It
 *         serves a class that helps manage a set of consensus annotations. When
 *         the menu item 'Create Consensus Set' is run an instance of 'knowtator
 *         set' will be created. Annotations in this set define a consensus set.
 *         When redundant annotations exist in the set we want to consolidate
 *         them. This is done by identifying that there is an annotation from
 *         each of the individual annotators that is exactly the same. We can
 *         then change the annotator to one of the annotations to the team
 *         annotator and discard the others. This class facilitates the
 *         consolidation of such redundant annotations - typically one text
 *         source at a time.
 */

public class ConsensusSet {
	Logger logger = Logger.getLogger(ConsensusSet.class);

	// A collection of all of the annotations in the ConsensusSet
	Set<SimpleInstance> annotations;

	// A collection of all of the mentions in the ConsensusSet
	Set<SimpleInstance> mentions;

	// contains each of the individual annotators in the ConsensusSet.
	// These are obtained from the filter passed into the constructor.
	Set<SimpleInstance> annotators;

	// The team annotator that consists of each of the annotators found
	// in the annotators member variable (previous).
	SimpleInstance teamAnnotator;

	// key is an annotator, value is a set of annotations by the annotator.
	Map<SimpleInstance, Set<SimpleInstance>> annotatorAnnotations;

	// key is a class or instance mention that is a value of a slot mention. The
	// value is a complex slot mention.
	Map<SimpleInstance, Set<SimpleInstance>> slotValueToComplexMention;

	KnowtatorManager manager;

	AnnotationUtil annotationUtil;

	MentionUtil mentionUtil;

	KnowtatorProjectUtil kpu;

	KnowledgeBase kb;

//	ConsensusFrameAdapter frameListener;

	boolean consolidatingAnnotations = false;

	/**
	 * @param annotations
	 *            some subset of the annotations that are found in a consensus
	 *            set. Typically, all of the annotations corresponding to a
	 *            single text source that pass through the consensusFilter
	 * @throws ConsensusException
	 */
	/**
	 * 
	 */
	public ConsensusSet(Set<SimpleInstance> annotations, SimpleInstance consensusSet, KnowtatorManager manager)
			throws ConsensusException {
		logger.debug("creating consensus set");
		this.annotations = annotations;
		this.manager = manager;
		this.annotationUtil = manager.getAnnotationUtil();
		this.mentionUtil = manager.getMentionUtil();
		this.kpu = manager.getKnowtatorProjectUtil();
		this.kb = manager.getKnowledgeBase();

		this.teamAnnotator = (SimpleInstance) consensusSet.getOwnSlotValue(kpu.getConsensusSetTeamAnnotatorSlot());
		SimpleInstance individualFilter = (SimpleInstance) consensusSet.getOwnSlotValue(kpu
				.getConsensusSetIndividualFilterSlot());
		this.annotators = new HashSet<SimpleInstance>(FilterUtil.getAnnotators(individualFilter));

		/*
		 * Throw some exceptions if the annotators provided by the filter do not
		 * makes sense.
		 */
		if (teamAnnotator == null)
			throw new ConsensusException("There is no team annotator for this consensus set."
					+ "\nPlease make sure that the provided filter has" + "\na single team annotator.");
		if (annotators.size() < 2)
			throw new ConsensusException("There are less than two annotators for this "
					+ "\nconsensus set.  Please make sure that the " + "\nprovided filter has two or more individual "
					+ "\nannotators.");

		Collection<SimpleInstance> teamAnnotators = (Collection<SimpleInstance>) teamAnnotator.getOwnSlotValues(kpu
				.getAnnotatorTeamMembersSlot());
		if (teamAnnotators.size() != annotators.size())
			throw new ConsensusException("The team annotator provided by the filter is "
					+ "\nnot consistent with the individual annotators " + "\nalso specified in the filter." + "\n "
					+ teamAnnotators.size() + " annotators specified " + "\nin team annotator and " + annotators.size()
					+ " " + "\nspecified by the filter passed into the consensus " + "\nset.");
		for (SimpleInstance tmAnnotator : teamAnnotators) {
			if (!annotators.contains(tmAnnotator))
				throw new ConsensusException("The team annotator provided by the filter is "
						+ "\nnot consistent with the individual annotators " + "\nalso specified in the filter"
						+ "\n TeamAnnotator=" + teamAnnotator.getBrowserText() + "\nand missing annotator="
						+ tmAnnotator.getBrowserText());
		}

		// collect all of the mentions associated with the annotations and
		// populate annotatorAnnotations
		mentions = new HashSet<SimpleInstance>();
		annotatorAnnotations = new HashMap<SimpleInstance, Set<SimpleInstance>>();
//		frameListener = new ConsensusFrameAdapter();
		for (SimpleInstance annotation : annotations) {
//			annotation.addFrameListener(frameListener);
			SimpleInstance mention = annotationUtil.getMention(annotation);
			mentions.addAll(mentionUtil.getAllConnectedMentions(mention));

			SimpleInstance annotator = annotationUtil.getAnnotator(annotation);
			if (!annotators.contains(annotator) && !annotator.equals(teamAnnotator))
				throw new ConsensusException(
						"An annotation in this set was created by an annotator other than the annotators specified by the filter or the team annotators"
								+ "\n The offending annotations is \""
								+ annotation.getBrowserText()
								+ "\" with annotator = " + annotator.getBrowserText());
			if (!annotatorAnnotations.containsKey(annotator)) {
				annotatorAnnotations.put(annotator, new HashSet<SimpleInstance>());
			}
			annotatorAnnotations.get(annotator).add(annotation);
		}

		slotValueToComplexMention = new HashMap<SimpleInstance, Set<SimpleInstance>>();
		for (SimpleInstance mention : mentions) {
//			mention.addFrameListener(frameListener);

			if (mentionUtil.isComplexSlotMention(mention)) {
				java.util.List<Object> slotValues = mentionUtil.getSlotMentionValues(mention);
				if (slotValues.size() > 0) {
					for (Object slotValue : slotValues) {
						SimpleInstance slotValueInstance = (SimpleInstance) slotValue;
						if (!slotValueToComplexMention.containsKey(slotValueInstance))
							slotValueToComplexMention.put(slotValueInstance, new HashSet<SimpleInstance>());
						slotValueToComplexMention.get(slotValueInstance).add(mention);
					}
				}
			}
		}
	}

	/**
	 * This method finds all complex slot mentions that have originalMention as
	 * one of its values and 'replaces' it with newMention. The originalMention
	 * will always be removed from the set of slot values of the complex slot
	 * mention. However, newMention will only be added to the set of slot values
	 * only if it is not already there.
	 * 
	 * @param originalMention
	 * @param newMention
	 */

	private void replaceSlotValue(SimpleInstance originalMention, SimpleInstance newMention) {
		if (slotValueToComplexMention.containsKey(originalMention)) {
			Set<SimpleInstance> complexMentions = slotValueToComplexMention.get(originalMention);
			for (SimpleInstance complexMention : complexMentions) {
				java.util.List<Object> complexMentionValues = mentionUtil.getSlotMentionValues(complexMention);
				if (!complexMentionValues.contains(newMention))
					mentionUtil.addValueToSlotMention(complexMention, newMention);
				mentionUtil.removeValueFromSlotMention(complexMention, originalMention);
			}
		}
	}


	public void consolidateAnnotations(SimpleInstance consensusAnnotation, SimpleInstance redundantAnnotation) {
		consolidateAnnotations(consensusAnnotation, redundantAnnotation, manager.getConsensusAcceptRecursive());
	}
		/**
	 * This method consolidates two anntotations into one. It does this by
	 * removing one annotation from the consensus set (the redundantAnnotation)
	 * and making the other as annotated by the 'Team Annotator'. One of the
	 * important things that is done before the redundant annotation is deleted
	 * is to have the complex slot mentions whose value is the redundant
	 * annotation change their value to the consensusAnnotation. This is done
	 * with a call to replaceSlotValue.
	 * 
	 * @param consensusAnnotation
	 *            the annotation that will stick around
	 * @param redundantAnnotation
	 *            the annotation that is going away
	 * @see #replaceSlotValue(SimpleInstance, SimpleInstance)
	 */
	public void consolidateAnnotations(SimpleInstance consensusAnnotation, SimpleInstance redundantAnnotation, boolean recurse) {

		SimpleInstance consensusMention = annotationUtil.getMention(consensusAnnotation);
		
		if(recurse) {
			Set<SimpleInstance> consensusAnnotations = annotationUtil.getRelatedAnnotations(consensusAnnotation);
			for(SimpleInstance ca : consensusAnnotations) {
				annotationUtil.setAnnotator(ca, teamAnnotator);
			}
			
			if(redundantAnnotation != null) {
				Set<SimpleInstance> redundantAnnotations = annotationUtil.getRelatedAnnotations(redundantAnnotation);
				for(SimpleInstance ra : redundantAnnotations) {
					annotations.remove(ra);
					SimpleInstance annotator = annotationUtil.getAnnotator(ra);
					if (annotator != null && !annotator.equals(teamAnnotator)) {			
						Set<SimpleInstance> set = annotatorAnnotations.get(annotator);
						if( set != null ) {
							set.remove( redundantAnnotation );
						}			
						manager.deleteAnnotation(ra);
					}
				}
			}
		}
		// set annotator of consensus annotation to the team annotator.
		annotationUtil.setAnnotator(consensusAnnotation, teamAnnotator);

		// remove redundantAnnotation from ConsensusSet member variables
		annotations.remove(redundantAnnotation);
		SimpleInstance annotator = annotationUtil.getAnnotator(redundantAnnotation);
		if (annotator != null) {			
			Set<SimpleInstance> set = annotatorAnnotations.get(annotator);
			if( set != null ) {
				set.remove( redundantAnnotation );
			}			
		}

		// replace slot mentions/values that correspond to the redundant
		// annotation with the slot mention/value of the consensus annotation
		SimpleInstance redundantMention = annotationUtil.getMention(redundantAnnotation);
		replaceSlotValue(redundantMention, consensusMention);
		// remove redundantMention from ConsensusSet member variables
		mentions.remove(redundantMention);
		slotValueToComplexMention.remove(redundantMention);

//		if(redundantMention != null)
//			redundantMention.removeFrameListener(frameListener);
		if(redundantMention != null) {
//			redundantAnnotation.removeFrameListener(frameListener);
			manager.deleteAnnotation(redundantAnnotation);
		}
	}

	/**
	 * This method finds annotations that are 'redundant' in the consensus set.
	 * An annotation is considered redundant if it is created by an individual
	 * annotator and it is the same as an annotation from each of the other
	 * annotators. Two annotations are the same if they have the same same span
	 * and the corresponding mentions of the annotations are identical.
	 * 
	 * @param annotation
	 *            we are looking for annotations that are exactly like this one.
	 * @return the annotations that are the same as the passed in annotation.
	 *         Does not contain the passed in annotation. Will not return null;
	 */
	private Set<SimpleInstance> getRedundantAnnotations(SimpleInstance annotation) {
		SimpleInstance mention = annotationUtil.getMention(annotation);
		SimpleInstance annotator = annotationUtil.getAnnotator(annotation);
		Set<SimpleInstance> matchedAnnotations = new HashSet<SimpleInstance>();

		for (SimpleInstance compareAnnotator : annotators) {
			if (!annotator.equals(compareAnnotator) && !annotator.equals(teamAnnotator)) {
				boolean annotatorMatched = false;
				Set<SimpleInstance> candidateAnnotations = annotatorAnnotations.get(compareAnnotator);
				if (candidateAnnotations == null) {
					matchedAnnotations.clear();
					return matchedAnnotations;
				}
				for (SimpleInstance candidateAnnotation : candidateAnnotations) {
					if (candidateAnnotation.isDeleted() || candidateAnnotation.isBeingDeleted())
						continue;
					if (strictMatch(annotation, candidateAnnotation, annotationUtil, mentionUtil)) {
						SimpleInstance matchMention = annotationUtil.getMention(candidateAnnotation);
						if (mentionUtil.equals(mention, matchMention, true)) {
							matchedAnnotations.add(candidateAnnotation);
							annotatorMatched = true;
							// add break here if you want to make sure that
							// annotation matches only one annotation from each
							// annotator.
							// It may be that the individual has created
							// redundant annotations.
						}
					}
				}
				if (!annotatorMatched) {
					matchedAnnotations.clear();
					return matchedAnnotations;
				}
			}
		}
		return matchedAnnotations;
	}

	private boolean strictMatch(SimpleInstance annotation1, SimpleInstance annotation2, AnnotationUtil annotationUtil,
			MentionUtil mentionUtil) {
		SimpleInstance mention1 = annotationUtil.getMention(annotation1);
		Cls mentionType1 = mentionUtil.getMentionCls(mention1);
		SimpleInstance mention2 = annotationUtil.getMention(annotation2);
		Cls mentionType2 = mentionUtil.getMentionCls(mention2);
		boolean typesMatch = false;
		if (mentionType1 == null && mentionType2 == null)
			typesMatch = true;
		else if (mentionType1 != null && mentionType1.equals(mentionType2))
			typesMatch = true;

		if (typesMatch && annotationUtil.compareSpans(annotation1, annotation2)) {
			return true;
		}
		return false;
	}

	public void consolidateAnnotations() {
		logger.debug("");
		try {
			consolidatingAnnotations = true;

			Set<SimpleInstance> excludeAnnotations = new HashSet<SimpleInstance>();
			// key is an annotation that we will keep, the values are redundant
			// copies
			Map<SimpleInstance, Set<SimpleInstance>> consolidatedAnnotations = new HashMap<SimpleInstance, Set<SimpleInstance>>();
			// key is a mention that can be removed/replaced with the value
			Map<SimpleInstance, SimpleInstance> consolidatedMentions = new HashMap<SimpleInstance, SimpleInstance>();

			// loop through all the annotations created for the consensus set
			// and look for identical/redundant annotations to remove.
			// this loop will populate consolidatedAnnotations and
			// consolidatedMentions
			for (SimpleInstance annotation : annotations) {
				if (annotation.isDeleted() || annotation.isBeingDeleted())
					continue;
				SimpleInstance annotator = annotationUtil.getAnnotator(annotation);
				if (annotator.equals(teamAnnotator))
					continue;
				if (!excludeAnnotations.contains(annotation)) {
					Set<SimpleInstance> redundantAnnotations = getRedundantAnnotations(annotation);
					if (redundantAnnotations == null)
						continue;
					if (redundantAnnotations.size() > 0) {
						consolidatedAnnotations.put(annotation, redundantAnnotations);
						excludeAnnotations.addAll(redundantAnnotations);
						SimpleInstance mention = annotationUtil.getMention(annotation);
						for (SimpleInstance redundantAnnotation : redundantAnnotations) {
							SimpleInstance redundantMention = annotationUtil.getMention(redundantAnnotation);
							consolidatedMentions.put(redundantMention, mention);
						}
					}
				}
			}

			for (SimpleInstance redundantMention : consolidatedMentions.keySet()) {
				replaceSlotValue(redundantMention, consolidatedMentions.get(redundantMention));
			}

			for (SimpleInstance consensusAnnotation : consolidatedAnnotations.keySet()) {
				Set<SimpleInstance> redundantAnnotations = consolidatedAnnotations.get(consensusAnnotation);
				for (SimpleInstance redundantAnnotation : redundantAnnotations) {
					consolidateAnnotations(consensusAnnotation, redundantAnnotation);
				}
			}
			consolidatingAnnotations = false;
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
		manager.updateCurrentAnnotations();
	}

	protected void finalize() {
//		for (SimpleInstance mention : mentions) {
//			mention.removeFrameListener(frameListener);
//		}
	}

	public void destroy() {
//		for (SimpleInstance mention : mentions) {
//			mention.removeFrameListener(frameListener);
//		}
//		for (SimpleInstance annotation : annotations) {
//			annotation.removeFrameListener(frameListener);
//		}
		annotations.clear();
		mentions.clear();
		annotators.clear();
		annotatorAnnotations.clear();
		slotValueToComplexMention.clear();
	}

	public class ConsensusFrameAdapter extends FrameAdapter {
		public void ownSlotValueChanged(FrameEvent event) {

			if (!consolidatingAnnotations) {
				Frame eventFrame = event.getFrame();

				// TODO looks like I need to also check for mentions here.
				if (eventFrame instanceof SimpleInstance) {
					SimpleInstance eventInstance = (SimpleInstance) eventFrame;
					if (annotationUtil.isAnnotation(eventInstance)) {
						SimpleInstance annotator = annotationUtil.getAnnotator(eventInstance);
						if (annotator != null && annotator.equals(teamAnnotator))
							return;
					}
				}
				Slot eventSlot = event.getSlot();
				if (eventSlot == null)
					return;
				if (eventSlot.equals(kpu.getMentionSlotSlot()) || eventSlot.equals(kpu.getMentionSlotValueSlot())
						|| eventSlot.equals(kpu.getMentionClassSlot())
						|| eventSlot.equals(kpu.getMentionInstanceSlot())
						|| eventSlot.equals(kpu.getAnnotatedMentionSlot())
						|| eventSlot.equals(kpu.getAnnotationSpanSlot()))
					consolidateAnnotations();
				return;
			}
		}

		public void deleted(FrameEvent event) {
			// System.out.println("deleted");
			// Frame eventFrame = event.getFrame();
			// System.out.println("eventFrame="+eventFrame);
			// why doesn't this spit out statements?
		}
	}

	public SimpleInstance getTeamAnnotator() {
		return teamAnnotator;
	}
}

/*
 * Here we find the team annotator and the individual annotators and initialize
 * the variables teamAnnotator and annotators, respectively. If the passed in
 * filter does not have the appropriate annotators, then an exception will be
 * thrown.
 */
// Set<SimpleInstance> filterAnnotators =
// FilterUtil.getAnnotators(consensusFilter);
// this.annotators = new HashSet<SimpleInstance>();
// for (SimpleInstance filterAnnotator : filterAnnotators)
// {
// if(AnnotatorUtil.isTeamAnnotator(filterAnnotator))
// {
// this.teamAnnotator = filterAnnotator;
// }
// else
// annotators.add(filterAnnotator);
// }
