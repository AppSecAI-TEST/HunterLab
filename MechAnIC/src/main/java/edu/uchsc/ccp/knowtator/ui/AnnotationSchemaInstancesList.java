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

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.ui.FrameComparator;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.SelectableList;
import edu.stanford.smi.protege.util.SimpleListModel;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.BrowserTextUtil;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.MentionUtil;
import edu.uchsc.ccp.knowtator.ProjectSettings;
import edu.uchsc.ccp.knowtator.TextSourceUtil;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.event.RefreshAnnotationsDisplayListener;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeEvent;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeListener;
import edu.uchsc.ccp.knowtator.event.SelectedClsChangeEvent;
import edu.uchsc.ccp.knowtator.event.SelectedClsChangeListener;

public class AnnotationSchemaInstancesList extends SelectableList implements SelectedAnnotationChangeListener,
		RefreshAnnotationsDisplayListener {
	KnowtatorManager manager;

	KnowledgeBase kb;

	KnowtatorProjectUtil kpu;

	AnnotationUtil annotationUtil;

	MentionUtil mentionUtil;

	TextSourceUtil textSourceUtil;

	BrowserTextUtil browserTextUtil;

	ColorFrameRenderer renderer;

	JPopupMenu popupMenu;

	public AnnotationSchemaInstancesList(KnowtatorManager manager) {
		super();
		initialize(manager);
	}

	private void initialize(KnowtatorManager manager) {
		this.manager = manager;
		setModel(new SimpleListModel());

		kb = manager.getKnowledgeBase();
		kpu = manager.getKnowtatorProjectUtil();
		renderer = manager.getRenderer();
		annotationUtil = manager.getAnnotationUtil();
		mentionUtil = manager.getMentionUtil();
		textSourceUtil = manager.getTextSourceUtil();
		browserTextUtil = manager.getBrowserTextUtil();

		setCellRenderer(renderer);

		popupMenu = new JPopupMenu();

		addMouseListener(createSelectableListener());
		EventHandler.getInstance().addSelectedClsChangeListener(new SelectedClsChangeListener() {
			public void clsSelectionChanged(SelectedClsChangeEvent scce) {
				Cls selectedCls = AnnotationSchemaInstancesList.this.manager.getSelectedCls();
				ArrayList instances = new ArrayList(selectedCls.getDirectInstances());
				Collections.sort(instances, new FrameComparator());
				if (ProjectSettings.getShowInstances(getKb().getProject())) {
					((SimpleListModel) (AnnotationSchemaInstancesList.this.getModel())).setValues(instances);
				}
			}
		});

		EventHandler.getInstance().addSelectedAnnotationChangeListener(this);
		EventHandler.getInstance().addRefreshAnnotationsDisplayListener(this);

	}

	private KnowledgeBase getKb() {
		return kb;
	}

	private MouseListener createSelectableListener() {
		return new MouseAdapter() {

			public void mouseClicked(MouseEvent event) {
				if (event.isPopupTrigger())
					return;
				Collection selection = getSelection();

				if (selection.size() == 1) {
					final Frame selectedFrame = (Frame) selection.iterator().next();

					int index = locationToIndex(event.getPoint());
					Rectangle cellBounds = getCellBounds(index, index);
					if (!cellBounds.contains(event.getPoint()))
						return;

					popupMenu.removeAll();
					JMenuItem createAnnotationMenuItem = AnnotationSchemaMenuItemFactory.createAnnotationMenuItem(
							manager, selectedFrame, textSourceUtil);
					if (createAnnotationMenuItem != null) {
						popupMenu.add(createAnnotationMenuItem);
						popupMenu.addSeparator();
						JMenuItem fastAnnotateMenuItem = AnnotationSchemaMenuItemFactory.createFastAnnotateMenuItem(
								manager, selectedFrame);
						popupMenu.add(fastAnnotateMenuItem);
						if (manager.isFastAnnotateMode() && manager.fastAnnotateToolBarContains(selectedFrame)) {
							JMenuItem removeItem = AnnotationSchemaMenuItemFactory.createRemoveClsFromToolbarMenuItem(
									manager, selectedFrame);
							popupMenu.add(removeItem);
						}

					} else {
						popupMenu.add(new JMenuItem(
								"The current filter does not allow creation of annotations of this type."));
					}
					List<JMenuItem> selectAnnotationMenuItems = AnnotationSchemaMenuItemFactory
							.createSelectAnnotationMenuItems(manager, selectedFrame, AnnotationSchemaInstancesList.this);
					if (selectAnnotationMenuItems != null && selectAnnotationMenuItems.size() > 0)
						popupMenu.addSeparator();
					for (JMenuItem selectAnnotationMenuItem : selectAnnotationMenuItems) {
						popupMenu.add(selectAnnotationMenuItem);
					}

					if (popupMenu.getSubElements() != null && popupMenu.getSubElements().length > 0)
						popupMenu.show(event.getComponent(), event.getX() + 10, event.getY());
				}
			}
		};
	}

	public void annotationSelectionChanged(SelectedAnnotationChangeEvent sace) {
		SimpleInstance annotation = sace.getSelectedAnnotation();
		if (annotation == null)
			return;
		SimpleInstance selectedMention = annotationUtil.getMention(annotation);

		mentionUtil.initializeSlotMentions(selectedMention);

		SimpleInstance mentionInstance = mentionUtil.getMentionInstance(selectedMention);
		if (mentionInstance != null) {
			ComponentUtilities.setSelectedValue(this, mentionInstance);
		} else
			clearSelection();
	}

	public void refreshAnnotationsDisplay(boolean scrollToSelection) {
		validate();
		repaint();
	}
}
