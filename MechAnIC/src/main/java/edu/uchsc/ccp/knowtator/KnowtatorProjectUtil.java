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

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import edu.stanford.smi.protege.model.BrowserSlotPattern;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.uchsc.ccp.knowtator.util.ProjectUtil;
import org.semanticweb.owlapi.model.OWLClass;

@SuppressWarnings("ALL")
public class KnowtatorProjectUtil {
	public static final String KNOWTATOR_SUPPORT_CLS_NAME = "knowtator support class";

	public static final String KNOWTATOR_VERSION_CLS_NAME = "knowtator version";

	public static final String ANNOTATION_CLS_NAME = "knowtator annotation";

	public static final String ANNOTATED_MENTION_SLOT_NAME = "knowtator_annotated_mention";

	public static final String ANNOTATION_ANNOTATOR_SLOT_NAME = "knowtator_annotation_annotator";

	public static final String ANNOTATION_COMMENT_SLOT_NAME = "knowtator_annotation_comment";

	public static final String ANNOTATION_CREATION_DATE_SLOT_NAME = "knowtator_annotation_creation_date";

	public static final String ANNOTATION_SPAN_SLOT_NAME = "knowtator_annotation_span";

	public static final String ANNOTATION_TEXT_SLOT_NAME = "knowtator_annotation_text";

	public static final String ANNOTATION_TEXT_SOURCE_SLOT_NAME = "knowtator_annotation_text_source";

	public static final String SET_SLOT_NAME = "knowtator_set";

	public static final String MENTION_CLS_NAME = "knowtator mention";

	public static final String MENTION_ANNOTATION_SLOT_NAME = "knowtator_mention_annotation";

	public static final String CLASS_MENTION_CLS_NAME = "knowtator class mention";

	public static final String MENTION_CLASS_SLOT_NAME = "knowtator_mention_class";

	public static final String SLOT_MENTION_SLOT_NAME = "knowtator_slot_mention";

	public static final String INSTANCE_MENTION_CLS_NAME = "knowtator instance mention";

	public static final String MENTION_INSTANCE_SLOT_NAME = "knowtator_mention_instance";

	public static final String SLOT_MENTION_CLS_NAME = "knowtator slot mention";

	public static final String MENTION_SLOT_SLOT_NAME = "knowtator_mention_slot";

	public static final String MENTION_SLOT_VALUE_SLOT_NAME = "knowtator_mention_slot_value";

	public static final String MENTIONED_IN_SLOT_NAME = "knowtator_mentioned_in";

	public static final String COMPLEX_SLOT_MENTION_CLS_NAME = "knowtator complex slot mention";

	public static final String BOOLEAN_SLOT_MENTION_CLS_NAME = "knowtator boolean slot mention";

	public static final String FLOAT_SLOT_MENTION_CLS_NAME = "knowtator float slot mention";

	public static final String INTEGER_SLOT_MENTION_CLS_NAME = "knowtator integer slot mention";

	public static final String STRING_SLOT_MENTION_CLS_NAME = "knowtator string slot mention";

	public static final String TEXT_SOURCE_CLS_NAME = "knowtator text source";

	public static final String ANNOTATOR_CLS_NAME = "knowtator annotator";

	public static final String ANNOTATOR_ID_SLOT_NAME = "knowtator_annotator_id";

	public static final String HUMAN_ANNOTATOR_CLS_NAME = "knowtator human annotator";

	public static final String ANNOTATOR_AFFILIATION_SLOT_NAME = "knowtator_annotation_annotator_affiliation";

	public static final String ANNOTATOR_FIRST_NAME_SLOT_NAME = "knowtator_annotation_annotator_firstname";

	public static final String ANNOTATOR_LAST_NAME_SLOT_NAME = "knowtator_annotation_annotator_lastname";

	public static final String ANNOTATOR_TEAM_CLS_NAME = "knowtator annotator team";

	public static final String ANNOTATOR_TEAM_NAME_SLOT_NAME = "knowtator_annotator_team_name";

	public static final String ANNOTATOR_TEAM_MEMBERS_SLOT_NAME = "knowtator_annotator_team_members";

	public static final String DISPLAY_COLOR_CLS_NAME = "knowtator display color";

	public static final String DISPLAY_COLOR_B_SLOT_NAME = "knowtator_display_color_B";

	public static final String DISPLAY_COLOR_G_SLOT_NAME = "knowtator_display_color_G";

	public static final String DISPLAY_COLOR_NAME_SLOT_NAME = "knowtator_display_color_name";

	public static final String DISPLAY_COLOR_R_SLOT_NAME = "knowtator_display_color_R";

	public static final String SET_CLS_NAME = "knowtator set";

	public static final String SET_DESCRIPTION_SLOT_NAME = "knowtator_set_description";

	public static final String SET_NAME_SLOT_NAME = "knowtator_set_name";

	public static final String CONSENSUS_SET_CLS_NAME = "knowtator consensus set";

	public static final String CONSENSUS_SET_INDIVIDUAL_FILTER_SLOT_NAME = "knowtator_individual_filter";

	public static final String CONSENSUS_SET_CONSENSUS_FILTER_SLOT_NAME = "knowtator_consensus_filter";

	public static final String CONSENSUS_SET_TEAM_ANNOTATOR_SLOT_NAME = "knowtator_team_annotator";

	public static final String FILTER_CLS_NAME = "knowtator filter";

	public static final String FILTER_ANNOTATOR_SLOT_NAME = "knowtator_filter_annotator";

	public static final String FILTER_NAME_SLOT_NAME = "knowtator_filter_name";

	public static final String FILTER_SET_SLOT_NAME = "knowtator_filter_set";

	public static final String FILTER_TYPE_SLOT_NAME = "knowtator_filter_type";

	public static final String CONSENSUS_FILTER_CLS_NAME = "knowtator consensus filter";

	public static final String FILTER_TYPES_NOT_SELECTABLE_FROM_TEXT_VIEWER = "knowtator_types_not_selectable_from_text_viewer";

	public static final String COLOR_ASSIGNMENT_CLS_NAME = "knowtator color assignment";

	public static final String COLOR_CLASS_SLOT_NAME = "knowtator_color_class";

	public static final String DISPLAY_COLOR_SLOT_NAME = "knowtator_display_color";

	public static final String TSC_IMPLEMENTATIONS_CLS_NAME = "knowtator text source collection implementations";

	public static final String TSC_IMPLEMENTATION_SLOT_NAME = "knowtator_tsc_implementation";

	public static final String SELECTED_FILTERS_CLS_NAME = "knowtator selected filters";

	public static final String SELECTED_FILTERS_SLOT_NAME = "knowtator_selected_filters";

	public static final String SLOT_MATCHER_CONFIG_CLS_NAME = "knowtator slot matcher config";

	public static final String COMPLEX_SLOT_MATCH_CRITERIA_CLS_NAME = "knowtator complex slot match criteria";

	public static final String SIMPLE_SLOT_MATCH_CRITERIA_CLS_NAME = "knowtator simple slot match criteria";

	public static final String CLASS_MATCH_CRITERIA_SLOT_NAME = "knowtator_class_match_criteria";

	public static final String SLOT_MATCH_CRITERIA_SLOT_NAME = "knowtator_slot_match_criteria";

	public static final String SPAN_MATCH_CRITERIA_SLOT_NAME = "knowtator_span_match_criteria";

	public static final String SLOT_MATCHER_SLOT_SLOT_NAME = "knowtator_slot_matcher_slot";

	public static final String SLOT_MATCHER_SIMPLE_SLOTS_SLOT_NAME = "knowtator_slot_matcher_simple_slots";

	public static final String PROPOGATE_TRIVIAL_MATCH_SLOT_NAME = "knowtator_propogate_trivial_match";

	Cls knowtatorSupportCls;

	Cls annotationCls;

	Slot annotatedMentionSlot;

	Slot annotationAnnotatorSlot;

	Slot annotationCommentSlot;

	Slot annotationCreationDateSlot;

	Slot annotationSpanSlot;

	Slot annotationTextSlot;

	Slot annotationTextSourceSlot;

	Slot setSlot;

	Cls mentionCls;

	Slot mentionAnnotationSlot;

	Cls classMentionCls;

	Slot mentionClassSlot;

	Slot slotMentionSlot;

	Cls instanceMentionCls;

	Slot mentionInstanceSlot;

	Cls slotMentionCls;

	Slot mentionSlotSlot;

	Slot mentionSlotValueSlot;

	Slot mentionedInSlot;

	Cls complexSlotMentionCls;

	Cls booleanSlotMentionCls;

	Cls floatSlotMentionCls;

	Cls integerSlotMentionCls;

	Cls stringSlotMentionCls;

	Cls textSourceCls;

	Cls annotatorCls;

	Slot annotatorIDSlot;

	Cls humanAnnotatorCls;

	Slot annotatorAffiliationSlot;

	Slot annotatorFirstNameSlot;

	Slot annotatorLastNameSlot;

	Cls teamAnnotatorCls;

	Slot annotatorTeamNameSlot;

	Slot annotatorTeamMembersSlot;

	Cls displayColorCls;

	Slot displayColorBSlot;

	Slot displayColorGSlot;

	Slot displayColorNameSlot;

	Slot displayColorRSlot;

	Cls setCls;

	Slot setDescriptionSlot;

	Slot setNameSlot;

	Cls consensusSetCls;

	Slot consensusSetIndividualFilterSlot;

	Slot consensusSetConsensusFilterSlot;

	Slot consensusSetTeamAnnotatorSlot;

	Cls filterCls;

	Slot filterAnnotatorSlot;

	Slot filterNameSlot;

	Slot filterSetSlot;

	Slot filterTypeSlot;

	Slot filterTypesNotSelectableFromTextViewerSlot;

	Cls consensusFilterCls;

	Cls colorAssignmentCls;

	Slot colorClassSlot;

	Slot displayColorSlot;

	Cls tscImplementationsCls;

	Slot tscImplementationsSlot;

	Cls selectedFiltersCls;

	Slot selectedFiltersSlot;

	Cls complexSlotMatchCriteriaCls;

	Slot classMatchCriteriaSlot;

	Slot propogateTrivialMatchSlot;

	Slot slotMatcherSimpleSlotsSlot;

	Slot slotMatcherSlotSlot;

	Slot spanMatchCriteriaSlot;

	Cls simpleSlotMatchCriteriaCls;

	Cls slotMatcherConfigCls;

	Slot slotMatchCriteriaSlot;

	public static final String CONFIGURATION_CLS_NAME = "knowtator configuration";

	public static final String ACTIVE_FILTERS_SLOT_NAME = "knowtator_active_filters";

	public static final String COLOR_ASSIGNMENTS_SLOT_NAME = "knowtator_color_assignments";

	public static final String ROOT_CLSES_SLOT_NAME = "knowtator_root_clses";

	public static final String SELECTED_ANNOTATION_SET_SLOT_NAME = "knowtator_selected_annotation_set";

	public static final String SELECTED_ANNOTATOR_SLOT_NAME = "knowtator_selected_annotator";

	public static final String SELECTED_FILTER_SLOT_NAME = "knowtator_selected_filter";

	public static final String TOKEN_REGEX_SLOT_NAME = "knowtator_token_regex";

	public static final String FADE_UNSELECTED_ANNOTATIONS = "knowtator_fade_unselected_annotations";

	public static final String CONSENSUS_ACCEPT_RECURSIVE = "knowtator_consensus_accept_recursive";

	public static final String SUBTEXT_SLOT_SLOT_NAME = "knowtator_subtext_slot";
	
	public static final String FAST_ANNOTATE_SLOT_NAME = "knowtator_fast_annotate";
	
	public static final String NEXT_ANNOTATION_ON_DELETE = "knowtator_next_annotation_on_delete";

	Cls configurationCls;

	Slot activeFiltersSlot;

	Slot colorAssignmentsSlot;

	Slot rootClsesSlot;

	Slot selectedAnnotationSetSlot;

	Slot selectedAnnotatorSlot;

	Slot selectedFilterSlot;

	Slot tokenRegexSlot;

	Slot fadeUnselectedAnnotationsSlot;
	
	Slot consensusAcceptRecursiveSlot;

	Slot subtextSlotSlot;

	Slot fastAnnotateSlot;
	
	Slot nextAnnotationOnDeleteSlot;
	
	public static final String KNOWTATOR_SUPPORT_CLASSES_VERSION = "KNOWTATOR_SUPPORT_CLASSES_VERSION";

	public static final String UNVERSIONED = "UNVERSIONED";

	public static final String V001 = "V001";

	public static final String V002 = "V002";

	public static final String V003 = "V003";

	public static final String V004 = "V004";

	public static final String V005 = "V005";

	public static final String V006 = "V006";

	public static final String V007 = "V007";

	public static final String V008 = "V008";

	public static final String V009 = "V009";

	public static final String CURRENT_VERSION = V009;

	public static final String VERSION_BEING_UPDATED = "VERSION_BEING_UPDATED";

	private static final Set<String> VALID_VERSIONS = new HashSet<String>();
	static {
		for (String version : new String[] { V001, V002, V003, V004, V005, V006, V007, V008, V009 })
			VALID_VERSIONS.add(version);
	}

	KnowledgeBase kb;

	String version = null;

	Logger logger = Logger.getLogger(KnowtatorProjectUtil.class);

	public KnowtatorProjectUtil(KnowledgeBase kb) {
		this(kb, null);
	}

	public KnowledgeBase getKnowledgeBase() {
		return kb;
	}

	/** Creates a new instance of KnowtatorKBUtil */
	public KnowtatorProjectUtil(KnowledgeBase kb, Component parent) {
		this.kb = kb;
		version = getKnowtatorSupportClassesVersion();
		if (!isKnowtatorSupportClassesCurrent()) {
			logger.info("knowtator.pprj version is out of date: expected = " + CURRENT_VERSION + ", actual = "
					+ getKnowtatorSupportClassesVersion());
		} else { 
			logger.info("knowtator.pprj version is up-to-date: " + getKnowtatorSupportClassesVersion());
		}
		initKnowtatorFrames();
		initConfiguration();
	}

	private void initConfiguration() {
		logger.debug("");
		SimpleInstance configuration = ProjectSettings.getActiveConfiguration(kb.getProject());
		if (configuration == null) {
			configuration = (SimpleInstance) kb.createInstance(null, configurationCls);
			ProjectSettings.setActiveConfiguration(kb.getProject(), configuration);
			JOptionPane.showMessageDialog(null, "The configuration information for Knowtator has been reset.\n"
					+ "To create color assignments, set the default annotator, or \n"
					+ "specify root classes please select Menu->Knowtator->Configure.\n"
					+ "For additional help please consult the online documentation at:\n"
					+ "http://knowtator.sourceforge.net//configure.shtml", "Configuration Reset",
					JOptionPane.INFORMATION_MESSAGE);
			ProjectUtil.saveProject(kb.getProject());
			logger.debug("added new configuration and saved project");
		} else {
			logger.debug("annotation project has an active configuration: " + configuration.getName());
		}
	}

	public String getKnowtatorSupportClassesVersion() {
		logger.debug("");
		if (version != null)
			return version;
		else
			return getKnowtatorSupportClassesVersion(kb);
	}
	
	public static String getKnowtatorSupportClassesVersion(KnowledgeBase kb) {
		Instance versionInstance = kb.getInstance("knowtator_Instance_20002");
		if (versionInstance == null) {
			Cls knowtatorVersionCls = kb.getCls(KNOWTATOR_VERSION_CLS_NAME);
			if (knowtatorVersionCls != null) {
				Collection versionInstances = kb.getInstances();
				if (versionInstances != null && versionInstances.size() > 0) {
					versionInstance = (SimpleInstance) CollectionUtilities.getFirstItem(versionInstances);
				}
			}
		}
		if (versionInstance != null) {
			String version = (String) versionInstance.getDirectOwnSlotValue(kb.getSlot("version"));
			if (VALID_VERSIONS.contains(version)) {
				return version;
			}
		}
		return UNVERSIONED;
	}
	
	
	public boolean isKnowtatorSupportClassesCurrent() {
		String version = getKnowtatorSupportClassesVersion();
		return version.equals(CURRENT_VERSION);
	}
	
	public static boolean isKnowtatorSupportClassesCurrent(KnowledgeBase kb) {
		String version = getKnowtatorSupportClassesVersion(kb);
		return version.equals(CURRENT_VERSION);
	}

	public static void updateKnowtatorProject(Project project) throws IOException {
		KnowledgeBase kb = project.getKnowledgeBase();
		String oldVersion = getKnowtatorSupportClassesVersion(kb);

		// this code is needed to get from V002 to V003 of knowtator.pprj
		// is safe to keep in here for any upgrade
		Cls knowtatorSelectedFilters = kb.getCls("knowtator selected filters");
		if (knowtatorSelectedFilters != null) {
			Collection selectedFiltersInstances = kb.getInstances(knowtatorSelectedFilters);
			for (Iterator it = selectedFiltersInstances.iterator(); it.hasNext();) {
				Instance instance = (Instance) it.next();
				kb.deleteInstance(instance);
			}
		}

		Slot knowtatorTextSourceAnnotationSlot = kb.getSlot("knowtator_text_source_annotation");
		if (knowtatorTextSourceAnnotationSlot != null) {
			kb.deleteSlot(knowtatorTextSourceAnnotationSlot);
		}

		// this code is needed to remove any color assignments created with
		// colors that have been removed in
		// version V003 (and replaced with 141 new colors)
		if (oldVersion.equals(V003) || oldVersion.equals(V002) || oldVersion.equals(V001) || oldVersion.equals(V001)
				|| oldVersion.equals(UNVERSIONED)) {
			Cls colorAssignmentCls = kb.getCls("knowtator color assignment");
			if (colorAssignmentCls != null) {
				Collection colorAssignments = kb.getInstances(colorAssignmentCls);
				for (Iterator it = colorAssignments.iterator(); it.hasNext();) {
					Instance instance = (Instance) it.next();
					kb.deleteInstance(instance);
				}
			}
		}
		ProjectUtil.saveProjectAs(kb.getProject(), new File(kb.getProject().getProjectURI()));
		
	}

	public void displayAnnotationAuthor() {
		Cls annotationCls = getAnnotationCls();
		BrowserSlotPattern pattern = new BrowserSlotPattern(Arrays.asList(new Object[] { getAnnotationTextSlot(), " [",
				getAnnotationAnnotatorSlot(), " : ", getSetSlot(), "]" }));
		annotationCls.setDirectBrowserSlotPattern(pattern);
	}

	public SimpleInstance getShowAllFilter() {
		SimpleInstance showAllFilter = kb.getSimpleInstance("knowtator_Instance_0");
		return showAllFilter;
	}

	public SimpleInstance getShowNoneFilter() {
		SimpleInstance showAllFilter = kb.getSimpleInstance("knowtator_Instance_20000");
		return showAllFilter;
	}

	private void initKnowtatorFrames() {
		annotationCls = kb.getCls(ANNOTATION_CLS_NAME);
		annotatedMentionSlot = kb.getSlot(ANNOTATED_MENTION_SLOT_NAME);
		annotationAnnotatorSlot = kb.getSlot(ANNOTATION_ANNOTATOR_SLOT_NAME);
		annotationCommentSlot = kb.getSlot(ANNOTATION_COMMENT_SLOT_NAME);
		annotationCreationDateSlot = kb.getSlot(ANNOTATION_CREATION_DATE_SLOT_NAME);
		annotationSpanSlot = kb.getSlot(ANNOTATION_SPAN_SLOT_NAME);
		annotationTextSlot = kb.getSlot(ANNOTATION_TEXT_SLOT_NAME);
		annotationTextSourceSlot = kb.getSlot(ANNOTATION_TEXT_SOURCE_SLOT_NAME);
		setSlot = kb.getSlot(SET_SLOT_NAME);

		mentionCls = kb.getCls(MENTION_CLS_NAME);
		mentionAnnotationSlot = kb.getSlot(MENTION_ANNOTATION_SLOT_NAME);
		classMentionCls = kb.getCls(CLASS_MENTION_CLS_NAME);
		mentionClassSlot = kb.getSlot(MENTION_CLASS_SLOT_NAME);
		slotMentionSlot = kb.getSlot(SLOT_MENTION_SLOT_NAME);
		instanceMentionCls = kb.getCls(INSTANCE_MENTION_CLS_NAME);
		mentionInstanceSlot = kb.getSlot(MENTION_INSTANCE_SLOT_NAME);
		slotMentionCls = kb.getCls(SLOT_MENTION_CLS_NAME);
		mentionSlotSlot = kb.getSlot(MENTION_SLOT_SLOT_NAME);
		mentionSlotValueSlot = kb.getSlot(MENTION_SLOT_VALUE_SLOT_NAME);
		mentionedInSlot = kb.getSlot(MENTIONED_IN_SLOT_NAME);
		complexSlotMentionCls = kb.getCls(COMPLEX_SLOT_MENTION_CLS_NAME);
		booleanSlotMentionCls = kb.getCls(BOOLEAN_SLOT_MENTION_CLS_NAME);
		floatSlotMentionCls = kb.getCls(FLOAT_SLOT_MENTION_CLS_NAME);
		integerSlotMentionCls = kb.getCls(INTEGER_SLOT_MENTION_CLS_NAME);
		stringSlotMentionCls = kb.getCls(STRING_SLOT_MENTION_CLS_NAME);

		textSourceCls = kb.getCls(TEXT_SOURCE_CLS_NAME);

		annotatorCls = kb.getCls(ANNOTATOR_CLS_NAME);
		annotatorIDSlot = kb.getSlot(ANNOTATOR_ID_SLOT_NAME);
		humanAnnotatorCls = kb.getCls(HUMAN_ANNOTATOR_CLS_NAME);
		annotatorAffiliationSlot = kb.getSlot(ANNOTATOR_AFFILIATION_SLOT_NAME);
		annotatorFirstNameSlot = kb.getSlot(ANNOTATOR_FIRST_NAME_SLOT_NAME);
		annotatorLastNameSlot = kb.getSlot(ANNOTATOR_LAST_NAME_SLOT_NAME);
		teamAnnotatorCls = kb.getCls(ANNOTATOR_TEAM_CLS_NAME);
		annotatorTeamNameSlot = kb.getSlot(ANNOTATOR_TEAM_NAME_SLOT_NAME);
		annotatorTeamMembersSlot = kb.getSlot(ANNOTATOR_TEAM_MEMBERS_SLOT_NAME);

		displayColorCls = kb.getCls(DISPLAY_COLOR_CLS_NAME);
		displayColorBSlot = kb.getSlot(DISPLAY_COLOR_B_SLOT_NAME);
		displayColorGSlot = kb.getSlot(DISPLAY_COLOR_G_SLOT_NAME);
		displayColorNameSlot = kb.getSlot(DISPLAY_COLOR_NAME_SLOT_NAME);
		displayColorRSlot = kb.getSlot(DISPLAY_COLOR_R_SLOT_NAME);

		setCls = kb.getCls(SET_CLS_NAME);
		setDescriptionSlot = kb.getSlot(SET_DESCRIPTION_SLOT_NAME);
		setNameSlot = kb.getSlot(SET_NAME_SLOT_NAME);
		consensusSetCls = kb.getCls(CONSENSUS_SET_CLS_NAME);
		consensusSetIndividualFilterSlot = kb.getSlot(CONSENSUS_SET_INDIVIDUAL_FILTER_SLOT_NAME);
		consensusSetConsensusFilterSlot = kb.getSlot(CONSENSUS_SET_CONSENSUS_FILTER_SLOT_NAME);
		consensusSetTeamAnnotatorSlot = kb.getSlot(CONSENSUS_SET_TEAM_ANNOTATOR_SLOT_NAME);

		filterCls = kb.getCls(FILTER_CLS_NAME);
		filterAnnotatorSlot = kb.getSlot(FILTER_ANNOTATOR_SLOT_NAME);
		filterNameSlot = kb.getSlot(FILTER_NAME_SLOT_NAME);
		filterSetSlot = kb.getSlot(FILTER_SET_SLOT_NAME);
		filterTypeSlot = kb.getSlot(FILTER_TYPE_SLOT_NAME);
		filterTypesNotSelectableFromTextViewerSlot = kb.getSlot(FILTER_TYPES_NOT_SELECTABLE_FROM_TEXT_VIEWER);
		consensusFilterCls = kb.getCls(CONSENSUS_FILTER_CLS_NAME);

		colorAssignmentCls = kb.getCls(COLOR_ASSIGNMENT_CLS_NAME);
		colorClassSlot = kb.getSlot(COLOR_CLASS_SLOT_NAME);
		displayColorSlot = kb.getSlot(DISPLAY_COLOR_SLOT_NAME);

		tscImplementationsCls = kb.getCls(TSC_IMPLEMENTATIONS_CLS_NAME);
		tscImplementationsSlot = kb.getSlot(TSC_IMPLEMENTATION_SLOT_NAME);

		selectedFiltersCls = kb.getCls(SELECTED_FILTERS_CLS_NAME);
		selectedFiltersSlot = kb.getSlot(SELECTED_FILTERS_SLOT_NAME);

		complexSlotMatchCriteriaCls = kb.getCls(COMPLEX_SLOT_MATCH_CRITERIA_CLS_NAME);
		classMatchCriteriaSlot = kb.getSlot(CLASS_MATCH_CRITERIA_SLOT_NAME);
		propogateTrivialMatchSlot = kb.getSlot(PROPOGATE_TRIVIAL_MATCH_SLOT_NAME);
		slotMatcherSimpleSlotsSlot = kb.getSlot(SLOT_MATCHER_SIMPLE_SLOTS_SLOT_NAME);
		slotMatcherSlotSlot = kb.getSlot(SLOT_MATCHER_SLOT_SLOT_NAME);
		spanMatchCriteriaSlot = kb.getSlot(SPAN_MATCH_CRITERIA_SLOT_NAME);
		simpleSlotMatchCriteriaCls = kb.getCls(SIMPLE_SLOT_MATCH_CRITERIA_CLS_NAME);
		slotMatcherConfigCls = kb.getCls(SLOT_MATCHER_CONFIG_CLS_NAME);
		slotMatchCriteriaSlot = kb.getSlot(SLOT_MATCH_CRITERIA_SLOT_NAME);

		configurationCls = kb.getCls(CONFIGURATION_CLS_NAME);
		activeFiltersSlot = kb.getSlot(ACTIVE_FILTERS_SLOT_NAME);
		colorAssignmentsSlot = kb.getSlot(COLOR_ASSIGNMENTS_SLOT_NAME);
		rootClsesSlot = kb.getSlot(ROOT_CLSES_SLOT_NAME);
		selectedAnnotationSetSlot = kb.getSlot(SELECTED_ANNOTATION_SET_SLOT_NAME);
		selectedAnnotatorSlot = kb.getSlot(SELECTED_ANNOTATOR_SLOT_NAME);
		selectedFilterSlot = kb.getSlot(SELECTED_FILTER_SLOT_NAME);
		tokenRegexSlot = kb.getSlot(TOKEN_REGEX_SLOT_NAME);
		fadeUnselectedAnnotationsSlot = kb.getSlot(FADE_UNSELECTED_ANNOTATIONS);
		consensusAcceptRecursiveSlot = kb.getSlot(CONSENSUS_ACCEPT_RECURSIVE);
		subtextSlotSlot = kb.getSlot(SUBTEXT_SLOT_SLOT_NAME);
		fastAnnotateSlot = kb.getSlot(FAST_ANNOTATE_SLOT_NAME);
		nextAnnotationOnDeleteSlot = kb.getSlot(NEXT_ANNOTATION_ON_DELETE);
	}

	public Cls getAnnotationCls() {
		return this.annotationCls;
	}

	public Slot getAnnotatedMentionSlot() {
		return this.annotatedMentionSlot;
	}

	public Slot getAnnotationAnnotatorSlot() {
		return this.annotationAnnotatorSlot;
	}

	public Slot getAnnotationCommentSlot() {
		return this.annotationCommentSlot;
	}

	public Slot getAnnotationCreationDateSlot() {
		return this.annotationCreationDateSlot;
	}

	public Slot getAnnotationSpanSlot() {
		return this.annotationSpanSlot;
	}

	public Slot getAnnotationTextSlot() {
		return this.annotationTextSlot;
	}

	public Slot getAnnotationTextSourceSlot() {
		return this.annotationTextSourceSlot;
	}

	public Slot getSetSlot() {
		return this.setSlot;
	}

	public Cls getMentionCls() {
		return this.mentionCls;
	}

	public Slot getMentionAnnotationSlot() {
		return this.mentionAnnotationSlot;
	}

	public Cls getClassMentionCls() {
		return this.classMentionCls;
	}

	public Slot getMentionClassSlot() {
		return this.mentionClassSlot;
	}

	public Slot getSlotMentionSlot() {
		return this.slotMentionSlot;
	}

	public Cls getInstanceMentionCls() {
		return this.instanceMentionCls;
	}

	public Slot getMentionInstanceSlot() {
		return this.mentionInstanceSlot;
	}

	public Cls getSlotMentionCls() {
		return this.slotMentionCls;
	}

	public Slot getMentionSlotSlot() {
		return this.mentionSlotSlot;
	}

	public Slot getMentionSlotValueSlot() {
		return this.mentionSlotValueSlot;
	}

	public Slot getMentionedInSlot() {
		return this.mentionedInSlot;
	}

	public Cls getComplexSlotMentionCls() {
		return this.complexSlotMentionCls;
	}

	public Cls getBooleanSlotMentionCls() {
		return this.booleanSlotMentionCls;
	}

	public Cls getFloatSlotMentionCls() {
		return this.floatSlotMentionCls;
	}

	public Cls getIntegerSlotMentionCls() {
		return this.integerSlotMentionCls;
	}

	public Cls getStringSlotMentionCls() {
		return this.stringSlotMentionCls;
	}

	public Cls getTextSourceCls() {
		return this.textSourceCls;
	}

	public Cls getAnnotatorCls() {
		return this.annotatorCls;
	}

	public Cls getHumanAnnotatorCls() {
		return humanAnnotatorCls;
	}

	public Cls getTeamAnnotatorCls() {
		return teamAnnotatorCls;
	}

	public Slot getAnnotatorTeamMembersSlot() {
		return annotatorTeamMembersSlot;
	}

	public Slot getAnnotatorTeamNameSlot() {
		return annotatorTeamNameSlot;
	}

	public Cls getDisplayColorCls() {
		return this.displayColorCls;
	}

	public Slot getDisplayColorBSlot() {
		return this.displayColorBSlot;
	}

	public Slot getDisplayColorGSlot() {
		return this.displayColorGSlot;
	}

	public Slot getDisplayColorNameSlot() {
		return this.displayColorNameSlot;
	}

	public Slot getDisplayColorRSlot() {
		return this.displayColorRSlot;
	}

	public Cls getSetCls() {
		return this.setCls;
	}

	public Slot getSetDescriptionSlot() {
		return this.setDescriptionSlot;
	}

	public Slot getSetNameSlot() {
		return this.setNameSlot;
	}

	public Cls getConsensusSetCls() {
		return this.consensusSetCls;
	}

	public Slot getConsensusSetConsensusFilterSlot() {
		return this.consensusSetConsensusFilterSlot;
	}

	public Slot getConsensusSetIndividualFilterSlot() {
		return this.consensusSetIndividualFilterSlot;
	}

	public Slot getConsensusSetTeamAnnotatorSlot() {
		return this.consensusSetTeamAnnotatorSlot;
	}

	public Cls getFilterCls() {
		return this.filterCls;
	}

	public Slot getFilterAnnotatorSlot() {
		return this.filterAnnotatorSlot;
	}

	public Slot getFilterNameSlot() {
		return this.filterNameSlot;
	}

	public Slot getFilterSetSlot() {
		return this.filterSetSlot;
	}

	public Slot getFilterTypeSlot() {
		return this.filterTypeSlot;
	}

	public Cls getConsensusFilterCls() {
		return this.consensusFilterCls;
	}

	public Cls getColorAssignmentCls() {
		return this.colorAssignmentCls;
	}

	public Slot getColorClassSlot() {
		return this.colorClassSlot;
	}

	public Slot getDisplayColorSlot() {
		return this.displayColorSlot;
	}

	public Slot getClassMatchCriteriaSlot() {
		return classMatchCriteriaSlot;
	}

	public Cls getComplexSlotMatchCriteriaCls() {
		return complexSlotMatchCriteriaCls;
	}

	public Slot getPropogateTrivialMatchSlot() {
		return propogateTrivialMatchSlot;
	}

	public Cls getSimpleSlotMatchCriteriaCls() {
		return simpleSlotMatchCriteriaCls;
	}

	public Slot getSlotMatchCriteriaSlot() {
		return slotMatchCriteriaSlot;
	}

	public Cls getSlotMatcherConfigCls() {
		return slotMatcherConfigCls;
	}

	public Slot getSlotMatcherSimpleSlotsSlot() {
		return slotMatcherSimpleSlotsSlot;
	}

	public Slot getSlotMatcherSlotSlot() {
		return slotMatcherSlotSlot;
	}

	public Slot getSpanMatchCriteriaSlot() {
		return spanMatchCriteriaSlot;
	}

	public Slot getActiveFiltersSlot() {
		return activeFiltersSlot;
	}

	public Slot getAnnotatorAffiliationSlot() {
		return annotatorAffiliationSlot;
	}

	public Slot getAnnotatorFirstNameSlot() {
		return annotatorFirstNameSlot;
	}

	public Slot getAnnotatorIDSlot() {
		return annotatorIDSlot;
	}

	public Slot getAnnotatorLastNameSlot() {
		return annotatorLastNameSlot;
	}

	public Slot getColorAssignmentsSlot() {
		return colorAssignmentsSlot;
	}

	public Cls getConfigurationCls() {
		return configurationCls;
	}

	public Cls getKnowtatorSupportCls() {
		return knowtatorSupportCls;
	}

	public Slot getRootClsesSlot() {
		return rootClsesSlot;
	}
	
	public Slot getFastAnnotateSlot() {
		return fastAnnotateSlot;
	}

	public Slot getSelectedAnnotationSetSlot() {
		return selectedAnnotationSetSlot;
	}

	public Slot getSelectedAnnotatorSlot() {
		return selectedAnnotatorSlot;
	}

	public Cls getSelectedFiltersCls() {
		return selectedFiltersCls;
	}

	public Slot getSelectedFilterSlot() {
		return selectedFilterSlot;
	}

	public Slot getSelectedFiltersSlot() {
		return selectedFiltersSlot;
	}

	public Slot getTokenRegexSlot() {
		return tokenRegexSlot;
	}

	public Cls getTscImplementationsCls() {
		return tscImplementationsCls;
	}

	public Slot getTscImplementationsSlot() {
		return tscImplementationsSlot;
	}

	public Slot getFilterTypesNotSelectableFromTextViewerSlot() {
		return filterTypesNotSelectableFromTextViewerSlot;
	}

	public Slot getFadeUnselectedAnnotationsSlot() {
		return fadeUnselectedAnnotationsSlot;
	}

	public Slot getConsensusAcceptRecursiveSlot() {
		return consensusAcceptRecursiveSlot;
	}
	
	public Slot getSubtextSlotSlot() {
		return subtextSlotSlot;
	}
	
	public Slot getNextAnnotationOnDeleteSlot() {
		return nextAnnotationOnDeleteSlot;
	}
}
