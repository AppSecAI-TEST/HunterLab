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
 * 8/11/2005    pvo   modified highlightSelectedInstance such that annotations 
 *                    referenced by the selected instance are also highlighted.
 */
package edu.uchsc.ccp.knowtator.ui;

/**
 * 
 * changes 1/23/2006 added 
 * 		textPane.setFont(UIManager.getFont("TextArea.font")); 
 * to initialize method
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;

import edu.stanford.smi.protege.model.SimpleInstance;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.Span;
import edu.uchsc.ccp.knowtator.SpanUtil;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.event.FilterChangedEvent;
import edu.uchsc.ccp.knowtator.event.FilterChangedListener;
import edu.uchsc.ccp.knowtator.textsource.TextSource;
import edu.uchsc.ccp.knowtator.textsource.TextSourceChangeEvent;
import edu.uchsc.ccp.knowtator.textsource.TextSourceChangeListener;

public class TextViewer implements TextSourceChangeListener, FilterChangedListener {
	JPanel panel;

	KnowtatorTextPane textPane;

	JScrollPane scrollPane;

	JScrollBar verticalScrollBar;

	String textSourceDisplayText;

	String filterInstanceDisplayText;

	TitledBorder border;

	KnowtatorManager manager;

	Rectangle viewportBounds = null;

	public TextViewer(KnowtatorTextPane textPane, KnowtatorManager manager) {
		this.textPane = textPane;
		this.manager = manager;
		initialize();
		EventHandler.getInstance().addFilterChangedListener(this);
	}

	public Color getSelectedTextColor() {
		Color selectionColor = textPane.getSelectionColor();
		return selectionColor;
	}

	public void initialize() {
		scrollPane = new JScrollPane(textPane);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getViewport().addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent event) {
				manager.updateVisibleFilteredAnnotations();
				manager.refreshAnnotationsDisplay(false);
			}
		});
		verticalScrollBar = scrollPane.getVerticalScrollBar();

		panel = new JPanel(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);
		border = BorderFactory.createTitledBorder("");
		border.setTitleFont(UIManager.getFont("Label.font"));
		panel.setBorder(border);

		textPane.setScrollPane(scrollPane);
	}

	public void filterChanged(FilterChangedEvent event) {
		SimpleInstance filter = event.getNewFilter();
		setFilterInstanceDisplayText(filter.getBrowserText());
	}

	public JPanel getContentPane() {
		return panel;
	}

	public void textSourceChanged(TextSourceChangeEvent event) {

		try {
			TextSource textSource = event.getTextSource();
			setTextSourceDisplayText(textSource.getName());
			panel.repaint();
			panel.validate();
			textPane.setText(textSource.getText());
			verticalScrollBar.setValue(verticalScrollBar.getMinimum());
			manager.refreshAnnotationsDisplay(true);
		} catch (Exception exception) {
			StringWriter stackTraceWriter = new StringWriter();
			exception.printStackTrace(new PrintWriter(stackTraceWriter));
			String stackTrace = stackTraceWriter.toString();
			JOptionPane.showMessageDialog(panel, "Exception thrown when text source changed: " + "\n" + stackTrace,
					"Exception", JOptionPane.ERROR_MESSAGE);
			exception.printStackTrace();
		}
	}

	public void setTextSourceDisplayText(String textSourceDisplayText) {
		this.textSourceDisplayText = textSourceDisplayText;
		setBorderText();
	}

	private void setFilterInstanceDisplayText(String filterInstanceDisplayText) {
		this.filterInstanceDisplayText = filterInstanceDisplayText;
		setBorderText();
	}

	private void setBorderText() {
		String borderText = "";
		if (textSourceDisplayText != null)
			borderText = "text source: " + textSourceDisplayText;
		if (filterInstanceDisplayText != null)
			borderText = borderText + "        filter: " + filterInstanceDisplayText;
		border.setTitle(borderText);
		panel.repaint();
	}

	public KnowtatorTextPane getTextPane() {
		return textPane;
	}

	/**
	 * Calculates the verticle distance in pixels between annotation1 and
	 * annotation2 as they appear in the text. If annotation1 comes before
	 * annotation2 in the text, then the vertical distance will be a positive
	 * number. The spans compared are the spans returned from
	 * SpanUtil.getFirstSpan()
	 * 
	 * @param annotation1
	 * @param annotation2
	 * @return the calculated vertical distance or if spans do not exist, then
	 *         Integer.MIN_VALUE
	 * @see SpanUtil#getFirstSpan(SimpleInstance)
	 */
	public int getVerticalDistance(SimpleInstance annotation1, SimpleInstance annotation2) {
		Span span1 = manager.getSpanUtil().getFirstSpan(annotation1);
		Span span2 = manager.getSpanUtil().getFirstSpan(annotation2);

		if (span1 == null || span2 == null)
			return Integer.MIN_VALUE;

		try {
			Rectangle span1Pos = textPane.modelToView(span1.getStart());
			Rectangle span2Pos = textPane.modelToView(span2.getStart());
			return span2Pos.y - span1Pos.y;
		} catch (BadLocationException ble) {
			ble.printStackTrace();
			return Integer.MIN_VALUE;
		}

	}

	public boolean isVisible(SimpleInstance annotation) {
		List<Span> spans = manager.getAnnotationUtil().getSpans(annotation);
		Span span;
		if(spans == null || spans.size() == 0) {
			span = manager.getSpanUtil().getAReferencedSpan(annotation);
		} else {
			span = spans.get(0);
		}
		if (span != null) {
			try {
				Rectangle annotationLocation = textPane.modelToView(span.getStart());
				if (annotationLocation == null)
					return false;
				annotationLocation = new Rectangle(annotationLocation.x, annotationLocation.y, 1, 1);
				Rectangle viewportBounds = scrollPane.getViewport().getViewRect();
				return viewportBounds.contains(annotationLocation);
			} catch (BadLocationException ble) {
				return false;
			}
		}
		return false;
	}

	public Span getVisibleSpan() {
		Rectangle viewportBounds = scrollPane.getViewport().getViewRect();
		int start = textPane.viewToModel(new Point(viewportBounds.x, viewportBounds.y));

		start = Math.max(0, start);
		int end = textPane.viewToModel(new Point(viewportBounds.x + viewportBounds.width, viewportBounds.y
				+ viewportBounds.height));
		end = Math.max(end, start);
		return new Span(start, end);
	}

	public Comparator<SimpleInstance> comparator(final SimpleInstance annotation) {
		return new Comparator<SimpleInstance>() {
			public int compare(SimpleInstance annotation1, SimpleInstance annotation2) {
				boolean isVisible1 = isVisible(annotation1);
				boolean isVisible2 = isVisible(annotation2);
				if (isVisible1 && !isVisible2)
					return -1;
				if (!isVisible1 && isVisible2)
					return 1;
				else {
					int distance1 = Math.abs(getVerticalDistance(annotation, annotation1));
					int distance2 = Math.abs(getVerticalDistance(annotation, annotation2));
					return distance1 <= distance2 ? -1 : 1;
				}
			}
		};
	}
}
