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

package edu.uchsc.ccp.knowtator.textsource;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.border.TitledBorder;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.uchsc.ccp.knowtator.TextSourceUtil;

public class TextSourceSelector extends JPanel implements ActionListener, TextSourceChangeListener,
		TextSourceCollectionChangeListener {

	private static final long serialVersionUID = 6744321626988920914L;

	JPanel panel;

	JLabel textSourceCollectionLabel;

	JButton previousTextSourceButton;

	JButton nextTextSourceButton;

	JButton selectTextSourceButton;

	JButton findTextSourceButton;

	JButton openTextSourceCollectionButton;

	TitledBorder border;

	TextSourceUtil textSourceUtil;

	Project project;

	Component parent;

	JToolBar toolBar;

	/** Creates a new instance of TextSourceSelector */
	public TextSourceSelector(Project project, TextSourceUtil textSourceUtil, JToolBar toolBar, Component parent) {
		super(new FlowLayout());

		this.project = project;
		this.textSourceUtil = textSourceUtil;
		this.toolBar = toolBar;
		this.parent = parent;

		textSourceUtil.addTextSourceChangeListener(this);
		textSourceUtil.addTextSourceCollectionChangeListener(this);

		textSourceCollectionLabel = new JLabel("Text Source Collection: ");

		panel = new JPanel();
		previousTextSourceButton = new JButton(ComponentUtilities.loadImageIcon(TextSourceSelector.class,
				"/edu/uchsc/ccp/knowtator/images/prev.gif"));
		previousTextSourceButton.addActionListener(this);
		previousTextSourceButton.setEnabled(false);
		previousTextSourceButton.setToolTipText("previous text source");
		nextTextSourceButton = new JButton(new NextTextAction());
		nextTextSourceButton.setEnabled(false);
		nextTextSourceButton.setToolTipText("next text source");
		openTextSourceCollectionButton = new JButton(ComponentUtilities.loadImageIcon(TextSourceSelector.class,
				"/edu/uchsc/ccp/knowtator/images/Open24.gif"));
		openTextSourceCollectionButton.setToolTipText("Open text source collection");
		openTextSourceCollectionButton.addActionListener(this);
		selectTextSourceButton = new JButton(ComponentUtilities.loadImageIcon(TextSourceSelector.class,
				"/edu/uchsc/ccp/knowtator/images/History24.gif"));
		selectTextSourceButton.addActionListener(this);
		selectTextSourceButton.setEnabled(false);
		selectTextSourceButton.setToolTipText("Select text source from list");
		findTextSourceButton = new JButton(ComponentUtilities.loadImageIcon(TextSourceSelector.class,
				"/edu/uchsc/ccp/knowtator/images/Find24.gif"));
		findTextSourceButton.addActionListener(this);
		findTextSourceButton.setEnabled(false);
		findTextSourceButton.setToolTipText("Search for text source");

		// panel.add(previousTextSourceButton);
		// panel.add(nextTextSourceButton);
		// panel.add(selectTextSourceButton);
		// panel.add(findTextSourceButton);
		// panel.add(openTextSourceCollectionButton);

		toolBar.add(textSourceCollectionLabel);
		toolBar.addSeparator();
		toolBar.add(previousTextSourceButton);
		toolBar.addSeparator();
		toolBar.add(selectTextSourceButton);
		toolBar.addSeparator();
		toolBar.add(nextTextSourceButton);

		toolBar.addSeparator();
		toolBar.add(findTextSourceButton);
		toolBar.addSeparator();
		toolBar.add(openTextSourceCollectionButton);

		// setLayout(new GridLayout(1,1));
		// add(panel);

		// border =
		// BorderFactory.createTitledBorder("Text source collection: ");
		// setBorder(border);

	}

	public void dispose() {
		toolBar.remove(textSourceCollectionLabel);
		toolBar.remove(previousTextSourceButton);
		toolBar.remove(nextTextSourceButton);
		toolBar.remove(selectTextSourceButton);
		toolBar.remove(findTextSourceButton);
		toolBar.remove(openTextSourceCollectionButton);

		// remove the separators.
		toolBar.remove(toolBar.getComponent(toolBar.getComponentCount() - 1));
		toolBar.remove(toolBar.getComponent(toolBar.getComponentCount() - 1));
		toolBar.remove(toolBar.getComponent(toolBar.getComponentCount() - 1));
		toolBar.remove(toolBar.getComponent(toolBar.getComponentCount() - 1));
		toolBar.remove(toolBar.getComponent(toolBar.getComponentCount() - 1));

		previousTextSourceButton.removeActionListener(this);
		nextTextSourceButton.removeActionListener(this);
		selectTextSourceButton.removeActionListener(this);
		findTextSourceButton.removeActionListener(this);
		openTextSourceCollectionButton.removeActionListener(this);

		panel = null;
		textSourceCollectionLabel = null;
		previousTextSourceButton = null;
		nextTextSourceButton = null;
		selectTextSourceButton = null;
		findTextSourceButton = null;
		openTextSourceCollectionButton = null;
		border = null;

		textSourceUtil.removeTextSourceChangeListener(this);
		textSourceUtil.removeTextSourceCollectionChangeListener(this);

		project = null;
		toolBar = null;
	}

	public void textSourceChanged(TextSourceChangeEvent event) {
		TextSource textSource = event.getTextSource();
		if (textSource != null) {
			textSourceUtil.getTextSourceInstance(textSource, true);
			int textSourceIndex = textSourceUtil.getCurrentTextSourceCollection().getIndex(textSource);
			previousTextSourceButton.setEnabled(textSourceIndex > 0);
			nextTextSourceButton
					.setEnabled(textSourceIndex < (textSourceUtil.getCurrentTextSourceCollection().size() - 1));
		}
	}

	public void textSourceCollectionChanged(TextSourceCollectionChangeEvent event) {
		TextSourceCollection tsc = event.getTextSourceCollection();
		// border.setTitle("Text source collection: "+ tsc.getName());
		textSourceCollectionLabel.setText("Text source collection: " + tsc.getName());
		selectTextSourceButton.setEnabled(true);
		findTextSourceButton.setEnabled(true);
	}

	public void actionPerformed(ActionEvent actionEvent) {
		Object source = actionEvent.getSource();
		if (source == openTextSourceCollectionButton) {
			TextSourceCollection selectedTSC = textSourceUtil.selectTextSourceCollection(parent);
			if (selectedTSC != null)
				textSourceUtil.setCurrentTextSourceCollection(selectedTSC);
		} else if (source == previousTextSourceButton) {
			TextSource textSource = textSourceUtil.getCurrentTextSource();
			TextSourceCollection tsc = textSourceUtil.getCurrentTextSourceCollection();
			int textSourceIndex = tsc.getIndex(textSource);
			try {
				textSourceUtil.setCurrentTextSource(tsc.get(textSourceIndex - 1));
			} catch (TextSourceAccessException tsae) {
				tsae.printStackTrace();
			}
		} else if (source == selectTextSourceButton) {
			TextSource textSource = textSourceUtil.getCurrentTextSourceCollection().select(parent);
			textSourceUtil.setCurrentTextSource(textSource);
		} else if (source == findTextSourceButton) {
			TextSource textSource = textSourceUtil.getCurrentTextSourceCollection().find(parent);
			textSourceUtil.setCurrentTextSource(textSource);
		}
	}
	
	@SuppressWarnings("serial")
	class NextTextAction extends AbstractAction {
		public NextTextAction() {
			super("", ComponentUtilities.loadImageIcon(TextSourceSelector.class,
					"/edu/uchsc/ccp/knowtator/images/next.gif"));
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_DOWN_MASK));
		}

		public void actionPerformed(ActionEvent evt) {
			TextSource textSource = textSourceUtil.getCurrentTextSource();
			TextSourceCollection tsc = textSourceUtil.getCurrentTextSourceCollection();
			int textSourceIndex = tsc.getIndex(textSource);
			try {
				textSourceUtil.setCurrentTextSource(tsc.get(textSourceIndex + 1));
			} catch (TextSourceAccessException tsae) {
				tsae.printStackTrace();
			}
		}
	}

}
