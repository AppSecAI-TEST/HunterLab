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
 * Copyright (C) 2005-2008.  All Rights Reserved.
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
package edu.uchsc.ccp.knowtator.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;

import org.apache.log4j.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.BrowserTextUtil;
import edu.uchsc.ccp.knowtator.DisplayColors;
import edu.uchsc.ccp.knowtator.InvalidSpanException;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.MentionUtil;
import edu.uchsc.ccp.knowtator.Span;
import edu.uchsc.ccp.knowtator.SpanUtil;
import edu.uchsc.ccp.knowtator.event.AcceptedAnnotationEvent;
import edu.uchsc.ccp.knowtator.event.AcceptedAnnotationListener;
import edu.uchsc.ccp.knowtator.event.AnnotationCreatedEvent;
import edu.uchsc.ccp.knowtator.event.AnnotationCreatedListener;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.event.RefreshAnnotationsDisplayListener;
import edu.uchsc.ccp.knowtator.util.ProtegeUtil;

public class KnowtatorTextPane extends JTextPane implements RefreshAnnotationsDisplayListener,
															AnnotationCreatedListener,
															AcceptedAnnotationListener {

	public void annotationCreated(AnnotationCreatedEvent event) {
		clearSelectionHighlights();
	}

	public static final int NO_LINES = 0;

	public static final int SOLID_LINES = 1;

	public static final int DASHED_LINES = 2;

	public static final int VERTICAL_LINES = 3;

	Logger logger = Logger.getLogger(KnowtatorTextPane.class);

	static final long serialVersionUID = 0;

	Set<Span> annotationSpans;

	Map<Span, Color> annotationSpanColors;

	Map<Span, Integer> annotationSpanLines;

	Map<Span, String> annotationSpanSubtexts;

	List<Span> selectionSpans;

	List<Span> selectedAnnotationSpans;

	// we don't need selectionSpanColors because we will always use
	// getSelectionColor for selection spans

	Map<Color, KnowtatorHighlighter> painters;

	Map<Color, KnowtatorHighlighter> linePainters;

	Map<Color, KnowtatorHighlighter> dashedLinePainters;

	Map<Color, KnowtatorHighlighter> verticalLinePainters;

	String text = "";

	KnowtatorManager manager;

	AnnotationUtil annotationUtil;

	MentionUtil mentionUtil;

	DisplayColors displayColors;

	BrowserTextUtil browserTextUtil;

	SpanUtil spanUtil;

	String tokenRegex = null;

	Pattern tokenPattern;

	Highlighter highlighter;

	Color lineHighlightColor = Color.BLACK;

	Map<Integer, Set<SimpleInstance>> window2AnnotationsMap;

	int windowSize = 20;

	JPopupMenu popupMenu;

	boolean mouseButtonDown = false;

	JScrollPane scrollPane;

	Style doubleSpace;

	boolean initComplete = false;
	
	/** Contains the previous offset that was selected by the mouse. */
	private int previousOffset = 0;

	public KnowtatorTextPane(KnowtatorManager manager) {
		super();
		this.manager = manager;
		annotationUtil = manager.getAnnotationUtil();
		mentionUtil = manager.getMentionUtil();
		browserTextUtil = manager.getBrowserTextUtil();
		displayColors = manager.getDisplayColors();
		spanUtil = manager.getSpanUtil();

		popupMenu = new JPopupMenu();

		annotationSpans = new LinkedHashSet<Span>();
		annotationSpanColors = new HashMap<Span, Color>();
		annotationSpanLines = new HashMap<Span, Integer>();
		annotationSpanSubtexts = new HashMap<Span, String>();
		selectionSpans = new ArrayList<Span>();
		selectedAnnotationSpans = new ArrayList<Span>();

		addMouseListener(createMouseListener());
		addKeyListener(createKeyListener());

		painters = new HashMap<Color, KnowtatorHighlighter>();
		linePainters = new HashMap<Color, KnowtatorHighlighter>();
		dashedLinePainters = new HashMap<Color, KnowtatorHighlighter>();
		verticalLinePainters = new HashMap<Color, KnowtatorHighlighter>();
		updateTokenRegex();
		setEditable(false);

		window2AnnotationsMap = new HashMap<Integer, Set<SimpleInstance>>();
		highlighter = getHighlighter();

		EventHandler.getInstance().addRefreshAnnotationsDisplayListener(this);
		EventHandler.getInstance().addAnnotationCreatedListener(this);
		EventHandler.getInstance().addAcceptedAnnotationListener(this);
		logger.debug("made it to here.");
		initComplete = true;
	}

	public void refreshAnnotationsDisplay(boolean scrollToSelection) {
		logger.debug("");
		highlightSpans(scrollToSelection);
		repaint();
	}

	public void repaint() {
		super.repaint();
		if (!initComplete)
			return;
		if (doubleSpace == null) {
			StyledDocument doc = getStyledDocument();
			if (doc == null)
				return;
			Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
			if (def == null)
				return;
			doubleSpace = doc.addStyle("doubleSpace", def);
		}
		if (doubleSpace != null && text.length() > 0) {
			Font font = UIManager.getFont("TextArea.font");
			if (font.getSize() != StyleConstants.getFontSize(doubleSpace)) {
				StyleConstants.setFontFamily(doubleSpace, "TextArea.font");
				StyleConstants.setFontSize(doubleSpace, font.getSize());
				setParagraphAttributes(doubleSpace, true);
			}
			if (manager.getSubtextSlot() == null) {
				if (StyleConstants.getLineSpacing(doubleSpace) != 0.2f) {
					StyleConstants.setLineSpacing(doubleSpace, 0.2f);
					setParagraphAttributes(doubleSpace, true);
				}
			} else if (StyleConstants.getLineSpacing(doubleSpace) != 1.0f) {
				StyleConstants.setLineSpacing(doubleSpace, 1.0f);
				setParagraphAttributes(doubleSpace, true);
			}
		}
	}

	public void highlightSpans(boolean scrollToSelection) {

		logger.debug("");
		SimpleInstance selectedAnnotation = manager.getSelectedAnnotation();
		List<SimpleInstance> visibleAnnotations = manager.getVisibleFilteredAnnotations();
		highlightSpans(selectedAnnotation, visibleAnnotations, scrollToSelection);
	}

	public void setScrollPane(JScrollPane scrollPane) {
		logger.debug("");
		this.scrollPane = scrollPane;
	}

	public void setAnnotationUtil(AnnotationUtil annotationUtil) {
		logger.debug("");
		this.annotationUtil = annotationUtil;
	}

	public void setDisplayColors(DisplayColors displayColors) {
		logger.debug("");
		this.displayColors = displayColors;
	}

	public void updateTokenRegex() {
		logger.debug("");
		if (tokenRegex == null) {
			tokenRegex = manager.getTokenRegex();
			tokenPattern = Pattern.compile(tokenRegex);
		} else {
			String currentRegex = manager.getTokenRegex();
			if (!tokenRegex.equals(currentRegex)) {
				tokenRegex = currentRegex;
				tokenPattern = Pattern.compile(tokenRegex);
			}
		}
	}

	public void select(int start) {
		logger.debug("");
		int st = Math.max(0, start);
		st = Math.min(st, text.length() - 1);
		select(st, st);
	}

	/*
	 * overrides inherited method. super.select is called in
	 * select(Collection<Span)
	 */
	public void select(int start, int end) {
		logger.debug("");
		if (start <= end)
			select(new Span(start, end));
		else
			select(start);
	}

	public void select(Span span) {
		logger.debug("");
		Collection<Span> spans = new ArrayList<Span>();
		spans.add(span);
		select(spans);
	}

	public boolean select(Span span, boolean isNew) {
		logger.debug("");
		select(span);
		if (annotationSpans.contains(span))
			return true;
		return false;
	}

	public void select(Collection<Span> spans) {
		logger.debug("");
		for (Span span : spans) {
			int start = Math.max(0, span.getStart());
			int end = Math.min(span.getEnd(), text.length() - 1);
			super.select(start, end);
			if (start != end) {
				selectionSpans.add(span);
				manager.setSelectedSpans(selectionSpans);
				highlightSpan(span, getSelectionColor(), NO_LINES);
			}
		}
	}

	public void addAnnotationHighlights(Collection<Span> spans, Color color, int lines) {
		logger.debug("");
		for (Span span : spans) {
			if (annotationSpans.contains(span))
				continue;
			annotationSpans.add(span);
			annotationSpanColors.put(span, color);
			annotationSpanLines.put(span, lines);
		}
	}

	public void clearHighlights() {
		logger.debug("");
		clearAnnotationHighlights();
		// clearSelectionHighlights();
		hideHighlights();
	}

	public void hideHighlights() {
		logger.debug("");
		highlighter.removeAllHighlights();
		repaint();
	}

	public void clearAnnotationHighlights() {
		logger.debug("");
		annotationSpans.clear();
		annotationSpanColors.clear();
		annotationSpanLines.clear();

		annotationSpanSubtexts.clear();
	}

	public void clearSelectionHighlights() {
		logger.debug("");
		selectionSpans.clear();
		manager.setSelectedSpans(selectionSpans);
	}

	// consider getting rid of this method. I don't think it does anything
	// useful as
	// showSelectionHighlights is superceded by shoAnnotationHighlights
	public void showAllHighlights() {
		logger.debug("");
		showAnnotationHighlights();
		showSelectionHighlights();
	}

	public void showSelectionHighlights() {
		logger.debug(" number of selection spans = " + selectionSpans.size());
		for (Span span : selectionSpans) {
			Color color = getSelectionColor();

			highlightSpan(span, color, NO_LINES);
		}
	}

	public void showAnnotationHighlights() {
		logger.debug("");
		for (Span span : annotationSpans) {
			Color color = annotationSpanColors.get(span);
			int lines = annotationSpanLines.get(span);
			highlightSpan(span, color, lines);
		}
		repaint();
	}

	private void updateAnnotationLines() {
		logger.debug("");
		List<Span> sortedSpans = new ArrayList<Span>(annotationSpans);
		Collections.sort(sortedSpans);

		for (int i = 0; i < sortedSpans.size(); i++) {
			if (i == 0 || i == sortedSpans.size() - 1)
				continue;
			else {
				Span previousSpan = sortedSpans.get(i - 1);
				Span currentSpan = sortedSpans.get(i);
				if (previousSpan.getEnd() == currentSpan.getStart()) {
					if (annotationSpanLines.containsKey(currentSpan)
							&& annotationSpanLines.get(currentSpan) != NO_LINES) {
						continue;
					} else {
						annotationSpanLines.put(currentSpan, VERTICAL_LINES);
						highlightSpan(currentSpan, annotationSpanColors.get(currentSpan), VERTICAL_LINES);
					}
				}
			}
		}
	}

	private Object highlightSpan(Span span, Color color, int lines) {
		try {
			int start = Math.max(0, span.getStart());
			int end = Math.min(span.getEnd(), text.length() - 1);
			KnowtatorHighlighter painter = getHighlightPainter(color, lines);

			Object highlight = highlighter.addHighlight(start, end, painter);
			return highlight;
		} catch (BadLocationException ble) {
			ble.printStackTrace();
			return null;
		}
	}

	public void setText(String text) {
		try {
			logger.debug("");
			super.setText(text);
			// super.setPage(new File(
			// "C:/Documents and Settings/Philip/My Documents/CSLR/workspace/CoordinationResolution/data/annotation/pilot/textsources/test.html"
			// ).toURL());
			// this.text ="Hello!\nThis is some test html.\nGoodbye!";
			this.text = text;
			select(0, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * written to provide a way for AnnotationPicker and AnnotationSchemaTree
	 * context menu to temporarily highlight an annotation.
	 * 
	 * @param annotation
	 * @see AnnotationPicker#mouseMoved(MouseEvent)
	 */
	public void highlightAnnotationTemp(SimpleInstance annotation) {
		logger.debug("");
		hideHighlights();
		try {
			Color annotationColor = displayColors.getColor(annotation);
			java.util.List<Span> spans = annotationUtil.getSpans(annotation);
			if (spans.size() > 0) {
				for (Span span : spans) {
					highlightSpan(span, annotationColor, SOLID_LINES);
				}
				select(spans.get(0).getStart()); // this here so that the
												 // textpane will scroll to the
												 // highlighted text.
			} else {
				Set<SimpleInstance> referencedAnnotations = annotationUtil
						.getRelatedAnnotations((SimpleInstance) annotation);
				boolean textSelectionChanged = false; // we only want to do a
													  // textPane.select(int)
													  // once see comment below.
				for (SimpleInstance referencedAnnotation : referencedAnnotations) {
					annotationColor = displayColors.getColor(referencedAnnotation);
					spans = annotationUtil.getSpans(referencedAnnotation);
					for (Span span : spans) {
						highlightSpan(span, annotationColor, SOLID_LINES);
						if (!textSelectionChanged) {
							select(span.getStart()); // this here so that the
													 // textpane will scroll to
													 // the highlighted text.
							textSelectionChanged = true;
						}
					}
				}
			}
		} catch (InvalidSpanException ise) {
			ise.printStackTrace();
		}
		showAllHighlights();
	}

	private void selectAnnotation(int offset) {
		logger.debug("");
		Collection<SimpleInstance> clickedAnnotations = getAnnotationsAt(offset);
		if (clickedAnnotations.size() > 0) {
			SimpleInstance annotation = annotationUtil.getShortestAnnotation(clickedAnnotations);
			SimpleInstance selectedAnnotation = manager.getSelectedAnnotation();
			if (annotation.equals(selectedAnnotation)) {
				manager.setSelectedAnnotation(null);
				manager.setSelectedConsensusAnnotation( null );
			} else {
				manager.setSelectedAnnotation(annotation);
				
				if( manager.isConsensusMode() ) {
					manager.setSelectedConsensusAnnotation( null );
				}
			}
		}
	}

	public void highlightSelectedInstance(SimpleInstance selectedAnnotation, boolean scrollToSelection) throws InvalidSpanException {
		logger.debug("");
		selectedAnnotationSpans.clear();
		if (selectedAnnotation != null) {
			Color annotationColor;
			java.util.List<Span> spans;

			Set<SimpleInstance> referencedAnnotations = annotationUtil
					.getRelatedAnnotations((SimpleInstance) selectedAnnotation);

			boolean textSelectionChanged = false; // we only want to do a
												  // textPane.select(int) once
												  // see comment below.

			for (SimpleInstance referencedAnnotation : referencedAnnotations) {
				if (referencedAnnotation.equals(selectedAnnotation))
					continue;
				annotationColor = displayColors.getColor(referencedAnnotation);
				getSubtext(referencedAnnotation);
				spans = annotationUtil.getSpans(referencedAnnotation);
				if (spans.size() > 0) {
					addAnnotationHighlights(spans, annotationColor, DASHED_LINES);
					if (!textSelectionChanged && scrollToSelection) {
						select(spans.get(0).getStart()); // this here so that
														 // the textpane will
														 // scroll to the
														 // highlighted text.
						textSelectionChanged = true;
					}
				}
			}

			annotationColor = displayColors.getColor(selectedAnnotation);
			getSubtext(selectedAnnotation);
			spans = annotationUtil.getSpans(selectedAnnotation);
			if (spans.size() > 0) {
				addAnnotationHighlights(spans, annotationColor, SOLID_LINES);
				if(scrollToSelection) {
				select(spans.get(0).getStart()); // we do this so that the
												 // textpane will scroll to the
												 // highlighted annotation.
				}
			}
			selectedAnnotationSpans.addAll(spans);
		}
	}

	public void highlightSpans(SimpleInstance selectedAnnotation, java.util.List<SimpleInstance> annotations, boolean scrollToSelection)
			throws InvalidSpanException {
		logger.debug("");
		clearHighlights();
		highlightSelectedInstance(selectedAnnotation, scrollToSelection);
		Set<SimpleInstance> referencedAnnotations;
		if (selectedAnnotation != null)
			referencedAnnotations = annotationUtil.getRelatedAnnotations((SimpleInstance) selectedAnnotation);
		else
			referencedAnnotations = Collections.emptySet();

		if (annotations != null) {
			for (SimpleInstance annotation : annotations) {
				if (!annotation.equals(selectedAnnotation) && !referencedAnnotations.contains(annotation)) {
					Color annotationColor = displayColors.getColor(annotation);
					getSubtext(annotation);
					if (manager.getFadeUnselectedAnnotations())
						annotationColor = DisplayColors.getBackgroundColor(annotationColor);
					if (manager.isConsensusMode() && annotationUtil.hasTeamAnnotator(annotation)) {
						annotationColor = DisplayColors.getBackgroundColor(annotationColor);
					}
					java.util.List<Span> spans = annotationUtil.getSpans(annotation);
					addAnnotationHighlights(spans, annotationColor, NO_LINES);
				}
			}
		}
		populateSpan2Annotations(annotations);
		updateAnnotationLines();
		hideHighlights();
		showSelectionHighlights();
		showAnnotationHighlights();
	}

	private void populateSpan2Annotations(Collection<SimpleInstance> annotations) {
		logger.debug("");
		window2AnnotationsMap.clear();
		for (SimpleInstance annotation : annotations) {
			List<Span> spans = annotationUtil.getSpans(annotation);
			for (Span span : spans) {
				int startKey = span.getStart() / windowSize;
				int endKey = span.getEnd() / windowSize;
				for (int key = startKey; key <= endKey; key++) {
					if (!window2AnnotationsMap.containsKey(key)) {
						window2AnnotationsMap.put(key, new HashSet<SimpleInstance>());
					}
					window2AnnotationsMap.get(key).add(annotation);
				}
			}
		}
	}

	public List<SimpleInstance> getAnnotationsAt(int offset) {
		logger.debug("");
		Integer key = new Integer(offset / windowSize);
		List<SimpleInstance> returnValues = new ArrayList<SimpleInstance>();
		if (window2AnnotationsMap.containsKey(key)) {
			Set<SimpleInstance> candidateAnnotations = window2AnnotationsMap.get(key);
			for (SimpleInstance annotation : candidateAnnotations) {
				List<Span> spans = annotationUtil.getSpans(annotation);
				for (Span span : spans) {
					if (offset >= span.getStart() && offset <= span.getEnd()) {
						returnValues.add(annotation);
						break;
					}
				}
			}
		}
		return returnValues;
	}
	
	/**
	 * Works similar to <code>getAnnotationsAt</code>, although it checks the entire
	 *   document, not simply the currently visible annotations.
	 *   
	 * @param offset The offset in the text document where the annotations overlap
	 * @return A List of all annotation at the given offset in the document, that is
	 *          where the spans of those annotations contain the offset.
	 */
	public List<SimpleInstance> getAnnotationsAtFromFullDocument(int offset) {
		logger.debug("");
		List<SimpleInstance> returnValues = new ArrayList<SimpleInstance>();
		List<SimpleInstance> candidateAnnotations = manager.getCurrentFilteredAnnotations();
		for (SimpleInstance annotation : candidateAnnotations) {
			List<Span> spans = annotationUtil.getSpans(annotation);
			for (Span span : spans) {
				if (offset >= span.getStart() && offset <= span.getEnd()) {
					returnValues.add(annotation);
					break;
				}
			}
		}

		return returnValues;
	}	
	
	public void annotationAccepted(AcceptedAnnotationEvent evt) {
		List<SimpleInstance> clickedAnnotations = new ArrayList<SimpleInstance>(getAnnotationsAt(previousOffset));
		if( manager.isConsensusMode() && (clickedAnnotations.size() > 1) ) {
			for( SimpleInstance annotation : clickedAnnotations ) {
				if( annotation != evt.getAcceptedAnnotation() ) {
					manager.setSelectedConsensusAnnotation( annotation );
					return;
				}
			}
		}
	}
	
	public void setPreviousOffset( int offset ) {
		previousOffset = offset;
	}
	
	/**
	 * Finds out if either the selected annotation or the selected consensus annotation (on the right
	 *   side of consensus mode) is contained in the given list of annotations.
	 * 
	 * @param clickedAnnotations A list of annotations, usually the annotations that were clicked
	 *                             on in this pane with the mouse.
	 * 
	 * @return True if the selected annotation or the selected consensus annotation is in the
	 *               given list of annotations. 
	 */
	private boolean containsSelectedAnnotations( List<SimpleInstance> clickedAnnotations ) {
		for( SimpleInstance annotation : clickedAnnotations ) {
			if( (annotation == manager.getSelectedAnnotation()) ||
				(annotation == manager.getSelectedConsensusAnnotation()) ) {
				
				return true;
			}
		}
		
		return false;
	}

	private void showContextMenu(MouseEvent mouseEvent) {
		logger.debug("");
		popupMenu.removeAll();
		int offset = viewToModel(mouseEvent.getPoint());
		previousOffset = offset;
		
		List<SimpleInstance> clickedAnnotations = new ArrayList<SimpleInstance>(getAnnotationsAt(offset));
		if (clickedAnnotations == null || clickedAnnotations.size() == 0)
			return;
		Collections.sort(clickedAnnotations, spanUtil.comparator(browserTextUtil.comparator()));

		if( manager.isConsensusMode() && ((clickedAnnotations.size() == 2) || !containsSelectedAnnotations(clickedAnnotations)) ) {
			//We are in consensus mode, and more than one annotations were selected.
			//Let's bypass the pop-up menu and simply show the first annotation in the regular annotation details
			//  panel and the second annotation in the list in the the consensus mode comparison panel.		
			manager.setSelectedAnnotations( clickedAnnotations );
		}
		
		if( !manager.isConsensusMode() ) {
			//Not consensus mode
    		for (final SimpleInstance annotation : clickedAnnotations) {
    			JMenuItem menuItem = new JMenuItem("select: " + browserTextUtil.getBrowserText(annotation, 50),
    					displayColors.getIcon(displayColors.getColor(annotation)));
    			menuItem.addActionListener(new ActionListener() {
    				public void actionPerformed(ActionEvent actionEvent) {
    					manager.setSelectedAnnotation(annotation);
    					if( manager.isConsensusMode() ) {
    						//This is a single selection of an annotation, so clear out the right pane if in consensus mode
    						manager.setSelectedConsensusAnnotation( null );
    					}
    				}
    			});
    			menuItem.addMouseListener(new MouseAdapter() {
    				public void mouseEntered(MouseEvent event) {
    					highlightAnnotationTemp(annotation);
    				}
    
    				public void mouseExited(MouseEvent event) {
    					manager.refreshAnnotationsDisplay(true);
    				}
    				
    			});
    			popupMenu.add(menuItem);
    		}
		} else {
			
			if( manager.isConsensusMode() && clickedAnnotations.size() != 2 ) {
				
    			//This is consensus mode
        		for (final SimpleInstance annotation : clickedAnnotations) {
        			JMenuItem menuItem = new JMenuItem( browserTextUtil.getBrowserText(annotation, 50), displayColors.getIcon(displayColors.getColor(annotation)));
        			menuItem.setToolTipText( "Left click to show in the left pane. Right click to show in the right pane." );
        			
        			menuItem.addMouseListener( new MouseAdapter() {
    					@Override
    					public void mousePressed(MouseEvent e) {
    						if( e.isPopupTrigger() ) {
    							if( manager.getSelectedAnnotation() != annotation ) {
    								manager.setSelectedConsensusAnnotation( annotation );
    							}
    						} 
    						//the selected annotation will be set in the mouseReleased method
    					}
    
    					@Override
    					public void mouseReleased(MouseEvent e) {
    						if( e.isPopupTrigger() ) {
    							if( manager.getSelectedAnnotation() != annotation ) {
    								manager.setSelectedConsensusAnnotation( annotation );
    							}
    						} else {
    							SimpleInstance consensusAnnotation = manager.getSelectedConsensusAnnotation();
    							if( consensusAnnotation != annotation ) {    								    							
    								manager.setSelectedAnnotation( annotation );
    								manager.setSelectedConsensusAnnotation( consensusAnnotation );    								
    							}
    						}
    					}							
        			});
        			
        			menuItem.addMouseListener(new MouseAdapter() {
        				public void mouseEntered(MouseEvent event) {
        					highlightAnnotationTemp(annotation);
        				}
        
        				public void mouseExited(MouseEvent event) {
        					manager.refreshAnnotationsDisplay(true);
        				}				    				
        			});
        			
        			if( manager.isSelectedAnnotation( annotation ) || manager.isSelectedConsensusAnnotation( annotation ) ) {
        				menuItem.setEnabled( false );
        			}
        			        		        			
        			popupMenu.add(menuItem);
        		}
			}

		}

		try {
			SimpleInstance annotation = manager.getSelectedAnnotation();
			final SimpleInstance mention = annotationUtil.getMention(annotation);
			final Cls cls = manager.getMentionUtil().getMentionCls(mention);
			if (cls != null) {
				Collection<Slot> slots = cls.getDirectTemplateSlots();
				for (final Slot slot : slots) {
					if (slot.getBrowserText().startsWith(":"))
						continue;
					final SimpleInstance slotMention = mentionUtil.getSlotMention(mention, slot);
					if (slotMention == null)
						continue;

					List<SimpleInstance> slotFillerCandidates = manager.getMentionUtil().getSlotFillerCandidates(
							mention, slot, clickedAnnotations);
					for (final SimpleInstance candidate : slotFillerCandidates) {
						String slotLabel = ProtegeUtil.getSlotLabel(cls, slot, manager.getKnowledgeBase().getProject());
						// String annotationText =
						// browserTextUtil.getBrowserText(annotation);
						// <b>"+annotationText+"</b>
						JMenuItem menuItem = new JMenuItem("<html>fill " + "<b>" + slotLabel
								+ "</b> slot of selected annotation with " + "<b>"
								+ browserTextUtil.getBrowserText(candidate, 50) + "</b></html>");
						menuItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent actionEvent) {
								mentionUtil.addValueToSlotMention(slotMention, annotationUtil.getMention(candidate));
								mentionUtil.adjustSlotMentionForCardinality(cls, slot, mention);
								mentionUtil.addInverse(mention, slot, annotationUtil.getMention(candidate));

								refreshAnnotationsDisplay(true);
							}
						});
						popupMenu.add(menuItem);
					}
				}
			}

		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}

		if (popupMenu.getSubElements().length == 1)
			selectAnnotation(offset);
		else if (popupMenu.getSubElements().length > 1)
			popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY() + 10);
	}

	private String getSubtext(SimpleInstance annotation) {
		try {
			Slot subtextSlot = manager.getSubtextSlot();
			if (subtextSlot == null)
				return null;
			SimpleInstance mention = annotationUtil.getMention(annotation);
			SimpleInstance slotMention = mentionUtil.getSlotMention(mention, subtextSlot);
			List<Object> slotValues = mentionUtil.getSlotMentionValues(slotMention);
			if (slotValues != null && slotValues.size() > 0) {
				String subtext = slotValues.get(0).toString();
				List<Span> spans = annotationUtil.getSpans(annotation);
				if (subtext != null && !subtext.trim().equals("") && spans != null) {
					for (Span span : spans)
						annotationSpanSubtexts.put(span, subtext);
				}

				return subtext;
			}
		} catch (NullPointerException npe) {
			return null;
		}

		return null;
	}

	private KnowtatorHighlighter getHighlightPainter(Color color, int lines) {
		logger.debug("");
		if (lines == SOLID_LINES) {
			if (!linePainters.containsKey(color))
				linePainters.put(color, new KnowtatorSelectionHighlighter(color));
			return linePainters.get(color);
		} else if (lines == DASHED_LINES) {
			if (!dashedLinePainters.containsKey(color))
				dashedLinePainters.put(color, new KnowtatorDashedHighlighter(color));
			return dashedLinePainters.get(color);

		} else if (lines == VERTICAL_LINES) {
			if (!verticalLinePainters.containsKey(color))
				verticalLinePainters.put(color, new KnowtatorVerticalHighlighter(color));
			return verticalLinePainters.get(color);
		} else {
			if (!painters.containsKey(color))
				painters.put(color, new KnowtatorHighlighter(color));
			return painters.get(color);
		}
	}

	/**
	 * This code was taken right out of its superclass
	 * DefaultHighlighter.DefaultHighlightPainter and modified to draw lines
	 * above and below the selected annotation.
	 */
	class KnowtatorHighlighter extends DefaultHighlighter.DefaultHighlightPainter {

		public KnowtatorHighlighter(Color c) {
			super(c);
		}

		public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
			logger.debug("");
			Shape returnValue = super.paintLayer(g, offs0, offs1, bounds, c, view);

			g.setColor(lineHighlightColor);
			Font textFont = UIManager.getFont("TextArea.font");
			int fontSize = textFont.getSize();
			g.setFont(new Font(textFont.getName(), textFont.getStyle(), fontSize));

			Rectangle rec = getRectangle(offs0, offs1, bounds, view);
			if (rec != null) {
				int start = viewToModel(new Point(rec.x, rec.y));
				int end = viewToModel(new Point(rec.x + rec.width, rec.y));
				String subtext = annotationSpanSubtexts.get(new Span(start, end));
				if (subtext != null) {
					FontMetrics fontMetrics = g.getFontMetrics();
					int stringWidth = fontMetrics.stringWidth(subtext);
					int startPoint = rec.x + (rec.width / 2);
					startPoint = startPoint - (stringWidth / 2);
					startPoint = Math.max(0, startPoint);
					g.drawString(subtext, startPoint, rec.y + rec.height + fontSize + 3);
				}
			}
			return returnValue;
		}

		public Rectangle getRectangle(int offs0, int offs1, Shape bounds, View view) {
			logger.debug("");
			Rectangle rec = null;
			if (offs0 == view.getStartOffset() && offs1 == view.getEndOffset()) {
				rec = (bounds instanceof Rectangle) ? (Rectangle) bounds : bounds.getBounds();
			} else {
				try {
					Shape shape = view.modelToView(offs0, Position.Bias.Forward, offs1, Position.Bias.Backward, bounds);
					rec = (shape instanceof Rectangle) ? (Rectangle) shape : shape.getBounds();
				} catch (BadLocationException e) {
				}
			}
			return rec;
		}
	}

	/**
	 * This code was taken right out of its superclass
	 * DefaultHighlighter.DefaultHighlightPainter and modified to draw lines
	 * above and below the selected annotation.
	 */
	class KnowtatorSelectionHighlighter extends KnowtatorHighlighter {
		public KnowtatorSelectionHighlighter(Color c) {
			super(c);
		}

		public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
			logger.debug("");
			Shape returnValue = super.paintLayer(g, offs0, offs1, bounds, c, view);

			g.setColor(lineHighlightColor);

			Rectangle rec = getRectangle(offs0, offs1, bounds, view);
			if (rec != null) {
				g.drawLine(rec.x, rec.y, rec.x + rec.width - 1, rec.y);
				g.drawLine(rec.x, rec.y - 1, rec.x + rec.width - 1, rec.y - 1);
				g.drawLine(rec.x, rec.y + rec.height, rec.x + rec.width - 1, rec.y + rec.height);
				g.drawLine(rec.x, rec.y + rec.height + 1, rec.x + rec.width - 1, rec.y + rec.height + 1);
			}
			return returnValue;
		}
	}

	class KnowtatorDashedHighlighter extends KnowtatorHighlighter {
		public KnowtatorDashedHighlighter(Color c) {
			super(c);
		}

		public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
			logger.debug("");
			Shape returnValue = super.paintLayer(g, offs0, offs1, bounds, c, view);

			Rectangle rec = getRectangle(offs0, offs1, bounds, view);
			if (rec != null) {
				drawDashedLine(g, rec.x, rec.x + rec.width - 1, rec.y);
				drawDashedLine(g, rec.x, rec.x + rec.width - 1, rec.y - 1);
				drawDashedLine(g, rec.x, rec.x + rec.width - 1, rec.y + rec.height);
				drawDashedLine(g, rec.x, rec.x + rec.width - 1, rec.y + rec.height + 1);
			}
			return returnValue;
		}

		private void drawDashedLine(Graphics g, int x1, int x2, int y) {
			logger.debug("");
			if (x1 >= x2) {
				logger.debug("x1 is larger than x2");
				return;
			}
			for (int x = x1; x < x2; x += 10) {
				g.drawLine(x, y, Math.min(x + 5, x2), y);
			}
			g.drawLine(x2, y, x2 - 2, y);
		}
	}

	class KnowtatorVerticalHighlighter extends KnowtatorHighlighter {
		public KnowtatorVerticalHighlighter(Color c) {
			super(c);
		}

		public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
			logger.debug("");
			Shape returnValue = super.paintLayer(g, offs0, offs1, bounds, c, view);

			Rectangle rec = getRectangle(offs0, offs1, bounds, view);
			if (rec != null) {
				g.drawLine(rec.x, rec.y, rec.x, rec.y + rec.height - 1);
			}
			return returnValue;
		}
	}

	private MouseListener createMouseListener() {
		return new MouseAdapter() {
			public void mousePressed(MouseEvent mouseEvent) {
				logger.debug("");
				hideHighlights();
				if (!mouseEvent.isControlDown()) {
					clearSelectionHighlights();
				} else {
					showSelectionHighlights();
				}
				mouseButtonDown = true;
			}

			public void mouseClicked(MouseEvent mouseEvent) {
				logger.debug("");
				showContextMenu(mouseEvent);
				// int offset = viewToModel(mouseEvent.getPoint());
				// selectAnnotation(offset);
			}

			public void mouseReleased(MouseEvent mouseEvent) {
				logger.debug("");

				mouseButtonDown = false;
				if (getSelectionStart() != getSelectionEnd()) {
					Span selectedSpan = new Span(getSelectionStart(), getSelectionEnd());
					updateTokenRegex();
					Span expandedSpan = SpanUtil.expandToSplits(text, selectedSpan, 15, 15, tokenPattern);
					if (expandedSpan != null) {
						select(expandedSpan);
					}
					showSelectionHighlights();
				}
				showAnnotationHighlights();

				boolean fastAnnotateMode = manager.isFastAnnotateMode();
				Frame fastAnnotateFrame = manager.getFastAnnotateFrame();
				if (fastAnnotateMode && fastAnnotateFrame != null && manager.getSelectedSpans().size() > 0) {
					manager.createAnnotation((Instance) fastAnnotateFrame);
				}
			}
		};
	}

	private KeyListener createKeyListener() {
		return new KeyAdapter() {
			public void keyReleased(KeyEvent keyEvent) {
				logger.debug("");
				if (keyEvent.getKeyCode() == KeyEvent.VK_CONTROL && mouseButtonDown) {
					hideHighlights();
					clearSelectionHighlights();
				}
			}
		};
	}

}
