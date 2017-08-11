package edu.ucdenver.cpbs.mechanic.TextAnnotation;

import edu.ucdenver.cpbs.mechanic.ProfileManager;
import edu.ucdenver.cpbs.mechanic.ui.TextViewer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.io.BufferedWriter;
import java.io.IOException;

@SuppressWarnings("FieldCanBeLocal")
public class TextAnnotation {


    private String annotatorID;
    private String annotator;
    private Integer spanStart;
    private Integer spanEnd;
    private String spannedText;
    private String classID;
    private String className;

    public TextAnnotation(String annotatorID,
                          String annotator,
                          Integer spanStart,
                          Integer spanEnd,
                          String spannedText,
                          String classID,
                          String className) {
        this.annotatorID = annotatorID;
        this.annotator = annotator;
        this.spanStart = spanStart;
        this.spanEnd = spanEnd;
        this.spannedText = spannedText;
        this.classID = classID;
        this.className = className;
    }

    public TextAnnotation(String annotatorID,
                          String annotator,
                          Integer spanStart,
                          Integer spanEnd,
                          String spannedText) {
        this.annotatorID = annotatorID;
        this.annotator = annotator;
        this.spanStart = spanStart;
        this.spanEnd = spanEnd;
        this.spannedText = spannedText;

    }

    String getSpannedText() {
        return spannedText;
    }

    String getAnnotatorID() {
        return annotatorID;
    }

    String getAnnotator() {
        return annotator;
    }

    Integer getSpanStart() {
        return spanStart;
    }

    Integer getSpanEnd() {
        return spanEnd;
    }

    public String getClassID() {
        return classID;
    }

    public String getClassName() {
        return className;
    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
