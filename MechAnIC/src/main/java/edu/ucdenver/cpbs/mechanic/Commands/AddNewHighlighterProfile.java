package edu.ucdenver.cpbs.mechanic.Commands;

import edu.ucdenver.cpbs.mechanic.ui.TextAnnotationProfileViewer;
import org.protege.editor.core.ui.util.Icons;
import org.protege.editor.core.ui.view.DisposableAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class AddNewHighlighterProfile extends DisposableAction {

    private TextAnnotationProfileViewer profileViewer;

    public AddNewHighlighterProfile(TextAnnotationProfileViewer profileViewer) {
        super("Add Highlighter Profile", Icons.getIcon("add_highlighter.png"));
        this.profileViewer = profileViewer;
        this.putValue(AbstractAction.SHORT_DESCRIPTION, "Add a new highlighter profile");

    }

    @Override
    public void dispose() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        addHighlighterProfile();
    }

    private void addHighlighterProfile() {
        JTextField field1 = new JTextField();
        Object[] message = {
                "Highlighter profile name", field1,
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Enter name for the new highlighter profile", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION)
        {
            String profileName = field1.getText();

            Color c = JColorChooser.showDialog(null, "Highlighter color", Color.BLUE);
            if (c != null) {
                profileViewer.addProfile(profileName, c);
                profileViewer.setCurrentProfile(profileName);
            }
        }

    }
}