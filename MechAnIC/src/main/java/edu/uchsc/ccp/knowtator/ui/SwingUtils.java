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
 *   Brant Barney <brant.barney@hsc.utah.edu> (Original Author)
 */

package edu.uchsc.ccp.knowtator.ui;

import java.awt.Component;
import java.awt.Container;

/**
 * Class designed to contain various static utility functions that can be useful
 * when creating a Java Swing application.
 * 
 * @author Brant Barney
 */
public class SwingUtils {

	/**
	 * Gets the index of the component inside the given panel.
	 * 
	 * @param container
	 *            The container where the components is located
	 * @param comp
	 *            The components to find the index of
	 * 
	 * @return The index of the component in the given panel, or -1 if the
	 *         component was not found in the panel or the panel contains no
	 *         components.
	 */
	public static int indexOfComponent(Container container, Component comp) {
		Component[] components = container.getComponents();
		for (int i = 0; i < components.length; i++) {
			if (components[i] == comp) {
				return i;
			}
		}

		return -1;
	}
}
