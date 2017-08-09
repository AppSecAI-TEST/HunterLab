package edu.ucdenver.cpbs.mechanic.Commands;

import edu.ucdenver.cpbs.mechanic.TextAnnotation.TextAnnotationUtil;
import edu.ucdenver.cpbs.mechanic.MechAnICSelectionModel;
import edu.ucdenver.cpbs.mechanic.ui.TextViewer;
import org.protege.editor.core.ui.util.Icons;
import org.protege.editor.core.ui.view.DisposableAction;
import org.semanticweb.owlapi.model.OWLClass;

import javax.swing.*;
import java.awt.event.ActionEvent;

@SuppressWarnings("PackageAccessibility")
public class AddTextAnnotation extends DisposableAction {

    private TextAnnotationUtil textAnnotationUtil;
    private JTabbedPane tabbedPane;
    private MechAnICSelectionModel selectionModel;

    public AddTextAnnotation(TextAnnotationUtil textAnnotationUtil, JTabbedPane tabbedPane, MechAnICSelectionModel selectionModel) {
        super("Add TextAnnotation", Icons.getIcon("add_annotation.png"));
        this.textAnnotationUtil = textAnnotationUtil;
        this.tabbedPane = tabbedPane;
        this.selectionModel = selectionModel;

        this.putValue(AbstractAction.SHORT_DESCRIPTION, "Add an annotation");

    }

    @Override
    public void dispose() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        addAnnotation();
    }

    private void addAnnotation() {
        OWLClass cls = selectionModel.getSelectedClass();

        TextViewer selectedComponent = (TextViewer)((JScrollPane)tabbedPane.getSelectedComponent()).getViewport().getView();
        Integer spanStart = selectedComponent.getSelectionStart();
        Integer spanEnd = selectedComponent.getSelectionEnd();
        String spannedText = selectedComponent.getSelectedText();
        try {
            textAnnotationUtil.addTextAnnotation(cls, spanStart, spanEnd, spannedText, tabbedPane);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

}
