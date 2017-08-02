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

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.DisplayColors;
import edu.uchsc.ccp.knowtator.InvalidSpanException;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.MentionUtil;
import edu.uchsc.ccp.knowtator.Span;
import edu.uchsc.ccp.knowtator.TextSourceUtil;

/**
 * This class simply iterates over annotations and mentions in a Knowtator
 * project and accesses the slot values of each. The annotations are a flat data
 * structure so it is sufficient to simply iterate through them. The mentions
 * are recursively defined so iterating over them is slightly more complicated.
 * 
 * This code doesn't really do anything. If you were going to export the code to
 * xml or to UIMA you would want to do something with the data structures as you
 * accessed them.
 * 
 * This code does compile but it has not been run or tested. It is, however,
 * mostly code stolen from MergeAnnotations.java which does run and has been
 * "tested".
 */

public class ExportAnnotations {

	Project project;

	KnowledgeBase kb;

	KnowtatorProjectUtil kpu;

	KnowtatorManager manager;

	AnnotationUtil annotationUtil;

	TextSourceUtil textSourceUtil;

	MentionUtil mentionUtil;

	DisplayColors displayColors;

	java.util.List<Instance> annotations;

	java.util.List<Instance> mentions;

	public ExportAnnotations(Project project) throws Exception {
		this.project = project;
		kb = project.getKnowledgeBase();
		kpu = new KnowtatorProjectUtil(kb);
		manager = new KnowtatorManager(kpu);
		annotationUtil = manager.getAnnotationUtil();
		textSourceUtil = new TextSourceUtil(annotationUtil, kpu);
		textSourceUtil.init();
		annotationUtil.setTextSourceUtil(textSourceUtil);
		mentionUtil = manager.getMentionUtil();
		displayColors = manager.getDisplayColors();

		annotations = new ArrayList<Instance>(kb.getInstances(kpu.getAnnotationCls()));

		mentions = new ArrayList<Instance>(kb.getInstances(kpu.getMentionCls()));

		System.out.println("annotations.size()= " + annotations.size());
		System.out.println("mentions.size()= " + mentions.size());

		exportAnnotations();
		exportMentions();

	}

	public void exportAnnotations() {
		for (Instance ann : annotations) {
			SimpleInstance annotation = (SimpleInstance) ann;
			SimpleInstance mention = annotationUtil.getMention(annotation);
			System.out.println("mention = " + mention);

			SimpleInstance annotator = annotationUtil.getAnnotator(annotation);
			String annotatorID = annotator.getName();
			System.out.println("annotatorID=" + annotatorID);

			String annotatorName = annotator.getBrowserText();
			System.out.println("annotatorName=" + annotatorName);

			String comment = annotationUtil.getComment(annotation);
			System.out.println("comment=" + comment);

			// ?? how to get to Date object
			String dateString = annotationUtil.getCreationDate(annotation);
			System.out.println("dateString=" + dateString);

			Color annotationColor = displayColors.getColor(annotation);
			System.out.println("annotationColor=" + annotationColor);

			try {
				java.util.List<Span> spans = annotationUtil.getSpans(annotation);
				System.out.println("spans=" + spans);
			} catch (InvalidSpanException ise) {
			}

			String spannedText = annotationUtil.getText(annotation);
			System.out.println("spannedText=" + spannedText);

			SimpleInstance textSourceInstance = annotationUtil.getTextSource(annotation);
			String textSourceID = textSourceInstance.getName();
			System.out.println("textSourceID=" + textSourceID);

			Collection<SimpleInstance> annotationSets = (Collection<SimpleInstance>) annotation.getOwnSlotValues(kpu
					.getSetSlot());
			for (SimpleInstance setInstance : annotationSets) {
				String setName = setInstance.getName();
				System.out.println("setName=" + setName);
			}
		}
	}

	public void exportMentions() {
		Set<SimpleInstance> exportedMentions = new HashSet<SimpleInstance>();
		for (Instance mntn : mentions) {
			SimpleInstance mention = (SimpleInstance) mntn;
			exportMention(mention, exportedMentions);
		}
	}

	private SimpleInstance exportMention(SimpleInstance mention, Set<SimpleInstance> exportedMentions) {
		if (!exportedMentions.contains(mention)) {
			if (mentionUtil.isClassMention(mention)) {
				Cls mentionCls = mentionUtil.getMentionCls(mention);
				String mentionClsName = "";
				System.out.println("mentionClsName=" + mentionClsName);

				if (mentionCls != null)
					mentionClsName = mentionCls.getName();
				exportedMentions.add(mention);

				Collection<SimpleInstance> slotMentions = (Collection<SimpleInstance>) mention.getOwnSlotValues(kpu
						.getSlotMentionSlot());
				for (SimpleInstance slotMention : slotMentions) {
					exportMention(slotMention, exportedMentions);
				}
			} else if (mentionUtil.isInstanceMention(mention)) {
				SimpleInstance instance = mentionUtil.getMentionInstance(mention);
				String instanceName = "";
				System.out.println("instanceName=" + instanceName);

				if (instance != null)
					instanceName = instance.getName();
				exportedMentions.add(mention);

				Collection<SimpleInstance> slotMentions = (Collection<SimpleInstance>) mention.getOwnSlotValues(kpu
						.getSlotMentionSlot());
				for (SimpleInstance slotMention : slotMentions) {
					exportMention(slotMention, exportedMentions);
				}
			} else if (mentionUtil.isSlotMention(mention)) {
				Slot slot = mentionUtil.getSlotMentionSlot(mention);
				String slotName = slot.getName();
				System.out.println("slotName=" + slotName);

				exportedMentions.add(mention);

				Collection slotValues = mention.getOwnSlotValues(kpu.getMentionSlotValueSlot());

				if (slotValues != null && slotValues.size() > 0) {
					Object value = CollectionUtilities.getFirstItem(slotValues);
					if (value instanceof SimpleInstance) {
						for (Object slotValue : slotValues) {
							SimpleInstance slotValueInstance = (SimpleInstance) slotValue;
							exportMention(slotValueInstance, exportedMentions);
						}
					} else {
						// value is some primitive such as int, String, boolean
					}
				}
			}

			Collection<SimpleInstance> mentionAnnotations = (Collection<SimpleInstance>) mention.getOwnSlotValues(kpu
					.getMentionAnnotationSlot());
			if (mentionAnnotations != null) {
				for (SimpleInstance mentionAnnotation : mentionAnnotations) {
					System.out.println("mentionAnnotation=" + mentionAnnotation);

					// do something
				}
			}
		}
		return mention;
	}
}
