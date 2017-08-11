package edu.ucdenver.cpbs.mechanic.ui;


import javax.swing.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class TextViewer extends JTextPane {

	public TextViewer() {
		super();
		this.setName("Untitled");
		this.setEditable(false);
	}

	public void setJTextPaneFont(Font font) {
		// Start with the current input attributes for the JTextPane. This
		// should ensure that we do not wipe out any existing attributes
		// (such as alignment or other paragraph attributes) currently
		// set on the text area.
		MutableAttributeSet attrs = this.getInputAttributes();

		// Set the font family, size, and style, based on properties of
		// the Font object. Note that JTextPane supports a number of
		// character attributes beyond those supported by the Font class.
		// For example, underline, strike-through, super- and sub-script.
		StyleConstants.setFontFamily(attrs, font.getFamily());
		StyleConstants.setFontSize(attrs, font.getSize());
		StyleConstants.setItalic(attrs, (font.getStyle() & Font.ITALIC) != 0);
		StyleConstants.setBold(attrs, (font.getStyle() & Font.BOLD) != 0);


		// Retrieve the pane's document object
		StyledDocument doc = this.getStyledDocument();

		// Replace the style for the entire document. We exceed the length
		// of the document by 1 so that text entered at the end of the
		// document uses the attributes.
		doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
	}

	//TODO caretListener that selects only full words
	//TODO resize text buttons

}
