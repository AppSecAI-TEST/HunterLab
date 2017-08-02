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
 * Changes
 * 8/15/2005   pvo  added two methods getAnnotationFilters and setAnnotationFilters
 *          instead of using the project client information for persisting the  
 *          annotation filters that are selected, the knowtator.pprj was updated
 *          with a new instance for storing the selected filters.  If the instance
 *          does not exist, then it is created.  
 */
package edu.uchsc.ccp.knowtator;

/*
 * ProjectSettings
 *
 * Created on October 15, 2004, 8:20 AM
 */

import java.lang.reflect.Method;

import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.SimpleInstance;
import edu.uchsc.ccp.knowtator.textsource.TextSourceCollection;

public class ProjectSettings {

	/**
	 * Records the most recent type of TextSourceCollection (tsc) used by this
	 * project.
	 */
	public static final String PROJECT_RECENT_TSC = new String("PROJECT_RECENT_TSC");

	public static final String PROJECT_RECENT_TSC_INDEX = new String("PROJECT_RECENT_TSC_INDEX");

	public static final String SHOW_INSTANCES = new String("SHOW_INSTANCES");

	public static final String DEFAULT_TOKEN_REGEX = new String("\\W+");

	public static final String ACTIVE_CONFIGURATION = new String("ACTIVE_CONFIGURATION");

	public static boolean getShowInstances(Project project) {
		Object showInstances = project.getClientInformation(SHOW_INSTANCES);
		if (showInstances == null || !(showInstances instanceof Boolean))
			return false;
		return ((Boolean) showInstances).booleanValue();
	}

	public static void setShowInstances(Project project, boolean showInstances) {
		project.setClientInformation(SHOW_INSTANCES, new Boolean(showInstances));
	}

	public static TextSourceCollection getRecentTextSourceCollection(Project project) {
		/**
		 *call static method getProjectRecentTextSourceCollection on TSC
		 */
		try {
			TextSourceCollection tsc;
			String tscClassName = (String) project.getClientInformation(PROJECT_RECENT_TSC);
			if (tscClassName != null) {
				Class tscClass = Class.forName(tscClassName);
				Method tscMethod = tscClass.getMethod("open", new Class[] { Project.class });
				tsc = (TextSourceCollection) tscMethod.invoke(null, new Object[] { project });
				return tsc;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}

	public static void setRecentTextSourceCollection(Project project, TextSourceCollection tsc) {
		project.setClientInformation(ProjectSettings.PROJECT_RECENT_TSC, tsc.getClass().getName());
		tsc.save(project);
	}

	public static int getRecentTextSourceCollectionIndex(Project project) {
		Integer recentIndex = (Integer) project.getClientInformation(PROJECT_RECENT_TSC_INDEX);

		if (recentIndex == null) {
			return 0;
		}
		return recentIndex;
	}

	public static void setRecentTextSourceCollectionIndex(Project project, int recentIndex) {
		project.setClientInformation(PROJECT_RECENT_TSC_INDEX, new Integer(recentIndex));
	}

	public static SimpleInstance getActiveConfiguration(Project project) {
		return (SimpleInstance) project.getClientInformation(ACTIVE_CONFIGURATION);
	}

	public static void setActiveConfiguration(Project project, SimpleInstance configurationInstance) {
		project.setClientInformation(ACTIVE_CONFIGURATION, configurationInstance);
	}

	// public void setAnnotationPicker
}
