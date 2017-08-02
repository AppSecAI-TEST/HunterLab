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
 *   Brant Barney <brant.barney@hsc.utah.edu>
 */

package edu.uchsc.ccp.knowtator;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.languageExplorer.widgets.ScrollableBar;
import edu.stanford.smi.protege.model.*;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.plugin.PluginUtilities;
import edu.stanford.smi.protege.resource.LocalizedText;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.resource.Text;
import edu.stanford.smi.protege.ui.ClsTreeFinder;
import edu.stanford.smi.protege.ui.DisplayUtilities;
import edu.stanford.smi.protege.ui.ParentChildRoot;
import edu.stanford.smi.protege.ui.ProjectManager;
import edu.stanford.smi.protege.util.ApplicationProperties;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.util.URIUtilities;
import edu.stanford.smi.protege.widget.AbstractTabWidget;
import edu.uchsc.ccp.iaa.IAAException;
import edu.uchsc.ccp.knowtator.event.*;
import edu.uchsc.ccp.knowtator.exception.ActionNotFoundException;
import edu.uchsc.ccp.knowtator.stats.KnowtatorIAA;
import edu.uchsc.ccp.knowtator.textsource.TextSourceChangeEvent;
import edu.uchsc.ccp.knowtator.textsource.TextSourceSelector;
import edu.uchsc.ccp.knowtator.ui.*;
import edu.uchsc.ccp.knowtator.ui.action.ActionFactory;
import edu.uchsc.ccp.knowtator.util.*;
import edu.uchsc.ccp.knowtator.wizards.CreateConsensusSetWizard;
import edu.uchsc.ccp.knowtator.wizards.FindAnnotateDialog;
import edu.uchsc.ccp.knowtator.wizards.IAAWizard;
import edu.uchsc.ccp.knowtator.xml.XMLExport;
import edu.uchsc.ccp.knowtator.xml.XMLImport;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.List;

/**
 * Authors: Philip V. Ogren Created: September 2004 Description: This class
 * implements the AbstractTabWidget - the basic Protege plugin. This is the
 * central class for the Annotator plugin. Todo: Changes: 8/11/2005 pvo renderer
 * passed to KB.setClientInformation so that the ComplexSlotMentionValueWidget
 * can use it. 8/15/2005 pvo added method nextFilterInstance for applying the
 * "next" select annotation filter. This method should maybe be moved to
 * ProjectSettings class.
 */
public class Knowtator extends AbstractTabWidget implements java.awt.event.ActionListener, 
														    RefreshAnnotationsDisplayListener,
														    FastAnnotateListener,
														    FilterChangedListener,
														    NotifyTextChangeListener,
														    SlotMentionValueChangedListener,
														    SelectedAnnotationChangeListener {
	public Knowtator() {
	}

	public void filterChanged(FilterChangedEvent event) {
		filterSelectionLabel.setText("Filter: " + event.getNewFilter().getBrowserText());
					
		if (manager.isConsensusMode()) {
			
			if( manager.isRequiredMode() ) {
				//Consensus Mode and Required Mode cannot be on at the same time, if the filter is
				//  changed to consensus mode, 
				quitRequiredMode();
			}
			
			textViewerPanel.add(consensusModePanel, new GridBagConstraints(0, 1, 1, 1, 0, 0,
					GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 4, 4));
			consensusSetItem.setSelected(true);
		} else {
			textViewerPanel.remove(consensusModePanel);
			consensusSetItem.setSelected(false);
		}
		textViewerPanel.validate();
		textViewerPanel.repaint();
		
		if( manager.isRequiredMode() ) {
			refreshRequiredCount();
		}
	}

	private static final long serialVersionUID = 131L;

	/**
	 * This string is used to give a hash key for the AnnotationUtil that will
	 * be passed to the knowledge base as client information that other objects
	 * can access.
	 */

	public static final String KNOWTATOR_MANAGER = "KNOWTATOR_MANAGER";

	private static final String CHANGE_SORT_METHOD = "CHANGE_SORT_METHOD";

	private static final String SELECT_FILTER = "SELECT_FILTER";

	private static final String SELECT_PREVIOUS_ANNOTATION_FILTER = "SELECT_PREVIOUS_ANNOTATION_FILTER";

	private static final String SELECT_NEXT_ANNOTATION_FILTER = "SELECT_NEXT_ANNOTATION_FILTER";

    private JSplitPane annotationSchemaPane;
	private JLabel instancesPaneLabel;
	private AnnotationSchemaInstancesList instancesList;
	private JPanel textViewerPanel;
	private JLabel filterSelectionLabel;
	private JButton prevFilterButton;
	private JButton selectAnnotationFilterButton;
	private JButton nextFilterButton;
	private JPanel fastAnnotatePanel;
	private JPanel consensusModePanel;
	private JPanel requiredModePanel;
	private JButton requiredModeNextButton;
	private JProgressBar requiredModeProgressBar;
	private JButton consensusModeCreateButton;
	private JButton consensusModeNextButton;
	private JProgressBar consensusModeProgressBar;
	private JLabel consensusModeFinishedIconLabel;
    private TextSourceSelector textSourceSelector;
	private KnowtatorTextPane textPane;
	private TextViewer textViewer;
	private JLabel notifyLabel;
    private JMenuItem saveAsMenuItem;
	private JMenuItem newProjectMenuItem;
	private JMenu knowtatorMenu;
	private JMenuItem configureMenuItem;
	private JCheckBoxMenuItem fastAnnotateItem;
	private JMenuItem exportToXMLMenuItem;
	private JMenuItem importFromXMLMenuItem;
	private JMenuItem simpleFileImportMenuItem;
	private JMenuItem annotationRemovalMenuItem;
	private JCheckBoxMenuItem showInstancesMenuItem;
	private JMenuItem mergeAnnotationsItem;
	private JMenuItem batchAnnotatorChangeItem;
	private JMenuItem batchAnnotationSetChangeItem;
	private JMenuItem iaaItem;
	private JCheckBoxMenuItem consensusSetItem;
	private JCheckBoxMenuItem requiredModeMenuItem;
	KnowtatorProjectUtil kpu;
	KnowtatorManager manager;
	AnnotationUtil annotationUtil;
	private TextSourceUtil textSourceUtil;
	private SpanUtil spanUtil;
	private FilterUtil filterUtil;
	MentionUtil mentionUtil;
	private DisplayColors displayColors;
	private FindAnnotateDialog findAnnotateDialog;
    private boolean showInstances = false;
	private Comparator<SimpleInstance> alphabeticalComparator;
	private Comparator<SimpleInstance> spanComparator;
	private MouseListener instancesListMouseListener;
	private static JFrame protegeFrame;
	private Logger logger = Logger.getLogger(Knowtator.class);

	/**
	 * Map containing the toggle buttons that are displayed in the fast annotate
	 * tool bar.
	 */
	private Map<FrameID, FastAnnotateButton> fastAnnotateButtonMap = new HashMap<FrameID, FastAnnotateButton>();

	/**
	 * The button group associated with the toggle buttons on the fast annotate
	 * tool bar. This button group assures that only one is selected at a time.
	 */
	private ButtonGroup fastAnnotateButtonGroup = new ButtonGroup();

	/**
	 * The component that provides the scrolling for the fast annotate tool bar.
	 */
	private ScrollableBar fastAnnotateScrollableBar = null;

    /** SplitPane containing the entire Knowtator UI */
	private JSplitPane mainKnowtatorSplitPane;
		
	/** SplitPane that will consist of the mainKnowtatorSplitPane combined with the
	 *    <code>annotationDetailsPanelConsensusMode</code>. This will be toggled on and off
	 *    with consensus mode. */
	JSplitPane dualAnnotationKnowtatorSplitPane;	
		
	ImageIcon redCrossIcon = ComponentUtilities.loadImageIcon(Knowtator.class,"/edu/uchsc/ccp/knowtator/images/redCross_20.png");
	
	private ImageIcon greenCheckIcon = ComponentUtilities.loadImageIcon(Knowtator.class,"/edu/uchsc/ccp/knowtator/images/greenCheck_20.png");
	
	/** Label containing a green check icon. Used in the required mode panel to indicate when all required
	 *  	slots have values.  */
	private JLabel requiredGreenCheckLabel;

	// startup code
	public void initialize() {
		try {
			logger.debug("Plugins directory located at: " + PluginUtilities.getPluginsDirectory().getPath());

			setUIClassLoader();

			File projectFile = new File(getProject().getProjectURI());

			if (KnowtatorProjectUtil.getKnowtatorSupportClassesVersion(getKnowledgeBase()).equals(
					KnowtatorProjectUtil.UNVERSIONED)) {
				int result = JOptionPane.showConfirmDialog(this, "Adding Knowtator to project.\n"
						+ "Please select 'Yes' or 'No' from the following dialog\n"
						+ "then re-open your project.  To cancel click 'Cancel'.", "Confirm Knowtator",
						JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					if (ProjectManager.getProjectManager().closeProjectRequest()) {

						Project project = ProjectUtil.openProject(projectFile.getPath());
						try {
							KnowtatorProjectUtil.updateKnowtatorProject(project);
							URI uri = project.getProjectURI();
							ProjectManager.getProjectManager().loadProject(uri);
							return;
						} catch (IOException ioe) {
							JOptionPane
									.showMessageDialog(
											this,
											"Knowtator experienced difficulty loading the correct support classes.\n"
													+ "Knowtator thinks that it is installed at: "
													+ PluginUtilities.getPluginsDirectory().getPath()
													+ "\n"
													+ "Please verify that the file edu.uchsc.ccp.knowtator"
													+ File.separator
													+ "knowtator.pprj exists there.\n"
													+ "If so, please report this bug along with the stack trace written to the console.  Thanks.");
							ioe.printStackTrace();
						}
					}
				}
				JOptionPane.showMessageDialog(this, "Knowtator was not added to your project.\n"
						+ "Please de-select Knowtator from the list of Tab Widgets");
				return;
			} else if (!KnowtatorProjectUtil.isKnowtatorSupportClassesCurrent(getKnowledgeBase())) {
				int result = JOptionPane.showConfirmDialog(this, "You are opening an annotation project created\n"
						+ "by an older version of Knowtator.  Opening\n"
						+ "this project with this version of knowtator \n" 
						+" requires updating Knowtator support files.\n"
						+ "If you proceed with the update, then you should\n"
						+ "not open this project in the older version of Knowtator.\n"
						+ "To proceed with the update, please select 'OK' below and\n "
						+ "select 'Yes' or 'No' from the following dialog\n"
						+ "then re-open your project. \n To cancel click 'Cancel'.", "Proceed with Knowtator Update?",
						JOptionPane.OK_CANCEL_OPTION);
				if (result == JOptionPane.OK_OPTION) {
					if (ProjectManager.getProjectManager().closeProjectRequest()) {

						Project project = ProjectUtil.openProject(projectFile.getPath());
						try {
							KnowtatorProjectUtil.updateKnowtatorProject(project);
							JOptionPane.showMessageDialog(this, "Knowtator was updated for this annotation project.  Please close and reopen.");
							return;
						} catch (IOException ioe) {
							JOptionPane
									.showMessageDialog(
											this,
											"Knowtator experienced difficulty loading the correct support classes.\n"
													+ "Knowtator thinks that it is installed at: "
													+ PluginUtilities.getPluginsDirectory().getPath()
													+ "\n"
													+ "Please verify that the file edu.uchsc.ccp.knowtator"
													+ File.separator
													+ "knowtator.pprj exists there.\n"
													+ "If so, please report this bug along with the stack trace written to the console.  Thanks.");
							ioe.printStackTrace();
						}
					}
				}
				JOptionPane.showMessageDialog(this, "Knowtator was not updated.");
				return;

			}

			kpu = new KnowtatorProjectUtil(getKnowledgeBase(), this);

			manager = new KnowtatorManager(kpu);
			getKnowledgeBase().setClientInformation(KNOWTATOR_MANAGER, manager);

			annotationUtil = manager.getAnnotationUtil();
			textSourceUtil = manager.getTextSourceUtil();
			mentionUtil = manager.getMentionUtil();
			filterUtil = manager.getFilterUtil();
			displayColors = manager.getDisplayColors();
			BrowserTextUtil browserTextUtil = manager.getBrowserTextUtil();
			spanUtil = manager.getSpanUtil();					

			initMenu();

			// ((JFrame) SwingUtilities.getRoot(this)).setTitle("Knowtator");
			showInstances = ProjectSettings.getShowInstances(getProject());

			// initialize the tab text
			setLabel("Knowtator");

			// set the icon for the tab
			setIcon(ComponentUtilities.loadImageIcon(Knowtator.class, "/edu/uchsc/ccp/knowtator/images/annotate.gif"));

			// setIcon(ComponentUtilities.loadImageIcon(
			// Knowtator.class,
			// "/edu/uchsc/ccp/knowtator/images/nerd.jpg"));

			initializeFastAnnotatePanel();
			initializeConsensusModePanel();
			initializeRequiredModePanel();

			JPopupMenu popupMenu = new JPopupMenu();
			List<Cls> rootClses = manager.getRootClses();
			AnnotationSchemaTree clsTree = new AnnotationSchemaTree(manager, null, new ParentChildRoot(rootClses));
			ClsTreeFinder clsFinder = new ClsTreeFinder(manager.getKnowledgeBase(), clsTree);
			clsFinder.setMinimumSize( new Dimension( 0, 0 ) );

			instancesList = new AnnotationSchemaInstancesList(manager);

			textPane = new KnowtatorTextPane(manager);
			manager.setTextPane(textPane);
			textViewer = new TextViewer(textPane, manager);
			manager.setTextViewer(textViewer);
			
			/* Panel shown on the right of the Knowtator UI, containing all of the annotation
	     details, allowing the user to view and modify. */
            AnnotationComparisonPanel annotationComparisonPanel = new AnnotationComparisonPanel(kpu, manager, getProject());
			
			textSourceUtil.addTextSourceChangeListener(textViewer);
			//textSourceUtil.addTextSourceChangeListener(annotationComparisonPanel); // I had to
			// change the order of this so that the handling of the text source
			// change would be in
			// the right order.

			textSourceUtil.addTextSourceChangeListener(spanUtil);
			textSourceUtil.addTextSourceChangeListener(manager);

			textSourceSelector = new TextSourceSelector(getProject(), textSourceUtil, getMainWindowToolBar(), this);
			
			alphabeticalComparator = browserTextUtil.comparator();
			spanComparator = spanUtil.comparator(alphabeticalComparator);

			manager.setAnnotationComparator(spanComparator);
			
			JPanel clsTreePanel = new JPanel(new GridBagLayout());
			JLabel clsTreeLabel = new JLabel("annotation schema");
			clsTreeLabel.setMinimumSize( new Dimension( 0, 0 ) );
			
			clsTreePanel.add(clsTreeLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 4, 4));
			clsTreePanel.add(new JScrollPane(clsTree), new GridBagConstraints(0, 1, 1, 1, .5, .5,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			clsTreePanel.add(clsFinder, new GridBagConstraints(0, 2, 1, 1, 1.0, 0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

			JPanel instancesPanel = new JPanel(new GridBagLayout());
			instancesPaneLabel = new JLabel();

			EventHandler.getInstance().addSelectedClsChangeListener(new SelectedClsChangeListener() {
				public void clsSelectionChanged(SelectedClsChangeEvent scce) {
					Cls selectedCls = manager.getSelectedCls();
					if (ProjectSettings.getShowInstances(getProject())) {
						instancesPaneLabel.setText("Instances of '" + selectedCls.getBrowserText() + "'");
					}
				}
			});

			JScrollPane instancesPane = new JScrollPane(instancesList);
			instancesPanel.add(instancesPaneLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.WEST,
					GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 4, 4));
			instancesPanel.add(instancesPane, new GridBagConstraints(0, 1, 1, 1, .5, .5, GridBagConstraints.CENTER,
					GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			filterSelectionLabel = new JLabel("Filter: " + manager.getSelectedFilter().getBrowserText());
			prevFilterButton = new JButton(ComponentUtilities.loadImageIcon(Knowtator.class,
					"/edu/uchsc/ccp/knowtator/images/prev.gif"));
			prevFilterButton.setActionCommand(Knowtator.SELECT_PREVIOUS_ANNOTATION_FILTER);
			prevFilterButton.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			prevFilterButton.setToolTipText("select previous annotation filter");
			prevFilterButton.setBorder(null);
			prevFilterButton.addActionListener(this);

			selectAnnotationFilterButton = new JButton(ComponentUtilities.loadImageIcon(Knowtator.class,
					"/edu/uchsc/ccp/knowtator/images/History24.gif"));
			selectAnnotationFilterButton.setActionCommand(Knowtator.SELECT_FILTER);
			selectAnnotationFilterButton.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			selectAnnotationFilterButton.setToolTipText("Select from the list of active annotation filters");
			selectAnnotationFilterButton.setBorder(null);
			selectAnnotationFilterButton.addActionListener(this);

			nextFilterButton = new JButton(ComponentUtilities.loadImageIcon(Knowtator.class,
					"/edu/uchsc/ccp/knowtator/images/next.gif"));
			nextFilterButton.setActionCommand(Knowtator.SELECT_NEXT_ANNOTATION_FILTER);
			nextFilterButton.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			nextFilterButton.setToolTipText("select next annotation filter");
			nextFilterButton.setBorder(null);
			nextFilterButton.addActionListener(this);

			JButton nextAnnotationButton = new JButton(ComponentUtilities.loadImageIcon(Knowtator.class,
					"/edu/uchsc/ccp/knowtator/images/next.gif"));
			nextAnnotationButton.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			nextAnnotationButton.setToolTipText("select next annotation");
			nextAnnotationButton.setBorder(null);
			nextAnnotationButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					manager.selectNextAnnotation();
				}
			});
			JButton previousAnnotationButton = new JButton(ComponentUtilities.loadImageIcon(Knowtator.class,
					"/edu/uchsc/ccp/knowtator/images/prev.gif"));
			previousAnnotationButton.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			previousAnnotationButton.setToolTipText("select previous annotation");
			previousAnnotationButton.setBorder(null);
			previousAnnotationButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					manager.selectPreviousAnnotation();
				}
			});

			getMainWindowToolBar().addSeparator();
			getMainWindowToolBar().add(filterSelectionLabel);
			getMainWindowToolBar().addSeparator();
			getMainWindowToolBar().add(prevFilterButton);
			getMainWindowToolBar().addSeparator();
			getMainWindowToolBar().add(selectAnnotationFilterButton);
			getMainWindowToolBar().addSeparator();
			getMainWindowToolBar().add(nextFilterButton);
			getMainWindowToolBar().addSeparator();
			getMainWindowToolBar().add(previousAnnotationButton);
			getMainWindowToolBar().addSeparator();
			getMainWindowToolBar().add(nextAnnotationButton);

			// Layout and add components
			annotationSchemaPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, clsTreePanel, instancesPanel);
			annotationSchemaPane.setDividerLocation(400);

			textViewerPanel = new JPanel(new GridBagLayout());
			textViewerPanel.add(textViewer.getContentPane(), new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 4, 4));

			// JSplitPane rightPane1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
			// annotationInstancesPanel,
			// mentionDisplay);
			// rightPane1.setDividerLocation(350);
			
			JButton copyAnnotationButton = new JButton("COPY");
			copyAnnotationButton.setToolTipText("Copy this annotation");
			copyAnnotationButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					manager.duplicateSelectedAnnotation();
				}
			});				

			JSplitPane schemaAndTextViewerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
					                                                 annotationSchemaPane,
					                                                 textViewerPanel);
			schemaAndTextViewerSplitPane.setDividerLocation(200);
			schemaAndTextViewerSplitPane.setOneTouchExpandable(true);					

			mainKnowtatorSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
			                                        schemaAndTextViewerSplitPane,
                    annotationComparisonPanel);
			mainKnowtatorSplitPane.setDividerLocation(800);				
			mainKnowtatorSplitPane.setOneTouchExpandable(true);

			setLayout(new BorderLayout());
			add(mainKnowtatorSplitPane, BorderLayout.CENTER);

			showInstancesPane(showInstances);
			notifyLabel = new JLabel();

			textSourceUtil.init();

			EventHandler.getInstance().addRefreshAnnotationsDisplayListener(this);
			EventHandler.getInstance().addFastAnnotateListener(this);
			EventHandler.getInstance().addFilterChangedListener(this);
			EventHandler.getInstance().addNotifyTextChangeListener(this);
			EventHandler.getInstance().addSlotValueChangedListener(this);
			EventHandler.getInstance().addSelectedAnnotationChangeListener(this);

			try {
				manager.setSelectedFilter(manager.getSelectedFilter());
			} catch (Exception e) {
				e.printStackTrace();
			}

			manager.updateCurrentAnnotations();

		} catch (Throwable t) {
			t.printStackTrace();
		}

	}

	public void notifyTextChanged(String notifyText) {
		if (notifyText == null || notifyText.trim().equals(""))
			remove(notifyLabel);
		else {
			add(notifyLabel, BorderLayout.SOUTH);
			notifyLabel.setText(notifyText);
		}
				
		if( manager.isRequiredMode() ) {
			refreshRequiredCount();
		}
	}

	private void initializeConsensusModePanel() {
		JPanel consensusModePanel = new JPanel(new GridBagLayout());
        JLabel consensusModeLabel = new JLabel("consensus mode");
		Font labelFont = consensusModeLabel.getFont();
		consensusModeLabel.setFont(labelFont.deriveFont((float) (labelFont.getSize() + 8)));

		consensusModeCreateButton = new JButton("create");
		consensusModeCreateButton.setToolTipText("create a new consensus set to work on");
		consensusModeCreateButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				logger.debug("");
				try {
					CreateConsensusSetWizard wizard = new CreateConsensusSetWizard(getProtegeFrame( Knowtator.this ), manager);
					wizard.setVisible(true);
					if (wizard.createConsensusSet()) {
						ConsensusAnnotations.createConsensusAnnotations(manager, wizard.getFilter(), wizard
								.getConsensusSetName(), wizard.getTextSources());
						textSourceUtil.setCurrentTextSource(textSourceUtil.getCurrentTextSource());
					}
				} catch (ConsensusException ce) {
					JOptionPane.showMessageDialog(Knowtator.this, ce.getMessage(), "Consensus Set Error",
							JOptionPane.ERROR_MESSAGE);
					ce.printStackTrace();
				}
			}
		});

        JButton consensusModeSelectButton = new JButton("select");
		consensusModeSelectButton.setToolTipText("select consensus set to work on");
		consensusModeSelectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				List<Cls> clses = new ArrayList<Cls>();
				clses.add(kpu.getConsensusSetCls());
				Instance instance = DisplayUtilities.pickInstance(Knowtator.this, clses, "Select consensus set");
			}
		});
        JButton consensusModeRestartButton = new JButton("restart");
		consensusModeRestartButton.setEnabled(true);
		consensusModeRestartButton.setToolTipText("start over with consensus work on this text source");
		consensusModeRestartButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					int option = JOptionPane.showConfirmDialog(Knowtator.this,
							"To start over with consensus work on this text source, please click 'OK'.\n"
									+ "The annotations in the consensus set for this text source will be deleted.\n"
									+ "The original annotations that were used to create the consensus set will be\n"
									+ "copied into the consensus set again.\n\n" 
									+ "Please note, clicking on OK will delete ALL annotations in the consensus set\n" 
									+ "for this text source. Are you sure you want to continue?",
							"restart consensus work on this text source", JOptionPane.OK_CANCEL_OPTION);

					if (option != JOptionPane.OK_OPTION)
						return;

					manager.restartConsensusMode();
				} catch (ConsensusException ce) {
					JOptionPane.showMessageDialog(Knowtator.this, ce.getMessage(), "Consensus Set Error",
							JOptionPane.ERROR_MESSAGE);
					ce.printStackTrace();
				}
			}
		});

        JButton consensusModeConsolidateAllButton = new JButton("consolidate");
		consensusModeConsolidateAllButton.setEnabled(true);
		consensusModeConsolidateAllButton
				.setToolTipText("automatically consolidate annotations in this document that are identical");
		consensusModeConsolidateAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				manager.consolidateAnnotations();
			}
		});

        JButton consensusModeQuitButton = new JButton("quit");
		consensusModeQuitButton.setToolTipText("quit consensus mode");
		consensusModeQuitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				stopConsensusMode();
			}
		});
		
		try {
			consensusModeNextButton = new JButton( ActionFactory.getInstance( manager ).getAction( ActionFactory.ACTION_SELECT_NEXT_ANNOTATION ) );
			consensusModeNextButton.setText( "next" );
			
		} catch ( ActionNotFoundException e ) {
			e.printStackTrace();
		}

		consensusModeFinishedIconLabel = new JLabel("");
		consensusModeFinishedIconLabel.setVisible( false );
		
		consensusModeProgressBar = new JProgressBar();
		consensusModeProgressBar.setStringPainted( true );
		
		JPanel progressBarPanel = new JPanel();
		progressBarPanel.setLayout( new BoxLayout(progressBarPanel, BoxLayout.X_AXIS));
		progressBarPanel.add( consensusModeProgressBar );
		progressBarPanel.add( Box.createHorizontalGlue() );
		progressBarPanel.add( consensusModeFinishedIconLabel );

		//Row 1
		consensusModePanel.add(consensusModeLabel, new GridBagConstraints(0, 0, 3, 1, 0, 0, 
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 4, 4));
		consensusModePanel.add(progressBarPanel, new GridBagConstraints(3, 0, 3, 1, 0, 0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 4, 4));
		
		//Row 2
		consensusModePanel.add(consensusModeCreateButton, new GridBagConstraints(0, 1, 1, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 4, 4));
		consensusModePanel.add(consensusModeSelectButton, new GridBagConstraints(1, 1, 1, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 4, 4));
		consensusModePanel.add(consensusModeRestartButton, new GridBagConstraints(2, 1, 1, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 4, 4));
		consensusModePanel.add(consensusModeConsolidateAllButton, new GridBagConstraints(3, 1, 1, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 4, 4));		
		consensusModePanel.add(consensusModeNextButton, new GridBagConstraints(4, 1, 1, 1, 0, 0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 4, 4));	
		consensusModePanel.add(consensusModeQuitButton, new GridBagConstraints(5, 1, 1, 1, 0, 0,
				GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 4, 4));		
	}
	
	
	/**
	 * Initializes all components for the required mode panel.
	 */
	private void initializeRequiredModePanel() {
		requiredModePanel = new JPanel();
        JLabel requiredModeLabel = new JLabel("required mode");
		Font labelFont = requiredModeLabel.getFont();
		requiredModeLabel.setFont(labelFont.deriveFont((float) (labelFont.getSize() + 8)));
		
		requiredModeProgressBar = new JProgressBar();
		requiredModeProgressBar.setPreferredSize( new Dimension(100, 18 ) );
		requiredModeProgressBar.setStringPainted( true );

		try {
			requiredModeNextButton = new JButton( ActionFactory.getInstance( manager ).getAction( ActionFactory.ACTION_REQUIRED_MODE_NEXT_ACTION ) );
			requiredModeNextButton.setText( "next" );
		} catch( ActionNotFoundException e ) {
			e.printStackTrace();
		}

        JButton requiredModeQuitButton = new JButton(new RequiredModeQuitAction());
		requiredGreenCheckLabel = new JLabel();
					
		requiredModePanel.setLayout( new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				new ColumnSpec( "default" ),
				FormFactory.RELATED_GAP_COLSPEC,
				new ColumnSpec( "default:grow" ),				
				FormFactory.RELATED_GAP_COLSPEC,
				new ColumnSpec( "default" ),
				FormFactory.RELATED_GAP_COLSPEC,
				new ColumnSpec( "32dlu" ),
				FormFactory.RELATED_GAP_COLSPEC,
				new ColumnSpec( "32dlu" ),
				FormFactory.RELATED_GAP_COLSPEC},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				new RowSpec( "20dlu" ),
				FormFactory.RELATED_GAP_ROWSPEC}));			
			
		requiredModePanel.add( requiredModeLabel, "2, 2" );
		requiredModePanel.add( requiredModeProgressBar, "4, 2" );
		requiredModePanel.add( requiredGreenCheckLabel, "6, 2" );
		requiredModePanel.add( requiredModeNextButton, "8, 2" );
		requiredModePanel.add( requiredModeQuitButton, "10, 2" );		
	}	 
	
	/**
	 * Implementation of SelectedAnnotationChangeListener used to refresh the required
	 *   mode progress.
	 */
	public void annotationSelectionChanged(SelectedAnnotationChangeEvent sace) {
		if( manager.isRequiredMode() ) {
			refreshRequiredCount();
		}
	}
	
	/**
	 * Implementation of SlotMentionValueChangedListener used to refresh the
	 *   required mode progress.
	 */
	public void slotMentionValueChanged() {
		if( manager.isRequiredMode() ) {
			refreshRequiredCount();
		}
	}
	
	/**
	 * Action used in the quit button on the required mode panel. It is used to
	 *   exit from Required Mode.
	 *   
	 * @author brant
	 */
	@SuppressWarnings("serial")
	class RequiredModeQuitAction extends AbstractAction {
		RequiredModeQuitAction() {
			super( "quit" );
		}
		
		public void actionPerformed( ActionEvent evt ) {
			logger.debug( "Now quitting required mode..." );
			
			quitRequiredMode();
		}
	}
	
	/**
	 * Quits required mode, removing the required mode panel from the main text viewer panel
	 */
	private void quitRequiredMode() {
		requiredModeMenuItem.setSelected( false );
		
		textViewerPanel.remove(requiredModePanel);
		textViewerPanel.validate();
		textViewerPanel.repaint();
	}

	/**
	 * Calculates the percentage of finished required annotations, and sets the progress bar and
	 *   green check label accordingly.
	 */
	private void refreshRequiredCount() {
		
		int requiredAnnotationCount = manager.getRequiredAnnotationCount();
		List<SimpleInstance> annotationList = manager.getCurrentFilteredAnnotations();
		int totalAnnotations = 0;
		if( annotationList != null ) {
			totalAnnotations = annotationList.size();
		}
		 
		int requiredPercentage = (int)(((double)requiredAnnotationCount / (double)totalAnnotations) * 100);
		 		
		requiredModeProgressBar.setString( requiredPercentage + "%  (" + requiredAnnotationCount + "/" + totalAnnotations + ")" );
		requiredModeProgressBar.setValue( requiredPercentage );
		
		if( requiredPercentage == 100 ) {
			requiredGreenCheckLabel.setIcon( greenCheckIcon );
		} else {
			requiredGreenCheckLabel.setIcon( null );
		}
	}
	
	// use class selection mode used by find/annotate
	private void initializeFastAnnotatePanel() {
		fastAnnotatePanel = new JPanel(new GridBagLayout());
        JLabel fastAnnotateLabel = new JLabel("fast annotate ");
        JButton fastAnnotateSelectButton = new JButton("select class");
		fastAnnotateSelectButton.setToolTipText("click to change fast annotate class");
        JButton fastAnnotateQuitButton = new JButton("quit");
		fastAnnotateQuitButton.setToolTipText("click to quit fast annotate mode");

		Font labelFont = fastAnnotateLabel.getFont();
		fastAnnotateLabel.setFont(labelFont.deriveFont((float) (labelFont.getSize() + 8)));
		fastAnnotateSelectButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				List<Cls> fastAnnotateClasses = manager.getFastAnnotateClses();
				Instance instance = DisplayUtilities.pickCls(Knowtator.this, getKnowledgeBase(), fastAnnotateClasses,
						"Select fast annotation class");
				if (instance != null) {
					Cls fastAnnotateCls = (Cls) instance;
					manager.setFastAnnotateFrame(fastAnnotateCls);
				}
			}
		});
		fastAnnotateQuitButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				manager.quitFastAnnotate();
			}
		});

		fastAnnotatePanel.setLayout(new BoxLayout(fastAnnotatePanel, BoxLayout.X_AXIS));
		fastAnnotatePanel.add(Box.createRigidArea(new Dimension(5, 30)));
		fastAnnotatePanel.add(fastAnnotateLabel);
		fastAnnotatePanel.add(Box.createHorizontalStrut(10));
		fastAnnotatePanel.add(fastAnnotateQuitButton);
		fastAnnotatePanel.add(Box.createHorizontalStrut(5));

		fastAnnotateScrollableBar = new ScrollableBar(fastAnnotatePanel);
		List<Cls> fastAnnotateClses = manager.getFastAnnotateClses();
		Collections.reverse( fastAnnotateClses );
		for (Cls fastAnnotateCls : fastAnnotateClses) {
			// Initialize the toolbar with the fast annotate classes
			fastAnnotateAddCls(fastAnnotateCls);
		}
	}

	
	public void fastAnnotateRefreshToolbar() {
		Collection<FastAnnotateButton> buttonColl = new ArrayList<FastAnnotateButton>( fastAnnotateButtonMap.values() );
		
		for( FastAnnotateButton button : buttonColl ) {
			fastAnnotateRemoveCls( button.getFrame(), false );
		}
		
		List<Cls> fastAnnotateClses = manager.getFastAnnotateClses();
		Collections.reverse( fastAnnotateClses );
		for (Cls fastAnnotateCls : fastAnnotateClses) {
			// Initialize the toolbar with the fast annotate classes
			fastAnnotateAddCls(fastAnnotateCls);
		}		
	}

	public void fastAnnotateStart() {
		add(fastAnnotateScrollableBar, BorderLayout.NORTH);
		validate();
		repaint();
		fastAnnotateItem.setSelected(true);

		// If no buttons exist on the toolbar, allow the user to select a class,
		// add
		// the button for the class, and select it
		Frame fastAnnotateFrame = manager.getFastAnnotateFrame();
		if (fastAnnotateFrame == null || fastAnnotateButtonMap.isEmpty()) {
			List<Cls> rootClses = manager.getRootClses();
			Instance instance = DisplayUtilities.pickCls(Knowtator.this, getKnowledgeBase(), rootClses,
					"Select fast annotation class");
			if (instance != null) {
				Cls fastAnnotateCls = (Cls) instance;
				manager.setFastAnnotateFrame(fastAnnotateCls);
			} else {
				// User canceled dialog
				manager.quitFastAnnotate();
				fastAnnotateItem.setSelected(false);
			}
		}

		if (!isFastAnnotateButtonSelected()) {
			selectFirstFastAnnotateButton();
		}
	}

	public void fastAnnotateQuit() {
		remove(fastAnnotateScrollableBar);
		fastAnnotateItem.setSelected(false);
		validate();
		repaint();
	}

	public void fastAnnotateClsChange() {
		Frame fastAnnotateFrame = manager.getFastAnnotateFrame();
		if (fastAnnotateFrame != null) {
			JToggleButton toggleButton = fastAnnotateButtonMap.get(fastAnnotateFrame.getFrameID());
			if (toggleButton != null) {
				toggleButton.setSelected(true);
			}
		}
	}

	public void fastAnnotateAddCls(final Frame frame) {
		if (!fastAnnotateButtonMap.containsKey(frame.getFrameID())) {
			final FastAnnotateButton newButton = new FastAnnotateButton(frame);
			newButton.setIcon(displayColors.getIcon(displayColors.getColor(frame)));
			newButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					manager.setFastAnnotateFrame(frame);
				}
			});

			newButton.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent evt) {
					invokeRemoveMenu(evt);
				}

				public void mouseReleased(MouseEvent evt) {
					invokeRemoveMenu(evt);
				}

				private void invokeRemoveMenu(MouseEvent evt) {
					if (evt.isPopupTrigger()) {
						AbstractAction removeFastAnnotateButtonAction = new AbstractAction("Remove") {							
							public void actionPerformed(ActionEvent evt) {
								fastAnnotateRemoveCls(frame);
								
								manager.removeFastAnnotateCls( frame );															
							}
						};
						JPopupMenu fastAnnotatePopupMenu = new JPopupMenu();
						fastAnnotatePopupMenu.add(removeFastAnnotateButtonAction);
						fastAnnotatePopupMenu.show(newButton, evt.getX(), evt.getY());
					}
				}
			});

			fastAnnotateButtonGroup.add(newButton);
			fastAnnotateButtonMap.put(frame.getFrameID(), newButton);
			fastAnnotatePanel.add(newButton, 3);

			fastAnnotatePanel.add(Box.createHorizontalStrut(3), 4);
			manager.setFastAnnotateFrame(frame);
			fastAnnotatePanel.validate();
			fastAnnotatePanel.repaint();
			fastAnnotateScrollableBar.validate();
			fastAnnotateScrollableBar.repaint();
		}
	}
	
	public void fastAnnotateRemoveCls(Frame frame) {
		fastAnnotateRemoveCls( frame, true );
	}

	private void fastAnnotateRemoveCls(Frame frame, boolean quitIfLastCls) {
		JToggleButton button = fastAnnotateButtonMap.get(frame.getFrameID());
		if (button != null) {
			boolean removedButtonIsSelected = button.isSelected();
			int index = SwingUtils.indexOfComponent(fastAnnotatePanel, button);
			fastAnnotatePanel.remove(button);

			if (index != -1) {
				// Remove the padding (strut) between components
				fastAnnotatePanel.remove(index);
			}					

			fastAnnotatePanel.validate();
			fastAnnotatePanel.repaint();
			fastAnnotateButtonMap.remove(frame.getFrameID());
			if (fastAnnotateButtonMap.isEmpty() && quitIfLastCls) {
				manager.quitFastAnnotate();
			}

			if (removedButtonIsSelected) {
				selectFirstFastAnnotateButton();
			}

			fastAnnotateScrollableBar.validate();
			fastAnnotateScrollableBar.repaint();
		}
	}
	
	@SuppressWarnings("serial")
	class FastAnnotateButton extends JToggleButton {
		
		private Frame frame;
		
		FastAnnotateButton(Frame frame) {
			super( frame.getBrowserText() );
			
			this.frame = frame;			
		}
		
		public Frame getFrame() {
			return frame;
		}

		public void setFrame(Frame frame) {
			this.frame = frame;
		}
	}

	/**
	 * Finds out if one of the fast annotate buttons is selected. This is used
	 * to automatically select a button when starting fast annotate mode.
	 * 
	 * @return True if and only if one of the toggle buttons in the fast
	 *         annotate button map is selected. Returns false if no buttons are
	 *         selected.
	 */
	private boolean isFastAnnotateButtonSelected() {
		for (JToggleButton fastAnnotateButton : fastAnnotateButtonMap.values()) {
			if (fastAnnotateButton.isSelected()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Automatically selects the first button found in the toggle button map.
	 * There is no guaranteed order as to which button will be selected.
	 */
	private void selectFirstFastAnnotateButton() {
		Collection<FastAnnotateButton> fastAnnotateButtons = fastAnnotateButtonMap.values();
		for (JToggleButton togButton : fastAnnotateButtons) {
			togButton.setSelected(true);
			break;
		}
	}

	public void refreshAnnotationsDisplay(boolean scrollToSelection) {				
		if (manager.isConsensusMode()) {
			int progress = manager.getConsensusModeProgress();
			int numberOfAnnotations = manager.getCurrentFilteredAnnotations().size();
			if (progress == numberOfAnnotations) {
				consensusModeFinishedIconLabel.setIcon(greenCheckIcon);
				consensusModeFinishedIconLabel.setVisible( true );
				
				consensusModeProgressBar.setValue( 100 );
				consensusModeProgressBar.setString( 100 + "%  (" + progress + "/" + numberOfAnnotations + ")");
			}
			else {			
				int percentage = (int)( ((double)progress / (double) numberOfAnnotations) * 100);

				consensusModeProgressBar.setValue( percentage );
				consensusModeProgressBar.setString( percentage + "%  (" + progress + "/" + numberOfAnnotations + ")");
				
				consensusModeFinishedIconLabel.setVisible( false );
			}
		}
	}

	private void initFindAnnotateDialog() {
		findAnnotateDialog = new FindAnnotateDialog(getProject(), getProtegeFrame( this ), textPane, manager);
		textSourceUtil.addTextSourceChangeListener(findAnnotateDialog);
		textViewer.getTextPane().addCaretListener(findAnnotateDialog);
		findAnnotateDialog.textSourceChanged(new TextSourceChangeEvent(textSourceUtil.getCurrentTextSource()));
	}

	static JFrame getProtegeFrame(JComponent component) {
		if (protegeFrame == null) {
			Container container = component.getParent();
			if (container == null)
				return null;
			while (container != null && !(container instanceof JFrame))
				container = container.getParent();
			protegeFrame = (JFrame) container;
		}

		return protegeFrame;
	}

	private void showInstancesPane(boolean show) {
		if (show) {
			annotationSchemaPane.setDividerLocation(350);
		} else {
			annotationSchemaPane.setDividerLocation(2000);
		}
		ProjectSettings.setShowInstances(getProject(), show);
		showInstancesMenuItem.setSelected(show);
		showInstances = show;
	}

	private void updateProjectMenuBar() {
		JMenuBar pmb = ProjectManager.getProjectManager().getCurrentProjectMenuBar();
		Component[] components = pmb.getComponents();

		for (Component component : components) {
			if (component instanceof JMenu) {
				JMenu menu = (JMenu) component;
				if (menu.getText().equals(newProjectMenuItem.getText())) {
					return;
				}
			}
		}

		for (Component component : components) {
			if (component instanceof JMenu) {
				JMenu menu = (JMenu) component;
				String localizedText = LocalizedText.getText(ResourceKey.MENUBAR_FILE);
				if (localizedText.equals(menu.getText())) {
					menu.add(newProjectMenuItem, 0);
					components = menu.getMenuComponents();
					int i = 0;
					for (Component component2 : components) {
						if (component2 instanceof JMenuItem) {
							JMenuItem jmi = (JMenuItem) component2;
							localizedText = LocalizedText.getText(ResourceKey.PROJECT_SAVE_AS);
							if (jmi.getText().equals(localizedText)) {
								menu.add(saveAsMenuItem, i);
							}
						}
						i++;
					}
				}
			}
		}
	}

	private void initMenu() {
		JMenuBar menuBar = getMainWindowMenuBar();

		saveAsMenuItem = new JMenuItem("Save Knowtator Project As...");
		saveAsMenuItem.addActionListener(new SaveAsAction());
		saveAsMenuItem.setMnemonic('v');

		newProjectMenuItem = new JMenuItem("New Knowtator Project...");
		newProjectMenuItem.addActionListener(new NewProjectAction());
		newProjectMenuItem.setMnemonic('k');

		updateProjectMenuBar();

		knowtatorMenu = new JMenu("Knowtator");
		knowtatorMenu.setMnemonic('k');
		knowtatorMenu.setFont(UIManager.getFont("MenuBar.font"));
		configureMenuItem = new JMenuItem(new ConfigureAction());
		fastAnnotateItem = new JCheckBoxMenuItem(new FastAnnotateAction());
        JMenuItem findAnnotateMenuItem = new JMenuItem(new FindAnnotateAction());

        JMenuItem sortSelectionMenuItem = new JMenuItem("Change annotation sort order");
		sortSelectionMenuItem.setEnabled(true);
		sortSelectionMenuItem.setActionCommand(CHANGE_SORT_METHOD);
		sortSelectionMenuItem.addActionListener(this);
		sortSelectionMenuItem.setMnemonic('s');

		exportToXMLMenuItem = new JMenuItem(new XMLExportAction());
		importFromXMLMenuItem = new JMenuItem(new XMLImportAction());
		simpleFileImportMenuItem = new JMenuItem(new SimpleImportAction());

		annotationRemovalMenuItem = new JMenuItem("Remove annotations");
		annotationRemovalMenuItem.addActionListener(this);
		annotationRemovalMenuItem.setMnemonic('r');

		showInstancesMenuItem = new JCheckBoxMenuItem(new ShowInstancesAction());

		mergeAnnotationsItem = new JMenuItem("Merge annotations");
		mergeAnnotationsItem.addActionListener(this);
		mergeAnnotationsItem.setMnemonic('m');

		batchAnnotatorChangeItem = new JMenuItem("Reassign annotator value in batch");
		batchAnnotatorChangeItem.addActionListener(this);
		batchAnnotatorChangeItem.setMnemonic('b');

		batchAnnotationSetChangeItem = new JMenuItem("Assign annotation set value in batch");
		batchAnnotationSetChangeItem.addActionListener(this);
		batchAnnotationSetChangeItem.setMnemonic('b');

		iaaItem = new JMenuItem("Calculate IAA");
		iaaItem.addActionListener(this);
		iaaItem.setMnemonic('c');

		consensusSetItem = new JCheckBoxMenuItem(new ConsensusModeAction());			

		knowtatorMenu.add(configureMenuItem);
		knowtatorMenu.add(fastAnnotateItem);
		knowtatorMenu.add(findAnnotateMenuItem);
		knowtatorMenu.add(sortSelectionMenuItem);
		knowtatorMenu.add(exportToXMLMenuItem);
		knowtatorMenu.add(importFromXMLMenuItem);
		knowtatorMenu.add(simpleFileImportMenuItem);
		knowtatorMenu.add(annotationRemovalMenuItem);
		knowtatorMenu.add(showInstancesMenuItem);
		knowtatorMenu.add(mergeAnnotationsItem);
		knowtatorMenu.add(batchAnnotatorChangeItem);
		knowtatorMenu.add(batchAnnotationSetChangeItem);
		knowtatorMenu.add(iaaItem);
		knowtatorMenu.add(consensusSetItem);
		
		try {
			knowtatorMenu.add( ActionFactory.getInstance( manager ).getAction( ActionFactory.ACTION_ACCEPT_ANNOTATION ) );
			knowtatorMenu.add( ActionFactory.getInstance( manager ).getAction( ActionFactory.ACTION_ACCEPT_CONSENSUS_ANNOTATION ) );
			knowtatorMenu.add( ActionFactory.getInstance( manager ).getAction( ActionFactory.ACTION_SELECT_NEXT_ANNOTATION ) );
			knowtatorMenu.add( ActionFactory.getInstance( manager ).getAction( ActionFactory.ACTION_DELETE_ANNOTATION ) );
			knowtatorMenu.add( ActionFactory.getInstance( manager ).getAction( ActionFactory.ACTION_DELETE_CONSENSUS_ANNOTATION ) );			
			knowtatorMenu.add( ActionFactory.getInstance( manager ).getAction( ActionFactory.ACTION_CLEAR_ANNOTATION ) );
			knowtatorMenu.add( ActionFactory.getInstance( manager ).getAction( ActionFactory.ACTION_CLEAR_CONSENSUS_ANNOTATION ) );	
						
			requiredModeMenuItem = new RequiredModeCheckBoxMenuItem( new RequiredModeAction() ); 				
			knowtatorMenu.add( requiredModeMenuItem );
			
			knowtatorMenu.add( ActionFactory.getInstance( manager ).getAction( ActionFactory.ACTION_REQUIRED_MODE_NEXT_ACTION ) );
			
		} catch( ActionNotFoundException e ) {
			e.printStackTrace();
		}		
		
		menuBar.add(knowtatorMenu);
	}
	
	/**
	 * Menu item used to set the required mode if this menu item
	 *   is toggled or not.
	 * 
	 * @author brant	 
	 */
	@SuppressWarnings("serial")
	class RequiredModeCheckBoxMenuItem extends JCheckBoxMenuItem {
		
		@SuppressWarnings("serial")
		RequiredModeCheckBoxMenuItem( AbstractAction action ) {
			super( action );
			
			setModel(new JToggleButton.ToggleButtonModel() {
				public void setSelected(boolean b) {
					super.setSelected( b );
										
					manager.setRequiredMode( isSelected() );
				}
			});			
		}	
	}

	public static boolean isSuitable(Project project, Collection errors) {
		boolean isSuitable = true;
		if (project.getLoadingURI() == null) {
			isSuitable = false;
			errors.add("Knowtator cannot be loaded until project is saved.");
		}
		return isSuitable;
	}

	private void selectFilterInstance() {
		Collection<SimpleInstance> filterInstances = manager.getActiveFilters();
		SimpleInstance selectedFilter = (SimpleInstance) edu.stanford.smi.protege.ui.DisplayUtilities
				.pickInstanceFromCollection(this, filterInstances, 0, "Choose an annotation filter");
		if (selectedFilter != null) {
			try {
				manager.setSelectedFilter(selectedFilter);
			} catch (ConsensusException ce) {
				JOptionPane.showMessageDialog(Knowtator.this, ce.getMessage(), "Consensus Set Error",
						JOptionPane.ERROR_MESSAGE);
				ce.printStackTrace();
			}
		}
	}

	/**
	 * Action that toggles the fast annotate mode.
	 * 
	 * @author brantbarney
	 */
	@SuppressWarnings("serial")
	class FastAnnotateAction extends AbstractAction {
		FastAnnotateAction() {
			super("fast annotate mode");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(MNEMONIC_KEY, KeyEvent.VK_F);
		}

		public void actionPerformed(ActionEvent evt) {
			if (fastAnnotateItem.isSelected()) {
				manager.startFastAnnotate();
			} else {
				manager.quitFastAnnotate();
			}
		}
	}

	/**
	 * Action used to launch the find/annotate dialog used to search the text
	 * documents
	 * 
	 * @author brantbarney
	 */
	@SuppressWarnings("serial")
	class FindAnnotateAction extends AbstractAction {
		FindAnnotateAction() {
			super("Find/Annotate");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(MNEMONIC_KEY, KeyEvent.VK_A);
		}

		public void actionPerformed(ActionEvent evt) {
			if (findAnnotateDialog == null) {
				initFindAnnotateDialog();
			}
			findAnnotateDialog.setVisible(true);
		}
	}

	/**
	 * Action used to launch the Knowtator configuration dialog
	 * 
	 * @author brantbarney
	 */
	@SuppressWarnings("serial")
	class ConfigureAction extends AbstractAction {
		ConfigureAction() {
			super("Configure");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(MNEMONIC_KEY, KeyEvent.VK_C);
		}

		public void actionPerformed(ActionEvent evt) {
			getProject().show(ProjectSettings.getActiveConfiguration(getProject()));
		}
	}

	/**
	 * UI Action used to start the XML export wizard, exporting the current
	 * project to XML format
	 * 
	 * @author brantbarney
	 */
	@SuppressWarnings("serial")
	class XMLExportAction extends AbstractAction {
		XMLExportAction() {
			super("Export annotations to xml");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(MNEMONIC_KEY, KeyEvent.VK_X);
		}

		public void actionPerformed(ActionEvent evt) {
			XMLExport.writeToXML(Knowtator.this, getKnowledgeBase(), kpu, textSourceUtil, annotationUtil, mentionUtil,
					filterUtil, getProject());
		}
	}

	/**
	 * UI Action to start the XML import wizard, to import a project from an XML
	 * file.
	 * 
	 * @author brantbarney
	 */
	@SuppressWarnings("serial")
	class XMLImportAction extends AbstractAction {
		XMLImportAction() {
			super("Import annotations from xml");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(MNEMONIC_KEY, KeyEvent.VK_I);
		}

		public void actionPerformed(ActionEvent evt) {			
			XMLImport.readFromXML( getProtegeFrame( Knowtator.this ), getKnowledgeBase(), kpu, textSourceUtil, annotationUtil, mentionUtil,
					filterUtil, getProject());
		}
	}

	@SuppressWarnings("serial")
	class SimpleImportAction extends AbstractAction {
		SimpleImportAction() {
			super("Import annotations from simple file format.");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(MNEMONIC_KEY, KeyEvent.VK_I);
		}

		public void actionPerformed(ActionEvent e) {
			SimpleFileImport.importFromFile(Knowtator.this, manager, textSourceUtil);
		}
	}

	/**
	 * UI Action that toggles the Show Instances pane
	 * 
	 * @author brantbarney
	 */
	@SuppressWarnings("serial")
	class ShowInstancesAction extends AbstractAction {
		ShowInstancesAction() {
			super("Show instances");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(MNEMONIC_KEY, KeyEvent.VK_I);
		}

		public void actionPerformed(ActionEvent evt) {
			showInstancesPane(showInstancesMenuItem.isSelected());
		}
	}
	
	/**
	 * UI Action that enters and exits consensus mode
	 */
	@SuppressWarnings("serial")
	class ConsensusModeAction extends AbstractAction {
		ConsensusModeAction() {
			super( "Consensus Mode" );
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(MNEMONIC_KEY, KeyEvent.VK_O);
		}

		public void actionPerformed(ActionEvent evt) {
			if (consensusSetItem.isSelected()) {
				startConsensusMode();
			} else {
				stopConsensusMode();
			}
		}
	}
	
	class RequiredModeAction extends AbstractAction implements FilterChangedListener {
		RequiredModeAction() {
			super( "Required Mode" );
			
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(MNEMONIC_KEY, KeyEvent.VK_Q);
			
			EventHandler.getInstance().addFilterChangedListener( this );
		}
		
		public void actionPerformed( ActionEvent evt ) {
			toggleRequiredMode();			
		}

		public void filterChanged(FilterChangedEvent event) {		
			setEnabled( !manager.isConsensusMode() );
		}			
	}
	
	private void toggleRequiredMode() {
		logger.debug( "Toggling RequiredMode" );
		if( !manager.isConsensusMode() && requiredModeMenuItem.isSelected() ) {
			textViewerPanel.add(requiredModePanel, new GridBagConstraints(0, 1, 1, 1, 0, 0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 4, 4));
			
			refreshRequiredCount();
		} else {
			textViewerPanel.remove(requiredModePanel);
		}
				
		textViewerPanel.validate();
		textViewerPanel.repaint();		
	}

	public void actionPerformed(ActionEvent actionEvent) {
		Object source = actionEvent.getSource();
		String command = actionEvent.getActionCommand();

		if (source == annotationRemovalMenuItem) {
			AnnotationCleanup.cleanup(this, getKnowledgeBase(), kpu, textSourceUtil, annotationUtil, mentionUtil,
					filterUtil, getProject());
		} else if (source == mergeAnnotationsItem) {
			MergeAnnotations.mergeAnnotations(this, getProject(), textSourceUtil);
			manager.updateCurrentAnnotations();
		} else if (source == batchAnnotatorChangeItem) {
			AnnotatorUtil.batchAnnotatorChange(this, kpu, annotationUtil, filterUtil);
		} else if (source == batchAnnotationSetChangeItem) {
			AnnotatorUtil.batchAnnotationSetChange(this, kpu, annotationUtil, filterUtil);
		} else if (source == iaaItem) {
			IAAWizard wizard = new IAAWizard(getProject(), getKnowledgeBase(), kpu, getProtegeFrame( this ));
			wizard.setVisible(true);
			if (wizard.isGoClicked()) {
				try {
					File outputDirectory = wizard.getOutputDirectory();
					SimpleInstance filter = wizard.getFilter();
					Set<SimpleInstance> textSources = wizard.getTextSources();
					SimpleInstance slotMatcherConfig = wizard.getSlotMatcherConfig();
					Set<Slot> simpleSlots = null;
					Set<Slot> complexSlots = null;
					if (slotMatcherConfig != null) {
						simpleSlots = KnowtatorIAA.getSimpleSlotsFromMatcherConfig(slotMatcherConfig, kpu);
						complexSlots = KnowtatorIAA.getComplexSlotsFromMatcherConfig(slotMatcherConfig, kpu);
					}
					KnowtatorIAA knowtatorIAA = new KnowtatorIAA(outputDirectory, filter, textSources, getProject(),
							simpleSlots, complexSlots, manager, textSourceUtil.getCurrentTextSourceCollection(),
							annotationUtil, mentionUtil, filterUtil);

					if (wizard.isClassMatcherSelected())
						knowtatorIAA.runClassIAA();
					if (wizard.isSpanMatcherSelected())
						knowtatorIAA.runSpanIAA();
					if (wizard.isClassAndSpanMatcherSelected())
						knowtatorIAA.runClassAndSpanIAA();
					if (wizard.isSubclassMatcherSelected())
						knowtatorIAA.runSubclassIAA();
					if (slotMatcherConfig != null) {
						knowtatorIAA.runFeatureMatcherIAA(slotMatcherConfig);
					}

					JOptionPane.showMessageDialog(this, "IAA results written to " + outputDirectory.getPath());

				} catch (IAAException iaae) {
					JOptionPane.showMessageDialog(this, "An exception occured while calculating IAA: "
							+ iaae.getMessage() + "\nPlease see output window for a stack trace.", "IAA Exception",
							JOptionPane.ERROR_MESSAGE);
					iaae.printStackTrace();
				}
			}
		} else if (command.equals(SELECT_PREVIOUS_ANNOTATION_FILTER)) {
			try {
				filterUtil.selectPreviousFilter(FilterUtil.ANY_FILTER);
			} catch (ConsensusException ce) {
				JOptionPane.showMessageDialog(Knowtator.this, ce.getMessage(), "Consensus Set Error",
						JOptionPane.ERROR_MESSAGE);
				ce.printStackTrace();
			}
		} else if (command.equals(SELECT_NEXT_ANNOTATION_FILTER)) {
			try {
				filterUtil.selectNextFilter(FilterUtil.ANY_FILTER);
			} catch (ConsensusException ce) {
				JOptionPane.showMessageDialog(Knowtator.this, ce.getMessage(), "Consensus Set Error",
						JOptionPane.ERROR_MESSAGE);
				ce.printStackTrace();
			}
		} else if (command.equals(SELECT_FILTER)) {
			selectFilterInstance();
		} else if (command.equals(Knowtator.CHANGE_SORT_METHOD)) {
			String[] values = new String[] { "Sort alphabetically", "Sort by span indices" };
			Object selectionValue = JOptionPane.showInputDialog(this, "Select a sort method.", "Sort method selection",
					JOptionPane.PLAIN_MESSAGE, null, values, values[0]);
			if (selectionValue.equals(values[0])) {
				manager.setAnnotationComparator(alphabeticalComparator);
			} else {
				manager.setAnnotationComparator(spanComparator);
			}
			manager.updateCurrentAnnotations();
		}
	}

	private void startConsensusMode() {
		//addAnnotationDetailsPanelForConsensusMode();
		
		try {
			SimpleInstance nextFilter = filterUtil.getNextFilter(FilterUtil.CONSENSUS_FILTER);
			if (nextFilter != null) {
				manager.setSelectedFilter(nextFilter);
			} else {
				consensusModeCreateButton.doClick();
			}			
		} catch (ConsensusException ce) {
			JOptionPane.showMessageDialog(Knowtator.this, ce.getMessage(), "Consensus Set Error",
					JOptionPane.ERROR_MESSAGE);
			ce.printStackTrace();
		}
	}

	private void stopConsensusMode() {
		//removeAnnotationDetailsPanelForConsensusMode();
		
		try {
			SimpleInstance nextFilter = filterUtil.getNextFilter(FilterUtil.NONCONSENSUS_FILTER);
			if (nextFilter != null)
				manager.setSelectedFilter(nextFilter);
			else {
				manager.setSelectedFilter(kpu.getShowAllFilter());
			}
		} catch (ConsensusException ce) {
			ce.printStackTrace();// this code should be unreachable
		}
	}

	/**
	 * removing the instance from currentInstances was interferring with the
	 * code that changes the type of an instance. The instances directType would
	 * be changed causing the clsListener to trigger directInstanceRemoved on
	 * the class that was the old directType of the instance. This would remove
	 * the instance from the currentInstances list. I tried to add it back in
	 * after the directType was changed but this behaved erratically.
	 * 
	 * Use of currentInstances will have to be aware that the list may contain
	 * deleted instances. In HighlightSpans - I simply remove instances that
	 * cause a NullPointerException from currentInstances.
	 * 
	 * I should probably have a InstanceListener instead.
	 */

	// private ClsListener _clsListener = new ClsAdapter()
	// {
	// public void directInstanceRemoved(ClsEvent event) {
	// // currentInstances.remove(event.getInstance());
	// }
	// };
	public void dispose() {
		try {
			JMenuBar menuBar = getMainWindowMenuBar();
			menuBar.remove(knowtatorMenu);

			configureMenuItem.removeActionListener(this);
			exportToXMLMenuItem.removeActionListener(this);
			importFromXMLMenuItem.removeActionListener(this);
			simpleFileImportMenuItem.removeActionListener(this);
			annotationRemovalMenuItem.removeActionListener(this);
			showInstancesMenuItem.removeActionListener(this);
			mergeAnnotationsItem.removeActionListener(this);
			batchAnnotatorChangeItem.removeActionListener(this);
			iaaItem.removeActionListener(this);

			knowtatorMenu.remove(configureMenuItem);
			knowtatorMenu.remove(exportToXMLMenuItem);
			knowtatorMenu.remove(importFromXMLMenuItem);
			knowtatorMenu.remove(simpleFileImportMenuItem);
			knowtatorMenu.remove(annotationRemovalMenuItem);
			knowtatorMenu.remove(showInstancesMenuItem);
			knowtatorMenu.remove(mergeAnnotationsItem);
			knowtatorMenu.remove(batchAnnotatorChangeItem);
			knowtatorMenu.remove(iaaItem);

			configureMenuItem = null;
			exportToXMLMenuItem = null;
			importFromXMLMenuItem = null;
			simpleFileImportMenuItem = null;
			annotationRemovalMenuItem = null;
			showInstancesMenuItem = null;
			mergeAnnotationsItem = null;
			batchAnnotatorChangeItem = null;
			iaaItem = null;

			getMainWindowToolBar().remove(filterSelectionLabel);
			getMainWindowToolBar().remove(prevFilterButton);
			getMainWindowToolBar().remove(selectAnnotationFilterButton);
			getMainWindowToolBar().remove(nextFilterButton);
			getMainWindowToolBar().remove(
					getMainWindowToolBar().getComponent(getMainWindowToolBar().getComponentCount() - 1));
			getMainWindowToolBar().remove(
					getMainWindowToolBar().getComponent(getMainWindowToolBar().getComponentCount() - 1));
			getMainWindowToolBar().remove(
					getMainWindowToolBar().getComponent(getMainWindowToolBar().getComponentCount() - 1));
			getMainWindowToolBar().remove(
					getMainWindowToolBar().getComponent(getMainWindowToolBar().getComponentCount() - 1));

			instancesList.removeMouseListener(instancesListMouseListener);

			textSourceUtil.removeTextSourceChangeListener(textViewer);
			//textSourceUtil.removeTextSourceChangeListener(annotationDetailsPanel);
			textSourceUtil.removeTextSourceChangeListener(spanUtil);

			EventHandler.getInstance().dispose();
			textSourceSelector.dispose();
			
			ActionFactory.getInstance(manager).dispose();
			
		} catch (Exception exception) // null pointer exceptions can be thrown
		// if menu doesn't exist. This can
		// happen when user configures Protege
		// to use knowtator but then cancel when
		// asked to save and reload.
		{
			exception.printStackTrace();
		}
		super.dispose();
	}

	/**
	 * Protege uses a custom class loader
	 * (edu.stanford.smi.protege.util.DirectoryClassLoader) to load the jar
	 * files in the plug-in directories. When Java loads a UI class, it uses the
	 * default system class loader (AppClassLoader). When attempting to load the
	 * custom UI class ScrollableBarUI, it was not found because the system
	 * class loader doesn't load classes in the protege plug-in directories.
	 * 
	 * This method sets the UI class loader to the Protege custom class loader,
	 * so it can find the necessary UI classes.
	 */
	private void setUIClassLoader() {
		ClassLoader loader = getClass().getClassLoader();
		UIDefaults uiDefaults = UIManager.getDefaults();
		uiDefaults.put("ClassLoader", loader);
	}

	@SuppressWarnings("serial")
	private class SaveAsAction extends AbstractAction {
		SaveAsAction() {
			super("Save Knowtator Project As...");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(MNEMONIC_KEY, KeyEvent.VK_V);
		}

		public void actionPerformed(ActionEvent evt) {
			JFileChooser chooser = new JFileChooser();
			File projectFile = new File(getProject().getProjectDirectoryURI());
			System.out.println(projectFile);
            chooser.setSelectedFile(projectFile);
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			chooser.setDialogTitle("Save Knowtator Project As.... Please provide project name.");
			int returnVal = chooser.showOpenDialog(Knowtator.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				if (!file.getParentFile().exists())
					file.getParentFile().mkdirs();
				try {
					ProjectUtil.saveProjectAs(getProject(), file);
					ApplicationProperties.addProjectToMRUList(getProject().getProjectURI());
					resetProtegeTitle(getProject());
					JOptionPane.showMessageDialog(Knowtator.this,
							"Please reopen your text source collection if it is no longer visible",
							"Reopen Text Source Collection", JOptionPane.INFORMATION_MESSAGE);

				} catch (IOException ioe) {
					JOptionPane.showMessageDialog(Knowtator.this, "Unable to save file" + ioe.getMessage(), "",
							JOptionPane.ERROR_MESSAGE);

				}
			}
		}
	}

	/**
	 * This code was copied and modified from
	 * {@link edu.stanford.smi.protege.ui.ProjectManager#updateUI()}
	 * 
	 * @param project
	 */
	private static void resetProtegeTitle(Project project) {
		URI uri = project.getProjectURI();
		String shortname = URIUtilities.getBaseName(uri);
		String longname = URIUtilities.getDisplayText(uri);
		KnowledgeBaseFactory factory = project.getKnowledgeBaseFactory();
		String backend = "";
		if (factory != null) {
			backend = ", " + factory.getDescription();
		}
		String programName = Text.getProgramNameAndVersion();
		String text = shortname + "  " + programName + "    (" + longname + backend + ")";

		ComponentUtilities.setFrameTitle(ProjectManager.getProjectManager().getMainPanel(), text);

	}

	@SuppressWarnings("serial")
	private class NewProjectAction extends AbstractAction {
		NewProjectAction() {
			super("New Knowtator Project...");
			putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_K, Toolkit.getDefaultToolkit()
					.getMenuShortcutKeyMask()));
			putValue(MNEMONIC_KEY, KeyEvent.VK_K);
		}

		public void actionPerformed(ActionEvent evt) {
			File projectFile = new File(getProject().getProjectDirectoryURI());
			if (ProjectManager.getProjectManager().closeProjectRequest()) {
				JFileChooser chooser = new JFileChooser();
                chooser.setSelectedFile(projectFile);
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setDialogTitle("Create New Knowtator Project.  Please provide project name.");
				int returnVal = chooser.showOpenDialog(Knowtator.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					projectFile = chooser.getSelectedFile();
					try {
						Project project = ProjectUtil.createNewProject(projectFile);
						URI uri = project.getProjectURI();
						ProjectManager.getProjectManager().loadProject(uri);
					} catch (IOException ioe) {
						JOptionPane.showMessageDialog(Knowtator.this, "Unable to save file" + ioe.getMessage(), "",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
	}	

	public static void main(String[] args) {
		edu.stanford.smi.protege.Application.main(args);
	}
}
