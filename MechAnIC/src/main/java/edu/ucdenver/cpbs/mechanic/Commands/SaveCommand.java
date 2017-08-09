package edu.ucdenver.cpbs.mechanic.Commands;

import edu.ucdenver.cpbs.mechanic.TextAnnotation.TextAnnotationUtil;
import org.protege.editor.core.ui.util.Icons;
import org.protege.editor.core.ui.view.DisposableAction;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;

public class SaveCommand extends DisposableAction {

    private TextAnnotationUtil textAnnotationUtil;
    private JTabbedPane tabbedPane;

    public SaveCommand(TextAnnotationUtil textAnnotationUtil, JTabbedPane tabbedPane) {
        super("Save", Icons.getIcon("save.png"));
        this.textAnnotationUtil = textAnnotationUtil;
        this.tabbedPane = tabbedPane;

        this.putValue(AbstractAction.SHORT_DESCRIPTION, "Save");


    }

    @Override
    public void dispose() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        FileFilter fileFilter = new FileNameExtensionFilter(" XML", "xml");
        fileChooser.setFileFilter(fileFilter);
        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            FileWriter fw;
            try {

                fw = new FileWriter(fileChooser.getSelectedFile().getAbsolutePath());
                textAnnotationUtil.write(fw, tabbedPane);
                fw.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (NoSuchFieldException e1) {
                e1.printStackTrace();
            }
        }
    }
}
