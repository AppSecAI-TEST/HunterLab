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
package edu.uchsc.ccp.knowtator.wizards;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.ui.DisplayUtilities;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.uchsc.ccp.knowtator.FilterUtil;
import edu.uchsc.ccp.knowtator.KnowtatorManager;

public class CreateConsensusSetWizard implements ActionListener {

	public static final String WELCOME_MESSAGE = "This wizard allows you to create a consensus set from existing, merged annotations. "
			+ "Typically, text sources will be individually annotated in separate projects and then "
			+ "merged together into a single project at which point consensus mode is used.   "
			+ "This action will not delete or modify existing annotations.  A consensus set "
			+ "consists of annotations that have been copied from the set of annotations "
			+ "defined by the filter that you will choose in this wizard.  Consensus mode will then act on these "
			+ "copied annotations.  For more information about "
			+ "how consensus mode works please consult the online documentation. http://knowtator.sourceforge.net//consensus.shtml";

	public static final String FILTER_INSTRUCTIONS = "Please select a filter that describes the set of annotations that you want to create a consensus "
			+ "set for.  The selected filter must have either two annotators or two annotation sets specified. If you "
			+ "have not yet defined an appropriate filter, then please exit out of this wizard and create one.  Please see"
			+ "http://knowtator.sourceforge.net//filters.shtml for information about filters.";

	public static final String NAME_INSTRUCTIONS = "Please enter a descriptive name for your consensus set. If you plan to have "
			+ "only one consensus set in your annotation project, then selecting " + "the default name should be fine.";

	public static final String TEXT_SOURCES_INSTRUCTIONS = "Please select the text sources that you want included in the consensus set creation.";

	public static final String TO_WELCOME_FRAME = "TO_WELCOME_FRAME";

	public static final String TO_NAME_FRAME = "TO_NAME_FRAME";

	public static final String TO_FILTER_FRAME = "TO_FILTER_FRAME";

	public static final String TO_TEXTSOURCES_FRAME = "TO_TEXTSOURCES_FRAME";

	public static final String CREATE_BUTTON_CLICKED = "CREATE_BUTTON_CLICKED";

	SimpleInstance filter;

	String consensusSetName = "consensus set";

	Set<SimpleInstance> textSources;

	boolean createClicked = false;

	WizardFrame welcomeFrame;

	JTextArea welcomeMessageTextArea;

	WizardFrame nameFrame;

	JTextArea nameInstructionsTextArea;

	JLabel nameLabel;

	JTextField nameTextField;

	WizardFrame filterFrame;

	JTextArea filterInstructionsTextArea;

	JLabel filterLabel;

	JTextField filterTextField;

	JButton filterButton;

	WizardFrame textSourcesFrame;

	JTextArea textSourcesInstructionsTextArea;

	JLabel textSourcesLabel;

	JList textSourcesList;

	JButton textSourcesRemoveButton;

	JButton textSourcesAddButton;

	WizardFrame visibleFrame; // one of the above frames

	JFrame parent;

	Dimension wizardSize;

	Point wizardLocation;

	KnowtatorManager manager;

	public CreateConsensusSetWizard(JFrame parent, KnowtatorManager manager) {
		this.parent = parent;
		this.manager = manager;

		textSources = new TreeSet<SimpleInstance>();

		createWelcomeFrame();
		createConsensusNameFrame();
		createFilterFrame();
		createTextSourcesFrame();
	}

	private void createWelcomeFrame() {
		welcomeFrame = new WizardFrame(parent, "consensus set creation wizard", new Dimension(500, 400));
		welcomeFrame.setAlwaysOnTop(false);
		welcomeFrame.previousButton.setEnabled(false);
		welcomeFrame.nextButton.setActionCommand(TO_NAME_FRAME);
		welcomeFrame.nextButton.addActionListener(this);
		welcomeFrame.cancelButton.addActionListener(this);
		welcomeFrame.contentPane.setLayout(new BorderLayout());
		welcomeFrame.setLocation(WizardUtil.getCentralDialogLocation(parent, welcomeFrame));

		welcomeMessageTextArea = new JTextArea();
		welcomeMessageTextArea.setEditable(false);
		welcomeMessageTextArea.setText(WELCOME_MESSAGE);
		welcomeMessageTextArea.setLineWrap(true);
		welcomeMessageTextArea.setWrapStyleWord(true);
		welcomeFrame.contentPane.add(welcomeMessageTextArea, BorderLayout.CENTER);
	}

	private void createConsensusNameFrame() {
		nameFrame = new WizardFrame(parent, "enter a name");
		nameFrame.setAlwaysOnTop(false);
		nameFrame.previousButton.setActionCommand(TO_WELCOME_FRAME);
		nameFrame.previousButton.addActionListener(this);
		nameFrame.nextButton.setActionCommand(TO_FILTER_FRAME);
		nameFrame.nextButton.addActionListener(this);
		nameFrame.cancelButton.addActionListener(this);

		nameInstructionsTextArea = new JTextArea();
		nameInstructionsTextArea.setEditable(false);
		nameInstructionsTextArea.setText(NAME_INSTRUCTIONS);
		nameInstructionsTextArea.setLineWrap(true);
		nameInstructionsTextArea.setWrapStyleWord(true);

		nameLabel = new JLabel("consensus set name ");

		nameTextField = new JTextField(consensusSetName);
		nameTextField.addCaretListener(new CaretListener() {
			public void caretUpdate(CaretEvent e) {
				String name = nameTextField.getText();
				if (name == null || name.trim().equals("")) {
					nameFrame.nextButton.setEnabled(false);
				} else
					nameFrame.nextButton.setEnabled(true);
				consensusSetName = name;
			}
		});

		nameFrame.contentPane.setLayout(new GridBagLayout());
		nameFrame.contentPane.add(nameInstructionsTextArea, new GridBagConstraints(0, 0, 2, 1, 1.0d, 1.0d,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 2, 2));
		nameFrame.contentPane.add(nameLabel, new GridBagConstraints(0, 1, 1, 1, 0d, 0d, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 2, 2));
		nameFrame.contentPane.add(nameTextField, new GridBagConstraints(1, 1, 1, 1, 1.0d, 0d, GridBagConstraints.WEST,
				GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 2, 2));
	}

	private void createFilterFrame() {
		filterFrame = new WizardFrame(parent, "select annotation filter");
		filterFrame.setAlwaysOnTop(false);
		filterFrame.previousButton.setActionCommand(TO_NAME_FRAME);
		filterFrame.previousButton.addActionListener(this);
		filterFrame.nextButton.setActionCommand(TO_TEXTSOURCES_FRAME);
		filterFrame.nextButton.addActionListener(this);
		filterFrame.nextButton.setEnabled(false);
		filterFrame.cancelButton.addActionListener(this);

		filterInstructionsTextArea = new JTextArea();
		filterInstructionsTextArea.setEditable(false);
		filterInstructionsTextArea.setText(FILTER_INSTRUCTIONS);
		filterInstructionsTextArea.setLineWrap(true);
		filterInstructionsTextArea.setWrapStyleWord(true);

		filterLabel = new JLabel("selected filter");
		filterTextField = new JTextField();
		filterTextField.setEditable(false);
		filterButton = new JButton("select filter");
		filterButton.addActionListener(this);

		filterFrame.contentPane.setLayout(new GridBagLayout());
		filterFrame.contentPane.add(filterInstructionsTextArea, new GridBagConstraints(0, 0, 3, 1, 1.0d, 1.0d,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 2, 2));
		filterFrame.contentPane.add(filterLabel, new GridBagConstraints(0, 1, 1, 1, 0d, 0d, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
		filterFrame.contentPane.add(filterTextField, new GridBagConstraints(1, 1, 1, 1, 1.0d, 0d,
				GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(3, 3, 3, 3), 2, 2));
		filterFrame.contentPane.add(filterButton, new GridBagConstraints(2, 1, 1, 1, 0d, 0d, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
	}

	private void createTextSourcesFrame() {
		textSourcesFrame = new WizardFrame(parent, "select text sources");
		textSourcesFrame.setAlwaysOnTop(false);
		textSourcesFrame.previousButton.setActionCommand(TO_FILTER_FRAME);
		textSourcesFrame.previousButton.addActionListener(this);
		textSourcesFrame.nextButton.setText("create");
		textSourcesFrame.nextButton.setActionCommand(CREATE_BUTTON_CLICKED);
		textSourcesFrame.nextButton.addActionListener(this);
		textSourcesFrame.nextButton.setEnabled(false);
		textSourcesFrame.cancelButton.addActionListener(this);

		textSourcesInstructionsTextArea = new JTextArea();
		textSourcesInstructionsTextArea.setEditable(false);
		textSourcesInstructionsTextArea.setText(TEXT_SOURCES_INSTRUCTIONS);
		textSourcesInstructionsTextArea.setLineWrap(true);
		textSourcesInstructionsTextArea.setWrapStyleWord(true);

		textSourcesLabel = new JLabel("Selected text sources");

		textSourcesList = new JList();
		textSourcesList.setCellRenderer(new FrameRenderer());
		JScrollPane scrollPane = new JScrollPane(textSourcesList);

		textSourcesAddButton = new JButton("Add");
		textSourcesAddButton.addActionListener(this);
		textSourcesRemoveButton = new JButton("Remove");
		textSourcesRemoveButton.addActionListener(this);

		textSourcesFrame.contentPane.setLayout(new GridBagLayout());
		textSourcesFrame.contentPane.add(textSourcesInstructionsTextArea, new GridBagConstraints(0, 0, 2, 1, 0d, 0d,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
		textSourcesFrame.contentPane.add(textSourcesLabel, new GridBagConstraints(0, 1, 2, 1, 0d, 0d,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
		textSourcesFrame.contentPane.add(scrollPane, new GridBagConstraints(0, 2, 2, 1, 1.0d, 1.0d,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 2, 2));
		textSourcesFrame.contentPane.add(textSourcesAddButton, new GridBagConstraints(0, 3, 1, 1, 0d, 0d,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
		textSourcesFrame.contentPane.add(textSourcesRemoveButton, new GridBagConstraints(1, 3, 1, 1, 0d, 0d,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
	}

	private void chooseFilter() {
		SimpleInstance chosenFilter = (SimpleInstance) DisplayUtilities.pickInstance(filterFrame, CollectionUtilities
				.createCollection(manager.getKnowtatorProjectUtil().getFilterCls()), "Select annotation filter");

		if (chosenFilter == null)
			return;
		Set<SimpleInstance> annotators = new HashSet<SimpleInstance>(FilterUtil.getAnnotators(chosenFilter));
		Set<SimpleInstance> sets = FilterUtil.getSets(chosenFilter);
		if (annotators.size() < 2 && sets.size() < 2) {
			JOptionPane.showMessageDialog(filterFrame,
					"The selected filter must have either two annotators or two annotation sets specified.",
					"Incomplete annotation filter", JOptionPane.ERROR_MESSAGE);
			return;
		}

		filter = chosenFilter;
		filterFrame.nextButton.setEnabled(true);
		filterTextField.setText(filter.getBrowserText());
	}

	private void addTextSources(Collection addedTextSources) {
		if (addedTextSources != null && addedTextSources.size() > 0) {
			for (Object addedTextSource : addedTextSources) {
				textSources.add((SimpleInstance) addedTextSource);
			}
			textSourcesList.setListData(textSources.toArray());
		}
		if (textSources.size() > 0)
			textSourcesFrame.nextButton.setEnabled(true);
	}

	private void chooseTextSources() {
		Collection textSources = DisplayUtilities.pickInstances(textSourcesFrame, manager.getKnowledgeBase(),
				CollectionUtilities.createCollection(manager.getKnowtatorProjectUtil().getTextSourceCls()),
				"select text sources");
		addTextSources(textSources);
	}

	private void removeTextSources() {
		Object[] selectedValues = textSourcesList.getSelectedValues();
		for (Object selectedValue : selectedValues) {
			textSources.remove(selectedValue);
		}
		textSourcesList.setListData(textSources.toArray());
		if (textSources.size() == 0)
			textSourcesFrame.nextButton.setEnabled(false);
	}

	public void actionPerformed(ActionEvent actionEvent) {
		String command = actionEvent.getActionCommand();
		Object source = actionEvent.getSource();
		if (command.equals(TO_WELCOME_FRAME)) {
			showFrame(welcomeFrame);
		} else if (command.equals(TO_NAME_FRAME)) {
			showFrame(nameFrame);
		} else if (command.equals(TO_FILTER_FRAME)) {
			showFrame(filterFrame);
		} else if (command.equals(TO_TEXTSOURCES_FRAME)) {
			showFrame(textSourcesFrame);
		} else if (command.equals("CANCEL")) {
			setVisible(false);
		} else if (source == filterButton) {
			chooseFilter();
		} else if (source == textSourcesAddButton) {
			chooseTextSources();
		} else if (source == textSourcesRemoveButton) {
			removeTextSources();
		} else if (command.equals(CREATE_BUTTON_CLICKED)) {
			createClicked = true;
			setVisible(false);
		}
	}

	public void setVisible(boolean visible) {
		if (visible) {
			createClicked = false;
			showFrame(welcomeFrame);
		} else {
			welcomeFrame.setVisible(false);
			nameFrame.setVisible(false);
			filterFrame.setVisible(false);
			textSourcesFrame.setVisible(false);
		}
	}

	public void showFrame(WizardFrame wizardFrame) {
		if (visibleFrame != null) {
			wizardSize = visibleFrame.getSize();
			wizardLocation = visibleFrame.getLocation();
			wizardFrame.setSize(wizardSize);
			wizardFrame.setLocation(wizardLocation);
		}
		visibleFrame = wizardFrame;
		setVisible(false);
		visibleFrame.setVisible(true);
	}

	public boolean createConsensusSet() {
		return createClicked;
	}

	public SimpleInstance getFilter() {
		return filter;
	}

	public Set<SimpleInstance> getTextSources() {
		return textSources;
	}

	public String getConsensusSetName() {
		return consensusSetName;
	}
}
