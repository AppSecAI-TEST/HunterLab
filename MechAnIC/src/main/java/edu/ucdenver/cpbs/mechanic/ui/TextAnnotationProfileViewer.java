package edu.ucdenver.cpbs.mechanic.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultHighlighter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

public class TextAnnotationProfileViewer extends JPanel implements ActionListener {
    private String currentHighlighterProfile;
    private HashMap<String, DefaultHighlighter.DefaultHighlightPainter> highlighterPainters;
    private ButtonGroup buttonGroup;

    public TextAnnotationProfileViewer() {
        super(new GridLayout(0, 1));
        Border border = BorderFactory.createTitledBorder("Text Annotation Profiles");this.highlighterPainters = new HashMap<String, DefaultHighlighter.DefaultHighlightPainter>();
        setBorder(border);

        buttonGroup = new ButtonGroup();
    }

    public void addProfile(String profileName, Color c) {
        highlighterPainters.put(profileName, new DefaultHighlighter.DefaultHighlightPainter(c));
        JRadioButton profileSelector = new JRadioButton(profileName);
        profileSelector.setSelected(true);
        profileSelector.setActionCommand(profileName);

        buttonGroup.add(profileSelector);
        profileSelector.addActionListener(this);
        add(profileSelector);
        repaint();
    }

    public void setCurrentProfile(String profile) {
        currentHighlighterProfile = profile;
    }

    public DefaultHighlighter.DefaultHighlightPainter getCurrentHighlighter() {
        return highlighterPainters.get(currentHighlighterProfile);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setCurrentProfile(e.getActionCommand());
    }
}
