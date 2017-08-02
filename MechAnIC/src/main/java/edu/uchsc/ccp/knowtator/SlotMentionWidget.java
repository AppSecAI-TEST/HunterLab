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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.widget.ClsWidget;
import edu.stanford.smi.protege.widget.SlotWidget;
import edu.stanford.smi.protegex.widget.contains.ContainsWidget;

/**
 * This class is a simple extension of the ContainsWidget that simply overrides
 * the setValues method so that the slot mentions of a class mention or instance
 * mention will always be in a predictable order - rather than a random order(!)
 * The order of the slots is currently alphabetical by the slot name. Preference
 * is given to the slot's form label as it appears in the mentioned classes slot
 * widget as configured in the Forms tab.
 * 
 * @author Philip V. Ogren
 */
public class SlotMentionWidget extends ContainsWidget {

	static final long serialVersionUID = 0;

	MentionUtil mentionUtil;

	SlotMentionComparator slotMentionComparator;

	public void initialize() {
		KnowtatorManager manager = (KnowtatorManager) getKnowledgeBase().getClientInformation(
				Knowtator.KNOWTATOR_MANAGER);
		mentionUtil = manager.getMentionUtil();
		slotMentionComparator = new SlotMentionComparator();
		super.initialize();
	}

	public void setValues(java.util.Collection values) {
		ArrayList<SimpleInstance> sortedValues = new ArrayList<SimpleInstance>();
		Iterator valuesIterator = values.iterator();
		while (valuesIterator.hasNext()) {
			sortedValues.add((SimpleInstance) valuesIterator.next());
		}

		Collections.sort(sortedValues, slotMentionComparator);

		super.setValues(sortedValues);
	}

	public class SlotMentionComparator implements Comparator<SimpleInstance> {

		public int compare(SimpleInstance mention1, SimpleInstance mention2) {
			if (mentionUtil.isSlotMention(mention1) && mentionUtil.isSlotMention(mention2)) {
				Slot slot1 = mentionUtil.getSlotMentionSlot(mention1);
				Slot slot2 = mentionUtil.getSlotMentionSlot(mention2);

				if (slot1 == null && slot2 == null)
					return 0;
				if (slot1 == null)
					return 1;
				if (slot2 == null)
					return -1;

				String mentionLabel1 = null;
				String mentionLabel2 = null;

				try {
					SimpleInstance mentionedByMention = mentionUtil.getMentionedBy(mention1);
					Cls mentionCls = mentionUtil.getMentionCls(mentionedByMention);
					ClsWidget clsWidget = getProject().getDesignTimeClsWidget(mentionCls);
					SlotWidget slotWidget = clsWidget.getSlotWidget(slot1);
					mentionLabel1 = slotWidget.getLabel();
				} catch (Exception e) {
				}

				if (mentionLabel1 == null)
					mentionLabel1 = slot1.getBrowserText();

				try {
					SimpleInstance mentionedByMention = mentionUtil.getMentionedBy(mention2);
					Cls mentionCls = mentionUtil.getMentionCls(mentionedByMention);
					ClsWidget clsWidget = getProject().getDesignTimeClsWidget(mentionCls);
					SlotWidget slotWidget = clsWidget.getSlotWidget(slot2);
					mentionLabel2 = slotWidget.getLabel();
				} catch (Exception e) {
				}

				if (mentionLabel2 == null)
					mentionLabel2 = slot2.getBrowserText();

				return mentionLabel1.compareTo(mentionLabel2);
			}
			return 0;
		}
	}

}
