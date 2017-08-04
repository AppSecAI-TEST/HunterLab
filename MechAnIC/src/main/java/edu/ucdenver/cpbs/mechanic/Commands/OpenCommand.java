package edu.ucdenver.cpbs.mechanic.Commands;

import edu.ucdenver.cpbs.mechanic.ui.TextViewer;
import org.protege.editor.core.ui.util.Icons;
import org.protege.editor.core.ui.view.DisposableAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileReader;
import java.io.IOException;

public class OpenCommand extends DisposableAction {

    private JTabbedPane tabbedPane;
    private JFileChooser fileChooser;

    public OpenCommand(JTabbedPane tabbedPane, JFileChooser fileChooser) {
        super("Open", Icons.getIcon("open.png"));
        this.putValue(AbstractAction.SHORT_DESCRIPTION, "Open");
        this.tabbedPane = tabbedPane;
        this.fileChooser = fileChooser;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            openFile(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void openFile(String fileName) {
        FileReader fr;
        try {
            fr = new FileReader(fileName);
            TextViewer textViewer = new TextViewer();
            textViewer.setName(fileName);
            if (tabbedPane.getTabCount() == 1 && tabbedPane.getTitleAt(0).equals("Untitled")) {
                tabbedPane.setComponentAt(0, textViewer);
            } else {
                tabbedPane.add(textViewer);
            }
            tabbedPane.setTitleAt(tabbedPane.getTabCount()-1, fileName);


            textViewer.getTextArea().read(fr, null);
            fr.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {

    }
}
