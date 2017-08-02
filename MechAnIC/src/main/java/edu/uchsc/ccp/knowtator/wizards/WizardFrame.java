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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class WizardFrame extends JDialog {
	private static final long serialVersionUID = 0;

	public static final String PREVIOUS = "PREVIOUS";

	public static final String NEXT = "NEXT";

	public static final String CANCEL = "CANCEL";

	JButton previousButton;

	JButton nextButton;

	JButton cancelButton;

	JPanel contentPane;

	public WizardFrame(JFrame parent, String title) {
		this(parent, title, new Dimension(400, 300));
	}

	public WizardFrame(JFrame parent, String title, Dimension dimension) {
		super(parent, title, true);
		setSize(dimension);
		JPanel buttonsPanel = new JPanel();

		previousButton = new JButton("Previous");
		previousButton.setActionCommand(PREVIOUS);
		nextButton = new JButton("Next");
		nextButton.setActionCommand(NEXT);
		cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand(CANCEL);

		buttonsPanel.add(previousButton);
		buttonsPanel.add(nextButton);
		buttonsPanel.add(cancelButton);

		contentPane = new JPanel();

		setLayout(new BorderLayout());
		add(buttonsPanel, BorderLayout.SOUTH);
		add(contentPane, BorderLayout.CENTER);
	}

}
