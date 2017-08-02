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
package edu.uchsc.ccp.knowtator.util;

import java.awt.Component;
import java.awt.Cursor;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.ui.DisplayUtilities;
import edu.stanford.smi.protege.util.CollectionUtilities;
import edu.stanford.smi.protege.util.ExtensionFilter;
import edu.uchsc.ccp.knowtator.AnnotationUtil;
import edu.uchsc.ccp.knowtator.FilterUtil;
import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;
import edu.uchsc.ccp.knowtator.MentionUtil;
import edu.uchsc.ccp.knowtator.Span;
import edu.uchsc.ccp.knowtator.TextSourceUtil;
import edu.uchsc.ccp.knowtator.textsource.TextSource;
import edu.uchsc.ccp.knowtator.textsource.TextSourceAccessException;

public class MergeAnnotations {

	Project addedProject;

	Project masterProject;

	TextSourceUtil masterTextSourceUtil;

	Component parentComponent;

	KnowledgeBase masterKB;

	KnowtatorManager masterManager;

	KnowtatorProjectUtil masterKPU;

	AnnotationUtil masterAnnotationUtil;

	MentionUtil masterMentionUtil;

	KnowledgeBase addedKB;

	KnowtatorManager addedManager;

	KnowtatorProjectUtil addedKPU;

	AnnotationUtil addedAnnotationUtil;

	MentionUtil addedMentionUtil;

	FilterUtil addedFilterUtil;

	Map<SimpleInstance, SimpleInstance> annotatorMap;

	Map<SimpleInstance, SimpleInstance> setsMap;

	Set<SimpleInstance> addedAnnotations;

	Set<SimpleInstance> addedMentions;

	Map<SimpleInstance, SimpleInstance> annotationsMap;

	Map<SimpleInstance, SimpleInstance> mentionsMap;

	/**
	 * This class facilitates the merging of an annotation project with a
	 * 'master' annotation project.
	 * 
	 * @param addedProject
	 *            - the project that will be added to the master project. This
	 *            project will not be modified. The .pont for this project
	 *            should be identical to that of the master project.
	 * @param masterProject
	 *            - the project that the added project will be added to. This
	 *            project will be modified - all annotations from the added
	 *            project will be added. However, the project will not be saved
	 *            - so changes can be discarded if desired.
	 */

	public MergeAnnotations(Project addedProject, Project masterProject, TextSourceUtil masterTextSourceUtil,
			Component parentComponent) throws TextSourceAccessException {
		this.addedProject = addedProject;
		this.masterProject = masterProject;
		this.masterTextSourceUtil = masterTextSourceUtil;
		this.parentComponent = parentComponent;

		masterKB = masterProject.getKnowledgeBase();
		masterKPU = new KnowtatorProjectUtil(masterKB);
		masterManager = new KnowtatorManager(masterKPU);
		masterAnnotationUtil = masterManager.getAnnotationUtil();
		masterAnnotationUtil.setTextSourceUtil(masterTextSourceUtil);
		masterMentionUtil = masterManager.getMentionUtil();

		addedKB = addedProject.getKnowledgeBase();
		addedKPU = new KnowtatorProjectUtil(addedKB);
		addedManager = new KnowtatorManager(addedKPU);
		addedAnnotationUtil = addedManager.getAnnotationUtil();
		addedMentionUtil = addedManager.getMentionUtil();
		addedFilterUtil = addedManager.getFilterUtil();

		annotatorMap = new HashMap<SimpleInstance, SimpleInstance>();
		setsMap = new HashMap<SimpleInstance, SimpleInstance>();
		addedAnnotations = new HashSet<SimpleInstance>();
		addedMentions = new HashSet<SimpleInstance>();
		annotationsMap = new HashMap<SimpleInstance, SimpleInstance>();
		mentionsMap = new HashMap<SimpleInstance, SimpleInstance>();

	}

	/*
	 * To do: - I do not worry about subclasses of annotation. If we allow
	 * annotation to be subclassed and have slot values that point to other slot
	 * annotations or any other slot values, then we will need to expand the
	 * code here.
	 */

	public void mergeAnnotations(SimpleInstance addedFilter, Collection<SimpleInstance> addedTextSources)
			throws Exception {

		annotatorMap.clear();
		setsMap.clear();
		addedAnnotations.clear();
		addedMentions.clear();
		annotationsMap.clear();
		mentionsMap.clear();

		// collect the annotations and mentions from the added project that are
		// going to be added to the master project
		for (SimpleInstance addedTextSource : addedTextSources) {
			Collection<SimpleInstance> tsAnnotations = addedAnnotationUtil.getAnnotations(addedTextSource);
			tsAnnotations = addedFilterUtil.filterAnnotations(tsAnnotations, addedFilter);
			for (SimpleInstance tsAnnotation : tsAnnotations) {
				addedAnnotations.add(tsAnnotation);

				SimpleInstance mention = addedAnnotationUtil.getMention(tsAnnotation);
				addedMentions.add(mention);
				Set<SimpleInstance> connectedMentions = addedMentionUtil.getAllConnectedMentions(mention);
				addedMentions.addAll(connectedMentions);
				for (SimpleInstance connectedMention : connectedMentions) {
					SimpleInstance connectedAnnotation = addedMentionUtil.getMentionAnnotation(connectedMention);
					if (connectedAnnotation != null)
						addedAnnotations.add(connectedAnnotation);
				}
			}
		}

		System.out.println("adding " + addedAnnotations.size() + " annotations:");

		int npes = 0;

		ProgressMonitor progressMonitor = new ProgressMonitor(parentComponent, "Merging annotations", null, 0,
				addedAnnotations.size() + addedMentions.size());

		try {

			int progress = 0;

			for (SimpleInstance addedAnnotation : addedAnnotations) {
				progressMonitor.setProgress(progress++);

				if (progressMonitor.isCanceled())
					throw new Exception("Merge cancelled by user.");

				// get the spans of the new annotation
				java.util.List<Span> spans = addedAnnotationUtil.getSpans(addedAnnotation);
				// get the spanned text of the new annotation
				String spannedText = addedAnnotationUtil.getText(addedAnnotation);
				// we want the text source instance in the master kb for the new
				// annotation - not the one from the added kb
				Slot addedTSSlot = addedKPU.getAnnotationTextSourceSlot();
				Instance addedTS = (Instance) addedAnnotation.getOwnSlotValue(addedTSSlot);
				String addedTextSourceName = addedTS.getName();

				SimpleInstance masterTextSourceInstance = masterKB.getSimpleInstance(addedTextSourceName);
				if (masterTextSourceInstance == null) {
					TextSource masterTextSource = masterTextSourceUtil.getCurrentTextSourceCollection().get(
							addedTextSourceName);
					masterTextSourceInstance = masterTextSourceUtil.getTextSourceInstance(masterTextSource, true);
				}

				SimpleInstance addedAnnotator = (SimpleInstance) (addedAnnotation.getOwnSlotValue(addedKPU
						.getAnnotationAnnotatorSlot()));
				SimpleInstance masterAnnotator = getMasterAnnotator(addedAnnotator);

				Collection<SimpleInstance> masterSets = getMasterSets(addedAnnotation);

				SimpleInstance masterAnnotation = masterAnnotationUtil.createAnnotation(null, masterAnnotator, spans,
						spannedText, masterTextSourceInstance, masterSets);

				String comment = addedAnnotationUtil.getComment(addedAnnotation);
				if (comment != null) {
					masterAnnotationUtil.setComment(masterAnnotation, comment);
				}
				annotationsMap.put(addedAnnotation, masterAnnotation);
			}
			System.out.println("Number of NullPointerExceptions for new annotations = " + npes);

			System.out.println("adding " + addedMentions.size() + " mentions:");

			for (SimpleInstance addedMention : addedMentions) {
				System.out.print(".");
				System.out.flush();
				if (progress % 30 == 0)
					System.out.println("");
				progressMonitor.setProgress(progress++);
				addMention(addedMention, addedKPU, masterKPU, addedMentionUtil, annotationsMap, mentionsMap,
						masterMentionUtil, masterKB);
			}

			progressMonitor.close();

		} catch (Exception exception) {
			progressMonitor.close();
			throw exception;
		}
	}

	/**
	 * This method adds a mention from added project to the master project.
	 * 
	 * @param addedMention
	 *            an instance of a mention from the added project
	 * @param addedKPU
	 *            a kpu initialized with the knowledge base from the added
	 *            project
	 * @param masterKPU
	 *            a kpu initialized with the knowledge base from the master
	 *            project
	 * @param addedMentionUtil
	 *            a MentionUtil initialized with addedKPU
	 * @param annotationsMap
	 *            keys are added annotations and values are master annotations.
	 *            Each annotation in the added project is copied and added to
	 *            the master project. An added annotation is an instance in the
	 *            added project. A master annotation is an instance in the
	 *            master project.
	 * @param mentionsMap
	 *            keys are added mentions and values are master mentions. Each
	 *            mention in the added project is copied and added to the master
	 *            project. An added mention is an instance in the added project.
	 *            A master mention is an instance in the master project.
	 * @param masterMentionUtil
	 *            a MentionUtil initialized with masterKPU
	 * @param masterKB
	 *            the knowledge base of the master project
	 */

	private static SimpleInstance addMention(SimpleInstance addedMention, KnowtatorProjectUtil addedKPU,
			KnowtatorProjectUtil masterKPU, MentionUtil addedMentionUtil,
			Map<SimpleInstance, SimpleInstance> annotationsMap, Map<SimpleInstance, SimpleInstance> mentionsMap,
			MentionUtil masterMentionUtil, KnowledgeBase masterKB) {
		if (!mentionsMap.containsKey(addedMention)) {
			SimpleInstance masterMention = null;

			if (addedMentionUtil.isClassMention(addedMention)) {
				Cls addedCls = addedMentionUtil.getMentionCls(addedMention);
				String addedClsName = addedCls.getName();
				Cls masterCls = masterKB.getCls(addedClsName);
				masterMention = masterMentionUtil.createClassMention(masterCls);
				mentionsMap.put(addedMention, masterMention);
				Collection<SimpleInstance> addedSlotMentions = ProtegeUtil.castSimpleInstances(addedMention
						.getOwnSlotValues(addedKPU.getSlotMentionSlot()));
				for (SimpleInstance addedSlotMention : addedSlotMentions) {
					SimpleInstance masterSlotMention = addMention(addedSlotMention, addedKPU, masterKPU,
							addedMentionUtil, annotationsMap, mentionsMap, masterMentionUtil, masterKB);
					if (masterSlotMention != null)
						masterMention.addOwnSlotValue(masterKPU.getSlotMentionSlot(), masterSlotMention);
				}
			} else if (addedMentionUtil.isInstanceMention(addedMention)) {
				String addedInstanceName = addedMentionUtil.getMentionInstance(addedMention).getName();
				SimpleInstance masterInstance = (SimpleInstance) masterKB.getInstance(addedInstanceName);
				masterMention = masterMentionUtil.createInstanceMention(masterInstance);
				mentionsMap.put(addedMention, masterMention);
				Collection<SimpleInstance> addedSlotMentions = ProtegeUtil.castSimpleInstances(addedMention
						.getOwnSlotValues(addedKPU.getSlotMentionSlot()));
				for (SimpleInstance addedSlotMention : addedSlotMentions) {
					SimpleInstance masterSlotMention = addMention(addedSlotMention, addedKPU, masterKPU,
							addedMentionUtil, annotationsMap, mentionsMap, masterMentionUtil, masterKB);
					masterMention.addOwnSlotValue(masterKPU.getSlotMentionSlot(), masterSlotMention);
				}
			} else if (addedMentionUtil.isSlotMention(addedMention)) {
				Slot addedSlot = addedMentionUtil.getSlotMentionSlot(addedMention);
				if (addedSlot == null) {
					System.out.println("WARNING: slot mention has no slot specified.  It will not be added.");
					return null;
				}
				String addedSlotName = addedMentionUtil.getSlotMentionSlot(addedMention).getName();
				Slot masterSlot = masterKB.getSlot(addedSlotName);
				masterMention = masterMentionUtil.createSlotMention(masterSlot);
				mentionsMap.put(addedMention, masterMention);
				Collection addedValues = addedMention.getOwnSlotValues(addedKPU.getMentionSlotValueSlot());

				if (addedValues != null && addedValues.size() > 0) {
					Object value = CollectionUtilities.getFirstItem(addedValues);
					if (value instanceof SimpleInstance) {
						for (Object addedValue : addedValues) {
							SimpleInstance addedInstance = (SimpleInstance) addedValue;
							SimpleInstance masterInstance = mentionsMap.get(addedInstance);
							if (masterInstance == null) {
								masterInstance = addMention(addedInstance, addedKPU, masterKPU, addedMentionUtil,
										annotationsMap, mentionsMap, masterMentionUtil, masterKB);
							}
							masterMention.addOwnSlotValue(masterKPU.getMentionSlotValueSlot(), masterInstance);
						}
					} else {
						masterMention.setOwnSlotValues(masterKPU.getMentionSlotValueSlot(), addedValues);
					}
				}
			}
			if (masterMention != null) {
				Collection<SimpleInstance> mentionAnnotations = ProtegeUtil.castSimpleInstances(addedMention
						.getOwnSlotValues(addedKPU.getMentionAnnotationSlot()));
				if (mentionAnnotations != null) {
					for (SimpleInstance mentionAnnotation : mentionAnnotations) {
						SimpleInstance masterAnnotation = annotationsMap.get(mentionAnnotation);
						masterMention.addOwnSlotValue(masterKPU.getMentionAnnotationSlot(), masterAnnotation);
					}
				}
				return masterMention;
			}
		} else {
			return mentionsMap.get(addedMention);
		}
		return null;
	}

	/**
	 * Does not handle 'knowtator consensus set' slot values
	 * 
	 * @param addedAnnotation
	 * @return
	 */
	private Collection<SimpleInstance> getMasterSets(SimpleInstance addedAnnotation) {
		Set<SimpleInstance> addedSets = addedAnnotationUtil.getSets(addedAnnotation);
		Collection<SimpleInstance> masterSets = new ArrayList<SimpleInstance>();
		SETS_LOOP: for (SimpleInstance addedSet : addedSets) {
			// if there is already a mapping from the added set to the master
			// set then use it
			if (setsMap.containsKey(addedSet)) {
				masterSets.add(setsMap.get(addedSet));
				continue;
			}

			// //if the same instance exists in both projects (i.e. they have
			// the same system id) then use it
			// String setName = addedSet.getName();
			// SimpleInstance masterSet = masterKB.getSimpleInstance(setName);
			// if(masterSet != null)
			// {
			// masterSets.add(masterSet);
			// setsMap.put(addedSet, masterSet);
			// continue;
			// }

			// try to find a set in the master project that has the same slot
			// value for knowtator_set_name
			String name = (String) addedSet.getOwnSlotValue(addedKPU.getSetNameSlot());
			if (name != null) {
				Collection<SimpleInstance> allMasterSets = ProtegeUtil.castSimpleInstances(masterKB
						.getInstances(masterKPU.getSetCls()));
				for (SimpleInstance mstrSet : allMasterSets) {
					String masterName = (String) mstrSet.getOwnSlotValue(masterKPU.getSetNameSlot());
					if (masterName != null && masterName.equals(name)) {
						masterSets.add(mstrSet);
						setsMap.put(addedSet, mstrSet);
						continue SETS_LOOP;
					}
				}
			}

			// create the set in the master project if it does not exist
			String description = (String) addedSet.getOwnSlotValue(addedKPU.getSetDescriptionSlot());
			SimpleInstance masterSet = (SimpleInstance) masterKB.createInstance(null, masterKPU.getSetCls());
			if (name != null)
				masterSet.setOwnSlotValue(masterKPU.getSetNameSlot(), name);
			if (description != null)
				masterSet.setOwnSlotValue(masterKPU.getSetDescriptionSlot(), description);
			masterSets.add(masterSet);
			setsMap.put(addedSet, masterSet);
		}
		return masterSets;
	}

	private SimpleInstance getMasterAnnotator(SimpleInstance addedAnnotator) {
		if (annotatorMap.containsKey(addedAnnotator))
			return annotatorMap.get(addedAnnotator);

		if (addedAnnotator != null) {
			String addedAnnotatorName = addedAnnotator.getName();
			System.out.println("addedAnnotatorName=" + addedAnnotatorName);
			System.out.println("frame id = " + addedAnnotator.getFrameID().toString());

			SimpleInstance masterAnnotator = (SimpleInstance) masterKB.getInstance(addedAnnotatorName);

			if (masterAnnotator != null && masterAnnotator.hasType(masterKPU.getAnnotatorCls())) {
				annotatorMap.put(addedAnnotator, masterAnnotator);
				return masterAnnotator;
			} else {
				if (addedAnnotator.hasType(addedKB.getCls("knowtator human annotator"))) {
					String affiliation = (String) addedAnnotator.getOwnSlotValue(addedKB
							.getSlot("knowtator_annotation_annotator_affiliation"));
					String firstName = (String) addedAnnotator.getOwnSlotValue(addedKB
							.getSlot("knowtator_annotation_annotator_firstname"));
					String lastName = (String) addedAnnotator.getOwnSlotValue(addedKB
							.getSlot("knowtator_annotation_annotator_lastname"));

					Cls masterAnnotatorCls = masterKB.getCls("knowtator human annotator");
					masterAnnotator = (SimpleInstance) masterKB.createInstance(null, masterAnnotatorCls);
					masterAnnotator.setOwnSlotValue(masterKB.getSlot("knowtator_annotation_annotator_affiliation"),
							affiliation);
					masterAnnotator.setOwnSlotValue(masterKB.getSlot("knowtator_annotation_annotator_firstname"),
							firstName);
					masterAnnotator.setOwnSlotValue(masterKB.getSlot("knowtator_annotation_annotator_lastname"),
							lastName);

					annotatorMap.put(addedAnnotator, masterAnnotator);
					return masterAnnotator;
				} else if (addedAnnotator.hasType(addedKB.getCls("knowtator annotator team"))) {
					String teamName = (String) addedAnnotator.getOwnSlotValue(addedKB
							.getSlot("knowtator_annotator_team_name"));
					Collection addedTeamMembers = addedAnnotator.getOwnSlotValues(addedKB
							.getSlot("knowtator_annotator_team_members"));
					Collection<SimpleInstance> masterTeamMembers = new ArrayList<SimpleInstance>();
					for (Object teamMember : addedTeamMembers) {
						masterTeamMembers.add(getMasterAnnotator((SimpleInstance) teamMember));
					}

					Cls masterAnnotatorCls = masterKB.getCls("knowtator annotator team");
					masterAnnotator = (SimpleInstance) masterKB.createInstance(null, masterAnnotatorCls);
					masterAnnotator.setOwnSlotValue(masterKB.getSlot("knowtator_annotator_team_name"), teamName);
					masterAnnotator.setOwnSlotValues(masterKB.getSlot("knowtator_annotator_team_members"),
							masterTeamMembers);
					annotatorMap.put(addedAnnotator, masterAnnotator);
					return masterAnnotator;
				}
			}
		}
		return null;
	}

	public static void mergeAnnotations(Component parent, Project masterProject, TextSourceUtil masterTextSourceUtil) {
		int option = JOptionPane
				.showConfirmDialog(
						parent,
						"Select 'OK' to proceed with merging annotation projects.\n"
								+ "Please make an archive of your Protege project and consult available documentation before using this feature.\n"
								+ "Loading and merging the annotations from the Protege project may take some time.\n"
								+ "Please be patient!", "Merge annotation projects", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileFilter(new ExtensionFilter(".pprj", "Protege projects"));
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = chooser.showOpenDialog(parent);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				ArrayList errors = new ArrayList();
				System.out.println("file.getPath()= " + file.getPath());
				parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				Project addedProject = new Project(file.getPath(), errors);
				parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				option = JOptionPane.showConfirmDialog(parent, "Project loaded into memory.  Proceed with merging?",
						"Proceed?", JOptionPane.OK_CANCEL_OPTION);

				if (option == JOptionPane.OK_OPTION) {
					try {
						MergeAnnotations merge = new MergeAnnotations(addedProject, masterProject,
								masterTextSourceUtil, parent);

						SimpleInstance addedFilter = (SimpleInstance) DisplayUtilities.pickInstance(parent,
								CollectionUtilities.createCollection(addedProject.getKnowledgeBase().getCls(
										KnowtatorProjectUtil.FILTER_CLS_NAME)),
								"Select filter for annotations to add from selected project.");
						if (addedFilter == null)
							return;

						Collection<SimpleInstance> textSources = ProtegeUtil.castSimpleInstances(DisplayUtilities
								.pickInstances(parent, addedProject.getKnowledgeBase(), CollectionUtilities
										.createCollection(addedProject.getKnowledgeBase().getCls(
												KnowtatorProjectUtil.TEXT_SOURCE_CLS_NAME)),
										"Select text sources that contain annotations you want to add."));
						if (textSources == null || textSources.size() == 0)
							return;

						merge.mergeAnnotations(addedFilter, textSources);

						JOptionPane
								.showMessageDialog(
										parent,
										"Merge completed without error.  To save merged annotations, save the Protege project.",
										"Merge Completion", JOptionPane.INFORMATION_MESSAGE);
					} catch (Exception tsae) {
						tsae.printStackTrace();
						JOptionPane.showMessageDialog(parent,
								"Merge caused an exception to be thrown.  Please discard changes caused by aborted merge\n"
										+ "by closing the project without saving changes.\n" + tsae.getMessage());
					}
				}
			}
		}
	}
}

// public static void main(String[] args)
// {
// public static void mergeAnnotations(Project addedProject,
// Project masterProject,
// TextSourceUtil textSourceUtil)
// throws Exception
//    
// String addedProjectFileName = args[0];
// String masterProjectFileName = args[0];
// String grifLinesTSCFileName = args[1];
// String filterInstanceName = args[2];
// String log4jConfigurationFileName = args[3];
//            
// PropertyConfigurator.configure(log4jConfigurationFileName);
//            
//
// // GRIFFileLinesTextSourceCollection tsc = new
// GRIFFileLinesTextSourceCollection(grifLinesTSCFileName);
// GRIFTextSourceCollection tsc = new
// GRIFTextSourceCollection(grifLinesTSCFileName);
//
// ArrayList errors = new ArrayList();
// Project project = new Project(protegeProjectFileName, errors);
// if(errors.isEmpty())
// {
// System.out.println("Protege project loaded without error.");
// }
// else
// {
// System.out.println("Protege project loaded with errors.");
// for(int i=0; i<errors.size();i++)
// {
// System.out.println("errors.get(i)="+errors.get(i));
// }
// }
//
// }

// Instance masterAnnotationSet = null;
// Instance addedAnnotationSet =
// (Instance)(addedAnnotation.getOwnSlotValue(addedKPU.getSetSlot()));
// if(addedAnnotationSet != null)
// {
// String addedAnnotationSetName = addedAnnotationSet.getName();
// masterAnnotationSet = masterKB.getInstance(addedAnnotationSetName);
// }
//        
// SimpleInstance masterAnnotation = masterAnnotationUtil.createAnnotation(
// null,
// masterAnnotator,
// spans,
// masterTextSourceInstance,
// masterAnnotationSet);

