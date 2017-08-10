package edu.ucdenver.cpbs.mechanic.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.util.HashMap;

public class TextAnnotationProfileViewer extends JPanel {
    private String currentHighlighterProfile;
    private HashMap<String, DefaultHighlighter.DefaultHighlightPainter> highlighterPainters;

    public TextAnnotationProfileViewer() {
        super(new GridLayout(10, 5));
        Border border = BorderFactory.createTitledBorder("Text Annotation Profiles");this.highlighterPainters = new HashMap<String, DefaultHighlighter.DefaultHighlightPainter>();
        setBorder(border);
    }

    public void addProfile(String profileName, Color c) {
        highlighterPainters.put(profileName, new DefaultHighlighter.DefaultHighlightPainter(c));
        JCheckBox profileSelector = new JCheckBox(profileName);
        add(profileSelector);
        repaint();
    }

    public void setCurrentProfile(String profile) {
        currentHighlighterProfile = profile;
    }

    public DefaultHighlighter.DefaultHighlightPainter getCurrentHighlighter() {
        return highlighterPainters.get(currentHighlighterProfile);
    }
}
