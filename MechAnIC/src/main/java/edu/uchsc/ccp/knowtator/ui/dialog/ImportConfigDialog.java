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
 * Copyright (C) 2005 - 2010.  All Rights Reserved.
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
 *   Brant Barney (Original Author)
 */

package edu.uchsc.ccp.knowtator.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Initial dialog shown when importing annotations from the Knowtator XML format.
 * 
 * In January of 2010, two new options were added to automatically generate the classes and
 *   slots that currently don't exist in the Knowtator project.
 * 
 * @author brant 
 */
@SuppressWarnings("serial")
public class ImportConfigDialog extends javax.swing.JDialog {
	
	String instructionText = "The following dialogs allow you to import annotations from XML. "
                            	+ "Please consult available documentation before using this feature. "
                            	+ "Before importing annotations please save and/or archive your current "
                            	+ "Knowtator project.  If an exception is thrown during annotation import, "
                            	+ "then importing will stop and an error message will appear stating the problem. "
                            	+ "Otherwise, a message stating successful completion will appear.  If annotation "
                            	+ "import does not successfully complete, please discard all changes made by "
                            	+ "the partial import by closing your Knowtator project without saving changes "
                            	+ "and reopening.";

	/** Return value when the OK button is clicked */
	public static final int OK_OPTION = 1;
	
	/** Return value when the Cancel button is clicked */
	public static final int CANCEL_OPTION = 2;
	
	/** Stores the value of which button was clicked to close the dialog. */
	private int closeOption = -1;
		
	private JPanel contentPanel = new JPanel();
	private JButton okButton;
	private JButton cancelButton;	
	
	/** Check box containing the generate classes value */
	private JCheckBox clsCheckBox;
	
	/** Check box containing the generate slots value*/
	private JCheckBox slotCheckBox;
	
	/**
	 * Creates a new <code>ImportConfigDialog</code> 
	 * 
	 * @param frame
	 */
	public ImportConfigDialog(Frame frame) {
		super(frame, true);
		
		initComponents();
		setLocationRelativeTo( frame );
	}
	
	/**
	 * Initializes the UI components of the dialog
	 */
	private void initComponents() {	
		setTitle( "XML Import" );
		
		setBounds(100, 100, 550, 260);
		setResizable( false );
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		
		contentPanel.setLayout( new FormLayout( new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				new ColumnSpec( "default:grow" ),
				FormFactory.RELATED_GAP_COLSPEC},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				new RowSpec( "default:grow" ),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,				
				FormFactory.RELATED_GAP_ROWSPEC}));
		
		JTextArea textArea = new JTextArea( instructionText );
		textArea.setEditable( false );
		textArea.setLineWrap( true );
		textArea.setWrapStyleWord( true );
		
		contentPanel.add( textArea, "2, 2" );	
		
		JPanel configOptionsPanel = new JPanel();
		configOptionsPanel.setBorder( new TitledBorder( "XML Import Options" ) );
		configOptionsPanel.setLayout( new FormLayout( new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				new ColumnSpec( "default:grow" ),
				FormFactory.RELATED_GAP_COLSPEC},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,	
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,	
				FormFactory.RELATED_GAP_ROWSPEC }));
		
		clsCheckBox = new JCheckBox( "Create missing classes" );
		slotCheckBox = new JCheckBox( "Create missing slots" );

		configOptionsPanel.add( clsCheckBox, "2, 2" );
		configOptionsPanel.add( slotCheckBox, "2, 4" );
					
		contentPanel.add( configOptionsPanel, "2, 4" );
	
		okButton = new JButton( new OkAction() );
		cancelButton = new JButton( new CancelAction() );
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout( new FormLayout(new ColumnSpec[] {
				new ColumnSpec( "default:grow" ),
				new ColumnSpec( "42dlu" ),
				FormFactory.RELATED_GAP_COLSPEC,
				new ColumnSpec( "42dlu" ),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC },
			new RowSpec[] {
				new RowSpec( "20dlu" ),
				FormFactory.RELATED_GAP_ROWSPEC}));			
			
		buttonPane.add( okButton, "2, 1" );
		buttonPane.add( cancelButton, "4, 1" );
	
		getContentPane().add( buttonPane, BorderLayout.SOUTH );			
	}
	
	@SuppressWarnings("serial")
	class OkAction extends AbstractAction {
		public OkAction() {
			super( "OK" );
		}
		
		public void actionPerformed( ActionEvent evt ) {
			closeOption = OK_OPTION;
			setVisible( false );
			dispose();
		}
	}
	
	@SuppressWarnings("serial")
	class CancelAction extends AbstractAction {
		public CancelAction() {
			super( "Cancel" );
		}
		
		public void actionPerformed( ActionEvent evt ) {						
			closeOption = CANCEL_OPTION;
			setVisible( false );
			dispose();
		}
	}
	
	/**
	 * Returns the value of which button was clicked to close the dialog.
	 * 
	 * @return Either <code>ImportConfigDialog.OK_OPTION</code> or <code>ImportConfigDialog.CANCEL_OPTION</code>
	 *           depending on which button was clicked on to close the dialog.
	 */
	public int getCloseOption() {
		return closeOption;
	}
	
	/**		
	 * @return True is the user selected to automatically create the classes if they don't exist, returns false otherwise.
	 */
	public boolean isGenerateClasses() {
		return clsCheckBox.isSelected();
	}

	/**
	 * @return True is the user selected to automatically create the slots if they don't exist, returns false otherwise.
	 */
	public boolean isGenerateSlots() {
		return slotCheckBox.isSelected();
	}
	
	/**
	 * For testing purposes only.
	 * 
	 * Auto-generated main method to display this JDialog.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame();
				ImportConfigDialog inst = new ImportConfigDialog(frame);
				inst.setVisible(true);
			}
		});
	}
}
