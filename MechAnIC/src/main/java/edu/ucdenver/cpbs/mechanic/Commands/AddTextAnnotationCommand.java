package edu.ucdenver.cpbs.mechanic.Commands;

import edu.ucdenver.cpbs.mechanic.TextAnnotation.TextAnnotationUtil;
import edu.ucdenver.cpbs.mechanic.MechAnICSelectionModel;
import edu.ucdenver.cpbs.mechanic.ui.MechAnICIcons;
import edu.ucdenver.cpbs.mechanic.ui.TextViewer;
import org.protege.editor.core.ui.view.DisposableAction;
import org.semanticweb.owlapi.model.OWLClass;

import javax.swing.*;
import java.awt.event.ActionEvent;

@SuppressWarnings("PackageAccessibility")
public class AddTextAnnotationCommand extends DisposableAction {

    private TextAnnotationUtil textAnnotationUtil;
    private JTabbedPane tabbedPane;
    private MechAnICSelectionModel selectionModel;

    public AddTextAnnotationCommand(TextAnnotationUtil textAnnotationUtil, JTabbedPane tabbedPane, MechAnICSelectionModel selectionModel) {
        super("Add TextAnnotation", MechAnICIcons.getIcon(MechAnICIcons.ADD_TEXT_ANNOTATION_ICON));
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
        addTextAnnotation();
    }

    private void addTextAnnotation() {
        OWLClass cls = selectionModel.getSelectedClass();

        TextViewer textViewer = (TextViewer)((JScrollPane)tabbedPane.getSelectedComponent()).getViewport().getView();
        Integer spanStart = textViewer.getSelectionStart();
        Integer spanEnd = textViewer.getSelectionEnd();
        String spannedText = textViewer.getSelectedText();
        try {
            textAnnotationUtil.addTextAnnotation(cls, spanStart, spanEnd, spannedText);
            textAnnotationUtil.highlightAnnotation(spanStart, spanEnd, textViewer, textAnnotationUtil.getProfileManager().getCurrentHighlighterName());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

}
