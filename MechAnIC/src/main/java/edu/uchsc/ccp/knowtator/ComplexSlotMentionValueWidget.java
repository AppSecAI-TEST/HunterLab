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
 *   Angus Roberts
 */

/**
 * Changes:
 *  2/7/2007    ar	   updated handleAddAction to handle inverse slots
 *  2/7/2007	ar	   added handleRemoveAction
 *  9/7/2005    pvo    added handleCreateAction which allows one to highlight a span of text and 
 *                     click the 'Create Instance' button for an annotations slot value and create
 *                     an annotation for that slot at the selected span.
 *  9/7/2005    pvo    updated the SelectAnnotationsFromCollectionPanel such that mousing over an annotation
 *                     in the panel causes the corresponding text in the text viewer to be highlighted.
 *  8/10/2005   pvo    list now uses ListCellRenderer obtained from the KB.getClientInformation()
 *                     this was done in conjunction with creation of the BrowserTextUtil code
 *  8/11/2005   pvo    the handleAddAction calls pickAnnotationsFromCollection to ask for an annotation instead of the
 *                     previously used DisplayUtilities.pickInstancesFromCollection.
 *                     the method was copied from the DisplayUtilities and modified so that the 
 *                     displayed list uses the desired ListCellRenderer.
 */

package edu.uchsc.ccp.knowtator;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.DoubleClickListener;
import edu.stanford.smi.protege.widget.InstanceListWidget;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.ui.AnnotationPicker;
import edu.uchsc.ccp.knowtator.util.ProtegeUtil;

public class ComplexSlotMentionValueWidget extends InstanceListWidget implements DoubleClickListener {

	static final long serialVersionUID = 0;

	KnowtatorManager manager;

	MentionUtil mentionUtil;

	AnnotationUtil annotationUtil;

	KnowtatorProjectUtil kpu;

	TextSourceUtil textSourceUtil;

	BrowserTextUtil browserTextUtil;

	KnowledgeBase kb;

	DisplayColors displayColors;

	Logger logger = Logger.getLogger(ComplexSlotMentionValueWidget.class);

	public void initialize() {
		super.initialize();
		manager = (KnowtatorManager) getKnowledgeBase().getClientInformation(Knowtator.KNOWTATOR_MANAGER);
		mentionUtil = manager.getMentionUtil();
		annotationUtil = manager.getAnnotationUtil();
		kpu = manager.getKnowtatorProjectUtil();
		textSourceUtil = manager.getTextSourceUtil();
		browserTextUtil = manager.getBrowserTextUtil();
		displayColors = manager.getDisplayColors();

		kb = kpu.getKnowledgeBase();

		setDoubleClickListener(this);
		// we want the list to use our cell renderer
		setRenderer(manager.getRenderer()); // this does nothing because it is
											// reset in the super class
											// AbstractSlotWidget.setInstance
											// method.
	}

	public void setValues(Collection values) {
		super.setValues(values);
		try {
			SimpleInstance slotMention = (SimpleInstance) getInstance();
			Slot mentionSlot = mentionUtil.getSlotMentionSlot(slotMention);
			if (mentionSlot == null)
				return;
			SimpleInstance mentionedByMention = mentionUtil.getMentionedBy(slotMention);
			Cls mentionCls = mentionUtil.getMentionCls(mentionedByMention);
			if (mentionCls == null)
				return;

			// this code doesn't work for some reason. It is supposed to paint
			// the red border
			// around this slot
			// int minCardinality =
			// mentionCls.getTemplateSlotMinimumCardinality(mentionSlot);
			// int maxCardinality =
			// mentionCls.getTemplateSlotMaximumCardinality(mentionSlot);
			//            
			// if(values.size() < minCardinality || values.size() >
			// maxCardinality)
			// setInvalidValueBorder();
			// else
			// setNormalBorder();

			getLabeledComponent().setHeaderLabel(ProtegeUtil.getSlotLabel(mentionCls, mentionSlot, getProject()));
		} catch (NullPointerException npe) {
			getLabeledComponent().setHeaderLabel("");
		}
	}

	public void setInstance(Instance instance) {
		super.setInstance(instance);
		// the renderer is set in the super class AbstractSlotWidget - we must
		// set it to our renderer to get it to show up.
		setRenderer(manager.getRenderer());
	}

	@Override
	protected void handleViewAction(Instance instance) {
		logger.debug(browserTextUtil.getBrowserText((SimpleInstance) instance, 100));
		SimpleInstance annotation = mentionUtil.getMentionAnnotation((SimpleInstance) instance);
		manager.setSelectedAnnotation(annotation);
		
		EventHandler.getInstance().fireSlotValueChanged();
	}

	public void onDoubleClick(Object item) {
		if (item instanceof SimpleInstance)
			handleViewAction((SimpleInstance) item);
	}

	protected void handleCreateAction() {
		// slotMention is the mention that corresponds to the instance handled
		// by this widget
		SimpleInstance slotMention = (SimpleInstance) getInstance();
		// mentionSlot is the slot corresponding to the slotMention
		Slot mentionSlot = mentionUtil.getSlotMentionSlot(slotMention);
		if (mentionSlot == null) {
			showSlotMissingMessage();
			return;
		}

		// mention has slotMention as a value of one of its
		// knowtator_slot_mention
		SimpleInstance mention = mentionUtil.getMentionedBy(slotMention);
		// mentionCls is the cls of the mention
		Cls mentionCls = mentionUtil.getMentionCls(mention);
		if (mentionCls == null) {
			showTypeMissingMessage(mention);
			return;
		}

		if (!checkValueTypeConstraint(mentionCls, mentionSlot))
			return;

		// Get the classes that are allowed by this class at this slot.
		// This assumes the slot is constrained to be an instance of a cls and
		// does not account for
		// the possibility that the slot may be constrained to be a cls.
		Set<Cls> allowedClses = new HashSet<Cls>((Collection<Cls>) mentionCls.getTemplateSlotAllowedClses(mentionSlot));

		// this code needs to be expanded in two ways - if the slot of the cls
		// is of type cls then
		// we need to call mentionCls.getTemplateSlotAllowedParents(slot) and
		// make sure that instances are not displayed for selection.
		// if the slot of the cls is of type instance then we should display the
		// annotations corresponding to instances for selection.
		if (allowedClses == null || allowedClses.size() < 1) {
			allowedClses = new HashSet<Cls>(manager.getRootClses());
		}

		Set<Cls> descendants = new HashSet<Cls>();
		for (Cls allowedCls : allowedClses) {
			descendants.addAll(allowedCls.getSubclasses());
		}

		descendants.addAll(allowedClses);

		Cls newMentionCls;
		if (descendants.size() > 1) {
			newMentionCls = edu.stanford.smi.protege.ui.DisplayUtilities.pickCls(this, kb, allowedClses,
					"Choose type for new annotation filling in the slot");
		} else {
			newMentionCls = (Cls) CollectionUtilities.getFirstItem(descendants);
		}

		if (newMentionCls == null)
			return;

		SimpleInstance newAnnotation = manager.createAnnotation(newMentionCls, false);

		SimpleInstance newMention = annotationUtil.getMention(newAnnotation);

		addItem(newMention);
		mentionUtil.addInverse(mention, mentionSlot, newMention);
		mentionUtil.adjustSlotMentionForCardinality(mentionCls, mentionSlot, mention);
		manager.updateCurrentAnnotations();
		
		EventHandler.getInstance().fireSlotValueChanged();
	}

	private boolean checkValueTypeConstraint(Cls cls, Slot slot) {
		ValueType type = cls.getTemplateSlotValueType(slot);
		if (type == ValueType.INSTANCE || type == ValueType.CLS)
			return true;
		else {
			JOptionPane.showMessageDialog(this, "It appears that the value type constraint\n"
					+ "has changed since the slot values for this\n" + "slot were entered.\n"
					+ "Please remove the slot values for this slot,\n"
					+ "select a different annotation, and re-select\n " + "the currently selected annotation.",
					"value type constraint inconsistency", JOptionPane.WARNING_MESSAGE);
			return false;
		}
	}

	private void showTypeMissingMessage(SimpleInstance mention) {
		if (mentionUtil.isClassMention(mention))
			JOptionPane.showMessageDialog(this, "There is no class assigned to this annotation.\n"
					+ "You may not add a value to this slot until a \n" + "class is assigned.", "No class assigned",
					JOptionPane.WARNING_MESSAGE);
		else if (mentionUtil.isInstanceMention(mention))
			JOptionPane.showMessageDialog(this, "There is no instance assigned to this annotation.\n"
					+ "You may not add a value to this slot until an \n" + "instance is assigned.",
					"No instance assigned", JOptionPane.WARNING_MESSAGE);

	}

	private void showSlotMissingMessage() {
		JOptionPane.showMessageDialog(this, "There is not a slot specified for this slot value.\n"
				+ "This is most likely a result of deleting a slot \n"
				+ "from the annotation schema after this annotation \n"
				+ "was created.  Please remove this slot value, select\n"
				+ "another annotation, and re-select this annotation.", "Slot missing", JOptionPane.WARNING_MESSAGE);
	}

	/**
	 * At the moment the only value you can add is a mention from another
	 * annotation. First, loop through all of the available annotations, then
	 * choose only those that make sense for the slot.
	 * 
	 * why isn't the allowedClses filled with getTemplateSlotAllowedParents too?
	 */
	protected void handleAddAction() {
		SimpleInstance slotMention = (SimpleInstance) getInstance();
		Slot mentionSlot = mentionUtil.getSlotMentionSlot(slotMention);
		if (mentionSlot == null) {
			showSlotMissingMessage();
			return;
		}
		SimpleInstance mention = mentionUtil.getMentionedBy(slotMention);
		Cls mentionCls = mentionUtil.getMentionCls(mention);
		if (mentionCls == null) {
			showTypeMissingMessage(mention);
			return;
		}

		if (!checkValueTypeConstraint(mentionCls, mentionSlot))
			return;

		List<SimpleInstance> partiallyFilteredAnnotations = manager.getCurrentPartiallyFilteredAnnotations();
		List<SimpleInstance> annotations = mentionUtil.getSlotFillerCandidates(mention, mentionSlot,
				partiallyFilteredAnnotations);

		if (annotations.size() == 0) {
			if (getList().getModel().getSize() == 0)
				JOptionPane
						.showMessageDialog(
								this,
								"There are no annotations available that satisfy the constraints for this slot on this annotation.",
								"No appropriate annotations", JOptionPane.INFORMATION_MESSAGE);
			else
				JOptionPane
						.showMessageDialog(
								this,
								"There are no other annotations available that satisfy the constraints for this slot on this annotation.",
								"No appropriate annotations", JOptionPane.INFORMATION_MESSAGE);

			return;
		} else if (annotations.size() == 1) {
			SimpleInstance annotationInstance = annotations.get(0);
			SimpleInstance annotationMention = annotationUtil.getMention(annotationInstance);
			addItem(annotationMention);
			mentionUtil.addInverse(mention, mentionSlot, annotationMention);
		} else {
			Comparator comparator = manager.getAnnotationComparator();
			Collections.sort(annotations, comparator);
			List<SimpleInstance> pickedAnnotations = AnnotationPicker.pickAnnotationsFromCollection(this, manager,
					annotations, "select annotation for '" + mentionSlot.getBrowserText() + "'");

			for (SimpleInstance annotation : pickedAnnotations) {
				SimpleInstance annotationMention = annotationUtil.getMention(annotation);
				addItem(annotationMention);
				mentionUtil.addInverse(mention, mentionSlot, annotationMention);
			}
		}

		mentionUtil.adjustSlotMentionForCardinality(mentionCls, mentionSlot, mention);

		// int maxCardinality =
		// mentionCls.getTemplateSlotMaximumCardinality(mentionSlot);
		// if(maxCardinality > 0)
		// {
		// ArrayList values = new ArrayList(getValues());
		// removeAllItems();
		// for(int i=values.size()-1, j=0; i >=0 && j<maxCardinality; i--)
		// {
		// addItem(values.get(i));
		// j++;
		// }
		// }
		manager.refreshAnnotationsDisplay(true);

		EventHandler.getInstance().fireSlotValueChanged();
	}

	protected void handleRemoveAction(Collection instances) {
		super.handleRemoveAction(instances);

		SimpleInstance slotMention = (SimpleInstance) getInstance();
		Slot mentionSlot = mentionUtil.getSlotMentionSlot(slotMention);
		if (mentionSlot != null) {
			SimpleInstance mentionedByMention = mentionUtil.getMentionedBy(slotMention);

			for (Object annotationMentionObj : instances)
				mentionUtil.removeInverse(mentionedByMention, mentionSlot, (SimpleInstance) annotationMentionObj);
		}
		manager.refreshAnnotationsDisplay(true);
		
		EventHandler.getInstance().fireSlotValueChanged();
	}
}
