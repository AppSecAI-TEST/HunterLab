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

public class XMLConstants {
	public static final String ANNOTATIONS_ELEMENT_NAME = "annotations";

	public static final String TEXT_SOURCE_ATTRIBUTE_NAME = "textSource";

	public static final String ID_ATTRIBUTE_NAME = "id";

	public static final String VALUE_ATTRIBUTE_NAME = "value";

	public static final String ANNOTATION_ELEMENT_NAME = "annotation";

	public static final String MENTION_ELEMENT_NAME = "mention";

	public static final String ANNOTATOR_ELEMENT_NAME = "annotator";

	public static final String SPAN_ELEMENT_NAME = "span";

	public static final String SPAN_START_ATTRIBUTE_NAME = "start";

	public static final String SPAN_END_ATTRIBUTE_NAME = "end";

	public static final String SPANNED_TEXT_ELEMENT_NAME = "spannedText";

	public static final String COMMENT_ELEMENT_NAME = "annotationComment";

	/** 
	 * The name of the element that represents the annotation creation date 
	 */
	public static final String CREATION_DATE_ELEMENT_NAME = "creationDate";

	/**
	 * The name of the element that represents a class mention.
	 */
	public static final String CLASS_MENTION_ELEMENT_NAME = "classMention";

	/**
	 * The name of the element that represents the mentioned class of a class
	 * mention.
	 */
	public static final String MENTION_CLASS_ELEMENT_NAME = "mentionClass";

	public static final String HAS_SLOT_MENTION_ELEMENT_NAME = "hasSlotMention";

	/**
	 * The name of the element that represents an instance mention.
	 */
	public static final String INSTANCE_MENTION_ELEMENT_NAME = "instanceMention";

	/**
	 * The name of the element that represents the mentioned instance of an
	 * instance mention.
	 */
	public static final String MENTION_INSTANCE_ELEMENT_NAME = "mentionInstance";

	/**
	 * The name of the element that represents a slot mention.
	 */
	public static final String COMPLEX_SLOT_MENTION_ELEMENT_NAME = "complexSlotMention";

	public static final String INTEGER_SLOT_MENTION_ELEMENT_NAME = "integerSlotMention";

	public static final String BOOLEAN_SLOT_MENTION_ELEMENT_NAME = "booleanSlotMention";

	public static final String FLOAT_SLOT_MENTION_ELEMENT_NAME = "floatSlotMention";

	public static final String STRING_SLOT_MENTION_ELEMENT_NAME = "stringSlotMention";

	/**
	 * The name of the element that represents the mentioned slot of a slot
	 * mention.
	 */
	public static final String MENTION_SLOT_ELEMENT_NAME = "mentionSlot";

	public static final String COMPLEX_SLOT_MENTION_VALUE_ELEMENT_NAME = "complexSlotMentionValue";

	public static final String INTEGER_SLOT_MENTION_VALUE_ELEMENT_NAME = "integerSlotMentionValue";

	public static final String BOOLEAN_SLOT_MENTION_VALUE_ELEMENT_NAME = "booleanSlotMentionValue";

	public static final String FLOAT_SLOT_MENTION_VALUE_ELEMENT_NAME = "floatSlotMentionValue";

	public static final String STRING_SLOT_MENTION_VALUE_ELEMENT_NAME = "stringSlotMentionValue";

}
