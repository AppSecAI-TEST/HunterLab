package edu.ucdenver.cpbs.mechanic.TextAnnotation;

import edu.ucdenver.cpbs.mechanic.MechAnICView;
import edu.ucdenver.cpbs.mechanic.ProfileManager;
import edu.ucdenver.cpbs.mechanic.owl.OWLAPIDataExtractor;
import edu.ucdenver.cpbs.mechanic.ui.TextViewer;
import org.semanticweb.owlapi.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.*;
import java.util.*;

import static edu.ucdenver.cpbs.mechanic.xml.XmlUtil.asList;

public final class TextAnnotationUtil {
    private static String TAG_ANNOTATIONS = "annotations";
    private static String TAG_TEXTSOURCE = "textSource";

    private static String TAG_MENTION = "mention";
    private static String TAG_MENTION_ID = "id";
    private static String TAG_ANNOTATOR = "annotator";
    private static String TAG_ANNOTATOR_ID = "id";
    private static String TAG_SPAN = "span";
    private static String TAG_SPAN_START = "start";
    private static String TAG_SPAN_END = "end";
    private static String TAG_SPANNEDTEXT = "spannedText";
    private static String TAG_ANNOTATION = "annotation";

    private static String TAG_CLASS_MENTION = "classMention";
    private static String TAG_CLASS_MENTION_ID = "id";
    private static String TAG_MENTION_CLASS = "mentionClass";
    private static String TAG_MENTION_CLASS_ID = "id";

    private DocumentBuilder dBuilder;

   private HashMap<String, HashMap<Integer, TextAnnotation>> textAnnotations;

   private MechAnICView view;
   private ProfileManager profileManager;
   private OWLAPIDataExtractor dataExtractor;

    public TextAnnotationUtil(MechAnICView view) throws ParserConfigurationException {
        this.view = view;

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        this.dBuilder = dbFactory.newDocumentBuilder();

        dataExtractor = new OWLAPIDataExtractor(this.view.getOWLModelManager());


        textAnnotations = new HashMap<>();
    }

    public void loadTextAnnotationsFromXML(InputStream is, TextViewer textViewer) throws ParserConfigurationException, IOException, SAXException {

        Document doc = dBuilder.parse(is);
        doc.getDocumentElement().normalize();

        for (Node node: asList(doc.getElementsByTagName("annotation"))) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element)node;

                String fullMention = ((Element)eElement.getElementsByTagName(TAG_MENTION).item(0)).getAttribute(TAG_MENTION_ID);
                String annotatorName = eElement.getElementsByTagName(TAG_ANNOTATOR).item(0).getTextContent();
                String annotatorID = ((Element)eElement.getElementsByTagName(TAG_ANNOTATOR).item(0)).getAttribute(TAG_ANNOTATOR_ID);
                Integer spanStart = Integer.parseInt(((Element)eElement.getElementsByTagName(TAG_SPAN).item(0)).getAttribute(TAG_SPAN_START));
                Integer spanEnd = Integer.parseInt(((Element)eElement.getElementsByTagName(TAG_SPAN).item(0)).getAttribute(TAG_SPAN_END));
                String spannedText = eElement.getElementsByTagName(TAG_SPANNEDTEXT).item(0).getTextContent();

                String mentionSource = getMentionSourceFromXML(fullMention);
                int mentionID = getMentionIDFromXML(fullMention);

                TextAnnotation newAnnotation = new TextAnnotation(annotatorID, annotatorName, spanStart, spanEnd, spannedText);
                if(!textAnnotations.containsKey(mentionSource)) {
                    textAnnotations.put(mentionSource, new HashMap<>());
                }
                textAnnotations.get(mentionSource).put(mentionID, newAnnotation);
                highlightAnnotation(spanStart, spanEnd, textViewer, mentionSource);
            }
        }
        for (Node node : asList(doc.getElementsByTagName("classMention"))) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element)node;
                String fullMention = eElement.getAttribute(TAG_CLASS_MENTION_ID);
                String classID = ((Element)eElement.getElementsByTagName(TAG_MENTION_CLASS).item(0)).getAttribute(TAG_MENTION_CLASS_ID);
                String className = eElement.getElementsByTagName(TAG_MENTION_CLASS).item(0).getTextContent();

                String mentionSource = getMentionSourceFromXML(fullMention);
                int mentionID = getMentionIDFromXML(fullMention);

                if(!textAnnotations.containsKey(mentionSource)) {
                    textAnnotations.put(mentionSource, new HashMap<>());
                }
                TextAnnotation textAnnotation = textAnnotations.get(mentionSource).get(mentionID);
                textAnnotation.setClassID(classID);
                textAnnotation.setClassName(className);
            }
        }
    }

    private String getMentionSourceFromXML(String fullMention) {
        String mentionSource;
        if(fullMention.indexOf("_new_Instance") < fullMention.indexOf("Instance") && fullMention.contains("_new_Instance")) {
            mentionSource = fullMention.substring(0, fullMention.indexOf("_new_Instance"));
        } else {
            mentionSource = fullMention.substring(0, fullMention.indexOf("_Instance"));
        }
        return mentionSource;
    }

    private Integer getMentionIDFromXML(String fullMention) {
        return Integer.parseInt(fullMention.substring(fullMention.indexOf("_Instance_")+10));
    }

    public void addTextAnnotation(OWLClass cls, Integer spanStart, Integer spanEnd, String spannedText) throws NoSuchFieldException {

        dataExtractor.extractOWLClassData(cls);


        String mentionSource = profileManager.getCurrentHighlighterName();
        int mentionID = textAnnotations.size();
        String classID = dataExtractor.getClassID();
        String className = dataExtractor.getClassName();

        TextAnnotation newTextAnnotation = new TextAnnotation(
                profileManager.getCurrentProfile().getAnnotatorID(),
                profileManager.getCurrentProfile().getAnnotatorName(),
                spanStart,
                spanEnd,
                spannedText,
                classID,
                className
        );
        if (!textAnnotations.containsKey(mentionSource)) {
            textAnnotations.put(mentionSource, new HashMap<>());
        }
        textAnnotations.get(mentionSource).put(mentionID, newTextAnnotation);
    }

    public void removeTextAnnotation(Integer spanStart, Integer spanEnd, TextViewer textViewer) {
        String mentionSource = profileManager.getCurrentHighlighterName();
        if (textAnnotations.containsKey(mentionSource)) {
            for (Map.Entry<Integer, TextAnnotation> instance : textAnnotations.get(mentionSource).entrySet()) {
                int mentionID = instance.getKey();
                TextAnnotation textAnnotation = instance.getValue();
                if (Objects.equals(spanStart, textAnnotation.getSpanStart()) && Objects.equals(spanEnd, textAnnotation.getSpanEnd())) {
                    textAnnotations.get(mentionSource).remove(mentionID);
                    break;
                }
            }
        }
        highlightAllAnnotations(textViewer);
    }


    private void highlightAllAnnotations(TextViewer textViewer) {
        textViewer.getHighlighter().removeAllHighlights();
        for (Map.Entry<String, HashMap<Integer, TextAnnotation>> instance1 : textAnnotations.entrySet()) {
            String mentionSource = instance1.getKey();
            for (Map.Entry<Integer, TextAnnotation> instance2 : instance1.getValue().entrySet() ){
                TextAnnotation textAnnotation = instance2.getValue();
                highlightAnnotation(textAnnotation.getSpanStart(), textAnnotation.getSpanEnd(), textViewer, mentionSource);
            }
        }
    }

    public void highlightAnnotation(int spanStart, int spanEnd, TextViewer textViewer, String mentionSource) {
        DefaultHighlighter.DefaultHighlightPainter highlighter = profileManager.getCurrentProfile().getHighlighter(mentionSource);
        if (highlighter == null) {
            Color c = JColorChooser.showDialog(null, String.format("Pick a color for %s", mentionSource), Color.BLUE);
            if (c != null) {
                profileManager.addHighlighter(mentionSource, c, profileManager.getCurrentProfile());
            }
        }
        highlighter = profileManager.getCurrentHighlighter();
        try {
            textViewer.getHighlighter().addHighlight(spanStart, spanEnd, highlighter);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void write(FileWriter fw, JTabbedPane tabbedPane) throws IOException, NoSuchFieldException {
        BufferedWriter bw = new BufferedWriter(fw);
        File textSource =new File(tabbedPane.getTitleAt((tabbedPane.getSelectedIndex())));

        bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        bw.newLine();
        bw.write(String.format("<%s %s=\"%s\">", TAG_ANNOTATIONS, TAG_TEXTSOURCE, textSource.getName()));
        bw.newLine();

        for (Map.Entry<String, HashMap<Integer, TextAnnotation>> instance1 : textAnnotations.entrySet()) {
            String mentionSource = instance1.getKey();
            for (Map.Entry<Integer, TextAnnotation> instance2 : instance1.getValue().entrySet() ){
                Integer mentionID = instance2.getKey();
                TextAnnotation textAnnotation = instance2.getValue();

                String mention = String.format("%s_Instance_%d", mentionSource, mentionID);

                for (String tag : toXML(textAnnotation, mention)) {
                    bw.write(tag);
                    bw.newLine();
                }

                bw.write(String.format("  <%s %s=\"%s\">", TAG_CLASS_MENTION, TAG_CLASS_MENTION_ID, mention));
                bw.newLine();
                bw.write(String.format("    <%s %s=\"%s\">%s</%s>", TAG_MENTION_CLASS, TAG_MENTION_CLASS_ID, textAnnotation.getClassID(), textAnnotation.getClassName(), TAG_MENTION_CLASS));
                bw.newLine();
                bw.write(String.format("  </%s>", TAG_CLASS_MENTION));
                bw.newLine();
            }
        }

        bw.write(String.format("</%s>", TAG_ANNOTATIONS));
        bw.flush();
    }

    private String[] toXML(TextAnnotation textAnnotation, String mention) {
        String[] toWrite = new String[6];

        toWrite[0] = String.format("  <%s>", TAG_ANNOTATION);
        toWrite[1] = String.format("    <%s %s=\"%s\" />", TAG_MENTION, TAG_MENTION_ID, mention);
        toWrite[2] = String.format("    <%s %s=\"%s\" />%s</%s>", TAG_ANNOTATOR, TAG_ANNOTATOR_ID, textAnnotation.getAnnotatorID(), textAnnotation.getAnnotator(), TAG_ANNOTATOR);
        toWrite[3] = String.format("    <%s %s=\"%s\" %s=\"%s\" />", TAG_SPAN, TAG_SPAN_START, textAnnotation.getSpanStart().toString(), TAG_SPAN_END, textAnnotation.getSpanEnd().toString());
        toWrite[4] = String.format("    <%s>%s</%s>", TAG_SPANNEDTEXT, textAnnotation.getSpannedText(), TAG_SPANNEDTEXT);
        toWrite[5] = String.format("  </%s>", TAG_ANNOTATION);

        return toWrite;
    }

    //TODO remove annotation and annotationHighlighting
    //TODO hover over annotations in text to see what they are

    public void setProfileManager(ProfileManager profileManager) {
        this.profileManager = profileManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }
}
