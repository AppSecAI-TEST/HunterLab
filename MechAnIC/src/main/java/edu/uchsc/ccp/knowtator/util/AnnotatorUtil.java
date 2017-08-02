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
/*
 * AnnotatorUtil.java
 *
 * Created on July 26, 2005, 5:22 PM
 */

package edu.uchsc.ccp.knowtator.util;

import java.awt.Component;
import java.util.Collection;

import javax.swing.JOptionPane;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.ui.DisplayUtilities;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.FilterUtil;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;

/**
 * 
 * @author Philip V. Ogren
 */
public class AnnotatorUtil {

	/**
	 * This method provides dialogs which collect the annotator and filter
	 * instances needed to forward on to the other batchAnnotatorChange method.
	 */

	public static void batchAnnotatorChange(Component parent, KnowtatorProjectUtil kpu, AnnotationUtil annotationUtil,
			FilterUtil filterUtil) {
		int option = JOptionPane.showConfirmDialog(parent,
				"The following dialogs provide a way to set the annotator slot for a set of \n"
						+ "annotations.  The next dialog will ask you to choose an annotator.  The value\n"
						+ "you select will be used to set the value of the 'annotator' slot.  The \n"
						+ "following dialog will ask you to choose an annotation filter.  The value\n "
						+ "you select will be used to decide which annotations will be updated",
				"Change annotator for annotations", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			SimpleInstance annotator = (SimpleInstance) DisplayUtilities.pickInstance(parent, CollectionUtilities
					.createCollection(kpu.getAnnotatorCls()), "Choose annotator of annotations");
			if (annotator == null)
				return;
			SimpleInstance filter = (SimpleInstance) DisplayUtilities.pickInstance(parent, CollectionUtilities
					.createCollection(kpu.getFilterCls()), "Choose an annotation filter");
			if (filter == null)
				return;
			batchAnnotatorChange(annotator, kpu, filter, annotationUtil, filterUtil);
			JOptionPane.showMessageDialog(parent, "The annotator slot for each annotation satisfying the filter '"
					+ filter.getBrowserText() + "' has been set to '" + annotator.getBrowserText() + "'");

		}
	}

	/**
	 * This method sets the annotator slot value for a set of annotations. This
	 * method simply retrieves all annotations from the annotationUtil and
	 * filters them using the supplied filter. The annotator slot value for each
	 * annotation that passes the filter is updated with the first parameter.
	 * 
	 * @param annotator
	 *            the value of the knowtator_annotation_annotator slot for the
	 *            annotations to be changed
	 * @param kpu
	 *            provides the name of the slot that is being changed via
	 *            getAnnotationAnnotatorSlot()
	 * @param filter
	 *            an instance of 'knowtator filter' that will be used to select
	 *            the annotations that will be modified.
	 * @param annotationUtil
	 *            provides the annotations in the current knowledgebase via the
	 *            retrieveAllAnnotations method
	 * @param filterUtil
	 *            provides the filter method which selects only the annotations
	 *            that satisfy the filter.
	 */

	public static void batchAnnotatorChange(SimpleInstance annotator, KnowtatorProjectUtil kpu, SimpleInstance filter,
			AnnotationUtil annotationUtil, FilterUtil filterUtil) {
		java.util.List<SimpleInstance> annotations = annotationUtil.retrieveAllAnnotations();
		Collection<SimpleInstance> filteredAnnotations = filterUtil.filterAnnotations(annotations, filter);

		for (SimpleInstance annotation : filteredAnnotations) {
			annotation.setDirectOwnSlotValue(kpu.getAnnotationAnnotatorSlot(), annotator);
		}
	}

	public static void batchAnnotationSetChange(Component parent, KnowtatorProjectUtil kpu,
			AnnotationUtil annotationUtil, FilterUtil filterUtil) {
		int option = JOptionPane.showConfirmDialog(parent,
				"The following dialogs provide a way to assign the annotation set for a large number of \n"
						+ "annotations.  The next dialog will ask you to choose an annotation set.  The value\n"
						+ "you select will be added to the 'annotation set' slot.  The \n"
						+ "following dialog will ask you to choose an annotation filter.  The value\n "
						+ "you select will be used to decide which annotations will be updated",
				"Change annotator for annotations", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			SimpleInstance annotationSet = (SimpleInstance) DisplayUtilities.pickInstance(parent, CollectionUtilities
					.createCollection(kpu.getSetCls()), "Choose annotation set");
			if (annotationSet == null)
				return;
			SimpleInstance filter = (SimpleInstance) DisplayUtilities.pickInstance(parent, CollectionUtilities
					.createCollection(kpu.getFilterCls()), "Choose an annotation filter");
			if (filter == null)
				return;
			batchAnnotationSetChange(annotationSet, kpu, filter, annotationUtil, filterUtil);
			JOptionPane.showMessageDialog(parent,
					"The 'annotation set' slot for each annotation satisfying the filter '" + filter.getBrowserText()
							+ "' has been set to '" + annotationSet.getBrowserText() + "'");

		}
	}

	/**
	 * This method sets the annotator slot value for a set of annotations. This
	 * method simply retrieves all annotations from the annotationUtil and
	 * filters them using the supplied filter. The annotator slot value for each
	 * annotation that passes the filter is updated with the first parameter.
	 * 
	 * @param annotationSet
	 *            the value of the knowtator_annotation_set slot for the
	 *            annotations to be changed
	 * @param kpu
	 *            provides the name of the slot that is being changed via
	 *            getAnnotationAnnotatorSlot()
	 * @param filter
	 *            an instance of 'knowtator filter' that will be used to select
	 *            the annotations that will be modified.
	 * @param annotationUtil
	 *            provides the annotations in the current knowledgebase via the
	 *            retrieveAllAnnotations method
	 * @param filterUtil
	 *            provides the filter method which selects only the annotations
	 *            that satisfy the filter.
	 */

	public static void batchAnnotationSetChange(SimpleInstance annotationSet, KnowtatorProjectUtil kpu,
			SimpleInstance filter, AnnotationUtil annotationUtil, FilterUtil filterUtil) {
		java.util.List<SimpleInstance> annotations = annotationUtil.retrieveAllAnnotations();
		Collection<SimpleInstance> filteredAnnotations = filterUtil.filterAnnotations(annotations, filter);

		for (SimpleInstance annotation : filteredAnnotations) {
			annotation.setDirectOwnSlotValue(kpu.getSetSlot(), annotationSet);
		}
	}

	public static boolean isTeamAnnotator(SimpleInstance annotator) {
		KnowledgeBase kb = annotator.getKnowledgeBase();
		Cls type = annotator.getDirectType();
		if (type.equals(kb.getCls(KnowtatorProjectUtil.ANNOTATOR_TEAM_CLS_NAME)))
			return true;
		return false;

	}

}

// /**
// * This method finds annotations created by oldAnnotator and sets the
// annotator
// * slot to newAnnotator.
// * @param kb the knowledge base the annotations reside in
// * @param oldAnnotator the annotator that is being replaced. If null then
// * method is equivalent to replaceAnnotator(KnowledgeBase kb, Instance
// annotator)
// * @param newAnnotator the annotations created by oldAnnotator will have the
// * annotator slot set to newAnnotator.
// */
//    
// public static void replaceAnnotator(KnowledgeBase kb,
// Instance oldAnnotator,
// Instance newAnnotator)
// {
// Cls annotationCls = kb.getCls(KnowtatorProjectUtil.ANNOTATION_CLS_NAME);
// Slot annotatorSlot =
// kb.getSlot(KnowtatorProjectUtil.ANNOTATION_ANNOTATOR_SLOT_NAME);
// Collection<Instance> annotations = kb.getInstances(annotationCls);
// for(Instance annotation : annotations)
// {
// if(oldAnnotator == null)
// {
// annotation.setOwnSlotValue(annotatorSlot, newAnnotator);
// }
// else
// {
// Instance currentAnnotator = (Instance)
// annotation.getOwnSlotValue(annotatorSlot);
// if(currentAnnotator != null && currentAnnotator.equals(oldAnnotator))
// {
// annotation.setOwnSlotValue(annotatorSlot, newAnnotator);
// }
// }
// }
// }
//    
// /**
// * This method finds annotations that have no value in the annotator slot and
// * sets the annotator value to annotator.
// * @param kb the knowledge base the annotations reside in
// * @param annotator all annotations that have no value in the annotator slot
// will
// * have their annotator slot value set to annotator.
// */
//    
// public static void setEmptyAnnotator(KnowledgeBase kb,
// Instance annotator)
// {
// Cls annotationCls = kb.getCls(KnowtatorProjectUtil.ANNOTATION_CLS_NAME);
// Slot annotatorSlot =
// kb.getSlot(KnowtatorProjectUtil.ANNOTATION_ANNOTATOR_SLOT_NAME);
// Collection<Instance> annotations = kb.getInstances(annotationCls);
// for(Instance annotation : annotations)
// {
// Instance currentAnnotator = (Instance)
// annotation.getOwnSlotValue(annotatorSlot);
// if(currentAnnotator == null)
// {
// annotation.setOwnSlotValue(annotatorSlot, annotator);
// }
// }
// }
// /**
// * This method sets the annotator for all annotations in the knowledge base.
// * @param kb the knowledge base the annotations reside in
// * @param annotator all annotations will have there annotator slot set to the
// * instance passed in this parameter.
// *
// */
// public static void replaceAnnotator(KnowledgeBase kb,
// Instance annotator)
// {
// replaceAnnotator(kb, null, annotator);
// }
//    

