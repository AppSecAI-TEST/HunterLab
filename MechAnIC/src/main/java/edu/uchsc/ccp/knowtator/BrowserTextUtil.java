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

/**
 * Changes:
 *  8/18/2005   pvo   added license
 *  8/18/2005   pvo   added method getMentionAnnotationText to remove some duplicate code and 
 *                    to support next change item
 *  8/18/2005   pvo   if a browser pattern element contains a special token for the annotation text, then
 *                    this is added to the browserText of the annotation.  When editing the
 *                    the multi-slot browser pattern in Protege, one can add the string "[text]" in 
 *                    one of the text fields to have the annotated text of the annotation inserted.
 *                    This makes the integration of the annotation text and browser patterns possible.  
 *  8/18/2005   pvo   addded method comparator for sorting annotations by their browsertext
 */
package edu.uchsc.ccp.knowtator;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.stanford.smi.protege.model.BrowserSlotPattern;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;

/**
 * 
 * @author Philip V. Ogren
 */
public class BrowserTextUtil {

	public static final String ANNOTATION_TEXT = "[text]";

	AnnotationUtil annotationUtil;

	MentionUtil mentionUtil;

	KnowtatorProjectUtil kpu;

	/** Creates a new instance of BrowserText */
	public BrowserTextUtil(AnnotationUtil annotationUtil, MentionUtil mentionUtil, KnowtatorProjectUtil kpu) {
		this.annotationUtil = annotationUtil;
		this.mentionUtil = mentionUtil;
		this.kpu = kpu;
	}

	public String getBrowserText(SimpleInstance instance, int maxLength) {
		String browserText = getBrowserText(instance);
		if (browserText == null)
			return "";
		if (browserText.length() > maxLength) {
			browserText = browserText.substring(0, maxLength) + "...";
		}
		return browserText.trim();
	}

	/**
	 * The BrowserSlotPattern in the Protege code does not work very well with
	 * annotations and their mentions. This code generates browser text that
	 * tries to capture the BrowserSlotPatterns of the Instances and Clses that
	 * are being mentioned/annotated.
	 * 
	 * If an annotation is passed in, then the instance.getBrowserText() will be
	 * returned if there is any. This will typically be the text found in
	 * knowtator_annotation_text. If there is no text returned, then text
	 * corresponding to the annotated mention will be returned via a recursive
	 * call (see next paragraph).
	 * 
	 * If a mention is passed in, then a browser text corresponding to the
	 * BrowserSlotPattern of the mentioned cls will be constructed.
	 * 
	 * Much of this code was copied and modified from
	 * edu.stanford.smi.protege.model.BrowserSlotPattern.java
	 */
	public String getBrowserText(SimpleInstance instance) {
		Set<SimpleInstance> visitedInstances = new HashSet<SimpleInstance>();
		return _getBrowserText(instance, visitedInstances).trim();
	}

	private String _getBrowserText(SimpleInstance instance, Set<SimpleInstance> visitedInstances) {
		if (instance == null)
			return "";

		if (visitedInstances.contains(instance))
			return "";

		visitedInstances.add(instance);

		// if the instance is an annotation then we will simply return the
		// instance.getBrowserText()
		// unless there isn't any in which case we will recursively call
		// getBrowserText on the mention
		// of the annotation (if possible).
		if (annotationUtil.isAnnotation(instance)) {
			// String instanceBrowserText = instance.getBrowserText();
			// if(!instanceBrowserText.equals(instance.getName()))
			// return instanceBrowserText;

			String instanceName = instance.getName();

			SimpleInstance mention = annotationUtil.getMention(instance);
			if (mention == null) {
				return instanceName;
			} else {
				String browserText = _getBrowserText(mention, visitedInstances);
				if (browserText == null || browserText.trim().length() == 0) {
					return instanceName;
				} else if (browserText.equals(mention.getName())) {
					return instanceName;
				} else
					return browserText;
			}
		} else if (mentionUtil.isMention(instance)) {
			Cls mentionCls = mentionUtil.getMentionCls(instance);
			if (mentionCls != null) {
				BrowserSlotPattern slotPattern = mentionCls.getBrowserSlotPattern();
				if (slotPattern != null) {
					StringBuffer browserTextBuffer = new StringBuffer();
					List patternElements = slotPattern.getElements();
					for (int i = 0; i < patternElements.size(); i++) {
						Object patternElement = patternElements.get(i);
						if (patternElement instanceof Slot) {
							browserTextBuffer.append(getText((Slot) patternElement, instance, visitedInstances));
						} else {
							String patternString = patternElement.toString();
							if (patternString.contains(ANNOTATION_TEXT)) {
								String annotationText = getMentionAnnotationText(instance);
								if (annotationText != null) {
									patternString = patternString.replace(ANNOTATION_TEXT, annotationText);
									patternElement = patternString;
								}
							}

							browserTextBuffer.append(patternElement);
						}
					}
					if (browserTextBuffer.length() > 0) {
						return browserTextBuffer.toString();
					}
				} else if (mentionUtil.isClassMention(instance) || mentionUtil.isInstanceMention(instance)) {
					String annotationText = getMentionAnnotationText(instance);

					if (annotationText != null)
						return annotationText;
				} else {
					try {
						if (mentionUtil.isClassMention(instance)) {
							return mentionCls.getBrowserText();
						} else if (mentionUtil.isInstanceMention(instance)) {
							SimpleInstance mentionInstance = mentionUtil.getMentionInstance(instance);
							if (mentionInstance != null)
								return mentionInstance.getBrowserText();
						} else if (mentionUtil.isSlotMention(instance)) {
							Slot mentionSlot = mentionUtil.getSlotMentionSlot(instance);
							if (mentionSlot != null)
								return mentionSlot.getBrowserText();
						}
					} catch (NullPointerException npe) {
					}
				}
			}
			return instance.getBrowserText();
		}
		return instance.getBrowserText();
	}

	/**
	 * Much of this code was copied and modified from
	 * edu.stanford.smi.protege.model.BrowserSlotPattern.java
	 */
	private String getText(Slot slot, SimpleInstance mention, Set<SimpleInstance> visitedInstances) {
		String text;

		SimpleInstance slotMention = mentionUtil.getSlotMention(mention, slot);
		if (slotMention == null)
			return "";

		Collection slotMentionValues = slotMention.getOwnSlotValues(kpu.mentionSlotValueSlot);
		if (slotMentionValues == null)
			return "";
		if (slotMentionValues.size() > 1) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("{");
			boolean isFirst = true;
			Iterator i = slotMentionValues.iterator();
			while (i.hasNext()) {
				if (isFirst) {
					isFirst = false;
				} else {
					buffer.append(", ");
				}
				Object o = i.next();
				buffer.append(getText(o, mention, visitedInstances));
			}
			buffer.append("}");
			text = buffer.toString();
		} else {
			Object o = CollectionUtilities.getFirstItem(slotMentionValues);
			text = getText(o, mention, visitedInstances);
		}
		return text;
	}

	/**
	 * Much of this code was copied and modified from
	 * edu.stanford.smi.protege.model.BrowserSlotPattern.java
	 */
	private String getText(Object slotValue, Instance instance, Set<SimpleInstance> visitedInstances) {
		if (slotValue == null) {
			return "";
		}
		if (slotValue instanceof Frame) {
			if (slotValue.equals(instance)) {
				return "<recursive call>";
			} else if (slotValue instanceof SimpleInstance) {
				SimpleInstance simpleInstance = (SimpleInstance) slotValue;
				return _getBrowserText(simpleInstance, visitedInstances);
			} else {
				return ((Frame) slotValue).getBrowserText();
			}
		} else {
			return slotValue.toString();
		}
	}

	private String getMentionAnnotationText(SimpleInstance instance) {
		if (mentionUtil.isMention(instance)) {
			SimpleInstance mentionAnnotation = mentionUtil.getMentionAnnotation(instance);
			if (mentionAnnotation != null) {
				String annotationText = mentionAnnotation.getBrowserText();
				if (annotationText.equals(mentionAnnotation.getName()))
					return "";
				else {
					return annotationText;
				}

			}
		}
		return null;
	}

	public Comparator<SimpleInstance> comparator() {
		return new Comparator<SimpleInstance>() {
			public int compare(SimpleInstance annotation1, SimpleInstance annotation2) {
				String browserText1 = getBrowserText(annotation1).toLowerCase();
				String browserText2 = getBrowserText(annotation2).toLowerCase();
				return browserText1.compareTo(browserText2);
			}
		};
	}
}
