package org.eclipse.team.tests.ccvs.ui.benchmark;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.tests.ccvs.ui.CVSUITestCase;
import org.eclipse.team.tests.ccvs.ui.LoggingTestResult;
import org.eclipse.team.tests.ccvs.ui.SequenceGenerator;
import org.eclipse.team.tests.ccvs.ui.Util;

public class WorkflowTests extends CVSUITestCase {
	private int FILE_SIZE_MEAN = 16384;
	private int FILE_SIZE_VARIANCE = 12288;
	private int PROB_BINARY = 5;
	public WorkflowTests(String name) {
		super(name);
	}
	public WorkflowTests() {
		super("");
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
		Util.renameResource(outProject, moduleName + "out");
		outProject = Util.getProject(moduleName + "out");
		
		// test initial project checkout
		startGroup("test initial project checkout");
		actionCheckoutProjects(new String[] { moduleName }, new CVSTag[] { new CVSTag() });
		endGroup();
		IProject inProject = Util.getProject(moduleName);
		
		// test scenarios
		startGroup("test incoming and outgoing change scenarios");
		startGroup("adding a new component - localized additions and some changes");
		Util.modifyRandomDeepFiles(gen, outProject, 5);
		Util.touchRandomDeepFiles(gen, outProject, 2);
		IFolder componentRoot = Util.createRandomDeepFolder(gen, outProject);
		Util.createRandomDeepFiles(gen, componentRoot, 12, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY);
		syncCommitResources(new IResource[] { outProject }, null, "");
		endGroup();		

		startGroup("catching up to a new component - localized additions and some changes");
		syncUpdateResources(new IResource[] { inProject }, null);
		endGroup();

		startGroup("fixing a bug - localized changes");
		Util.modifyRandomDeepFiles(gen, componentRoot, 2);
		Util.touchRandomDeepFiles(gen, componentRoot, 2);
		syncCommitResources(new IResource[] { outProject }, null, "");
		endGroup();

		startGroup("catching up to a bug fix - localized changes");
		syncUpdateResources(new IResource[] { inProject }, null);
		endGroup();
		
		startGroup("moving a package - scattered changes, files moved");
		Util.modifyRandomDeepFiles(gen, outProject, 5);        // a few scattered changes
		Util.modifyRandomDeepFiles(gen, componentRoot, 12); // changes to "package" stmt
		Util.renameResource(componentRoot, Util.makeUniqueName(gen, "folder", null));
		syncCommitResources(new IResource[] { outProject }, null, "");
		endGroup();

		startGroup("catching up to a moved package - scattered changes, files moved");
		syncUpdateResources(new IResource[] { inProject }, null);
		endGroup();
		
		startGroup("big refactoring - scattered changes, files renamed and balanced additions/deletions");		
		Util.deleteRandomDeepFiles(gen, outProject, 4);  // some stuff deleted
		Util.modifyRandomDeepFiles(gen, outProject, 20); // many scattered changes
		Util.renameRandomDeepFiles(gen, outProject, 5);  // renamed some stuff
		Util.createRandomDeepFiles(gen, outProject, 4, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY);  // some new stuff added
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
		Util.deleteRandomDeepFiles(gen, inProject, 4); // some stuff locally deleted
		Util.modifyRandomDeepFiles(gen, inProject, 6); // a few unimportant changes to forget
		Util.createRandomDeepFiles(gen, inProject, 4, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY); // some new work to abandon
		actionReplaceWithRemote(new IResource[] { inProject });
		endGroup();

		startGroup("no local dirty files, many remote changes");
		// e.g. returning from a long vacation
		Util.deleteRandomDeepFiles(gen, outProject, 10); // some components obsoleted
		Util.modifyRandomDeepFiles(gen, outProject, 42); // many changes
		Util.renameRandomDeepFiles(gen, outProject, 8);  // evidence of some refactoring
		Util.createRandomDeepFiles(gen, outProject, 10, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY); // a few new components added
		disableLog();
		syncCommitResources(new IResource[] { outProject }, null, "");
		enableLog();
		actionReplaceWithRemote(new IResource[] { inProject });
		endGroup();
		endGroup();
	}
}
