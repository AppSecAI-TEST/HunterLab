package edu.ucdenver.cpbs.mechanic.Commands;

import edu.ucdenver.cpbs.mechanic.TextAnnotation.TextAnnotationUtil;
import org.protege.editor.core.ui.util.Icons;
import org.protege.editor.core.ui.view.DisposableAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SetAnnotator extends DisposableAction {

    private TextAnnotationUtil textAnnotationUtil;

    public SetAnnotator(TextAnnotationUtil textAnnotationUtil) {
        super("Set Annotator", Icons.getIcon("set_annotator.png"));
        this.putValue(AbstractAction.SHORT_DESCRIPTION, "Set current annotator");
        this.textAnnotationUtil = textAnnotationUtil;

    }

    @Override
    public void dispose() {

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        setAnnotator();
    }

    private void setAnnotator() {
        JTextField field1 = new JTextField();
        JTextField field2 = new JTextField();
        Object[] message = {
                "Annotator name", field1,
                "Annotator ID", field2,
        };
        int option = JOptionPane.showConfirmDialog(null, message, "Enter annotator name and ID", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION)
        {
            String annotator = field1.getText();
            String annotatorID = field2.getText();
            textAnnotationUtil.setCurrentAnnotator(annotator, annotatorID);
        }

    }
}
