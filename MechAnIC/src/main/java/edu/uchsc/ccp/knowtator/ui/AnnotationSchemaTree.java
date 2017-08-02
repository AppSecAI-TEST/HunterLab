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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.ModelUtilities;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.ui.ParentChildRoot;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.LazyTreeNode;
import edu.stanford.smi.protege.util.LazyTreeRoot;
import edu.stanford.smi.protege.util.Selectable;
import edu.stanford.smi.protege.util.SelectableTree;
import edu.stanford.smi.protege.util.SelectionListener;
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

public class AnnotationSchemaTree extends SelectableTree implements SelectedAnnotationChangeListener,
		SelectionListener, RefreshAnnotationsDisplayListener {
	public void refreshAnnotationsDisplay(boolean scrollToSelection) {
		validate();
		repaint();
	}

	static final long serialVersionUID = 0;

	KnowtatorManager manager;

	KnowledgeBase kb;

	AnnotationUtil annotationUtil;

	MentionUtil mentionUtil;

	KnowtatorProjectUtil kpu;

	TextSourceUtil textSourceUtil;

	BrowserTextUtil browserTextUtil;

	ColorFrameRenderer renderer;

	JPopupMenu popupMenu;

	Logger logger = Logger.getLogger(AnnotationSchemaTree.class);

	public AnnotationSchemaTree(KnowtatorManager manager, Action doubleClickAction, LazyTreeRoot root) {
		super(doubleClickAction, root);

		initialize(manager);
	}

	private void initialize(KnowtatorManager manager) {
		this.manager = manager;
		mentionUtil = manager.getMentionUtil();
		annotationUtil = manager.getAnnotationUtil();
		kpu = manager.getKnowtatorProjectUtil();
		textSourceUtil = manager.getTextSourceUtil();
		browserTextUtil = manager.getBrowserTextUtil();
		renderer = manager.getRenderer();

		popupMenu = new JPopupMenu();

		setLargeModel(true);
		// setRowHeight(rootCls.getIcon().getIconHeight());
		setSelectionRow(0);
		setAutoscrolls(true);
		setCellRenderer(renderer);
		ToolTipManager.sharedInstance().registerComponent(this);
		addSelectionListener(this);
		addMouseListener(createSelectableListener());

		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kpu.getKnowledgeBase().getProject());
		configuration.addFrameListener(new FrameAdapter() {
			public void ownSlotValueChanged(FrameEvent frameEvent) {
				Slot slot = frameEvent.getSlot();
				if (slot.equals(kpu.getRootClsesSlot())) {
					setRoot(new ParentChildRoot(AnnotationSchemaTree.this.manager.getRootClses()));
				}
			}
		});

		EventHandler.getInstance().addSelectedAnnotationChangeListener(this);
		EventHandler.getInstance().addRefreshAnnotationsDisplayListener(this);
	}

	public void annotationSelectionChanged(SelectedAnnotationChangeEvent sace) {
		SimpleInstance selectedAnnotation = sace.getSelectedAnnotation();
		if (selectedAnnotation == null)
			return;

		SimpleInstance selectedMention = annotationUtil.getMention(selectedAnnotation);

		logger.debug("selected annotation = \"" + browserTextUtil.getBrowserText(selectedAnnotation, 100));
		mentionUtil.initializeSlotMentions(selectedMention);

		Cls selectedCls = mentionUtil.getMentionCls(selectedMention);		

		if (selectedCls != null) {
			java.util.List path = ModelUtilities.getPathToRoot(selectedCls);
			List<Cls> rootClses = manager.getRootClses();
			if (rootClses.size() > 0) {
				boolean baseOfPathFound = false;
				while (!baseOfPathFound && path.size() > 0) {
					for (Cls rootCls : rootClses) {
						if (rootCls.equals(path.get(0))) {
							baseOfPathFound = true;
						}
					}
					if (!baseOfPathFound)
						path.remove(0);
				}
				if (path.size() > 0)
					ComponentUtilities.setSelectedObjectPath(this, path);
			}
		}

	}

	public void selectionChanged(edu.stanford.smi.protege.util.SelectionEvent selectionEvent) {
		Selectable selectable = selectionEvent.getSelectable();
		Collection selection = selectable.getSelection();

		if (selection.size() == 1) {
			Cls selectedCls = (Cls) selection.iterator().next();
			logger.debug("selected cls = \"" + selectedCls + "\"");
			manager.setSelectedCls(selectedCls);
		}
	}

	private MouseListener createSelectableListener() {
		return new MouseAdapter() {

			public void mouseClicked(MouseEvent event) {
				Collection selection = getSelection();
				logger.debug("selection size=" + selection.size());
				if (selection.size() == 1) {
					final Frame selectedFrame = (Frame) selection.iterator().next();

					final TreePath treePath = getPathForLocation(event.getX(), event.getY());
					if (treePath == null) {
						return;
					}
					boolean addCollapseMenuItem = false;
					if (treePath.getPathCount() == 2) {
						Object lastComponent = treePath.getLastPathComponent();
						if (lastComponent instanceof LazyTreeNode) {
							LazyTreeNode node = (LazyTreeNode) lastComponent;
							int childCount = node.getChildCount();
							if (childCount > 0) {
								TreeNode childNode = node.getChildAt(0);
								TreePath childTreePath = treePath.pathByAddingChild(childNode);
								if (!isVisible(childTreePath)) {
									expandPath(treePath);
									return;
								} else
									addCollapseMenuItem = true;
							}
						}
					}

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
							.createSelectAnnotationMenuItems(manager, selectedFrame, AnnotationSchemaTree.this);
					if (selectAnnotationMenuItems != null && selectAnnotationMenuItems.size() > 0)
						popupMenu.addSeparator();
					for (JMenuItem selectAnnotationMenuItem : selectAnnotationMenuItems) {
						popupMenu.add(selectAnnotationMenuItem);
					}

					if (addCollapseMenuItem) {
						popupMenu.addSeparator();
						JMenuItem menuItem = new JMenuItem("collapse root node");
						menuItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent actionEvent) {
								collapsePath(treePath);
							}
						});
						popupMenu.add(menuItem);
					}

					if (popupMenu.getSubElements() != null && popupMenu.getSubElements().length > 0)
						popupMenu.show(event.getComponent(), event.getX() + 10, event.getY());
				}
			}
		};
	}
}

// final SimpleInstance selectedAnnotation = manager.getSelectedAnnotation();
// final Rectangle textBounds = manager.getVisibleTextRect();
// popupMenu.addPopupMenuListener(new PopupMenuListener() {
// public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
//		
// }
// public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
//		
// }
// public void popupMenuCanceled(PopupMenuEvent e) {
// if(manager.getSelectedAnnotation() ==
// selectedAnnotation)
// manager.setVisibleTextRect(textBounds);
// }
// });
