package org.eclipse.team.tests.ccvs.ui.benchmark;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.tests.ccvs.ui.CVSUITestCase;
import org.eclipse.team.tests.ccvs.ui.Util;
import org.eclipse.team.tests.ccvs.ui.SequenceGenerator;
import org.eclipse.team.tests.ccvs.ui.Util;

public class SyncTests extends CVSUITestCase {
	private int FILE_SIZE_MEAN = 20000;
	private int FILE_SIZE_VARIANCE = 0;
	private int PROB_BINARY = 0;
	private IProject project;
	private IProject parallelProject;
	
	public SyncTests(String name) {
		super(name);
	}
	public SyncTests() {
		super("");
	}

	protected void setUp() throws Exception {
		super.setUp();
		project = createAndImportProject("testSync", BenchmarkTestSetup.TINY_ZIP_FILE);
		startGroup("initial project commit");
		actionShareProject(project);
		syncCommitResources(new IResource[] { project }, null, getGroupName());
		endGroup();
		parallelProject = Util.createParallelProject(
			testRepository.getLocation(), project.getName());
	}

	public static Test suite() {
    	return new BenchmarkTestSetup(new TestSuite(SyncTests.class));
	}

	public void testSync0() throws Exception {
		// test sync on project with no changes
		startGroup("test sync with no changes");
		syncCommitResources(new IResource[] { project }, null, getGroupName());
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

	/**
	 * Runs a sequence of operations for the synchronizer tests.
	 * A parallel project is used to generate incoming changes.
	 */
	protected void runTestSync(int size) throws Exception {
		final SequenceGenerator gen = new SequenceGenerator();

		/*** outgoing changes ***/
		startGroup("checking in " + size + " added file(s)");
		Util.createRandomDeepFiles(gen, project, size, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY);
		syncCommitResources(new IResource[] { project }, null, getGroupName());
		endGroup();
		startGroup("checking in " + size + " modified file(s)");
		Util.modifyRandomDeepFiles(gen, project, size);
		syncCommitResources(new IResource[] { project }, null, getGroupName());
		endGroup();
		startGroup("checking in " + size + " renamed file(s)");
		Util.renameRandomDeepFiles(gen, project, size);
		syncCommitResources(new IResource[] { project }, null, getGroupName());
		endGroup();
		startGroup("checking in " + size + " touched file(s)");
		Util.touchRandomDeepFiles(gen, project, size);
		syncCommitResources(new IResource[] { project }, null, getGroupName());
		endGroup();
		startGroup("checking in " + size + " removed file(s)");
		Util.deleteRandomDeepFiles(gen, project, size);
		syncCommitResources(new IResource[] { project }, null, getGroupName());
		endGroup();

		/*** incoming changes ***/
		Util.checkoutParallelProject(parallelProject);
		startGroup("checking out " + size + " added file(s)");
		Util.createRandomDeepFiles(gen, project, size, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY);
		Util.checkinParallelProject(parallelProject, getGroupName());
		syncUpdateResources(new IResource[] { project }, null);
		endGroup();
		startGroup("checking out " + size + " modified file(s)");
		Util.modifyRandomDeepFiles(gen, parallelProject, size);
		Util.checkinParallelProject(parallelProject, getGroupName());
		syncUpdateResources(new IResource[] { project }, null);
		endGroup();
		startGroup("checking out " + size + " renamed file(s)");
		Util.renameRandomDeepFiles(gen, parallelProject, size);
		Util.checkinParallelProject(parallelProject, getGroupName());
		syncUpdateResources(new IResource[] { project }, null);
		endGroup();
		startGroup("checking out " + size + " removed file(s)");
		Util.deleteRandomDeepFiles(gen, parallelProject, size);
		Util.checkinParallelProject(parallelProject, getGroupName());
		syncUpdateResources(new IResource[] { project }, null);
		endGroup();
	}
}
