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
		// test sync on project with no changes
		IProject project = setupOutProject();
		startGroup("test sync with no changes");
		syncCommitResources(new IResource[] { project }, "");
		endGroup();
	}

    public void testSync1() throws Exception {
		runTestSync(1);
	}

	public void testSync10() throws Exception {
		runTestSync(10);
	}

	public void testSync100() throws Exception {
		runTestSync(100);
	}

	protected IProject setupOutProject() throws Exception {
	    disableLog();
		IProject project = createUniqueProject(BenchmarkTestSetup.SMALL_ZIP_FILE);
		shareProject(project);
		enableLog();
		return project;
	}
	
	/**
	 * Runs a sequence of operations for the synchronizer tests.
	 * A parallel project is used to generate incoming changes.
	 */
	protected void runTestSync(int size) throws Exception {
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
		startGroup("synchronize " + size + " added file(s)");
		BenchmarkUtils.createRandomDeepFiles(gen, outProject, size, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY);
		startGroup("as outgoing changes");
		syncCommitResources(new IResource[] { outProject }, "");
		endGroup();
		startGroup("as incoming changes");
		syncUpdateResources(new IResource[] { inProject });
		endGroup();
		endGroup();
		
		startGroup("synchronize " + size + " modified file(s)");
		BenchmarkUtils.modifyRandomDeepFiles(gen, outProject, size);
		startGroup("as outgoing changes");
		syncCommitResources(new IResource[] { outProject }, "");
		endGroup();
		startGroup("as incoming changes");
		syncUpdateResources(new IResource[] { inProject });
		endGroup();
		endGroup();

		startGroup("synchronize " + size + " removed file(s)");
		BenchmarkUtils.deleteRandomDeepFiles(gen, outProject, size);
		startGroup("as outgoing changes");
		syncCommitResources(new IResource[] { outProject }, "");
		endGroup();
		startGroup("as incoming changes");
		syncUpdateResources(new IResource[] { inProject });
		endGroup();
		endGroup();
	}
}
