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

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;

import edu.stanford.smi.protege.model.*;
import org.annotation.gui.SwatchIcon;

import edu.stanford.smi.protege.event.FrameAdapter;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.util.CollectionUtilities;

public class DisplayColors {
	public static final Color DEFAULT_ANNOTATION_COLOR = new Color(128, 128, 128);

	public static final Icon DEFAULT_ICON = new SwatchIcon(10, 10, DEFAULT_ANNOTATION_COLOR);

	KnowtatorManager manager;

	KnowtatorProjectUtil kpu;

	KnowledgeBase kb;

	MentionUtil mentionUtil;

	AnnotationUtil annotationUtil;

	Map<Frame, Color> clsColorMap;

	Map<Color, Icon> colorIconMap;

	static Map<Color, Color> backgroundColors = new HashMap<Color, Color>();

	/** Creates a new instance of DisplayColors */
	public DisplayColors(KnowtatorManager manager) {
		this.manager = manager;
		this.kpu = manager.getKnowtatorProjectUtil();
		this.kb = manager.getKnowledgeBase();
		this.mentionUtil = manager.getMentionUtil();
		this.annotationUtil = manager.getAnnotationUtil();

		clsColorMap = new HashMap<Frame, Color>();
		colorIconMap = new HashMap<Color, Icon>();

		List<SimpleInstance> colorAssignments = manager.getColorAssignments();
		for (SimpleInstance colorAssignment : colorAssignments) {
			addAssignment(colorAssignment);
		}
		initListener();
	}

	private void initListener() {
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kb.getProject());
		configuration.addFrameListener(new FrameAdapter() {
			public void ownSlotValueChanged(FrameEvent frameEvent) {
				updateColorAssignments(frameEvent.getSlot());
			}

			public void ownSlotAdded(FrameEvent frameEvent) {
				updateColorAssignments(frameEvent.getSlot());
			}

			public void ownSlotRemoved(FrameEvent frameEvent) {
				updateColorAssignments(frameEvent.getSlot());
			}

			private void updateColorAssignments(Slot slot) {
				if (slot.equals(kpu.getColorAssignmentsSlot())) {
					updateColors();
				}
			}
		});
	}
	
	/**
	 * Updates the color maps, pulling any new color assignments from the 
	 *   manager.
	 */
	public void updateColors() {
		clsColorMap.clear();
		colorIconMap.clear();

		List<SimpleInstance> colorAssignments = manager.getColorAssignments();
		for (SimpleInstance colorAssignment : colorAssignments) {
			addAssignment(colorAssignment);
		}
	}

	private boolean isColorAssignmentInstance(SimpleInstance colorAssignmentInstance) {
		return colorAssignmentInstance.getDirectType().equals(kpu.colorAssignmentCls);
	}

	public void addAssignment(SimpleInstance colorAssignmentInstance) {
		if (isColorAssignmentInstance(colorAssignmentInstance)) {
			try {
				Cls colorClass = (Cls) colorAssignmentInstance.getOwnSlotValue(kpu.colorClassSlot);
				Instance displayColorInstance = (Instance) colorAssignmentInstance
						.getOwnSlotValue(kpu.displayColorSlot);
				int blue = (Integer) displayColorInstance.getOwnSlotValue(kpu.displayColorBSlot);
				int green = (Integer) displayColorInstance.getOwnSlotValue(kpu.displayColorGSlot);
				int red = (Integer) displayColorInstance.getOwnSlotValue(kpu.displayColorRSlot);
				Color clsColor = new Color(red, green, blue);
				clsColorMap.put(colorClass, clsColor);
				if (!colorIconMap.containsKey(clsColor))
					colorIconMap.put(clsColor, new SwatchIcon(10, 10, clsColor));
			} catch (NullPointerException npe) {
			}
		}
	}

	public void addAssignment(Cls assignedCls, SimpleInstance colorInstance) {
		SimpleInstance colorAssignment = kb.createSimpleInstance(new FrameID(null), CollectionUtilities
				.createCollection(kpu.colorAssignmentCls), true);
		colorAssignment.setOwnSlotValue(kpu.colorClassSlot, assignedCls);
		colorAssignment.setOwnSlotValue(kpu.displayColorSlot, colorInstance);
		addAssignment(colorAssignment);
	}

	public Icon getIcon(Color color) {
		if (colorIconMap.containsKey(color)) {
			return colorIconMap.get(color);
		} else {
			return DEFAULT_ICON;
		}
	}

	private Color _getColor(Cls cls) {
		Collection<Cls> superClses = (Collection<Cls>) cls.getDirectSuperclasses();
		if (superClses == null)
			return DEFAULT_ANNOTATION_COLOR;
		for (Cls superCls : superClses) {
			if (clsColorMap.containsKey(superCls)) {
				return clsColorMap.get(superCls);
			}
		}
		for (Cls superCls : superClses) {
			return _getColor(superCls);
		}
		return DEFAULT_ANNOTATION_COLOR;
	}

	public Color getColor(Frame frame) {
		try {
			if (frame == null)
				return DEFAULT_ANNOTATION_COLOR;

			if (clsColorMap.containsKey(frame)) {
				return clsColorMap.get(frame);
			} else if (frame instanceof Cls) {
				Color clsColor = _getColor((Cls) frame);
				clsColorMap.put(frame, clsColor);
				return clsColor;
			} else if (frame instanceof SimpleInstance) {
				SimpleInstance instance = (SimpleInstance) frame;
				Color frameColor = null;
				if (annotationUtil.isAnnotation(instance)) {
					SimpleInstance annotatedMention = annotationUtil.getMention(instance);
					frameColor = getColor(annotatedMention); 
				} else if (mentionUtil.isClassMention(instance) || mentionUtil.isInstanceMention(instance)) {
					Cls mentionCls = mentionUtil.getMentionCls(instance);
					frameColor = getColor(mentionCls);
				} else {
					frameColor = getColor(instance.getDirectType());
				}
				clsColorMap.put(frame, frameColor);
				return frameColor;
			}
		} catch (NullPointerException npe) {
		}

		return DEFAULT_ANNOTATION_COLOR;
	}

	public static Color getBackgroundColor(Color color) {
		if (backgroundColors.containsKey(color))
			return backgroundColors.get(color);
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		red = red + 4 * ((255 - red) / 5);
		green = green + 4 * ((255 - green) / 5);
		blue = blue + 4 * ((255 - blue) / 5);
		Color backgroundColor = new Color(red, green, blue);
		backgroundColors.put(color, backgroundColor);
		return backgroundColor;
	}
}

/**
 *TODO need to check for existing color assignment for a given class first and
 * update it if possible rather than always creating a new one.
 */

// public void askForColorAssignment(Component component)
// {
// Cls rootCls = ProjectSettings.getRootCls(kb.getProject());
// Cls cls = DisplayUtilities.pickCls(component,
// kb,
// CollectionUtilities.createCollection(rootCls),
// "Choose class to assign a color to");
//    
// if(cls != null)
// {
// Instance colorInstance = DisplayUtilities.pickInstance(component,
// CollectionUtilities.createCollection(kpu.displayColorCls),
// "Choose color for "+cls.getBrowserText());
// if(colorInstance != null)
// {
// addAssignment(cls, (SimpleInstance) colorInstance);
// JOptionPane.showMessageDialog(component,
// "Color assignments may not take full effect until project is re-opened."+
// "  Color assignments may be deleted under the \"Instances\" tab.","",
// JOptionPane.INFORMATION_MESSAGE);
// }
// }
//
// }
