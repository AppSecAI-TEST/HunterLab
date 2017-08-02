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
package edu.uchsc.ccp.knowtator.wizards;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.ui.DisplayUtilities;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentFactory;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.SelectableList;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.Span;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.event.SelectedSpanChangeEvent;
import edu.uchsc.ccp.knowtator.event.SelectedSpanChangeListener;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;
import edu.uchsc.ccp.knowtator.textsource.TextSourceChangeEvent;
import edu.uchsc.ccp.knowtator.textsource.TextSourceChangeListener;
import edu.uchsc.ccp.knowtator.ui.KnowtatorTextPane;

public class FindAnnotateDialog extends JDialog implements ActionListener, TextSourceChangeListener, CaretListener,
		SelectedSpanChangeListener

{
	static final long serialVersionUID = 0;

	JLabel searchForLabel;

	// TODO this should probably be a drop down
	JTextField searchStringTextField;

	JLabel annotateWithLabel;

	SelectableList annotationClsList;

	JButton selectClsButton;

	JCheckBox regularExpressionCheckBox;

	JCheckBox matchCaseCheckBox;

	JCheckBox matchWordsCheckBox;

	JCheckBox spanCapturingGroupsCheckBox;

	JButton findNextButton;

	JButton findPreviousButton;

	JButton annotateButton;

	JButton annotateAllButton;

	JButton undoButton;

	JButton closeButton;

	Cls annotationCls;

	Project project;

	JFrame parent;

	KnowtatorManager manager;

	KnowtatorProjectUtil kpu;

	String text;

	int searchMark = 0;

	KnowtatorTextPane textPane;

	Matcher matcher;

	boolean currentlySelectedSpanAlreadyAnnotated = false;

	public FindAnnotateDialog(Project project, JFrame parent, KnowtatorTextPane textPane, KnowtatorManager manager) {
		super(parent, "Find/Annotate", false);
		this.project = project;
		this.parent = parent;
		this.textPane = textPane;
		this.manager = manager;
		initGUI();
		EventHandler.getInstance().addSelectedSpanChangeListener(this);
	}

	protected void initGUI() {
		setSize(630, 180);
		setLocation(WizardUtil.getCentralDialogLocation(parent, this));

		searchForLabel = new JLabel("Search for");
		searchForLabel.setDisplayedMnemonic('s');

		searchStringTextField = new JTextField();
		searchStringTextField.addCaretListener(createCaretListener());
		searchForLabel.setLabelFor(searchStringTextField);

		annotateWithLabel = new JLabel("Annotate with");
		annotateWithLabel.setDisplayedMnemonic('o');
		annotateWithLabel.setLabelFor(selectClsButton);

		annotationClsList = ComponentFactory.createSingleItemList(null);
		annotationClsList.setCellRenderer(manager.getRenderer());

		selectClsButton = new JButton("Choose class");
		selectClsButton.addActionListener(this);
		selectClsButton.setMnemonic('o');

		regularExpressionCheckBox = new JCheckBox("Regular expression search");
		regularExpressionCheckBox.setMnemonic('r');
		regularExpressionCheckBox.addActionListener(this);
		matchCaseCheckBox = new JCheckBox("Match case");
		matchCaseCheckBox.setMnemonic('m');
		matchCaseCheckBox.addActionListener(this);
		matchWordsCheckBox = new JCheckBox("Whole words only");
		matchWordsCheckBox.setMnemonic('w');
		matchWordsCheckBox.addActionListener(this);
		spanCapturingGroupsCheckBox = new JCheckBox("Annotate capturing groups");
		spanCapturingGroupsCheckBox.setMnemonic('g');
		spanCapturingGroupsCheckBox.setEnabled(false);

		findNextButton = new JButton("Find next");
		findNextButton.setMnemonic('f');
		findNextButton.addActionListener(this);
		findNextButton.setEnabled(false);
		findPreviousButton = new JButton("Find previous");
		findPreviousButton.setMnemonic('p');
		findPreviousButton.addActionListener(this);
		findPreviousButton.setEnabled(false);
		annotateButton = new JButton("Annotate");
		annotateButton.setMnemonic('a');
		annotateButton.addActionListener(this);
		annotateButton.setEnabled(false);
		annotateAllButton = new JButton("Annotate all");
		annotateAllButton.setMnemonic('l');
		annotateAllButton.addActionListener(this);
		annotateAllButton.setEnabled(false);
		undoButton = new JButton("Undo");
		undoButton.setMnemonic('u');
		undoButton.setEnabled(false);
		undoButton.addActionListener(this);
		closeButton = new JButton("Close");
		closeButton.setMnemonic('c');
		closeButton.addActionListener(this);

		JPanel searchPanel = new JPanel(new GridBagLayout());
		searchPanel.add(searchForLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 4, 4));
		searchPanel.add(searchStringTextField, new GridBagConstraints(1, 0, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 4, 4));
		searchPanel.add(annotateWithLabel, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 4, 4));
		searchPanel.add(annotationClsList, new GridBagConstraints(1, 1, 1, 1, 1.0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		searchPanel.add(selectClsButton, new GridBagConstraints(2, 1, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 4, 4));

		JPanel buttonsPanel = new JPanel(new GridBagLayout());
		buttonsPanel.add(findNextButton, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
		buttonsPanel.add(findPreviousButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
		buttonsPanel.add(annotateButton, new GridBagConstraints(0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
		buttonsPanel.add(annotateAllButton, new GridBagConstraints(1, 1, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
		buttonsPanel.add(undoButton, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));
		buttonsPanel.add(closeButton, new GridBagConstraints(1, 2, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 0, 0));

		JPanel checkBoxesPanel = new JPanel(new GridBagLayout());
		checkBoxesPanel.add(regularExpressionCheckBox, new GridBagConstraints(0, 0, 1, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		checkBoxesPanel.add(matchCaseCheckBox, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		checkBoxesPanel.add(matchWordsCheckBox, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));
		checkBoxesPanel.add(spanCapturingGroupsCheckBox, new GridBagConstraints(3, 0, 1, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 0, 0));

		JPanel dialogPanel = new JPanel(new GridBagLayout());
		dialogPanel.add(searchPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(10, 10, 0, 20), 0, 0));
		dialogPanel.add(buttonsPanel, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(10, 0, 0, 10), 0, 0));
		dialogPanel.add(checkBoxesPanel, new GridBagConstraints(0, 2, 2, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));

		setLayout(new BorderLayout());
		add(dialogPanel, BorderLayout.CENTER);

	}

	public void setVisible(boolean visible) {
		enableButtons();
		super.setVisible(visible);
	}

	private void enableButtons() {
		String searchString = searchStringTextField.getText();

		if (!searchString.equals("")) {
			findNextButton.setEnabled(true);
			findPreviousButton.setEnabled(true);
			// annotateAllButton.setEnabled(true);
		} else {
			findNextButton.setEnabled(false);
			findPreviousButton.setEnabled(false);
			// annotateAllButton.setEnabled(false);
		}
		List<Span> selectedSpans = manager.getSelectedSpans();
		if (selectedSpans.size() > 0) {
			annotateButton.setEnabled(true);
		} else {
			annotateButton.setEnabled(false);
		}
		if (regularExpressionCheckBox.isSelected()) {
			spanCapturingGroupsCheckBox.setEnabled(true);
		} else
			spanCapturingGroupsCheckBox.setEnabled(false);
	}

	private CaretListener createCaretListener() {
		return new CaretListener() {
			public void caretUpdate(CaretEvent caretEvent) {
				enableButtons();
			}
		};
	}

	private Pattern preparePattern() throws PatternSyntaxException {
		boolean regex = regularExpressionCheckBox.isSelected();
		boolean matchCase = matchCaseCheckBox.isSelected();
		boolean matchWord = matchWordsCheckBox.isSelected();
		String userEntry = searchStringTextField.getText();

		int mask = 0;
		String prefix = "";
		String base = "";
		String suffix = "";

		if (!matchCase) {
			mask = Pattern.CASE_INSENSITIVE;
		}
		if (matchWord) {
			prefix = "(?:^|\\W+)(";
			suffix = ")(?:\\W+|$)";
		} else {
			prefix = "(";
			suffix = ")";
		}
		if (regex) {
			base = userEntry;
		} else {
			base = Pattern.quote(userEntry);
		}

		return Pattern.compile(prefix + base + suffix, mask);
	}

	private void findNext() {
		reset();// we only need to reset if the search string has changed.
		find(true);
	}

	private void findPrevious() {
		reset();// we only need to reset if the search string has changed.
		find(false);
	}

	private void reset() {
		try {
			Pattern pattern = preparePattern();
			matcher = pattern.matcher(text);
		} catch (PatternSyntaxException pse) {
			pse.printStackTrace();
			JOptionPane.showMessageDialog(this, "Search string is not a valid regular expression.");
		}
	}

	private void found(Matcher matcher) {
		currentlySelectedSpanAlreadyAnnotated = false;
		textPane.clearSelectionHighlights();
		textPane.hideHighlights();
		if (!spanCapturingGroupsCheckBox.isSelected() || matcher.groupCount() == 1) {
			if (matcher.group(1) != null) {
				int start = matcher.start(1);
				int end = matcher.end(1);
				if (textPane.select(new Span(start, end), true)) {
					currentlySelectedSpanAlreadyAnnotated = true;
					Toolkit.getDefaultToolkit().beep();
				}
				searchMark = end;
			}
		} else {
			boolean spanHighlighted = false;
			for (int i = 2; i <= matcher.groupCount(); i++) {
				int start = matcher.start(i);
				int end = matcher.end(i);

				if (start <= end && start >= 0) {
					spanHighlighted = true;
					if (textPane.select(new Span(start, end), true)) {
						currentlySelectedSpanAlreadyAnnotated = true;
						Toolkit.getDefaultToolkit().beep();
					}
					searchMark = end;
				} else {
					searchMark = matcher.end();
				}
			}
			if (!spanHighlighted) {
				JOptionPane.showMessageDialog(this,
						"The search string was found but none of the capturing groups were matched.\n"
								+ "Please consider revising your search string or\n"
								+ "unchecking 'Annotate capturing groups'.", "capturing groups not matched",
						JOptionPane.WARNING_MESSAGE);
			}
		}
		textPane.showSelectionHighlights();
		textPane.showAnnotationHighlights();
	}

	private void find(boolean forward) {
		if (forward) {
			if (matcher.find(searchMark)) {
				found(matcher);
			} else if (searchMark != 0) {
				searchMark = 0;
				Toolkit.getDefaultToolkit().beep();
				find(true);
			} else {
				JOptionPane.showMessageDialog(this, "Search string was not found");
			}
		} else {
			int previousStart = -1;
			matcher.reset();
			while (matcher.find()) {
				int start = matcher.start();
				int end = matcher.end();
				if (end < searchMark) {
					previousStart = start;
				} else {
					if (previousStart > -1 && matcher.find(previousStart)) {
						found(matcher);
						searchMark = previousStart + 1; // add plus one -
														// otherwise it will
														// take two 'next finds'
														// to get to the next
														// find.
						enableButtons();
						return;
					}
				}
			}

			matcher.reset();
			while (matcher.find()) {
				previousStart = matcher.start();
			}
			if (matcher.find(previousStart)) {
				found(matcher);
				searchMark = previousStart + 1;
				Toolkit.getDefaultToolkit().beep();
			}

		}
		enableButtons();
	}

	private void annotate() {
		if (annotationClsList.getSelection().size() == 0) {
			selectCls();
		}
		if (currentlySelectedSpanAlreadyAnnotated) {
			int result = JOptionPane.showConfirmDialog(this,
					"The currently selected text has already been annotated.\n" + "Press OK to create an annotation",
					"duplicate annotation warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.CANCEL_OPTION)
				return;
		}

		Cls cls = (Cls) annotationClsList.getSelectedValue();
		List<Span> selectedSpans = manager.getSelectedSpans();
		if (selectedSpans.size() > 0) {
			manager.createAnnotation(cls);
		}
		searchMark++;
		find(true);
	}

	private void selectCls() {
		List<Cls> rootClses = manager.getRootClses();
		Instance instance = DisplayUtilities.pickCls(this, project.getKnowledgeBase(), rootClses);
		if (instance != null) {
			annotationCls = (Cls) instance;
			ComponentUtilities.setListValues(annotationClsList, CollectionUtilities.createCollection(instance));
			annotationClsList.setSelectedIndex(0);
			enableButtons();
		}
	}

	private void close() {
		searchMark = 0;
		textPane.showAllHighlights();
		setVisible(false);
	}

	public void actionPerformed(ActionEvent actionEvent) {
		Object source = actionEvent.getSource();
		if (source == annotateButton) {
			annotate();
		}
		if (source == findNextButton) {
			findNext();
		}
		if (source == findPreviousButton) {
			findPrevious();
		}
		if (source == selectClsButton) {
			selectCls();
		}
		if (source == closeButton) {
			close();
		}
		if (source == regularExpressionCheckBox || source == matchCaseCheckBox || source == matchWordsCheckBox)
			reset();
		if (source == regularExpressionCheckBox) {
			enableButtons();
		}
	}

	public void textSourceChanged(TextSourceChangeEvent event) {
		try {
			if (event.getTextSource() != null)
				text = event.getTextSource().getText();
		} catch (TextSourceAccessException tsae) {
			close();
		}
	}

	public void caretUpdate(CaretEvent caretEvent) {
		searchMark = caretEvent.getMark();
	}

	public void spanSelectionChanged(SelectedSpanChangeEvent ssce) {
		currentlySelectedSpanAlreadyAnnotated = false;
	}
}

// }
// catch(PatternSyntaxException pse)
// {
// throw new PatternSyntaxException(
// "The string you entered is not a valid regular expression.\n"
// +pse.getDescription(),
// userEntry, pse.getIndex());
// }

//	
// String searchString = searchStringTextField.getText();
// int matchIndex = text.indexOf(searchString, searchMark);
// if(matchIndex != -1)
// {
// int start = matchIndex;
// int end = matchIndex+searchString.length();
// textPane.clearSelectionHighlights();
// textPane.hideHighlights();
// textPane.select(new Span(start,end));
// textPane.showAnnotationHighlights();
// searchMark = end;
// }
// else if (searchMark != 0)
// {
// searchMark = 0;
// Toolkit.getDefaultToolkit().beep();
// find();
