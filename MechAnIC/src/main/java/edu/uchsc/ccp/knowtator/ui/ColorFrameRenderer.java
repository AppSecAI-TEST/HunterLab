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
 * 8/10/2005    pvo   the renderer is changed so that the main text
 *                    is set to the result returned from BrowserTextUtil.getBrowserText()
 *
 */
package edu.uchsc.ccp.knowtator.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JTree;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.ui.FrameRenderer;
import edu.stanford.smi.protege.ui.ParentChildNode;
import edu.uchsc.ccp.knowtator.BrowserTextUtil;
import edu.uchsc.ccp.knowtator.DisplayColors;
import edu.uchsc.ccp.knowtator.KnowtatorManager;

public class ColorFrameRenderer extends FrameRenderer {
	static final long serialVersionUID = 0;

	KnowtatorManager manager;

	DisplayColors displayColors;

	BrowserTextUtil browserTextUtil;

	public ColorFrameRenderer(KnowtatorManager manager) {
		this.manager = manager;
		this.displayColors = manager.getDisplayColors();
		this.browserTextUtil = manager.getBrowserTextUtil();
	}
	
	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
			boolean leaf, int row, boolean hasFocus) {		
		
		JComponent c = (JComponent)super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

		if( value instanceof ParentChildNode ) {
    		ParentChildNode node = (ParentChildNode)value;
    		String toolTip = "";
    		
    		if( node instanceof Cls ) {
    			Collection<?> col = ((Cls)node.getUserObject()).getDocumentation();
        		
        		Iterator<?> iter = col.iterator();
        		while( iter.hasNext() ) {
        			toolTip += (String)iter.next();
        		}
        						
        		if( !"".equals( toolTip ) ) {
        			c.setToolTipText( toolTip );
        		}
    		}    		
		}
    		
		return c;
	}

	protected Component setup(Component c, Object value, boolean hasFocus, boolean isSelected) {
		super.setup(c, value, hasFocus, isSelected);
		Color color = null;

		Frame displayedFrame = null;
		if (value instanceof Frame)
			displayedFrame = (Frame) value;

		if (value instanceof ParentChildNode) {
			ParentChildNode node = (ParentChildNode) value;
			Object userObject = node.getUserObject();
			if (userObject instanceof Frame)
				displayedFrame = (Frame) userObject;
		}

		if (displayedFrame != null) {
			color = displayColors.getColor(displayedFrame);
			int count = manager.getCurrentAnnotationCountForFrame(displayedFrame);
			if (count > 0) {
				if (manager.isConsensusMode()) {
					addText(" (" + manager.getConsolidatedAnnotationCountForFrame(displayedFrame) + "/" + count + ")");
				} else
					addText(" (" + count + ")");
			} else if (value instanceof SimpleInstance) {
				SimpleInstance instance = (SimpleInstance) value;
				if (manager.getAnnotationUtil().isAnnotation(instance)) {
					if (instance.getDirectType() != null) {
						clear();
						setMainText(browserTextUtil.getBrowserText(instance, 30));
						addText(getRendererAnnotationDisplayText(instance));
					}
				} else if (manager.getMentionUtil().isMention(instance)) {
					clear();
					setMainText(browserTextUtil.getBrowserText(instance, 30));
				}

			}
			// needs to be after clear() call
			setMainIcon(displayColors.getIcon(color));
		}
		return this;
	}

	protected String getRendererAnnotationDisplayText(SimpleInstance instance) {
		if (manager.getAnnotationUtil().isAnnotation(instance)) {
			SimpleInstance annotatedMention = manager.getAnnotationUtil().getMention(instance);

			if (manager.getMentionUtil().isClassMention(annotatedMention)) {
				Cls mentionCls = manager.getMentionUtil().getMentionCls(instance);
				if (mentionCls != null)
					return " (" + mentionCls.getName() + ")";
			} else if (manager.getMentionUtil().isInstanceMention(annotatedMention)) {
				Instance mentionInstance = manager.getMentionUtil().getMentionInstance(instance);
				if (mentionInstance != null)
					return " (" + mentionInstance.getBrowserText() + ")";
			}
		}
		return "";
	}

	protected Color getBackgroundColor() {
		Object value = getValue();
		if (_isSelected) {
			if (value instanceof SimpleInstance) {
				Color instanceColor = displayColors.getColor((SimpleInstance) value);
				if (instanceColor != null)
					return DisplayColors.getBackgroundColor(instanceColor);
			} else if (value instanceof ParentChildNode) {
				ParentChildNode node = (ParentChildNode) value;
				Object userObject = node.getUserObject();
				if (userObject instanceof Cls) {
					Color clsColor = displayColors.getColor((Cls) userObject);
					if (clsColor != null)
						return DisplayColors.getBackgroundColor(clsColor);
				}
			}
		}
		return super.getBackgroundColor();
	}
}
