package edu.ucdenver.cpbs.mechanic.TextAnnotation;

import edu.ucdenver.cpbs.mechanic.ui.TextAnnotationProfileViewer;
import edu.ucdenver.cpbs.mechanic.ui.TextViewer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.io.BufferedWriter;
import java.io.IOException;

@SuppressWarnings("FieldCanBeLocal")
public class TextAnnotation {

    private static String MENTION = "mention";
    private static String MENTION_ID = "id";
    private static String ANNOTATOR = "annotator";
    private static String ANNOTATOR_ID = "id";
    private static String SPAN = "span";
    private static String SPAN_START = "start";
    private static String SPAN_END = "end";
    private static String SPANNEDTEXT = "spannedText";
    private static String ANNOTATION = "annotation";
    public String mention;
    private String annotatorID;
    public String annotator;
    private Integer spanStart;
    private Integer spanEnd;
    private String spannedText;

    public TextAnnotation(String mention, String annotatorID, String annotator, Integer spanStart, Integer spanEnd, String spannedText, JTabbedPane tabbedPane, TextAnnotationProfileViewer profileViewer) {
        this.mention = mention;
        this.annotatorID = annotatorID;
        this.annotator = annotator;
        this.spanStart = spanStart;
        this.spanEnd = spanEnd;
        this.spannedText = spannedText;
        highlightAnnotation(spanStart, spanEnd, tabbedPane, profileViewer);
    }

    public TextAnnotation(Node node, JTabbedPane tabbedPane, TextAnnotationProfileViewer profileViewer) {

        Element eElement = (Element)node;

        this.mention = ((Element)eElement.getElementsByTagName(MENTION).item(0)).getAttribute(MENTION_ID);
        this.annotator = eElement.getElementsByTagName(ANNOTATOR).item(0).getTextContent();
        this.annotatorID = ((Element)eElement.getElementsByTagName(ANNOTATOR).item(0)).getAttribute(ANNOTATOR_ID);
        this.spanStart = Integer.parseInt(((Element)eElement.getElementsByTagName(SPAN).item(0)).getAttribute(SPAN_START));
        this.spanEnd = Integer.parseInt(((Element)eElement.getElementsByTagName(SPAN).item(0)).getAttribute(SPAN_END));
        this.spannedText = eElement.getElementsByTagName(SPANNEDTEXT).item(0).getTextContent();
        highlightAnnotation(spanStart, spanEnd, tabbedPane, profileViewer);
    }

    private void highlightAnnotation(int spanStart, int spanEnd, JTabbedPane tabbedPane, TextAnnotationProfileViewer profileViewer) {
        TextViewer selectedComponent = (TextViewer)((JScrollPane)tabbedPane.getSelectedComponent()).getViewport().getView();
        try {
            selectedComponent.getHighlighter().addHighlight(spanStart, spanEnd, profileViewer.getCurrentHighlighter());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private String[] toXML() {
        String[] toWrite = new String[6];

        toWrite[0] = String.format("  <%s>", ANNOTATION);
        toWrite[1] = String.format("    <%s %s=\"%s\" />", MENTION, MENTION_ID, mention);
        toWrite[2] = String.format("    <%s %s=\"%s\" />%s</%s>", ANNOTATOR, ANNOTATOR_ID, annotatorID, annotator, ANNOTATOR);
        toWrite[3] = String.format("    <%s %s=\"%s\" %s=\"%s\" />", SPAN, SPAN_START, spanStart.toString(), SPAN_END, spanEnd.toString());
        toWrite[4] = String.format("    <%s>%s</%s>", SPANNEDTEXT, spannedText, SPANNEDTEXT);
        toWrite[5] = String.format("  </%s>", ANNOTATION);

        return toWrite;
    }

    void write(BufferedWriter bw) throws IOException {
        for (String tag : toXML()) {
            bw.write(tag);
            bw.newLine();
        }
    }
}
