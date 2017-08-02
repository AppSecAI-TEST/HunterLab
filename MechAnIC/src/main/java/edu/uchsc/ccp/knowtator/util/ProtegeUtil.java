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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.widget.ClsWidget;
import edu.stanford.smi.protege.widget.SlotWidget;

public class ProtegeUtil {

	/**
	 * It seems like there should be a better way of accomplishing this. If you
	 * have suggestions please send them to me!
	 * 
	 * @param simpleInstances
	 */
	public static Collection<SimpleInstance> castSimpleInstances(Collection simpleInstances) {
		// return Collections.checkedCollection(simpleInstances,
		// SimpleInstance.class);
		List<SimpleInstance> returnValues = new ArrayList<SimpleInstance>();
		Iterator simpleInstancesIt = simpleInstances.iterator();
		while (simpleInstancesIt.hasNext()) {
			returnValues.add((SimpleInstance) simpleInstancesIt.next());
		}
		return returnValues;
	}

	public static Collection<String> castStrings(Collection strings) {
		List<String> returnValues = new ArrayList<String>();
		Iterator stringsIt = strings.iterator();
		while (stringsIt.hasNext()) {
			returnValues.add((String) stringsIt.next());
		}
		return returnValues;
	}

	public static String getSlotLabel(Cls cls, Slot slot, Project project) {
		String slotLabel = null;
		try {
			ClsWidget clsWidget = project.getDesignTimeClsWidget(cls);
			if (clsWidget != null) {
				SlotWidget slotWidget = clsWidget.getSlotWidget(slot);
				if (slotWidget != null)
					slotLabel = slotWidget.getLabel();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (slotLabel != null)
			return slotLabel;
		else
			return slot.getBrowserText();

	}
}
