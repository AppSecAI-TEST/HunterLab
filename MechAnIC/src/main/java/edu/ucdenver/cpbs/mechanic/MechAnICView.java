package edu.ucdenver.cpbs.mechanic;

import edu.ucdenver.cpbs.mechanic.TextAnnotation.TextAnnotationUtil;
import edu.ucdenver.cpbs.mechanic.Commands.*;
import edu.ucdenver.cpbs.mechanic.ui.ProfileViewer;
import edu.ucdenver.cpbs.mechanic.ui.TextViewer;
import org.apache.log4j.Logger;
import org.protege.editor.owl.ui.view.cls.AbstractOWLClassViewComponent;
import org.semanticweb.owlapi.model.*;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.dnd.*;
import java.io.*;

@SuppressWarnings("PackageAccessibility")
public class MechAnICView extends AbstractOWLClassViewComponent implements DropTargetListener {

    private static final Logger log = Logger.getLogger(MechAnICView.class);

    private JSplitPane splitPane;
    private JTabbedPane tabbedPane;
    private ProfileManager profileManager;

    private TextAnnotationUtil textAnnotationUtil;
    private MechAnICSelectionModel selectionModel;

    @Override
    public void initialiseClassView() throws Exception {

        selectionModel = new MechAnICSelectionModel();
        textAnnotationUtil = new TextAnnotationUtil(this);
        profileManager = new ProfileManager();

        textAnnotationUtil.setProfileManager(profileManager);

        createUI();
        DropTarget dt = new DropTarget(this, this);
        dt.setActive(true);

        setupTest();
        log.warn("Initialized MechAnIC");
    }

    @Override
    protected OWLClass updateView(OWLClass selectedClass) {
        selectionModel.setSelectedClass(selectedClass);
        return selectedClass;
    }

    //TODO add view to see and manage the mechanism Graphs
    private void createUI() {
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        tabbedPane.setMinimumSize(new Dimension(100, 50));

        ProfileViewer profileViewer = new ProfileViewer(profileManager);
        profileViewer.setMinimumSize(new Dimension(100, 50));
        profileManager.setProfileViewer(profileViewer);
        profileManager.setupDefault();

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        add(splitPane);
        splitPane.add(tabbedPane);
        splitPane.add(profileViewer);
        splitPane.setDividerLocation(1200);

        setupListeners();

        createToolBar();
    }

    private void setupTest() {
        try {
            String fileName = "/file/test_article.txt";
            TextViewer textViewer = new TextViewer();
            textViewer.setName(fileName);
            JScrollPane sp = new JScrollPane(textViewer);
            tabbedPane.add(sp);
            tabbedPane.setTitleAt(0, fileName);

            InputStream is = getClass().getResourceAsStream(fileName);
            textViewer.read(new BufferedReader(new InputStreamReader(is)), fileName);


        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            String fileName = "/file/test_annotations.xml";
            InputStream is = getClass().getResourceAsStream(fileName);
            TextViewer textViewer = (TextViewer)((JScrollPane)tabbedPane.getSelectedComponent()).getViewport().getView();
            textAnnotationUtil.loadTextAnnotationsFromXML(is, textViewer);
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }
    }

    private void createToolBar() {
        addAction(new OpenDocumentCommand(tabbedPane), "A", "A");
        addAction(new CloseDocumentCommand(tabbedPane), "A", "B");
        addAction(new IncreaseTextSizeCommand(tabbedPane), "A", "C");
        addAction(new DecreaseTextSizeCommand(tabbedPane), "A", "D");

        addAction(new LoadAnnotationsCommand(textAnnotationUtil, tabbedPane), "B", "A");
        addAction(new SaveAnnotationsCommand(textAnnotationUtil, tabbedPane), "B", "B");
        addAction(new AddTextAnnotationCommand(textAnnotationUtil, tabbedPane, selectionModel), "B", "C");
        addAction(new RemoveTextAnnotationCommand(textAnnotationUtil, tabbedPane, selectionModel), "B", "D");

        addAction(new NewProfileCommand(profileManager), "C", "A");
        addAction(new SwitchProfileCommand(profileManager), "C", "B");
        addAction(new NewHighlighterCommand(profileManager), "C", "C");
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
