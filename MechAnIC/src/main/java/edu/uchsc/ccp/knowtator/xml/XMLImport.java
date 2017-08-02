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
 *   Brant Barney
 */

package edu.uchsc.ccp.knowtator.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import edu.stanford.smi.protege.model.*;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.FilterUtil;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.MentionUtil;
import edu.uchsc.ccp.knowtator.Span;
import edu.uchsc.ccp.knowtator.TextSourceUtil;
import edu.uchsc.ccp.knowtator.textsource.TextSource;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;
import edu.uchsc.ccp.knowtator.ui.dialog.ImportConfigDialog;

public class XMLImport {
	public static final String XML_IMPORT_DIRECTORY = "XML_IMPORT_FILE";

	private static File getRecentXMLImportDirectory(Project project) {
		String path = (String) project.getClientInformation(XML_IMPORT_DIRECTORY);
		if (path == null)
			return null;

		File xmlImportDirectory = new File(path);
		if (xmlImportDirectory.exists() && xmlImportDirectory.isDirectory()) {
			return xmlImportDirectory;
		}
		return null;
	}

	public static void readFromXML(java.awt.Frame parent, KnowledgeBase kb, KnowtatorProjectUtil kpu,
			TextSourceUtil textSourceUtil, AnnotationUtil annotationUtil, MentionUtil mentionUtil,
			FilterUtil filterUtil, Project project) {
	
		ImportConfigDialog icd = new ImportConfigDialog( parent );
		icd.setVisible( true );
					
		int option = icd.getCloseOption();
		if( option != ImportConfigDialog.OK_OPTION ) {
			return;
		}					
				
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Please choose xml files to read from.");
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setMultiSelectionEnabled(true);

		File recentXMLImportDirectory = getRecentXMLImportDirectory(project);
		if (recentXMLImportDirectory != null) {
			chooser.setCurrentDirectory(recentXMLImportDirectory.getParentFile());
		}

		int returnVal = chooser.showOpenDialog(parent);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		File[] selectedFiles = chooser.getSelectedFiles();

		System.out.println("selectedFiles.size()=" + selectedFiles.length);
		if (selectedFiles.length > 0) {
			project.setClientInformation(XML_IMPORT_DIRECTORY, selectedFiles[0].getParent());
		}

		try {
			for (File xmlFile : selectedFiles) {
				readFromXML(xmlFile, kb, kpu, annotationUtil, filterUtil, mentionUtil, textSourceUtil, icd.isGenerateClasses(), icd.isGenerateSlots());
			}
		} catch (KnowtatorXMLException kxe) {
			JOptionPane.showMessageDialog(parent, kxe, "Exception thrown while importing annotations",
					JOptionPane.ERROR_MESSAGE);
			JOptionPane.showMessageDialog(parent, "Please discard all changes made by this partial import\n"
					+ "by close this Knowtator project without saving changes and reopening", "Please discard changes",
					JOptionPane.WARNING_MESSAGE);
			kxe.printStackTrace();
			return;
		}

		JOptionPane.showMessageDialog(parent, "XML import successfully completed.\n"
				+ "To discard changes created by import close this Protege project without saving.",
				"XML import complete", JOptionPane.INFORMATION_MESSAGE);
	}

	public static void readFromXML(File xmlFile, 
								   KnowledgeBase kb,
								   KnowtatorProjectUtil kpu,
								   AnnotationUtil annotationUtil,
								   FilterUtil filterUtil,
								   MentionUtil mentionUtil,
								   TextSourceUtil textSourceUtil,
								   boolean createClasses,
								   boolean createSlots )
								throws KnowtatorXMLException {
		try {
			SAXBuilder builder = new SAXBuilder();
			Document doc = builder.build(xmlFile);

			Element root = doc.getRootElement();
			Attribute textSourceAttribute = root.getAttribute(XMLConstants.TEXT_SOURCE_ATTRIBUTE_NAME);
			String textSourceID = textSourceAttribute.getValue();
			SimpleInstance textSource = kb.getSimpleInstance(textSourceID);
			if (textSource == null) {
				TextSource ts = textSourceUtil.getCurrentTextSourceCollection().get(textSourceID);
				textSource = textSourceUtil.getTextSourceInstance(ts, true);
			}
			
			readClsMentions(root, kb, kpu, createClasses);
			readInstanceMentions(root, kb, kpu);
			readSlotMentions(root, kb, kpu, createSlots);
			readMentionSlots(root, kb, kpu, XMLConstants.CLASS_MENTION_ELEMENT_NAME);
			readMentionSlots(root, kb, kpu, XMLConstants.INSTANCE_MENTION_ELEMENT_NAME);
			readAnnotations(root, kb, kpu, annotationUtil, textSource);
		} catch (IOException ioe) {
			throw new KnowtatorXMLException(ioe);
		} catch (JDOMException jde) {
			throw new KnowtatorXMLException(jde);
		} catch (TextSourceAccessException tsae) {
			throw new KnowtatorXMLException(tsae);
		}
	}

	/**
	 * This method iterates through all of the class mention elements and checks
	 * to see that a corresponding 'knowtator class mention' instance exists in
	 * the Protege project. If not, then they are created. The mentioned class
	 * is also set for the Protege instance.
	 * 
	 * @param root
	 * @param kb
	 * @param kpu
	 */

	private static void readClsMentions(Element root, KnowledgeBase kb, KnowtatorProjectUtil kpu, boolean createClass)
			throws KnowtatorXMLException {
		List classMentionElements = root.getChildren(XMLConstants.CLASS_MENTION_ELEMENT_NAME);
		Iterator classMentionElementsItr = classMentionElements.iterator();

		while (classMentionElementsItr.hasNext()) {
			Element classMentionElement = (Element) classMentionElementsItr.next();

			String classMentionID = classMentionElement.getAttribute(XMLConstants.ID_ATTRIBUTE_NAME).getValue();
			Instance classMentionInstance = kb.getInstance(classMentionID);
			if (classMentionInstance == null) // create class mention if it does
											  // not already exist from the
											  // class mention id
			{
				classMentionInstance = kb.createSimpleInstance(new FrameID(classMentionID), CollectionUtilities
						.createCollection(kpu.getClassMentionCls()), true);
			}

			String mentionedClassID = classMentionElement.getChild(XMLConstants.MENTION_CLASS_ELEMENT_NAME)
					.getAttribute(XMLConstants.ID_ATTRIBUTE_NAME).getValue();
			
			Cls mentionedCls = kb.getCls(mentionedClassID);
			if (mentionedCls == null) {
				
				if( createClass ) {
					mentionedCls = kb.createCls( mentionedClassID, kb.getRootClses() );					
					mentionedCls.setDocumentation( "Created automatically from XML import" );
				}
				
				if( mentionedCls == null ) {
					throw new KnowtatorXMLException("There is no class defined in Protege with the name '"
						+ mentionedClassID + "'.");
				}
			}
			
			classMentionInstance.setOwnSlotValue(kpu.getMentionClassSlot(), mentionedCls);
		}
	}

	private static void readInstanceMentions(Element root, KnowledgeBase kb, KnowtatorProjectUtil kpu)
			throws KnowtatorXMLException {
		List instanceMentionElements = root.getChildren(XMLConstants.INSTANCE_MENTION_ELEMENT_NAME);
		Iterator instanceMentionElementsItr = instanceMentionElements.iterator();

		while (instanceMentionElementsItr.hasNext()) { 
			Element instanceMentionElement = (Element) instanceMentionElementsItr.next();

			// create instance mention if it does not already exist from the
			// class mention id
			String instanceMentionID = instanceMentionElement.getAttribute(XMLConstants.ID_ATTRIBUTE_NAME).getValue();
			Instance instanceMentionInstance = kb.getInstance(instanceMentionID);
			if (instanceMentionInstance == null) {
				instanceMentionInstance = kb.createSimpleInstance(new FrameID(instanceMentionID), CollectionUtilities
						.createCollection(kpu.getInstanceMentionCls()), true);
			}

			// set the mentioned instance of the instance mention
			String mentionedInstanceID = instanceMentionElement.getChild(XMLConstants.MENTION_INSTANCE_ELEMENT_NAME)
					.getAttribute(XMLConstants.ID_ATTRIBUTE_NAME).getValue();
			Instance mentionedInstance = kb.getInstance(mentionedInstanceID);
			if (mentionedInstance == null) {
				throw new KnowtatorXMLException("There is no instance defined in Protege with the name '"
						+ mentionedInstanceID + "'.");
			}
			instanceMentionInstance.setOwnSlotValue(kpu.getMentionClassSlot(), mentionedInstance);
		}
	}

	private static void readSlotMentions(Element root, KnowledgeBase kb, KnowtatorProjectUtil kpu, boolean createSlot)
			throws KnowtatorXMLException {

		List<Element> slotMentionElements = getSlotMentionElements(root);

		for (Element slotMentionElement : slotMentionElements) {
			String slotMentionID = slotMentionElement.getAttribute(XMLConstants.ID_ATTRIBUTE_NAME).getValue();
			Instance slotMentionInstance = kb.getInstance(slotMentionID);
			// set the mentioned slot of the slot mention
			String mentionedSlotID = slotMentionElement.getChild(XMLConstants.MENTION_SLOT_ELEMENT_NAME).getAttribute(
					XMLConstants.ID_ATTRIBUTE_NAME).getValue();
			Slot mentionedSlot = kb.getSlot(mentionedSlotID);
			if (mentionedSlot == null) {
				if( createSlot ) {
					
					mentionedSlot = kb.createSlot( mentionedSlotID );					
					
					if (slotMentionElement.getName().equals(XMLConstants.COMPLEX_SLOT_MENTION_ELEMENT_NAME)) {
						mentionedSlot.setValueType( ValueType.INSTANCE );
					} else if (slotMentionElement.getName().equals(XMLConstants.BOOLEAN_SLOT_MENTION_ELEMENT_NAME)) {
						mentionedSlot.setValueType( ValueType.BOOLEAN );
					} else if (slotMentionElement.getName().equals(XMLConstants.FLOAT_SLOT_MENTION_ELEMENT_NAME)) {
						mentionedSlot.setValueType( ValueType.FLOAT );
					} else if (slotMentionElement.getName().equals(XMLConstants.INTEGER_SLOT_MENTION_ELEMENT_NAME)) {
						mentionedSlot.setValueType( ValueType.INTEGER );
					} else if (slotMentionElement.getName().equals(XMLConstants.STRING_SLOT_MENTION_ELEMENT_NAME)) {
						mentionedSlot.setValueType( ValueType.STRING );
					}
				}
				
				if( mentionedSlot == null ) {
					throw new KnowtatorXMLException("There is no slot defined in Protege with the name '" + mentionedSlotID
							+ "'.");
				}
			}

			if (slotMentionElement.getName().equals(XMLConstants.COMPLEX_SLOT_MENTION_ELEMENT_NAME)) {
				if (slotMentionInstance == null)
					slotMentionInstance = kb.createSimpleInstance(new FrameID(slotMentionID), CollectionUtilities
							.createCollection(kpu.getComplexSlotMentionCls()), true);
				else
					slotMentionInstance.setOwnSlotValues(kpu.getMentionSlotValueSlot(), Collections.EMPTY_LIST);
				readComplexSlotMentionValues(slotMentionElement, slotMentionInstance, kb, kpu);
			} else if (slotMentionElement.getName().equals(XMLConstants.BOOLEAN_SLOT_MENTION_ELEMENT_NAME)) {
				if (slotMentionInstance == null)
					slotMentionInstance = kb.createSimpleInstance(new FrameID(slotMentionID), CollectionUtilities
							.createCollection(kpu.getBooleanSlotMentionCls()), true);
				else
					slotMentionInstance.setOwnSlotValues(kpu.getMentionSlotValueSlot(), Collections.EMPTY_LIST);
				readSimpleSlotMentionValues(slotMentionElement, slotMentionInstance, kb, kpu,
						XMLConstants.BOOLEAN_SLOT_MENTION_VALUE_ELEMENT_NAME);
			} else if (slotMentionElement.getName().equals(XMLConstants.FLOAT_SLOT_MENTION_ELEMENT_NAME)) {
				if (slotMentionInstance == null)
					slotMentionInstance = kb.createSimpleInstance(new FrameID(slotMentionID), CollectionUtilities
							.createCollection(kpu.getFloatSlotMentionCls()), true);
				else
					slotMentionInstance.setOwnSlotValues(kpu.getMentionSlotValueSlot(), Collections.EMPTY_LIST);
				readSimpleSlotMentionValues(slotMentionElement, slotMentionInstance, kb, kpu,
						XMLConstants.FLOAT_SLOT_MENTION_VALUE_ELEMENT_NAME);
			} else if (slotMentionElement.getName().equals(XMLConstants.INTEGER_SLOT_MENTION_ELEMENT_NAME)) {
				if (slotMentionInstance == null)
					slotMentionInstance = kb.createSimpleInstance(new FrameID(slotMentionID), CollectionUtilities
							.createCollection(kpu.getIntegerSlotMentionCls()), true);
				else
					slotMentionInstance.setOwnSlotValues(kpu.getMentionSlotValueSlot(), Collections.EMPTY_LIST);
				readSimpleSlotMentionValues(slotMentionElement, slotMentionInstance, kb, kpu,
						XMLConstants.INTEGER_SLOT_MENTION_VALUE_ELEMENT_NAME);
			} else if (slotMentionElement.getName().equals(XMLConstants.STRING_SLOT_MENTION_ELEMENT_NAME)) {
				if (slotMentionInstance == null)
					slotMentionInstance = kb.createSimpleInstance(new FrameID(slotMentionID), CollectionUtilities
							.createCollection(kpu.getStringSlotMentionCls()), true);
				else
					slotMentionInstance.setOwnSlotValues(kpu.getMentionSlotValueSlot(), Collections.EMPTY_LIST);
				readSimpleSlotMentionValues(slotMentionElement, slotMentionInstance, kb, kpu,
						XMLConstants.STRING_SLOT_MENTION_VALUE_ELEMENT_NAME);
			}

			slotMentionInstance.setOwnSlotValue(kpu.getMentionSlotSlot(), mentionedSlot);
		}
	}

	private static void readComplexSlotMentionValues(Element slotMentionElement, Instance slotMentionInstance,
			KnowledgeBase kb, KnowtatorProjectUtil kpu) {
		List complexSlotMentionValueElements = slotMentionElement
				.getChildren(XMLConstants.COMPLEX_SLOT_MENTION_VALUE_ELEMENT_NAME);
		Iterator complexSlotMentionValueElementsItr = complexSlotMentionValueElements.iterator();

		while (complexSlotMentionValueElementsItr.hasNext()) {
			Element complexSlotMentionValueElement = (Element) complexSlotMentionValueElementsItr.next();
			String complexSlotMentionValueID = complexSlotMentionValueElement.getAttribute(
					XMLConstants.VALUE_ATTRIBUTE_NAME).getValue();
			Frame complexSlotMentionValue = kb.getFrame(complexSlotMentionValueID);
			slotMentionInstance.addOwnSlotValue(kpu.getMentionSlotValueSlot(), complexSlotMentionValue);
		}
	}

	private static void readSimpleSlotMentionValues(Element slotMentionElement, Instance slotMentionInstance,
			KnowledgeBase kb, KnowtatorProjectUtil kpu, String elementName) {

		List simpleSlotMentionValueElements = slotMentionElement.getChildren(elementName);
		Iterator simpleSlotMentionValueElementsItr = simpleSlotMentionValueElements.iterator();

		while (simpleSlotMentionValueElementsItr.hasNext()) {
			Element simpleSlotMentionValueElement = (Element) simpleSlotMentionValueElementsItr.next();
			Attribute simpleSlotMentionValueAttribute = simpleSlotMentionValueElement
					.getAttribute(XMLConstants.VALUE_ATTRIBUTE_NAME);
			Object slotMentionValue = null;
			try {
				if (elementName.equals(XMLConstants.BOOLEAN_SLOT_MENTION_VALUE_ELEMENT_NAME)) {
					slotMentionValue = new Boolean(simpleSlotMentionValueAttribute.getBooleanValue());
				} else if (elementName.equals(XMLConstants.INTEGER_SLOT_MENTION_VALUE_ELEMENT_NAME)) {
					slotMentionValue = new Integer(simpleSlotMentionValueAttribute.getIntValue());
				} else if (elementName.equals(XMLConstants.STRING_SLOT_MENTION_VALUE_ELEMENT_NAME)) {
					slotMentionValue = simpleSlotMentionValueAttribute.getValue();
				} else if (elementName.equals(XMLConstants.FLOAT_SLOT_MENTION_VALUE_ELEMENT_NAME)) {
					slotMentionValue = new Float(simpleSlotMentionValueAttribute.getFloatValue());
				}
			} catch (DataConversionException dce) {
				slotMentionValue = simpleSlotMentionValueAttribute.getValue();
			}

			slotMentionInstance.addOwnSlotValue(kpu.getMentionSlotValueSlot(), slotMentionValue);
		}
	}

	/**
	 * This method goes back through classMention or instanceMention elements
	 * and adds the slot mentions to the class mentions. In contrast the method
	 * readSlotMentions, reads in all the slotMentions so that they are in the
	 * knowledge base when this method is run.
	 * 
	 * @param root
	 * @param kb
	 * @param kpu
	 * @param elementName
	 */
	private static void readMentionSlots(Element root, KnowledgeBase kb, KnowtatorProjectUtil kpu, String elementName) {
		List mentionElements = root.getChildren(elementName);
		Iterator mentionElementsItr = mentionElements.iterator();

		while (mentionElementsItr.hasNext()) {
			Element mentionElement = (Element) mentionElementsItr.next();
			Attribute mentionIDAttribute = mentionElement.getAttribute(XMLConstants.ID_ATTRIBUTE_NAME);
			String mentionID = mentionIDAttribute.getValue();
			Instance mentionInstance = kb.getInstance(mentionID);

			List hasSlotMentionElements = mentionElement.getChildren(XMLConstants.HAS_SLOT_MENTION_ELEMENT_NAME);
			Iterator hasSlotMentionElementsItr = hasSlotMentionElements.iterator();
			while (hasSlotMentionElementsItr.hasNext()) {
				Element hasSlotMentionElement = (Element) hasSlotMentionElementsItr.next();
				Attribute slotMentionIDAttribute = hasSlotMentionElement.getAttribute(XMLConstants.ID_ATTRIBUTE_NAME);
				String slotMentionID = slotMentionIDAttribute.getValue();
				Instance slotMention = kb.getInstance(slotMentionID);
				mentionInstance.addOwnSlotValue(kpu.getSlotMentionSlot(), slotMention);
			}
		}
	}

	private static void readAnnotations(Element root, KnowledgeBase kb, KnowtatorProjectUtil kpu,
			AnnotationUtil annotationUtil, SimpleInstance textSourceInstance) throws KnowtatorXMLException {
		List annotationElements = root.getChildren(XMLConstants.ANNOTATION_ELEMENT_NAME);
		Iterator annotationElementsItr = annotationElements.iterator();

		while (annotationElementsItr.hasNext()) {
			Element annotationElement = (Element) annotationElementsItr.next();

			SimpleInstance mentionInstance = null;
			SimpleInstance annotatorInstance = null;
			List<Span> spans = new ArrayList<Span>();
			String spannedText = null;
			String creationDate = null;

			Element mentionElement = annotationElement.getChild(XMLConstants.MENTION_ELEMENT_NAME);
			if (mentionElement != null) {
				String mentionID = mentionElement.getAttribute(XMLConstants.ID_ATTRIBUTE_NAME).getValue();
				mentionInstance = kb.getSimpleInstance(mentionID);
			}

			Element annotatorElement = annotationElement.getChild(XMLConstants.ANNOTATOR_ELEMENT_NAME);
			if (annotatorElement != null) {
				String annotatorID = annotatorElement.getAttribute(XMLConstants.ID_ATTRIBUTE_NAME).getValue();
				annotatorInstance = kb.getSimpleInstance(annotatorID);
			}

			List spanElements = annotationElement.getChildren(XMLConstants.SPAN_ELEMENT_NAME);
			Iterator spanElementsItr = spanElements.iterator();

			try {
				while (spanElementsItr.hasNext()) {
					Element spanElement = (Element) spanElementsItr.next();
					int spanStart = spanElement.getAttribute(XMLConstants.SPAN_START_ATTRIBUTE_NAME).getIntValue();
					int spanEnd = spanElement.getAttribute(XMLConstants.SPAN_END_ATTRIBUTE_NAME).getIntValue();
					Span span = new Span(spanStart, spanEnd);
					spans.add(span);
				}
			} catch (DataConversionException dce) {
				throw new KnowtatorXMLException(dce);
			}

			Element spannedTextElement = annotationElement.getChild(XMLConstants.SPANNED_TEXT_ELEMENT_NAME);
			if (spannedTextElement != null)
				spannedText = spannedTextElement.getText();
			
			Element creationDateElement = annotationElement.getChild(XMLConstants.CREATION_DATE_ELEMENT_NAME);
			if (creationDateElement != null) {
				creationDate = creationDateElement.getText();
			}

			try {
				SimpleInstance annotation = annotationUtil.createAnnotation(mentionInstance, annotatorInstance, spans,
						spannedText, textSourceInstance, null, creationDate);
				Element commentElement = annotationElement.getChild(XMLConstants.COMMENT_ELEMENT_NAME);
				if (commentElement != null) {
					String comment = commentElement.getText();
					annotationUtil.setComment(annotation, comment);
				}
			} catch (TextSourceAccessException tsae) {
				throw new KnowtatorXMLException(tsae);
			}
		}
	}

	private static List<Element> getSlotMentionElements(Element root) {
		List<Element> slotMentionElements = new ArrayList<Element>();

		_addSlotMentionElements(root, XMLConstants.COMPLEX_SLOT_MENTION_ELEMENT_NAME, slotMentionElements);
		_addSlotMentionElements(root, XMLConstants.BOOLEAN_SLOT_MENTION_ELEMENT_NAME, slotMentionElements);
		_addSlotMentionElements(root, XMLConstants.FLOAT_SLOT_MENTION_ELEMENT_NAME, slotMentionElements);
		_addSlotMentionElements(root, XMLConstants.INTEGER_SLOT_MENTION_ELEMENT_NAME, slotMentionElements);
		_addSlotMentionElements(root, XMLConstants.STRING_SLOT_MENTION_ELEMENT_NAME, slotMentionElements);
		
		return slotMentionElements;
	}

	private static void _addSlotMentionElements(Element root, String elementName, List<Element> slotMentionElements) {
		List elements = root.getChildren(elementName);
		for (int i = 0; i < elements.size(); i++) {
			slotMentionElements.add((Element) elements.get(i));
		}
	}

}
