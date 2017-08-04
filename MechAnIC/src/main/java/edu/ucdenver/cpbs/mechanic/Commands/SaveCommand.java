package edu.ucdenver.cpbs.mechanic.Commands;

import edu.ucdenver.cpbs.mechanic.ui.TextViewer;
import org.protege.editor.core.ui.util.Icons;
import org.protege.editor.core.ui.view.DisposableAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;

public class SaveCommand extends DisposableAction {

    private JTabbedPane tabbedPane;
    private JFileChooser fileChooser;

    public SaveCommand(JTabbedPane tabbedPane, JFileChooser fileChooser) {
        super("Save", Icons.getIcon("save.png"));
        this.putValue(AbstractAction.SHORT_DESCRIPTION, "Save");

        this.tabbedPane = tabbedPane;
        this.fileChooser = fileChooser;
    }

    @Override
    public void dispose() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        saveFile();
    }

    private void saveFile() {
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            FileWriter fw;
            try {

                fw = new FileWriter(fileChooser.getSelectedFile().getAbsolutePath());
                TextViewer selectedComp = (TextViewer)tabbedPane.getSelectedComponent();
                selectedComp.getTextArea().write(fw);
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
