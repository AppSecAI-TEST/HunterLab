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
 * Copyright (C) 2005-2009.  All Rights Reserved.
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
 *   Brant Barney (Original Author)
 */

package edu.uchsc.ccp.knowtator.ui.action;

import java.util.HashMap;
import java.util.Map;

import javax.swing.Action;

import edu.uchsc.ccp.knowtator.KnowtatorManager;
import edu.uchsc.ccp.knowtator.exception.ActionNotFoundException;

/**
 * Class used to create UI actions and retrieve them. This way there will be a single action created
 *   that can be used in buttons, menus, etc.
 *    
 * @author brant
 */
public class ActionFactory {
	
	public static final String ACTION_ACCEPT_ANNOTATION = "ACCEPT_ANNOTATION_ACTION";	
	public static final String ACTION_CLEAR_ANNOTATION = "CLEAR_ANNOTATION_ACTION";	
	public static final String ACTION_ACCEPT_CONSENSUS_ANNOTATION = "ACCEPT_CONSENSUS_ANNOTATION_ACTION";
	public static final String ACTION_CLEAR_CONSENSUS_ANNOTATION = "CLEAR_CONSENSUS_ANNOTATION_ACTION";	
	public static final String ACTION_SELECT_NEXT_ANNOTATION = "ACTION_SELECT_NEXT_ANNOTATION";
	public static final String ACTION_DELETE_ANNOTATION = "ACTION_DELETE_ANNOTATION";
	public static final String ACTION_DELETE_CONSENSUS_ANNOTATION = "ACTION_DELETE_CONSENSUS_ANNOTATION";
	public static final String ACTION_REQUIRED_MODE_NEXT_ACTION = "ACTION_REQUIRED_MODE_NEXT_ACTION";
	
	private Map<String, Action> actionMap;
	
	private KnowtatorManager manager;
	
	private static ActionFactory factory;

	/**
	 * Singleton, cannot be instantiated. Use <code>getInstance()</code> instead.
	 * 
	 * @param manager
	 */
	private ActionFactory( KnowtatorManager manager ) {
		
		this.manager = manager;
		
		initializeActions();
	}
	
	/**
	 * Gets an instance of <code>ActionFactory</code>
	 * 
	 * @param manager
	 * @return
	 */
	public static ActionFactory getInstance( KnowtatorManager manager ) {
		if( factory == null ) {
			factory = new ActionFactory( manager );
		}
		
		return factory;
	}
		
	private void initializeActions() {
		actionMap = new HashMap<String, Action>();
		
		actionMap.put( ACTION_ACCEPT_ANNOTATION, new AcceptAnnotationAction( manager ) );
		actionMap.put( ACTION_CLEAR_ANNOTATION, new ClearAnnotationAction( manager ) );
		actionMap.put( ACTION_ACCEPT_CONSENSUS_ANNOTATION, new AcceptConsensusAnnotationAction( manager ) );
		actionMap.put( ACTION_CLEAR_CONSENSUS_ANNOTATION, new ClearConsensusAnnotationAction( manager ) );
		actionMap.put( ACTION_SELECT_NEXT_ANNOTATION, new SelectNextNonTeamAnnotationAction( manager ) );
		actionMap.put( ACTION_DELETE_ANNOTATION, new DeleteAnnotationAction( manager ) );
		actionMap.put( ACTION_DELETE_CONSENSUS_ANNOTATION, new DeleteConsensusAnnotationAction( manager ) );	
		actionMap.put( ACTION_REQUIRED_MODE_NEXT_ACTION, new RequiredModeNextAction( manager ) );
	}
	
	/**
	 * Gets the action that is associated with the given key.
	 * 
	 * @param key Will be one of the constants declared in this class
	 * 
	 * @return
	 * @throws ActionNotFoundException
	 */
	public Action getAction( String key ) throws ActionNotFoundException {
		if( actionMap.containsKey( key ) ) {
			return (Action)actionMap.get( key );
		} else {
			throw new ActionNotFoundException( "Action with key: " + key + " was not found." );
		}
	}
	
	/**
	 * Cleans up all local variables. To be called only when Knowtator.disable() is called.
	 */
	public void dispose() {
		actionMap = null;
		factory = null;
		manager = null;
	}
}
