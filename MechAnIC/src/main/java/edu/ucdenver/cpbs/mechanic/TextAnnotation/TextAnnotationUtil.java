package edu.ucdenver.cpbs.mechanic.TextAnnotation;

import edu.ucdenver.cpbs.mechanic.MechAnICView;
import edu.ucdenver.cpbs.mechanic.ui.TextAnnotationProfileViewer;
import org.protege.editor.owl.model.find.OWLEntityFinder;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;
import java.util.List;

import static edu.ucdenver.cpbs.mechanic.xml.XmlUtil.asList;

public final class TextAnnotationUtil {
    private String ANNOTATIONS = "annotations";
    private String TEXTSOURCE = "textSource";
    private String CLASS_MENTION = "classMention";
    private String CLASS_MENTION_ID = "id";
    private String MENTION_CLASS = "mentionClass";
    private String MENTION_CLASS_ID = "id";

    private DocumentBuilder dBuilder;

   private String currentAnnotator;
   private String currentAnnotatorID;

   private List<TextAnnotation> textAnnotations;
   private HashMap<String, String> clsList;
   private HashMap<String, String> classInfo;
   private HashMap<String, Integer> instancesTracker;
   private MechAnICView view;

   private TextAnnotationProfileViewer profileViewer;


    public TextAnnotationUtil(MechAnICView view) throws ParserConfigurationException {
        this.view = view;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        this.dBuilder = dbFactory.newDocumentBuilder();
        this.textAnnotations = new ArrayList<TextAnnotation>();
        this.clsList = new HashMap<String, String>();
        this.classInfo  = new HashMap<String, String>();
        this.instancesTracker = new HashMap<String, Integer>();
    }

    public void loadTextAnnotationsFromXML(InputStream is, JTabbedPane tabbedPane) throws ParserConfigurationException, IOException, SAXException {

        Document doc = dBuilder.parse(is);
        doc.getDocumentElement().normalize();

        for (Node node: asList(doc.getElementsByTagName("annotation"))) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                textAnnotations.add(new TextAnnotation(node, tabbedPane, profileViewer));
            }
        }
        for (Node node : asList(doc.getElementsByTagName("classMention"))) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element)node;
                String mention = eElement.getAttribute(CLASS_MENTION_ID);
                String classID = ((Element)eElement.getElementsByTagName(MENTION_CLASS).item(0)).getAttribute(MENTION_CLASS_ID);
                String className = eElement.getElementsByTagName(MENTION_CLASS).item(0).getTextContent();
                classInfo.put(classID, className);
                clsList.put(mention, classID);
            }
        }
    }

    private String addMention(String classNameSpace){
        String mention;
        if (instancesTracker.containsKey(classNameSpace)) {
            mention = classNameSpace + "_Instance_" + Integer.toString(instancesTracker.get(classNameSpace));
            instancesTracker.put(classNameSpace, instancesTracker.get(classNameSpace) + 1);
        }
        else{
            mention = classNameSpace + "_New_Instance_" + Integer.toString(0);
            instancesTracker.put(classNameSpace, 1);
        }
        return mention;
    }


    public void addTextAnnotation(OWLClass cls, Integer spanStart, Integer spanEnd, String spannedText, JTabbedPane tabbedPane) throws NoSuchFieldException {
        OWLOntology ont = view.getOWLModelManager().getActiveOntology();
        Collection<OWLAnnotation> owlAnnotations = EntitySearcher.getAnnotations(cls.getIRI(), ont);
//        Collection<OWLAnnotationProperty> currentOWLAnnoProps = ont.getAnnotationPropertiesInSignature();

        String classNameSpace = "";
        String classID = "";
        String className = "";

        OWLEntityFinder entityFinder = view.getOWLModelManager().getOWLEntityFinder();
        OWLAnnotationProperty nameSpaceLabel = entityFinder.getOWLAnnotationProperty("has_obo_namespace");
        OWLAnnotationProperty idLabel = entityFinder.getOWLAnnotationProperty("id");
//        OWLAnnotationProperty nameLabel = entityFinder.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI().toString());
        for (OWLAnnotation anno : owlAnnotations){
            if (anno.getProperty() == nameSpaceLabel) {
                if (anno.getValue() instanceof OWLLiteral) {
                    classNameSpace = ((OWLLiteral) anno.getValue()).getLiteral();
                }
            } else if (anno.getProperty() == idLabel) {
                if (anno.getValue() instanceof OWLLiteral) {
                    classID = ((OWLLiteral) anno.getValue()).getLiteral();
                }
            } if (anno.getProperty().isLabel()) {
                if (anno.getValue() instanceof OWLLiteral) {
                    className = ((OWLLiteral) anno.getValue()).getLiteral();
                }
            }
        }



        String mention = addMention(classNameSpace);
        System.out.printf("Namespace: %s\n", classNameSpace);
        System.out.printf("ID: %s\n", classID);
        System.out.printf("Name: %s\n", className);
        System.out.printf("Mention: %s\n", mention);

        classInfo.put(classID, className);
        clsList.put(mention, classID);
        textAnnotations.add(new TextAnnotation(mention, currentAnnotatorID, currentAnnotator, spanStart, spanEnd, spannedText, tabbedPane, profileViewer));
    }

    public void setCurrentAnnotator(String currentAnnotator, String currentAnnotatorID) {
        this.currentAnnotator = currentAnnotator;
        this.currentAnnotatorID = currentAnnotatorID;
    }

    public void write(FileWriter fw, JTabbedPane tabbedPane) throws IOException, NoSuchFieldException {
        BufferedWriter bw = new BufferedWriter(fw);
        File textSource =new File(tabbedPane.getTitleAt((tabbedPane.getSelectedIndex())));

        bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        bw.newLine();
        bw.write(String.format("<%s %s=\"%s\">", ANNOTATIONS, TEXTSOURCE, textSource.getName()));
        bw.newLine();

        for (TextAnnotation textAnnotation : textAnnotations) {
            textAnnotation.write(bw);
        }
        for (Map.Entry<String, String> instance : clsList.entrySet()) {
            String mention = instance.getKey();
            String classID = instance.getValue();

            bw.write(String.format("  <%s %s=\"%s\">", CLASS_MENTION, CLASS_MENTION_ID, mention));
            bw.newLine();
            bw.write(String.format("    <%s %s=\"%s\">%s</%s>", MENTION_CLASS, MENTION_CLASS_ID, classID, classInfo.get(classID), MENTION_CLASS));
            bw.newLine();
            bw.write(String.format("  </%s>", CLASS_MENTION));
            bw.newLine();
        }
        bw.write(String.format("</%s>", ANNOTATIONS));
        bw.flush();
    }

    public void setProfileViewer(TextAnnotationProfileViewer profileViewer) {
        this.profileViewer = profileViewer;
    }
}
