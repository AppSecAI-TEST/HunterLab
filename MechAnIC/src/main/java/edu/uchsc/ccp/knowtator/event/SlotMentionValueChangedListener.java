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
 * Copyright (C) 2005-2008.  All Rights Reserved.
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
 *   Brant Barney (Original Author)
 */

package edu.uchsc.ccp.knowtator.event;

/**
 * Interface used for notification when slot mention values
 *   have been changed.
 *   
 * Originally created so these events could update the red border for pair wise
 *   comparisons while in consensus mode.
 * 
 * @author brant
 */
public interface SlotMentionValueChangedListener {
	
	/**
	 * Called when a slot mention value has changed. 
	 * 
	 * Currently there is no specific event about which slot mention has been modified, as it wasn't
	 *   needed when this was created. A SlotMentionValueEvent can be added if the need arises in the
	 *   future.
	 */
	public void slotMentionValueChanged();
}
