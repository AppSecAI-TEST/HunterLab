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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.smi.protege.model.SimpleInstance;
import edu.uchsc.ccp.knowtator.event.EventHandler;
import edu.uchsc.ccp.knowtator.textsource.TextSource;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;
import edu.uchsc.ccp.knowtator.textsource.TextSourceChangeEvent;
import edu.uchsc.ccp.knowtator.textsource.TextSourceChangeListener;

public class SpanUtil implements TextSourceChangeListener {
	public static final String GROW_ANNOTATION_LEFT = "GROW_ANNOTATION_LEFT";

	public static final String GROW_ANNOTATION_LEFT_WORD = "GROW_ANNOTATION_LEFT_WORD";

	public static final String GROW_ANNOTATION_RIGHT = "GROW_ANNOTATION_RIGHT";

	public static final String GROW_ANNOTATION_RIGHT_WORD = "GROW_ANNOTATION_RIGHT_WORD";

	public static final String SHRINK_ANNOTATION_LEFT = "SHRINK_ANNOTATION_LEFT";

	public static final String SHRINK_ANNOTATION_LEFT_WORD = "SHRINK_ANNOTATION_LEFT_WORD";

	public static final String SHRINK_ANNOTATION_RIGHT = "SHRINK_ANNOTATION_RIGHT";

	public static final String SHRINK_ANNOTATION_RIGHT_WORD = "SHRINK_ANNOTATION_RIGHT_WORD";

	TextSource textSource;

	KnowtatorManager manager;

	KnowtatorProjectUtil kpu;

	AnnotationUtil annotationUtil;

	/** Creates a new instance of SpanUtil */
	public SpanUtil(KnowtatorManager manager) {
		this.manager = manager;
		this.kpu = manager.getKnowtatorProjectUtil();
		this.annotationUtil = manager.getAnnotationUtil();
	}

	public void textSourceChanged(TextSourceChangeEvent event) {
		textSource = event.getTextSource();
	}

	public boolean canGrowSpanRight(Span span) {
		try {
			if (Span.isValid(span.getStart(), span.getEnd() + 1)) {
				if (span.getEnd() + 1 <= textSource.getText().length()) {
					return true;
				}
			}
			return false;
		} catch (Exception exception) {
			return false;
		}
	}

	public boolean canShrinkSpanRight(Span span) {
		return Span.isValid(span.getStart(), span.getEnd() - 1);
	}

	public boolean canGrowSpanLeft(Span span) {
		return Span.isValid(span.getStart() - 1, span.getEnd());
	}

	public boolean canShrinkSpanLeft(Span span) {
		return Span.isValid(span.getStart() + 1, span.getEnd());
	}

	private void growSpanRight(Span span, SimpleInstance annotation) throws InvalidSpanException,
			TextSourceAccessException {
		if (canGrowSpanRight(span))
			editSpan(span, new Span(span.getStart(), span.getEnd() + 1), annotation);
	}

	private void growSpanRightWord(Span span, SimpleInstance annotation) throws InvalidSpanException,
			TextSourceAccessException {
		String text = manager.getTextSourceUtil().getCurrentTextSource().getText();
		Span superspan = expandToSplits(text, new Span(span.getStart(), span.getEnd() + 2), 30, 30, Pattern
				.compile(manager.getTokenRegex()));
		if (superspan != null) {
			if (superspan.getEnd() == span.getEnd())
				growSpanRight(span, annotation);
			else if (superspan.getEnd() > span.getEnd()) {
				editSpan(span, new Span(span.getStart(), superspan.getEnd()), annotation);
			}
		}
	}

	private void shrinkSpanRight(Span span, SimpleInstance annotation) throws InvalidSpanException,
			TextSourceAccessException {
		if (canShrinkSpanRight(span))
			editSpan(span, new Span(span.getStart(), span.getEnd() - 1), annotation);
	}

	private void shrinkSpanRightWord(Span span, SimpleInstance annotation) throws InvalidSpanException,
			TextSourceAccessException {
		String text = manager.getTextSourceUtil().getCurrentTextSource().getText();
		Span subspan = shrinkRight(text, span, Pattern.compile(manager.getTokenRegex()));
		if (subspan != null) {
			if (subspan.getEnd() == span.getEnd())
				shrinkSpanRight(span, annotation);
			else if (subspan.getEnd() < span.getEnd()) {
				editSpan(span, subspan, annotation);
			}
		}
	}

	private void shrinkSpanLeftWord(Span span, SimpleInstance annotation) throws InvalidSpanException,
			TextSourceAccessException {
		String text = manager.getTextSourceUtil().getCurrentTextSource().getText();
		Span subspan = shrinkLeft(text, span, Pattern.compile(manager.getTokenRegex()));
		if (subspan != null) {
			if (subspan.getStart() == span.getStart())
				shrinkSpanLeft(span, annotation);
			else if (subspan.getStart() > span.getStart()) {
				editSpan(span, subspan, annotation);
			}
		}
	}

	private void growSpanLeft(Span span, SimpleInstance annotation) throws InvalidSpanException,
			TextSourceAccessException {
		if (canGrowSpanLeft(span))
			editSpan(span, new Span(span.getStart() - 1, span.getEnd()), annotation);
	}

	private void growSpanLeftWord(Span span, SimpleInstance annotation) throws InvalidSpanException,
			TextSourceAccessException {
		if (span.getStart() == 0)
			return;
		String text = manager.getTextSourceUtil().getCurrentTextSource().getText();

		Span superspan = expandToSplits(text, new Span(span.getStart() - 2, span.getEnd()), 30, 30, Pattern
				.compile(manager.getTokenRegex()));
		if (superspan != null) {
			if (superspan.getStart() == span.getStart())
				growSpanLeft(span, annotation);
			else if (superspan.getStart() < span.getStart() && superspan.getStart() >= 0) {
				editSpan(span, new Span(superspan.getStart(), span.getEnd()), annotation);
			}
		}
	}

	private void shrinkSpanLeft(Span span, SimpleInstance annotation) throws InvalidSpanException,
			TextSourceAccessException {
		if (canShrinkSpanLeft(span))
			editSpan(span, new Span(span.getStart() + 1, span.getEnd()), annotation);
	}

	/**
	 * The strategy for updating a span is to get the Span objects for the
	 * annotationInstance loop through them and find the one that matches
	 * 'oldSpan' and update that span with the 'newSpan' in the list of spans
	 * returned from annotationUtil.getSpans. The list of spans are then sent
	 * back to annotationUtil.setSpans.
	 */

	private void editSpan(Span oldSpan, Span newSpan, SimpleInstance annotation) throws InvalidSpanException,
			TextSourceAccessException {
		List<Span> spans = new ArrayList<Span>(annotationUtil.getSpans(annotation));
		if (spans.size() == 0) {
			System.out.println("empty span list");
		}
		for (int i = 0; i < spans.size(); i++) {
			Span annotationSpan = spans.get(i);
			if (annotationSpan.equals(oldSpan)) {
				spans.set(i, newSpan);
				if (newSpan.getStart() == newSpan.getEnd())
					spans.remove(i);
				break;
			}
		}
		annotationUtil.setSpans(annotation, spans, null);
		manager.refreshAnnotationsDisplay(true);
	}

	public void editSpans(List<Span> spans, SimpleInstance annotation, String editType) throws InvalidSpanException,
			TextSourceAccessException {
		for (Span span : spans) {
			if (editType.equals(GROW_ANNOTATION_LEFT))
				growSpanLeft(span, annotation);
			if (editType.equals(GROW_ANNOTATION_LEFT_WORD))
				growSpanLeftWord(span, annotation);
			else if (editType.equals(GROW_ANNOTATION_RIGHT))
				growSpanRight(span, annotation);
			else if (editType.equals(GROW_ANNOTATION_RIGHT_WORD))
				growSpanRightWord(span, annotation);
			else if (editType.equals(SHRINK_ANNOTATION_LEFT))
				shrinkSpanLeft(span, annotation);
			else if (editType.equals(SHRINK_ANNOTATION_LEFT_WORD))
				shrinkSpanLeftWord(span, annotation);
			else if (editType.equals(SHRINK_ANNOTATION_RIGHT))
				shrinkSpanRight(span, annotation);
			else if (editType.equals(SHRINK_ANNOTATION_RIGHT_WORD))
				shrinkSpanRightWord(span, annotation);
		}
		EventHandler.getInstance().fireSpanEditted(annotation);

	}

	/**
	 * The purpose of this method is to "expand" the substringSpan such that the
	 * start and end of the returned span are at splits in the string.
	 * 
	 * @param superString
	 *            the string that is being looked at
	 * @param substringSpan
	 *            a span that designates some arbitrary substring of the
	 *            superString
	 * @param frontWindowSize
	 *            the string that immediately precedes the substring is
	 *            considered the frontwindow. This parameter sets the maximum
	 *            size of the frontwindow (it may be smaller if the superstring
	 *            doesn't have enough text that precedes the substring.)
	 * @param rearWindowSize
	 *            the string that immediately follows the substring is
	 *            considered the rearwindow. This parameter sets the maximum
	 *            size of the rearwindow (it may be smaller if the superstring
	 *            doesn't have enough text that follows the substring.)
	 * @param splitPattern
	 *            a regular expression that defines a "split" or a word
	 *            boundary. For example, you might pass in Pattern.compile(\\W+)
	 *            to define a boundary as non-word characters.
	 * @return a span that is "expanded" to start and end at word boundaries.
	 *         See the associated unit tests for examples. Typically the
	 *         substring associated with the returned span will be larger than
	 *         the substring associated with the substringSpan parameter.
	 *         However, if the substring associated with the substringSpan
	 *         begins or ends with the splitPattern, then the returned substring
	 *         may be shorter.
	 * 
	 */
	public static Span expandToSplits(String superString, Span substringSpan, int frontWindowSize, int rearWindowSize,
			Pattern splitPattern) {
		try {
			if (!(new Span(0, superString.length()).contains(substringSpan))) {
				return null;
			}

			String substring = superString.substring(substringSpan.getStart(), substringSpan.getEnd());

			// First we will determine the start of the returned span
			// If the substring starts with the splitPattern then we will move
			// the start
			// to the "right". Otherwise we will look for the last splitPattern
			// in the
			// front window.
			int returnSpanStart = substringSpan.getStart();

			// shrink the span start if substring begins with splitPattern
			boolean shrinkSpanStart = false;
			Matcher matcher = splitPattern.matcher(substring);
			if (matcher.find()) {
				if (matcher.start() == 0) {
					returnSpanStart += matcher.end();
					shrinkSpanStart = true;
				}
			}

			// if substring does not begin with splitPattern, then find last
			// splitPattern
			// in frontWindow
			if (!shrinkSpanStart) {
				// The front window starts at either 0 or frontWindowSize from
				// the substringSpan start
				int frontWindowStart = Math.max(0, substringSpan.getStart() - frontWindowSize);
				String frontWindow = superString.substring(frontWindowStart, substringSpan.getStart());
				matcher = splitPattern.matcher(frontWindow);
				int lastMatch = -1;
				if (frontWindowStart == 0) {
					lastMatch = 0;
				}
				while (matcher.find()) {
					lastMatch = matcher.end();
				}

				if (lastMatch > -1) {
					returnSpanStart -= frontWindow.substring(lastMatch).length();
				}
			}

			// now we figure out the end of the returned span
			// If the substring ends with the splitPattern then we will move the
			// end
			// to the "left". Otherwise we will look for the last splitPattern
			// in the
			// front window.
			int returnSpanEnd = substringSpan.getEnd();

			// shrink the span end if substring ends with splitPattern
			boolean shrinkSpanEnd = false;
			StringBuffer reverseStringBuffer = new StringBuffer(substring).reverse();
			String reverseSubstring = reverseStringBuffer.toString();

			matcher = splitPattern.matcher(reverseSubstring);
			if (matcher.find()) {
				if (matcher.start() == 0) {
					returnSpanEnd -= matcher.end();
					shrinkSpanEnd = true;
				}
			}

			if (!shrinkSpanEnd) {
				int rearWindowEnd = Math.min(superString.length(), substringSpan.getEnd() + rearWindowSize);
				String rearWindow = superString.substring(substringSpan.getEnd(), rearWindowEnd);
				matcher = splitPattern.matcher(rearWindow);
				if (matcher.find()) {
					if (matcher.start() != 0) {
						returnSpanEnd = returnSpanEnd + matcher.start();
					}
				} else if (rearWindowEnd == superString.length()) {
					returnSpanEnd = rearWindowEnd;
				}

			}
			return new Span(returnSpanStart, returnSpanEnd);
		} catch (InvalidSpanException ise) {
			return substringSpan;
		}

	}

	public static Span shrinkRight(String superString, Span span, Pattern splitPattern) {
		if (!(new Span(0, superString.length()).contains(span))) {
			return null;
		}

		String spannedString = Span.substring(superString, span);
		Matcher matcher = splitPattern.matcher(spannedString);
		int lastIndex = -1;
		while (matcher.find()) {
			lastIndex = matcher.start();
		}

		if (lastIndex == -1)
			return span;

		lastIndex = span.getStart() + lastIndex;
		if (lastIndex > span.getStart() && lastIndex < span.getEnd())
			return new Span(span.getStart(), lastIndex);

		return span;
	}

	public static Span shrinkLeft(String superString, Span span, Pattern splitPattern) {
		if (!(new Span(0, superString.length()).contains(span))) {
			return null;
		}

		String spannedString = Span.substring(superString, span);
		Matcher matcher = splitPattern.matcher(spannedString);
		int lastIndex = -1;
		if (matcher.find()) {
			lastIndex = matcher.end();
		}

		if (lastIndex == -1)
			return span;

		lastIndex = span.getStart() + lastIndex;
		if (lastIndex > span.getStart() && lastIndex < span.getEnd())
			return new Span(lastIndex, span.getEnd());

		return span;
	}

	public Comparator<SimpleInstance> comparator(final Comparator<SimpleInstance> noSpansComparator) {
		return new Comparator<SimpleInstance>() {
			public int compare(SimpleInstance annotation1, SimpleInstance annotation2) {
				List<Span> spans1;
				try {
					spans1 = new ArrayList<Span>(annotationUtil.getSpans(annotation1));
					if (spans1.size() == 0) {
						Span referencedSpan = getAReferencedSpan(annotation1);
						if (referencedSpan != null)
							spans1.add(referencedSpan);
					}
				} catch (InvalidSpanException ise) {
					spans1 = new ArrayList<Span>();
				}
				List<Span> spans2;
				try {
					spans2 = new ArrayList<Span>(annotationUtil.getSpans(annotation2));
					if (spans2.size() == 0) {
						Span referencedSpan = getAReferencedSpan(annotation2);
						if (referencedSpan != null)
							spans2.add(referencedSpan);
					}

				} catch (InvalidSpanException ise) {
					spans2 = new ArrayList<Span>();
				}
				if (spans1.size() == 0 && spans2.size() == 0 && noSpansComparator != null) {
					return noSpansComparator.compare(annotation1, annotation2);
				} else if (spans1.size() == 0) {
					return 1;
				} else if (spans2.size() == 0) {
					return -1;
				} else {
					int comparison = spans1.get(0).compareTo(spans2.get(0));
					if (comparison == 0 && noSpansComparator != null) {
						return noSpansComparator.compare(annotation1, annotation2);
					} else
						return comparison;
				}
			}
		};
	}

	public Comparator lengthComparator() {
		return new Comparator<SimpleInstance>() {
			public int compare(SimpleInstance annotation1, SimpleInstance annotation2) {
				List<Span> spans1 = annotationUtil.getSpans(annotation1);
				List<Span> spans2 = annotationUtil.getSpans(annotation2);
				if (spans1.size() == 0) {
					return -1;
				} else if (spans2.size() == 0) {
					return 1;
				} else {
					int length1 = spansLength(spans1);
					int length2 = spansLength(spans2);

					if (length1 <= length2)
						return -1;
					else
						return 1;
				}
			}
		};
	}

	public int spansLength(List<Span> spans) {
		int length = 0;
		for (Span span : spans) {
			length += span.getSize();
		}
		return length;
	}

	public Span getAReferencedSpan(SimpleInstance annotation) {
		List<Span> returnValues = new ArrayList<Span>();
		Set<SimpleInstance> referencedAnnotations = annotationUtil.getRelatedAnnotations(annotation);
		for (SimpleInstance referencedAnnotation : referencedAnnotations) {
			List<Span> spans = annotationUtil.getSpans(referencedAnnotation);
			returnValues.addAll(spans);

		}
		if (returnValues.size() > 0) {
			Collections.sort(returnValues);
			return returnValues.get(0);
		}
		return null;
	}

	/**
	 * 
	 * @param annotation
	 * @return the span of the annotation that has the smallest start index. If
	 *         no span exists for the annotation, then it will return the span
	 *         with the smallest start index from all of the spans of all of the
	 *         annotations (recursively) related annotations
	 */
	public Span getFirstSpan(SimpleInstance annotation) {
		List<Span> spans1 = annotationUtil.getSpans(annotation);
		if (spans1 == null || spans1.size() == 0) {
			return manager.getSpanUtil().getAReferencedSpan(annotation);
		} else {
			Collections.sort(spans1);
			return spans1.get(0);
		}
	}
}