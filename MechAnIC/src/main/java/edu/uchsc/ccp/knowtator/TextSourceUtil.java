/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Knowtator.
 *
 * The Initial Developer of the Original Code is University of Colorado.  
 * Copyright (C) 2005 - 2008.  All Rights Reserved.
 *
 * Knowtator was developed by the Center for Computational Pharmacology
 * (http://compbio.uchcs.edu) at the University of Colorado Health 
 *  Sciences Center School of Medicine with support from the National 
 *  Library of Medicine.  
 *
 * Current information about Knowtator can be obtained at 
 * http://knowtator.sourceforge.net/
 *
 * Contributor(s):
 *   Philip V. Ogren <philip@ogren.info> (Original Author)
 */

package edu.uchsc.ccp.knowtator;

import java.awt.Component;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.uchsc.ccp.knowtator.textsource.TextSource;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;
import edu.uchsc.ccp.knowtator.textsource.TextSourceChangeEvent;
import edu.uchsc.ccp.knowtator.textsource.TextSourceChangeListener;
import edu.uchsc.ccp.knowtator.textsource.TextSourceCollection;
import edu.uchsc.ccp.knowtator.textsource.TextSourceCollectionChangeEvent;
import edu.uchsc.ccp.knowtator.textsource.TextSourceCollectionChangeListener;

/**
 * This class helps one to find a "text source" instance in the knowledgebase
 * given a TextSource object or vice versa.
 * 
 * In order to make this as simple to code up as possible, I made some
 * simplifying assumptions which result in less flexibility for the user.
 * 
 * One is that all instances of "text source" share the same namespace - and
 * there should be no overlap - even across different types of "text source"
 * instances.
 * 
 * A user must know where a to find a TextSource given its name if the code
 * cannot find it in the TextSouceCollection that it has open.
 * 
 * 
 */

public class TextSourceUtil {

	AnnotationUtil annotationUtil;

	KnowtatorProjectUtil kpu;

	KnowledgeBase kb;

	Project project;

	Class[] textSourceCollectionClasses;

	String[] displayNames;

	HashMap displayNames2TSC;

	String[] clsNames;

	HashMap clsNames2TSC;

	TextSource currentTextSource;

	java.util.List<TextSourceChangeListener> textSourceChangeListeners;

	TextSourceCollection currentTextSourceCollection;

	java.util.List<TextSourceCollectionChangeListener> textSourceCollectionChangeListeners;

	Logger logger = Logger.getLogger(TextSourceUtil.class);

	public TextSourceUtil(AnnotationUtil annotationUtil, KnowtatorProjectUtil kpu) {
		this.annotationUtil = annotationUtil;
		this.kpu = kpu;
		this.kb = annotationUtil.kb;
		this.project = kb.getProject();

		textSourceChangeListeners = new ArrayList<TextSourceChangeListener>();
		textSourceCollectionChangeListeners = new ArrayList<TextSourceCollectionChangeListener>();

		java.util.List<Class> classes = new ArrayList<Class>();

		Collection<Instance> tscImplementations = (Collection<Instance>) kb
				.getInstances(kpu.getTscImplementationsCls());

		String[] textSourceCollectionClassNames = new String[tscImplementations.size()];

		int _tsc = 0;
		for (Instance tscImplementation : tscImplementations) {
			String className = (String) tscImplementation.getOwnSlotValue(kb.getSlot("knowtator_tsc_implementation"));
			textSourceCollectionClassNames[_tsc++] = className;
		}

		Class textSourceCollectionClass = null;
		try {
			textSourceCollectionClass = Class.forName("edu.uchsc.ccp.knowtator.textsource.TextSourceCollection");
		} catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}

		for (int i = 0; i < textSourceCollectionClassNames.length; i++) {
			try {
				Class tscClass = Class.forName(textSourceCollectionClassNames[i]);
				if (isSubclass(tscClass, textSourceCollectionClass)) {
					classes.add(tscClass);
				} else
					System.err.println("WARNING: class " + textSourceCollectionClassNames[i]
							+ " is not a subclass of edu.uchsc.ccp.knowtator.textsource.TextSourceCollection");
			} catch (ClassNotFoundException cnfe) {
				System.err.println("WARNING: class " + textSourceCollectionClassNames[i] + " not found in classpath");
			}
		}
		textSourceCollectionClasses = (Class[]) (classes.toArray(new Class[classes.size()]));
		displayNames = new String[textSourceCollectionClasses.length];
		displayNames2TSC = new HashMap(textSourceCollectionClasses.length);
		clsNames = new String[textSourceCollectionClasses.length];
		clsNames2TSC = new HashMap(textSourceCollectionClasses.length);

		for (int i = 0; i < textSourceCollectionClasses.length; i++) {
			try {
				Field displayNameField = textSourceCollectionClasses[i].getField("DISPLAY_NAME");
				String displayName = (String) displayNameField.get(null);
				displayNames[i] = displayName;
				displayNames2TSC.put(displayName, textSourceCollectionClasses[i]);

				Field clsNameField = textSourceCollectionClasses[i].getField("CLS_NAME");
				String clsName = (String) clsNameField.get(null);
				clsNames[i] = clsName;
				clsNames2TSC.put(clsName, textSourceCollectionClasses[i]);

				Method createClsMethod = textSourceCollectionClasses[i].getMethod("createCls",
						new Class[] { KnowledgeBase.class });
				createClsMethod.invoke(null, kb);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
		logger.debug("made it to here");

	}

	public void init() {
		TextSourceCollection tsc = ProjectSettings.getRecentTextSourceCollection(project);
		if (tsc != null) {
			int recentIndex = ProjectSettings.getRecentTextSourceCollectionIndex(project);

			setCurrentTextSourceCollection(tsc);

			TextSource recentTextSource = null;
			try {
				recentTextSource = tsc.get(recentIndex);
			} catch (TextSourceAccessException tsae) {
				try {
					recentTextSource = tsc.get(0);
				} catch (TextSourceAccessException e) {
				}
			}
			if (recentTextSource != null) {
				setCurrentTextSource(recentTextSource);
			}
		}
	}

	public TextSourceCollection selectTextSourceCollection(Component parent) {
		Object selection = JOptionPane.showInputDialog(parent, "Please select a text source type.",
				"Text source type selection", JOptionPane.PLAIN_MESSAGE, null, displayNames, displayNames[0]);

		TextSourceCollection newTextSourceCollection;

		try {
			if (selection != null) {
				Class textSourceCollectionClass = (Class) displayNames2TSC.get(selection);
				Method selectionMethod = textSourceCollectionClass.getMethod("open", new Class[] { Project.class,
						Component.class });
				newTextSourceCollection = (TextSourceCollection) selectionMethod.invoke(null, new Object[] { project,
						parent });
				return newTextSourceCollection;
			}
		} catch (Exception exception) {
			StringWriter stackTraceWriter = new StringWriter();
			exception.printStackTrace(new PrintWriter(stackTraceWriter));
			String stackTrace = stackTraceWriter.toString();
			JOptionPane.showMessageDialog(parent, "Error opening text source collection:  Stack trace = " + stackTrace,
					"Error opening text source collection", JOptionPane.ERROR_MESSAGE, null);
			exception.printStackTrace();
		}
		return null;
	}

	private static boolean isSubclass(Class subClass, Class superClass) {
		Class cls = subClass.getSuperclass();
		if (cls == null)
			return false;
		else if (cls.equals(superClass)) {
			return true;
		} else {
			return isSubclass(cls, superClass);
		}

	}

	/**
	 * This method finds a "text source" instance in the knowledgebase for the
	 * given TextSouce object. The "search" is very simplistic - simply return
	 * the instance in the knowledgebase that has the name textSource.getName().
	 * The namespace of your text sources - regardless of what kind they are -
	 * should not have conflicts/overlaps
	 * 
	 * @param textSource
	 * @param create
	 *            - if true: if there is not a "text source" instance for the
	 *            TextSource object then create one.
	 * @return the "text source" instance for the given TextSource object.
	 */

	public SimpleInstance getTextSourceInstance(TextSource textSource, boolean create) {
		SimpleInstance textSourceInstance = (SimpleInstance) kb.getInstance(textSource.getName());
		if (textSourceInstance == null && create) {
			return (SimpleInstance) textSource.createTextSourceInstance(kb);
		}
		return textSourceInstance;
	}

	public TextSource getTextSource(Instance textSourceInstance) throws TextSourceAccessException {
		TextSourceCollection tsc = getCurrentTextSourceCollection();
		String name = textSourceInstance.getName();
		return tsc.get(name);
	}

	public String getTextSourceAnnotationComment(Instance textSourceInstance) {

		String annotationComment = (String) textSourceInstance.getOwnSlotValue(kpu.annotationCommentSlot);
		if (annotationComment == null) {
			return "";
		}
		return annotationComment;
	}

	public String getTextSourceAnnotationComment(TextSource textSource) {
		Instance textSourceInstance = getTextSourceInstance(textSource, false);
		if (textSourceInstance != null) {
			return getTextSourceAnnotationComment(textSourceInstance);
		}
		return "";
	}

	public void setTextSourceAnnotationComment(Instance textSourceInstance, String comment) {
		textSourceInstance.setOwnSlotValue(kpu.annotationCommentSlot, comment);
	}

	public void setTextSourceAnnotationComment(TextSource textSource, String comment) {
		Instance textSourceInstance = getTextSourceInstance(textSource, true);
		if (textSourceInstance != null) {
			setTextSourceAnnotationComment(textSourceInstance, comment);
		}
	}

	public void addTextSourceChangeListener(TextSourceChangeListener textSourceChangeListener) {
		textSourceChangeListeners.add(textSourceChangeListener);
	}

	public void removeTextSourceChangeListener(TextSourceChangeListener textSourceChangeListener) {
		textSourceChangeListeners.remove(textSourceChangeListener);
	}

	public TextSource getCurrentTextSource() {
		return currentTextSource;
	}

	public void setCurrentTextSource(TextSource textSource) {
		if (textSource != null) {
			this.currentTextSource = textSource;
			ProjectSettings.setRecentTextSourceCollectionIndex(project, currentTextSourceCollection
					.getIndex(textSource));
			fireTextSourceChanged(textSource);
		}
	}

	// invoke SwingWorker or something....
	void fireTextSourceChanged(TextSource textSource) {
		TextSourceChangeEvent event = new TextSourceChangeEvent(textSource);
		for (TextSourceChangeListener tscl : textSourceChangeListeners) {
			tscl.textSourceChanged(event);
		}
	}

	/**
	 * TextSourceCollection methods
	 */

	public void addTextSourceCollectionChangeListener(
			TextSourceCollectionChangeListener textSourceCollectionChangeListener) {
		textSourceCollectionChangeListeners.add(textSourceCollectionChangeListener);
	}

	public void removeTextSourceCollectionChangeListener(
			TextSourceCollectionChangeListener textSourceCollectionChangeListener) {
		textSourceCollectionChangeListeners.remove(textSourceCollectionChangeListener);
	}

	public TextSourceCollection getCurrentTextSourceCollection() {
		return currentTextSourceCollection;
	}

	public void setCurrentTextSourceCollection(TextSourceCollection textSourceCollection) {
		if (textSourceCollection != null) {
			this.currentTextSourceCollection = textSourceCollection;
			ProjectSettings.setRecentTextSourceCollection(project, textSourceCollection);
			fireTextSourceCollectionChange(currentTextSourceCollection);
			try {
				setCurrentTextSource(textSourceCollection.get(0));
			} catch (TextSourceAccessException tsae) {
				tsae.printStackTrace();
			}
		}
	}

	// invoke SwingWorker or something....
	void fireTextSourceCollectionChange(TextSourceCollection textSourceCollection) {
		TextSourceCollectionChangeEvent event = new TextSourceCollectionChangeEvent(textSourceCollection);
		for (TextSourceCollectionChangeListener tsccl : textSourceCollectionChangeListeners) {
			tsccl.textSourceCollectionChanged(event);
		}
	}

}
