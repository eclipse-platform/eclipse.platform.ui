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

import java.io.File;

import junit.framework.Test;

import org.eclipse.core.resources.*;
import org.eclipse.team.internal.ccvs.core.CVSTag;

public class WorkflowTests extends BenchmarkTest {
	private int FILE_SIZE_MEAN = 16384;
	private int FILE_SIZE_VARIANCE = 12288;
	private int PROB_BINARY = 5;

	public WorkflowTests() {
		super();
	}

	public WorkflowTests(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(WorkflowTests.class);
	}

	public void testBigWorkflow() throws Exception {
		runWorkflowTests("testBig", BenchmarkTestSetup.BIG_ZIP_FILE);
	}
	
	public void testSmallWorkflow() throws Exception {
		runWorkflowTests("testSmall", BenchmarkTestSetup.SMALL_ZIP_FILE);
	}

	public void testTinyWorkflow() throws Exception {
		runWorkflowTests("testTiny", BenchmarkTestSetup.TINY_ZIP_FILE);
	}

	/**
	 * Runs a series of incoming and outgoing workflow-related tests.
	 */
	protected void runWorkflowTests(String name, File initialContents) throws Exception {
		final SequenceGenerator gen = new SequenceGenerator();
		IProject outProject = createAndImportProject(name, initialContents);
		
		// test project sharing
		startGroup("test initial project commit");
		shareProject(outProject);
		endGroup();
		
		// move the project out of the way
		String moduleName = outProject.getName();
		BenchmarkUtils.renameResource(outProject, moduleName + "out");
		outProject = BenchmarkUtils.getProject(moduleName + "out");
		
		// test initial project checkout
		IProject inProject = BenchmarkUtils.getProject(moduleName);
		startGroup("test initial project checkout");
		checkoutProject(inProject, moduleName, null);
		endGroup();
		
		
		// test scenarios
		startGroup("test incoming and outgoing change scenarios");
		startGroup("adding a new component - localized additions and some changes");
		BenchmarkUtils.modifyRandomDeepFiles(gen, outProject, 5);
		BenchmarkUtils.touchRandomDeepFiles(gen, outProject, 2);
		IFolder componentRoot = BenchmarkUtils.createRandomDeepFolder(gen, outProject);
		BenchmarkUtils.createRandomDeepFiles(gen, componentRoot, 12, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY);
		syncCommitResources(new IResource[] { outProject }, "");
		endGroup();		

		startGroup("catching up to a new component - localized additions and some changes");
		syncUpdateResources(new IResource[] { inProject });
		endGroup();

		startGroup("fixing a bug - localized changes");
		BenchmarkUtils.modifyRandomDeepFiles(gen, componentRoot, 2);
		BenchmarkUtils.touchRandomDeepFiles(gen, componentRoot, 2);
		syncCommitResources(new IResource[] { outProject }, "");
		endGroup();

		startGroup("catching up to a bug fix - localized changes");
		syncUpdateResources(new IResource[] { inProject });
		endGroup();
		
		startGroup("moving a package - scattered changes, files moved");
		BenchmarkUtils.modifyRandomDeepFiles(gen, outProject, 5);        // a few scattered changes
		BenchmarkUtils.modifyRandomDeepFiles(gen, componentRoot, 12); // changes to "package" stmt
		BenchmarkUtils.renameResource(componentRoot, BenchmarkUtils.makeUniqueName(gen, "folder", null));
		syncCommitResources(new IResource[] { outProject }, "");
		endGroup();

		startGroup("catching up to a moved package - scattered changes, files moved");
		syncUpdateResources(new IResource[] { inProject });
		endGroup();
		
		startGroup("big refactoring - scattered changes, files renamed and balanced additions/deletions");		
		BenchmarkUtils.deleteRandomDeepFiles(gen, outProject, 4);  // some stuff deleted
		BenchmarkUtils.modifyRandomDeepFiles(gen, outProject, 20); // many scattered changes
		BenchmarkUtils.renameRandomDeepFiles(gen, outProject, 5);  // renamed some stuff
		BenchmarkUtils.createRandomDeepFiles(gen, outProject, 4, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY);  // some new stuff added
		syncCommitResources(new IResource[] { outProject }, "");
		endGroup();

		startGroup("catching up to a big refactoring - scattered changes, files renamed and balanced additions/deletions");		
		syncUpdateResources(new IResource[] { inProject });
		endGroup();
		endGroup();

		// test tagging a project
		startGroup("tag project");
		tagProject(outProject, new CVSTag("v101", CVSTag.VERSION), false);
		endGroup();

		// replace with remote contents
		startGroup("test replace with remote contents scenarios");
		startGroup("no local dirty files, no remote changes");
		replace(new IResource[] { inProject }, null, true);
		endGroup();

		startGroup("abandoning some local work, no remote changes");
		BenchmarkUtils.deleteRandomDeepFiles(gen, inProject, 4); // some stuff locally deleted
		BenchmarkUtils.modifyRandomDeepFiles(gen, inProject, 6); // a few unimportant changes to forget
		BenchmarkUtils.createRandomDeepFiles(gen, inProject, 4, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY); // some new work to abandon
		replace(new IResource[] { inProject }, null, true);
		endGroup();

		startGroup("no local dirty files, many remote changes");
		// e.g. returning from a long vacation
		BenchmarkUtils.deleteRandomDeepFiles(gen, outProject, 10); // some components obsoleted
		BenchmarkUtils.modifyRandomDeepFiles(gen, outProject, 42); // many changes
		BenchmarkUtils.renameRandomDeepFiles(gen, outProject, 8);  // evidence of some refactoring
		BenchmarkUtils.createRandomDeepFiles(gen, outProject, 10, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY); // a few new components added
		disableLog();
		syncCommitResources(new IResource[] { outProject }, "");
		enableLog();
		replace(new IResource[] { inProject }, null, true);
		endGroup();
		endGroup();
	}
}
