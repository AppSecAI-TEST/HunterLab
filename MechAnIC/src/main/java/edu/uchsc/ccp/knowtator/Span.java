
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
 * The Original Code is the WordFreak annotation tool.
 *
 * The Initial Developer of the Original Code is Thomas S. Morton
 * Copyright (C) 2002.  All Rights Reserved.
 * 
 * Contributor(s):
 *   Thomas S. Morton <tsmorton@cis.upenn.edu> (Original Author)
 *   Jeremy LaCivita <lacivita@linc.cis.upenn.edu>
 */

/**
 *  This code was taken from the wordfreak project and modified for Knowtator.  See
 *  license above for more information.
 *
 */
package edu.uchsc.ccp.knowtator;

import java.util.List;

public class Span implements Comparable<Span> {
	private int start;

	private int end;

	public Span(int start, int end) throws InvalidSpanException {
		this.start = start;
		this.end = end;
		if (start > end) {
			throw new InvalidSpanException(
					"Span is invalid because the start of the span is greater than the end of it: start=" + start
							+ " end=" + end);
		}
		if (start < 0) {
			throw new InvalidSpanException("Span is invalid because the start of the span is less than zero: start="
					+ start);
		}
	}

	public static boolean isValid(int start, int end) {
		if (start > end || start < 0)
			return false;
		return true;
	}

	public int length() {
		return end - start;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	/**
	 * @return the length of the span (end - start)
	 * 
	 */
	public int getSize() {
		return getEnd() - getStart();
	}

	public int compareTo(Span span) {
		if (getStart() < span.getStart()) {
			return -1;
		} else if (getStart() == span.getStart()) {
			if (getEnd() > span.getEnd()) {
				return -1;
			} else if (getEnd() < span.getEnd()) {
				return 1;
			} else {
				return 0;
			}
		} else {
			return 1;
		}
	}

	public String toString() {
		return "" + start + "|" + end;
	}

	public boolean equals(Object object) {
		if (object == null || !(object instanceof Span)) {
			return false;
		}
		Span span = (Span) object;
		return getStart() == span.getStart() && getEnd() == span.getEnd();
	}

	public int hashCode() {
		return ((this.start << 16) | (0x0000FFFF | this.end));
	}

	public boolean contains(Span span) {
		return (getStart() <= span.getStart() && span.getEnd() <= getEnd());
	}

	public boolean contains(int i) {
		return (getStart() <= i && i < getEnd());
	}

	/**
	 * we need some junit tests
	 */
	public boolean intersects(Span span) {
		int spanStart = span.getStart();
		// either span's start is in this or this' start is in span
		return this.contains(span)
				|| span.contains(this)
				|| (getStart() <= spanStart && spanStart < getEnd() || spanStart <= getStart()
						&& getStart() < span.getEnd());
	}

	public boolean crosses(Span span) {
		int spanStart = span.getStart();

		// either s's start is in this or this' start is in s
		return !this.contains(span)
				&& !span.contains(this)
				&& (getStart() <= spanStart && spanStart < getEnd() || spanStart <= getStart()
						&& getStart() < span.getEnd());
	}

	public boolean lessThan(Span span) {
		return getStart() < span.getStart() && getEnd() < span.getEnd();
	}

	public boolean greaterThan(Span span) {
		return getStart() > span.getStart() && getEnd() > span.getEnd();
	}

	public static Span parseSpan(String spanString) throws InvalidSpanException {
		try {
			String startString = spanString.substring(0, spanString.indexOf("|"));
			String endString = spanString.substring(spanString.indexOf("|") + 1);
			int start = Integer.parseInt(startString);
			int end = Integer.parseInt(endString);
			return new Span(start, end);
		} catch (Exception exception) {
			throw new InvalidSpanException("Span is invalid because the spanString did not parse correctly", exception);
		}
	}

	public static boolean intersects(List<Span> spans1, List<Span> spans2) {
		for (Span span1 : spans1) {
			for (Span span2 : spans2) {
				if (span1.intersects(span2))
					return true;
			}
		}
		return false;
	}

	public static boolean spansMatch(List<Span> spans1, List<Span> spans2) {
		if (spans1.size() == spans2.size()) {
			for (int i = 0; i < spans1.size(); i++) {
				if (!spans1.get(i).equals(spans2.get(i))) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public static Span shortest(List<Span> spans) {
		if (spans.size() == 0)
			return null;
		if (spans.size() == 1)
			return spans.get(0);

		Span shortestSpan = spans.get(0);
		int shortestSize = shortestSpan.getSize();
		for (int i = 1; i < spans.size(); i++) {
			if (spans.get(i).getSize() < shortestSize) {
				shortestSpan = spans.get(i);
				shortestSize = shortestSpan.getSize();
			}
		}

		return shortestSpan;
	}

	/**
	 * This method takes two spans that intersect and returns a Span that
	 * "merges" the two spans. The start of the returned span is the minimum of
	 * span1.getStart and span2.getStart. The end of the returned span is the
	 * maximum of span1.getEnd and span2.getEnd
	 * 
	 * @param span1
	 * @param span2
	 * @return null if the two spans do not intersect
	 */
	public static Span merge(Span span1, Span span2) {
		if (!span1.intersects(span2))
			return null;
		else {
			return new Span(Math.min(span1.getStart(), span2.getStart()), Math.max(span1.getEnd(), span2.getEnd()));
		}
	}

	public static String substring(String string, Span span) {
		int start = Math.max(0, span.getStart());
		start = Math.min(start, string.length());
		int end = Math.max(0, span.getEnd());
		end = Math.min(end, string.length());
		return string.substring(start, end);
	}

}