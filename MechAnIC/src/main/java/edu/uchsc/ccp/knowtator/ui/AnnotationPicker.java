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
package edu.uchsc.ccp.knowtator.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.ui.ListFinder;
import edu.stanford.smi.protege.util.Assert;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.DisplayColors;
import edu.uchsc.ccp.knowtator.KnowtatorManager;

/**
 * The following code was copied and modified from
 * edu.stanford.smi.protege.ui.DisplayUtilities written by the Protege team.
 */

public class AnnotationPicker extends JComponent implements MouseMotionListener, ListSelectionListener {
	static final long serialVersionUID = 0;

	Logger logger = Logger.getLogger(AnnotationPicker.class);

	private JList _list;

	List<SimpleInstance> annotations;

	int mouseOverIndex = -1;

	ListCellRenderer renderer;

	KnowtatorManager manager;

	DisplayColors displayColors;

	AnnotationUtil annotationUtil;

	public AnnotationPicker(KnowtatorManager manager, List<SimpleInstance> annotations, boolean allowMultipleSelection) {
		this.manager = manager;
		displayColors = manager.getDisplayColors();
		annotationUtil = manager.getAnnotationUtil();

		renderer = manager.getRenderer();

		this.annotations = annotations;
		// Collections.sort(slotList, new FrameComparator());
		_list = ComponentFactory.createList(ModalDialog.getCloseAction(this));
		_list.setListData(annotations.toArray());
		_list.setCellRenderer(renderer);
		_list.addMouseMotionListener(this);
		_list.addListSelectionListener(this);
		if (!allowMultipleSelection)
			_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setLayout(new BorderLayout());
		add(new JScrollPane(_list), BorderLayout.CENTER);
		add(new ListFinder(_list, "Find"), BorderLayout.SOUTH);
		setPreferredSize(new Dimension(300, 300));
	}

	public Collection getSelection() {
		return ComponentUtilities.getSelection(_list);
	}

	public void mouseDragged(MouseEvent e) {
	}

	public void mouseMoved(MouseEvent e) {
		if (_list.getSelectedIndex() != -1)
			return;
		Point p = e.getPoint();
		int index = _list.locationToIndex(p);
		if (index != mouseOverIndex) {
			mouseOverIndex = index;
			SimpleInstance annotation = (SimpleInstance) annotations.get(index);
			manager.getTextPane().highlightAnnotationTemp(annotation);
		}
	}

	public void valueChanged(ListSelectionEvent arg0) {
		int index = _list.getSelectedIndex();
		if (index != mouseOverIndex) {
			mouseOverIndex = index;
			SimpleInstance annotation = (SimpleInstance) annotations.get(index);
			manager.getTextPane().highlightAnnotationTemp(annotation);
		}
	}

	public static List<SimpleInstance> pickAnnotationsFromCollection(Component component, KnowtatorManager manager,
			List<SimpleInstance> annotations, String label) {
		return pickAnnotationsFromCollection(component, manager, annotations, label, true);
	}

	public static List<SimpleInstance> pickAnnotationsFromCollection(Component component, KnowtatorManager manager,
			List<SimpleInstance> annotations, String label, boolean allowMultipleSelection) {
		AnnotationPicker panel = new AnnotationPicker(manager, annotations, allowMultipleSelection);

		Collection pickedAnnotations = Collections.EMPTY_LIST;
		int result = ModalDialog.showDialog(component, panel, label, ModalDialog.MODE_OK_CANCEL, null);
		switch (result) {
		case ModalDialog.OPTION_OK:
			pickedAnnotations = panel.getSelection();
			break;
		case ModalDialog.OPTION_CANCEL:
			break;
		default:
			Assert.fail("bad result: " + result);
		}
		List<SimpleInstance> returnValues = new ArrayList<SimpleInstance>();
		for (Object obj : pickedAnnotations) {
			returnValues.add((SimpleInstance) obj);
		}
		return returnValues;
	}

}
