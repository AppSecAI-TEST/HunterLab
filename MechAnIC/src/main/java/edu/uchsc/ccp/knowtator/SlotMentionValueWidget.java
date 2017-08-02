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

package edu.uchsc.ccp.knowtator;

import java.awt.Container;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.util.AddAction;
import edu.stanford.smi.protege.util.AllowableAction;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.RemoveAction;
import edu.stanford.smi.protege.widget.AbstractListWidget;
import edu.stanford.smi.protege.widget.ClsWidget;
import edu.stanford.smi.protege.widget.SlotWidget;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.ui.dialog.ListConfirmDialog;

public class SlotMentionValueWidget extends AbstractListWidget {
	static final long serialVersionUID = 0;

	KnowtatorManager manager;

	MentionUtil mentionUtil;

	AnnotationUtil annotationUtil;

	KnowtatorProjectUtil kpu;

	private AllowableAction _addValueAction;

	private AllowableAction _removeValueAction;

	Logger logger = Logger.getLogger(SlotMentionValueWidget.class);

	public void initialize() {
		super.initialize();
		addButton(getAddValueAction());
		addButton(getRemoveValueAction());
		manager = (KnowtatorManager) getKnowledgeBase().getClientInformation(Knowtator.KNOWTATOR_MANAGER);
		mentionUtil = manager.getMentionUtil();
		annotationUtil = manager.getAnnotationUtil();
		kpu = manager.getKnowtatorProjectUtil();
	}

	public void setValues(Collection values) {
		super.setValues(values);
		SimpleInstance slotMention = (SimpleInstance) getInstance();
		Slot mentionSlot = mentionUtil.getSlotMentionSlot(slotMention);

		String slotLabel = null;
		try {
			SimpleInstance mentionedByMention = mentionUtil.getMentionedBy(slotMention);
			Cls mentionCls = mentionUtil.getMentionCls(mentionedByMention);
			ClsWidget clsWidget = getProject().getDesignTimeClsWidget(mentionCls);
			SlotWidget slotWidget = clsWidget.getSlotWidget(mentionSlot);
			slotLabel = slotWidget.getLabel();
		} catch (Exception e) {
			logger.debug("", e);
		}

		if (slotLabel != null)
			getLabeledComponent().setHeaderLabel(slotLabel);
		else
			getLabeledComponent().setHeaderLabel(mentionSlot.getBrowserText());

	}

	protected Action getAddValueAction() {
		_addValueAction = new AddAction(ResourceKey.VALUE_ADD) {
			static final long serialVersionUID = 0;

			public void onAdd() {
				handleAddAction();
			}
		};
		return _addValueAction;
	}

	String requestSymbolValue(Cls mentionCls, Slot mentionSlot) {
		Object defaultValue = getDefaultValue(mentionCls, mentionSlot);

		java.util.List<Object> values = new ArrayList<Object>();
		values.addAll(mentionCls.getTemplateSlotAllowedValues(mentionSlot));
		if (values.size() > 0) {
			if (defaultValue == null) {
				defaultValue = values.get(0);
			}
			
			//Modified to show a list instead of a drop down, reducing the amount of clicks required											
			ListConfirmDialog dia = new ListConfirmDialog( Knowtator.getProtegeFrame( this ),
														   "Symbol Selection",
														   "Select Symbol",
														   true, values, defaultValue );					
			dia.setLocationRelativeTo( this );					
			dia.setVisible( true );
						
			if( dia.getCloseOption() == ListConfirmDialog.OK_OPTION ) {
				Object selection = dia.getSelectedItem();
				if( selection != null ) {
					return selection.toString();
				}
			}						
		}
		
		return null;
	}

	Boolean requestBooleanValue(Cls mentionCls, Slot mentionSlot) {
		Object defaultValue = getDefaultValue(mentionCls, mentionSlot);
		if (defaultValue == null)
			defaultValue = Boolean.TRUE;

		java.util.List values = new ArrayList();
		values.add(Boolean.TRUE);
		values.add(Boolean.FALSE);
		Object selection = JOptionPane.showInputDialog(this, "Select true or false", "Boolean selection",
				JOptionPane.PLAIN_MESSAGE, null, values.toArray(), defaultValue);
		if (selection != null) {
			return (Boolean) selection;
		}
		return null;
	}

	String requestStringValue(Cls mentionCls, Slot mentionSlot) {
		Object defaultValue = getDefaultValue(mentionCls, mentionSlot);
		if (defaultValue == null)
			defaultValue = "";

		String stringValue = JOptionPane.showInputDialog(this, "Enter string value", defaultValue);
		if (stringValue != null && stringValue.trim().length() > 0) {
			return stringValue;
		}
		return null;
	}

	Integer requestIntegerValue(Cls mentionCls, Slot mentionSlot) {
		Object defaultValue = getDefaultValue(mentionCls, mentionSlot);

		String intValue = null;
		if (defaultValue == null)
			intValue = JOptionPane.showInputDialog(this, "Enter an integer value");
		else
			intValue = JOptionPane.showInputDialog(this, "Enter an integer value", defaultValue);

		if (intValue == null || intValue.trim().equals(""))
			return null;

		String invalidErrorMessage = getInvalidIntegerDescription(intValue, mentionCls, mentionSlot);
		if (invalidErrorMessage != null) {
			JOptionPane
					.showMessageDialog(this, invalidErrorMessage, "Invalid integer value", JOptionPane.ERROR_MESSAGE);
			return null;
		} else
			return new Integer(intValue);
	}

	Float requestFloatValue(Cls mentionCls, Slot mentionSlot) {
		Object defaultValue = getDefaultValue(mentionCls, mentionSlot);

		String floatValue = null;
		if (defaultValue == null)
			floatValue = JOptionPane.showInputDialog(this, "Enter a number");
		else
			floatValue = JOptionPane.showInputDialog(this, "Enter a number", defaultValue);

		if (floatValue == null || floatValue.trim().equals(""))
			return null;

		String invalidErrorMessage = getInvalidFloatDescription(floatValue, mentionCls, mentionSlot);
		if (invalidErrorMessage != null) {
			JOptionPane.showMessageDialog(this, invalidErrorMessage, "Invalid number", JOptionPane.ERROR_MESSAGE);
			return null;
		} else
			return new Float(floatValue);
	}

	Object getDefaultValue(Cls mentionCls, Slot mentionSlot) {
		Collection defaultValues = mentionCls.getTemplateSlotValues(mentionSlot);
		if (defaultValues == null || defaultValues.size() == 0) {
			defaultValues = mentionCls.getTemplateSlotDefaultValues(mentionSlot);
		}
		if (defaultValues != null && defaultValues.size() > 0)
			return CollectionUtilities.getFirstItem(defaultValues);
		return null;
	}

	private void showSlotMissingMessage() {
		JOptionPane.showMessageDialog(this, "There is not a slot specified for this slot value.\n"
				+ "This is most likely a result of deleting a slot \n"
				+ "from the annotation schema after this annotation \n"
				+ "was created.  Please remove this slot value, select\n"
				+ "another annotation, and re-select this annotation.", "Slot missing", JOptionPane.WARNING_MESSAGE);
	}

	private void showTypeMissingMessage(SimpleInstance mention) {
		if (mentionUtil.isClassMention(mention))
			JOptionPane.showMessageDialog(this, "There is no class assigned to this annotation.\n"
					+ "You may not add a value to this slot until a \n" + "class is assigned.", "No class assigned",
					JOptionPane.WARNING_MESSAGE);
		else if (mentionUtil.isInstanceMention(mention))
			JOptionPane.showMessageDialog(this, "There is no instance assigned to this annotation.\n"
					+ "You may not add a value to this slot until an \n" + "instance is assigned.",
					"No instance assigned", JOptionPane.WARNING_MESSAGE);

	}
	
	

	protected void handleAddAction() {
		SimpleInstance slotMention = (SimpleInstance) getInstance();
		Slot mentionSlot = mentionUtil.getSlotMentionSlot(slotMention);
		if (mentionSlot == null) {
			showSlotMissingMessage();
			return;
		}

		SimpleInstance mentionedByMention = mentionUtil.getMentionedBy(slotMention);
		Cls mentionCls = mentionUtil.getMentionCls(mentionedByMention);
		if (mentionCls == null) {
			showTypeMissingMessage(mentionedByMention);
			return;
		}

		Object value = null;
		ValueType type = mentionCls.getTemplateSlotValueType(mentionSlot);

		if (type == ValueType.SYMBOL) {
			value = requestSymbolValue(mentionCls, mentionSlot);
		} else if (type == ValueType.BOOLEAN) {
			value = requestBooleanValue(mentionCls, mentionSlot);
		} else if (type == ValueType.STRING) {
			value = requestStringValue(mentionCls, mentionSlot);
		} else if (type == ValueType.INTEGER) {
			value = requestIntegerValue(mentionCls, mentionSlot);
		} else if (type == ValueType.FLOAT) {
			value = requestFloatValue(mentionCls, mentionSlot);
		} else if (type == ValueType.CLS || type == ValueType.INSTANCE) {
			JOptionPane.showMessageDialog(this, "It appears that the value type constraint\n"
					+ "has changed since the slot values for this\n" + "slot were entered.\n"
					+ "Please remove the slot values for this slot,\n"
					+ "select a different annotation, and re-select\n " + "the currently selected annotation.",
					"value type constraint inconsistency", JOptionPane.WARNING_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this, "this type is not handled yet!");
		}
		if (value != null)
			addItem(value);

		/**
		 * "enforces" maximum cardinality constraint. Assumes that the values
		 * will be ordered as they appear in the widget (oldest to newest
		 * additions)
		 */
		int maxCardinality = mentionCls.getTemplateSlotMaximumCardinality(mentionSlot);
		if (maxCardinality > 0) {
			ArrayList values = new ArrayList(getValues());
			removeAllItems();
			for (int i = values.size() - 1, j = 0; i >= 0 && j < maxCardinality; i--) {
				addItem(values.get(i));
				j++;
			}
		}

		// now we need to check if the values are valid (and in setValues)
		// if the values are not valid we can call
		// AbstractSlotWidget.setInvalidValueBorder
		// if they are fine, then call AbstractSlotWidget.setNormalBorder
		
		EventHandler.getInstance().fireSlotValueChanged();
	}

	/**
	 * The code for this method was copied from
	 * edu.stanford.smi.protege.widget.IntegerFieldWidget and modified slightly
	 */

	protected String getInvalidIntegerDescription(String text, Cls mentionCls, Slot mentionSlot) {
		String result = null;
		try {
			int i = new Integer(text).intValue();
			Number min = mentionCls.getTemplateSlotMinimumValue(mentionSlot);
			if (min != null && i < min.intValue()) {
				result = "The minimum value is " + min;
			}
			Number max = mentionCls.getTemplateSlotMaximumValue(mentionSlot);
			if (max != null && i > max.intValue()) {
				result = "The maximum value is " + max;
			}
		} catch (NumberFormatException e) {
			result = "The value must be an integer";
		}
		return result;
	}

	/**
	 * The code for this method was copied from
	 * edu.stanford.smi.protege.widget.FloatFieldWidget and modified slightly
	 */

	protected String getInvalidFloatDescription(String text, Cls mentionCls, Slot mentionSlot) {
		String result = null;
		try {
			float f = Float.parseFloat(text);
			Number min = mentionCls.getTemplateSlotMinimumValue(mentionSlot);
			if (min != null && f < min.floatValue()) {
				result = "The minimum value is " + min;
			}
			Number max = mentionCls.getTemplateSlotMaximumValue(mentionSlot);
			if (max != null && f > max.floatValue()) {
				result = "The maximum value is " + max;
			}
		} catch (NumberFormatException e) {
			result = "The value must be a number";
		}
		return result;
	}

	protected Action getRemoveValueAction() {
		_removeValueAction = new RemoveAction(ResourceKey.VALUE_REMOVE, this) {
			public void onRemove(Collection values) {
				handleRemoveAction(values);
			}
		};
		return _removeValueAction;
	}

	protected void handleRemoveAction(Collection values) {
		removeItems(values);
		
		EventHandler.getInstance().fireSlotValueChanged();
	}

	public static boolean isSuitable(Cls cls, Slot slot, Facet facet) {
		KnowledgeBase kb = slot.getKnowledgeBase();
		Cls complexSlotMentionCls = kb.getCls(KnowtatorProjectUtil.COMPLEX_SLOT_MENTION_CLS_NAME);
		Slot mentionSlotValueSlot = kb.getSlot(KnowtatorProjectUtil.MENTION_SLOT_VALUE_SLOT_NAME);
		if (!cls.equals(complexSlotMentionCls) && mentionSlotValueSlot.equals(slot))
			return true;
		return false;
	}

	public void setEditable(boolean b) {
		setAllowed(_addValueAction, b);
	}

}
