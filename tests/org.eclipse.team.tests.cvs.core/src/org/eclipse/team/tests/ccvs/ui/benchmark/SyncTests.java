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
	
	private static final String NO_CHANGES_GROUP_SUFFIX = "NoChanges";
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
    
	public void testSync0() throws Exception {
	    setupGroups(new String[] {NO_CHANGES_GROUP_SUFFIX} );
		IProject project = setupOutProject();
		for (int i = 0; i < BenchmarkTestSetup.LOOP_COUNT; i++) {
			startGroup(NO_CHANGES_GROUP_SUFFIX);
			syncCommitResources(new IResource[] { project }, "");
			endGroup();
        }
		commitGroups();
	}

    public void testSync1() throws Exception {
		runTestSync(1, null);
	}

	public void testSync10() throws Exception {
		runTestSync(10, null);
	}

	public void testSync100() throws Exception {
		runTestSync(100, SYNC_GROUP);
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
	protected void runTestSync(int size, String globalName) throws Exception {
	    setupGroups(PERFORMANCE_GROUPS, globalName);
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
	    commitGroups();
	}
}
