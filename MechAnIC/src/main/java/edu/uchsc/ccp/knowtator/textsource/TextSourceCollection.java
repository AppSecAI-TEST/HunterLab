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

package edu.uchsc.ccp.knowtator.textsource;

import java.awt.Component;
import java.util.ArrayList;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.uchsc.ccp.knowtator.KnowtatorProjectUtil;

/**
 * Authors: Philip V. Ogren Created: October, 2004 Description:
 * 
 * Every extension of this class should have a corresponding extension of
 * TextSource. Both extensions should be built together in parallel.
 * 
 */

public abstract class TextSourceCollection {

	/**
	 * Corresponds to the Protege class name of the text sources that are
	 * retrieved by this collection.
	 */
	public static String CLS_NAME = KnowtatorProjectUtil.TEXT_SOURCE_CLS_NAME;

	/**
	 * This is the name that is displayed when users are asked what type of text
	 * source collection they would like to access.
	 */
	public static String DISPLAY_NAME;

	/**
	 * provides an easy way for a script to iterate through all TextSources in a
	 * collection.
	 */
	public abstract TextSourceIterator iterator();

	/**
	 * @return The name of the text source collection. Might correspond to the
	 *         name of the directory, database or file that the text source
	 *         collection accesses to get text sources.
	 */
	public abstract String getName();

	public abstract int size();

	public abstract TextSource get(int index) throws TextSourceAccessException;

	/**
	 * @return the text source that has the given name. A text source collection
	 *         should have no name collisions.
	 */
	public abstract TextSource get(String textSourceName) throws TextSourceAccessException;

	/**
	 * @return -1 if text source does not exist.
	 */
	public abstract int getIndex(TextSource textSource);

	/**
	 * @return a TextSource selected by a human user through a user interface.
	 *         Implementations of this method might provide a dialog with a list
	 *         of available text sources.
	 */
	public abstract TextSource select(Component parent);

	/**
	 * @return a TextSource selected by a human user through a user interface.
	 *         Implementations of this method might provide a dialog with a text
	 *         field that requires the user to enter the name of a text source
	 *         or some text that s/he is looking for.
	 */
	public abstract TextSource find(Component parent);

	/**
	 * This method saves information about this text source collection to the
	 * Protege project. The information saved should be sufficient to
	 * reinstantiate this text source collection the next time Protege is
	 * started.
	 * 
	 */
	public abstract void save(Project project);

	/**
	 * This method should return the text source collection most recently opened
	 * by the provided project. This method is called when Knowtator is
	 * initiated. This method eliminates the need for the user to open the same
	 * text source collection every time s/he opens Knowtator.
	 * 
	 * @return null if recent text source collection cannot be opened.
	 * 
	 */

	public static TextSourceCollection open(Project project) {
		return null;
	}

	/**
	 * @return a text source collection chosen by a human user through a user
	 *         interface. Implementations of this method might provide a
	 *         FileChooser, a dialog with database connection settings, etc.
	 */
	public static TextSourceCollection open(Project project, Component parent) {
		return null;
	}

	/**
	 * This method ensures that the Protege cls definition corresponding to the
	 * text sources in this collection does exist. If not, it creates the
	 * appropriate cls. Implementors of this class should first check the kb to
	 * make sure that the class does not already exist.
	 * 
	 */

	public static void createCls(KnowledgeBase kb) {
		if (kb.getCls(CLS_NAME) == null) {
			Cls textSourceParent = kb.getCls(KnowtatorProjectUtil.TEXT_SOURCE_CLS_NAME);
			ArrayList parents = new ArrayList();
			parents.add(textSourceParent);
			kb.createCls(CLS_NAME, parents);
		}
		return;
	}

}
