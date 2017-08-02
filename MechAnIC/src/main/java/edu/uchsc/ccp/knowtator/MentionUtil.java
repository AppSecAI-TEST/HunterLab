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
 *   Angus Roberts
 *
 */

/**
 * Changes
 * 2/8/2007   ar	fixed recursive infinite loop bug in copyMention
 * 8/11/2005  pvo    added isComplexSlotMention, getComplexMentionSlotValues (2 versions)
 *                   getComplexSlotMentions 
 *                   
 * 11/19/2005 pvo   Added copyMention() method
 * 					added documentation for supporting methods.
 * 					Refactored a few existing methods.  Mostly added code for "null" conditions and javadocs.
 */

package edu.uchsc.ccp.knowtator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.smi.protege.model.*;
import org.apache.log4j.Logger;

import edu.stanford.smi.protege.util.CollectionUtilities;

public class MentionUtil {
	KnowtatorProjectUtil kpu;

	KnowledgeBase kb;

	AnnotationUtil annotationUtil;

	Logger logger = Logger.getLogger(KnowtatorManager.class);

	/** Creates a new instance of MentionUtil */
	public MentionUtil(KnowtatorProjectUtil kpu) {
		this.kpu = kpu;
		this.kb = kpu.kb;
	}

	public void setAnnotationUtil(AnnotationUtil annotationUtil) {
		this.annotationUtil = annotationUtil;
	}

	public boolean equals(SimpleInstance mention1, SimpleInstance mention2, boolean compareAnnotationSpans) {
		return equals( mention1, mention2, compareAnnotationSpans, true );
	}
	
	public boolean equals(SimpleInstance mention1, SimpleInstance mention2, boolean compareAnnotationSpans, boolean compareClass) {
		Set<SimpleInstance> comparedMentions = new HashSet<SimpleInstance>();
		return equals(mention1, mention2, compareAnnotationSpans, comparedMentions, compareClass);
	}
	
	/*
	 * Compares two mentions to see if they are exactly the same including
	 * (recursively) the slot mention values. If the values of two slot mentions
	 * are the same but in a different order, then true is returned.
	 * 
	 * @param compareAnnotationSpans indicate whether the corresponding
	 * annotations of the mentions must have the same spans for the mentions to
	 * be considered equal.
	 * 
	 * @param compareClass When true, the mention classes will be compared, when false, this
	 *         comparison will be skipped. This was needed for consensus mode when highlighting
	 *         the slots.
	 */	
	private boolean equals(SimpleInstance mention1, SimpleInstance mention2, boolean compareAnnotationSpans, Set<SimpleInstance> comparedMentions, boolean compareClass) {
		// return false if either mention (or both) is null.
		if (mention1 == null || mention2 == null)
			return false;
		
		if(comparedMentions.contains(mention1) && comparedMentions.contains(mention2)) {
			return true;
		}
		comparedMentions.add(mention1);
		comparedMentions.add(mention2);
		
		// The two mentions must be the same type
		if (!mention1.getDirectType().equals(mention2.getDirectType()))
			return false;

		if (compareAnnotationSpans) {
			if (!isSlotMention(mention1) && !compareAnnotationSpans(mention1, mention2))
				return false;
		}

		// if both mentions are class mentions
		if (isClassMention(mention1) && isClassMention(mention2)) {
			Cls mentionedCls1 = getMentionCls(mention1);
			Cls mentionedCls2 = getMentionCls(mention2);
			if (mentionedCls1 == null && mentionedCls2 == null)
				return compareSlotMentions(mention1, mention2, compareAnnotationSpans, comparedMentions, compareClass);
			if (mentionedCls1 == null || mentionedCls2 == null)
				return false;
			// return false if the mentioned classes are different
			if (compareClass && !mentionedCls1.equals(mentionedCls2)) {
				return false;
			}
			return compareSlotMentions(mention1, mention2, compareAnnotationSpans, comparedMentions, compareClass);
		}
		if (isInstanceMention(mention1) && isInstanceMention(mention2)) {
			Instance mentionedInstance1 = getMentionInstance(mention1);
			Instance mentionedInstance2 = getMentionInstance(mention2);
			if (mentionedInstance1 == null && mentionedInstance2 == null)
				return compareSlotMentions(mention1, mention2, compareAnnotationSpans, comparedMentions, compareClass);
			if (mentionedInstance1 == null || mentionedInstance2 == null)
				return false;
			// return false if the mentioned classes are different
			if (!mentionedInstance1.equals(mentionedInstance2))
				return false;
			return compareSlotMentions(mention1, mention2, compareAnnotationSpans, comparedMentions, compareClass);
		}
		if (isSlotMention(mention1) && isSlotMention(mention2)) {
			Slot mentionedSlot1 = getSlotMentionSlot(mention1);
			Slot mentionedSlot2 = getSlotMentionSlot(mention2);
			List<Object> slotValues1 = getSlotMentionValues(mention1);
			List<Object> slotValues2 = getSlotMentionValues(mention2);

			if (mentionedSlot1 == null && mentionedSlot2 == null)
				return false;
			if (!mentionedSlot1.equals(mentionedSlot2))
				return false;
			if (slotValues1.size() == 0 && slotValues2.size() == 0)
				return true;
			if (slotValues1.size() != slotValues2.size())
				return false;

			// This is the only "tricky" code in the method. The values of the
			// two slot mentions are not
			// required to be in the same order. The brute force approach was
			// used to simplify the code and
			// because it is assumed that few slot mentions will have many
			// values (usually 1).
			// For each slot value of mention1 it is assumed that there is no
			// match until one is found from
			// the other list of values. If each value is matched then we can
			// return true.
			for (Object slotValue1 : slotValues1) {
				boolean matchedSlotValue = false;
				for (Object slotValue2 : slotValues2) {
					if (compareSlotValues(slotValue1, slotValue2, compareAnnotationSpans, comparedMentions, compareClass)) {
						matchedSlotValue = true;
						break;
					}
				}
				if (!matchedSlotValue)
					return false;
			}
			return true;
		}

		return false;
	}

	private boolean compareSlotValues(Object value1, Object value2, boolean compareAnnotationSpans, Set<SimpleInstance> comparedMentions, boolean compareClass) {
		if (value1 instanceof SimpleInstance && value2 instanceof SimpleInstance) {
			return equals((SimpleInstance) value1, (SimpleInstance) value2, compareAnnotationSpans, comparedMentions, compareClass);
		} else
			return value1.equals(value2);
	}

	private boolean compareSlotMentions(SimpleInstance mention1, SimpleInstance mention2, boolean compareAnnotationSpans, Set<SimpleInstance> comparedMentions, boolean compareClass) {
		List<SimpleInstance> slotMentions1 = getSlotMentions(mention1);
		List<SimpleInstance> slotMentions2 = getSlotMentions(mention2);

		for (SimpleInstance slotMention1 : slotMentions1) {
			Slot mentionedSlot = getSlotMentionSlot(slotMention1);
			List<Object> slotMention1Values = getSlotMentionValues(slotMention1);
			SimpleInstance slotMention2 = getSlotMention(mention2, mentionedSlot);
			if (slotMention2 == null && slotMention1Values.size() == 0)
				continue;
			// return false if the two slot mentions for the slot are not equal
			if (!equals(slotMention1, slotMention2, compareAnnotationSpans, comparedMentions, compareClass))
				return false;
		}
		for (SimpleInstance slotMention2 : slotMentions2) {
			Slot mentionedSlot = getSlotMentionSlot(slotMention2);
			List<Object> slotMention2Values = getSlotMentionValues(slotMention2);
			SimpleInstance slotMention1 = getSlotMention(mention1, mentionedSlot);
			if (slotMention1 == null && slotMention2Values.size() == 0)
				continue;
			// return false if the two slot mentions for the slot are not equal
			if (!equals(slotMention1, slotMention2, compareAnnotationSpans, comparedMentions, compareClass))
				return false;
		}
		return true;
	}

	private boolean compareAnnotationSpans(SimpleInstance mention1, SimpleInstance mention2) {
		SimpleInstance annotation1 = getMentionAnnotation(mention1);
		if (annotation1 == null)
			return false;

		SimpleInstance annotation2 = getMentionAnnotation(mention2);
		if (annotation2 == null)
			return false;

		return annotationUtil.compareSpans(annotation1, annotation2);
	}

	/**
	 * 
	 * @param mention
	 * @param copiesMap
	 * @return TODO simplify the signature.
	 */
	public SimpleInstance copyMention(SimpleInstance mention, Map<SimpleInstance, SimpleInstance> copiesMap) {
		if (copiesMap != null && copiesMap.containsKey(mention))
			return copiesMap.get(mention);

		if (isMention(mention)) {
			SimpleInstance mentionCopy = kb.createSimpleInstance(new FrameID(null), CollectionUtilities
					.createCollection(mention.getDirectType()), true);
			if (copiesMap != null)
				copiesMap.put(mention, mentionCopy);

			// Cls mentionType = mentionCopy.getDirectType();

			if (isClassMention(mention)) {
				Cls mentionCls = getMentionCls(mention);
				if (mentionCls != null)
					setMentionCls(mentionCopy, mentionCls);
			} else if (isInstanceMention(mention)) {
				SimpleInstance mentionInstance = getMentionInstance(mention);
				if (mentionInstance != null)
					setMentionInstance(mentionCopy, mentionInstance);
			}

			if (isClassMention(mention) || isInstanceMention(mention)) {
				List<SimpleInstance> slotMentions = getSlotMentions(mention);
				for (SimpleInstance slotMention : slotMentions) {
					addSlotMention(mentionCopy, copyMention(slotMention, copiesMap));
				}
			} else if (isSlotMention(mention)) {
				Slot mentionSlot = getSlotMentionSlot(mention);
				setMentionSlot(mentionCopy, mentionSlot);
				List<Object> slotValues = getSlotMentionValues(mention);
				for (Object slotValue : slotValues) {
					if (slotValue instanceof SimpleInstance)
						addValueToSlotMention(mentionCopy, copyMention((SimpleInstance) slotValue, copiesMap));
					else
						addValueToSlotMention(mentionCopy, slotValue);
				}
			}
			return mentionCopy;
		}
		return null;
	}

	public SimpleInstance createClassMention(Cls cls) {
		SimpleInstance clsMention = kb.createSimpleInstance(new FrameID(null), CollectionUtilities
				.createCollection(kpu.classMentionCls), true);
		// if(cls != null)
		// {
		clsMention.setOwnSlotValue(kpu.mentionClassSlot, cls);
		// }
		return clsMention;
	}

	public SimpleInstance createInstanceMention(SimpleInstance simpleInstance) {
		SimpleInstance instanceMention = kb.createSimpleInstance(new FrameID(null), CollectionUtilities
				.createCollection(kpu.instanceMentionCls), true);
		instanceMention.setOwnSlotValue(kpu.mentionInstanceSlot, simpleInstance);
		return instanceMention;
	}

	/**
	 * This method adds a slotMention to a class or instance mention. Please
	 * refer to the knowtator model in knowtator.pprj and look at the class
	 * definition for "knowtator class mention". This method adds a value to the
	 * slot 'knowtator_slot_mention'.
	 * 
	 * @see #addValueToSlotMention(SimpleInstance, Object)
	 */

	public void addSlotMention(SimpleInstance mention, SimpleInstance slotMention) {
		if ((isClassMention(mention) || isInstanceMention(mention)) && isSlotMention(slotMention)) {
			mention.addOwnSlotValue(kpu.getSlotMentionSlot(), slotMention);
		}
	}

	/**
	 * This method adds a value to a slotMention. Please refer to the knowtator
	 * model in knowtator.pprj and look at the class definition for "knowtator
	 * slot mention". This method adds a value to the slot
	 * 'knowtator_mention_slot_value'.
	 * 
	 * @param slotMention
	 *            must be an instance of a slot mention
	 * @param slotValue
	 *            can be a class mention, instance mention, or an Integer,
	 *            Float, String or Boolean.
	 */

	public void addValueToSlotMention(SimpleInstance slotMention, Object slotValue) {
		if (isComplexSlotMention(slotMention)) {
			if (slotValue instanceof SimpleInstance) {
				SimpleInstance slotValueInstance = (SimpleInstance) slotValue;
				if (isClassMention(slotValueInstance) || isInstanceMention(slotValueInstance)) {
					slotMention.addOwnSlotValue(kpu.getMentionSlotValueSlot(), slotValueInstance);
				}
			}
		} else if (isSlotMention(slotMention)) {
			slotMention.addOwnSlotValue(kpu.getMentionSlotValueSlot(), slotValue);
		}
	}

	public void removeValueFromSlotMention(SimpleInstance slotMention, Object slotValue) {
		if (isSlotMention(slotMention)) {
			slotMention.removeOwnSlotValue(kpu.getMentionSlotValueSlot(), slotValue);
		}
	}

	public SimpleInstance createSlotMention(Slot slot) {
		ValueType slotType = slot.getValueType();
		Cls instanceType = kpu.stringSlotMentionCls;
		if (slotType == ValueType.CLS || slotType == ValueType.INSTANCE)
			instanceType = kpu.complexSlotMentionCls;
		else if (slotType == ValueType.BOOLEAN)
			instanceType = kpu.booleanSlotMentionCls;
		else if (slotType == ValueType.FLOAT)
			instanceType = kpu.floatSlotMentionCls;
		else if (slotType == ValueType.INTEGER)
			instanceType = kpu.integerSlotMentionCls;

		SimpleInstance slotMention = kb.createSimpleInstance(new FrameID(null), CollectionUtilities
				.createCollection(instanceType), true);
		slotMention.setOwnSlotValue(kpu.mentionSlotSlot, slot);
		return slotMention;
	}

	/**
	 * Creates a mention based on the passed in instance. Instance is the super
	 * interface for Cls, SimpleInstance and Slot. Depending on the Class of the
	 * passed instance, the return value will be a "class mention", "instance
	 * mention" or "slot mention".
	 * 
	 * @param instance
	 *            should be Cls, SimpleInstance or Slot
	 * @return null - if instance is not a Cls, SimpleInstance or Slot.
	 *         Otherwise, returns a "class mention", "instance mention", or
	 *         "slot mention"
	 */

	public SimpleInstance createMention(Instance instance) {
		if (instance instanceof Cls)
			return createClassMention((Cls) instance);
		else if (instance instanceof SimpleInstance)
			return createInstanceMention((SimpleInstance) instance);
		else if (instance instanceof Slot)
			return createSlotMention((Slot) instance);
		return null;
	}

	public boolean isClassMention(SimpleInstance mention) {
		if (mention == null)
			return false;
		// return mention.getDirectType().equals(kpu.classMentionCls);
		return mention.hasType(kpu.classMentionCls);
	}

	public boolean isInstanceMention(SimpleInstance mention) {
		if (mention == null)
			return false;
		// return mention.getDirectType().equals(kpu.instanceMentionCls);
		return mention.hasType(kpu.instanceMentionCls);

	}

	public boolean isMention(SimpleInstance mention) {
		if (mention == null)
			return false;
		return mention.hasType(kpu.mentionCls);
	}

	public boolean isSlotMention(SimpleInstance mention) {
		if (mention == null)
			return false;
		if (mention.hasType(kpu.slotMentionCls) || mention.hasType(kpu.complexSlotMentionCls)
				|| mention.hasType(kpu.booleanSlotMentionCls) || mention.hasType(kpu.floatSlotMentionCls)
				|| mention.hasType(kpu.integerSlotMentionCls) || mention.hasType(kpu.stringSlotMentionCls))
			return true;

		return false;
	}

	public boolean isBooleanSlotMention(SimpleInstance mention) {
		if (mention == null)
			return false;
		if (mention.hasType(kpu.booleanSlotMentionCls))
			return true;
		return false;
	}

	public boolean isFloatSlotMention(SimpleInstance mention) {
		if (mention == null)
			return false;
		if (mention.hasType(kpu.floatSlotMentionCls))
			return true;
		return false;
	}

	public boolean isIntegerSlotMention(SimpleInstance mention) {
		if (mention == null)
			return false;
		if (mention.hasType(kpu.integerSlotMentionCls))
			return true;
		return false;
	}

	public boolean isStringSlotMention(SimpleInstance mention) {
		if (mention == null)
			return false;
		if (mention.hasType(kpu.stringSlotMentionCls))
			return true;
		return false;
	}

	public boolean isSimpleSlotMention(SimpleInstance mention) {
		if (mention == null)
			return false;
		if (isStringSlotMention(mention) || isIntegerSlotMention(mention) || isFloatSlotMention(mention)
				|| isBooleanSlotMention(mention))
			return true;
		else
			return false;
	}

	/**
	 * Determines whether an instance is a complex slot mention. Please refer to
	 * the knowtator model in knowtator.pprj and look at the class definition
	 * for "knowtator complex slot mention". If the instance is of type
	 * "knowtator complex slot mention" of one of its descendants, then true is
	 * returned.
	 * 
	 * @return true if the instance is of type "knowtator complex slot mention".
	 */

	public boolean isComplexSlotMention(SimpleInstance mention) {
		if (mention == null)
			return false;
		Cls mentionType = mention.getDirectType();
		if (mentionType == null)
			return false;
		if (mentionType.equals(kpu.complexSlotMentionCls) || mentionType.hasSuperclass(kpu.complexSlotMentionCls)) {
			return true;
		}
		return false;
	}

	/**
	 * This method gathers all the mentions (e.g. slot mentions, class mentions
	 * and instance mentions) that are connected to the passed in mention.
	 * Connections are directed and start from the passed in mention and are
	 * found recursively via slot mentions. For example, if a class mention is
	 * passed in, then its slot mentions, the mention values of the slot
	 * mentions, and their slot mentions (and so on) will all be in the returned
	 * set. The returned set does not parallel the structure of the
	 * relationships between the mentions - it simply gathers them up and dumps
	 * in the returned set. The returned set contains the mention that is passed
	 * to the method.
	 * 
	 * 
	 * @param mention
	 *            should be a Protege instance of mention.
	 * @return if mention is not a Protege instance of mention, then an empty
	 *         set will be returned. Otherwise, the set will contain the passed
	 *         in mention and all mentions (class, instance, and slot) that it
	 *         is connected to (see above.)
	 */
	public Set<SimpleInstance> getAllConnectedMentions(SimpleInstance mention) {
		Set<SimpleInstance> mentions = new HashSet<SimpleInstance>();
		if (isMention(mention)) {
			mentions.add(mention);
			_getAllConnectedMentions(mention, mentions);
		}
		return mentions;
	}

	private void _getAllConnectedMentions(SimpleInstance mention, Set<SimpleInstance> mentions) {
		if (isClassMention(mention) || isInstanceMention(mention)) {
			List<SimpleInstance> slotMentions = getSlotMentions(mention);
			for (SimpleInstance slotMention : slotMentions) {
				if (!mentions.contains(slotMention)) {
					mentions.add(slotMention);
					_getAllConnectedMentions(slotMention, mentions);
				}
			}
		}
		if (isSlotMention(mention)) {
			List<Object> slotValues = getSlotMentionValues(mention);
			for (Object slotValue : slotValues) {
				if (slotValue instanceof SimpleInstance) {
					if (isMention((SimpleInstance) slotValue)) {
						if (!mentions.contains(slotValue)) {
							mentions.add((SimpleInstance) slotValue);
							_getAllConnectedMentions((SimpleInstance) slotValue, mentions);
						}
					}
				}
			}
		}
	}

	/**
	 * This method returns all class mentions or instance mentions related to
	 * the passed in mention via slot mentions.
	 * 
	 * This method returns all of the complex slot mention values for a class
	 * mention or instance mention. Please refer to the knowtator model in
	 * knowtator.pprj and look at the class definition for "knowtator class
	 * mention" or "knowtator instance mention". The values of the slot
	 * "knowtator_slot_mention" may be of type "knowtator complex slot mention".
	 * Values of the slot "knowtator_mention_slot_value" for values of the
	 * passed in mentions values of the slot "knowtator_slot_mention" are
	 * returned.
	 * 
	 * @param mention
	 *            should be of type 'knowtator class mention' or 'knowtator
	 *            instance mention'.
	 * @return the slot mention values of the mention's slot mentions. All
	 *         values in the returned list should be of type 'knowtator class
	 *         mention' or 'knowtator instance mention'. This method will return
	 *         an empty list if there are no related mentions or if the mention
	 *         is not a class mention or instance mention.
	 */
	public List<SimpleInstance> getRelatedMentions(SimpleInstance mention) {
		List<SimpleInstance> returnValues = new ArrayList<SimpleInstance>();

		if (isClassMention(mention) || isInstanceMention(mention)) {
			List<SimpleInstance> complexSlotMentions = getComplexSlotMentions(mention);
			for (SimpleInstance complexSlotMention : complexSlotMentions) {
				Collection<SimpleInstance> mentionSlotValues = (Collection<SimpleInstance>) complexSlotMention
						.getOwnSlotValues(kpu.getMentionSlotValueSlot());
				if (mentionSlotValues != null)
					returnValues.addAll(mentionSlotValues);
			}
		}
		return returnValues;
	}

	/**
	 * This method returns all class mentions or instance mentions related to
	 * the passed in mention via slot mentions whose mentioned slot is the
	 * passed in slot.
	 * 
	 * Please refer to the knowtator model in knowtator.pprj and look at the
	 * class definition for "knowtator class mention" or "knowtator instance
	 * mention". The values of the slot "knowtator_slot_mention" may be of type
	 * "knowtator complex slot mention". Values of the slot
	 * "knowtator_mention_slot_value" for values of the passed in mention's
	 * values of the slot "knowtator_slot_mention" are returned if the slot
	 * mention's "knowtator_mention_slot" is the same as the passed in slot.
	 * 
	 * @param mention
	 *            should be of type 'knowtator class mention' or 'knowtator
	 *            instance mention'.
	 * @param slot
	 *            a slot corresponding to a knowtator_mention_slot of a
	 *            'knowtator complex slot mention'
	 * 
	 * @return All values in the returned list should be of type 'knowtator
	 *         class mention' or 'knowtator instance mention'. This method will
	 *         return an empty list if there are no related mentions or if the
	 *         mention is not a class mention or instance mention.
	 */
	public List<SimpleInstance> getRelatedMentions(SimpleInstance mention, Slot slot) {
		List<SimpleInstance> returnValues = new ArrayList<SimpleInstance>();
		if (isClassMention(mention) || isInstanceMention(mention)) {
			SimpleInstance slotMention = getSlotMention(mention, slot);
			if (isComplexSlotMention(slotMention)) {
				Collection<SimpleInstance> values = (Collection<SimpleInstance>) slotMention.getOwnSlotValues(kpu
						.getMentionSlotValueSlot());
				returnValues.addAll(values);
			}
		}
		return returnValues;
	}

	public Frame getMentionFrame(SimpleInstance mention) {
		if (mention == null)
			return null;
		if (isClassMention(mention)) {
			return (Cls) mention.getOwnSlotValue(kpu.mentionClassSlot);
		} else if (isInstanceMention(mention)) {
			return (Instance) mention.getOwnSlotValue(kpu.mentionInstanceSlot);
		}
		return null;
	}

	/**
	 * If the mention is a "class mention" then the Cls corresponding to the
	 * mentioned class is returned. If the mention is a "instance mention" then
	 * the Cls corresponding to the direct type of the mentioned instance is
	 * returned.
	 * 
	 * @return the class of a class mention, or the direct type of the instance
	 *         of an instance mention or null if the class or instance no longer
	 *         exists
	 */

	public Cls getMentionCls(SimpleInstance mention) {
		if (isClassMention(mention)) {
			return (Cls) mention.getOwnSlotValue(kpu.mentionClassSlot);
		} else if (isInstanceMention(mention)) {
			Instance mentionInstance = (Instance) mention.getOwnSlotValue(kpu.mentionInstanceSlot);
			if (mentionInstance != null)
				return mentionInstance.getDirectType();
		}
		return null;
	}

	/**
	 * This method sets the cls that is mentioned by a class mention. Please
	 * refer to the knowtator model in knowtator.pprj and look at the class
	 * definition for "knowtator class mention". This method sets the value for
	 * the slot "knowtator_mention_class".
	 * 
	 * @param clsMention
	 *            the mention must be a class mention or this method does
	 *            nothing
	 * @param mentionCls
	 *            the class that is mentioned by the instance mention
	 */
	public void setMentionCls(SimpleInstance clsMention, Cls mentionCls) {
		if (isClassMention(clsMention)) {
			clsMention.setDirectOwnSlotValue(kpu.getMentionClassSlot(), mentionCls);
		}
	}

	/**
	 * This method sets the instance that is mentioned by an instance mention.
	 * Please refer to the knowtator model in knowtator.pprj and look at the
	 * class definition for "knowtator instance mention". This method sets the
	 * value for the slot "knowtator_mention_instance".
	 * 
	 * @param instanceMention
	 *            the mention must be an instance mention or this method does
	 *            nothing
	 * @param mentionInstance
	 *            the instance that is mentioned by the instance mention
	 */
	public void setMentionInstance(SimpleInstance instanceMention, SimpleInstance mentionInstance) {
		if (isInstanceMention(instanceMention)) {
			instanceMention.setDirectOwnSlotValue(kpu.getMentionInstanceSlot(), mentionInstance);
		}
	}

	/**
	 * This method sets the slot that is mentioned by a slot mention. Please
	 * refer to the knowtator model in knowtator.pprj and look at the class
	 * definition for "knowtator slot mention". This method sets the value for
	 * the slot "knowtator_mention_slot".
	 * 
	 * @param slotMention
	 *            the mention must be a slot mention or this method does nothing
	 * @param mentionSlot
	 *            the slot that is mentioned by the slot mention
	 */
	public void setMentionSlot(SimpleInstance slotMention, Slot mentionSlot) {
		if (isSlotMention(slotMention)) {
			slotMention.setDirectOwnSlotValue(kpu.getMentionSlotSlot(), mentionSlot);
		}
	}

	public SimpleInstance getMentionInstance(SimpleInstance instanceMention) {
		if (isInstanceMention(instanceMention)) {
			return (SimpleInstance) instanceMention.getOwnSlotValue(kpu.mentionInstanceSlot);
		}
		return null;
	}

	/**
	 * This method gets the slot mentions of a class mention or instance
	 * mention. Please refer to the knowtator model in knowtator.pprj and look
	 * at the class definition for "knowtator class mention" or "knowtator
	 * instance mention". This method returns the values of the slot
	 * "knowtator_slot_mention".
	 * 
	 * @param mention
	 *            must be a class mention or instance mention otherwise an empty
	 *            list will be returned
	 * @return all slot mentions for the mention. If none exist, then an empty
	 *         list will be returned.
	 */
	public List<SimpleInstance> getSlotMentions(SimpleInstance mention) {
		List<SimpleInstance> returnValues = new ArrayList<SimpleInstance>();
		if (isClassMention(mention) || isInstanceMention(mention)) {
			Collection<SimpleInstance> slotMentions = (Collection<SimpleInstance>) mention.getDirectOwnSlotValues(kpu
					.getSlotMentionSlot());
			if (slotMentions != null) {
				returnValues.addAll(slotMentions);
			}
		}
		return returnValues;
	}

	/**
	 * This method gets the complex slot mentions of a class mention or instance
	 * mention. Please refer to the knowtator model in knowtator.pprj and look
	 * at the class definition for "knowtator class mention" or "knowtator
	 * instance mention". This method returns the values of the slot
	 * "knowtator_slot_mention" that are of type "knowtator complex slot
	 * mention".
	 * 
	 * @param mention
	 *            should be a class mention or instance mention;
	 * @return all complex slot mentions for the mention. If none exist, then an
	 *         empty list will be returned.
	 */
	public List<SimpleInstance> getComplexSlotMentions(SimpleInstance mention) {
		List<SimpleInstance> returnValues = new ArrayList<SimpleInstance>();
		if (isClassMention(mention) || isInstanceMention(mention)) {
			List<SimpleInstance> allSlotMentions = getSlotMentions(mention);
			for (SimpleInstance slotMention : allSlotMentions) {
				if (isComplexSlotMention(slotMention)) {
					returnValues.add(slotMention);
				}
			}
		}
		return returnValues;
	}

	public List<Slot> getMentionSlots(SimpleInstance mention) {
		Collection<Slot> slots = null;
		if (isClassMention(mention)) {
			Cls mentionCls = getMentionCls(mention);
			if (mentionCls != null)
				slots = (Collection<Slot>) mentionCls.getTemplateSlots();
		} else if (isInstanceMention(mention)) {
			Instance mentionInstance = getMentionInstance(mention);
			if (mentionInstance != null) {
				slots = mentionInstance.getOwnSlots();
				slots.remove(kb.getSlot(":DIRECT-TYPE"));
				slots.remove(kb.getSlot(":NAME"));
			}
		}
		if (slots != null) {
			return new ArrayList<Slot>(slots);
		} else
			return new ArrayList<Slot>();
	}

	/**
	 * gets a slot mention for the given slot of a class mention or instance
	 * mention. Please refer to the knowtator model in knowtator.pprj and look
	 * at the class definition for "knowtator class mention" or "knowtator
	 * instance mention". This method returns the values of the slot
	 * "knowtator_slot_mention" that correspond to the passed in slot.
	 * 
	 * @param mention
	 *            must be a class mention or instance mention otherwise null
	 *            will be returned.
	 * @param mentionSlot
	 *            the "knowtator_mention_slot" of the slot mention.
	 * @return an instance of 'knowtator slot mention' or one of its subtypes or
	 *         null if none exist.
	 */

	// what if there are two slot mentions with the same slot? editor does not
	// allow it but
	// knowtator model does.
	public SimpleInstance getSlotMention(SimpleInstance mention, Slot mentionSlot) {
		if (mentionSlot == null)
			return null;
		List<SimpleInstance> slotMentions = getSlotMentions(mention);
		for (SimpleInstance slotMention : slotMentions) {
			if (mentionSlot.equals(getSlotMentionSlot(slotMention))) {
				return slotMention;
			}
		}
		return null;
	}

	public boolean hasSlotMention(SimpleInstance mention, Slot mentionSlot) {
		SimpleInstance slotMention = getSlotMention(mention, mentionSlot);
		if (slotMention != null)
			return true;
		return false;
	}

	public boolean hasSlotValue(SimpleInstance slotMention) {
		if (isSlotMention(slotMention)) {
			Object value = slotMention.getOwnSlotValue(kpu.mentionSlotValueSlot);
			if (value != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * this method takes a mention and initializes slot mentions for slots that
	 * do not yet have a slotMention for the mention.
	 */

	public void initializeSlotMentions(SimpleInstance mention) {
		List<Slot> mentionSlots = getMentionSlots(mention);
		List<SimpleInstance> slotMentions = new ArrayList<SimpleInstance>(getSlotMentions(mention));
		for (Slot mentionSlot : mentionSlots) {
			if (!hasSlotMention(mention, mentionSlot)) {
				slotMentions.add(createSlotMention(mentionSlot));
			}
		}
		if (slotMentions.size() > 0) {
			mention.setOwnSlotValues(kpu.slotMentionSlot, slotMentions);
		}
	}

	public void removeEmptySlotMentions(SimpleInstance mention) {
		List<SimpleInstance> slotMentions = new ArrayList<SimpleInstance>(getSlotMentions(mention));
		List<SimpleInstance> deletedMentions = new ArrayList<SimpleInstance>();
		for (SimpleInstance slotMention : slotMentions) {
			Object value = slotMention.getOwnSlotValue(kpu.mentionSlotValueSlot);
			if (value == null) {
				deletedMentions.add(slotMention);
			}
		}
		slotMentions.removeAll(deletedMentions);
		mention.setOwnSlotValues(kpu.slotMentionSlot, slotMentions);
		for (Instance slotMention : deletedMentions) {
			kb.deleteInstance(slotMention);
		}
	}

	public void deleteMention(SimpleInstance mention) {
		List<SimpleInstance> slotMentions = getSlotMentions(mention);
		for (SimpleInstance slotMention : slotMentions) {
			kb.deleteInstance(slotMention);
		}
		kb.deleteInstance(mention);

	}

	public SimpleInstance getMentionAnnotation(SimpleInstance mention) {
		SimpleInstance mentionAnnotation = (SimpleInstance) mention.getOwnSlotValue(kpu.getMentionAnnotationSlot());
		return mentionAnnotation;
	}

	public void setMentionAnnotations(SimpleInstance mention, Collection<SimpleInstance> annotations) {
		mention.setOwnSlotValues(kpu.mentionAnnotationSlot, annotations);
	}

	public Slot getSlotMentionSlot(SimpleInstance mention) {
		if (isSlotMention(mention)) {
			return (Slot) mention.getOwnSlotValue(kpu.mentionSlotSlot);
		} else
			return null;
	}

	public List<Object> getSlotMentionValues(SimpleInstance slotMention) {
		List<Object> returnValues = new ArrayList<Object>();
		if (isSlotMention(slotMention)) {
			Collection<Object> values = (Collection<Object>) slotMention
					.getOwnSlotValues(kpu.getMentionSlotValueSlot());
			if (values != null) {
				returnValues.addAll(values);
			}
		}
		return returnValues;
	}

	public void setSlotMentionValues(SimpleInstance slotMention, List<Object> values) {
		if (isSlotMention(slotMention)) {
			slotMention.setOwnSlotValues(kpu.getMentionSlotValueSlot(), values);
		}
	}

	public SimpleInstance getMentionedBy(SimpleInstance slotMention) {
		if (isSlotMention(slotMention)) {
			return (SimpleInstance) slotMention.getOwnSlotValue(kpu.mentionedInSlot);
		}
		return null;
	}

	/**
	 * This method was written and contributed by Angus Roberts.
	 * 
	 * This method takes a mention slot with a known value on an annotation
	 * mention, and adds an inverse slot on the value, with the annotation
	 * mentions as its valu. If the inverse slot does not exist, it is added.
	 * Given annotationMention--[mentionSlot]--mentionSlotValueMention, the
	 * method will add
	 * mentionSlotValueMention--[inverse_of_mentionSlot]--annotationMention
	 * 
	 * @param annotationMention
	 *            an annotation mention with a mention slot
	 * @param mentionSlot
	 *            a slot, the inverse of which will be added
	 * @param mentionSlotValueMention
	 *            the value of that will be added to mentionSlot
	 */

	public void addInverse(SimpleInstance annotationMention, Slot mentionSlot, SimpleInstance mentionSlotValueMention) {
		Slot inverse = mentionSlot.getInverseSlot();
		if (inverse != null) {
			SimpleInstance inverseSlotMention = getSlotMention(mentionSlotValueMention, inverse);
			if (inverseSlotMention != null) {
				// The mentionSlotValueMention already has this inverse slot
				addValueToSlotMention(inverseSlotMention, annotationMention);
			} else {
				// It doesn't have this inverse slot, add it
				inverseSlotMention = createSlotMention(inverse);
				addValueToSlotMention(inverseSlotMention, annotationMention);
				addSlotMention(mentionSlotValueMention, inverseSlotMention);
			} // end of else
		} // end of if ()
	}

	/**
	 * This method was written and contributed by Angus Roberts.
	 * 
	 * This method takes a mention slot with a known value on an annotation
	 * mention, and checks for an inverse slot on the value. If the inverse
	 * exists, it is removed. Given
	 * annotationMention--[mentionSlot]--mentionSlotValueMention, the method
	 * will remove
	 * mentionSlotValueMention--[inverse_of_mentionSlot]--annotationMention
	 * 
	 * @param annotationMention
	 *            an annotation mention with a mention slot
	 * @param mentionSlot
	 *            a slot, the inverse of which will be removed
	 * @param mentionSlotValueMention
	 *            the value of mentionSlot
	 */

	public void removeInverse(SimpleInstance annotationMention, Slot mentionSlot, SimpleInstance mentionSlotValueMention) {
		Slot inverse = mentionSlot.getInverseSlot();
		if (inverse != null) {
			SimpleInstance inverseSlotMention = getSlotMention(mentionSlotValueMention, inverse);
			if (inverseSlotMention != null)
				removeValueFromSlotMention(inverseSlotMention, annotationMention);
		} // end of if ()
	}

	public List<SimpleInstance> getSlotFillerCandidates(SimpleInstance mention, Slot slot,
			List<SimpleInstance> annotations) {
		Set<SimpleInstance> currentSlotValues = new HashSet<SimpleInstance>(getRelatedMentions(mention, slot));

		List<SimpleInstance> returnValues = new ArrayList<SimpleInstance>();

		Cls mentionCls = getMentionCls(mention);
		if (mentionCls == null)
			return returnValues;

		HashSet<Cls> allowedClses = new HashSet<Cls>((Collection<Cls>) mentionCls.getTemplateSlotAllowedClses(slot));

		for (SimpleInstance annotation : annotations) {
			if (annotation.isDeleted())
				continue;

			SimpleInstance annotationMention = annotationUtil.getMention(annotation);
			if (currentSlotValues.contains(annotationMention))
				continue;

			if (isInstanceMention(annotationMention)) {
				SimpleInstance annotatedInstance = getMentionInstance(annotationMention);
				if (mentionCls.isValidOwnSlotValue(slot, annotatedInstance)) {
					returnValues.add(annotation);
				}
			} else if (isClassMention(annotationMention)) {
				Cls annotatedCls = getMentionCls(annotationMention);

				if (slot.getValueType().equals(ValueType.INSTANCE)) {
					HashSet<Cls> annotatedClsAncestors = new HashSet<Cls>((Collection<Cls>) annotatedCls
							.getSuperclasses());
					annotatedClsAncestors.add(annotatedCls);

					annotatedClsAncestors.retainAll(allowedClses);
					if (annotatedClsAncestors.size() > 0) {
						returnValues.add(annotation);
					}
				}
				if (slot.getValueType().equals(ValueType.CLS)) {
					HashSet annotatedClsAncestors = new HashSet(annotatedCls.getSuperclasses());
					allowedClses.retainAll(annotatedClsAncestors);
					if (allowedClses.size() > 0) {
						returnValues.add(annotation);
					}
				}
			}
		}
		return returnValues;
	}

	public void adjustSlotMentionForCardinality(Cls cls, Slot slot, SimpleInstance mention) {
		int maxCardinality = cls.getTemplateSlotMaximumCardinality(slot);
		if (maxCardinality > 0) {
			SimpleInstance slotMention = getSlotMention(mention, slot);
			if (slotMention != null) {
				List<Object> slotValues = getSlotMentionValues(slotMention);
				List<Object> newValues = new ArrayList<Object>();
				for (int i = slotValues.size() - 1, j = 0; i >= 0 && j < maxCardinality; i--) {
					newValues.add(slotValues.get(i));
					j++;
				}
				setSlotMentionValues(slotMention, newValues);
			}
		}
	}

}
