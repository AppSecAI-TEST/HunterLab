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
 * Copyright (C) 2005 - 2009.  All Rights Reserved.
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
 *   Brant Barney <brant.barney@hsc.utah.edu>
 */

package edu.uchsc.ccp.knowtator.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.stanford.smi.protege.widget.FormWidget;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.event.FilterChangedEvent;
import edu.uchsc.ccp.knowtator.event.FilterChangedListener;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeEvent;
import edu.uchsc.ccp.knowtator.event.SelectedAnnotationChangeListener;
import edu.uchsc.ccp.knowtator.event.SelectedConsensusAnnotationChangeListener;
import edu.uchsc.ccp.knowtator.exception.ActionNotFoundException;
import edu.uchsc.ccp.knowtator.ui.action.ActionFactory;

/**
 * Panel used to display the annotation details such as the SpanEditPanel,
 * the class used to create the annotation, and the associated slots. It 
 * also contains a delete button to delete the annotation.
 * 
 * This was originally called the <code>upperRightPanel</code> located
 * in <code>Knowtator.java</code>. The code was abstracted out into this
 * new class on May 12, 2009.
 * 
 * @author Brant Barney 
 */
public class SlotAndSpanEditPanel extends JPanel 
						 	      implements SelectedAnnotationChangeListener,
						 	                 SelectedConsensusAnnotationChangeListener,
						 	                 FilterChangedListener {	
	
	private Logger logger = Logger.getLogger(SlotAndSpanEditPanel.class);

	/** Auto generated UID */
	private static final long serialVersionUID = 4141441804590207424L;
	
	/** Button used to delete the annotation from the project */
	private JButton deleteAnnotationButton;
	
	/** Button used to select the consensus set annotation during consensus mode. */
	protected JButton acceptAnnotationButton;

	/** Button used to select the consensus set annotation during consensus mode. */
	protected JButton clearAnnotationButton;

	/** Combo box showing the currently selected annotation, and allowing the user
	 *    to select an annotation. */		
	protected JComboBox selectedAnnotationComboBox;
	
	/** Panel allowing the editing of the annotation span. It contains
	 *    the buttons allowing easy modification of the span. */
	private SpanEditPanel spanEditPanel;
	
	/** Panel allowing the editing of annotation slot values. */
	private MentionDisplay mentionDisplay;
	
	/** Instance of the protege project */
	protected Project project;
	
	/** Needed here as the SpanEditPanel is dependent on this. */
	private AnnotationInstanceDisplay annotationDisplay;	
	
	/** The KnowtatorManager instance. Originally created in the <code>Knowtator.java</code> component*/
	protected KnowtatorManager manager;
	
	/** The KnowtatorProjectUtil instance. Retrieved from the knowator manager. */
	private KnowtatorProjectUtil kpu;
	
	/** Label that will display either the green check or red cross icon. This is an indicator
	 *   used in consensus mode of whether the annotation has been accepted or not. */
	protected JLabel consensusStatusIconLabel;
	
	/** An icon image of a green check box. Used to indicate an accepted annotation in consensus mode. */
	protected ImageIcon greenCheckIcon;
	
	/** An icon image of a red cross. Used to indicate an annotation that has not yet been accepted (in consensus mode). */
	protected ImageIcon redCrossIcon;

	/**
	 * Creates a new instance of <code>AnnotationDetailsPanel</code>
	 * 
	 * @param manager The instance of KnowtatorManager
	 * @param annotationDisplay Needed to create the <code>SpanEditPanel</code>
	 * @param project The Protege project
	 */
	public SlotAndSpanEditPanel( KnowtatorManager manager, 
			                     AnnotationInstanceDisplay annotationDisplay,			                    
			                     Project project) {
		
		super( new GridBagLayout() );
		
		this.manager = manager;
		this.annotationDisplay = annotationDisplay;
		this.project = project;
		this.kpu = manager.getKnowtatorProjectUtil();
		
		initialize();
	}	
	
	/**
	 * Initializes all UI components, and registers the necessary listeners
	 */
	private void initialize() {
		
		mentionDisplay = createMentionDisplay();
		acceptAnnotationButton = new JButton( createAcceptAction() );
		acceptAnnotationButton.setText( "accept" );
		
		clearAnnotationButton = new JButton(createClearAnnotationAction());
		clearAnnotationButton.setText("clear");
		
		selectedAnnotationComboBox = createComboBox();
		
		deleteAnnotationButton = new JButton( createDeleteAction() );
		deleteAnnotationButton.setText( "delete" );
				
		greenCheckIcon = ComponentUtilities.loadImageIcon(SpanEditPanel.class,"/edu/uchsc/ccp/knowtator/images/greenCheck_20.png");
		redCrossIcon = ComponentUtilities.loadImageIcon(SpanEditPanel.class,"/edu/uchsc/ccp/knowtator/images/redCross_20.png");

		consensusStatusIconLabel = new JLabel();		
						
		spanEditPanel = new SpanEditPanel(manager, annotationDisplay);
				
    	add(selectedAnnotationComboBox, new GridBagConstraints(0, 1, 5, 1, 1.0, 0,
    			GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 2, 2, 2), 4, 4));
    	add(spanEditPanel, new GridBagConstraints(0, 2, 1, 1, 0, 0, GridBagConstraints.WEST,
    			GridBagConstraints.WEST, new Insets(2, 2, 2, 2), 4, 4));
    	add(acceptAnnotationButton, new GridBagConstraints(1, 2, 1, 1, 0, 0,
    			GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 4, 4));
    	add(clearAnnotationButton, new GridBagConstraints(2, 2, 1, 1, 0, 0,
    			GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 4, 4));
    	add(deleteAnnotationButton, new GridBagConstraints(3, 2, 1, 1, 0, 0,
    			GridBagConstraints.LINE_START, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 4, 4));    	
    	add(consensusStatusIconLabel, new GridBagConstraints(4, 2, 1, 1, 0, 0,
    			GridBagConstraints.LINE_END, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 4, 4));    	
    	add(mentionDisplay, new GridBagConstraints(0, 3, 5, 1, 1.0, 1.0, GridBagConstraints.WEST,
    			GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 4, 4));    	 
    	
    	EventHandler evtHandler = EventHandler.getInstance();
    	evtHandler.addSelectedAnnotationChangeListener(this);
    	evtHandler.addSelectedConsensusAnnotationChangeListener(this);
    	evtHandler.addFilterChangedListener(this);    	
	}
	
	/**
	 * Creates the combo box to be used in this panel to select the annotation.
	 * 
	 * Designed to be overridden in child class to provide a specific combo box for consensus mode.
	 * 
	 * @return An instance of <code>SelectedAnnotationComboBox</code> for use
	 *          with the left (non-consensus mode) annotation details panel.
	 */
	protected JComboBox createComboBox() {
		return new SelectedAnnotationComboBox( manager );
	}
	
	/**
	 * Creates the instance of MentionDisplay that will be shown in this panel.
	 * 
	 * Designed to be overridden in a child class to return a specific <code>MentionDisplay</code>,
	 *   such as the <code>ConsensusMentionDisplay</code>
	 * 
	 * @return The mention display showing the slots for the selected annotation. 
	 */
	protected MentionDisplay createMentionDisplay() {
		return new MentionDisplay(manager, project);
	}
	
	/**
	 * Creates the accept action that will be used with the AcceptButton.
	 * 
	 * Designed to be overridden in a child class for consensus mode customization. 
	 * 
	 * @return The action used to accept the selected annotation as the consensus annotation.
	 */
	protected Action createAcceptAction() {
		try {
			return ActionFactory.getInstance( manager ).getAction( ActionFactory.ACTION_ACCEPT_ANNOTATION );
		} catch (ActionNotFoundException e) {
			// Should probably handle this exception better
			e.printStackTrace();
			return null;
		}
	}	

	protected Action createClearAnnotationAction() {
		try {
			return ActionFactory.getInstance( manager ).getAction( ActionFactory.ACTION_CLEAR_ANNOTATION );
		} catch (ActionNotFoundException e) {
			// Should probably handle this exception better
			e.printStackTrace();
			return null;
		}
	}	

	protected Action createDeleteAction() {
		//return new DeleteAction();
		try {
			return ActionFactory.getInstance(manager).getAction( ActionFactory.ACTION_DELETE_ANNOTATION );
		} catch( ActionNotFoundException e ) {
			e.printStackTrace();
		}
		
		return null;
	}	
		
	/**
	 * Used to enable the accept button if both the selected annotation and selected consensus annotations
	 *   are not null, and they are not equal (it is possible to have the same annotation in both panes).
	 */
	public void annotationSelectionChanged(SelectedAnnotationChangeEvent sace) {
		SimpleInstance selectedAnnotation = manager.getSelectedAnnotation();			
		
		try {
			Action acceptAnnotationAction = ActionFactory.getInstance( manager ).getAction( ActionFactory.ACTION_ACCEPT_ANNOTATION );
			acceptAnnotationAction.setEnabled( manager.isConsensusMode() && (selectedAnnotation != null) );
		} catch ( ActionNotFoundException e ) {
			e.printStackTrace();
		}
		
		showConsensusIcon(selectedAnnotation);
		
		if( manager.isRequiredMode() ) {
			if( manager.hasUnsetRequiredSlots( selectedAnnotation ) ) {
				highlightMentionDisplay();
			} else {
				resetMentionDisplayHighlight();
			}
		}
	}
	
	protected void showConsensusIcon( SimpleInstance selectedAnnotation ) {
		if( manager.isConsensusMode() ) {
			if( manager.isTeamAnnotation( selectedAnnotation ) ) {
				consensusStatusIconLabel.setIcon(greenCheckIcon);
				consensusStatusIconLabel.setToolTipText( "TextAnnotation has been accepted as the consensus team annotation" );
			} else {
				if( selectedAnnotation != null ) {
					consensusStatusIconLabel.setIcon(redCrossIcon);
					consensusStatusIconLabel.setToolTipText( "TextAnnotation has NOT yet been accepted as the consensus team annotation" );
				} else {
					consensusStatusIconLabel.setIcon( null );
					consensusStatusIconLabel.setToolTipText( null );
				}				
			}			
		} else {
			consensusStatusIconLabel.setIcon( null );
			consensusStatusIconLabel.setToolTipText( null );
		}
	}
	
	public void consensusAnnotationSelectionChanged(SelectedAnnotationChangeEvent sace) {
		annotationSelectionChanged(sace);
	}

	/**
	 * Used to toggle the accept button visibility, depending on if consensus mode is enabled
	 */
	public void filterChanged(FilterChangedEvent event) {
		
		try {
			Action acceptAnnotationAction = ActionFactory.getInstance( manager ).getAction( ActionFactory.ACTION_ACCEPT_ANNOTATION );
						
			acceptAnnotationButton.setVisible( event.isConsensusMode() );
			acceptAnnotationAction.setEnabled( event.isConsensusMode() );
			
		} catch ( ActionNotFoundException e ) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Highlights the entire slot mention display with a red border.
	 * 
	 * TODO: Figure out how to highlight individual slots, instead of the entire
	 *        slot mention display.
	 */
	public void highlightMentionDisplay() {
		FormWidget clsWidget = (FormWidget)mentionDisplay.getFirstClsWidget();
		if( clsWidget != null ) {
			clsWidget.highlightSlot( kpu.getSlotMentionSlot(), Color.red );
		}
	}
	
	/**
	 * Resets the highlight of the slot mention display, so there is no border.
	 */
	public void resetMentionDisplayHighlight() {
		FormWidget clsWidget = (FormWidget)mentionDisplay.getFirstClsWidget();
		if( clsWidget != null ) {
			clsWidget.highlightSlot( kpu.getSlotMentionSlot(), null );
		}
	}
	
	/**
	 * Highlights the mention class with a red border. This is the area
	 *  titled "annotated class".
	 */
	public void highlightMentionCls() {
		FormWidget clsWidget = (FormWidget)mentionDisplay.getFirstClsWidget();
		if( clsWidget != null ) {
			clsWidget.highlightSlot( kpu.getMentionClassSlot(), Color.red );
		}
	}
	
	/**
	 * Resets the highlight of the mention class display, so there is no border.
	 * This is the area titled "annotated class".
	 */
	public void resetMentionClsHighlight() {
		FormWidget clsWidget = (FormWidget)mentionDisplay.getFirstClsWidget();
		if( clsWidget != null ) {
			clsWidget.highlightSlot( kpu.getMentionClassSlot(), null );
		}
	}
	
	protected void disableAllComponents() {
		selectedAnnotationComboBox.setEnabled( false );
		spanEditPanel.enableButtons( false );		
	}
	
	protected void enableAllComponents() {
		selectedAnnotationComboBox.setEnabled( true );
		spanEditPanel.enableButtons( true );
	}
}
