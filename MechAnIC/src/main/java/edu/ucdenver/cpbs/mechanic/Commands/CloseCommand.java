package edu.ucdenver.cpbs.mechanic.Commands;

import edu.ucdenver.cpbs.mechanic.ui.TextViewer;
import org.protege.editor.core.ui.util.Icons;
import org.protege.editor.core.ui.view.DisposableAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class CloseCommand extends DisposableAction {

    private JTabbedPane tabbedPane;

    public CloseCommand(JTabbedPane tabbedPane) {
        super("Close", Icons.getIcon("close.png"));
        this.putValue(AbstractAction.SHORT_DESCRIPTION, "Close");
        this.tabbedPane = tabbedPane;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int selectedComp = tabbedPane.getSelectedIndex();
        if (tabbedPane.getTabCount() > 1) {
            tabbedPane.remove(selectedComp);
        } else {
            tabbedPane.setComponentAt(0, new TextViewer());
            tabbedPane.setTitleAt(0, "Untitled");
        }



    }


    @Override
    public void dispose() {

    }

}
