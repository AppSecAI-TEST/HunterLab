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
 *   Brant Barney (Original Author)
 */

package edu.uchsc.ccp.knowtator.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

/**
 * Dialog containing a JList and OK/Cancel buttons. Facilitates the
 *   selection of an object from the list.
 *   
 * See the code in <code>SlotMentionValueWidget.requestSymbolValue</code> for example usage.
 * 
 * @author brant
 */
@SuppressWarnings("serial")
public class ListConfirmDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	
	private JList list;
	private JButton okButton;
	private JButton cancelButton;

	private List<Object> valueList;
	private Object defaultValue;
	
	private String labelText;
	
	public static final int OK_OPTION = 1;
	public static final int CANCEL_OPTION = 2;
	
	private int closeOption = -1;
	
	/**
	 * Create the dialog.
	 */
	public ListConfirmDialog( Frame parent,
							  String title,
							  String labelText,
							  boolean isModal,
							  List<Object> valueList,
							  Object defaultValue ) {
		
		super( parent, title );
	
		this.valueList = valueList;
		this.defaultValue = defaultValue;
		this.labelText = labelText;
		
		initComponents();
		setModal( isModal );	
	}
	
	private void initComponents() {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				new ColumnSpec( "default:grow" ),
				FormFactory.RELATED_GAP_COLSPEC},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,	
				FormFactory.RELATED_GAP_ROWSPEC,
				new RowSpec( "default:grow" )}));
		
		JLabel label = new JLabel( labelText );
		contentPanel.add(label, "2, 2");
		
		okButton = new JButton( new OkAction() );
		cancelButton = new JButton( new CancelAction() );
		
		list = new JList( valueList.toArray() );
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		if( defaultValue != null ) {
			list.setSelectedValue( defaultValue, true );
		}
		
		JScrollPane listScrollPane = new JScrollPane( list );
		contentPanel.add(listScrollPane, "2, 4, fill, fill" );
			
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
		registerListListeners();
	}
	
	private void registerListListeners() {
		list.addMouseListener( new MouseAdapter() {
    		public void mouseClicked( MouseEvent e ){
    		   if( e.getClickCount() == 2 ){
    			   
    			   if( list.getSelectedValue() != null ) {    			   
    				   closeOption = OK_OPTION;
    				   setVisible( false );
    				   dispose();
    			   }
    		   }
    		}
		});

		list.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent ke) {				
				Object obj = list.getSelectedValue();
				
				if( obj == null ) {
					return;
				}
				
				if (ke.getKeyCode() == KeyEvent.VK_ENTER) {					
					closeOption = OK_OPTION;
					setVisible( false );					
					ke.consume();
					dispose();
				}
			}
		});
		
		list.addListSelectionListener( new ListSelectionListener() {		
			public void valueChanged(ListSelectionEvent e) {
				okButton.setEnabled( list.getSelectedValue() != null );
			}
		});
	}
	
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
	
	public Object getSelectedItem() {
		return list.getSelectedValue();
	}
	
	public int getCloseOption() {
		return closeOption;
	}	
}
