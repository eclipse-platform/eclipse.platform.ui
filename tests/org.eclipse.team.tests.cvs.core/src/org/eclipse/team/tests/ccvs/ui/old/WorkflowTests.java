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
package org.eclipse.team.tests.ccvs.ui.old;


import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.internal.ccvs.core.CVSTag;

public class WorkflowTests extends CVSUITestCase {
	private int FILE_SIZE_MEAN = 16384;
	private int FILE_SIZE_VARIANCE = 12288;
	private int PROB_BINARY = 5;
	public WorkflowTests(Test test) {
		super(test);
	}

	public static Test suite() {
    	return new BenchmarkTestSetup(new TestSuite(WorkflowTests.class));
		//return new BenchmarkTestSetup(new WorkflowTests("testTinyWorkflow"));
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
		startGroup("test project sharing");
		actionShareProject(outProject);
		endGroup();
		
		// test initial project commit
		startGroup("test initial project commit");
		syncCommitResources(new IResource[] { outProject }, null, "initial");
		endGroup();
		
		// move the project out of the way
		String moduleName = outProject.getName();
		Utils.renameResource(outProject, moduleName + "out");
		outProject = Utils.getProject(moduleName + "out");
		
		// test initial project checkout
		startGroup("test initial project checkout");
		actionCheckoutProjects(new String[] { moduleName }, new CVSTag[] { new CVSTag() });
		endGroup();
		IProject inProject = Utils.getProject(moduleName);
		
		// test scenarios
		startGroup("test incoming and outgoing change scenarios");
		startGroup("adding a new component - localized additions and some changes");
		Utils.modifyRandomDeepFiles(gen, outProject, 5);
		Utils.touchRandomDeepFiles(gen, outProject, 2);
		IFolder componentRoot = Utils.createRandomDeepFolder(gen, outProject);
		Utils.createRandomDeepFiles(gen, componentRoot, 12, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY);
		syncCommitResources(new IResource[] { outProject }, null, "");
		endGroup();		

		startGroup("catching up to a new component - localized additions and some changes");
		syncUpdateResources(new IResource[] { inProject }, null);
		endGroup();

		startGroup("fixing a bug - localized changes");
		Utils.modifyRandomDeepFiles(gen, componentRoot, 2);
		Utils.touchRandomDeepFiles(gen, componentRoot, 2);
		syncCommitResources(new IResource[] { outProject }, null, "");
		endGroup();

		startGroup("catching up to a bug fix - localized changes");
		syncUpdateResources(new IResource[] { inProject }, null);
		endGroup();
		
		startGroup("moving a package - scattered changes, files moved");
		Utils.modifyRandomDeepFiles(gen, outProject, 5);        // a few scattered changes
		Utils.modifyRandomDeepFiles(gen, componentRoot, 12); // changes to "package" stmt
		Utils.renameResource(componentRoot, Utils.makeUniqueName(gen, "folder", null));
		syncCommitResources(new IResource[] { outProject }, null, "");
		endGroup();

		startGroup("catching up to a moved package - scattered changes, files moved");
		syncUpdateResources(new IResource[] { inProject }, null);
		endGroup();
		
		startGroup("big refactoring - scattered changes, files renamed and balanced additions/deletions");		
		Utils.deleteRandomDeepFiles(gen, outProject, 4);  // some stuff deleted
		Utils.modifyRandomDeepFiles(gen, outProject, 20); // many scattered changes
		Utils.renameRandomDeepFiles(gen, outProject, 5);  // renamed some stuff
		Utils.createRandomDeepFiles(gen, outProject, 4, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY);  // some new stuff added
		syncCommitResources(new IResource[] { outProject }, null, "");
		endGroup();

		startGroup("catching up to a big refactoring - scattered changes, files renamed and balanced additions/deletions");		
		syncUpdateResources(new IResource[] { inProject }, null);
		endGroup();
		endGroup();

		// test tagging a project
		startGroup("tag project");
		actionCVSTag(new IResource[] { outProject }, "v101");
		endGroup();

		// replace with remote contents
		startGroup("test replace with remote contents scenarios");
		startGroup("no local dirty files, no remote changes");
		actionReplaceWithRemote(new IResource[] { inProject });
		endGroup();

		startGroup("abandoning some local work, no remote changes");
		Utils.deleteRandomDeepFiles(gen, inProject, 4); // some stuff locally deleted
		Utils.modifyRandomDeepFiles(gen, inProject, 6); // a few unimportant changes to forget
		Utils.createRandomDeepFiles(gen, inProject, 4, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY); // some new work to abandon
		actionReplaceWithRemote(new IResource[] { inProject });
		endGroup();

		startGroup("no local dirty files, many remote changes");
		// e.g. returning from a long vacation
		Utils.deleteRandomDeepFiles(gen, outProject, 10); // some components obsoleted
		Utils.modifyRandomDeepFiles(gen, outProject, 42); // many changes
		Utils.renameRandomDeepFiles(gen, outProject, 8);  // evidence of some refactoring
		Utils.createRandomDeepFiles(gen, outProject, 10, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY); // a few new components added
		disableLog();
		syncCommitResources(new IResource[] { outProject }, null, "");
		enableLog();
		actionReplaceWithRemote(new IResource[] { inProject });
		endGroup();
		endGroup();
	}
}
