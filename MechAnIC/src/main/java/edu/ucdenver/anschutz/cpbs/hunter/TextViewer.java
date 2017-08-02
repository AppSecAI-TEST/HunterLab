package edu.ucdenver.anschutz.cpbs.hunter;

import org.apache.log4j.Logger;
import org.protege.editor.owl.model.OWLModelManager;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

class TextViewer extends JPanel {

    private static final Logger log = Logger.getLogger(TextViewer.class);
    private JTextPane textPane1;
    private JButton openButton;
    private JFileChooser fileChooser = new JFileChooser();
    private ActionListener openAction = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            TextViewer.this.openActionPerformed(e);
        }
    };

    private OWLModelManager modelManager;

    TextViewer(final OWLModelManager modelManager) {
        this.modelManager = modelManager;
        this.fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

        this.openButton.addActionListener(this.openAction);
        this.add(this.textPane1);
        this.add(openButton);
    }

    void dispose() {
        this.openButton.removeActionListener(this.openAction);
    }

    private void openActionPerformed(ActionEvent e) {
        int result = fileChooser.showOpenDialog(TextViewer.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            log.warn("Selected file: " + selectedFile.getAbsolutePath());
        }
    }

}
