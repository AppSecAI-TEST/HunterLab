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
 *   Brant Barney <brant.barney@hsc.utah.edu>
 */
package edu.uchsc.ccp.knowtator.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.util.ComponentUtilities;
import edu.uchsc.ccp.knowtator.InvalidSpanException;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.Span;
import edu.uchsc.ccp.knowtator.SpanUtil;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;

public class SpanEditPanel extends JPanel implements ActionListener {
	static final long serialVersionUID = 0;

	KnowtatorManager manager;

	AnnotationInstanceDisplay annotationDisplay;

	SpanUtil spanUtil;

	JButton growLeftAnnotationButton;

	JButton shrinkLeftAnnotationButton;

	JButton shrinkRightAnnotationButton;

	JButton growRightAnnotationButton;

	public SpanEditPanel(KnowtatorManager manager, AnnotationInstanceDisplay annotationDisplay) {
		super(new GridBagLayout());
		this.manager = manager;
		this.annotationDisplay = annotationDisplay;
		this.spanUtil = manager.getSpanUtil();
		initialize();
	}

	private void initialize() {
		growLeftAnnotationButton = new JButton(ComponentUtilities.loadImageIcon(SpanEditPanel.class,
				"/edu/uchsc/ccp/knowtator/images/extend_left.gif"));
		growLeftAnnotationButton.setActionCommand(SpanUtil.GROW_ANNOTATION_LEFT);
		growLeftAnnotationButton.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		growLeftAnnotationButton.setToolTipText("Grow annotation on left side");
		growLeftAnnotationButton.setBorder(null);
		shrinkLeftAnnotationButton = new JButton(ComponentUtilities.loadImageIcon(SpanEditPanel.class,
				"/edu/uchsc/ccp/knowtator/images/shrink_left.gif"));
		shrinkLeftAnnotationButton.setActionCommand(SpanUtil.SHRINK_ANNOTATION_LEFT);
		shrinkLeftAnnotationButton.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		shrinkLeftAnnotationButton.setToolTipText("Shrink annotation on left side");
		shrinkLeftAnnotationButton.setBorder(null);
		shrinkRightAnnotationButton = new JButton(ComponentUtilities.loadImageIcon(SpanEditPanel.class,
				"/edu/uchsc/ccp/knowtator/images/shrink_right.gif"));
		shrinkRightAnnotationButton.setActionCommand(SpanUtil.SHRINK_ANNOTATION_RIGHT);
		shrinkRightAnnotationButton.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		shrinkRightAnnotationButton.setToolTipText("Shrink annotation on right side");
		shrinkRightAnnotationButton.setBorder(null);
		growRightAnnotationButton = new JButton(ComponentUtilities.loadImageIcon(SpanEditPanel.class,
				"/edu/uchsc/ccp/knowtator/images/extend_right.gif"));
		growRightAnnotationButton.setActionCommand(SpanUtil.GROW_ANNOTATION_RIGHT);
		growRightAnnotationButton.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		growRightAnnotationButton.setToolTipText("Grow annotation on right side");
		growRightAnnotationButton.setBorder(null);

		growLeftAnnotationButton.addActionListener(this);
		shrinkLeftAnnotationButton.addActionListener(this);
		growRightAnnotationButton.addActionListener(this);
		shrinkRightAnnotationButton.addActionListener(this);

		JLabel spanEditLabel = new JLabel("span edit:");
		spanEditLabel.setMinimumSize(new Dimension( 0, 0 ) );
		
		add(spanEditLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.EAST,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 2, 2));
		add(growLeftAnnotationButton, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 2, 2));
		add(shrinkLeftAnnotationButton, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 2, 2));
		add(shrinkRightAnnotationButton, new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 2, 2));
		add(growRightAnnotationButton, new GridBagConstraints(4, 0, 1, 1, 0, 0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 2, 2));
	}

	public void actionPerformed(ActionEvent event) {
		String command = event.getActionCommand();

		if (command.equals(SpanUtil.GROW_ANNOTATION_LEFT) || command.equals(SpanUtil.GROW_ANNOTATION_RIGHT)
				|| command.equals(SpanUtil.SHRINK_ANNOTATION_LEFT) || command.equals(SpanUtil.SHRINK_ANNOTATION_RIGHT)) {
			try {
				List<Span> selectedSpans = annotationDisplay.getSelectedSpans();
				//SimpleInstance selectedAnnotation = manager.getSelectedAnnotation();
				
				SimpleInstance selectedAnnotation = manager.getSelectedAnnotation();
				
				if( annotationDisplay instanceof ConsensusAnnotationInstanceDisplay ) {
					//Must be the consensus annotation details panel, so use the consensus annotation instead					
					selectedAnnotation = manager.getSelectedConsensusAnnotation();
					
					//Note, the probably is a better way of doing this, but this seems to work for now.
				}
				
				if((event.getModifiers() & ActionEvent.CTRL_MASK) > 0)
					command = command+"_WORD";
				
				if (selectedSpans.size() > 0) {
					int[] selectedSpanIndices = annotationDisplay.getSelectedSpanIndices();
					spanUtil.editSpans(selectedSpans, selectedAnnotation, command);
					annotationDisplay.setSelectedSpanIndices(selectedSpanIndices);
				}
			} catch (InvalidSpanException ise) {
				JOptionPane.showMessageDialog(this, "TextAnnotation has an invalid span value: " + ise.getMessage(),
						"Invalid span", JOptionPane.ERROR_MESSAGE);
			} catch (TextSourceAccessException tsae) {
				JOptionPane.showMessageDialog(null, "There was a problem retrieving the text from the text source: "
						+ tsae.getMessage(), "Text Source Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}	
	
	/**
	 * Enables or disables the grow and shrink buttons based on the given parameter
	 * 
	 * @param enable When true, the buttons will be enabled, when false, the buttons
	 *                will be disabled.
	 */
	public void enableButtons( boolean enable ) {
		growLeftAnnotationButton.setEnabled( enable );
		shrinkLeftAnnotationButton.setEnabled( enable );
		shrinkRightAnnotationButton.setEnabled( enable );
		growRightAnnotationButton.setEnabled( enable );
	}
}
