package edu.ucdenver.cpbs.mechanic.ui;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;

public class TextViewer extends JPanel {

	private JTextArea textArea;


	public TextViewer() {
		this.setName("Untitled");
		textArea = new JTextArea(40, 80);
		textArea.setLayout(new BorderLayout());
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane scrollPane;
		this.add(scrollPane = new JScrollPane(textArea));
		scrollPane.getVerticalScrollBar().setUnitIncrement(20);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(20);
	}
	

	public JTextArea getTextArea() {
		return textArea;
	}

}
