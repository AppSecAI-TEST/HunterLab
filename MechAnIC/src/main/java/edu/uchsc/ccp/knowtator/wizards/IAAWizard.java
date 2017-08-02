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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.ui.DisplayUtilities;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.ui.InstanceDisplay;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.uchsc.ccp.knowtator.FilterUtil;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;

public class IAAWizard implements ActionListener, CaretListener {

	public static final String IAA_DIRECTORY = "IAA_DIRECTORY";

	public static final String IAA_FILTER = "IAA_FILTER";

	public static final String IAA_TEXTSOURCES = "IAA_TEXTSOURCES";

	public static final String IAA_CONFIG = "IAA_CONFIG";

	public static final String IAA_CONFIG_DISPLAY = "IAA_CONFIG_DISPLAY";

	public static final String IAA_WELCOME_MESSAGE = "This wizard allows you to configure how inter-annotator agreement (IAA) metrics will be calculated.  "
			+ "All IAA results are written to local html files that are veiwable using a web browser.  Please "
			+ "consult available documentation for details on how IAA is calculated and what the results mean.  "
			+ "Calculating the IAA may take some time depending on how many matchers are used and how many "
			+ "annotations are in your project.  Please be patient!";

	public static final String TO_WELCOME_FRAME = "TO_WELCOME_FRAME";

	public static final String TO_OUTPUT_FRAME = "TO_OUTPUT_FRAME";

	public static final String TO_FILTER_FRAME = "TO_FILTER_FRAME";

	public static final String TO_TEXTSOURCES_FRAME = "TO_TEXTSOURCES_FRAME";

	public static final String TO_CONFIG_FRAME = "TO_CONFIG_FRAME";

	public static final String GO_CLICKED = "GO_CLICKED";

	public static final String ADD_SLOT_MATCHER_CLICKED = "ADD_SLOT_MATCHER_CLICKED";

	public static final String REMOVE_SLOT_MATCHER_CLICKED = "REMOVE_SLOT_MATCHER_CLICKED";

	File outputDirectory;

	SimpleInstance filter;

	Set<SimpleInstance> textSources;

	boolean goClicked = false;

	SimpleInstance slotMatcherConfig;

	Project project;

	KnowledgeBase kb;

	KnowtatorProjectUtil kpu;

	JFrame parent;

	WizardFrame welcomeFrame;

	WizardFrame outputFrame;

	WizardFrame filterFrame;

	WizardFrame textSourcesFrame;

	WizardFrame configFrame;

	WizardFrame visibleFrame; // one of the above frames

	JLabel outputDirectoryLabel;

	JTextField outputDirectoryField;

	JButton outputDirectoryButton;

	JLabel outputDirectoryWarningLabel;

	JLabel filterLabel;

	JButton filterButton;

	JLabel textSourcesLabel;

	JList textSourcesList;

	JButton textSourcesRemoveButton;

	JButton textSourcesAddButton;

	JPanel configPanel;

	JCheckBox classMatcherCheckBox;

	JCheckBox spanMatcherCheckBox;

	JCheckBox classAndSpanMatcherCheckBox;

	JCheckBox subclassMatcherCheckBox;

	JPanel slotMatcherPanel;

	JButton addSlotMatcherButton;

	InstanceDisplay instanceDisplay;

	Dimension wizardSize;

	Point wizardLocation;

	public IAAWizard(Project project, KnowledgeBase kb, KnowtatorProjectUtil kpu, JFrame parent)

	{
		this.project = project;
		this.kb = kb;
		this.kpu = kpu;
		this.parent = parent;

		textSources = new TreeSet<SimpleInstance>();

		createWelcomeFrame();
		createOutputFrame();
		createFilterFrame();
		createTextSourcesFrame();
		createConfigFrame();

		setOutputDirectory(getRecentIAADirectory(), true);
		setIAAFilter(getRecentIAAFilter());
		addTextSources(getRecentTextSources());
	}

	private void createWelcomeFrame() {
		welcomeFrame = new WizardFrame(parent, "Inter-annotator agreement metrics");
		welcomeFrame.setAlwaysOnTop(false);
		welcomeFrame.previousButton.setEnabled(false);
		welcomeFrame.nextButton.setActionCommand(TO_OUTPUT_FRAME);
		welcomeFrame.nextButton.addActionListener(this);
		welcomeFrame.cancelButton.addActionListener(this);
		welcomeFrame.contentPane.setLayout(new BorderLayout());
		welcomeFrame.setLocation(WizardUtil.getCentralDialogLocation(parent, welcomeFrame));

		JTextArea welcomeMessage = new JTextArea();
		welcomeMessage.setEditable(false);
		welcomeMessage.setText(IAA_WELCOME_MESSAGE);
		welcomeMessage.setLineWrap(true);
		welcomeMessage.setWrapStyleWord(true);
		Font font = welcomeMessage.getFont();
		Font newFont = font.deriveFont((float) (font.getSize() + 4));
		welcomeMessage.setFont(newFont);
		welcomeFrame.contentPane.add(welcomeMessage, BorderLayout.CENTER);
	}

	private void createOutputFrame() {
		outputFrame = new WizardFrame(parent, "Select IAA output directory");
		outputFrame.setAlwaysOnTop(false);
		outputFrame.previousButton.setActionCommand(TO_WELCOME_FRAME);
		outputFrame.previousButton.addActionListener(this);
		outputFrame.nextButton.setActionCommand(TO_FILTER_FRAME);
		outputFrame.nextButton.addActionListener(this);
		outputFrame.cancelButton.addActionListener(this);

		outputDirectoryLabel = new JLabel("Output directory");

		outputDirectoryField = new JTextField();
		outputDirectoryField.addCaretListener(this);

		outputDirectoryButton = new JButton(ComponentUtilities.loadImageIcon(IAAWizard.class,
				"/edu/uchsc/ccp/knowtator/images/Open24.gif"));
		outputDirectoryButton.setToolTipText("Browse for output directory using file chooser.");
		outputDirectoryButton.addActionListener(this);

		outputDirectoryWarningLabel = new JLabel();
		outputDirectoryWarningLabel.setForeground(Color.BLUE);

		outputFrame.contentPane.setLayout(new GridBagLayout());
		outputFrame.contentPane.add(outputDirectoryLabel, new GridBagConstraints(0, 0, 1, 1, 0d, 0d,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
		outputFrame.contentPane.add(outputDirectoryField, new GridBagConstraints(1, 0, 1, 1, 1.0d, 0d,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(3, 3, 3, 3), 2, 2));
		outputFrame.contentPane.add(outputDirectoryButton, new GridBagConstraints(2, 0, 1, 1, 0d, 0d,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
		outputFrame.contentPane.add(outputDirectoryWarningLabel, new GridBagConstraints(1, 1, 1, 1, 0d, 0d,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
	}

	private void createFilterFrame() {
		filterFrame = new WizardFrame(parent, "Select annotation filter that specifies which annotations to compare.");
		filterFrame.setAlwaysOnTop(false);
		filterFrame.previousButton.setActionCommand(TO_OUTPUT_FRAME);
		filterFrame.previousButton.addActionListener(this);
		filterFrame.nextButton.setActionCommand(TO_TEXTSOURCES_FRAME);
		filterFrame.nextButton.addActionListener(this);
		filterFrame.cancelButton.addActionListener(this);

		filterLabel = new JLabel();
		filterButton = new JButton("Choose filter");
		filterButton.addActionListener(this);

		filterFrame.contentPane.setLayout(new GridBagLayout());
		filterFrame.contentPane.add(filterLabel, new GridBagConstraints(0, 0, 1, 1, 0d, 0d, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
		filterFrame.contentPane.add(filterButton, new GridBagConstraints(1, 0, 1, 1, 0d, 0d, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
	}

	private void createTextSourcesFrame() {
		textSourcesFrame = new WizardFrame(parent, "Select text sources to perform IAA on.");
		textSourcesFrame.setAlwaysOnTop(false);
		textSourcesFrame.previousButton.setActionCommand(TO_FILTER_FRAME);
		textSourcesFrame.previousButton.addActionListener(this);
		textSourcesFrame.nextButton.setActionCommand(TO_CONFIG_FRAME);
		textSourcesFrame.nextButton.addActionListener(this);
		textSourcesFrame.cancelButton.addActionListener(this);

		textSourcesLabel = new JLabel("Selected text sources");
		textSourcesList = new JList();
		textSourcesList.setCellRenderer(new FrameRenderer());
		JScrollPane scrollPane = new JScrollPane(textSourcesList);

		textSourcesAddButton = new JButton("Add");
		textSourcesAddButton.addActionListener(this);
		textSourcesRemoveButton = new JButton("Remove");
		textSourcesRemoveButton.addActionListener(this);

		textSourcesFrame.contentPane.setLayout(new GridBagLayout());
		textSourcesFrame.contentPane.add(textSourcesLabel, new GridBagConstraints(0, 0, 2, 1, 0d, 0d,
				GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
		textSourcesFrame.contentPane.add(scrollPane, new GridBagConstraints(0, 1, 2, 1, 1.0d, 1.0d,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 2, 2));
		textSourcesFrame.contentPane.add(textSourcesAddButton, new GridBagConstraints(0, 2, 1, 1, 0d, 0d,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
		textSourcesFrame.contentPane.add(textSourcesRemoveButton, new GridBagConstraints(1, 2, 1, 1, 0d, 0d,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
	}

	private void createConfigFrame() {
		configFrame = new WizardFrame(parent, "IAA configuration");
		configFrame.setAlwaysOnTop(false);
		configFrame.previousButton.setActionCommand(TO_TEXTSOURCES_FRAME);
		configFrame.previousButton.addActionListener(this);
		configFrame.nextButton.setText("Go!");
		configFrame.nextButton.setActionCommand(GO_CLICKED);
		configFrame.nextButton.addActionListener(this);

		configFrame.cancelButton.addActionListener(this);

		configPanel = new JPanel(new GridBagLayout());

		classMatcherCheckBox = new JCheckBox("class matcher");
		classMatcherCheckBox.setSelected(true);

		spanMatcherCheckBox = new JCheckBox("span matcher");
		spanMatcherCheckBox.setSelected(true);

		classAndSpanMatcherCheckBox = new JCheckBox("class and span matcher");
		classAndSpanMatcherCheckBox.setSelected(true);

		subclassMatcherCheckBox = new JCheckBox("subclass matcher");
		subclassMatcherCheckBox.setSelected(true);

		slotMatcherPanel = new JPanel(new GridBagLayout());

		addSlotMatcherButton = new JButton("add slot matcher");
		addSlotMatcherButton.setActionCommand(ADD_SLOT_MATCHER_CLICKED);
		addSlotMatcherButton.addActionListener(this);
		slotMatcherPanel.add(addSlotMatcherButton, new GridBagConstraints(0, 0, 1, 1, 0d, 0d,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));

		instanceDisplay = new InstanceDisplay(project);
		if (getRecentDisplayConfig())
			showSlotMatcherConfig();

		configPanel.add(classMatcherCheckBox, new GridBagConstraints(0, 0, 1, 1, 0d, 0d, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
		configPanel.add(spanMatcherCheckBox, new GridBagConstraints(0, 1, 1, 1, 0d, 0d, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
		configPanel.add(classAndSpanMatcherCheckBox, new GridBagConstraints(0, 2, 1, 1, 0d, 0d,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
		configPanel.add(subclassMatcherCheckBox, new GridBagConstraints(0, 3, 1, 1, 0d, 0d, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
		configPanel.add(subclassMatcherCheckBox, new GridBagConstraints(0, 4, 1, 1, 0d, 0d, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(3, 3, 3, 3), 2, 2));
		configPanel.add(slotMatcherPanel, new GridBagConstraints(0, 5, 1, 1, 1.0d, 1.0d, GridBagConstraints.NORTHWEST,
				GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 2, 2));

		JScrollPane configScrollPane = new JScrollPane(configPanel);
		configFrame.contentPane.setLayout(new BorderLayout());
		configFrame.contentPane.add(configScrollPane, BorderLayout.CENTER);

	}

	private void showSlotMatcherConfig() {
		if (slotMatcherConfig == null) {
			slotMatcherConfig = getRecentSlotMatcherConfig();
			if (slotMatcherConfig == null) {
				slotMatcherConfig = kb.createSimpleInstance(new FrameID(null),  CollectionUtilities.createCollection(kb
						.getCls("knowtator slot matcher config")), true);
				setRecentSlotMatcherConfig(slotMatcherConfig);
			}
		}

		instanceDisplay.setInstance(slotMatcherConfig);

		slotMatcherPanel.add(instanceDisplay, new GridBagConstraints(0, 2, 1, 1, 1.0d, 1.0d,
				GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(3, 3, 3, 3), 2, 2));

		addSlotMatcherButton.setActionCommand(REMOVE_SLOT_MATCHER_CLICKED);
		addSlotMatcherButton.setText("remove slot matcher");

		slotMatcherPanel.repaint();

		setRecentDisplayConfig(true);
	}

	private void hideSlotMatcherConfig() {
		slotMatcherPanel.remove(instanceDisplay);

		addSlotMatcherButton.setActionCommand(ADD_SLOT_MATCHER_CLICKED);
		addSlotMatcherButton.setText("add slot matcher");

		slotMatcherPanel.repaint();
		setRecentDisplayConfig(false);

	}

	private SimpleInstance getRecentSlotMatcherConfig() {
		SimpleInstance recentSlotMatcherConfig = (SimpleInstance) project.getClientInformation(IAA_CONFIG);
		return recentSlotMatcherConfig;
	}

	private void setRecentSlotMatcherConfig(SimpleInstance slotMatcherConfig) {
		project.setClientInformation(IAA_CONFIG, slotMatcherConfig);
	}

	private boolean getRecentDisplayConfig() {
		Boolean recentDisplayConfig = (Boolean) project.getClientInformation(IAA_CONFIG_DISPLAY);
		if (recentDisplayConfig != null)
			return recentDisplayConfig.booleanValue();
		else
			return false;
	}

	private void setRecentDisplayConfig(boolean displayed) {
		project.setClientInformation(IAA_CONFIG_DISPLAY, new Boolean(displayed));
	}

	private void setOutputDirectory(File directory, boolean updateOutputDirectoryField) {
		if (directory != null && directory.exists() && directory.isDirectory()) {
			outputDirectory = directory;
			setRecentIAADirectory(outputDirectory);
			outputDirectoryWarningLabel.setText("");
			if (updateOutputDirectoryField) {
				outputDirectoryField.setText(outputDirectory.getPath());
			}
		} else {
			outputDirectoryWarningLabel.setText("Output directory is not valid.");
		}
	}

	private void chooseOutputDirectory() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		File chooserDirectory = null;
		File userEnteredDirectory = new File(outputDirectoryField.getText());
		if (userEnteredDirectory.exists()) {
			if (userEnteredDirectory.isDirectory())
				chooserDirectory = userEnteredDirectory;
			else
				chooserDirectory = userEnteredDirectory.getParentFile();
		} else {
			File recentDirectory = getRecentIAADirectory();
			if (recentDirectory != null && recentDirectory.exists() && recentDirectory.isDirectory()) {
				chooserDirectory = recentDirectory;
			}
		}
		if (chooserDirectory != null) {
			chooser.setCurrentDirectory(chooserDirectory);
		}

		int returnVal = chooser.showOpenDialog(outputFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			setOutputDirectory(chooser.getSelectedFile(), true);
		}
	}

	private File getRecentIAADirectory() {
		String path = (String) project.getClientInformation(IAA_DIRECTORY);
		if (path == null)
			return null;
		File projectDirectory = new File(path);
		if (projectDirectory.exists()) {
			if (projectDirectory.isDirectory()) {
				return projectDirectory;
			} else {
				return projectDirectory.getParentFile();
			}
		}
		return null;
	}

	private void setRecentIAADirectory(File iaaDirectory) {
		project.setClientInformation(IAA_DIRECTORY, iaaDirectory.getPath());
	}

	private void setIAAFilter(SimpleInstance setFilter) {
		if (setFilter != null) {
			Set<SimpleInstance> annotators = new HashSet<SimpleInstance>(FilterUtil.getAnnotators(setFilter));
			Set<SimpleInstance> sets = FilterUtil.getSets(setFilter);
			if (annotators.size() >= 2 || sets.size() >= 2) {
				filter = setFilter;
				filterLabel.setText("Selected filter is '" + filter.getBrowserText() + "'");
				setRecentIAAFilter(filter);
				return;
			}
		}
		filterLabel.setText("No filter selected.");
	}

	private void chooseIAAFilter() {
		SimpleInstance chosenFilter = (SimpleInstance) DisplayUtilities.pickInstance(filterFrame, CollectionUtilities
				.createCollection(kpu.getFilterCls()), "Select annotation filter");

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
		setIAAFilter(chosenFilter);
	}

	private SimpleInstance getRecentIAAFilter() {
		SimpleInstance filter = (SimpleInstance) project.getClientInformation(IAA_FILTER);
		return filter;
	}

	private void setRecentIAAFilter(SimpleInstance filter) {
		project.setClientInformation(IAA_FILTER, filter);
	}

	private Collection<SimpleInstance> getRecentTextSources() {
		Collection<SimpleInstance> recentTextSources = (Collection<SimpleInstance>) project
				.getClientInformation(IAA_TEXTSOURCES);
		return recentTextSources;
	}

	private void setRecentTextSources(Set<SimpleInstance> textSources) {
		project.setClientInformation(IAA_TEXTSOURCES, textSources);
	}

	private void addTextSources(Collection<SimpleInstance> addedTextSources) {
		if (addedTextSources != null && addedTextSources.size() > 0) {
			textSources.addAll(addedTextSources);
			textSourcesList.setListData(textSources.toArray());
			setRecentTextSources(textSources);
		}
	}

	private void chooseTextSources() {
		Collection<SimpleInstance> textSources = (Collection<SimpleInstance>) DisplayUtilities.pickInstances(
				textSourcesFrame, project.getKnowledgeBase(), CollectionUtilities.createCollection(kpu
						.getTextSourceCls()), "Select text sources");
		addTextSources(textSources);
	}

	private void removeTextSources() {
		Object[] selectedValues = textSourcesList.getSelectedValues();
		for (Object selectedValue : selectedValues) {
			textSources.remove(selectedValue);
		}
		textSourcesList.setListData(textSources.toArray());
	}

	public void actionPerformed(ActionEvent actionEvent) {
		String command = actionEvent.getActionCommand();
		Object source = actionEvent.getSource();
		if (command.equals(TO_WELCOME_FRAME)) {
			showFrame(welcomeFrame);
		} else if (command.equals(TO_OUTPUT_FRAME)) {
			showFrame(outputFrame);
		} else if (command.equals(TO_FILTER_FRAME)) {
			showFrame(filterFrame);
		} else if (command.equals(TO_TEXTSOURCES_FRAME)) {
			showFrame(textSourcesFrame);
		} else if (command.equals(TO_CONFIG_FRAME)) {
			showFrame(configFrame);
		}

		else if (command.equals("CANCEL")) {
			setVisible(false);
		} else if (source == outputDirectoryButton) {
			chooseOutputDirectory();
		} else if (source == filterButton) {
			chooseIAAFilter();
		} else if (source == textSourcesAddButton) {
			chooseTextSources();
		} else if (source == textSourcesRemoveButton) {
			removeTextSources();
		} else if (command.equals(GO_CLICKED)) {
			goClicked = true;
			setVisible(false);
		} else if (command.equals(ADD_SLOT_MATCHER_CLICKED)) {
			showSlotMatcherConfig();
		} else if (command.equals(REMOVE_SLOT_MATCHER_CLICKED)) {
			hideSlotMatcherConfig();
		}
	}

	public void setVisible(boolean visible) {
		if (visible) {
			goClicked = false;
			showFrame(welcomeFrame);
		} else {
			welcomeFrame.setVisible(false);
			outputFrame.setVisible(false);
			filterFrame.setVisible(false);
			textSourcesFrame.setVisible(false);
			configFrame.setVisible(false);
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

	public void caretUpdate(CaretEvent caretEvent) {
		Object source = caretEvent.getSource();
		if (source == outputDirectoryField) {
			String directoryString = outputDirectoryField.getText();
			File outputFile = new File(directoryString);
			setOutputDirectory(outputFile, false);
		}
	}

	public boolean isGoClicked() {
		return goClicked;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public SimpleInstance getFilter() {
		return filter;
	}

	public Set<SimpleInstance> getTextSources() {
		return textSources;
	}

	public boolean isClassMatcherSelected() {
		return classMatcherCheckBox.isSelected();
	}

	public boolean isSpanMatcherSelected() {
		return spanMatcherCheckBox.isSelected();
	}

	public boolean isClassAndSpanMatcherSelected() {
		return classAndSpanMatcherCheckBox.isSelected();
	}

	public boolean isSubclassMatcherSelected() {
		return subclassMatcherCheckBox.isSelected();
	}

	public SimpleInstance getSlotMatcherConfig() {
		return slotMatcherConfig;
	}
}

// private static List<Slot> askForSlots(SimpleInstance filter, Project project,
// Component parent)
// {
// List<Slot> slots = new ArrayList<Slot>();
// Set<Cls> clses = new HashSet<Cls>();
// Set<Cls> filterTypes = FilterUtil.getTypes(filter);
// if(filterTypes.size() > 0)
// {
// clses.addAll(filterTypes);
// for(Cls filterType : filterTypes)
// {
// clses.addAll(filterType.getSubclasses());
// }
// }
// else
// {
// Cls rootCls = ProjectSettings.getRootCls(project);
// if(rootCls != null)
// {
// clses.add(rootCls);
// clses.addAll(rootCls.getSubclasses());
// }
// }
// for(Cls cls : clses)
// {
// Collection<Slot> clsSlots = (Collection<Slot>) cls.getTemplateSlots();
// for(Slot clsSlot : clsSlots)
// {
// ValueType slotType = clsSlot.getValueType();
// if(slotType == ValueType.BOOLEAN ||
// slotType == ValueType.FLOAT ||
// slotType == ValueType.INTEGER ||
// slotType == ValueType.STRING ||
// slotType == ValueType.SYMBOL)
// {
// slots.add(clsSlot);
// }
// }
// }
// if(slots.size() > 0)
// {
// Collection<Slot> selectedSlots = (Collection<Slot>)
// DisplayUtilities.pickSlots(parent, slots,
// "Select slots that you want matched.");
// slots.clear();
// slots.addAll(selectedSlots);
// }
// return slots;
// }

