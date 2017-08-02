package edu.uchsc.ccp.knowtator.util;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.InvalidSpanException;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.Span;
import edu.uchsc.ccp.knowtator.TextSourceUtil;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;

public class SimpleFileImport {

	public static int importFromFile(KnowtatorManager manager, File file) throws TextSourceAccessException, NumberFormatException, InvalidSpanException, IOException {
		AnnotationUtil annotationUtil = manager.getAnnotationUtil();
		BufferedReader input = new BufferedReader(new FileReader(file));
		String line;
		int i=1;
		while((line = input.readLine()) != null) {
			try {
				String[] columns = line.split("\\|");
				for(String column : columns) {
					System.out.println("column: "+column);
				}
    			String textSourceName = columns[0];
    			String annotationClassName = columns[1];
    			int begin = Integer.parseInt(columns[2]);
    			int end = Integer.parseInt(columns[3]);
    			String annotatorInstanceId = null;
    			if(columns.length > 4) {
    				annotatorInstanceId = columns[4];
    			}
    			String annotationSetId = null;
    			if(columns.length > 5)
    				annotationSetId = columns[5];
    			
    			Cls annotationCls = manager.getKnowledgeBase().getCls(annotationClassName);
    			if(annotationCls == null) {
    				throw new RuntimeException("Unknown class '"+annotationClassName+"'");
    			}
    			
    			SimpleInstance annotator = null;
    			if(annotatorInstanceId != null && !annotatorInstanceId.equals("")) {
    				 annotator = manager.getKnowledgeBase().getSimpleInstance(annotatorInstanceId);
    			}
    			if(annotator == null) {
    				annotator = manager.getSelectedAnnotator();
    			}
    			
    			SimpleInstance annotationSet = null;
    			if(annotationSetId != null && !annotationSetId.equals(""))
    				annotationSet = manager.getKnowledgeBase().getSimpleInstance(annotationSetId);
    			if(annotationSet == null) {
    				annotationSet = manager.getSelectedAnnotationSet();
    			}
    			
    			List<Span> spans = new ArrayList<Span>();
    			spans.add(new Span(begin, end));
    
    			annotationUtil.createAnnotation(annotationCls, spans, null, textSourceName, annotator, annotationSet);
			} catch (Throwable t) {
				throw new RuntimeException("the file import failed at line: "+i+".  "+t.getMessage(), t);
			}
			i++;
		}
		
		return i-1;
	}
	
	public static void importFromFile(Component parent, KnowtatorManager manager, TextSourceUtil textSourceUtil) {

		int option = JOptionPane.showConfirmDialog(parent,
						  "The following dialogs allow you to import annotations from a file \n"
						+ "conforming to a simple format.  Each line of the file should be in\n"
						+ "the following format: \n"
						+ "textsource|annotation-type|offset-begin|offset-end|annotator|annotator-set\n"
						+ "where \n" 
						+ "textsource is the name of textsource to add the annotation to. \n" 
						+ "annotation-type is the name of the class type of the new annotation.\n"
						+ "offset-begin is the begin offset of the annotation\n"
						+ "offset-end is the end offset of the annotation\n"
						+ "annotator is the annotator id of the annotator assigned to this annotation\n"
						+ "\tyou can leave this value blank to use the default selected annotator\n"
						+ "\tor if, there is no default annotator, to leave the annotator unassigned.\n"
						+ "annotator-set is the id of the annotation set to be assigned to this annotation\n"
						+ "\tyou can leave this value blank to use the default selected annotation set\n"
						+ "\tor if, there is no default annotation set, to leave the annotation set unassigned.\n"
						+ "If an exception is thrown during annotation import,\n"
						+ "then importing will stop and an error message will appear stating the problem.\n"
						+ "Otherwise, a message stating successful completion will appear.  If annotation\n"
						+ "import does not successfully complete, please discard all changes made by\n"
						+ "the partial import by closing your Knowtator project without saving changes\n"
						+ "and reopening.", "Simple Import", JOptionPane.OK_CANCEL_OPTION);
		if (option != JOptionPane.OK_OPTION)
			return;

		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Please choose the file to import.");
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(true);

		int returnVal = chooser.showOpenDialog(parent);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		File selectedFile = chooser.getSelectedFile();

		int importedAnnotationCount = 0;
		try {
			importedAnnotationCount = importFromFile(manager, selectedFile);

		} catch (Exception e) {
			JOptionPane.showMessageDialog(parent, e, "Exception thrown while importing annotations",
					JOptionPane.ERROR_MESSAGE);
			JOptionPane.showMessageDialog(parent, "Please discard all changes made by this partial import\n"
					+ "by close this Knowtator project without saving changes and reopening", "Please discard changes",
					JOptionPane.WARNING_MESSAGE);
			e.printStackTrace();
			return;
		} finally {
			textSourceUtil.setCurrentTextSource(textSourceUtil.getCurrentTextSource());
		}

		JOptionPane.showMessageDialog(parent, ""+importedAnnotationCount+" annotations successfully imported. \n"
				+ "To discard changes created by import close this Protege project without saving.",
				"simple import complete", JOptionPane.INFORMATION_MESSAGE);
	}

}
