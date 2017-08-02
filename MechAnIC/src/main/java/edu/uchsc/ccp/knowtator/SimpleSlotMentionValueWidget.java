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

package edu.uchsc.ccp.knowtator;

import java.util.Collection;

import javax.swing.JOptionPane;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.widget.AbstractSlotWidget;
import edu.stanford.smi.protege.widget.ClsWidget;
import edu.stanford.smi.protege.widget.SlotWidget;
import edu.stanford.smi.protege.widget.TextFieldWidget;

/**
 * This class will hopefully replace SlotMentionValueWidget if I can ever get it
 * to work the way I want it to. The code here is intended to address feature
 * request [ 1720295 ] simple slots should dynamically load widget from class
 * for
 * http://sourceforge.net/tracker/index.php?func=detail&aid=1720295&group_id=
 * 128424&atid=714371
 * 
 * @author Philip
 * 
 */
public class SimpleSlotMentionValueWidget extends TextFieldWidget {

	static final long serialVersionUID = 0;

	KnowtatorManager manager;

	MentionUtil mentionUtil;

	AnnotationUtil annotationUtil;

	KnowtatorProjectUtil kpu;

	public void initialize() {
		super.initialize();
		manager = (KnowtatorManager) getKnowledgeBase().getClientInformation(Knowtator.KNOWTATOR_MANAGER);
		mentionUtil = manager.getMentionUtil();
		annotationUtil = manager.getAnnotationUtil();
		kpu = manager.getKnowtatorProjectUtil();
		setPreferredColumns(2);
		setPreferredRows(2);
	}

	public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
		return true;
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

	public void setValues(Collection values) {
		super.setValues(values);

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

		ClsWidget clsWidget = getProject().getDesignTimeClsWidget(mentionCls);
		SlotWidget slotWidget = clsWidget.getSlotWidget(mentionSlot);
		AbstractSlotWidget widget = (AbstractSlotWidget) slotWidget;
		widget.setSlot(kpu.getSlotMentionSlot());
		widget.setInstance(getInstance());
		removeAll();
		add(widget);
	}
}
