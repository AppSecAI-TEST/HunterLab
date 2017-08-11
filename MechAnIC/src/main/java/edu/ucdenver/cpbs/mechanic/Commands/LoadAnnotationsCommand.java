package edu.ucdenver.cpbs.mechanic.Commands;

import edu.ucdenver.cpbs.mechanic.TextAnnotation.TextAnnotationUtil;
import edu.ucdenver.cpbs.mechanic.ui.MechAnICIcons;
import edu.ucdenver.cpbs.mechanic.ui.TextViewer;
import org.protege.editor.core.ui.view.DisposableAction;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class LoadAnnotationsCommand extends DisposableAction {

    private TextAnnotationUtil textAnnotationUtil;
    private JTabbedPane tabbedPane;

    public LoadAnnotationsCommand(TextAnnotationUtil textAnnotationUtil, JTabbedPane tabbedPane) {
        super("Load Annotations", MechAnICIcons.getIcon(MechAnICIcons.LOAD_ANNOTATIONS_ICON));
        this.textAnnotationUtil = textAnnotationUtil;
        this.tabbedPane = tabbedPane;
        this.putValue(AbstractAction.SHORT_DESCRIPTION, "Load annotations");
    }

    @Override
    public void dispose() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        FileFilter fileFilter = new FileNameExtensionFilter(" XML", "xml");
        fileChooser.setFileFilter(fileFilter);

        TextViewer textViewer = (TextViewer)((JScrollPane)tabbedPane.getSelectedComponent()).getViewport().getView();

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                textAnnotationUtil.loadTextAnnotationsFromXML(new FileInputStream(new File(fileChooser.getSelectedFile().getAbsolutePath())), textViewer);
            } catch (IOException | SAXException | ParserConfigurationException e1) {
                e1.printStackTrace();
            }
        }
    }
}
