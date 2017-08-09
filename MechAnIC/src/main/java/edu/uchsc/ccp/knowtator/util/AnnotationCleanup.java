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

import java.awt.Component;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.ui.DisplayUtilities;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.FilterUtil;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.MentionUtil;
import edu.uchsc.ccp.knowtator.TextSourceUtil;

public class AnnotationCleanup {
	public static void cleanup(Component parent, KnowledgeBase kb, KnowtatorProjectUtil kpu,
			TextSourceUtil textSourceUtil, AnnotationUtil annotationUtil, MentionUtil mentionUtil,
			FilterUtil filterUtil, Project project) {

		int option = JOptionPane.showConfirmDialog(parent,
				"This option allows you to delete large numbers of unwanted annotations.\n"
						+ "This is done by choosing an annotation filter that defines the set of \n"
						+ "annotations to keep and removing all others.  Only annotations that pass\n"
						+ "through the annotation filter and belong to the selected text sources will\n"
						+ "be kept.  All others will be deleted.  Please archive your project prior to\n"
						+ "to using this option.  Also, if you are unhappy with the results, simply close\n"
						+ "the project without saving the changes that have been made.  ", "Remove annotations",
				JOptionPane.OK_CANCEL_OPTION);
		if (option != JOptionPane.OK_OPTION)
			return;

		SimpleInstance filter = (SimpleInstance) DisplayUtilities.pickInstance(parent, CollectionUtilities
				.createCollection(kpu.getFilterCls()), "Select filter for annotations to be kept.");
		if (filter == null)
			return;

		Collection<SimpleInstance> textSources = (Collection<SimpleInstance>) DisplayUtilities.pickInstances(parent,
				kb, CollectionUtilities.createCollection(kpu.getTextSourceCls()),
				"Select text sources that contain annotations you want to keep.");
		if (textSources == null || textSources.size() == 0)
			return;

		option = JOptionPane.showConfirmDialog(parent, "Click OK to proceed with annotation removal.",
				"Confirm annotation removal", JOptionPane.OK_CANCEL_OPTION);
		if (option != JOptionPane.OK_OPTION)
			return;

		cleanup(filter, filterUtil, textSources, annotationUtil, mentionUtil, kb, kpu);

		JOptionPane.showMessageDialog(parent, "Removal of annotations complete.\n"
				+ "To undo changes, please close the project\n" + "without saving it and re-open it.",
				"TextAnnotation removal complete", JOptionPane.INFORMATION_MESSAGE);
	}

	public static void cleanup(SimpleInstance filter, FilterUtil filterUtil, Collection<SimpleInstance> textSources,
			AnnotationUtil annotationUtil, MentionUtil mentionUtil, KnowledgeBase kb, KnowtatorProjectUtil kpu)

	{
		Set<SimpleInstance> keepers = new HashSet<SimpleInstance>();

		// First we go through the selected text sources and find all the
		// "keepers" that have passed through the annotation filter.
		// This will be all annotations and there corresponding mentions and the
		// annotations that are connected via those
		// mentions.
		// Technically, an annotation that does not pass through the filter
		// could be a keeper if it is a slot value of an annotation
		// that does.
		for (SimpleInstance textSource : textSources) {
			Collection<SimpleInstance> annotations = annotationUtil.getAnnotations(textSource);
			annotations = filterUtil.filterAnnotations(annotations, filter);
			for (SimpleInstance annotation : annotations) {
				keepers.add(annotation);
				SimpleInstance mention = annotationUtil.getMention(annotation);
				keepers.add(mention);
				Set<SimpleInstance> connectedMentions = mentionUtil.getAllConnectedMentions(mention);
				keepers.addAll(connectedMentions);
				for (SimpleInstance connectedMention : connectedMentions) {
					SimpleInstance connectedAnnotation = mentionUtil.getMentionAnnotation(connectedMention);
					keepers.add(connectedAnnotation);
				}
			}
		}

		// Collect information about the annotation sets so that we can delete
		// them now and reconstruct them later
		// at the end of the method. This is done for performance reasons
		// because large annotation sets can really
		// slow down the process of deleting annotations because of the linear
		// searching that occurs.
		// Collection<SimpleInstance> annotationSets =
		// (Collection<SimpleInstance>) kb.getInstances(kpu.getSetCls());
		// Map<String, String> setDescriptions = new HashMap<String, String>();
		// Map<String, Set<SimpleInstance>> setAnnotations = new HashMap<String,
		// Set<SimpleInstance>>();
		//    	
		// for(SimpleInstance annotationSet : annotationSets)
		// {
		// String setName = (String)
		// annotationSet.getOwnSlotValue(kpu.getSetNameSlot());
		// String setDescription = (String)
		// annotationSet.getOwnSlotValue(kpu.getSetDescriptionSlot());
		// setDescriptions.put(setName, setDescription);
		// Collection<SimpleInstance> annotations = (Collection<SimpleInstance>)
		// annotationSet.getOwnSlotValues(kpu.getSetAnnotationSlot());
		// setAnnotations.put(setName, new HashSet<SimpleInstance>());
		// setAnnotations.get(setName).addAll(annotations);
		// kb.deleteInstance(annotationSet);
		// }

		// delete all annotations that are not in 'keepers'
		Collection<Instance> annotations = kb.getInstances(kpu.getAnnotationCls());
		for (Instance annotation : annotations) {
			if (!keepers.contains(annotation)) {
				kb.deleteInstance(annotation);
			}
		}

		// now delete all mentions not in 'keepers' too.
		Collection<Instance> mentions = kb.getInstances(kpu.getMentionCls());
		for (Instance mention : mentions) {
			if (!keepers.contains(mention)) {
				kb.deleteInstance(mention);
			}
		}

		// we will now reconstruct the annotation sets with the annotations that
		// remain.
		// for(String setName : setDescriptions.keySet())
		// {
		// SimpleInstance setInstance = kb.createSimpleInstance(
		// null,
		// null,
		// CollectionUtilities.createCollection(kpu.getSetCls()),
		// true);
		// setInstance.setOwnSlotValue(kpu.getSetNameSlot(), setName);
		// setInstance.setOwnSlotValue(kpu.getSetDescriptionSlot(),
		// setDescriptions.get(setName));
		//            
		// Set<SimpleInstance> setAnns = setAnnotations.get(setName);
		// Set<SimpleInstance> existingAnns = new HashSet<SimpleInstance>();
		// for(SimpleInstance setAnn : setAnns)
		// {
		// if(!setAnn.isDeleted())
		// existingAnns.add(setAnn);
		// }
		// setInstance.setOwnSlotValues(kpu.getSetAnnotationSlot(),
		// existingAnns);
		// }

	}

}
