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

package edu.uchsc.ccp.iaa.html;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.Map;
import java.util.Set;

import edu.uchsc.ccp.iaa.Annotation;
import edu.uchsc.ccp.iaa.AnnotationSpanIndex;
import edu.uchsc.ccp.iaa.IAA;
import edu.uchsc.ccp.iaa.matcher.SubclassMatcher;

public class SubclassMatcherHTML {

	public static void printIAA(IAA iaa, SubclassMatcher matcher, File directory, int numberOfDocs,
			Map<Annotation, String> annotationTexts, Map<Annotation, String> annotationTextNames) throws IOException {
		String fileName = matcher.getName();
		PrintStream html = new PrintStream(new File(directory, fileName + ".html"));

		NumberFormat percentageFormat = NumberFormat.getPercentInstance();
		percentageFormat.setMinimumFractionDigits(2);

		html.println(IAA2HTML.initHTML(matcher.getName(), matcher.getDescription()));
		html.println("<h2>" + iaa.getSetNames().size() + "-way IAA Results</h2>");

		html.println("<p>");
		html.println("<table border=1>\n");
		html.println("<tr><td><b>IAA</b></td><td><b>matches</b></td><td><b>non-matches</b></td></tr>");

		Set<String> sets = iaa.getSetNames();

		Map<String, Set<Annotation>> allwayMatches = iaa.getNontrivialAllwayMatches();
		Map<String, Set<Annotation>> allwayNonmatches = iaa.getNontrivialAllwayNonmatches();

		Set<Annotation> allwayMatchesSingleSet = IAA2HTML.getSingleSet(allwayMatches);
		Set<Annotation> allwayNonmatchesSingleSet = IAA2HTML.getSingleSet(allwayNonmatches);

		int totalAllwayMatches = allwayMatchesSingleSet.size();
		int totalAllwayNonmatches = allwayNonmatchesSingleSet.size();

		double iaaScore = (double) totalAllwayMatches / ((double) totalAllwayMatches + (double) totalAllwayNonmatches);

		html.println("<tr><td>" + percentageFormat.format(iaaScore) + "</td>" + "<td>" + totalAllwayMatches + "</td>"
				+ "<td>" + totalAllwayNonmatches + "</td></tr>");
		html.println("</table>");

		html.println("<br>IAA = matches / matches + non-matches");
		html.println("<br>IAA calculated on " + numberOfDocs + " documents.");
		html.println("<br>There are " + (totalAllwayMatches + totalAllwayNonmatches)
				+ " annotations with the class or subclass of " + matcher.getIAAClass());

		Map<String, Set<Annotation>> nonmatches = iaa.getAllwayNonmatches();
		Set<Annotation> nonmatchesSingleSet = IAA2HTML.getSingleSet(nonmatches);
		AnnotationSpanIndex spanIndex = new AnnotationSpanIndex(nonmatchesSingleSet);

		IAA2HTML.printMatchData(html, sets, fileName, directory, allwayMatches, annotationTexts, annotationTextNames,
				matcher.getSubclasses(), iaa);

		IAA2HTML.printNonmatchData(html, sets, fileName, directory, allwayNonmatches, spanIndex, annotationTexts,
				annotationTextNames, matcher.getSubclasses(), iaa);

		Map<String, Map<String, Set<Annotation>>> pairwiseMatches = iaa.getNontrivialPairwiseMatches();
		Map<String, Map<String, Set<Annotation>>> pairwiseNonmatches = iaa.getNontrivialPairwiseNonmatches();

		IAA2HTML.printPairwiseAgreement(html, sets, pairwiseMatches, pairwiseNonmatches, percentageFormat);

		html.flush();
		html.close();

	}

}