/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.ui.benchmark;


import junit.framework.Test;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public class SyncTests extends BenchmarkTest {
	private static final int FILE_SIZE_MEAN = 16384;
	private static final int FILE_SIZE_VARIANCE = 0;
	private static final int PROB_BINARY = 0;
	
	private static final String ADDED_GROUP_SUFFIX = "AddedFiles";
	private static final String REMOVED_GROUP_SUFFIX = "RemovedFiles";
	private static final String MODIFIED_GROUP_SUFFIX = "ModifiedFiles";
    private static final String[] PERFORMANCE_GROUPS = new String[] {ADDED_GROUP_SUFFIX, MODIFIED_GROUP_SUFFIX, REMOVED_GROUP_SUFFIX};
	
	public SyncTests() {
		super();
	}

	public SyncTests(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(SyncTests.class);
	}
    
	public void testSync10() throws Exception {
		runTestSync(10, "CVS Synchronize 10", false);
	}

	public void testSync100() throws Exception {
		runTestSync(100, "CVS Synchronize 100", false);
	}
	
	public void testSync100Global() throws Exception {
		runTestSync(100, "CVS Synchronize", true);
	}

	protected IProject setupOutProject() throws Exception {
		IProject project = createUniqueProject(BenchmarkTestSetup.SMALL_ZIP_FILE);
		shareProject(project);
		return project;
	}
	
	/**
	 * Runs a sequence of operations for the synchronizer tests.
	 * A parallel project is used to generate incoming changes.
	 */
	protected void runTestSync(int size, String globalName, boolean global) throws Exception {
	    setupGroups(PERFORMANCE_GROUPS, globalName, global);
	    for (int i = 0; i < BenchmarkTestSetup.LOOP_COUNT; i++) {
			final SequenceGenerator gen = new SequenceGenerator();
	
			// setup out project then move it out of the way
			IProject outProject = setupOutProject();
			String moduleName = outProject.getName();
			BenchmarkUtils.renameResource(outProject, moduleName + "out");
			outProject = BenchmarkUtils.getProject(moduleName + "out");
	
			// setup in project
			IProject inProject = BenchmarkUtils.getProject(moduleName);
			checkoutProject(inProject, moduleName, null);
			
			/*** outgoing and incoming changes ***/
			startGroup(ADDED_GROUP_SUFFIX);
			BenchmarkUtils.createRandomDeepFiles(gen, outProject, size, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY);
			syncCommitResources(new IResource[] { outProject }, "");
			syncUpdateResources(new IResource[] { inProject });
			endGroup();
			
			startGroup(MODIFIED_GROUP_SUFFIX);
			BenchmarkUtils.modifyRandomDeepFiles(gen, outProject, size);
			syncCommitResources(new IResource[] { outProject }, "");
			syncUpdateResources(new IResource[] { inProject });
			endGroup();
	
			startGroup(REMOVED_GROUP_SUFFIX);
			BenchmarkUtils.deleteRandomDeepFiles(gen, outProject, size);
			syncCommitResources(new IResource[] { outProject }, "");
			syncUpdateResources(new IResource[] { inProject });
			endGroup();
        }
	    commitGroups(global);
	}
}
