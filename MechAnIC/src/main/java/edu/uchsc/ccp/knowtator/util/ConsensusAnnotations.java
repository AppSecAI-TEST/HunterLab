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
 */
package edu.uchsc.ccp.knowtator.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.FilterUtil;
import edu.uchsc.ccp.knowtator.InvalidSpanException;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.MentionUtil;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;

public class ConsensusAnnotations {

	public static ConsensusSet recreateConsensusAnnotations(SimpleInstance textSource, SimpleInstance consensusSet,
			KnowtatorManager manager) throws ConsensusException {
		KnowledgeBase kb = manager.getKnowledgeBase();
		KnowtatorProjectUtil kpu = manager.getKnowtatorProjectUtil();
		AnnotationUtil annotationUtil = manager.getAnnotationUtil();
		MentionUtil mentionUtil = manager.getMentionUtil();
		FilterUtil filterUtil = manager.getFilterUtil();

		SimpleInstance consensusFilter = (SimpleInstance) consensusSet.getOwnSlotValue(kpu
				.getConsensusSetConsensusFilterSlot());

		Collection<SimpleInstance> annotations = annotationUtil.getAnnotations(textSource);
		if (annotations != null) {
			Collection<SimpleInstance> oldConsensusAnnotations = filterUtil.filterAnnotations(annotations,
					consensusFilter);
			for (SimpleInstance oldAnnotation : oldConsensusAnnotations) {
				SimpleInstance mention = annotationUtil.getMention(oldAnnotation);
				Set<SimpleInstance> mentions = mentionUtil.getAllConnectedMentions(mention);
				for (SimpleInstance oldMention : mentions) {
					kb.deleteFrame(oldMention);
				}
				kb.deleteFrame(oldAnnotation);
			}
		}

		return createConsensusSet(textSource, manager, consensusSet);
	}

	public static void createConsensusAnnotations(KnowtatorManager manager, SimpleInstance filter,
			String consensusSetName, Collection<SimpleInstance> textSources) throws ConsensusException {
		KnowledgeBase kb = manager.getKnowledgeBase();
		KnowtatorProjectUtil kpu = manager.getKnowtatorProjectUtil();

		// need to document the fact that we are relying on the passed in filter
		// to have the annotators specified.
		// Or we need to throw an appropriate exception and display an error
		// message.
		Set<SimpleInstance> annotators = new HashSet<SimpleInstance>(FilterUtil.getAnnotators(filter));

		SimpleInstance teamAnnotator = kb.createSimpleInstance(new FrameID(null), CollectionUtilities.createCollection(kpu
				.getTeamAnnotatorCls()), true);
		teamAnnotator.setDirectOwnSlotValues(kpu.getAnnotatorTeamMembersSlot(), annotators);
		teamAnnotator.setDirectOwnSlotValue(kpu.getAnnotatorTeamNameSlot(), consensusSetName + " annotator team");

		SimpleInstance consensusSet = null;
		SimpleInstance consensusFilter = null;
		Collection<Instance> consensusSetInstances = kb.getInstances(kpu.getConsensusSetCls());
		for (Instance consensusSetInstance : consensusSetInstances) {
			Object consensusSetInstanceName = consensusSetInstance.getDirectOwnSlotValue(kpu.getSetNameSlot());
			if (consensusSetInstanceName.equals(consensusSetName)) {
				consensusSet = (SimpleInstance) consensusSetInstance;
				consensusFilter = (SimpleInstance) consensusSet.getDirectOwnSlotValue(kpu
						.getConsensusSetConsensusFilterSlot());
			}

		}
		if (consensusSet == null) {
			consensusSet = kb.createSimpleInstance(new FrameID(null), CollectionUtilities.createCollection(kpu
					.getConsensusSetCls()), true);
			consensusSet.setDirectOwnSlotValue(kpu.getSetNameSlot(), consensusSetName);
			consensusSet.setDirectOwnSlotValue(kpu.getSetDescriptionSlot(),
					"This set corresponds to a consensus set of annotations generated by Knowtator.");
			consensusSet.setDirectOwnSlotValue(kpu.getConsensusSetIndividualFilterSlot(), filter);
			consensusSet.setDirectOwnSlotValue(kpu.getConsensusSetTeamAnnotatorSlot(), teamAnnotator);

			consensusFilter = kb.createSimpleInstance(new FrameID(null), CollectionUtilities.createCollection(kpu
					.getConsensusFilterCls()), true);
			consensusFilter.setDirectOwnSlotValue(kpu.getFilterNameSlot(), consensusSetName + " filter");
			consensusFilter.setDirectOwnSlotValue(kpu.getFilterSetSlot(), consensusSet);
			consensusFilter.setDirectOwnSlotValues(kpu.getFilterAnnotatorSlot(), annotators);
			consensusFilter.addOwnSlotValue(kpu.getFilterAnnotatorSlot(), teamAnnotator);
			consensusSet.setDirectOwnSlotValue(kpu.getConsensusSetConsensusFilterSlot(), consensusFilter);
			Set<Cls> filterTypes = FilterUtil.getTypes(filter);
			if (filterTypes.size() > 0)
				consensusFilter.setDirectOwnSlotValues(kpu.getFilterTypeSlot(), filterTypes);
			else {
				// we need to add a root cls so that IAA will not throw an
				// exception.
				consensusFilter.setDirectOwnSlotValues(kpu.getFilterTypeSlot(), manager.getRootClses());
			}

		}

		for (SimpleInstance textSource : textSources) {
			ConsensusSet textSourceConsensusSet = createConsensusSet(textSource, manager, consensusSet);
			textSourceConsensusSet.destroy();
		}

		manager.addActiveFilter(consensusFilter);
		manager.setSelectedFilter(consensusFilter);
		manager.setSelectedAnnotator(teamAnnotator);
		manager.setSelectedAnnotationSet(consensusSet);
	}

	public static ConsensusSet createConsensusSet(SimpleInstance textSource, KnowtatorManager manager,
			SimpleInstance consensusSet) throws ConsensusException {
		KnowtatorProjectUtil kpu = manager.getKnowtatorProjectUtil();
		AnnotationUtil annotationUtil = manager.getAnnotationUtil();
		FilterUtil filterUtil = manager.getFilterUtil();
		MentionUtil mentionUtil = manager.getMentionUtil();
		SimpleInstance individualFilter = (SimpleInstance) consensusSet.getOwnSlotValue(kpu
				.getConsensusSetIndividualFilterSlot());
		Set<SimpleInstance> annotators = new HashSet<SimpleInstance>(FilterUtil.getAnnotators(individualFilter));
		if (annotators.size() < 2)
			throw new ConsensusException("The filter corresponding to the individually annotated "
					+ "\nannotations for the selected consensus set does not "
					+ "\nhave two or more annotators specified.");

		// The key is an original mention, and the value is a copied mention
		// created for the consensus set.
		Map<SimpleInstance, SimpleInstance> mentionCopies = new HashMap<SimpleInstance, SimpleInstance>();

		java.util.Set<SimpleInstance> consensusAnnotations = new HashSet<SimpleInstance>();
		// java.util.Set<SimpleInstance> consensusMentions = new
		// HashSet<SimpleInstance>();

		Collection<SimpleInstance> annotations = annotationUtil.getAnnotations(textSource);
		annotations = filterUtil.filterAnnotations(annotations, individualFilter);

		for (SimpleInstance annotation : annotations) {
			SimpleInstance mention = annotationUtil.getMention(annotation);
			SimpleInstance mentionCopy = mentionCopies.get(mention);
			if (mentionCopy == null) {
				mentionCopy = mentionUtil.copyMention(mention, mentionCopies);
				mentionCopies.put(mention, mentionCopy);
			}

			SimpleInstance annotator = annotationUtil.getAnnotator(annotation);
			try {
				SimpleInstance consensusAnnotation = annotationUtil.createAnnotation(mentionCopy, annotator,
						annotationUtil.getSpans(annotation), textSource, consensusSet);
				// collect all of the annotations created for the consensus set.
				consensusAnnotations.add(consensusAnnotation);
			} catch (TextSourceAccessException tsae) {
				tsae.printStackTrace();
			} catch (InvalidSpanException ise) {
				ise.printStackTrace();
			}
		}
		ConsensusSet textSourceConsensusSet = new ConsensusSet(consensusAnnotations, consensusSet, manager);
		textSourceConsensusSet.consolidateAnnotations();
		return textSourceConsensusSet;
	}
}
