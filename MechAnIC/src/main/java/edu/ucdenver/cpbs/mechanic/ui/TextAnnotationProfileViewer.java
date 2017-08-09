package edu.ucdenver.cpbs.mechanic.ui;

import javax.swing.*;

public class TextAnnotationProfileViewer extends JPanel {
    public TextAnnotationProfileViewer() {
        super();
        this.setName("Text Annotation Profiles");
    }

    public void addProfile(String profileName) {
        JCheckBox profileSelector = new JCheckBox(profileName);
    }
}
