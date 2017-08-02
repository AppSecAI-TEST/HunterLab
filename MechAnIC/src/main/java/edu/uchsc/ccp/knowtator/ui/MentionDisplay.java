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
package edu.uchsc.ccp.knowtator.ui;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.ui.InstanceDisplay;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.MentionUtil;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeEvent;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeListener;

public class MentionDisplay extends InstanceDisplay implements SelectedAnnotationChangeListener {
	static final long serialVersionUID = 0;

	MentionUtil mentionUtil;

	AnnotationUtil annotationUtil;

	public MentionDisplay(KnowtatorManager manager, Project project) {
		super(project, false, false);
		
		mentionUtil = manager.getMentionUtil();
		annotationUtil = manager.getAnnotationUtil();
		
		registerListeners();
	}

	public MentionDisplay(MentionUtil mentionUtil, AnnotationUtil annotationUtil, Project project) {
		super(project);
		
		this.mentionUtil = mentionUtil;
		this.annotationUtil = annotationUtil;
		
		registerListeners();
	}
	
	/**
	 * Registers the necessary listeners for this MentionDisplay. Designed in this manner
	 *   so it can by overridden in a child class for customization.
	 */
	protected void registerListeners() {
		EventHandler.getInstance().addSelectedAnnotationChangeListener(this);
	}

	public void annotationSelectionChanged(SelectedAnnotationChangeEvent sace) {		
		SimpleInstance annotation = sace.getSelectedAnnotation();
		if (annotation != null) {
			SimpleInstance selectedMention = annotationUtil.getMention(annotation);
			mentionUtil.initializeSlotMentions(selectedMention);
			setInstance(selectedMention);
		} else {
			setInstance(null);
		}
	}
}
