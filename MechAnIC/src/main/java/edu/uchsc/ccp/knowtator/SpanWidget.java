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

/**
 * 8/16/2005   pvo  modified handleAddAction such that a message dialog appears if
 *                  there is no span selected instructing the user to select a span
 *                  of text before clicking the add button.
 */

package edu.uchsc.ccp.knowtator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.widget.StringListWidget;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;

public class SpanWidget extends StringListWidget {
	static final long serialVersionUID = 0;

	KnowtatorManager manager;

	KnowtatorProjectUtil kpu;

	AnnotationUtil annotationUtil;

	public void initialize() {
		super.initialize();
		manager = (KnowtatorManager) getKnowledgeBase().getClientInformation(Knowtator.KNOWTATOR_MANAGER);
		kpu = manager.getKnowtatorProjectUtil();
		annotationUtil = manager.getAnnotationUtil();
	}

	protected void handleCreateAction() {
		handleAddAction();
	}

	private List<Span> getIntersectingSpans(Span span, List<Span> spans) {
		List<Span> returnValues = new ArrayList<Span>();
		for (Span spn : spans) {
			if (spn.intersects(span)) {
				returnValues.add(spn);
			}
		}
		return returnValues;
	}

	protected void handleAddAction() {
		try {
			SimpleInstance annotation = (SimpleInstance) getInstance();
			List<Span> selectedSpans = manager.getSelectedSpans();

			if (selectedSpans.size() > 0) {
				List<Span> newSpans = new ArrayList<Span>();
				Set<Span> retiredSpans = new HashSet<Span>();
				List<Span> currentSpans = annotationUtil.getSpans(annotation);
				for (Span selectedSpan : selectedSpans) {
					List<Span> intersectingSpans = getIntersectingSpans(selectedSpan, currentSpans);
					if (intersectingSpans.size() > 0) {
						Span mergedSpan = selectedSpan;
						for (Span intersectingSpan : intersectingSpans) {
							mergedSpan = Span.merge(mergedSpan, intersectingSpan);
						}
						newSpans.add(mergedSpan);
						retiredSpans.addAll(intersectingSpans);
					} else {
						newSpans.add(selectedSpan);
					}
				}

				List<Span> spans = new ArrayList<Span>();
				for (Span currentSpan : currentSpans) {
					if (!retiredSpans.contains(currentSpan)) {
						spans.add(currentSpan);
					}
				}
				spans.addAll(newSpans);
				annotationUtil.setSpans(annotation, spans, null);
				manager.refreshAnnotationsDisplay(true);
			} else {
				JOptionPane.showMessageDialog(null,
						"Select a span of text in the text viewer before clicking the 'add span' button.",
						"Select Span First", JOptionPane.INFORMATION_MESSAGE);
			}
		} catch (InvalidSpanException ise) {
			JOptionPane.showMessageDialog(this, "TextAnnotation has an invalid span value: " + ise.getMessage(),
					"Invalid span", JOptionPane.ERROR_MESSAGE);
		} catch (TextSourceAccessException tsae) {
			JOptionPane.showMessageDialog(null, "There was a problem retrieving the text from the text source: "
					+ tsae.getMessage(), "Text Source Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void handleRemoveAction(Collection instances) {
		try {
			super.handleRemoveAction(instances);
			annotationUtil.setSpans((SimpleInstance) getInstance(), annotationUtil.getSpans(
					(SimpleInstance) getInstance(), true), null);
			manager.refreshAnnotationsDisplay(true);
		} catch (InvalidSpanException ise) {
			JOptionPane.showMessageDialog(this, "TextAnnotation has an invalid span value: " + ise.getMessage(),
					"Invalid span", JOptionPane.ERROR_MESSAGE);
		} catch (TextSourceAccessException tsae) {
			JOptionPane.showMessageDialog(null, "There was a problem retrieving the text from the text source: "
					+ tsae.getMessage(), "Text Source Error", JOptionPane.ERROR_MESSAGE);
		}

	}

	public int[] getSelectedIndices() {
		return getList().getSelectedIndices();
	}

	public void setSelectedIndices(int[] selectedIndices) {
		getList().setSelectedIndices(selectedIndices);
	}

	@Override
	protected void handleViewAction(String str) {
		return;
	}

}
