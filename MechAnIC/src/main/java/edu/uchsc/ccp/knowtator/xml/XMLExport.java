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

package edu.uchsc.ccp.knowtator.xml;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.ui.DisplayUtilities;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.FilterUtil;
import edu.uchsc.ccp.knowtator.InvalidSpanException;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.MentionUtil;
import edu.uchsc.ccp.knowtator.Span;
import edu.uchsc.ccp.knowtator.TextSourceUtil;

public class XMLExport {
	public static final String XML_EXPORT_DIRECTORY = "XML_EXPORT_FILE";

	private static File getRecentXMLExportDirectory(Project project) {
		String path = (String) project.getClientInformation(XML_EXPORT_DIRECTORY);
		if (path == null)
			return null;

		File xmlExportDirectory = new File(path);
		if (xmlExportDirectory.exists() && xmlExportDirectory.isDirectory()) {
			return xmlExportDirectory;
		}
		return null;
	}

	public static void writeToXML(Component parent, KnowledgeBase kb, KnowtatorProjectUtil kpu,
			TextSourceUtil textSourceUtil, AnnotationUtil annotationUtil, MentionUtil mentionUtil,
			FilterUtil filterUtil, Project project) {

		int option = JOptionPane.showConfirmDialog(parent, "The following dialogs allow you to export \n"
				+ "a set of Knowtator annotations to XML.", "XML Export", JOptionPane.OK_CANCEL_OPTION);
		if (option != JOptionPane.OK_OPTION)
			return;

		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Please choose a directory to write xml files to.");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		File recentXMLExportDirectory = getRecentXMLExportDirectory(project);
		if (recentXMLExportDirectory != null) {
			chooser.setCurrentDirectory(recentXMLExportDirectory.getParentFile());
		}

		int returnVal = chooser.showOpenDialog(parent);
		if (returnVal != JFileChooser.APPROVE_OPTION)
			return;

		File directory = chooser.getSelectedFile();

		project.setClientInformation(XML_EXPORT_DIRECTORY, directory.getPath());

		SimpleInstance filter = (SimpleInstance) DisplayUtilities.pickInstance(parent, CollectionUtilities
				.createCollection(kpu.getFilterCls()), "Select filter for annotations to be exported.");
		if (filter == null)
			return;

		Collection textSources = DisplayUtilities.pickInstances(parent, kb, CollectionUtilities.createCollection(kpu
				.getTextSourceCls()), "Select text sources from which to choose annotations for export.");

		if (textSources == null || textSources.size() == 0)
			return;

		Iterator textSourcesItr = textSources.iterator();
		while (textSourcesItr.hasNext()) {
			SimpleInstance textSource = (SimpleInstance) textSourcesItr.next();
			try {
				writeToXML(textSource, filter, directory, annotationUtil, filterUtil, mentionUtil);
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(parent, ioe, "Exception thrown while opening file",
						JOptionPane.ERROR_MESSAGE);
			} catch (Exception e) {
				JOptionPane.showMessageDialog(parent, e, "Exception thrown while exporting annotations to XML",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		JOptionPane.showMessageDialog(parent, "XML export complete", "XML export complete",
				JOptionPane.INFORMATION_MESSAGE);
	}

	public static void writeToXML(SimpleInstance textSource, SimpleInstance filter, File outputDirectory,
			AnnotationUtil annotationUtil, FilterUtil filterUtil, MentionUtil mentionUtil) throws IOException {
		Collection<SimpleInstance> annotations = new ArrayList<SimpleInstance>(annotationUtil
				.getAnnotations(textSource));
		annotations = filterUtil.filterAnnotations(annotations, filter);

		Document xmlDocument = new Document();
		buildXMLDocument(xmlDocument, annotations, textSource.getName(), annotationUtil, mentionUtil);

		XMLOutputter xmlOut = new XMLOutputter(Format.getPrettyFormat());
		xmlOut.output(xmlDocument, new FileOutputStream(new File(outputDirectory, textSource.getName()
				+ ".knowtator.xml")));
	}

	public static void buildXMLDocument(Document xmlDocument, Collection<SimpleInstance> annotations,
			String textSourceName, AnnotationUtil annotationUtil, MentionUtil mentionUtil) {
		Element annotationsElement = new Element(XMLConstants.ANNOTATIONS_ELEMENT_NAME);
		xmlDocument.addContent(annotationsElement);

		annotationsElement.setAttribute(XMLConstants.TEXT_SOURCE_ATTRIBUTE_NAME, textSourceName);

		Set<SimpleInstance> mentions = new HashSet<SimpleInstance>();

		for (SimpleInstance annotation : annotations) {
			Element annotationElement = new Element(XMLConstants.ANNOTATION_ELEMENT_NAME);

			SimpleInstance mention = annotationUtil.getMention(annotation);
			if (mention != null) {
				mentions.add(mention);
				String mentionName = mention.getName();
				Element mentionElement = new Element(XMLConstants.MENTION_ELEMENT_NAME);
				mentionElement.setAttribute(XMLConstants.ID_ATTRIBUTE_NAME, mentionName);
				annotationElement.addContent(mentionElement);
			}

			SimpleInstance annotator = annotationUtil.getAnnotator(annotation);
			if (annotator != null) {
				String annotatorName = annotator.getName();
				String annotatorText = annotator.getBrowserText();
				Element annotatorElement = new Element(XMLConstants.ANNOTATOR_ELEMENT_NAME);
				annotatorElement.setAttribute(XMLConstants.ID_ATTRIBUTE_NAME, annotatorName);
				annotatorElement.addContent(annotatorText);
				annotationElement.addContent(annotatorElement);
			}

			try {
				java.util.List<Span> spans = annotationUtil.getSpans(annotation);
				for (Span span : spans) {
					Element spanElement = new Element(XMLConstants.SPAN_ELEMENT_NAME);
					spanElement.setAttribute(XMLConstants.SPAN_START_ATTRIBUTE_NAME, "" + span.getStart());
					spanElement.setAttribute(XMLConstants.SPAN_END_ATTRIBUTE_NAME, "" + span.getEnd());
					annotationElement.addContent(spanElement);
				}
			} catch (InvalidSpanException ise) {
				ise.printStackTrace(); // lame! this is where I've finally
				// admitted to myself that
				// InvalidSpanException is stupid and
				// should be an error....
				// throw some appropriate exception here.
			}
			String spannedText = annotationUtil.getText(annotation);
			if (spannedText != null) {
				Element spannedTextElement = new Element(XMLConstants.SPANNED_TEXT_ELEMENT_NAME);
				spannedTextElement.addContent(spannedText);
				annotationElement.addContent(spannedTextElement);
			}
			String comment = annotationUtil.getComment(annotation);
			if (comment != null) {
				Element commentElement = new Element(XMLConstants.COMMENT_ELEMENT_NAME);
				commentElement.addContent(comment);
				annotationElement.addContent(commentElement);
			}

			String creationDate = annotationUtil.getCreationDate(annotation);
			if (creationDate != null) {
				Element creationDateElement = new Element(XMLConstants.CREATION_DATE_ELEMENT_NAME);
				creationDateElement.addContent(creationDate);
				annotationElement.addContent(creationDateElement);
			}

			annotationsElement.addContent(annotationElement);
		}

		Set<SimpleInstance> writtenMentions = new HashSet<SimpleInstance>();
		for (SimpleInstance mention : mentions) {
			buildMention(annotationsElement, mention, writtenMentions, mentionUtil);
		}

	}

	private static void buildMention(Element annotationsElement, SimpleInstance mention,
			Set<SimpleInstance> writtenMentions, MentionUtil mentionUtil) {
		if (!writtenMentions.contains(mention)) {
			writtenMentions.add(mention);
			if (mentionUtil.isClassMention(mention)) {
				Cls mentionCls = mentionUtil.getMentionCls(mention);
				Element classMentionElement = new Element(XMLConstants.CLASS_MENTION_ELEMENT_NAME);
				annotationsElement.addContent(classMentionElement);
				classMentionElement.setAttribute(XMLConstants.ID_ATTRIBUTE_NAME, mention.getName());
				Element mentionClassElement = new Element(XMLConstants.MENTION_CLASS_ELEMENT_NAME);
				if (mentionCls != null) {
					mentionClassElement.setAttribute(XMLConstants.ID_ATTRIBUTE_NAME, mentionCls.getName());
					mentionClassElement.addContent(mentionCls.getBrowserText());
				} else
					mentionClassElement.setAttribute(XMLConstants.ID_ATTRIBUTE_NAME, "NULL CLASS");

				classMentionElement.addContent(mentionClassElement);

				Collection<SimpleInstance> slotMentions = mentionUtil.getSlotMentions(mention);
				for (SimpleInstance slotMention : slotMentions) {
					// it is possible that the mention has slot mentions that do
					// not have a value (i.e. a slot mention is there, the slot
					// is filled in but there is not actually a value for the
					// slot.
					// we only want to write slot mentions that actually have
					// values.
					if (mentionUtil.hasSlotValue(slotMention)) {
						Element hasSlotMentionElement = new Element(XMLConstants.HAS_SLOT_MENTION_ELEMENT_NAME);
						hasSlotMentionElement.setAttribute(XMLConstants.ID_ATTRIBUTE_NAME, slotMention.getName());
						classMentionElement.addContent(hasSlotMentionElement);
					}
				}
				for (SimpleInstance slotMention : slotMentions) {
					if (mentionUtil.hasSlotValue(slotMention))
						buildMention(annotationsElement, slotMention, writtenMentions, mentionUtil);
				}
			} else if (mentionUtil.isInstanceMention(mention)) {
				Instance mentionInstance = mentionUtil.getMentionInstance(mention);
				Element instanceMentionElement = new Element(XMLConstants.INSTANCE_MENTION_ELEMENT_NAME);
				annotationsElement.addContent(instanceMentionElement);
				instanceMentionElement.setAttribute(XMLConstants.ID_ATTRIBUTE_NAME, mention.getName());
				Element mentionInstanceElement = new Element(XMLConstants.MENTION_INSTANCE_ELEMENT_NAME);
				if (mentionInstance != null) {
					mentionInstanceElement.setAttribute(XMLConstants.ID_ATTRIBUTE_NAME, mentionInstance.getName());
					mentionInstanceElement.addContent(mentionInstance.getBrowserText());
				}

				instanceMentionElement.addContent(mentionInstanceElement);

				Collection<SimpleInstance> slotMentions = mentionUtil.getSlotMentions(mention);
				for (SimpleInstance slotMention : slotMentions) {
					// we only want to write slot mentions that actually have
					// values.
					if (mentionUtil.hasSlotValue(slotMention)) {
						Element hasSlotMentionElement = new Element(XMLConstants.HAS_SLOT_MENTION_ELEMENT_NAME);
						hasSlotMentionElement.setAttribute(XMLConstants.ID_ATTRIBUTE_NAME, slotMention.getName());
						instanceMentionElement.addContent(hasSlotMentionElement);

					}
				}
				for (SimpleInstance slotMention : slotMentions) {
					if (mentionUtil.hasSlotValue(slotMention))
						buildMention(annotationsElement, slotMention, writtenMentions, mentionUtil);
				}
			} else if (mentionUtil.isSlotMention(mention)) {
				Slot slot = mentionUtil.getSlotMentionSlot(mention);

				Element slotMentionElement = null;
				if (mentionUtil.isComplexSlotMention(mention)) {
					slotMentionElement = new Element(XMLConstants.COMPLEX_SLOT_MENTION_ELEMENT_NAME);
				} else if (mentionUtil.isBooleanSlotMention(mention)) {
					slotMentionElement = new Element(XMLConstants.BOOLEAN_SLOT_MENTION_ELEMENT_NAME);
				} else if (mentionUtil.isFloatSlotMention(mention)) {
					slotMentionElement = new Element(XMLConstants.FLOAT_SLOT_MENTION_ELEMENT_NAME);
				} else if (mentionUtil.isIntegerSlotMention(mention)) {
					slotMentionElement = new Element(XMLConstants.INTEGER_SLOT_MENTION_ELEMENT_NAME);
				} else if (mentionUtil.isStringSlotMention(mention)) {
					slotMentionElement = new Element(XMLConstants.STRING_SLOT_MENTION_ELEMENT_NAME);
				}

				annotationsElement.addContent(slotMentionElement);
				slotMentionElement.setAttribute(XMLConstants.ID_ATTRIBUTE_NAME, mention.getName());
				Element mentionSlotElement = new Element(XMLConstants.MENTION_SLOT_ELEMENT_NAME);
				if (slot != null)
					mentionSlotElement.setAttribute(XMLConstants.ID_ATTRIBUTE_NAME, slot.getName());
				else
					mentionSlotElement.setAttribute(XMLConstants.ID_ATTRIBUTE_NAME, "NULL SLOT");

				slotMentionElement.addContent(mentionSlotElement);

				Collection<Object> slotValues = mentionUtil.getSlotMentionValues(mention);
				ArrayList<SimpleInstance> complexSlotValues = new ArrayList<SimpleInstance>();
				if (slotValues != null && slotValues.size() > 0) {
					if (mentionUtil.isComplexSlotMention(mention)) {
						for (Object slotValue : slotValues) {
							SimpleInstance slotValueInstance = (SimpleInstance) slotValue;
							Element complexSlotMentionValueElement = new Element(
									XMLConstants.COMPLEX_SLOT_MENTION_VALUE_ELEMENT_NAME);
							complexSlotMentionValueElement.setAttribute(XMLConstants.VALUE_ATTRIBUTE_NAME,
									slotValueInstance.getName());
							slotMentionElement.addContent(complexSlotMentionValueElement);
							complexSlotValues.add(slotValueInstance);
						}
					} else {
						for (Object slotValue : slotValues) {
							Element simpleSlotMentionValueElement = null;
							if (mentionUtil.isIntegerSlotMention(mention))
								simpleSlotMentionValueElement = new Element(
										XMLConstants.INTEGER_SLOT_MENTION_VALUE_ELEMENT_NAME);
							else if (mentionUtil.isBooleanSlotMention(mention))
								simpleSlotMentionValueElement = new Element(
										XMLConstants.BOOLEAN_SLOT_MENTION_VALUE_ELEMENT_NAME);
							else if (mentionUtil.isStringSlotMention(mention))
								simpleSlotMentionValueElement = new Element(
										XMLConstants.STRING_SLOT_MENTION_VALUE_ELEMENT_NAME);
							else if (mentionUtil.isFloatSlotMention(mention))
								simpleSlotMentionValueElement = new Element(
										XMLConstants.FLOAT_SLOT_MENTION_VALUE_ELEMENT_NAME);
							if (simpleSlotMentionValueElement != null) {
								simpleSlotMentionValueElement.setAttribute(XMLConstants.VALUE_ATTRIBUTE_NAME, ""
										+ slotValue);
								slotMentionElement.addContent(simpleSlotMentionValueElement);
							}
						}
					}
				}
				for (SimpleInstance slotValueInstance : complexSlotValues) {
					buildMention(annotationsElement, slotValueInstance, writtenMentions, mentionUtil);
				}
			}
		}
	}
}
