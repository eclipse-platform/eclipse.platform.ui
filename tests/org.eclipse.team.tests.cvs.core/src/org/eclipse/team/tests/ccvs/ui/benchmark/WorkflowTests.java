package org.eclipse.team.tests.ccvs.ui.benchmark;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.tests.ccvs.ui.CVSUITestCase;
import org.eclipse.team.tests.ccvs.ui.Util;
import org.eclipse.team.tests.ccvs.ui.SequenceGenerator;
import org.eclipse.team.tests.ccvs.ui.Util;

public class WorkflowTests extends CVSUITestCase {
	private int FILE_SIZE_MEAN = 20000;
	private int FILE_SIZE_VARIANCE = 10000;
	private int PROB_BINARY = 5;
	public WorkflowTests(String name) {
		super(name);
	}
	public WorkflowTests() {
		super("");
	}

	public static Test suite() {
    	return new BenchmarkTestSetup(new TestSuite(WorkflowTests.class));
	}

	public void testBigWorkflow() throws Exception {
		runWorkflowTests(createAndImportProject("testBig", BenchmarkTestSetup.BIG_ZIP_FILE));
	}
	
	public void testSmallWorkflow() throws Exception {
		runWorkflowTests(createAndImportProject("testSmall", BenchmarkTestSetup.SMALL_ZIP_FILE));
	}

	public void testTinyWorkflow() throws Exception {
		runWorkflowTests(createAndImportProject("testTiny", BenchmarkTestSetup.TINY_ZIP_FILE));
	}
	
	/**
	 * Runs a series of workflow-related tests on a project.
	 */
	protected void runWorkflowTests(IProject project) throws Exception {
		final SequenceGenerator gen = new SequenceGenerator();
		// test project sharing
		startGroup("test project sharing");
		actionShareProject(project);
		endGroup();
		
		// test initial project commit
		startGroup("test initial project commit");
		syncCommitResources(new IResource[] { project }, null, getGroupName());
		endGroup();

		// test initial project checkout
		deleteProject(project);
		startGroup("test initial project checkout");
		actionCheckoutProjects(new String[] { project.getName() }, new CVSTag[] { new CVSTag() });
		endGroup();
		project = Util.createProject(project.getName());

		// create a project for testing incoming changes
		IProject parallelProject = Util.createParallelProject(
			testRepository.getLocation(), project.getName());

		// test typical outgoing workflows
		runOutgoingWorkflowTests(gen, project, parallelProject);

		// test tagging a project
		startGroup("tag project");
		actionCVSTag(new IResource[] { project }, "v101");
		endGroup();
		
		// test typical incoming workflows
		runIncomingWorkflowTests(gen, project, parallelProject);
	}
	
	protected void runOutgoingWorkflowTests(SequenceGenerator gen, IProject project, IProject parallelProject)
		throws Exception {
		startGroup("test outgoing change scenarios");
		startGroup("adding a new component - localized additions and some changes");
		Util.modifyRandomDeepFiles(gen, project, 5);
		Util.touchRandomDeepFiles(gen, project, 2);
		IFolder componentRoot = Util.createRandomDeepFolder(gen, project);
		Util.createRandomDeepFiles(gen, componentRoot, 12, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY);
		syncCommitResources(new IResource[] { project }, null, getGroupName());
		endGroup();		

		startGroup("fixing a bug - localized changes");
		Util.modifyRandomDeepFiles(gen, componentRoot, 2);
		Util.touchRandomDeepFiles(gen, componentRoot, 2);
		syncCommitResources(new IResource[] { project }, null, getGroupName());
		endGroup();

//		beginGroup("moving a package - scattered changes, files moved");
//		FileUtil.modifyRandomDeepFiles(gen, project, 5);        // a few scattered changes
//		FileUtil.modifyRandomDeepFiles(gen, componentRoot, 12); // changes to "package" stmt		
//		componentRoot.move(new Path(FileUtil.makeUniqueName("folder", null, project)), true, null);
//		syncCommitResources(new IResource[] { project }, null, getGroupName());
//		endGroup();
		
		startGroup("big refactoring - scattered changes, files renamed and balanced additions/deletions");		
		Util.deleteRandomDeepFiles(gen, project, 4);  // some stuff deleted
		Util.modifyRandomDeepFiles(gen, project, 20); // many scattered changes
		Util.renameRandomDeepFiles(gen, project, 5);  // renamed some stuff
		Util.createRandomDeepFiles(gen, project, 4, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY);  // some new stuff added
		syncCommitResources(new IResource[] { project }, null, getGroupName());
		endGroup();
		endGroup();
	}
	
	protected void runIncomingWorkflowTests(SequenceGenerator gen, IProject project, IProject parallelProject)
		throws Exception {
		startGroup("test incoming change scenarios");
		Util.checkoutParallelProject(parallelProject);

		startGroup("catching up to a new component - localized additions and some changes");
		Util.modifyRandomDeepFiles(gen, parallelProject, 5);
		Util.touchRandomDeepFiles(gen, parallelProject, 2);
		IFolder componentRoot = Util.createRandomDeepFolder(gen, parallelProject);
		Util.createRandomDeepFiles(gen, componentRoot, 12, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY);
		Util.checkinParallelProject(parallelProject, getGroupName());
		syncUpdateResources(new IResource[] { project }, null);
		endGroup();
		
		startGroup("catching up to a bug fix - localized changes");
		Util.modifyRandomDeepFiles(gen, componentRoot, 2);
		Util.touchRandomDeepFiles(gen, componentRoot, 2);
		Util.checkinParallelProject(parallelProject, getGroupName());
		syncUpdateResources(new IResource[] { project }, null);
		endGroup();

//		beginGroup("catching up to a moved package - scattered changes, files moved");
//		FileUtil.modifyRandomDeepFiles(gen, parallelProject, 5); // a few scattered changes
//		FileUtil.modifyRandomDeepFiles(gen, componentRoot, 12); // changes to "package" stmt		
//		componentRoot.move(new Path(FileUtil.makeUniqueName("folder", null, parallelProject)), true, null);
//		Util.checkinParallelProject(parallelProject, getGroupName());
//		syncUpdateResources(new IResource[] { project }, null);
//		endGroup();
		
		startGroup("catching up to a big refactoring - scattered changes, files renamed and balanced additions/deletions");		
		Util.deleteRandomDeepFiles(gen, parallelProject, 4);  // some stuff deleted
		Util.modifyRandomDeepFiles(gen, parallelProject, 20); // many scattered changes
		Util.renameRandomDeepFiles(gen, parallelProject, 5);  // renamed some stuff
		Util.createRandomDeepFiles(gen, parallelProject, 4, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY);  // some new stuff added
		Util.checkinParallelProject(parallelProject, getGroupName());
		syncUpdateResources(new IResource[] { project }, null);
		endGroup();

		startGroup("replacing with remote contents - no local dirty files, no remote changes");
		actionReplaceWithRemote(new IResource[] { project});
		endGroup();

		startGroup("replacing with remote contents - abandoning some local work, no remote changes");
		Util.deleteRandomDeepFiles(gen, project, 4); // some stuff locally deleted
		Util.modifyRandomDeepFiles(gen, project, 6); // a few unimportant changes to forget
		Util.createRandomDeepFiles(gen, project, 2, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY); // some new work to abandon
		actionReplaceWithRemote(new IResource[] { project});
		endGroup();

		startGroup("replacing with remote contents - no local dirty files, many remote changes");
		// e.g. returning from a long vacation
		Util.deleteRandomDeepFiles(gen, parallelProject, 10); // some components obsoleted
		Util.modifyRandomDeepFiles(gen, parallelProject, 42); // many changes
		Util.renameRandomDeepFiles(gen, parallelProject, 8);  // evidence of some refactoring
		Util.createRandomDeepFiles(gen, parallelProject, 10, FILE_SIZE_MEAN, FILE_SIZE_VARIANCE, PROB_BINARY); // a few new components added
		Util.checkinParallelProject(parallelProject, getGroupName());
		actionReplaceWithRemote(new IResource[] { project});
		endGroup();
		endGroup();
	}


}
