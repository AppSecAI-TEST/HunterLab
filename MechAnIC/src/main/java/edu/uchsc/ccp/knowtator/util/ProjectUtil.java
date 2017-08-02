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
 *   Health Language, Inc.
 *   
 *   The original contents of this file were developed under contract with Health Language, Inc. and were subsequently generously donated to this project.
 *   The original methods were:
 *    - openProject
 *    - saveProject
 *    - examineErrors
 *    - saveProjectAs
 *    - fixKnowtatorProjectPath
 */

package edu.uchsc.ccp.knowtator.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.plugin.PluginUtilities;
import edu.stanford.smi.protege.storage.clips.ClipsKnowledgeBaseFactory;
import edu.stanford.smi.protege.util.MessageError;
import edu.stanford.smi.protege.util.PropertyList;
import edu.uchsc.ccp.util.io.FileCopy;

public class ProjectUtil {

	public static Project openProject(String projectFileName) {
		List<?> errors = new ArrayList<Object>();
		Project project = new Project(projectFileName, errors);
		examineErrors(errors);
		return project;
	}

	public static void saveProject(Project project) {
		List<?> errors = new ArrayList<Object>();
		project.save(errors);
		examineErrors(errors);
	}

	private static void examineErrors(List<?> errors) {
		if (errors.size() > 0) {
			Object error = errors.get(0);
			if (error instanceof MessageError) {
				MessageError msgError = (MessageError) error;
				throw new IllegalArgumentException(msgError.getMessage(), msgError.getException());
			} else
				throw new IllegalArgumentException(error.toString());
		}
	}

	/**
	 * This method returns the default location of the knowtator project files:
	 * knowtator.pprj, knowtator.pins, knowtator.pont, new-project.pprj, etc.
	 * This method will only work from the context of a running Protege
	 * application - i.e. it will work if called from Knowtator.java but will
	 * not run if you are writing a script that runs from the command line. In
	 * such cases you will need to know the location of the knowtator project
	 * files to call methods such as {@link #saveProjectAs(Project, File, File)}
	 * , {@link #fixKnowtatorProjectPath(Project, File)}, or
	 * {@link #createNewProject(File, File)}. If you need to call one of these
	 * methods from a script, then you must find the project files on your file
	 * system. If you have knowtator installed, then you can pass in the
	 * knowtator plugin directory. If you have a local copy of the source code,
	 * then you can find them locally at "resources/knowtator.pprj".
	 * 
	 * @return the default location of the knowtator project files.
	 */
	public static File getKnowtatorProjectDirectory() {
		return new File(PluginUtilities.getPluginsDirectory().getPath() + File.separator + "edu.uchsc.ccp.knowtator");
	}

	/**
	 * @see #getKnowtatorProjectDirectory()
	 */
	public static void saveProjectAs(Project project, File projectFile) throws IOException {
		saveProjectAs(project, projectFile, getKnowtatorProjectDirectory());
	}

	/**
	 * This method provides a programmatic way to safely save a Knowtator
	 * project with a different name. Simply changing the name of the files on
	 * your file system is problematic for a number of reasons. Using Protege's
	 * "Save as" is also problematic for reasons described in
	 * {@link #fixKnowtatorProjectPath(Project, File)}.
	 * <p>
	 * NOTE: This method does not update the reference to your text source
	 * collection. If your text source collection is referenced by a relative
	 * path (by default) then you will need to update the reference.
	 * 
	 * 
	 * @param project
	 *            the project to save with a different name
	 * @param projectFile
	 *            the new file name of the project
	 * @param knowtatorProjectDirectory
	 *            see note for {@link #getKnowtatorProjectDirectory()}
	 * 
	 * @see #saveProjectAs(Project, File)
	 */
	public static void saveProjectAs(Project project, File projectFile, File knowtatorProjectDirectory)
			throws IOException {
		String projectName = projectFile.getName();
		File projectDirectory = projectFile.getParentFile();

		if (projectName.endsWith(".pprj")) {
			projectName = projectName.substring(0, projectName.length() - 5);
		}

		PropertyList sources = project.getSources();
		ClipsKnowledgeBaseFactory.setSourceFiles(sources, projectName + ".pont", projectName + ".pins");
		projectFile = new File(projectDirectory, projectName + ".pprj");
		project.setProjectURI(projectFile.toURI());
		saveProject(project);
		fixKnowtatorProjectPath(project, knowtatorProjectDirectory);
	}

    /**
     * @see #getKnowtatorProjectDirectory()
     */
	public static void fixKnowtatorProjectPath(Project project) throws IOException {
		fixKnowtatorProjectPath(project, getKnowtatorProjectDirectory());
	}

	/**
	 * Knowtator annotation projects require that the project knowtator.pprj is
	 * a directly included project - i.e. the annotation project
	 * <your-project>.pprj will include another project called knowtator.pprj.
	 * This project is found in the knowtator plugin directory and is copied to
	 * your projects directory when you initially set up knowtator and is
	 * referenced with a relative path. There are times when Protege will insert
	 * an absolute path into this reference to knowtator.pprj (usually when
	 * using Protege's "Save As..." menu option.) This often goes undetected
	 * until you try to open the project on a different computer in which case
	 * the absolute path is often no longer valid. It is possible (and easy -
	 * though annoying) to manually fix this problem by editing
	 * <your-project>.pprj. However, this method provides a way to do this
	 * programmatically.
	 * 
	 * @param project
	 * @param knowtatorProjectDirectory
	 *            - the name of the directory containing current knowtator
	 *            project files: knowtator.pprj, knowtator.pont, and
	 *            knowtator.pins - e.g. "C:\Program
	 *            Files\Protege_3.3.1\plugins\edu.uchsc.ccp.knowtator"
	 * @throws IOException
	 * @see #saveProject(Project)
	 */
	public static void fixKnowtatorProjectPath(Project project, File knowtatorProjectDirectory) throws IOException {
		KnowledgeBase kb = project.getKnowledgeBase();

		File annotationProjectDirectory = new File(project.getProjectDirectoryURI());
		copyKnowtatorFilesToProjectDirectory(annotationProjectDirectory, knowtatorProjectDirectory);

		Collection<URI> directIncludedProjects = kb.getProject().getDirectIncludedProjectURIs();
		Collection<URI> updatedIncludedProjects = new ArrayList<URI>();
		for (URI projectURI : directIncludedProjects) {
			if (!projectURI.getPath().endsWith("knowtator.pprj")) {
				updatedIncludedProjects.add(projectURI);
			}
		}

		File localKnowtatorPPRJ = new File(annotationProjectDirectory, "knowtator.pprj");

		updatedIncludedProjects.add(localKnowtatorPPRJ.toURI());

		project.setDirectIncludedProjectURIs(updatedIncludedProjects);
		saveProject(project);
	}

	
	/**
	 * @see #getKnowtatorProjectDirectory()
	 */
	public static void copyKnowtatorFilesToProjectDirectory(Project project) throws IOException {
		copyKnowtatorFilesToProjectDirectory(project, getKnowtatorProjectDirectory());
	}

	public static void copyKnowtatorFilesToProjectDirectory(Project project, File knowtatorProjectDirectory)
			throws IOException {
		copyKnowtatorFilesToProjectDirectory(new File(project.getProjectDirectoryURI()), knowtatorProjectDirectory);
	}

	public static void copyKnowtatorFilesToProjectDirectory(File projectDirectory, File knowtatorProjectDirectory)
			throws IOException {

		File knowtatorPPRJ = new File(knowtatorProjectDirectory, "knowtator.pprj");
		File knowtatorPONT = new File(knowtatorProjectDirectory, "knowtator.pont");
		File knowtatorPINS = new File(knowtatorProjectDirectory, "knowtator.pins");

		File localKnowtatorPPRJ = new File(projectDirectory, "knowtator.pprj");
		File localKnowtatorPONT = new File(projectDirectory, "knowtator.pont");
		File localKnowtatorPINS = new File(projectDirectory, "knowtator.pins");

		FileCopy.copyFile(knowtatorPPRJ, localKnowtatorPPRJ);
		FileCopy.copyFile(knowtatorPONT, localKnowtatorPONT);
		FileCopy.copyFile(knowtatorPINS, localKnowtatorPINS);

	}

	/**
	 * @see #getKnowtatorProjectDirectory()
	 */
	public static Project createNewProject(File newProjectFile) throws IOException {
		return createNewProject(getKnowtatorProjectDirectory(), newProjectFile);
	}
	
	public static Project createNewProject(File knowtatorProjectDirectory, File newProjectFile) throws IOException {

		File newProjectDirectory = newProjectFile.getParentFile();

		copyKnowtatorFilesToProjectDirectory(newProjectDirectory, knowtatorProjectDirectory);
		
		File newPPRJ = new File(knowtatorProjectDirectory, "new-project.pprj");
		File newPONT = new File(knowtatorProjectDirectory, "new-project.pont");
		File newPINS = new File(knowtatorProjectDirectory, "new-project.pins");

		File localNewPPRJ = new File(newProjectDirectory, "new-project.pprj");
		File localNewPONT = new File(newProjectDirectory, "new-project.pont");
		File localNewPINS = new File(newProjectDirectory, "new-project.pins");

		FileCopy.copyFile(newPPRJ, localNewPPRJ);
		FileCopy.copyFile(newPONT, localNewPONT);
		FileCopy.copyFile(newPINS, localNewPINS);

		Project newProject = openProject(localNewPPRJ.getPath());
		saveProjectAs(newProject, newProjectFile, knowtatorProjectDirectory);

		return newProject;
	}
}
