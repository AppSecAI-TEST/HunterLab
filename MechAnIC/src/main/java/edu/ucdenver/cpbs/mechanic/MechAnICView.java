package edu.ucdenver.cpbs.mechanic;

import edu.ucdenver.cpbs.mechanic.TextAnnotation.TextAnnotationUtil;
import edu.ucdenver.cpbs.mechanic.Commands.*;
import edu.ucdenver.cpbs.mechanic.ui.TextViewer;
import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.cls.AbstractOWLClassViewComponent;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static edu.ucdenver.cpbs.mechanic.Commands.OpenDocumentCommand.openInitialFile;


@SuppressWarnings("PackageAccessibility")
public class MechAnICView extends AbstractOWLClassViewComponent implements DropTargetListener {

    private static final Logger log = Logger.getLogger(MechAnICView.class);

    private JTabbedPane tabbedPane;

    private TextAnnotationUtil textAnnotationUtil;
    private MechAnICSelectionModel selectionModel;

    @Override
    public void initialiseClassView() throws Exception {

        selectionModel = new MechAnICSelectionModel();
        textAnnotationUtil = new TextAnnotationUtil(this);
        textAnnotationUtil.setCurrentAnnotator("none", "none");

        createUI();
        DropTarget dt = new DropTarget(this, this);
        dt.setActive(true);

        log.warn("Initialized MechAnIC");
    }

    @Override
    protected OWLClass updateView(OWLClass selectedClass) {
        selectionModel.setSelectedClass(selectedClass);
        return selectedClass;
    }

    private void createUI() {
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        add(tabbedPane);
        openInitialFile("C:/Users/Harrison/Documents/test_article.txt", tabbedPane);

        setupListeners();

        createToolBar();

    }

    private void createToolBar() {
        addAction(new OpenDocumentCommand(tabbedPane), "A", "A");
        addAction(new SaveCommand(textAnnotationUtil, tabbedPane), "A", "B");
        addAction(new CloseCommand(tabbedPane), "A", "C");
        addAction(new SetAnnotator(textAnnotationUtil), "B", "A");
        addAction(new AddTextAnnotation(textAnnotationUtil, tabbedPane, selectionModel), "B", "B");
        addAction(new LoadAnnotations(textAnnotationUtil, tabbedPane), "B", "C");
        addAction(new AddNewHighlighterProfile(textAnnotationUtil), "C", "A");
    }

    private void setupListeners() {
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

    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        TextViewer tp = new TextViewer();
        JScrollPane sp = new JScrollPane(tp);
        frame.add(sp);
        String fileName = "C:/Users/Harrison/Desktop/plant_catalog.xml/";
        tp.read(new BufferedReader(new FileReader(fileName)), fileName);

        frame.pack();
        frame.setVisible(true);
    }

}
