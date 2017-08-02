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
package edu.uchsc.ccp.knowtator.stats;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.uchsc.ccp.iaa.Annotation;
import edu.uchsc.ccp.iaa.IAA;
import edu.uchsc.ccp.iaa.IAAException;
import edu.uchsc.ccp.iaa.html.IAA2HTML;
import edu.uchsc.ccp.iaa.html.SpanMatcherHTML;
import edu.uchsc.ccp.iaa.html.SubclassMatcherHTML;
import edu.uchsc.ccp.iaa.matcher.ClassAndSpanMatcher;
import edu.uchsc.ccp.iaa.matcher.ClassHierarchy;
import edu.uchsc.ccp.iaa.matcher.ClassHierarchyImpl;
import edu.uchsc.ccp.iaa.matcher.ClassMatcher;
import edu.uchsc.ccp.iaa.matcher.ComplexFeatureMatchCriteria;
import edu.uchsc.ccp.iaa.matcher.FeatureMatcher;
import edu.uchsc.ccp.iaa.matcher.SpanMatcher;
import edu.uchsc.ccp.iaa.matcher.SubclassMatcher;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.FilterUtil;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.MentionUtil;
import edu.uchsc.ccp.knowtator.Span;
import edu.uchsc.ccp.knowtator.textsource.TextSource;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;
import edu.uchsc.ccp.knowtator.textsource.TextSourceCollection;

public class KnowtatorIAA {
	File outputDirectory;

	SimpleInstance filter;

	Collection<SimpleInstance> textSources;

	Collection<Slot> simpleFeatureSlots;

	Collection<Slot> complexFeatureSlots;

	Project project;

	KnowtatorManager manager;

	KnowtatorProjectUtil kpu;

	TextSourceCollection tsc;

	AnnotationUtil annotationUtil;

	MentionUtil mentionUtil;

	FilterUtil filterUtil;

	Map<Annotation, String> annotationTexts;

	Map<Annotation, String> annotationTextNames;

	Map<String, Set<Annotation>> textSourceAnnotationsMap;

	PrintStream html;

	boolean setNameDeterminedByAnnotators;

	Set<String> setNames;

	public KnowtatorIAA(File outputDirectory, SimpleInstance filter, Collection<SimpleInstance> textSources,
			Project project, Collection<Slot> simpleFeatureSlots, Collection<Slot> complexFeatureSlots,
			KnowtatorManager manager, TextSourceCollection tsc, AnnotationUtil annotationUtil, MentionUtil mentionUtil,
			FilterUtil filterUtil) throws IAAException

	{
		this.outputDirectory = outputDirectory;
		this.filter = filter;
		this.textSources = textSources;
		if (simpleFeatureSlots != null)
			this.simpleFeatureSlots = simpleFeatureSlots;
		else
			this.simpleFeatureSlots = new HashSet<Slot>();
		if (complexFeatureSlots != null)
			this.complexFeatureSlots = complexFeatureSlots;
		else
			this.complexFeatureSlots = new HashSet<Slot>();

		this.project = project;
		this.manager = manager;
		this.tsc = tsc;
		this.annotationUtil = annotationUtil;
		this.mentionUtil = mentionUtil;
		this.filterUtil = filterUtil;

		kpu = manager.getKnowtatorProjectUtil();
		annotationTexts = new HashMap<Annotation, String>();
		annotationTextNames = new HashMap<Annotation, String>();

		setNameDeterminedByAnnotators = FilterUtil.getAnnotators(filter).size() > 1 ? true : false;
		initSetNames();
		initTextSourceAnnotations();
		initHTML();
	}

	private void initHTML() throws IAAException {
		try {
			html = new PrintStream(new File(outputDirectory, "index.html"));
			html.println("<html><head><title>Inter-Annotator Agreement</title></head>");
			html.println("<body><ul>");
		} catch (IOException ioe) {
			throw new IAAException(ioe);
		}
	}

	public void closeHTML() {
		html.println("</ul>");
		html.println("</body></html>");
		html.flush();
		html.close();
	}

	private void initTextSourceAnnotations() throws IAAException {
		textSourceAnnotationsMap = new HashMap<String, Set<Annotation>>();
		for (SimpleInstance textSourceInstance : textSources) {
			Collection<SimpleInstance> tsAnnotations = annotationUtil.getAnnotations(textSourceInstance);
			tsAnnotations = filterUtil.filterAnnotations(tsAnnotations, filter);
			Set<Annotation> annotations = convertAnnotations(tsAnnotations);
			textSourceAnnotationsMap.put(textSourceInstance.getName(), annotations);
		}
	}

	private void initSetNames() {
		setNames = new HashSet<String>();
		Set<SimpleInstance> setNameInstances;
		if (setNameDeterminedByAnnotators)
			setNameInstances = new HashSet<SimpleInstance>(FilterUtil.getAnnotators(filter));
		else
			setNameInstances = FilterUtil.getSets(filter);

		for (SimpleInstance setNameInstance : setNameInstances) {
			setNames.add(setNameInstance.getBrowserText());
		}
	}

	private String getAnnotationSetName(SimpleInstance knowtatorAnnotation) {
		if (setNameDeterminedByAnnotators) {
			String annotatorName = annotationUtil.getAnnotator(knowtatorAnnotation).getBrowserText();
			return annotatorName;
		} else {
			Set<SimpleInstance> sets = annotationUtil.getSets(knowtatorAnnotation);
			for (SimpleInstance set : sets) {
				String setName = set.getBrowserText();
				if (setNames.contains(setName))
					return setName;
			}
		}
		return null;
	}

	public Map<Annotation, String> getAnnotationTexts() {
		return annotationTexts;
	}

	public Annotation convertAnnotation(SimpleInstance knowtatorAnnotation, boolean convertComplexFeatures)
			throws IAAException {
		try {
			Annotation annotation = new Annotation();

			SimpleInstance textSourceInstance = annotationUtil.getTextSource(knowtatorAnnotation);
			TextSource textSource = tsc.get(textSourceInstance.getName());

			annotationTexts.put(annotation, textSource.getText());
			annotationTextNames.put(annotation, textSource.getName());
			annotation.setDocID(textSource.getName());

			List<Span> knowtatorSpans = annotationUtil.getSpans(knowtatorAnnotation);
			List<edu.uchsc.ccp.iaa.Span> iaaSpans = new ArrayList<edu.uchsc.ccp.iaa.Span>(knowtatorSpans.size());
			for (Span knowtatorSpan : knowtatorSpans) {
				edu.uchsc.ccp.iaa.Span iaaSpan = new edu.uchsc.ccp.iaa.Span(knowtatorSpan.getStart(), knowtatorSpan
						.getEnd());
				iaaSpans.add(iaaSpan);
			}
			annotation.setSpans(iaaSpans);

			String annotationSetName = getAnnotationSetName(knowtatorAnnotation);
			annotation.setSetName(annotationSetName);

			SimpleInstance mention = annotationUtil.getMention(knowtatorAnnotation);
			Cls mentionType = mentionUtil.getMentionCls(mention);
			if (mentionType != null)
				annotation.setAnnotationClass(mentionType.getBrowserText());

			for (Slot simpleFeatureSlot : simpleFeatureSlots) {
				SimpleInstance slotMention = mentionUtil.getSlotMention(mention, simpleFeatureSlot);
				if (slotMention != null && mentionUtil.isSimpleSlotMention(slotMention)) {
					List<Object> values = mentionUtil.getSlotMentionValues(slotMention);
					Set<Object> valuesSet = new HashSet<Object>(values);
					annotation.setSimpleFeature(simpleFeatureSlot.getBrowserText(), valuesSet);
				}
				if (slotMention != null && !mentionUtil.isSimpleSlotMention(slotMention)) {
					throw new IAAException("The slot " + simpleFeatureSlot.getBrowserText()
							+ " in slot matcher config is not a 'simple' slot.");
				}
			}

			if (convertComplexFeatures) {
				for (Slot complexFeatureSlot : complexFeatureSlots) {
					List<SimpleInstance> relatedMentions = mentionUtil.getRelatedMentions(mention, complexFeatureSlot);
					Set<Annotation> featureAnnotations = new HashSet<Annotation>();
					for (SimpleInstance relatedMention : relatedMentions) {
						SimpleInstance relatedAnnotation = mentionUtil.getMentionAnnotation(relatedMention);
						featureAnnotations.add(convertAnnotation(relatedAnnotation, false));
					}
					annotation.setComplexFeature(complexFeatureSlot.getBrowserText(), featureAnnotations);
				}
			}
			return annotation;
		} catch (TextSourceAccessException tsae) {
			throw new IAAException(tsae);
		}
	}

	private Set<Annotation> convertAnnotations(Collection<SimpleInstance> knowtatorAnnotations) throws IAAException {
		Set<Annotation> annotations = new HashSet<Annotation>();
		for (SimpleInstance knowtatorAnnotation : knowtatorAnnotations) {
			annotations.add(convertAnnotation(knowtatorAnnotation, true));
		}
		return annotations;
	}

	private static int convertMatchSpans(String matchSpans) throws IAAException {
		if (matchSpans.equals("SpansMatchExactly"))
			return Annotation.SPANS_EXACT_COMPARISON;
		else if (matchSpans.equals("SpansOverlap"))
			return Annotation.SPANS_OVERLAP_COMPARISON;
		else if (matchSpans.equals("IgnoreSpans"))
			return Annotation.IGNORE_SPANS_COMPARISON;
		else
			throw new IAAException(
					"Span match criteria of slot matcher must be one of SpansMatchExactly, SpansOverlap, or IgnoreSpans");
	}

	public static FeatureMatcher createFeatureMatcher(SimpleInstance slotMatcherConfig, KnowtatorProjectUtil kpu,
			String matcherName) throws IAAException {
		FeatureMatcher featureMatcher = new FeatureMatcher(matcherName);
		if (!slotMatcherConfig.getDirectType().equals(kpu.getSlotMatcherConfigCls()))
			throw new IAAException("Unable to create slot matcher from instance='" + slotMatcherConfig.getBrowserText()
					+ "'");

		Boolean matchClasses = (Boolean) slotMatcherConfig.getOwnSlotValue(kpu.getClassMatchCriteriaSlot());
		if (matchClasses != null)
			featureMatcher.setMatchClasses(matchClasses.booleanValue());
		else
			featureMatcher.setMatchClasses(false);

		String matchSpans = (String) slotMatcherConfig.getOwnSlotValue(kpu.getSpanMatchCriteriaSlot());
		if (matchSpans != null) {
			featureMatcher.setMatchSpans(convertMatchSpans(matchSpans));
		} else
			throw new IAAException("Slot matcher must specify how to compare spans.");

		Collection<SimpleInstance> slotMatchCriteria = (Collection<SimpleInstance>) slotMatcherConfig
				.getOwnSlotValues(kpu.getSlotMatchCriteriaSlot());

		for (SimpleInstance slotMatchCriterium : slotMatchCriteria) {
			if (slotMatchCriterium.getDirectType().equals(kpu.getSimpleSlotMatchCriteriaCls())) {
				Slot slotMatcherSlot = (Slot) slotMatchCriterium.getOwnSlotValue(kpu.getSlotMatcherSlotSlot());
				featureMatcher.addComparedSimpleFeatures(slotMatcherSlot.getBrowserText());
			} else if (slotMatchCriterium.getDirectType().equals(kpu.getComplexSlotMatchCriteriaCls())) {
				Slot slotMatcherSlot = (Slot) slotMatchCriterium.getOwnSlotValue(kpu.getSlotMatcherSlotSlot());
				Boolean b = (Boolean) slotMatchCriterium.getOwnSlotValue(kpu.getClassMatchCriteriaSlot());
				boolean matchSlotClasses = b != null ? b.booleanValue() : false;

				String str = (String) slotMatchCriterium.getOwnSlotValue(kpu.getSpanMatchCriteriaSlot());
				if (str == null)
					throw new IAAException("Slot matcher must specify how to compare spans of complex slot "
							+ slotMatcherSlot.getBrowserText());
				int matchSlotSpans = convertMatchSpans(str);

				Collection<Slot> comparedSimpleSlots = (Collection<Slot>) slotMatchCriterium.getOwnSlotValues(kpu
						.getSlotMatcherSimpleSlotsSlot());
				Set<String> comparedSimpleFeatures = new HashSet<String>();
				for (Slot comparedSimpleSlot : comparedSimpleSlots) {
					comparedSimpleFeatures.add(comparedSimpleSlot.getBrowserText());
				}

				Boolean propogateTrivialMatch = (Boolean) slotMatchCriterium.getOwnSlotValue(kpu
						.getPropogateTrivialMatchSlot());
				boolean trivialSimpleFeatureMatchesCauseTrivialMatch = propogateTrivialMatch != null ? propogateTrivialMatch
						.booleanValue()
						: false;

				ComplexFeatureMatchCriteria matchCriteria = new ComplexFeatureMatchCriteria(matchSlotClasses,
						matchSlotSpans, comparedSimpleFeatures, trivialSimpleFeatureMatchesCauseTrivialMatch);
				featureMatcher.addComparedComplexFeature(slotMatcherSlot.getBrowserText(), matchCriteria);
			}
		}

		return featureMatcher;
	}

	public IAA runFeatureMatcherIAA(SimpleInstance slotMatcherConfig) throws IAAException {
		return runFeatureMatcherIAA(slotMatcherConfig, "Feature Matcher");
	}

	public IAA runFeatureMatcherIAA(SimpleInstance slotMatcherConfig, String matcherName) throws IAAException {
		try {
			FeatureMatcher featureMatcher = createFeatureMatcher(slotMatcherConfig, kpu, matcherName);
			IAA featureIAA = new IAA(setNames);
			for (Set<Annotation> annotations : textSourceAnnotationsMap.values()) {
				featureIAA.setAnnotations(annotations);
				featureIAA.allwayIAA(featureMatcher);
				featureIAA.pairwiseIAA(featureMatcher);
			}

			IAA2HTML.printIAA(featureIAA, featureMatcher, outputDirectory, textSources.size(), annotationTexts,
					annotationTextNames);
			html.println("<li><a href=\"" + featureMatcher.getName() + ".html\">" + featureMatcher.getName()
					+ "</a></li>");
			return featureIAA;
		} catch (Exception exception) {
			throw new IAAException(exception);
		}
	}

	public IAA runClassIAA() throws IAAException {
		try {
			ClassMatcher classMatcher = new ClassMatcher();
			IAA classIAA = new IAA(setNames);

			for (Set<Annotation> annotations : textSourceAnnotationsMap.values()) {
				classIAA.setAnnotations(annotations);
				classIAA.allwayIAA(classMatcher);
				classIAA.pairwiseIAA(classMatcher);
			}

			IAA2HTML.printIAA(classIAA, classMatcher, outputDirectory, textSources.size(), annotationTexts,
					annotationTextNames);
			html.println("<li><a href=\"" + classMatcher.getName() + ".html\">" + classMatcher.getName() + "</a></li>");
			return classIAA;
		} catch (Exception e) {
			throw new IAAException(e);
		}
	}

	public IAA runSpanIAA() throws IAAException {
		try {
			SpanMatcher spanMatcher = new SpanMatcher();
			IAA spanIAA = new IAA(setNames);

			for (Set<Annotation> annotations : textSourceAnnotationsMap.values()) {
				spanIAA.setAnnotations(annotations);
				spanIAA.allwayIAA(spanMatcher);
				spanIAA.pairwiseIAA(spanMatcher);
			}
			SpanMatcherHTML.printIAA(spanIAA, spanMatcher, outputDirectory, textSources.size(), annotationTexts,
					annotationTextNames);
			html.println("<li><a href=\"" + spanMatcher.getName() + ".html\">" + spanMatcher.getName() + "</a></li>");
			return spanIAA;
		} catch (Exception e) {
			throw new IAAException(e);
		}
	}

	public IAA runClassAndSpanIAA() throws IAAException {
		try {
			ClassAndSpanMatcher classAndSpanMatcher = new ClassAndSpanMatcher();
			IAA classAndSpanIAA = new IAA(setNames);

			for (Set<Annotation> annotations : textSourceAnnotationsMap.values()) {
				classAndSpanIAA.setAnnotations(annotations);
				classAndSpanIAA.allwayIAA(classAndSpanMatcher);
				classAndSpanIAA.pairwiseIAA(classAndSpanMatcher);
			}
			IAA2HTML.printIAA(classAndSpanIAA, classAndSpanMatcher, outputDirectory, textSources.size(),
					annotationTexts, annotationTextNames);
			html.println("<li><a href=\"" + classAndSpanMatcher.getName() + ".html\">" + classAndSpanMatcher.getName()
					+ "</a></li>");
			return classAndSpanIAA;
		} catch (Exception e) {
			throw new IAAException(e);
		}
	}

	public void runSubclassIAA() throws IAAException {
		try {
			Set<Cls> topLevelClses = getTopLevelClses();
			Set<Cls> parentClses = new HashSet<Cls>();
			for (Cls topLevelCls : topLevelClses) {
				parentClses.add(topLevelCls);
				Collection subclasses = topLevelCls.getSubclasses();
				if (subclasses != null) {
					Iterator subclassesItr = subclasses.iterator();
					while (subclassesItr.hasNext()) {
						Cls subclass = (Cls) subclassesItr.next();
						Collection subsubclasses = subclass.getSubclasses();
						if (subsubclasses != null && subsubclasses.size() > 0) {
							parentClses.add(subclass);
						}
					}
				}
			}

			html.println("<li><a href=\"subclassMatcher.html\">subclass matcher</a></li>");

			PrintStream subclassHTML = new PrintStream(new File(outputDirectory, "subclassMatcher.html"));
			subclassHTML.println(IAA2HTML.initHTML("Subclass Matcher", ""));
			subclassHTML.println("Subclass matcher");
			subclassHTML.println("<table border=1>\n");
			subclassHTML
					.println("<tr><td><b>Class</b></td><td><b>IAA</b></td><td><b>matches</b></td><td><b>non-matches</b></td></tr>");

			SubclassMatcher subclassMatcher = new SubclassMatcher(createClassHierarchy(topLevelClses));
			edu.uchsc.ccp.iaa.IAA subclassIAA = new edu.uchsc.ccp.iaa.IAA(setNames);

			NumberFormat percentageFormat = NumberFormat.getPercentInstance();
			percentageFormat.setMinimumFractionDigits(2);

			for (Cls parentCls : parentClses) {
				calculateSubclassIAA(parentCls, subclassMatcher, subclassIAA, textSourceAnnotationsMap);
				SubclassMatcherHTML.printIAA(subclassIAA, subclassMatcher, outputDirectory, textSources.size(),
						annotationTexts, annotationTextNames);

				Map<String, Set<Annotation>> allwayMatches = subclassIAA.getNontrivialAllwayMatches();
				Set<Annotation> matches = IAA2HTML.getSingleSet(allwayMatches);

				Map<String, Set<Annotation>> allwayNonmatches = subclassIAA.getNontrivialAllwayNonmatches();
				Set<Annotation> nonmatches = IAA2HTML.getSingleSet(allwayNonmatches);

				double subclsIAA = (double) matches.size() / ((double) matches.size() + (double) nonmatches.size());

				subclassHTML.println("<tr><td><a href=\"" + subclassMatcher.getName() + ".html\">"
						+ parentCls.getName() + "</a></td>" + "<td>" + percentageFormat.format(subclsIAA) + "</td><td>"
						+ matches.size() + "</td><td>" + nonmatches.size() + "</td></tr>");
			}
			subclassHTML.println("</table>");
			subclassHTML.println("</body></html>");
			subclassHTML.flush();
			subclassHTML.close();
		} catch (Exception e) {
			throw new IAAException(e);
		}
	}

	private static void calculateSubclassIAA(Cls cls, SubclassMatcher subclassMatcher,
			edu.uchsc.ccp.iaa.IAA subclassIAA, Map<String, Set<Annotation>> textSourceAnnotationsMap)
			throws edu.uchsc.ccp.iaa.IAAException {
		subclassIAA.reset();
		subclassMatcher.setIAAClass(cls.getName());
		for (Set<Annotation> annotations : textSourceAnnotationsMap.values()) {
			subclassIAA.setAnnotations(annotations);
			subclassIAA.allwayIAA(subclassMatcher);
			subclassIAA.pairwiseIAA(subclassMatcher);
		}
	}

	private Set<Cls> getTopLevelClses() {
		Set<Cls> topLevelClses = new HashSet<Cls>(FilterUtil.getTypes(filter));
		if (topLevelClses.size() == 0) {
			topLevelClses.addAll(manager.getRootClses());
		}
		return topLevelClses;
	}

	private static ClassHierarchy createClassHierarchy(Set<Cls> topLevelClses) {
		Map<String, Set<String>> subclassMap = new HashMap<String, Set<String>>();
		for (Cls topLevelCls : topLevelClses) {
			populateSubclassMap(topLevelCls, subclassMap);
		}
		return new ClassHierarchyImpl(subclassMap);
	}

	private static void populateSubclassMap(Cls cls, Map<String, Set<String>> subclassMap) {
		String clsName = cls.getName();
		if (!subclassMap.containsKey(clsName)) {
			Collection subclses = cls.getDirectSubclasses();
			if (subclses != null && subclses.size() > 0) {
				subclassMap.put(clsName, new HashSet<String>());
				for (Iterator subclsItr = subclses.iterator(); subclsItr.hasNext();) {
					Cls subcls = (Cls) subclsItr.next();
					String subclsName = subcls.getName();
					subclassMap.get(clsName).add(subclsName);
					populateSubclassMap(subcls, subclassMap);
				}
			}
		}
	}

	public static Set<Slot> getSimpleSlotsFromMatcherConfig(SimpleInstance slotMatcherConfig, KnowtatorProjectUtil kpu) {
		Set<Slot> returnValues = new HashSet<Slot>();

		Collection<SimpleInstance> slotMatchCriteria = (Collection<SimpleInstance>) slotMatcherConfig
				.getOwnSlotValues(kpu.getSlotMatchCriteriaSlot());

		for (SimpleInstance slotMatchCriterium : slotMatchCriteria) {
			if (slotMatchCriterium.getDirectType().equals(kpu.getSimpleSlotMatchCriteriaCls())) {
				Slot slotMatcherSlot = (Slot) slotMatchCriterium.getOwnSlotValue(kpu.getSlotMatcherSlotSlot());
				returnValues.add(slotMatcherSlot);
			} else if (slotMatchCriterium.getDirectType().equals(kpu.getComplexSlotMatchCriteriaCls())) {
				Collection<Slot> comparedSimpleSlots = (Collection<Slot>) slotMatchCriterium.getOwnSlotValues(kpu
						.getSlotMatcherSimpleSlotsSlot());
				if (comparedSimpleSlots != null)
					returnValues.addAll(comparedSimpleSlots);
			}
		}
		return returnValues;
	}

	public static Set<Slot> getComplexSlotsFromMatcherConfig(SimpleInstance slotMatcherConfig, KnowtatorProjectUtil kpu) {
		Set<Slot> returnValues = new HashSet<Slot>();

		Collection<SimpleInstance> slotMatchCriteria = (Collection<SimpleInstance>) slotMatcherConfig
				.getOwnSlotValues(kpu.getSlotMatchCriteriaSlot());

		for (SimpleInstance slotMatchCriterium : slotMatchCriteria) {
			if (slotMatchCriterium.getDirectType().equals(kpu.getComplexSlotMatchCriteriaCls())) {
				Slot slotMatcherSlot = (Slot) slotMatchCriterium.getOwnSlotValue(kpu.getSlotMatcherSlotSlot());
				returnValues.add(slotMatcherSlot);
			}
		}
		return returnValues;
	}

	public Map<String, Set<Annotation>> getTextSourceAnnotationsMap() {
		return textSourceAnnotationsMap;
	}

}
