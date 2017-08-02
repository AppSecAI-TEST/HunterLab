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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JMenuItem;

import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.Span;
import edu.uchsc.ccp.knowtator.TextSourceUtil;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;

public class AnnotationSchemaMenuItemFactory {
	public static final int SELECTION_ITEMS_COUNT_THRESHOLD = 20;

	public static JMenuItem createAnnotationMenuItem(final KnowtatorManager manager, final Frame selectedFrame,
			TextSourceUtil textSourceUtil) {
		boolean create = manager.getFilterUtil().isClsLicensedByFilter(manager.getSelectedFilter(), selectedFrame);

		if (create) {
			List<Span> selectedSpans = manager.getSelectedSpans();
			String annotationText = "<html>create <b>" + selectedFrame.getBrowserText() + "</b> annotation</html>";
			if (selectedSpans != null && selectedSpans.size() != 0) {
				try {
					String selectedText = textSourceUtil.getCurrentTextSource().getText(selectedSpans);
					if (selectedText.length() > 30) {
						selectedText = selectedText.substring(0, 30) + "...";
					}
					annotationText = "<html>create <b>" + selectedFrame.getBrowserText() + "</b> annotation with <b>"
							+ selectedText + "</b></html>";
				} catch (TextSourceAccessException tsae) {
					tsae.printStackTrace();
				}
			}

			JMenuItem menuItem = new JMenuItem(annotationText);
			menuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					manager.createAnnotation((Instance) selectedFrame);
				}
			});
			return menuItem;
		}
		return null;
	}

	public static JMenuItem createFastAnnotateMenuItem(final KnowtatorManager manager, final Frame selectedFrame) {
		Frame fastAnnotateFrame = manager.getFastAnnotateFrame();
		JMenuItem fastAnnotateMenuItem = null;
		fastAnnotateMenuItem = new JMenuItem("<html>fast annotate with <b>" + selectedFrame.getBrowserText()
				+ "</b></html>");
		fastAnnotateMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				manager.startFastAnnotate(selectedFrame);
			}
		});

		return fastAnnotateMenuItem;
	}

	/**
	 * Creates and returns the menu item that will remove the selected class
	 * (button) from the fast annotation toolbar.
	 * 
	 * @param manager
	 * @param selectedFrame
	 *            The class that will be removed from the fast annotate toolbar.
	 * 
	 * @return The menu item that will remove the class from the toolbar
	 */
	public static JMenuItem createRemoveClsFromToolbarMenuItem(final KnowtatorManager manager, final Frame selectedFrame) {
		JMenuItem removeClsMenuItem = new JMenuItem("<html>remove <b>" + selectedFrame.getBrowserText()
				+ "</b> from fast annotate toolbar</html>");
		removeClsMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				manager.removeFastAnnotateFrame(selectedFrame);
			}
		});
		return removeClsMenuItem;
	}

	public static List<JMenuItem> createSelectAnnotationMenuItems(final KnowtatorManager manager,
			final Frame selectedFrame, final Component parent) {
		List<JMenuItem> selectAnnotationMenuItems = new ArrayList<JMenuItem>();

		final List<SimpleInstance> clsAnnotations = new ArrayList<SimpleInstance>(manager
				.getCurrentAnnotationsForFrame(selectedFrame));

		boolean showMoreItem = false;

		if (clsAnnotations != null && clsAnnotations.size() > 0) {
			// SimpleInstance selectedAnnotation =
			// manager.getLastSelectedAnnotation();

			Comparator<SimpleInstance> annotationComparator = manager.getSpanUtil().comparator(
					manager.getBrowserTextUtil().comparator());
			Collections.sort(clsAnnotations, annotationComparator);

			List<SimpleInstance> filteredAnnotations = new ArrayList<SimpleInstance>();

			boolean belowVisibleRange = false;
			for (SimpleInstance annotation : clsAnnotations) {
				if (manager.isAnnotationVisible(annotation)) {
					filteredAnnotations.add(annotation);
					belowVisibleRange = true;
				} else if (belowVisibleRange)
					filteredAnnotations.add(annotation);
			}

			//this could happen if none of the annotations are visible - e.g. because they are all spanless 
			//or above the visible range.  We will only add the spanless annotations here because we do not want the 
			//screen jumping around on us.  
			if(filteredAnnotations.size() == 0) {
				for (SimpleInstance annotation : clsAnnotations) {
					List<Span> spans = manager.getAnnotationUtil().getSpans(annotation);
					if(spans == null || spans.size() == 0) {
						filteredAnnotations.add(annotation);
					}
				}
			}
			
			filteredAnnotations = filteredAnnotations.subList(0, Math.min(filteredAnnotations.size(),
					SELECTION_ITEMS_COUNT_THRESHOLD));
			if (filteredAnnotations.size() < clsAnnotations.size())
				showMoreItem = true;

			JMenuItem selectAnnotationMenuItem;

			if (showMoreItem) {
				selectAnnotationMenuItem = new JMenuItem("more ...");
				selectAnnotationMenuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						List<SimpleInstance> chosenAnnotations = AnnotationPicker.pickAnnotationsFromCollection(parent,
								manager, clsAnnotations, "select annotation of type '" + selectedFrame + "'");
						if (chosenAnnotations.size() > 0)
							manager.setSelectedAnnotation(chosenAnnotations.get(0));
					}
				});
				selectAnnotationMenuItems.add(selectAnnotationMenuItem);
			}

			// TODO add the 20 closest annotations to the most recently selected
			// annotation.
			for (int i = 0; i < filteredAnnotations.size(); i++) {
				final SimpleInstance annotation = filteredAnnotations.get(i);
				selectAnnotationMenuItem = new JMenuItem("select: "
						+ manager.getBrowserTextUtil().getBrowserText(annotation, 30));
				selectAnnotationMenuItem.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionEvent) {
						manager.setSelectedAnnotation(annotation);
					}
				});
				selectAnnotationMenuItem.addMouseListener(new MouseAdapter() {
					public void mouseEntered(MouseEvent event) {
						manager.getTextPane().highlightAnnotationTemp(annotation);
						manager.setNotifyText(manager.getBrowserTextUtil().getBrowserText(annotation));
					}

					public void mouseExited(MouseEvent event) {
						manager.refreshAnnotationsDisplay(true);
					}
				});
				selectAnnotationMenuItems.add(selectAnnotationMenuItem);
			}
		}
		return selectAnnotationMenuItems;
	}
}

// if(selectedAnnotation != null)
// {
// Comparator<SimpleInstance> positionComparator =
// manager.getPositionComparator(selectedAnnotation);
// Collections.sort(clsAnnotations, positionComparator);
// while(clsAnnotations.size() > SELECTION_ITEMS_COUNT_THRESHOLD)
// clsAnnotations.remove(SELECTION_ITEMS_COUNT_THRESHOLD);
// }

