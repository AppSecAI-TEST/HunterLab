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
 *   Philip V. Ogren <philip@ogren.info> (Original Author)
 */
package edu.uchsc.ccp.knowtator.event;

import java.util.EventObject;

import edu.stanford.smi.protege.model.SimpleInstance;

public class FilterChangedEvent extends EventObject {
	static final long serialVersionUID = 0;

	SimpleInstance newFilter;

	boolean consensusMode;

	/**
	 * @param source
	 *            will typically be an instance of KnowtatorManager
	 * @param newFilter
	 */
	public FilterChangedEvent(Object source, SimpleInstance newFilter, boolean consensusMode) {
		super(source);
		this.newFilter = newFilter;
		this.consensusMode = consensusMode;
	}

	public SimpleInstance getNewFilter() {
		return this.newFilter;
	}

	public boolean isConsensusMode() {
		return consensusMode;
	}
}
