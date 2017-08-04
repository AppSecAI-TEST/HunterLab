package edu.ucdenver.cpbs.mechanic;

import edu.ucdenver.cpbs.mechanic.Commands.CloseCommand;
import edu.ucdenver.cpbs.mechanic.Commands.OpenCommand;
import edu.ucdenver.cpbs.mechanic.Commands.SaveCommand;
import edu.ucdenver.cpbs.mechanic.ui.MechAnICSelectionModel;
import edu.ucdenver.cpbs.mechanic.ui.TextViewer;
import edu.ucdenver.cpbs.mechanic.ui.options.MechAnICViewOptions;
import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.cls.AbstractOWLClassViewComponent;
import org.semanticweb.owlapi.model.OWLClass;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.dnd.*;


public class MechAnICView extends AbstractOWLClassViewComponent implements DropTargetListener {

    private static final Logger log = Logger.getLogger(MechAnICView.class);
    private MechAnICSelectionModel selectionModel;
    private JTabbedPane tabbedPane;
    private JFileChooser fileChooser;
    private MechAnICViewOptions options;


    @Override
    public void initialiseClassView() throws Exception {
        selectionModel = new MechAnICSelectionModel();

        createUI();

        DropTarget dt = new DropTarget(this, this);
        dt.setActive(true);

        log.warn("Initialized MechAnIC");
    }

    private void createUI() {
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        add(tabbedPane);
        tabbedPane.add(new TextViewer());

        fileChooser = new JFileChooser();
        FileFilter fileFilter = new FileNameExtensionFilter("Plain text", "txt");
        fileChooser.setFileFilter(fileFilter);

        setupListeners();

        createToolBar();

    }

    private void createToolBar() {
        addAction(new OpenCommand(tabbedPane, fileChooser), "A", "A");
        addAction(new SaveCommand(tabbedPane, fileChooser), "A", "B");
        addAction(new CloseCommand(tabbedPane), "A", "B");
    }

    private void setupListeners() {

    }



    @Override
    protected OWLClass updateView(OWLClass selectedClass) {
        selectionModel.setSelectedClass(selectedClass);
        return selectedClass;
    }

    @Override
    public void disposeView() {

    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {

    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragExit(DropTargetEvent dte) {

    }

    @Override
    public void drop(DropTargetDropEvent dtde) {

    }

    public MechAnICViewOptions getOptions() {
        return options;
    }
}
