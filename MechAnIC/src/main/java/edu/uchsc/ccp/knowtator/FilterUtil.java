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
 * 8/15/2005  The type filtering was using the old Knowtator model.  Changed the code
 *            so that the type and supertypes of the mention of the annotation are 
 *            verified.  
 */
package edu.uchsc.ccp.knowtator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.uchsc.ccp.knowtator.util.ConsensusException;

public class FilterUtil {
	public static final int ANY_FILTER = 0;

	public static final int CONSENSUS_FILTER = 1;

	public static final int NONCONSENSUS_FILTER = 2;

	KnowtatorManager manager;

	KnowtatorProjectUtil kpu;

	AnnotationUtil annotationUtil;

	MentionUtil mentionUtil;

	Logger logger = Logger.getLogger(FilterUtil.class);

	/** Creates a new instance of FilterUtil */
	public FilterUtil(KnowtatorManager manager) {
		this.manager = manager;
		this.kpu = manager.getKnowtatorProjectUtil();
		this.annotationUtil = manager.getAnnotationUtil();
		this.mentionUtil = manager.getMentionUtil();
	}

	public Collection<SimpleInstance> filterAnnotations(Collection<SimpleInstance> annotationInstances,
			SimpleInstance filterInstance) {
		return filterAnnotations(annotationInstances, filterInstance, false);
	}

	public boolean isClsLicensedByFilter(SimpleInstance filter, Frame frame) {
		Cls cls = null;
		if (frame instanceof Cls)
			cls = (Cls) frame;
		else
			cls = ((Instance) frame).getDirectType();

		Collection goodTypes = filter.getOwnSlotValues(kpu.filterTypeSlot);
		if (goodTypes == null || goodTypes.size() == 0)
			return true;
		else {
			Collection superTypes = cls.getSuperclasses();
			ArrayList types = new ArrayList(superTypes);
			types.add(cls);
			for (Iterator iterator = types.iterator(); iterator.hasNext();) {
				if (goodTypes.contains(iterator.next())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param textViewerSelectable
	 *            - true if you want to filter out annotations of the types
	 *            specified in the
	 *            knowtator_types_not_selectable_from_text_viewer slot of the
	 *            filter.
	 */
	public List<SimpleInstance> filterAnnotations(Collection<SimpleInstance> annotationInstances,
			SimpleInstance filterInstance, boolean ignoreTypeFilter, boolean textViewerSelectable) {
		logger.debug("");
		Collection badTypes = filterInstance.getOwnSlotValues(kpu.getFilterTypesNotSelectableFromTextViewerSlot());
		// logger.debug("badTypes.size()="+badTypes.size());
		List<SimpleInstance> annotations = filterAnnotations(annotationInstances, filterInstance, ignoreTypeFilter);
		// logger.debug("annotations.size()="+annotations.size());
		List<SimpleInstance> returnValues = new ArrayList<SimpleInstance>();

		if (badTypes.size() > 0 && textViewerSelectable) {
			for (SimpleInstance annotation : annotations) {
				boolean goodType = true;
				SimpleInstance mention = annotationUtil.getMention(annotation);
				Cls mentionCls = mentionUtil.getMentionCls(mention);
				if (mentionCls != null) {
					Collection superTypes = mentionCls.getSuperclasses();
					ArrayList types = new ArrayList(superTypes);
					types.add(mentionCls);
					// logger.debug("annotation types");
					// for (Object object : types) {
					// logger.debug(object);
					// }
					for (Iterator iterator = types.iterator(); iterator.hasNext();) {
						if (badTypes.contains(iterator.next())) {
							// logger.debug(
							// "badTypes.contains superclass of annotation");
							// logger.debug("mentionCls="+mentionCls);
							goodType = false;
							break;
						}
					}
					if (goodType)
						returnValues.add(annotation);
				}
			}
			logger.debug("returning returnValues: " + returnValues.size());
			return returnValues;
		} else
			return annotations;
	}

	/**
	 * 
	 * @param annotationInstances
	 * @param filterInstance
	 * @param ignoreTypeFilter
	 *            - will ignore the type of the annotation when filtering if
	 *            true. If true, then the value of textviewerSelectable is
	 *            ignored.
	 */
	public List<SimpleInstance> filterAnnotations(Collection<SimpleInstance> annotationInstances,
			SimpleInstance filterInstance, boolean ignoreTypeFilter) {
		Collection goodAnnotators = filterInstance.getOwnSlotValues(kpu.filterAnnotatorSlot);
		Collection goodAnnotationSets = filterInstance.getOwnSlotValues(kpu.filterSetSlot);
		Collection goodTypes = filterInstance.getOwnSlotValues(kpu.filterTypeSlot);

		ArrayList<SimpleInstance> goodAnnotations = new ArrayList<SimpleInstance>();

		if (annotationInstances != null) {
			for (SimpleInstance annotation : annotationInstances) {
				boolean goodAnnotator = goodAnnotators.size() > 0 ? false : true;
				boolean goodAnnotationSet = goodAnnotationSets.size() > 0 ? false : true;
				boolean goodType = (goodTypes.size() > 0 && !ignoreTypeFilter) ? false : true;

				if (goodAnnotator && goodAnnotationSet && goodType) {
					goodAnnotations.add(annotation);
					continue;
				}

				/**
				 * might be better to iterate through members rather than
				 * calling retainAll for performance reasons (can continue as
				 * soon as you find a match using contains)
				 */
				if (!goodType) {
					SimpleInstance mention = annotationUtil.getMention(annotation);
					Cls mentionCls = mentionUtil.getMentionCls(mention);
					if (mentionCls != null) {
						Collection superTypes = mentionCls.getSuperclasses();
						ArrayList types = new ArrayList(superTypes);
						types.add(mentionCls);
						for (Iterator iterator = types.iterator(); iterator.hasNext();) {
							if (goodTypes.contains(iterator.next())) {
								goodType = true;
								break;
							}
						}
					}
				}
				if (!goodAnnotator) {
					Collection annotators = annotation.getOwnSlotValues(kpu.getAnnotationAnnotatorSlot());
					for (Iterator iterator = annotators.iterator(); iterator.hasNext();) {
						if (goodAnnotators.contains(iterator.next())) {
							goodAnnotator = true;
							break;
						}
					}
				}

				if (!goodAnnotationSet) {
					Collection annotationSets = annotation.getOwnSlotValues(kpu.getSetSlot());
					for (Iterator iterator = annotationSets.iterator(); iterator.hasNext();) {
						if (goodAnnotationSets.contains(iterator.next())) {
							goodAnnotationSet = true;
							break;
						}
					}
				}

				if (goodAnnotator && goodAnnotationSet && goodType) {
					goodAnnotations.add(annotation);
					continue;
				}
			}
		}

		return goodAnnotations;
	}

	/**
	 * Returns the types specified by the filter. This method will not return
	 * null - but rather an empty set if no type has been specified by the
	 * filter.
	 */
	public static Set<Cls> getTypes(SimpleInstance filter) {
		KnowledgeBase kb = filter.getKnowledgeBase();
		Collection<Cls> filterTypes = (Collection<Cls>) filter.getOwnSlotValues(kb
				.getSlot(KnowtatorProjectUtil.FILTER_TYPE_SLOT_NAME));
		Set<Cls> types = new HashSet<Cls>();
		if (filterTypes != null) {
			types.addAll(filterTypes);
		}
		return types;
	}

	/**
	 * Returns the annotators specified by the filter. This method will not
	 * return null - but rather an empty set if no annotator has been specified
	 * by the filter.
	 */
	public static List<SimpleInstance> getAnnotators(SimpleInstance filter) {
		KnowledgeBase kb = filter.getKnowledgeBase();
		Collection<SimpleInstance> filterAnnotators = (Collection<SimpleInstance>) filter.getOwnSlotValues(kb
				.getSlot(KnowtatorProjectUtil.FILTER_ANNOTATOR_SLOT_NAME));
		List<SimpleInstance> annotators = new ArrayList<SimpleInstance>();
		if (filterAnnotators != null) {
			annotators.addAll(filterAnnotators);
		}
		return annotators;
	}

	public static Set<SimpleInstance> getSets(SimpleInstance filter) {
		KnowledgeBase kb = filter.getKnowledgeBase();
		Collection<SimpleInstance> filterSets = (Collection<SimpleInstance>) filter.getOwnSlotValues(kb
				.getSlot(KnowtatorProjectUtil.FILTER_SET_SLOT_NAME));
		Set<SimpleInstance> sets = new HashSet<SimpleInstance>();
		if (filterSets != null) {
			sets.addAll(filterSets);
		}
		return sets;
	}

	public static boolean isConsensusFilter(SimpleInstance filter) {
		if (filter == null)
			return false;
		KnowledgeBase kb = filter.getKnowledgeBase();
		Cls type = filter.getDirectType();
		if (type.equals(kb.getCls(KnowtatorProjectUtil.CONSENSUS_FILTER_CLS_NAME)))
			return true;
		return false;
	}

	public SimpleInstance getPreviousFilter(int filterType) {
		int currentFilterIndex = getCurrentFilterIndex();
		List<SimpleInstance> activeFilters = new ArrayList<SimpleInstance>(manager.getActiveFilters());
		Collections.rotate(activeFilters, activeFilters.size() - currentFilterIndex); // put
																					  // current
																					  // filter
																					  // at
																					  // begin
																					  // of
																					  // list
		for (int i = activeFilters.size() - 1; i >= 0; i--) {
			SimpleInstance filter = activeFilters.get(i);
			if (filterType == ANY_FILTER)
				return filter;
			if (filterType == CONSENSUS_FILTER) {
				if (isConsensusFilter(filter))
					return filter;
			}
			if (filterType == NONCONSENSUS_FILTER) {
				if (!isConsensusFilter(filter))
					return filter;
			}
		}
		return null;
	}

	public int getCurrentFilterIndex() {
		SimpleInstance currentFilter = manager.getSelectedFilter();
		List<SimpleInstance> filterInstances = manager.getActiveFilters();
		if (currentFilter == null && filterInstances.size() > 0) {
			return 0;
		}
		for (int i = 0; i < filterInstances.size(); i++) {
			if (currentFilter.equals(filterInstances.get(i))) {
				if (i < filterInstances.size() - 1)
					return i;
				else
					return 0;
			}
		}
		return -1;
	}

	/**
	 * 
	 * @param filterType
	 *            one of ANY_FILTER, CONSENSUS_FILTER, or NONCONSENSUS_FILTER
	 * @return the next filter of the provided type
	 */
	public SimpleInstance getNextFilter(int filterType) {
		int currentFilterIndex = getCurrentFilterIndex();
		List<SimpleInstance> activeFilters = new ArrayList<SimpleInstance>(manager.getActiveFilters());
		Collections.rotate(activeFilters, activeFilters.size() - currentFilterIndex - 1);// put
																						 // current
																						 // filter
																						 // at
																						 // end
																						 // of
																						 // list
		for (int i = 0; i < activeFilters.size(); i++) {
			SimpleInstance filter = activeFilters.get(i);
			if (filterType == ANY_FILTER)
				return filter;
			if (filterType == CONSENSUS_FILTER) {
				if (isConsensusFilter(filter))
					return filter;
			}
			if (filterType == NONCONSENSUS_FILTER) {
				if (!isConsensusFilter(filter))
					return filter;
			}
		}
		return null;
	}

	public void selectNextFilter(int filterType) throws ConsensusException {
		logger.debug("");
		manager.setSelectedFilter(getNextFilter(filterType));
	}

	public void selectPreviousFilter(int filterType) throws ConsensusException {
		logger.debug("");
		manager.setSelectedFilter(getNextFilter(filterType));
	}

}
